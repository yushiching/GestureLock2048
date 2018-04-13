package com.uberspot.a2048;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.opencsv.CSVWriter;
import com.orhanobut.logger.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pipi.win.a2048.utility.LogUtil;


/**
 * Created by xiaopeng on 9/6/17.
 */

public class SensorService extends Service implements SensorEventListener {
    public static SensorManager mSensorManager ;
    private ArrayList<String[]> mSensorData = new ArrayList<String[]>();

    CSVWriter writer = null;
    private long accLastTimestamp = 0;
    private long laccLastTimestamp = 0;
    private long gyroLastTimestamp = 0;
    private long magLastTimestamp = 0;




    public static void startService(Context context){
        context.startService(new Intent( context, SensorService.class));
    }
    public static void stopService(Context context){
        context.stopService(new Intent(context, SensorService.class));
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i("SensorService.onCreate");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registorChosenSensors(mSensorManager, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LogUtil.i("SensorService.onStartCMD");
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    protected class SensorEventLoggerTask extends AsyncTask<SensorEvent, Void, Void> {
        @Override
        protected Void doInBackground(SensorEvent... events) {
            long currentTime = System.currentTimeMillis();

            SensorEvent event = events[0];
            Sensor sensor = event.sensor;

            long currentTimestamp = event.timestamp;
            long sampleInterval = 10000000;  //denotes 10ms

            String[] data = new String[7];
            data[1] = Long.toString(currentTimestamp); // data[0] is reserved

            // Write the data to files when its size is greater than 40
            if (mSensorData.size() <= 40) {
                switch (sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        // Limit the sampling frequency. The interval must be greater than 20ms
                        if ((currentTimestamp-accLastTimestamp) < sampleInterval) {
                            break;
                        } else {
                            accLastTimestamp = currentTimestamp;
                            data[2] = "0";  //0 denotes sensor type "ACCELEROMETER"

                            //TODO: get values
                            float x = event.values[0];
                            float y = event.values[1];
                            float z = event.values[2];
                            //Log.i("accelerometer sensor ", "data: " +
                            //        event.values[0] + " " + event.values[1] + " " + event.values[2]);
                            data[3] = Float.toString(event.values[0]);
                            data[4] = Float.toString(event.values[1]);
                            data[5] = Float.toString(event.values[2]);
                            data[6] = Long.toString(currentTime);
                            mSensorData.add(data);
                        }
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        if ((currentTimestamp-laccLastTimestamp) < sampleInterval) {
                            break;
                        } else {
                            laccLastTimestamp = currentTimestamp;
                            data[2] = "1";  //1 denotes sensor type "LINEAR_ACCELERATION"

                            //TODO: get values
                            //Log.i("linear accelemrometer ", "data: " +
                            //       event.values[0] + " " + event.values[1] + " " + event.values[2]);
                            data[3] = Float.toString(event.values[0]);
                            data[4] = Float.toString(event.values[1]);
                            data[5] = Float.toString(event.values[2]);
                            data[6] = Long.toString(currentTime);
                            mSensorData.add(data);
                        }
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        if ((currentTimestamp-gyroLastTimestamp) < sampleInterval) {
                            break;
                        } else {
                            gyroLastTimestamp = currentTimestamp;
                            data[2] = "2";  //1 denotes sensor type "GYROSCOPE"

                            //TODO: get values
                            //Log.i("gyroscope sensor ", "data: " +
                            //        event.values[0] + " " + event.values[1] + " " + event.values[2]);
                            data[3] = Float.toString(event.values[0]);
                            data[4] = Float.toString(event.values[1]);
                            data[5] = Float.toString(event.values[2]);
                            data[6] = Long.toString(currentTime);
                            mSensorData.add(data);
                        }
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        if ((currentTimestamp-magLastTimestamp) < sampleInterval) {
                            break;
                        } else {
                            magLastTimestamp = currentTimestamp;
                            data[2] = "3";  //3 denotes sensor type "MAGNETIC_FIELD"

                            //TODO: get values
                            //Log.i("magnetic ", "data: " + event.values[0]);
                            data[3] = Float.toString(event.values[0]);
                            data[4] = Float.toString(event.values[1]);
                            data[5] = Float.toString(event.values[2]);
                            data[6] = Long.toString(currentTime);
                            mSensorData.add(data);
                        }
                        break;
                    /*case Sensor.TYPE_LIGHT:
                        if ((currentTimestamp-lightLastTimestamp) < 5*sampleInterval) {
                            break;
                        } else {
                            lightLastTimestamp = currentTimestamp;
                            data[2] = "4";  //5 denotes sensor type "LIGHT"

                            //TODO: get values
                            //Log.i("LIGHT ", "data: " + event.values[0]);
                            data[3] = Float.toString(event.values[0]);
                            data[6] = Long.toString(currentTime);
                            mSensorData.add(data);
                        }
                        break;*/
                }
            } else {
                writeToFile(writer, LoginActivity.mSensorFilePath, mSensorData);
                mSensorData.clear();
            }

            return null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //LogUtil.i("SensorChanged");
        new SensorEventLoggerTask().execute(event);
        //Problems: 异步写入文件会导致潜在乱序的问题，如果线程同步失败的话，合理使用应该是写队列单实例

    }

    public void registorChosenSensors(SensorManager sensorManager, SensorEventListener listener) {
        Sensor mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mLAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor mGyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor mMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //Sensor mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sensorManager.registerListener(listener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, mLAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(listener, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /* Write an array list of strings to a specific path */
    public static void writeToFile(CSVWriter writer, String path, List<String[]> data) {
        try {
            writer = new CSVWriter(new FileWriter(path, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.writeAll(data);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("SensorService.onDestroy");
        mSensorManager.unregisterListener(this);

    }

}
