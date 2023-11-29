package com.example.opencv_integrate2;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * Calculates the sharpness of an image.
     *
     * This method uses the Discrete Fourier Transform (DFT) to calculate the sharpness of an image.
     * The sharpness is calculated as the mean of the magnitude spectrum of the DFT of the image.
     *
     * @param I The input image as a Mat object. The image should be in grayscale.
     * @return The sharpness of the image as a float. Higher values indicate a sharper image.
     */
    public static float calculateSharpness(Mat I) {
        if (I == null)
            return 0;
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
        float sharpness = (float) Core.mean(magI).val[0];
        return sharpness;
    }

    /**
     * Converts an RGBA image to a grayscale image.
     * @param rgba The input image as a Mat object.
     * @return The grayscale image as a Mat object.
     */
    public static Mat RgbaToGray(Mat rgba) {
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);
        return gray;
    }
}
