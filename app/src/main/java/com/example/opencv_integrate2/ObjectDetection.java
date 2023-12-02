package com.example.opencv_integrate2;

import android.content.Context;
import android.content.res.Resources;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetection {
    private static final long DETECTION_INTERVAL = 200;
    private static final long BACK_THRESHOLD = 2000;
    private static final long STABLE_THRESHOLD = 10;
    private final CascadeClassifier faceCascade;
    private  Rect previousRect = null;
    private long lastDetectionTime = 0;  // Variable to store the last detection time in milliseconds


    public ObjectDetection(Context context) {
        // load the model
        try {
            Resources resources = context.getResources();

            InputStream is = resources.openRawResource(R.raw.haarcascade_frontalface_default);
            File casecadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(casecadeDir, "haarcascade_frontalface_default.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int byteread;
            while ((byteread = is.read(buffer)) != -1) {
                os.write(buffer, 0, byteread);
            }
            is.close();
            os.close();

            faceCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // face detection
    // kết quả trả về là một khuôn mặt có theo thứ tự ưu tiên gần camera nhất -> gần trung tâm camera nhất
    public Rect faceDetection(Rect[] facesArray, int centerX, int centerY) {
        // khi nhận được mảng các khuôn mặt
        double[] distances = new double[facesArray.length];
        int index = 0;
        // tính khoảng cách từ mặt đến camera, cái này mang tính tương đối ch để so sánh chọn ra cái lớn nhất
        for (Rect face : facesArray) {
            distances[index++] = face.width * face.height; // distance min -> diện tích bounding box tìm được là lớn nhất
        }
        double maxDistance = Double.MIN_VALUE;
        for (double distance : distances) {
            maxDistance = Math.max(maxDistance, distance);
        }
        int count = 0;
        int closestIndex = 0;
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] == maxDistance) {
                count++;
                closestIndex = i;
            }
        }
        // nếu có nhiều hơn một khuôn mặt có cùng khoảng cách tới camera
        if (count > 1) {
            // Keep only faces with the closest distance
            Rect[] closestFaces = new Rect[count];
            index = 0;
            for (int i = 0; i < distances.length; i++) {
                if (distances[i] == maxDistance) {
                    closestFaces[index++] = facesArray[i];
                }
            }

            // Sort faces based on distance to the center of the camera
            // bằng cách tính bình phương khoảng cách euclide cho nó lớn
            // có một cách khác là đo khoảng cashc từ trung tâm ảnh mặt -> tâm, cũng được
            for (int i = 0; i < closestFaces.length - 1; i++) {
                for (int j = i + 1; j < closestFaces.length; j++) {
                    double distanceI = Math.pow((closestFaces[i].x + closestFaces[i].width / 2) - centerX, 2)
                            + Math.pow((closestFaces[i].y + closestFaces[i].height / 2) - centerY, 2);

                    double distanceJ = Math.pow((closestFaces[j].x + closestFaces[j].width / 2) - centerX, 2)
                            + Math.pow((closestFaces[j].y + closestFaces[j].height / 2) - centerY, 2);

                    if (distanceI > distanceJ) {
                        Rect temp = closestFaces[i];
                        closestFaces[i] = closestFaces[j];
                        closestFaces[j] = temp;
                    }
                }
            }

            // Choose the closest face to the center
            return closestFaces[0];
        }


        return facesArray[closestIndex];


    }


    public Rect CascadeRec(Mat mRgba, CameraMotionDetecion md) {
        long currentTime = System.currentTimeMillis();


        if(previousRect == null && currentTime - lastDetectionTime < DETECTION_INTERVAL) {
            int centerX = mRgba.cols() / 2;
            int centerY = mRgba.rows() / 2;
            int rectWidth = 200;
            int rectHeight = 200;

            int x = centerX - rectWidth / 2;
            int y = centerY - rectHeight / 2;

            Rect newRect = new Rect(x, y, rectWidth, rectHeight);
            return newRect;
        }else if(previousRect != null && currentTime - lastDetectionTime < BACK_THRESHOLD && md.getIsMotion() == false){
            return previousRect;


        }
        Mat gray = new Mat();

        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY);

        int height = gray.height();
        int width = gray.width();
        int centerX = height / 2;
        int centerY = width / 2;


        int absoluteFaceSize = (int) (height * 0.1);
        MatOfRect faces = new MatOfRect();

        // detect faces
        if (faceCascade != null) {
            faceCascade.detectMultiScale(gray, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();
//
//        for(Rect face : facesArray){
//            Imgproc.rectangle(mRgba, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
//
//        }


        Rect closestRegion = null;

        if (facesArray.length > 0) {
            closestRegion = faceDetection(facesArray, centerX, centerY);
            if (closestRegion != null) {
//                Imgproc.rectangle(mRgba, closestRegion.tl(), closestRegion.br(), new Scalar(0, 0, 255), 2);

                closestRegion.x = Math.max(0, closestRegion.x);
                closestRegion.x = Math.min(height - closestRegion.width, closestRegion.x);


                closestRegion.y = Math.max(0, closestRegion.y);
                closestRegion.y = Math.min(width - closestRegion.height, closestRegion.y);


                previousRect = closestRegion;


            }
        }
        lastDetectionTime = currentTime;
        md.setIsMotion(false);

        // tìm được thành công vật hoặc người/

        if(Math.abs(previousRect.x - closestRegion.x) < STABLE_THRESHOLD && Math.abs(previousRect.y - closestRegion.y) < STABLE_THRESHOLD){
            return previousRect;
        }

        return closestRegion;
    }

}
