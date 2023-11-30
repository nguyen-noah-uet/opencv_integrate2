package com.example.opencv_integrate2;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class CameraMotionDetecion implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor acceleroSensor;
    private Sensor gyroscopeSensor;
    private boolean isMotion = false;
    private float thresholdAcc = 0.25f;
    private float thresholdGyro = 0.9f;
    private float[] acceleroValues;
    private float[] gyroValues;

    private float previousAccMag;
    private boolean isStartAcc = true;

    public CameraMotionDetecion(SensorManager sensorManager){

        if(sensorManager != null ){
            acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(acceleroSensor != null){
                sensorManager.registerListener( this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
                acceleroValues = new float[3];
            }

            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroscopeSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
                gyroValues = new float[3];
            }

        }
    }

    public boolean getIsMotion(){
        return isMotion;
    }
    public void setIsMotion(boolean type){
            this.isMotion = type;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                updateAccelerometerData(sensorEvent.values);
                if(isStartAcc == true){
                    previousAccMag = (float) Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0] +
                            sensorEvent.values[1] * sensorEvent.values[1] + sensorEvent.values[2] * sensorEvent.values[2]);
                    isStartAcc = false;
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                updateGyroscopeData(sensorEvent.values);
                break;
        }

        detectCameraMotion();

    }

    public void detectCameraMotion(){
        float accMag =  (float)Math.sqrt(acceleroValues[0] * acceleroValues[0] +
                acceleroValues[1] * acceleroValues[1] + acceleroValues[2] + acceleroValues[2]
        );

        float gyroMag =  (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                gyroValues[1] * gyroValues[1] + gyroValues[2] + gyroValues[2]
        );


        if(gyroMag > thresholdGyro || Math.abs(accMag - previousAccMag) >  thresholdAcc){
            isMotion = true;
            previousAccMag = accMag;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateAccelerometerData(float[] values) {
        acceleroValues[0] = values[0];
        acceleroValues[1] = values[1];
        acceleroValues[2] = values[2];
    }

    private void updateGyroscopeData(float[] values){
        gyroValues[0] = values[0];
        gyroValues[1] = values[1];
        gyroValues[2] = values[2];
    }
}
