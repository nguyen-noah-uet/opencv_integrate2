package com.example.opencv_integrate2;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCamera2View;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Date;
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
    int maxIteration = 7;
    float currentSharpness = 0;
    double sharpnessDiff = 0;
    private boolean flag = false; // true if f_x1 > f_x2 else false
    int skipFrameDefault = 5;
    int skippedFrame = 5;

    public void setSkipFrameDefault(int skipFrameDefault) {
        this.skipFrameDefault = skipFrameDefault;
    }
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
    public void performAutoFocus(Mat roi, CameraMotionDetecion cameraMotionDetecion, FrameDifference frameDifference) {
        if (roi == null) {
            Log.i(TAG, "roi is null");
            return;
        }
        currentSharpness = Utils.calculateSharpness(roi);
        switch (focusState) {
            case FOCUSING:
                try {
                    float focusDistance = Math.round(getFocusDistance()*100)/100.0f; // round to 2 decimal places

                    if (skippedFrame > 0 && iteration > 0) {
                        skippedFrame--;
                        return;
                    }
//                    Log.i(TAG, String.format("iteration: %d,", iteration));
                    if (sharpnessTable.containsKey(focusDistance)) {
                        // update sharpness
//                        Log.i(TAG, String.format(Locale.ENGLISH, "Update focusDistance: %.2f, sharpness: %.2f", focusDistance, currentSharpness));
                        sharpnessTable.replace(focusDistance, currentSharpness);
                    }
                    else {
//                        Log.i(TAG, String.format(Locale.ENGLISH, "Add focusDistance: %.2f, sharpness: %.2f", focusDistance, currentSharpness));
                        if(Math.abs(focusDistance - 10.0f) > 0.01)
                            sharpnessTable.put(focusDistance, currentSharpness);
                    }
                    if (iteration == 0) {
                        Log.i(TAG, "Start AF");
                        float d = (float) (goldenRatio * (b - a));
                        x1 = a + d;
                        x2 = b - d; // x1 always > x2
                        setFocusDistance(x1);
                    } else if (iteration == 1) {
                        // here we have sharpness of x1
                        f_x1 = currentSharpness;
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
                            setFocusDistance(x1);
                            flag = true;
                        } else {
                            // means f_x1 < f_x2
                            f_x2 = currentSharpness;
                            // eliminate all x > x1
                            b = x1;
                            x1 = x2;
                            f_x1 = f_x2;
                            x2 = (float) (b - goldenRatio * (b - a));
                            setFocusDistance(x2);
                            flag = false;
                        }
                    }
                    if (iteration == maxIteration) {
                        Log.i(TAG, getSharpnessTableInfo(sharpnessTable));

                        // remove key=10.0
                        sharpnessTable.remove(10.00f);
                        float finalFocusDistance = 0.0f;

                        // take average of 2 latest focus distances
                        Map.Entry<Float, Float> maxEntry1 = null;
                        Map.Entry<Float, Float> maxEntry2 = null;
                        for (Map.Entry<Float, Float> entry : sharpnessTable.entrySet()) {
                            if (maxEntry1 == null || entry.getValue().compareTo(maxEntry1.getValue()) > 0) {
                                maxEntry2 = maxEntry1;
                                maxEntry1 = entry;
                            } else if (maxEntry2 == null || entry.getValue().compareTo(maxEntry2.getValue()) > 0) {
                                maxEntry2 = entry;
                            }
                        }
                        finalFocusDistance = (maxEntry1.getKey() + maxEntry2.getKey()) / 2;

//                        // find max sharpness
//                        Map.Entry<Float, Float> maxEntry = null;
//                        for (Map.Entry<Float, Float> entry : sharpnessTable.entrySet()) {
//                            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
//                                maxEntry = entry;
//                            }
//                        }
//                        finalFocusDistance = maxEntry.getKey();
                        setFocusDistance(finalFocusDistance);
                        Log.i(TAG, String.format(Locale.ENGLISH, "Set final focusDistance: %.2f", finalFocusDistance));

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
                if(cameraMotionDetecion.getIsMotion()){
                    resetAutoFocus();
                    frameDifference.resetFrameDetection();
                }
                return;
            case NOT_FOCUSED:
                //resetAutoFocus();
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

    public boolean saveImageToGallery(Context context, boolean takeCapture, Mat rgba) {
        if(takeCapture) {
            if (isExternalStorageWritable()) {
                Mat save_mat = new Mat();
                Core.flip(rgba.t(), save_mat, 0);
                Core.rotate(save_mat, save_mat, Core.ROTATE_90_CLOCKWISE);
                // Convert image RGBA to BGRA
                Imgproc.cvtColor(save_mat, save_mat, Imgproc.COLOR_RGBA2BGRA);

                // Save Image to gallery
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourAppFolder");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }

                if (success) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String fileName = "IMG_" + timeStamp + ".jpg";
                    File file = new File(folder, fileName);

                    Imgcodecs.imwrite(file.getAbsolutePath(), save_mat);

                    // Add image to the gallery
                    galleryAddPic(context, file);
                    return true;

                } else {
                    Log.e(TAG, "Failed to create directory");
                }

            } else {
                Log.e(TAG, "External storage not available");
            }
            takeCapture = false;
        }
        return false;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void galleryAddPic(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
