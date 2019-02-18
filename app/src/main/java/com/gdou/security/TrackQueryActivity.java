package com.gdou.security;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.analysis.DrivingBehaviorRequest;
import com.baidu.trace.api.analysis.DrivingBehaviorResponse;
import com.baidu.trace.api.analysis.HarshAccelerationPoint;
import com.baidu.trace.api.analysis.HarshBreakingPoint;
import com.baidu.trace.api.analysis.HarshSteeringPoint;
import com.baidu.trace.api.analysis.OnAnalysisListener;
import com.baidu.trace.api.analysis.SpeedingInfo;
import com.baidu.trace.api.analysis.SpeedingPoint;
import com.baidu.trace.api.analysis.StayPoint;
import com.baidu.trace.api.analysis.StayPointRequest;
import com.baidu.trace.api.analysis.StayPointResponse;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.Point;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TransportMode;
import com.gdou.security.dialog.TrackAnalysisDialog;
import com.gdou.security.dialog.TrackAnalysisInfoLayout;
import com.gdou.security.utils.BitmapUtil;
import com.gdou.security.utils.CommonUtil;
import com.gdou.security.utils.Constants;
import com.gdou.security.utils.HttpUtil;
import com.gdou.security.utils.MapUtil;
import com.gdou.security.utils.ViewUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 轨迹查询
 */
public class TrackQueryActivity extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, BaiduMap.OnMarkerClickListener {

    private TrackApplication trackApp = null;

    private ViewUtil viewUtil = null;

    /**
     * 地图工具
     */
    private MapUtil mapUtil = null;

    /**
     * 历史轨迹请求
     */
    private HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest();

    /**
     * 轨迹监听器（用于接收历史轨迹回调）
     */
    private OnTrackListener mTrackListener = null;

    /**
     * 轨迹分析对话框
     */
    private TrackAnalysisDialog trackAnalysisDialog = null;

    /**
     * 轨迹分析详情框布局
     */
    private TrackAnalysisInfoLayout trackAnalysisInfoLayout = null;

    /**
     * 当前轨迹分析详情框对应的marker
     */
    private Marker analysisMarker = null;

    /**
     * 驾驶行为请求
     */
    private DrivingBehaviorRequest drivingBehaviorRequest = new DrivingBehaviorRequest();

    /**
     * 停留点请求
     */
    private StayPointRequest stayPointRequest = new StayPointRequest();

    /**
     * 轨迹分析监听器
     */
    private OnAnalysisListener mAnalysisListener = null;

    /**
     * 查询轨迹的开始时间
     */
    private long startTime = CommonUtil.getCurrentTime();

    /**
     * 查询轨迹的结束时间
     */
    private long endTime = CommonUtil.getCurrentTime();

    /**
     * 轨迹点集合
     */
    private List<LatLng> trackPoints = new ArrayList<>();

    /**
     * 轨迹分析  超速点集合
     */
    private List<Point> speedingPoints = new ArrayList<>();

    /**
     * 轨迹分析  急加速点集合
     */
    private List<Point> harshAccelPoints = new ArrayList<>();

    /**
     * 轨迹分析  急刹车点集合
     */
    private List<Point> harshBreakingPoints = new ArrayList<>();

    /**
     * 轨迹分析  急转弯点集合
     */
    private List<Point> harshSteeringPoints = new ArrayList<>();

    /**
     * 轨迹分析  停留点集合
     */
    private List<Point> stayPoints = new ArrayList<>();

    /**
     * 轨迹分析 超速点覆盖物集合
     */
    private List<Marker> speedingMarkers = new ArrayList<>();

    /**
     * 轨迹分析 急加速点覆盖物集合
     */
    private List<Marker> harshAccelMarkers = new ArrayList<>();

    /**
     * 轨迹分析  急刹车点覆盖物集合
     */
    private List<Marker> harshBreakingMarkers = new ArrayList<>();

    /**
     * 轨迹分析  急转弯点覆盖物集合
     */
    private List<Marker> harshSteeringMarkers = new ArrayList<>();

    /**
     * 轨迹分析  停留点覆盖物集合
     */
    private List<Marker> stayPointMarkers = new ArrayList<>();

    /**
     * 是否查询超速点
     */
    private boolean isSpeeding = false;

    /**
     * 是否查询急加速点
     */
    private boolean isHarshAccel = false;

    /**
     * 是否查询急刹车点
     */
    private boolean isHarshBreaking = false;

    /**
     * 是否查询急转弯点
     */
    private boolean isHarshSteering = false;

    /**
     * 是否查询停留点
     */
    private boolean isStayPoint = false;

    /**
     * 轨迹排序规则
     */
    private SortType sortType = SortType.asc;

    private int pageIndex = 1;

    /**
     * 轨迹分析上一次请求时间
     */
    private long lastQueryTime = 0;

    private FloatingActionButton sceenButton;

    private MapView mapView;

    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.track_query_title);
        setOnClickListener(this);
        trackApp = (TrackApplication) getApplicationContext();
        init();
        sceenButton = findViewById(R.id.screen_button);
        sceenButton.setOnClickListener(this);

        Intent intent = getIntent();
        account = intent.getStringExtra("name");
    }

    /**
     * 初始化
     */
    private void init() {
        viewUtil = new ViewUtil();
        mapUtil = MapUtil.getInstance();
        //mapUtil.init((MapView) findViewById(R.id.track_query_mapView));
        mapView = findViewById(R.id.track_query_mapView);
        mapUtil.init(mapView);
        mapUtil.baiduMap.setOnMarkerClickListener(this);
        mapUtil.setCenter(trackApp);
        trackAnalysisInfoLayout = new TrackAnalysisInfoLayout(this, mapUtil.baiduMap);
        initListener();

    }

    /**
     * 轨迹分析
     *
     * @param v
     */
    public void onTrackAnalysis(View v) {
        if (null == trackAnalysisDialog) {
            trackAnalysisDialog = new TrackAnalysisDialog(this);
        }
        // 显示窗口
        trackAnalysisDialog.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        // 处理PopupWindow在Android N系统上的兼容性问题
        if (Build.VERSION.SDK_INT < 24) {
            trackAnalysisDialog.update(trackAnalysisDialog.getWidth(), trackAnalysisDialog.getHeight());
        }
        if (CommonUtil.getCurrentTime() - lastQueryTime > Constants.ANALYSIS_QUERY_INTERVAL) {
            lastQueryTime = CommonUtil.getCurrentTime();
            speedingPoints.clear();
            harshAccelPoints.clear();
            harshBreakingPoints.clear();
            stayPoints.clear();
            queryDrivingBehavior();
            queryStayPoint();
        }

    }

    /**
     * 轨迹查询设置回调
     *
     * @param historyTrackRequestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int historyTrackRequestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }

        trackPoints.clear();
        pageIndex = 1;

        if (data.hasExtra("startTime")) {
            startTime = data.getLongExtra("startTime", CommonUtil.getCurrentTime());
        }
        if (data.hasExtra("endTime")) {
            endTime = data.getLongExtra("endTime", CommonUtil.getCurrentTime());
        }

        ProcessOption processOption = new ProcessOption();
        if (data.hasExtra("radius")) {
            processOption.setRadiusThreshold(data.getIntExtra("radius", Constants.DEFAULT_RADIUS_THRESHOLD));
        }
        if (data.hasExtra("transportMode")) {
            processOption.setTransportMode(TransportMode.valueOf(data.getStringExtra("transportMode")));
        }
        if (data.hasExtra("denoise")) {
            processOption.setNeedDenoise(data.getBooleanExtra("denoise", true));
        }
        if (data.hasExtra("vacuate")) {
            processOption.setNeedVacuate(data.getBooleanExtra("vacuate", true));
        }
        if (data.hasExtra("mapmatch")) {
            processOption.setNeedMapMatch(data.getBooleanExtra("mapmatch", true));
        }
        historyTrackRequest.setProcessOption(processOption);

        if (data.hasExtra("supplementMode")) {
            historyTrackRequest.setSupplementMode(SupplementMode.valueOf(data.getStringExtra("supplementMode")));
        }
        if (data.hasExtra("sortType")) {
            sortType = SortType.valueOf(data.getStringExtra("sortType"));
            historyTrackRequest.setSortType(sortType);
        }
        if (data.hasExtra("coordTypeOutput")) {
            historyTrackRequest.setCoordTypeOutput(CoordType.valueOf(data.getStringExtra("coordTypeOutput")));
        }
        if (data.hasExtra("processed")) {
            historyTrackRequest.setProcessed(data.getBooleanExtra("processed", true));
        }

        queryHistoryTrack();

        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String img_path = getPath(this, uri);
            File file = new File(img_path);
            uploadPic(file,account);
        }
    }

    /**
     * 查询历史轨迹
     */
    private void queryHistoryTrack() {
        trackApp.initRequest(historyTrackRequest);
        historyTrackRequest.setEntityName(trackApp.entityName);
        historyTrackRequest.setStartTime(startTime);
        historyTrackRequest.setEndTime(endTime);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        trackApp.mClient.queryHistoryTrack(historyTrackRequest, mTrackListener);
    }

    /**
     * 查询驾驶行为
     */
    private void queryDrivingBehavior() {
        trackApp.initRequest(drivingBehaviorRequest);
        drivingBehaviorRequest.setEntityName(trackApp.entityName);
        drivingBehaviorRequest.setStartTime(startTime);
        drivingBehaviorRequest.setEndTime(endTime);
        trackApp.mClient.queryDrivingBehavior(drivingBehaviorRequest, mAnalysisListener);
    }

    /**
     * 查询停留点
     */
    private void queryStayPoint() {
        trackApp.initRequest(stayPointRequest);
        stayPointRequest.setEntityName(trackApp.entityName);
        stayPointRequest.setStartTime(startTime);
        stayPointRequest.setEndTime(endTime);
        stayPointRequest.setStayTime(Constants.STAY_TIME);
        trackApp.mClient.queryStayPoint(stayPointRequest, mAnalysisListener);
    }

    /**
     * 轨迹分析对话框 选项点击事件
     *
     * @param compoundButton
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

        switch (compoundButton.getId()) {
            case R.id.chk_speeding:
                isSpeeding = isChecked;
                handleMarker(speedingMarkers, isSpeeding);
                break;

            case R.id.chk_harsh_breaking:
                isHarshBreaking = isChecked;
                handleMarker(harshBreakingMarkers, isHarshBreaking);
                break;

            case R.id.chk_harsh_accel:
                isHarshAccel = isChecked;
                handleMarker(harshAccelMarkers, isHarshAccel);
                break;

            case R.id.chk_harsh_steering:
                isHarshSteering = isChecked;
                handleMarker(harshSteeringMarkers, isHarshSteering);
                break;

            case R.id.chk_stay_point:
                isStayPoint = isChecked;
                handleMarker(stayPointMarkers, isStayPoint);
                break;

            default:
                break;
        }
    }

    /**
     * 按钮点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 轨迹查询选项
            case R.id.btn_activity_options:
                ViewUtil.startActivityForResult(this, TrackQueryOptionsActivity.class, Constants.REQUEST_CODE);
                break;
            case R.id.screen_button:
                //Bitmap bitmap = captureScreen(this);
                //File file;
                //try {
                //    file = saveBitmapFile(bitmap,System.currentTimeMillis() + ".png");
                //    uploadPic(file);
                //} catch (Exception e) {
                //    e.printStackTrace();
                //}
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            default:
                break;
        }
    }

    private void uploadPic(File file, String account) {
        HttpUtil.sendPic(file,account, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(trackApp, "服务器出错！", Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                byte[] bytes = body.bytes();
                String result = new String(bytes);
                if (result.contains("200")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(trackApp, "上传图片成功！", Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });
    }

    /**
     * 轨迹分析覆盖物点击事件
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle bundle = marker.getExtraInfo();
        // 如果bundle为空或者marker不可见，则过滤点击事件
        if (null == bundle || !marker.isVisible()) {
            return false;
        }
        int type = bundle.getInt("type");
        switch (type) {
            case R.id.chk_speeding:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_speeding_title);
                trackAnalysisInfoLayout.key1.setText(R.string.actual_speed);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("actualSpeed")));
                trackAnalysisInfoLayout.key2.setText(R.string.limit_speed);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("limitSpeed")));
                break;

            case R.id.chk_harsh_accel:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_accel_title);
                trackAnalysisInfoLayout.key1.setText(R.string.acceleration);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("acceleration")));
                trackAnalysisInfoLayout.key2.setText(R.string.initial_speed_2);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("initialSpeed")));
                trackAnalysisInfoLayout.key3.setText(R.string.end_speed_2);
                trackAnalysisInfoLayout.value3.setText(String.valueOf(bundle.getDouble("endSpeed")));
                break;

            case R.id.chk_harsh_breaking:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_breaking_title);
                trackAnalysisInfoLayout.key1.setText(R.string.acceleration);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("acceleration")));
                trackAnalysisInfoLayout.key2.setText(R.string.initial_speed_1);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("initialSpeed")));
                trackAnalysisInfoLayout.key3.setText(R.string.end_speed_1);
                trackAnalysisInfoLayout.value3.setText(String.valueOf(bundle.getDouble("endSpeed")));
                break;

            case R.id.chk_harsh_steering:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_steering_title);
                trackAnalysisInfoLayout.key1.setText(R.string.centripetal_acceleration);
                trackAnalysisInfoLayout.value1.setText(String.valueOf(bundle.getDouble("centripetalAcceleration")));
                trackAnalysisInfoLayout.key2.setText(R.string.turn_type);
                trackAnalysisInfoLayout.value2.setText(String.valueOf(bundle.getDouble("turnType")));
                trackAnalysisInfoLayout.key3.setText(R.string.turn_speed);
                trackAnalysisInfoLayout.value3.setText(String.valueOf(bundle.getDouble("turnSpeed")));
                break;

            case R.id.chk_stay_point:
                trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_stay_title);
                trackAnalysisInfoLayout.key1.setText(R.string.stay_start_time);
                trackAnalysisInfoLayout.value1.setText(CommonUtil.formatTime(bundle.getLong("startTime") * 1000));
                trackAnalysisInfoLayout.key2.setText(R.string.stay_end_time);
                trackAnalysisInfoLayout.value2.setText(CommonUtil.formatTime(bundle.getLong("endTime") * 1000));
                trackAnalysisInfoLayout.key3.setText(R.string.stay_duration);
                trackAnalysisInfoLayout.value3.setText(CommonUtil.formatSecond(bundle.getInt("duration")));
                break;

            default:
                break;
        }
        //  保存当前操作的marker
        analysisMarker = marker;

        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow trackAnalysisInfoWindow = new InfoWindow(trackAnalysisInfoLayout.mView, marker.getPosition(), -47);
        //显示InfoWindow
        mapUtil.baiduMap.showInfoWindow(trackAnalysisInfoWindow);

        return false;
    }

    private void clearAnalysisList() {
        if (null != speedingPoints) {
            speedingPoints.clear();
        }
        if (null != harshAccelPoints) {
            harshAccelPoints.clear();
        }
        if (null != harshBreakingPoints) {
            harshBreakingPoints.clear();
        }
        if (null != harshSteeringPoints) {
            harshSteeringPoints.clear();
        }
    }

    private void initListener() {
        mTrackListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                int total = response.getTotal();
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    viewUtil.showToast(TrackQueryActivity.this, response.getMessage());
                } else if (0 == total) {
                    viewUtil.showToast(TrackQueryActivity.this, getString(R.string.no_track_data));
                } else {
                    List<TrackPoint> points = response.getTrackPoints();
                    if (null != points) {
                        for (TrackPoint trackPoint : points) {
                            if (!CommonUtil.isZeroPoint(trackPoint.getLocation().getLatitude(),
                                    trackPoint.getLocation().getLongitude())) {
                                trackPoints.add(MapUtil.convertTrace2Map(trackPoint.getLocation()));
                            }
                        }
                    }
                }

                if (total > Constants.PAGE_SIZE * pageIndex) {
                    historyTrackRequest.setPageIndex(++pageIndex);
                    queryHistoryTrack();
                } else {
                    mapUtil.drawHistoryTrack(trackPoints, sortType);
                }
            }

            @Override
            public void onDistanceCallback(DistanceResponse response) {
                super.onDistanceCallback(response);
            }

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                super.onLatestPointCallback(response);
            }
        };

        mAnalysisListener = new OnAnalysisListener() {
            @Override
            public void onStayPointCallback(StayPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    lastQueryTime = 0;
                    viewUtil.showToast(TrackQueryActivity.this, response.getMessage());
                    return;
                }
                if (0 == response.getStayPointNum()) {
                    return;
                }
                stayPoints.addAll(response.getStayPoints());
                handleOverlays(stayPointMarkers, stayPoints, isStayPoint);
            }

            @Override
            public void onDrivingBehaviorCallback(DrivingBehaviorResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    lastQueryTime = 0;
                    viewUtil.showToast(TrackQueryActivity.this, response.getMessage());
                    return;
                }

                if (0 == response.getSpeedingNum() && 0 == response.getHarshAccelerationNum()
                        && 0 == response.getHarshBreakingNum() && 0 == response.getHarshSteeringNum()) {
                    return;
                }

                clearAnalysisList();
                clearAnalysisOverlay();

                List<SpeedingInfo> speedingInfos = response.getSpeedings();
                for (SpeedingInfo info : speedingInfos) {
                    speedingPoints.addAll(info.getPoints());
                }
                harshAccelPoints.addAll(response.getHarshAccelerationPoints());
                harshBreakingPoints.addAll(response.getHarshBreakingPoints());
                harshSteeringPoints.addAll(response.getHarshSteeringPoints());

                handleOverlays(speedingMarkers, speedingPoints, isSpeeding);
                handleOverlays(harshAccelMarkers, harshAccelPoints, isHarshAccel);
                handleOverlays(harshBreakingMarkers, harshBreakingPoints, isHarshBreaking);
                handleOverlays(harshSteeringMarkers, harshSteeringPoints, isHarshSteering);
            }
        };
    }

    /**
     * 处理轨迹分析覆盖物
     *
     * @param markers
     * @param points
     * @param isVisible
     */
    private void handleOverlays(List<Marker> markers, List<? extends Point> points, boolean
            isVisible) {
        if (null == markers || null == points) {
            return;
        }
        for (com.baidu.trace.model.Point point : points) {
            OverlayOptions overlayOptions = new MarkerOptions()
                    .position(MapUtil.convertTrace2Map(point.getLocation()))
                    .icon(BitmapUtil.bmGcoding).zIndex(9).draggable(true);
            Marker marker = (Marker) mapUtil.baiduMap.addOverlay(overlayOptions);
            Bundle bundle = new Bundle();

            if (point instanceof SpeedingPoint) {
                SpeedingPoint speedingPoint = (SpeedingPoint) point;
                bundle.putInt("type", R.id.chk_speeding);
                bundle.putDouble("actualSpeed", speedingPoint.getActualSpeed());
                bundle.putDouble("limitSpeed", speedingPoint.getLimitSpeed());

            } else if (point instanceof HarshAccelerationPoint) {
                HarshAccelerationPoint accelPoint = (HarshAccelerationPoint) point;
                bundle.putInt("type", R.id.chk_harsh_accel);
                bundle.putDouble("acceleration", accelPoint.getAcceleration());
                bundle.putDouble("initialSpeed", accelPoint.getInitialSpeed());
                bundle.putDouble("endSpeed", accelPoint.getEndSpeed());

            } else if (point instanceof HarshBreakingPoint) {
                HarshBreakingPoint breakingPoint = (HarshBreakingPoint) point;
                bundle.putInt("type", R.id.chk_harsh_breaking);
                bundle.putDouble("acceleration", breakingPoint.getAcceleration());
                bundle.putDouble("initialSpeed", breakingPoint.getInitialSpeed());
                bundle.putDouble("endSpeed", breakingPoint.getEndSpeed());

            } else if (point instanceof HarshSteeringPoint) {
                HarshSteeringPoint steeringPoint = (HarshSteeringPoint) point;
                bundle.putInt("type", R.id.chk_harsh_steering);
                bundle.putDouble("centripetalAcceleration", steeringPoint.getCentripetalAcceleration());
                bundle.putString("turnType", steeringPoint.getTurnType().name());
                bundle.putDouble("turnSpeed", steeringPoint.getTurnSpeed());

            } else if (point instanceof StayPoint) {
                StayPoint stayPoint = (StayPoint) point;
                bundle.putInt("type", R.id.chk_stay_point);
                bundle.putLong("startTime", stayPoint.getStartTime());
                bundle.putLong("endTime", stayPoint.getEndTime());
                bundle.putInt("duration", stayPoint.getDuration());
            }
            marker.setExtraInfo(bundle);
            markers.add(marker);
        }

        handleMarker(markers, isVisible);
    }

    /**
     * 处理marker
     *
     * @param markers
     * @param isVisible
     */
    private void handleMarker(List<Marker> markers, boolean isVisible) {
        if (null == markers || markers.isEmpty()) {
            return;
        }
        for (Marker marker : markers) {
            marker.setVisible(isVisible);
        }

        if (markers.contains(analysisMarker)) {
            mapUtil.baiduMap.hideInfoWindow();
        }

    }

    /**
     * 清除驾驶行为分析覆盖物
     */
    public void clearAnalysisOverlay() {
        clearOverlays(speedingMarkers);
        clearOverlays(harshAccelMarkers);
        clearOverlays(harshBreakingMarkers);
        clearOverlays(stayPointMarkers);
    }

    private void clearOverlays(List<Marker> markers) {
        if (null == markers) {
            return;
        }
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapUtil.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != trackAnalysisInfoLayout) {
            trackAnalysisInfoLayout = null;
        }
        if (null != trackAnalysisDialog) {
            trackAnalysisDialog.dismiss();
            trackAnalysisDialog = null;
        }
        if (null != trackPoints) {
            trackPoints.clear();
        }
        if (null != stayPoints) {
            stayPoints.clear();
        }
        clearAnalysisList();
        trackPoints = null;
        speedingPoints = null;
        harshAccelPoints = null;
        harshSteeringPoints = null;
        stayPoints = null;

        clearAnalysisOverlay();
        speedingMarkers = null;
        harshAccelMarkers = null;
        harshBreakingMarkers = null;
        stayPointMarkers = null;

        mapUtil.clear();

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_trackquery;
    }

    public Bitmap captureScreen(Activity context) {
        //View cv = context.getWindow().getDecorView();
        //cv.setDrawingCacheEnabled(true);
        //cv.buildDrawingCache();
        //Bitmap bmp = cv.getDrawingCache();
        //if (bmp == null) {
        //    return null;
        //}
        //bmp.setHasAlpha(false);
        //bmp.prepareToDraw();
        //return bmp;

        ////获取当前屏幕的大小
        //int width = getWindow().getDecorView().getRootView().getWidth();
        //int height = getWindow().getDecorView().getRootView().getHeight();
        ////找到当前页面的跟布局
        //View view =  getWindow().getDecorView().getRootView();
        ////设置缓存
        //view.setDrawingCacheEnabled(false);
        //view.buildDrawingCache();
        ////从缓存中获取当前屏幕的图片
        //Bitmap temBitmap = view.getDrawingCache();
        ////生成相同大小的图片
        //Bitmap bitmap = Bitmap.createBitmap(temBitmap, 0, 0, width, height);
        //view.destroyDrawingCache();//不清空，第二次截屏则使用的是缓存的同一张。
        //return bitmap;

        /*获取windows中最顶层的view*/
        View view = context.getWindow().getDecorView();

        //允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        //获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;

        WindowManager windowManager = context.getWindowManager();

        //获取屏幕宽和高
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;

        //去掉状态栏
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeight, width,
                height - statusBarHeight);

        //销毁缓存信息
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    public File saveBitmapFile(Bitmap bitmap, String fileName) throws IOException {
        String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";

        String SAVE_REAL_PATH = SAVE_PIC_PATH + "/good/savePic";

        String subForder = SAVE_REAL_PATH;
        File foder = new File(subForder);
        if (!foder.exists())
            foder.mkdirs();

        File myCaptureFile = new File(subForder, fileName);
        if (!myCaptureFile.exists())
            myCaptureFile.createNewFile();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();

        if (myCaptureFile != null) {
            Toast.makeText(this, "已截图", Toast.LENGTH_SHORT);
            return myCaptureFile;
        } else {
            return null;
        }

        //File file = new File(filePath);//将要保存图片的路径
        //try {
        //    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        //    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        //    bos.flush();
        //    bos.close();
        //    return file;
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        //return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}

