package com.example.kuson.customclass;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
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
    private boolean mSurfaceDestroyed = false;

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
        mSurfaceDestroyed = true;
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
        private Bitmap bitmap; //小车对象,可随意更换图片
        private int height, width;  //绘制对象宽高
        Paint bitmapPaint = new Paint();

        private float rotationAngel = 180;  //小车需要旋转的角度
        private float rotationStep = 0;  //每次旋转的梯度
        private Matrix matrix = null;

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

        /**
         * 实例化绘制在surfaceview上的小车
         */
        private void initBitmap(){
            BitmapFactory.Options op = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.map_location_driver_car, op);
            height = bitmap.getHeight();
            width = bitmap.getWidth();
        }

        @Override
        public void run() {
            float rotate = 0;  //旋转角度变量
            float tmpRotate = 0;  //设置旋转角度的中转变量,标记当前小车旋转是否已经完成
            while (isRun && !mSurfaceDestroyed){
                Canvas canvas = null;
                try{
                    synchronized (holder){
                        canvas = holder.lockCanvas();
                        if(canvas == null){
                            Log.e(TAG, "canvas is null");
                            continue;
                        }
                        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                        Point point = carThread.getCurrentPoint();
                        if(point == null){
                            continue;
                        }
                        Log.e(TAG, "current point is" + point.toString());
                        matrix = new Matrix();
                        if(!carThread.isRotationFinished()){  //旋转
                            rotationAngel = (float)carThread.getRotationAngel();  //获取当前小车需要旋转的角度
                            rotationStep = 5;  //car.getRotationStep();  模拟旋转梯度为5度,实际应用中应该根据获取小车两个经纬度的时间差及距离计算

                            //设置旋转角度
                            if(tmpRotate != rotationAngel){
                                if(rotationAngel > 0){
                                    tmpRotate  += rotationStep;  //旋转未结束,梯度加每次旋转角度
                                    if (tmpRotate > rotationAngel) {
                                        tmpRotate = rotationAngel;
                                        rotate = (float) (carThread.getPrevAngle() + tmpRotate);
                                    } else {
                                        rotate  = (rotate + rotationStep) % 360;
                                    }
                                } else if(rotationAngel < 0){
                                    rotationStep = -rotationStep;  //需要旋转的度数为负数,因此叠加的梯度也要转为负数
                                    tmpRotate = tmpRotate + rotationStep;
                                    if (tmpRotate < rotationAngel) {
                                        tmpRotate = rotationAngel;
                                        rotate = (float) (carThread.getPrevAngle() + tmpRotate);
                                    } else {
                                        rotate  = (rotate + rotationStep) % 360;
                                    }
                                }
                            } else {
                                //旋转结束,设置旋转变量为true结束旋转线程
                                tmpRotate = 0;
                                carThread.setRotationFinished(true);
                            }
                        } else {  //旋转结束,正常绘制

                        }
                        matrix.postRotate(rotate % 360, width / 2, height / 2);
                        // 设置左边距和上边距
                        matrix.postTranslate(point.x - width / 2, point.y - height / 2);
                        drawItem(canvas, bitmap, matrix);
                        Thread.sleep(20);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void drawItem(Canvas canvas, Bitmap bitmap, Matrix matrix) {
            canvas.drawBitmap(bitmap, matrix, bitmapPaint);
        }

    }

}
