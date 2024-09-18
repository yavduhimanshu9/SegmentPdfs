package com.example.SegmentaPDF.service;

import com.example.SegmentaPDF.model.PDFMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PDFSegmenterService {
    String  segmentAndZipPdf(MultipartFile file, int cuts) throws IOException;
    PDFMetadata getPdfMetadata(String id);
    byte[] updateSegmentation(String id, int newCuts) throws IOException;
    byte[] modifySegmentation(String id, List<Integer> newPositions) throws IOException;
    void deletePdf(String id);
    byte[] getZipFileById(String id);

}