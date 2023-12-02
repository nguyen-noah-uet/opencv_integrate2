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
    private boolean isMotion = false;
    private float thresholdAcc = 0.5f;
    private float thresholdGyro = 1f;

    private float currentAcc = 0;

    private float previousAcc = 0;
    private float valueAccVector = 0;

    public CameraMotionDetecion(SensorManager sensorManager){

        if(sensorManager != null ){
            acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(acceleroSensor != null){
                sensorManager.registerListener( this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
            currentAcc = calcAcc(sensorEvent.values);
            valueAccVector = Math.abs(currentAcc - previousAcc);
        }

        if(valueAccVector >  thresholdAcc){
            isMotion = true;
        } else {
            isMotion = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float calcAcc(float[] acc){
        return (float)Math.sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2]);
    }

}
