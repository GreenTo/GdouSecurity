package com.gdou.security.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.gdou.security.data.UserData;
import com.gdou.security.MapActivity;
import com.gdou.security.R;

public class LocationService extends Service {

    public LocationClient mLocationClient;

    private TextView positionText;

    private long id;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new LocationListener());
        Intent intent = new Intent(this, MapActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("正在巡逻")
                .setContentText("正在发送定位")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        startForeground(1,notification);
        id = UserData.id;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requestLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    public void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    public void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);
    }

    public class LocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            upLocation(String.valueOf(bdLocation.getLatitude()),String.valueOf(bdLocation.getLongitude()),id);
        }
    }

    public void upLocation(String latitude,String longitude,long id) {

    }

}
