package com.example.kuson.mapsoverlaysurfaceview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.kuson.customclass.CarManagerSingle;
import com.example.kuson.customclass.CarThread;
import com.example.kuson.customclass.MapOverLayViewSurfaceView;

import mapapi.overlayutil.DrivingRouteOverlay;
import mapapi.overlayutil.TransitRouteOverlay;
import mapapi.overlayutil.WalkingRouteOverlay;

/**
 * Created by Kuson on 16/7/25.
 * 在地图路线规划上直接画,demo级别
 */
public class MapsDemoActivity extends Activity implements OnGetRoutePlanResultListener{

    private MapOverLayViewSurfaceView auto_send_order_overy;
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;
    RouteLine route = null;
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用

    boolean useDefaultIcon = false;

    private CarThread mCarThread = null;
    private CarManagerSingle carMangager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initView();
    }

    private void initView(){
        auto_send_order_overy = (MapOverLayViewSurfaceView) findViewById(R.id.auto_send_order_overy);
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        mMapView.showZoomControls(false);  //隐藏比例尺
        mMapView.showScaleControl(false);  //隐藏缩放按钮
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
    }

    //路线规划搜索返回结果,步行
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MapsDemoActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            route = walkingRouteResult.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(walkingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    //路线规划搜索返回结果,公交
    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MapsDemoActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            route = transitRouteResult.getRouteLines().get(0);
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(transitRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    //路线规划搜索返回结果,驾车
    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MapsDemoActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            route = drivingRouteResult.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(drivingRouteResult.getRouteLines().get(0));
            overlay.addToMap();

            LatLng latLng = drivingRouteResult.getRouteLines().get(0).getAllStep().get(0).getWayPoints().get(0);
            mBaidumap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
            mCarThread = new CarThread(drivingRouteResult.getRouteLines().get(0).getAllStep(), mMapView);
            if (null == carMangager) {
                carMangager = new CarManagerSingle();
                carMangager.addCar(mCarThread);
                carMangager.doWork();
                auto_send_order_overy.startDraw(mMapView, mCarThread);
            } else {
                carMangager.addCarExist(mCarThread);
                carMangager.runCar(mCarThread);
                auto_send_order_overy.startDrawExt(mMapView, carMangager);
            }
        }
    }

    /**
     * 发起路线规划搜索示例
     * @param view
     */
    public void SearchButtonProcess(View view){
        //重置浏览节点的路线数据
        route = null;
        mBaidumap.clear();
        // 处理搜索按钮响应
        EditText editSt = (EditText) findViewById(R.id.start);
        EditText editEn = (EditText) findViewById(R.id.end);
        //设置起终点信息，对于tranist search 来说，城市名无意义
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", editSt.getText().toString());
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", editEn.getText().toString());

        // 实际使用中请对起点终点城市进行正确的设定
        if (view.getId() == R.id.drive) {
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        } else if (view.getId() == R.id.transit) {
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .city("北京")
                    .to(enNode));
        } else if (view.getId() == R.id.walk) {
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }
    }


    //定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_en);
            }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mSearch.destroy();
        if(carMangager != null)
            carMangager.stopWork();
    }


}
