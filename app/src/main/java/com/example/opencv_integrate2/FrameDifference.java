package com.example.opencv_integrate2;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class FrameDifference {
    private static final String TAG = "FrameDifference";
    private static final int TOTAL_PIXELS = 1072 * 1072;
    private static final long TARGET_FPS = 1;
    private static final long FRAME_INTERVAL = 1000 / TARGET_FPS;
    private long lastFrameTime = 0;
    Mat curr_gray, prev_gray, diff;
    boolean is_init;
    boolean reset;
    private int thresholdPixelsDiff = 100000;
    private boolean isMotionFrame = false;
    FrameDifference() {
        boolean resetFrameDifference = false;
        is_init = false;
        curr_gray = new Mat();
        prev_gray = new Mat();
        diff = new Mat();
    }

    public boolean getIsMotionFrame(){
        return isMotionFrame;
    }
    public void setIsMotionFrame(boolean type){
        this.isMotionFrame = type;
    }

    public void resetFrameDetection(){
        if(reset){
            setIsMotionFrame(false);
            reset = false;
        }
    }

    public void checkFrameDifference(Mat inputFrame) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime < FRAME_INTERVAL) {
            return;
        }

        lastFrameTime = currentTime;

        if (!is_init) {
            prev_gray = inputFrame.clone();
            is_init = true;
            return;
        }

        curr_gray = inputFrame;

        Core.absdiff(curr_gray, prev_gray, diff);
        Imgproc.threshold(diff, diff, 40, 255, Imgproc.THRESH_BINARY);
        int numPixelsDiff = Core.countNonZero(diff);

        if (numPixelsDiff > thresholdPixelsDiff && !reset) {
            setIsMotionFrame(true);
            Log.i(TAG, String.format("Pixels Different: %d", numPixelsDiff));
            reset = true;
        } else if (numPixelsDiff <= thresholdPixelsDiff && reset) {
            resetFrameDetection();
        }

        prev_gray = curr_gray.clone();
    }

}
