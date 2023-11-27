package com.example.opencv_integrate2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends CameraActivity {
    private static final String TAG = "MyMainActivity";
    CustomCamera customCamera;

    SwitchCompat customAFSwitch;
    Slider focusDistanceSlider;
    TextView focusDistanceTV;
    TextView sharpnessTV;
    RadioButton radioButtonFull, radioButtonObject, radioButtonTouch;
    RadioGroup radioGroup;
    boolean customAF = false;
    CustomCamera.FocusState focusState = CustomCamera.FocusState.NOT_FOCUSED;
    int currentEvaluation = 0;
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
    double currentSharpness = 0;
    double sharpnessDiff = 0;
    private boolean flag = false; // true if f_x1 > f_x2 else false
    int skipFrameDefault = 5;
    int skippedFrame = 5;
    Hashtable<Float, Double> sharpnessTable = new Hashtable<>();
    ObjectDetection ob;


    private void bindViews() {
        customCamera = findViewById(R.id.cameraView);
        customAFSwitch = findViewById(R.id.customAFSwitch);
        focusDistanceSlider = findViewById(R.id.focusDistanceSlider);
        focusDistanceTV = findViewById(R.id.focusDistanceTV);
        sharpnessTV = findViewById(R.id.sharpnessTV);
        radioGroup = findViewById(R.id.group_radio);

    }

    private void wireEvents(String options) {
        focusDistanceSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                customCamera.setFocusDistance(slider.getValue());
                focusDistanceTV.setText(String.format(Locale.ENGLISH,"Focus distance: %.2f", slider.getValue()));
            }
        });
        customAFSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                customAF = isChecked;
                if(customAF){
                    customCamera.setAutoFocus(false);
                    focusDistanceSlider.setEnabled(true);
                    iteration = 0;
                    a = minFocusDistance;
                    b = maxFocusDistance;
                    x1 = 0.0f;
                    x2 = 0.0f;
                    f_x1 = 0.0;
                    f_x2 = 0.0;
                    sharpnessTable.clear();
                }else {
                    customCamera.setAutoFocus(true);
                    focusDistanceSlider.setEnabled(false);
                }
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        });
        customCamera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                Mat rgba = inputFrame.rgba();
                Mat I = inputFrame.gray();
                Mat frame = null;

//                switch (options){
//                    case "Full":
//                        break;
//                    case "Object":
//                        Pair<Mat, Rect> results = ob.CascadeRec(rgba);
//                        frame = results.component1();
//                        Rect roiRect = results.component2();
//
//                        Mat roi = new Mat(frame, roiRect);
//                        /// xử lý roi ở đây
//                        Imgproc.rectangle(frame, roiRect.tl(), roiRect.br(), new Scalar(0, 255, 255), 2);
//                        break;
//                    case "Touch":
//                        break;
//                    default:
//                        //
//                        break;
//                }
                try {
//                    prevSharpness = currentSharpness;
                    currentSharpness = calculateSharpness(I);
//                    sharpnessDiff = Math.abs(currentSharpness - prevSharpness);
                    float focusDistance = customCamera.getFocusDistance();
                    runOnUiThread(()->{
                        try {
                            sharpnessTV.setText(String.format(Locale.ENGLISH,"Sharpness: %.2f", currentSharpness));
                            focusDistanceTV.setText(String.format(Locale.ENGLISH,"Focus distance: %.2f", focusDistance));
//                            focusDistanceSlider.setValue(focusDistance);
                        }catch (Exception e){
                            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                        }
                    });

                    if (customAF){
                        return useCustomAF(rgba, I);
                    }
                    else {
                        return rgba;
                    }

                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }

                return rgba;

            }
        });
    }

    private Mat useCustomAF(Mat rgba, Mat I) {
        try {
            float focusDistance = customCamera.getFocusDistance();
            if(skippedFrame > 0){
                skippedFrame--;
                return rgba;
            }

            Log.i(TAG, String.format("iteration: %d", iteration));
            Log.i(TAG, String.format("focusDistance: %.2f, sharpness:%.2f, sharpnessDiff: %.2f", focusDistance, currentSharpness, sharpnessDiff));
            sharpnessTable.put(focusDistance, currentSharpness);
            if(iteration == 0){
                float d = (float) (goldenRatio * (b - a));
                x1 = a + d;
                x2 = b - d; // x1 always > x2
                // set focus distance
                customCamera.setFocusDistance(x1);
                Log.i(TAG, "iteration == 0, set distance to x1");
            }
            else if (iteration == 1){
                // here we have sharpness of x1
                f_x1 = currentSharpness;
                // set focus distance
                customCamera.setFocusDistance(x2);
                Log.i(TAG, "iteration == 1, set distance to x2");
            }
            else if (iteration == 2){
                // here we have sharpness of x2
                f_x2 = currentSharpness;
                // set focus distance
                if (f_x1 > f_x2){
                    // eliminate all x < x2
                    a = x2;
                    x2 = x1;
                    f_x2 = f_x1;
                    float d = (float) (goldenRatio * (b - a));
                    x1 = a + d;
                    // set focus distance
                    customCamera.setFocusDistance(x1);
                    Log.i(TAG, "f_x1 > f_x2, set distance to x1");
                    flag = true;
                } else {
                    // eliminate all x > x1
                    b = x1;
                    x1 = x2;
                    f_x1 = f_x2;
                    float d = (float) (goldenRatio * (b - a));
                    x2 = b - d;
                    // set focus distance
                    customCamera.setFocusDistance(x2);
                    Log.i(TAG, "f_x1 < f_x2, set distance to x2");
                    flag = false;
                }
            }
            else if (iteration > 2 && iteration < maxIteration){
                if(flag){
                    // means f_x1 > f_x2
                    f_x1 = currentSharpness;
                    // eliminate all x < x2
                    a = x2;
                    x2 = x1;
                    f_x2 = f_x1;
                    x1 = (float) (a + goldenRatio * (b - a));
                    // set focus distance
                    customCamera.setFocusDistance(x1);
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
                    customCamera.setFocusDistance(x2);
                    flag = false;
                }
            }
            if (iteration == maxIteration){
                for (Float key : sharpnessTable.keySet()) {
                    Log.i(TAG, String.format("focusDistance: %.2f, sharpness:%.2f", key, sharpnessTable.get(key)));
                }
                float maxKey = sharpnessTable.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().equals(sharpnessTable.values().stream().max(Double::compare).orElse(null)))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse(0.0f);
                customCamera.setFocusDistance(maxKey);
                customAF = false;
            }
            iteration++;
            skippedFrame = skipFrameDefault;
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return rgba;
    }


    private double calculateSharpness(Mat I) {
        Mat padded = new Mat();                     //expand input image to optimal size
        int m = Core.getOptimalDFTSize(I.rows());
        int n = Core.getOptimalDFTSize(I.cols()); // on the border add zero values
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

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    RadioButton radioButton = findViewById(i);
                    radioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(i == R.id.radio_full){
                                wireEvents("Full");
                            }else if(i == R.id.radio_object){
                                wireEvents("Object");
                            }else if(i == R.id.radio_touch){
                                wireEvents("Touch");
                            }
                        }
                    });


                }
            });
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