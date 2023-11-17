package com.example.opencv_integrate2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {
    CustomCamera cameraBridgeViewBase;
    Button afButton;
    boolean af = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            setContentView(R.layout.activity_main);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            afButton = findViewById(R.id.afButton);
            afButton.setOnClickListener(v -> {
                try {
                    af = !af;
                    cameraBridgeViewBase.setAutoFocusMode(af);
                    if (af == false) {
                        cameraBridgeViewBase.setFocusDistance(10.1f);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            cameraBridgeViewBase = findViewById(R.id.cameraView);
            cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
                @Override
                public void onCameraViewStarted(int width, int height) {
                }

                @Override
                public void onCameraViewStopped() {
                }

                @Override
                public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                    Mat rgba = inputFrame.rgba();

                    Mat gray = inputFrame.gray();
                    // rotate to portrait
                    Core.rotate(rgba, rgba, Core.ROTATE_90_CLOCKWISE);

//                    Imgproc.Canny(gray, gray, 90, 100);
//                    Core.bitwise_not(gray, gray);
                    return rgba;
                }
            });

            if (!OpenCVLoader.initDebug()) {
                Log.e("OpenCVhuhuuh", "Unable to load OpenCV!");

            } else {
                Log.d("OpenCVhuhuuh", "OpenCV loaded Successfully!");
                try {
                    cameraBridgeViewBase.enableView();
                    // set back camera
                    cameraBridgeViewBase.setCameraIndex(0);
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
        return Collections.singletonList(cameraBridgeViewBase);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
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