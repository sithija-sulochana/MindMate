package com.mindmate.backend.service.impl;

import com.mindmate.backend.service.MultipleFileService;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

@Service
public class MultipleFileServiceImpl implements MultipleFileService {

    @Override
    public byte[] processRedaction(byte[] inputBytes) {

        Mat matrix = Imgcodecs.imdecode(new MatOfByte(inputBytes), Imgcodecs.IMREAD_UNCHANGED);


        double totalWidth = matrix.cols();
        double totalHeight = matrix.rows();


        double boxWidth = totalWidth * 0.5;
        double boxHeight = totalHeight * 0.3;


        double startX = (totalWidth - boxWidth) / 2.0;
        double startY = (totalHeight - boxHeight) / 2.0;
        Point startPoint = new Point(startX, startY);


        double endX = startX + boxWidth;
        double endY = startY + boxHeight;
        Point endPoint = new Point(endX, endY);


        Scalar blackColor = new Scalar(0, 0, 0);
        Imgproc.rectangle(matrix, startPoint, endPoint, blackColor, -1);


        MatOfByte outputMatOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", matrix, outputMatOfByte);

        return outputMatOfByte.toArray();
    }
}