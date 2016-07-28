package com.example.kuson.customclass;

import android.graphics.Point;
import android.util.Log;

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
    private final String TAG = "CarThread";
    private List<DrivingRouteLine.DrivingStep> listStep = null;
    private MapView mapView = null;
    private Projection projection = null;
    private List<LatLng> listLatLng = new ArrayList<>();

    private volatile LatLng currentLatLng = null;//标记当前小车在地图上要画的位置
    private LatLng prevLatLng = null;  //当前路段起点
    private LatLng nextLatLng = null;  //当前路段终点
    private volatile Point currentPoint = null;  //标记小车在屏幕上的当前位置
    private Point prevPoint = null;  //当前路段起点映射到手机屏幕上的位置
    private Point nextPoint = null;  //当前路段终点映射到手机屏幕上的位置
    private boolean rotationFinished = true;  //标记小车当前旋转是否结束
    private double rotationAngel = 180d;  //标记小车当前段需要旋转的角度
    private double prevAngle = 0d;  //标记小车当前所处的角度

    private double angle = 0d;  //当前路段两点偏移的角度
    private double sin = 0d;  //当前路段两点对应的正弦值
    private double tan = 0d;  //当前路段两点对应的正切值
    private double currentAngle = 0d;  //当前路段两点对应屏幕偏移的角度
    private double useAngle = 0;  //标记已经旋转了多少角度

    private double timeInterval = 20;  //时间梯度
    private int currentIndex = 0;  //标记当前路段终点在List中的索引
    private double timeCount = 5 * 1000;  //模拟跑一段路所需的总共时间
    private double ratio = 0;
    private double numberUsed = 0;  //标记一段路里已经跑过的点数

    private double rotationStep = 0d;  //每次旋转角度的梯度
    private double rotationTime = 3000;  //300 milliseconds 模拟每次旋转共需3秒
    private RotationThread rotationThread = null;

    //setter and getter
    public synchronized LatLng getCurrentLatLng() {
        return currentLatLng;
    }

    public void setCurrentLatLng(LatLng currentLatLng) {
        this.currentLatLng = currentLatLng;
    }

    public synchronized Point getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
    }

    public boolean isRotationFinished() {
        return rotationFinished;
    }

    public void setRotationFinished(boolean rotationFinished) {
        this.rotationFinished = rotationFinished;
    }

    public double getRotationAngel() {
        return rotationAngel;
    }

    public void setRotationAngel(double rotationAngel) {
        this.rotationAngel = rotationAngel;
    }

    public double getPrevAngle() {
        return prevAngle;
    }

    public void setPrevAngle(double prevAngle) {
        this.prevAngle = prevAngle;
    }

    public double getRotationStep() {
        return rotationStep;
    }

    public void setRotationStep(double rotationStep) {
        this.rotationStep = rotationStep;
    }

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

    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep((long) timeInterval);
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            doDrawCarExt();
        }
    }


    private void doDrawCarExt(){
        if(listLatLng != null && listLatLng.size() > 0 && currentIndex < listLatLng.size() - 1){
            if(prevLatLng == null){ //第一次跑,要进行初始化
                prevLatLng = listLatLng.get(0);
                nextLatLng = listLatLng.get(1);

                prevPoint = projection.toScreenLocation(prevLatLng);
                nextPoint = projection.toScreenLocation(nextLatLng);

                tan = (double)(nextPoint.y - prevPoint.y) / (double)(nextPoint.x - prevPoint.x);  //计算当前两点线段对应在屏幕上时的正切值
                angle = Math.atan(tan)/* * 180*/;//转化为角度
                sin = (nextPoint.y - prevPoint.y) / Math.sqrt(Math.pow(nextPoint.x - prevPoint.x, 2) + Math.pow(nextPoint.y - prevPoint.y, 2));

                currentAngle = Math.asin(sin) * 180/ Math.PI;
                rotationAngel = currentAngle;  //初始化时,首次需要旋转的角度就是当前偏移的角度
                if (rotationAngel < 0) {
                    rotationAngel = -rotationAngel + 180;
                }
                useAngle = rotationAngel;
                prevAngle = 0;
                ratio = timeCount / timeInterval;
                numberUsed = 0;
            } else {  //运行中
                if(isEqual(currentLatLng, nextLatLng)){  //小车刚好运行到当前路段的终点
                    if(currentIndex < listLatLng.size() - 1){
                        prevLatLng = currentLatLng;  //当前点就是下一段路程的起点
                        nextLatLng = listLatLng.get(++currentIndex);

                        prevPoint = projection.toScreenLocation(prevLatLng);
                        nextPoint = projection.toScreenLocation(nextLatLng);
                        sin = (nextPoint.y - prevPoint.y) / Math.sqrt(Math.pow(nextPoint.x - prevPoint.x, 2) + Math.pow(nextPoint.y - prevPoint.y, 2));
                        changeAngle();

                        if (!prevPoint.equals(nextPoint)) {
                            rotationThread = new RotationThread();
                            rotationThread.start();
                            try {
                                if(this.isAlive())
                                    rotationThread.join();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        currentPoint.set(prevPoint.x, prevPoint.y);
                        tan = (double)(nextPoint.y - prevPoint.y) / (double)(nextPoint.x - prevPoint.x);
                        //tan = (nextLatLng.latitude - prevLatLng.latitude) / (nextLatLng.longitude - prevLatLng.longitude);
                        /*angle = Math.atan(tan) * 180;//转化为角度制
                        rotationAngel = angle * 180;
                        sin = Math.sin(angle);*/
                        numberUsed = 0;

                    }else{
                        Log.e(TAG, "run is over!!!");
                    }
                }
            }

            if (null == currentLatLng) {  //从起点开始运行，初始化当前点
                currentLatLng = prevLatLng;
                Log.e(TAG, "currentLatLng = " + currentLatLng.toString());
                currentPoint = projection.toScreenLocation(currentLatLng);
                Log.e(TAG, "currentPoint = " + currentPoint.toString());
                //rotationAngel = angle * 180;
                rotationThread = new RotationThread();
                rotationThread.start();
                try {
                    if(this.isAlive())
                        rotationThread.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else { //已经初始过，计算要新的渲染点坐标(如果已到达当前路段终点，则为当前路段终点)。
	        		/*changeCurrentLatLngTime();
	        		if (shouldChangeLatLng()) {
	        			currentLatLng = nextLatLng;
	        		}*/
	        		/*changeCurrentPointTime();
	        		if (shouldChangePointIime()) {
	        			currentLatLng = nextLatLng;
	        		}*/
                changeCurrentPointTime();
                if (shouldChangeLatLng()) {
                    currentLatLng = nextLatLng;
                }
            }

        } else { //路程全部跑完,把当前位置映射到地图上
            currentPoint = projection.toScreenLocation(currentLatLng);
        }
    }

    /**
     * 判断两个经纬度是否相同
     * @param first
     * @param second
     * @return
     */
    private boolean isEqual(LatLng first, LatLng second) {
        if (first == second) {
            return true;
        } else {
            if (first.latitude != second.latitude
                    || first.longitude != second.longitude) {
                return false;
            }
            if (first.latitudeE6 != second.latitudeE6
                    || first.longitudeE6 != second.longitudeE6) {
                return false;
            }
        }
        return true;
    }

    //屏幕上两点x轴差值
    private int xComp() {
        return prevPoint.x - nextPoint.x;
    }

    //屏幕上两点y轴差值
    private int yComp() {
        return prevPoint.y - nextPoint.y;
    }

    //地图上两点经度差值
    private double xCompLatLng() {
        return prevLatLng.longitude - nextLatLng.longitude;
    }

    //地图上两点纬度差值
    private double yCompLatLng() {
        return prevLatLng.latitude - nextLatLng.latitude;
    }

    /**
     * 判断角度的改变,这也是核心所在,当角度从50变成290,这样小车要逆时针旋转240度,这样就需要转化为顺时针旋转120度
     * 判断旋转的情况繁杂,要动手在纸上模拟然后才能明了
     */
    private void changeAngle() {
        if (prevPoint.equals(nextPoint)) {
            return;
        }
        prevAngle = currentAngle;
        currentAngle = Math.asin(sin) * 180/ Math.PI;
        rotationAngel = currentAngle;
        if (rotationAngel < 0) {
            if (-xComp() < 0 && -yComp() < 0) {
                rotationAngel = -rotationAngel + 180;
            } else if (-xComp() > 0 && -yComp() < 0){
                rotationAngel = 90 + rotationAngel + 270;
            }

        } else if (rotationAngel > 0){
            if (-xComp() > 0 && -yComp() > 0) {
                rotationAngel = rotationAngel;
            } else if (-xComp() < 0 && -yComp() > 0) {
                rotationAngel = 180 - rotationAngel;
            }

        } else if (0 == currentAngle) {
            if (-xComp() > 0) {
                rotationAngel = 360;
            } else if (-xComp() < 0) {
                rotationAngel = 180;
            } else if (-yComp() > 0) {
                rotationAngel = 90;
            } else if (-yComp() < 0) {
                rotationAngel = 270;
            }
        }
        double tmpAngle = rotationAngel - useAngle;
        useAngle = rotationAngel;
        currentAngle = useAngle;

        if (Math.abs(tmpAngle) > 180) {
            if (tmpAngle > 0) {
                rotationAngel = -(360 - tmpAngle);
                //useAngle = rotationAngel;
            } else if (tmpAngle < 0){
                rotationAngel = 360 + tmpAngle;;
                //useAngle = rotationAngel;
            } else {
                rotationAngel = tmpAngle;;
                //useAngle = rotationAngel;
            }
        } else {
            rotationAngel = tmpAngle;
        }

        //prevAngle = currentAngle;
        if (360 == useAngle) {
            useAngle = 0;
        }
    }

    /**
     * 改变当前路段所在点的位置,任何一段路都是模拟有n各点之间的移动
     */
    private synchronized void changeCurrentPointTime() {
        prevPoint = projection.toScreenLocation(prevLatLng);
        nextPoint = projection.toScreenLocation(nextLatLng);
        ++numberUsed;
        currentPoint.x = (int) (prevPoint.x + (numberUsed / ratio) * (nextPoint.x - prevPoint.x));
        currentPoint.y = (int) (prevPoint.y + (numberUsed / ratio) * (nextPoint.y - prevPoint.y));

        double latitude = 0d;
        double longitude = 0d;
        latitude = prevLatLng.latitude + (numberUsed / ratio) * (nextLatLng.latitude - prevLatLng.latitude);
        longitude = prevLatLng.longitude + (numberUsed / ratio) * (nextLatLng.longitude - prevLatLng.longitude);
        currentLatLng = new LatLng(latitude, longitude);
    		/*Point prev = projection.toScreenLocation(prevLatLng);
    		Point next = projection.toScreenLocation(nextLatLng);
    		++numberUsed;
    		currentPoint.x = (int) (prev.x + (numberUsed / ratio) * (next.x - prev.x));
    		currentPoint.y = (int) (prev.y + (numberUsed / ratio) * (next.y - prev.y));*/
        Log.e(TAG, "kuson point = " + currentPoint);
    }

    /**
     * 判断当前点是否走到了这段路程的终点
     * 难点在于要判断在各个方向上的移动是否已经超过了终点的经纬度
     * @return
     */
    private boolean shouldChangeLatLng() {
        boolean change = false;
        if (isEqual(currentLatLng, nextLatLng)) {
            change = true;
        }
        if (0 == sin) {//X轴方向
            if (xCompLatLng() < 0) {//x轴正方向
                if (currentLatLng.longitude > nextLatLng.longitude) {
                    change = true;
                }
            } else if (xCompLatLng() > 0) {//x轴负方向
                if (currentLatLng.longitude < nextLatLng.longitude) {
                    change = true;
                }
            }
        } else if (1 == sin) {//y轴正方向
            if (currentLatLng.latitude > nextLatLng.latitude) {
                change = true;
            }
        } else if (-1 == sin) {//y轴负方向
            if (currentLatLng.latitude < nextLatLng.latitude) {
                change = true;
            }
        } else {
            if (xCompLatLng() > 0 && yCompLatLng() > 0) {
                if (currentLatLng.longitude < nextLatLng.longitude
                        || currentLatLng.latitude < nextLatLng.latitude) {
                    change = true;
                }
            } else  if (xCompLatLng() < 0 && yCompLatLng() < 0){
                if (currentLatLng.longitude > nextLatLng.longitude
                        || currentLatLng.latitude > nextLatLng.latitude) {
                    change = true;
                }
            } else if (xCompLatLng() < 0 && yCompLatLng() > 0) {
                if (currentLatLng.longitude > nextLatLng.longitude
                        || currentLatLng.latitude < nextLatLng.latitude) {
                    change = true;
                }
            } else if (xCompLatLng() > 0 && yCompLatLng() < 0) {
                if (currentLatLng.longitude < nextLatLng.longitude
                        || currentLatLng.latitude > nextLatLng.latitude) {
                    change = true;
                }
            }
        }
        return change;
    }

    public class RotationThread extends Thread {
        @Override
        public void run() {
            rotationStep = (rotationAngel * timeInterval) / rotationTime;
            rotationFinished = false;
            while (!rotationFinished) {
                try {
                    Thread.sleep((long) timeInterval);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

}
