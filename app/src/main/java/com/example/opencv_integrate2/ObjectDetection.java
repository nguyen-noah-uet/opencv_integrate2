package com.example.opencv_integrate2;

import android.content.Context;
import android.content.res.Resources;

import org.opencv.core.Core;
import org.opencv.core.Mat;
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
        Mat mRgb = new Mat();
        Imgproc.cvtColor(mRgba, mRgb, Imgproc.COLOR_RGBA2RGB);

        int height = mRgb.height();
        int width = mRgb.width();


        int absoluteFaceSize = (int) (height * 0.1);
        MatOfRect faces = new MatOfRect();

        if (faceCascade != null) {
            faceCascade.detectMultiScale(mRgb, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        Rect[] facesArray = faces.toArray();

        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgb, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 2);
        }

        Core.flip(mRgba.t(), mRgba, 0);
        return mRgba;
    }

}
