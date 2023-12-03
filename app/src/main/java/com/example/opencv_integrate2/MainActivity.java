package com.example.opencv_integrate2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kotlin.Pair;


public class MainActivity extends CameraActivity{
    private static final String TAG = "MyMainActivity";
    CustomCamera customCamera;
    SwitchCompat customAFSwitch;
    Slider focusDistanceSlider;
    TextView focusDistanceTV;
    TextView sharpnessTV;
    TextView accelerometerTV;
    Button captureButton;
    Button refreshAFButton;
    RadioButton radioButtonFull, radioButtonObject, radioButtonTouch;
    RadioGroup radioGroup;
    RadioGroup radioGroup2;
    TouchableView touchableView;
    CameraMotionDetecion cameraMotionDetecion;
    boolean useCustomAF = false;
    boolean takeCapture = false;

    ObjectDetection ob;
    CameraMotionDetecion md;

    public static Mat whitePatchReference(Mat frame) {
        // Separate color channels
        List<Mat> channelsList = new ArrayList<>();
        Core.split(frame, channelsList);

        // Calculate scaling factors for each channel
        Scalar scalingFactors = calculateScalingFactors(channelsList);

        // Apply white balancing to each channel
        for (Mat channel : channelsList) {
            // Scale the channel using the calculated factor
            Core.multiply(channel, scalingFactors, channel);
        }

        // Merge the channels back into the RGBA image
        Mat balancedFrame = new Mat();
        Core.merge(channelsList, balancedFrame);

        return balancedFrame;
    }

    private static Scalar calculateScalingFactors(List<Mat> channelsList) {
        // Initialize scaling factors
        Scalar scalingFactors = new Scalar(1.0, 1.0, 1.0, 1.0);

        // Find the maximum intensity in each channel
        for (int i = 0; i < channelsList.size(); i++) {
            Core.MinMaxLocResult minMaxResult = Core.minMaxLoc(channelsList.get(i));
            double maxVal = minMaxResult.maxVal;

            // Set the scaling factor for the channel
            scalingFactors.val[i] = 255.0 / maxVal;
        }

        return scalingFactors;
    }

    public static Mat applyGrayWorld(Mat rgbaFrame) {
        // Chuyển đổi hình ảnh RGBA sang BGR để sử dụng OpenCV
        Mat bgrFrame = new Mat();
        Imgproc.cvtColor(rgbaFrame, bgrFrame, Imgproc.COLOR_RGBA2BGR);

        // Tính toán giá trị trung bình của mỗi kênh màu
        Scalar mean = Core.mean(bgrFrame);

        // Tính giá trị trung bình của mức độ xám
        double meanGray = (mean.val[0] + mean.val[1] + mean.val[2]) / 3;

        // Tính tỉ lệ cần thay đổi cho mỗi kênh màu
        Scalar scale = new Scalar(meanGray / mean.val[0], meanGray / mean.val[1], meanGray / mean.val[2]);

        // Áp dụng tỉ lệ cho từng pixel trong hình ảnh
        Core.multiply(bgrFrame, scale, bgrFrame);

        // Chuyển đổi trở lại thành hình ảnh RGBA
        Mat rgbaResult = new Mat();
        Imgproc.cvtColor(bgrFrame, rgbaResult, Imgproc.COLOR_BGR2RGBA);

        return rgbaResult;
    }

    private void bindViews() {
        customCamera = findViewById(R.id.cameraView);
        customAFSwitch = findViewById(R.id.customAFSwitch);
        focusDistanceSlider = findViewById(R.id.focusDistanceSlider);
        focusDistanceTV = findViewById(R.id.focusDistanceTV);
        sharpnessTV = findViewById(R.id.sharpnessTV);
        radioGroup = findViewById(R.id.group_radio);
        radioGroup2 = findViewById(R.id.wb_group);
        touchableView = findViewById(R.id.touchableView);
        accelerometerTV = findViewById(R.id.accelerometerTV);
        captureButton = findViewById(R.id.captureButton);
        refreshAFButton = findViewById(R.id.refreshAFButton);
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
        refreshAFButton.setOnClickListener(v -> {
            try {
                customCamera.resetAutoFocus();
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        });
        customCamera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            String options = "Full";
            String wb_options = "WB Off";
            int boxSize = 200;
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
                            customCamera.setSkipFrameDefault(4);
//                                    Log.d("test", "full");
                        } else if (i == R.id.radio_object) {
                            options = "Object";
                            customCamera.setSkipFrameDefault(6);
//                                    Log.d("test", "obj");

                        } else if (i == R.id.radio_touch) {
                            options = "Touch";
                            customCamera.setSkipFrameDefault(10);
//                                    Log.d("test", "touch");

                        }
                    });
                });
                radioGroup2.setOnCheckedChangeListener((radioGroup2, i) -> {
                    RadioButton radioButton = findViewById(i);

                    radioButton.setOnClickListener(view -> {
                        if (i == R.id.wb_off) {
                            wb_options = "WB Off";
//                                    Log.d("test", "full");
                        } else if (i == R.id.gray_world) {
                            wb_options = "Gray World";
//                                    Log.d("test", "obj");

                        } else if (i == R.id.white_path) {
                            wb_options = "White Path";
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
                       float vectorAcc = cameraMotionDetecion.getValueAccVector();
                       if (vectorAcc > 0.5){
                           Log.i(TAG, String.format("vectorAcc: %.2f", vectorAcc));
                       }
                        accelerometerTV.setText(String.format((Locale.ENGLISH), "Acceleroment: %.2f", cameraMotionDetecion.getValueAccVector() ));
                    } catch (Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                });


                try {
                    // rotate 90 degree
                    Core.rotate(rgba, rgba, Core.ROTATE_90_CLOCKWISE);
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
                            int scalePercent = 40;

                            // Calculate the new dimensions
                            int width = (int) (I.width() * scalePercent / 100.0);
                            int height = (int) (I.height() * scalePercent / 100.0);

                            // Create a Size object with the new dimensions
                            Size dim = new Size(width, height);

                            // Resize the image
                            Mat resized = new Mat();
                            Imgproc.resize(I, resized, dim, 0, 0, Imgproc.INTER_AREA);
                            roi = resized;
                            break;
                        case "Object":
                            touchableView.setVisibility(View.INVISIBLE);
                            Rect roiRect = ob.CascadeRec(rgba, cameraMotionDetecion);

//                            roi = new Mat(rgba, roiRect);
                            /// xử lý roi ở đây
                            Imgproc.rectangle(rgba, roiRect.tl(), roiRect.br(), new Scalar(0, 255, 255), 2);
                            break;
                        case "Touch":
                            runOnUiThread(() -> {
                                // Stuff that updates the UI
                                touchableView.setVisibility(View.VISIBLE);

                            });

                            // width 480, height: 640
                        // dùng điều kiện của accelemeter để gọi roi
                            Rect roiRectTouch = touchableView.getRoi(I, cameraViewWidth, camerViewHeight);

                            if (roiRectTouch != null) {

                                roiRectTouch.x = Math.max(0, roiRectTouch.x);
                                roiRectTouch.x = Math.min(cameraViewWidth - boxSize, roiRectTouch.x);

                              Log.d("Test", String.valueOf(rgba.rows()));

                                roiRectTouch.y = Math.max(0, roiRectTouch.y);
                                roiRectTouch.y = Math.min(camerViewHeight - boxSize, roiRectTouch.y);



//                                Log.d(TAG, String.format("x: %d, y: %d, width: %d, height: %d", roiRectTouch.x, roiRectTouch.y, roiRectTouch.width, roiRectTouch.height));
                                previousRoiTouch = roiRectTouch;

                                roi = new Mat(I, roiRectTouch);
                                Imgproc.rectangle(rgba, roiRectTouch.tl(), roiRectTouch.br(), new Scalar(0, 255, 255), 2);
                            }

                            break;
                        default:
                            roi = I;
                            break;
                    }

                    switch (wb_options) {
                        case "WB Off":
                            break;
                        case "Gray World": {
                            Mat balancedFrame = applyGrayWorld(rgba);
                            return balancedFrame;
                        }
                        case "White Path":
                            Mat balancedFrame = whitePatchReference(rgba);
                            return balancedFrame;
                    }


                    if (canPerformAutoFocus()) {
                        customCamera.performAutoFocus(roi, cameraMotionDetecion);
                    }

                    // take_capute image
                    boolean captured = customCamera.saveImageToGallery(getApplicationContext(),takeCapture, rgba);
                    if (captured){
                        takeCapture = false;
                        Toast.makeText(getApplicationContext(), "Captured", Toast.LENGTH_SHORT).show();
                    }
                    return rgba;

                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }

                return rgba;

            }
        });

        captureButton.setOnClickListener(v -> {
            if (!takeCapture){
                Log.i(TAG, "True Capture");
                takeCapture = true;
            } else {
                Log.i(TAG, "False Capture");
                takeCapture = false;
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
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        cameraMotionDetecion = new CameraMotionDetecion(sensorManager);
        try {
            setContentView(R.layout.activity_main);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bindViews();
            wireEvent();

            if (!OpenCVLoader.initDebug()) {
                Log.e(TAG, "Unable to load OpenCV!");
            } else {
                Log.d(TAG, "OpenCV loaded Successfully!");
                try {
                    customCamera.enableView();
                    // set back camera
                    customCamera.setCameraIndex(0);
                    Toast.makeText(this, "opencv loaded success....", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, String.format("width: %d, height: %d", customCamera.getMeasuredWidth(), customCamera.getMeasuredWidth()));
                    Log.i(TAG, String.format("width: %d, height: %d", touchableView.getMeasuredWidth(), touchableView.getMeasuredWidth()));

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
