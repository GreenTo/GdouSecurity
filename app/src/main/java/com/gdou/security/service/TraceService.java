package com.gdou.security.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.model.LocationMode;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProtocolType;
import com.baidu.trace.model.PushMessage;
import com.gdou.security.utils.LogUtil;

public class TraceService extends Service {

    private static final String TAG = "TraceService";

    // 轨迹服务
    protected static Trace trace = null;

    // 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
    public static final long serviceId = 209048;

    // 是否需要对象存储服务，注意：若需要对象存储服务，一定要导入
    private boolean isNeedObjectStoraage = false;

    // 轨迹服务客户端
    public static LBSTraceClient client = null;

    // Entity监听器
    public static OnEntityListener entityListener = null;

    //轨道服务监视器
    public static OnTraceListener mTranceListener = null;

    // 采集周期（单位 : 秒）
    private int gatherInterval = 5;

    // 设置打包周期(单位 : 秒)
    private int packInterval = 5;

    protected static boolean isTraceStart = false;

    // 手机IMEI号设置为唯一轨迹标记号,只要该值唯一,就可以作为轨迹的标识号,使用相同的标识将导致轨迹混乱
    private String imei;


    public IBinder onBind(Intent arg0) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getExtras() != null){
            imei = intent.getStringExtra("imei");
        }
        init();
        //return super.onStartCommand(intent, START_STICKY, startId);
        return super.onStartCommand(intent,flags,startId);
    }

    //被销毁时反注册广播接收器
    public void onDestroy() {
        super.onDestroy();
        stopTrace();
    }

    /**
     * 初始化
     */
    private void init() {
        // 初始化轨迹服务客户端
        client = new LBSTraceClient(this);

        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);

        // 初始化轨迹服务
        trace = new Trace(serviceId, imei, isNeedObjectStoraage);

        // 采集周期,上传周期
        client.setInterval(gatherInterval, packInterval);

        // 设置http请求协议类型:http,https
        client.setProtocolType(ProtocolType.HTTP);

        // 初始化监听器
        initListener();

        // 启动轨迹上传
        startTrace();
    }
    // 开启轨迹服务
    private void startTrace() {
        // 通过轨迹服务客户端client开启轨迹服务
        client.startTrace(trace, mTranceListener);
        client.startGather(mTranceListener);
    }

    // 停止轨迹服务
    public static void stopTrace() {
        // 通过轨迹服务客户端client停止轨迹服务

        if(isTraceStart){
            client.stopTrace(trace,mTranceListener);
            client.stopTrace(trace,mTranceListener);
        }
    }

    // 初始化监听器
    private void initListener() {

        mTranceListener = new OnTraceListener() {

            @Override
            public void onBindServiceCallback(int i, String s) {

            }

            @Override
            public void onStartTraceCallback(int i, String s) {
                LogUtil.e(TAG,s);
                if (i == 10006){
                    isTraceStart = true;
                }
            }

            @Override
            public void onStopTraceCallback(int i, String s) {
                LogUtil.e(TAG,s);
                if (i == 0){
                    isTraceStart = false;
                }
            }

            @Override
            public void onStartGatherCallback(int i, String s) {
                LogUtil.e(TAG,s);
            }

            @Override
            public void onStopGatherCallback(int i, String s) {
                LogUtil.e(TAG,s);
            }

            @Override
            public void onPushCallback(byte b, PushMessage pushMessage) {
                LogUtil.e(TAG,pushMessage.toString());
            }

            @Override
            public void onInitBOSCallback(int i, String s) {
                LogUtil.d(TAG,s);
            }
        };

        //client.setOnTraceListener(mTranceListener);
    }

    //// 初始化OnEntityListener
    //private void initOnEntityListener() {
    //
    //    entityListener = new OnEntityListener() {
    //
    //        // 请求失败回调接口
    //        @Override
    //        public void onRequestFailedCallback(String arg0) {method stub
    //            Looper.prepare();
    //            //LogUtil.i(TAG, "entity请求失败回调接口消息 : " + arg0);
    //            Toast.makeText(getApplicationContext(), "entity请求失败回调接口消息 : " + arg0, Toast.LENGTH_SHORT).show();
    //            Looper.loop();
    //        }
    //
    //        // 添加entity回调接口
    //        @Override
    //        public void onAddEntityCallback(String arg0) {
    //            Looper.prepare();
    //            //LogUtil.i(TAG, "添加entity回调接口消息 : " + arg0);
    //            Toast.makeText(getApplicationContext(), "添加entity回调接口消息 : " + arg0, Toast.LENGTH_SHORT).show();
    //            Looper.loop();
    //        }
    //
    //        // 查询entity列表回调接口
    //        @Override
    //        public void onQueryEntityListCallback(String message) {
    //            LogUtil.i(TAG, "onQueryEntityListCallback : " + message);
    //        }
    //
    //        @Override
    //        public void onReceiveLocation(TraceLocation location) {
    //
    //        }
    //    };
    //}
}
