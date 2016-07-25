package com.example.kuson.mapsoverlaysurfaceview;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

/**
 * Created by Kuson on 16/7/25.
 * 在地图路线规划上直接画,demo级别
 */
public class MapsDemoActivity extends Activity {

    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initView();
    }

    private void initView(){
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
