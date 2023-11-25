package com.example.opencv_integrate2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import org.opencv.android.JavaCamera2View;

public class CustomCamera extends JavaCamera2View {
    private static final String TAG = "CustomCamera";
    private final CameraManager manager;

    public CustomCamera(Context context, int cameraId) {
        super(context, cameraId);
        manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }
    public CustomCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
        manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }
    public float getFocusDistance(){
        try {
            return mPreviewRequestBuilder.get(CaptureRequest.LENS_FOCUS_DISTANCE);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

    }
    public void setAutoFocus(boolean autoFocus) {
        try {
            if (mPreviewRequestBuilder == null) {
                Log.i(TAG, "mPreviewRequestBuilder is null");
                return;
            }
            if(autoFocus)
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            else
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
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

    @Override
    protected boolean connectCamera(int width, int height) {
        boolean b = super.connectCamera(width, height);
        if (mCaptureSession == null) {
            Log.i(TAG, "mCaptureSession is null");
            return b;
        }
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
        return b;
    }

    public enum FocusState {
        FOCUSING,
        FOCUSED,
        NOT_FOCUSED
    }
}
