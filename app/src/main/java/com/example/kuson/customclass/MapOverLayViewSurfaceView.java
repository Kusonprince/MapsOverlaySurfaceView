package com.example.kuson.customclass;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.example.kuson.mapsoverlaysurfaceview.R;

/**
 * Created by Kuson on 16/7/26.
 * 自定义surfaceview,其实小车就是画在这上面的,这也是为什么拖动地图会有抖动的原因
 */
public class MapOverLayViewSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private final String TAG = "SurfaceView";
    private Context context;
    private DrawCarThread mDrawCarThread = null;

    public MapOverLayViewSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public MapOverLayViewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MapOverLayViewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MapOverLayViewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.getHolder().addCallback(this);
        this.context = context;
        getHolder().setFormat(PixelFormat.TRANSPARENT);  //设置surfaceview透明
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.e(TAG, "surfaceview is surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e(TAG, "surfaceview is surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.e(TAG, "surfaceview is surfaceDestroyed");
    }

    public void startDraw(MapView mapView, CarThread car) {
        if (null == mDrawCarThread) {
            mDrawCarThread = new DrawCarThread(getHolder(), context, mapView, car);
            mDrawCarThread.start();
        }
    }
    public void startDrawExt(MapView mapView, CarManagerSingle carManager) {
        if (null == mDrawCarThread) {
            mDrawCarThread = new DrawCarThread(getHolder(), context, mapView, carManager);
            mDrawCarThread.start();
        }
    }

    public class DrawCarThread extends Thread {

        private SurfaceHolder holder;
        private Context context;
        private MapView mapView;
        private Projection projection;
        private CarThread carThread;
        private CarManagerSingle carManager;
        private boolean isRun;
        private Bitmap bitmap; //
        private int height, width;

        public  DrawCarThread(SurfaceHolder holder, Context context, MapView mapView, CarThread car) {
            this.holder =holder;
            this.context = context;
            this.mapView = mapView;
            this.projection = mapView.getMap().getProjection();
            this.carThread = car;
            isRun = true;
            initBitmap();
        }

        public  DrawCarThread(SurfaceHolder holder, Context context, MapView mapView, CarManagerSingle carManager) {
            this.holder =holder;
            this.context = context;
            this.mapView = mapView;
            this.projection = mapView.getMap().getProjection();
            this.carManager = carManager;
            isRun = true;
            initBitmap();
        }

        private void initBitmap(){
            BitmapFactory.Options op = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.map_location_driver_car, op);
            height = bitmap.getHeight();
            width = bitmap.getWidth();
        }

    }

}
