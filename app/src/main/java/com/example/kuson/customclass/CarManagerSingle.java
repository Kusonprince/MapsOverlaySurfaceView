package com.example.kuson.customclass;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Kuson on 16/7/26.
 */
public class CarManagerSingle {

    private final String TAG = "CarManagerSingle";
    private CarThread mCarThread;
    private WorkThread mWorkThread;

    public synchronized void addCar(CarThread car) {
        this.mCarThread = car;
    }

    public synchronized void addCarExist(CarThread car) {
        this.mCarThread = car;
        mWorkThread.addCar(car);
    }

    public void runCar(CarThread car) {
        mWorkThread.runCar(car);
    }

    public void doWork() {

        if (null == mWorkThread) {
            mWorkThread = new WorkThread(mCarThread);
            mWorkThread.start();
        }
    }

    public void stopWork() {
        if (null != mWorkThread) {
            Log.e(TAG, "stopWork");
            mWorkThread.stopCars();
        } else {
            Log.e(TAG, "task is null....");
        }

    }

    /***
     * 内部类管理线程
     */
    public class WorkThread extends Thread{
        private CarThread carThread;
        private ExecutorService threadPool;
        public WorkThread(CarThread carThread) {
            this.carThread = carThread;
            threadPool = Executors.newCachedThreadPool();
        }

        @Override
        public void run() {
            threadPool.execute(carThread);
        }

        public synchronized void addCar(CarThread car) {
            carThread = car;
        }

        public void stopCars() {
            threadPool.shutdownNow();
        }

        public void runCar(CarThread car) {
            threadPool.execute(car);
        }
    }

}
