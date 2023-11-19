package com.example.opencv_integrate2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;

import org.opencv.android.JavaCamera2View;

public class CustomCamera extends JavaCamera2View {
    private final CameraManager manager;

    public CustomCamera(Context context, int cameraId) {
        super(context, cameraId);
        manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }
    public CustomCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
        manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }
    public void setAutoFocusMode(boolean autoFocus) {
        try {
            if(autoFocus){
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            }
            else {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_OFF);
            }
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public void setFocusDistance(float distance) {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, distance);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
