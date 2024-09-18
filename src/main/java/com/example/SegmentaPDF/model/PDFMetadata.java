package com.example.SegmentaPDF.model;

import java.util.ArrayList;
import java.util.List;
public class PDFMetadata {
    private int numberOfSegment;
    private String zipFilePath;
    private String originalPdfPath;

    public List<SegmentDetails> segmentDetails=new ArrayList<>();

    public String getOriginalPdfPath() {
        return originalPdfPath;
    }

    public void setOriginalPdfPath(String originalPdfPath) {
        this.originalPdfPath = originalPdfPath;
    }

    public String getZipFilePath() {
        return zipFilePath;
    }

    public void setZipFilePath(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }

    public List<SegmentDetails> getSegmentDetails() {
        return segmentDetails;
    }

    public void setSegmentDetails(SegmentDetails segmentDetail) {
        segmentDetails.add(segmentDetail);
    }

    public int getNumberOfSegment() {
        return numberOfSegment;
    }

    public void setNumberOfSegment(int numberOfSegment) {
        this.numberOfSegment = numberOfSegment;
    }


}
