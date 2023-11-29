package com.example.opencv_integrate2;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Pair;

import kotlin.Pair;

public class MainActivity extends CameraActivity {
    private static final String TAG = "MyMainActivity";
    CustomCamera customCamera;

    SwitchCompat customAFSwitch;
    Slider focusDistanceSlider;
    TextView focusDistanceTV;
    TextView sharpnessTV;
    RadioGroup radioGroup;
    TouchableView touchableView;
    boolean customAF = false;
    CustomCamera.FocusState focusState = CustomCamera.FocusState.NOT_FOCUSED;
    int currentEvaluation = 0;
    int maxEvaluation = 10;
    float minFocusDistance = 0.0f;
    float maxFocusDistance = 15.0f;
    double goldenRatio = 0.61803398875;
    float a = minFocusDistance;
    float b = maxFocusDistance;
    double f_x1 = 0.0;
    double f_x2 = 0.0;
    static float x1 = 0.0f;
    static float x2 = 0.0f;
    boolean flag = false;
    int skipNum = 5;
    int skipCounter = 0;
    ObjectDetection ob;


    double prevDistance = 0.0;
    int iteration = 0;
    private void bindViews() {
        customCamera = findViewById(R.id.cameraView);
        customAFSwitch = findViewById(R.id.customAFSwitch);
        focusDistanceSlider = findViewById(R.id.focusDistanceSlider);
        focusDistanceTV = findViewById(R.id.focusDistanceTV);
        sharpnessTV = findViewById(R.id.sharpnessTV);
        radioGroup = findViewById(R.id.group_radio);
        touchableView = findViewById(R.id.touchableView);


    }

    private void wireEvents(Context context) {
>>>>>>> local
        focusDistanceSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                customCamera.setFocusDistance(slider.getValue());
<<<<<<< refs/remotes/origin/hieu
                focusDistanceTV.setText(String.format("Focus distance: %.2f", slider.getValue()));
=======
                focusDistanceTV.setText(String.format(Locale.ENGLISH, "Focus distance: %.2f", slider.getValue()));
>>>>>>> local
            }
        });
        customAFSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                customAF = isChecked;
                if (customAF) {
                    customCamera.setAutoFocus(false);
                    focusDistanceSlider.setEnabled(true);
                    focusState = CustomCamera.FocusState.NOT_FOCUSED;
                    currentEvaluation = 0;
                    a = minFocusDistance;
                    b = maxFocusDistance;
                    x1 = 0.0f;
                    x2 = 0.0f;
                    f_x1 = 0.0;
                    f_x2 = 0.0;
<<<<<<< refs/remotes/origin/hieu
                    flag = false;
                    skipCounter = 0;
                    iteration = 0;


                }else {
=======
                    sharpnessTable.clear();
                } else {
>>>>>>> local
                    customCamera.setAutoFocus(true);
                    focusDistanceSlider.setEnabled(false);
                }
                focusDistanceTV.setText(String.format("Focus distance: %.2f", customCamera.getFocusDistance()));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        customCamera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            String options = "Full";
            Rect previousRoiTouch =  null;


            @Override
<<<<<<< refs/remotes/origin/hieu
            public void onCameraViewStarted(int width, int height) {}
=======
            public void onCameraViewStarted(int width, int height) {


                touchableView.setVisibility(View.INVISIBLE);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        RadioButton radioButton = findViewById(i);

                        radioButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (i == R.id.radio_full) {
                                    options = "Full";
//                                    Log.d("test", "full");
                                } else if (i == R.id.radio_object) {
                                    options = "Object";
//                                    Log.d("test", "obj");

                                } else if (i == R.id.radio_touch) {
                                    options = "Touch";
//                                    Log.d("test", "touch");

                                }
                            }
                        });
                    }
                });
            }

>>>>>>> local
            @Override
            public void onCameraViewStopped() {}
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat rgba = inputFrame.rgba();
                Mat I = inputFrame.gray();
<<<<<<< refs/remotes/origin/hieu
               

                Pair<Mat, Rect> results = ob.CascadeRec(rgba);
                Mat frame = results.component1();
                Rect roiRect = results.component2();

                Mat roi = new Mat(frame, roiRect);
                /// xử lý roi ở đây

                Imgproc.rectangle(frame, roiRect.tl(), roiRect.br(), new Scalar(0, 255, 255), 2);

                // frame có dạng rgba
                // tạo ra một vùng roi để tiến hành tính sharpness tại vùng đấy

                // rotate to portrait
                Core.rotate(rgba, rgba, Core.ROTATE_90_CLOCKWISE);
                try {
//                    if (customAF){
//                        return useCustomAF(rgba, I);
//                    }
                    double sharpness = calculateSharpness(I);
                    runOnUiThread(() -> sharpnessTV.setText(String.format("Sharpness: %.2f", sharpness)));

=======
                Mat frame = null;
                Mat roi = null;

                switch (options) {
                    case "Full":
                        touchableView.setVisibility(View.INVISIBLE);

                        break;
                    case "Object":
                        touchableView.setVisibility(View.INVISIBLE);

                        Pair<Mat, Rect> results = ob.CascadeRec(rgba);
                        frame = results.component1();
                        Rect roiRect = results.component2();

                        roi = new Mat(frame, roiRect);
                        /// xử lý roi ở đây
                        Imgproc.rectangle(frame, roiRect.tl(), roiRect.br(), new Scalar(0, 255, 255), 2);
                        break;
                    case "Touch":
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                // Stuff that updates the UI
                                touchableView.setVisibility(View.VISIBLE);

                            }
                        });
//                         dùng điều kiện của accelemeter để gọi roi
                        Rect roiRectTouch = touchableView.getRoi(rgba);


//                        Log.d("TAGGg", String.valueOf(roiRectTouch));

                       if(!rgba.empty()){
                           if(roiRectTouch != null){

                               roiRectTouch.x = Math.max(0, roiRectTouch.x);
                               roiRectTouch.x = Math.min(rgba.rows() - 40, roiRectTouch.x);

//                               Log.d("Test", String.valueOf(rgba.rows()));

                               roiRectTouch.y = Math.max(0, roiRectTouch.y);
                               roiRectTouch.y = Math.min(rgba.cols() - 360, roiRectTouch.y);



//                               Log.d("Tesg", "tesg");
                               previousRoiTouch = roiRectTouch;

                                roi = new Mat(rgba, roiRectTouch);
                            Imgproc.rectangle(rgba, roiRectTouch.tl(), roiRectTouch.br(), new Scalar(0, 255, 255), 2);
                           }
                       }


                        break;
                    default:
                        //
                        break;
                }
                try {
//                    prevSharpness = currentSharpness;
                    currentSharpness = calculateSharpness(I);
//                    sharpnessDiff = Math.abs(currentSharpness - prevSharpness);
                    float focusDistance = customCamera.getFocusDistance();
                    runOnUiThread(() -> {
                        try {
                            sharpnessTV.setText(String.format(Locale.ENGLISH, "Sharpness: %.2f", currentSharpness));
                            focusDistanceTV.setText(String.format(Locale.ENGLISH, "Focus distance: %.2f", focusDistance));
//                            focusDistanceSlider.setValue(focusDistance);
                        } catch (Exception e) {
                            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                        }
                    });

                    if (customAF) {
                        return useCustomAF(rgba, I);
                    } else {
                        return rgba;
                    }
>>>>>>> local

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return frame;

            }
        });
    }

    private Mat useCustomAF(Mat rgba, Mat I) {
<<<<<<< refs/remotes/origin/hieu
        if (skipCounter <= skipNum){
            skipCounter++;
            return rgba;
        }
        if (iteration > 10){
            return rgba;
        }
        if(iteration <= 10) {
            Log.i(TAG, String.format("iteration: %d, a: %.2f, b: %.2f, x1: %.2f, x2: %.2f, f_x1: %.2f, f_x2: %.2f", iteration, a, b, x1, x2, f_x1, f_x2));
        }
        focusState = CustomCamera.FocusState.FOCUSING;
        switch (iteration){
            case 0:
                x1 = b-(float)(goldenRatio*(b-a));
                x2 = a+(float)(goldenRatio*(b-a));
                customCamera.setFocusDistance(x1);
                runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x1)));
                skipCounter = 0;
                iteration++;
                return rgba;
            case 1:
                f_x1 = calculateSharpness(I);
                customCamera.setFocusDistance(x2);
                runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x2)));
                skipCounter = 0;
                iteration++;
                return rgba;
            case 2:
                f_x2 = calculateSharpness(I);
                if (f_x1 < f_x2){
=======
        try {
            float focusDistance = customCamera.getFocusDistance();
            if (skippedFrame > 0) {
                skippedFrame--;
                return rgba;
            }

            Log.i(TAG, String.format("iteration: %d", iteration));
            Log.i(TAG, String.format("focusDistance: %.2f, sharpness:%.2f, sharpnessDiff: %.2f", focusDistance, currentSharpness, sharpnessDiff));
            sharpnessTable.put(focusDistance, currentSharpness);
            if (iteration == 0) {
                float d = (float) (goldenRatio * (b - a));
                x1 = a + d;
                x2 = b - d; // x1 always > x2
                // set focus distance
                customCamera.setFocusDistance(x1);
                Log.i(TAG, "iteration == 0, set distance to x1");
            } else if (iteration == 1) {
                // here we have sharpness of x1
                f_x1 = currentSharpness;
                // set focus distance
                customCamera.setFocusDistance(x2);
                Log.i(TAG, "iteration == 1, set distance to x2");
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
                    customCamera.setFocusDistance(x1);
                    Log.i(TAG, "f_x1 > f_x2, set distance to x1");
>>>>>>> local
                    flag = true;
                    a = x1;
                    x1 = x2;
                    x2 = a+(float)(goldenRatio*(b-a));
                    f_x1 = f_x2;
                    customCamera.setFocusDistance(x2);
                    runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x2)));
                }else {
                    flag = false;
<<<<<<< refs/remotes/origin/hieu
                    b = x2;
=======
                }
            } else if (iteration > 2 && iteration < maxIteration) {
                if (flag) {
                    // means f_x1 > f_x2
                    f_x1 = currentSharpness;
                    // eliminate all x < x2
                    a = x2;
>>>>>>> local
                    x2 = x1;
                    x1 = b-(float)(goldenRatio*(b-a));
                    f_x2 = f_x1;
                    customCamera.setFocusDistance(x1);
                    runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x1)));
                }
                skipCounter = 0;
                iteration++;
                return rgba;
        }
        if (iteration > 2 && iteration < 10){
            if (flag){
                f_x2 = calculateSharpness(I);
                if (f_x1 < f_x2){
                    a = x1;
                    x1 = x2;
                    x2 = a+(float)(goldenRatio*(b-a));
                    f_x1 = f_x2;
                    customCamera.setFocusDistance(x2);
                    runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x2)));
                }else {
                    b = x2;
                    x2 = x1;
                    x1 = b-(float)(goldenRatio*(b-a));
                    f_x2 = f_x1;
                    customCamera.setFocusDistance(x1);
                    runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x1)));
                }
            }
<<<<<<< refs/remotes/origin/hieu
            else {
                f_x1 = calculateSharpness(I);
                if (f_x1 < f_x2){
                    a = x1;
                    x1 = x2;
                    x2 = a+(float)(goldenRatio*(b-a));
                    f_x1 = f_x2;
                    customCamera.setFocusDistance(x2);
                    runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x2)));
                }else {
                    b = x2;
                    x2 = x1;
                    x1 = b-(float)(goldenRatio*(b-a));
                    f_x2 = f_x1;
                    customCamera.setFocusDistance(x1);
                    runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", x1)));
=======
            if (iteration == maxIteration) {
                for (Float key : sharpnessTable.keySet()) {
                    Log.i(TAG, String.format("focusDistance: %.2f, sharpness:%.2f", key, sharpnessTable.get(key)));
>>>>>>> local
                }
            }
            skipCounter = 0;
            iteration++;
            return rgba;

        }

        if (iteration == 10){
            customCamera.setFocusDistance((x1+x2)/2);
            runOnUiThread(() -> focusDistanceTV.setText(String.format("Focus distance: %.2f", (x1+x2)/2)));
            focusState = CustomCamera.FocusState.FOCUSED;
            iteration = 11;
            return rgba;
        }
        return rgba;
    }


    private double calculateSharpness(Mat I) {
        Mat padded = new Mat();                     //expand input image to optimal size
        int m = Core.getOptimalDFTSize( I.rows() );
        int n = Core.getOptimalDFTSize( I.cols() ); // on the border add zero values
        Core.copyMakeBorder(I, padded, 0, m - I.rows(), 0, n - I.cols(), Core.BORDER_CONSTANT, Scalar.all(0));
        List<Mat> planes = new ArrayList<Mat>();
        padded.convertTo(padded, CvType.CV_32F);
        planes.add(padded);
        planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        Mat complexI = new Mat();
        Core.merge(planes, complexI);         // Add to the expanded another plane with zeros
        Core.dft(complexI, complexI);         // this way the result may fit in the source matrix
        Core.split(complexI, planes);                               // planes.get(0) = Re(DFT(I), planes.get(1) = Im(DFT(I))
        Core.magnitude(planes.get(0), planes.get(1), planes.get(0));// planes.get(0) = magnitude
        Mat magI = planes.get(0);
        double sharpness = Core.mean(magI).val[0];
        currentEvaluation++;
        return sharpness;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ob = new ObjectDetection(getApplicationContext());
        try {
            setContentView(R.layout.activity_main);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bindViews();
            wireEvents();


            if (!OpenCVLoader.initDebug()) {
                Log.e("OpenCVhuhuuh", "Unable to load OpenCV!");
            } else {
                Log.d("OpenCVhuhuuh", "OpenCV loaded Successfully!");
                try {
                    customCamera.enableView();
                    // set back camera
                    customCamera.setCameraIndex(0);
                    Toast.makeText(this, "opencv loaded success....", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(customCamera);
    }

    @Override
    protected void onResume() {
        super.onResume();
        customCamera.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customCamera.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        customCamera.disableView();
    }

    private void getPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            getPermission();
        }
    }
}