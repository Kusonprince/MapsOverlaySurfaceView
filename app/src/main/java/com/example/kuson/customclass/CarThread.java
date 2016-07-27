package com.example.kuson.customclass;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuson on 16/7/26.
 * 对小车的各项控制都是在这里完成的,比如:偏转角度计算、角度改变等
 */
public class CarThread extends Thread {

    private List<DrivingRouteLine.DrivingStep> listStep = null;
    private MapView mapView = null;
    private Projection projection = null;
    private List<LatLng> listLatLng = new ArrayList<>();

    public CarThread(List<DrivingRouteLine.DrivingStep> listStep, MapView mapView) {
        this.listStep = listStep;
        getListLatLng();
        this.mapView = mapView;
        this.projection = mapView.getMap().getProjection();
    }

    /**
     * 将路线绘制成功后返回的liststep转换为对应的经纬度集合
     */
    private void getListLatLng() {
        if (null != this.listStep) {
            listLatLng.clear();
            for (int i = 0; i < listStep.size(); i++) {
                List<LatLng> listTmp = listStep.get(i).getWayPoints();
                for (int j = 0; j < listTmp.size(); j++) {
                    listLatLng.add(listTmp.get(j));
                }
            }
        }
    }



}
