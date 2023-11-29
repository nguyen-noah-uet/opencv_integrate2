package com.example.opencv_integrate2;

import android.content.Context;
import android.content.res.Resources;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
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

import kotlin.Pair;

public class ObjectDetection {
    private CascadeClassifier faceCascade;
    private final double W = 16.00;

    private long lastDetectionTime = 0;
    private final long detectionInterval = 500; // milliseconds

    private Rect previousRoi = null;


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

//    public double calculateSharpness(Mat gray) {
//
//        // Apply the Laplacian operator to calculate the gradient
//        Mat laplacian = new Mat();
//        Imgproc.Laplacian(gray, laplacian, CvType.CV_64F);
//
//        // Calculate the variance of the Laplacian (a measure of image sharpness)
//        MatOfDouble mean = new MatOfDouble(), sigma = new MatOfDouble();
//        Core.meanStdDev(laplacian, mean, sigma);
//        double sharpness = sigma.get(0, 0)[0] * sigma.get(0, 0)[0];
//        return sharpness;
//    }

    public double calculateDistance(double W, double personWidth) {
        return W / personWidth;
    }


    // face detection
    // return closest face
    public Rect faceDetection(Rect[] facesArray, int centerX, int centerY) {
        double[] distances = new double[facesArray.length];
        int index = 0;
        for (Rect face : facesArray) {
            distances[index++] = calculateDistance(W, face.width);
        }

        double minDistance = Double.MAX_VALUE;
        for (double distance : distances) {
            minDistance = Math.min(minDistance, distance);
        }
        int count = 0;
        int closestIndex = 0;
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] == minDistance) {
                count++;
                closestIndex = i;
            }
        }

        if (count > 1) {
            // Keep only faces with the closest distance
            Rect[] closestFaces = new Rect[count];
            index = 0;
            for (int i = 0; i < distances.length; i++) {
                if (distances[i] == minDistance) {
                    closestFaces[index++] = facesArray[i];
                }
            }

            // Sort faces based on distance to the center of the camera
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

    // return closest object
    public Rect itemDetection(Mat gray, int centerX, int centerY) {
        // tìm vật thể có kích thước lớn nhất
        Mat edges = new Mat();
        Rect closestObject = null;
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 2, 2);
        Imgproc.Canny(gray, edges, 50, 150);

        // Find contours in the edges image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the contour with the maximum area (largest object)
        double maxArea = 0;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                Rect boundingRect = Imgproc.boundingRect(contour);
                closestObject = boundingRect;
            }
        }
        return closestObject;
    }

    public Pair<Mat, Rect> CascadeRec(Mat mRgba) {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDetectionTime < detectionInterval) {
            if (previousRoi != null) {
                Pair<Mat, Rect> result = new Pair<>(mRgba, previousRoi.clone());
                return result;
            }
            int centerX = mRgba.cols() / 2;
            int centerY = mRgba.rows() / 2;
            int rectWidth = 100;
            int rectHeight = 100;

            int x = centerX - rectWidth / 2;
            int y = centerY - rectHeight / 2;

            Rect newRect = new Rect(x, y, rectWidth, rectHeight);
            Pair<Mat, Rect> result = new Pair<>(mRgba, newRect);
            return result;
        }

        Core.flip(mRgba.t(), mRgba, 1);
        Mat gray = new Mat();
        // sử dụng shaprness là variance of laplacian (high pass filter) -> có thể đổi sang tính sharpness tính bằng độ lớn của các thành phần tần số
        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY);
//        double sharpness = calculateSharpness(gray);

//        Imgproc.putText(mRgba, String.format("Sharpness: %.2f", sharpness), new Point(10, 30),
//                Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);

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


        Rect closestRegion = null;

        if (facesArray.length > 0) {


            closestRegion = faceDetection(facesArray, centerX, centerY);

            if (closestRegion != null) {
//                Imgproc.rectangle(mRgba, closestRegion.tl(), closestRegion.br(), new Scalar(0, 255, 0), 2);

            }

        } else {


            closestRegion = itemDetection(gray, centerX, centerY);

            // Draw a rectangle around the closest object
            if (closestRegion != null) {
//                Imgproc.rectangle(mRgba, closestRegion.tl(), closestRegion.br(), new Scalar(0, 255, 0, 255), 2);
            }
        }
        Core.flip(mRgba.t(), mRgba, 0);


        int roiWidth = 200;
        int roiHeight = 200;

        int roiX, roiY;


        if (closestRegion != null) {
            roiX = closestRegion.x - (roiWidth - closestRegion.width) / 2;
            roiY = closestRegion.y - (roiHeight - closestRegion.height) / 2;


        }else{
            roiX = (mRgba.cols() - roiWidth) / 2;
            roiY = (mRgba.rows() - roiHeight) / 2;

        }

        roiX = Math.max(0, Math.min(roiX, mRgba.cols() - roiWidth));
        roiY = Math.max(0, Math.min(roiY, mRgba.rows() - roiHeight));

        Rect roiRect = new Rect(roiX, roiY, roiWidth, roiHeight);

        lastDetectionTime = currentTime;

        Pair<Mat, Rect> result = new Pair<>(mRgba, roiRect);

        previousRoi = roiRect.clone();

        return result;
    }

}
