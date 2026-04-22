package com.mindmate.backend.service;

import org.opencv.core.Mat;

public interface MultipleFileService {

    public byte[] processRedaction(byte[] inputBytes);
}
