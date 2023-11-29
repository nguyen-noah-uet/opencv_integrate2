package com.example.opencv_integrate2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCamera2View;
import org.opencv.core.Mat;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CustomCamera extends JavaCamera2View {
    private static final String TAG = "CustomCamera";
    private final CameraManager manager;
    private FocusState focusState = FocusState.NOT_FOCUSED;
    float minFocusDistance = 0.0f;
    float maxFocusDistance = 15.0f;
    double goldenRatio = 0.61803398875;
    float a = minFocusDistance;
    float b = maxFocusDistance;
    double f_x1 = 0.0;
    double f_x2 = 0.0;
    static float x1 = 0.0f;
    static float x2 = 0.0f;
    int iteration = 0;
    int maxIteration = 8;
    float currentSharpness = 0;
    double sharpnessDiff = 0;
    private boolean flag = false; // true if f_x1 > f_x2 else false
    int skipFrameDefault = 6;
    int skippedFrame = 6;
    Hashtable<Float, Float> sharpnessTable = new Hashtable<>();

    public FocusState getFocusState() {
        return focusState;
    }
    public boolean resetFocusState() {
        if (focusState == FocusState.FOCUSED) {
            focusState = FocusState.NOT_FOCUSED;
            return true;
        }
        return false;
    }
    public float getCurrentSharpness() {
        return currentSharpness;
    }
    public CustomCamera(Context context, int cameraId) {
        super(context, cameraId);
        manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }

    public CustomCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
        manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }

    public void setAutoFocus(boolean autoFocus) {
        try {
            if (mPreviewRequestBuilder == null) {
                Log.i(TAG, "mPreviewRequestBuilder is null");
                return;
            }
            if (autoFocus)
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            else
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the current focus distance setting.
     *
     * @return The current focus distance setting.
     */
    public float getFocusDistance() {
        try {
            return mPreviewRequestBuilder.get(CaptureRequest.LENS_FOCUS_DISTANCE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Set the focus distance.
     *
     * @param distance The focus distance to set.
     */
    public void setFocusDistance(float distance) {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, distance);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Perform an auto focus operation.
     * This method uses the golden section search algorithm to find the best focus distance.
     * The algorithm will perform a maximum of `maxIteration` iterations.
     *
     * @param roi The region of interest to perform the auto focus operation on.
     */
    public void performAutoFocus(Mat roi) {
        if (roi == null) {
            Log.i(TAG, "roi is null");
            return;
        }
        currentSharpness = Utils.calculateSharpness(roi);
        switch (focusState) {
            case FOCUSING:
                try {
                    float focusDistance = getFocusDistance();
                    if (skippedFrame > 0) {
                        skippedFrame--;
                        return;
                    }
                    Log.i(TAG, String.format("iteration: %d,", iteration));
                    sharpnessTable.put(focusDistance, currentSharpness);
                    if (iteration == 0) {
                        Log.i(TAG, "Start AF");
                        float d = (float) (goldenRatio * (b - a));
                        x1 = a + d;
                        x2 = b - d; // x1 always > x2
                        setFocusDistance(x1);
                    } else if (iteration == 1) {
                        f_x1 = currentSharpness;
                        // set focus distance
                        setFocusDistance(x2);
                    } else if (iteration == 2) {
                        // here we have sharpness of x2
                        f_x2 = currentSharpness;
                        // set focus distance
                        if (f_x1 > f_x2) {
                            // eliminate all x < x2
                            a = x2;
                            x2 = x1;
                            f_x2 = f_x1;
                            float d = (float) (goldenRatio * (b - a));
                            x1 = a + d;
                            // set focus distance
                            setFocusDistance(x1);
                            flag = true;
                        } else {
                            // eliminate all x > x1
                            b = x1;
                            x1 = x2;
                            f_x1 = f_x2;
                            float d = (float) (goldenRatio * (b - a));
                            x2 = b - d;
                            setFocusDistance(x2);
                            flag = false;
                        }
                    } else if (iteration > 2 && iteration < maxIteration) {
                        if (flag) {
                            // means f_x1 > f_x2
                            f_x1 = currentSharpness;
                            // eliminate all x < x2
                            a = x2;
                            x2 = x1;
                            f_x2 = f_x1;
                            x1 = (float) (a + goldenRatio * (b - a));
                            // set focus distance
                            setFocusDistance(x1);
                            flag = true;
                        } else {
                            // means f_x1 < f_x2
                            f_x2 = currentSharpness;
                            // eliminate all x > x1
                            b = x1;
                            x1 = x2;
                            f_x1 = f_x2;
                            x2 = (float) (a + goldenRatio * (b - a));
                            // set focus distance
                            setFocusDistance(x2);
                            flag = false;
                        }
                    }
                    if (iteration == maxIteration) {
                        Log.i(TAG, getSharpnessTableInfo(sharpnessTable));
                        float maxKey = sharpnessTable.entrySet()
                                .stream()
                                .filter(entry -> entry.getValue().equals(sharpnessTable.values().stream().max(Double::compare).orElse(null)))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse(0.0f);
                        setFocusDistance(maxKey);
                        focusState = FocusState.FOCUSED;
                        Log.i(TAG, "Stop AF");
                    }
                    iteration++;
                    skippedFrame = skipFrameDefault;
                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }
                return;
            case FOCUSED:
                return;
            case NOT_FOCUSED:
                focusState = FocusState.FOCUSING;
                return;
            default:
                return;
        }
    }


    /**
     * Get the sharpness table info as a string.
     *
     * @return The sharpness table info as a string.
     */
    private String getSharpnessTableInfo(Hashtable<Float, Float> sharpnessTable) {
        StringBuilder stringBuilder = new StringBuilder();
        // Count: 6, {{key1, value1}, {key2, value2}, {key3, value3}, {key4, value4}, {key5, value5}, {key6, value6}}
        stringBuilder.append("Count: ").append(sharpnessTable.size()).append(", {");
        for (Map.Entry<Float, Float> entry : sharpnessTable.entrySet()) {
            String key = String.format(Locale.ENGLISH,"%.2f", entry.getKey());
            String value = String.format(Locale.ENGLISH,"%.2f", entry.getValue());
            stringBuilder.append("{").append(key).append(", ").append(value).append("}, ");
        }
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    public void resetAutoFocus() {
        focusState = FocusState.NOT_FOCUSED;
        iteration = 0;
        a = minFocusDistance;
        b = maxFocusDistance;
        f_x1 = 0.0;
        f_x2 = 0.0;
        x1 = 0.0f;
        x2 = 0.0f;
        flag = false;
        sharpnessTable.clear();
        Log.i(TAG, "Reset AF");
    }


    public enum FocusState {
        FOCUSING,
        FOCUSED,
        NOT_FOCUSED
    }
}
