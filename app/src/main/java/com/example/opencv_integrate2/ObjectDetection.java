package com.example.opencv_integrate2;

import android.content.Context;
import android.content.res.Resources;

import org.opencv.core.Core;
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
    private CascadeClassifier faceCascade;

    public ObjectDetection(Context context){
        // load the model
        try{
            Resources resources = context.getResources();

            InputStream is = resources.openRawResource(R.raw.haarcascade_frontalface_default);
            File casecadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(casecadeDir, "haarcascade_frontalface_default.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int byteread;
            while((byteread = is.read(buffer)) != -1){
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

    public Mat CascadeRec(Mat mRgba) {
        Core.flip(mRgba.t(), mRgba, 1);
        Mat gray = new Mat();
        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY);

        int height = gray.height();
        int width = gray.width();
        int centerX = height / 2;
        int centerY = width / 2;

        int absoluteFaceSize = (int) (height * 0.1);
        MatOfRect faces = new MatOfRect();

        if (faceCascade != null) {
            faceCascade.detectMultiScale(gray, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect closestFace = null;
        double minDistance = Double.MAX_VALUE;

        Rect[] facesArray = faces.toArray();

       if(facesArray.length > 0){
           for (int i = 0; i < facesArray.length; i++) {
               Rect face = facesArray[i];
               int faceCenterX = (int)(face.tl().x+ face.br().x) / 2;
               int faceCenterY = (int)(face.tl().y + face.br().y) / 2;
               double distance = Math.sqrt(Math.pow(centerX - faceCenterX, 2) + Math.pow(centerY - faceCenterY, 2));
               if (distance < minDistance) {
                   minDistance = distance;
                   closestFace = face;
               }
           }
           if (closestFace != null) {
               Imgproc.rectangle(mRgba, closestFace.tl(), closestFace.br(), new Scalar(0, 255, 0, 255), 2);
           }
       }else{
           Rect closestObject = null;
           Mat edges = new Mat();
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

           // Draw a rectangle around the closest object
           if (closestObject != null) {
               Imgproc.rectangle(mRgba, closestObject.tl(), closestObject.br(), new Scalar(0, 255, 0, 255), 2);
           }
       }

        Core.flip(mRgba.t(), mRgba, 0);
        return mRgba;
    }

}
