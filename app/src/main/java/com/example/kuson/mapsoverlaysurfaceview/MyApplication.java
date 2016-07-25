package com.example.kuson.mapsoverlaysurfaceview;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Kuson on 16/7/25.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
