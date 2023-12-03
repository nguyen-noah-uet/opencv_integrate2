package com.example.opencv_integrate2;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.animation.ImageMatrixProperty;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraMotionDetecion implements SensorEventListener {
    private static final String TAG = "CameraMotionDetection";
    private SensorManager sensorManager;
    private Sensor acceleroSensor;
    private Sensor gyroscopeSensor;
    private boolean isMotion = false;
    private float thresholdAcc = 0.5f;
    private float thresholdGyro = 1f;
    private float currentAcc = 0;
    private float previousAcc = 0;
    private float valueAccVector = 0;
    private float valueGyroscope = 0;

    public CameraMotionDetecion(SensorManager sensorManager){

        if(sensorManager != null ){
            acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if(acceleroSensor != null){
                sensorManager.registerListener( this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if(gyroscopeSensor != null){
                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }


            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if(gyroscopeSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }
    float getValueAccVector(){
        return valueAccVector;
    }

    public boolean getIsMotion(){
        return isMotion;
    }
    public void setIsMotion(boolean type){
            this.isMotion = type;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            previousAcc = currentAcc;
            currentAcc = calcValue(sensorEvent.values);
            valueAccVector = Math.abs(currentAcc - previousAcc);
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            valueGyroscope = calcValue(sensorEvent.values);
        }

        if(valueAccVector >  thresholdAcc || valueGyroscope > thresholdGyro){
            isMotion = true;
        } else {
            isMotion = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float calcValue(float[] acc){
        return (float)Math.sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2]);
    }

}
