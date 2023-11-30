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

import com.google.android.material.slider.Slider;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kotlin.Pair;


public class MainActivity extends CameraActivity {
    private static final String TAG = "MyMainActivity";
    CustomCamera customCamera;

    SwitchCompat customAFSwitch;
    Slider focusDistanceSlider;
    TextView focusDistanceTV;
    TextView sharpnessTV;
    RadioButton radioButtonFull, radioButtonObject, radioButtonTouch;
    RadioGroup radioGroup;
    TouchableView touchableView;

    boolean useCustomAF = false;

    ObjectDetection ob;


    private void bindViews() {
        customCamera = findViewById(R.id.cameraView);
        customAFSwitch = findViewById(R.id.customAFSwitch);
        focusDistanceSlider = findViewById(R.id.focusDistanceSlider);
        focusDistanceTV = findViewById(R.id.focusDistanceTV);
        sharpnessTV = findViewById(R.id.sharpnessTV);
        radioGroup = findViewById(R.id.group_radio);
        touchableView = findViewById(R.id.touchableView);
    }

    private void wireEvent() {
        focusDistanceSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                customCamera.setFocusDistance(slider.getValue());
                focusDistanceTV.setText(String.format(Locale.ENGLISH, "Focus distance: %.2f", slider.getValue()));
            }
        });
        customAFSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                useCustomAF = isChecked;
                if (useCustomAF) {
                    customCamera.setAutoFocus(false);
                    focusDistanceSlider.setEnabled(true);
                    customCamera.resetAutoFocus();
                } else {
                    customCamera.setAutoFocus(true);
                    focusDistanceSlider.setEnabled(false);
                }
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        });
        customCamera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            String options = "Full";
            Rect previousRoiTouch = null;
            int cameraViewWidth, camerViewHeight;

            @Override
            public void onCameraViewStarted(int width, int height) {
                cameraViewWidth = width;
                camerViewHeight = height;
                touchableView.setVisibility(View.INVISIBLE);
                radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                    RadioButton radioButton = findViewById(i);

                    radioButton.setOnClickListener(view -> {
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
                    });
                });
            }

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                Mat rgba = inputFrame.rgba();
                Mat I = inputFrame.gray();
                float currentSharpness = customCamera.getCurrentSharpness();
                float focusDistance = customCamera.getFocusDistance();

                runOnUiThread(() -> {
                    try {
                        sharpnessTV.setText(String.format(Locale.ENGLISH, "Sharpness: %.2f", currentSharpness));
                        focusDistanceTV.setText(String.format(Locale.ENGLISH, "Focus distance: %.2f", focusDistance));
                    } catch (Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                });

                try {
                    // rotate 90 degree
                    Core.rotate(rgba, rgba, Core.ROTATE_90_CLOCKWISE);
                    Mat frame;
                    Mat roi = null;
                    switch (options) {
                        case "Full":
                            touchableView.setVisibility(View.INVISIBLE);
//                            int top = I.width() / 4;
//                            int left = I.height() / 4;
//                            int width = I.width() / 2;
//                            int height = I.height() / 2;
//                            Rect roiRectFull = new Rect(left, top, width, height);
//                            roi = new Mat(I, roiRectFull);
                            roi = Utils.scaleImg(I, 40); // scale 40%
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
                            runOnUiThread(() -> {
                                // Stuff that updates the UI
                                touchableView.setVisibility(View.VISIBLE);

                            });
                            // width 480, height: 640
//                         dùng điều kiện của accelemeter để gọi roi
                            Rect roiRectTouch = touchableView.getRoi(I, cameraViewWidth, camerViewHeight);
//
////                        Log.d("TAGGg", String.valueOf(roiRectTouch));
//
                            if (roiRectTouch != null) {

                                roiRectTouch.x = Math.max(0, roiRectTouch.x);
                                roiRectTouch.x = Math.min(cameraViewWidth - 200, roiRectTouch.x);

//                               Log.d("Test", String.valueOf(rgba.rows()));

                                roiRectTouch.y = Math.max(0, roiRectTouch.y);
                                roiRectTouch.y = Math.min(camerViewHeight - 200, roiRectTouch.y);



                                Log.d(TAG, String.format("x: %d, y: %d, width: %d, height: %d", roiRectTouch.x, roiRectTouch.y, roiRectTouch.width, roiRectTouch.height));

//                               Log.d("Tesg", "tesg");
                                previousRoiTouch = roiRectTouch;

                                roi = new Mat(I, roiRectTouch);
                                Imgproc.rectangle(rgba, roiRectTouch.tl(), roiRectTouch.br(), new Scalar(0, 255, 255), 2);
                            }
                            break;
                        default:
                            roi = I;
                            break;
                    }


                    if (canPerformAutoFocus()) {
                        customCamera.performAutoFocus(roi);
                    }
                    return rgba;

                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }

                return rgba;

            }
        });
    }

    private boolean canPerformAutoFocus() {
        return useCustomAF;
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
            wireEvent();

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