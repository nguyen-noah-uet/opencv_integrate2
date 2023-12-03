package com.example.opencv_integrate2;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class WhiteBlance {
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
}
