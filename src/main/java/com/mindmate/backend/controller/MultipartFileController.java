package com.mindmate.backend.controller;

import com.mindmate.backend.service.MultipleFileService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class MultipartFileController {

    private final MultipleFileService multipleFileService;

    public MultipartFileController(MultipleFileService multipleFileService) {
        this.multipleFileService = multipleFileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> redactImage(@RequestParam("file")MultipartFile file) throws IOException{
        if(file.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        byte[] imageBytes = file.getBytes();
        byte[] processImage = multipleFileService.processRedaction(imageBytes);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(processImage);
    }

    @PostMapping(value = "/edges", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] detectEdges(@RequestParam("image") MultipartFile file) throws IOException {


        byte[] bytes = file.getBytes();


        Mat src = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_COLOR);


        Mat gray = new Mat();
        Mat edges = new Mat();


        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(gray, edges, 100, 200);


        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".jpg", edges, byteMat);

        return byteMat.toArray();
    }

}
