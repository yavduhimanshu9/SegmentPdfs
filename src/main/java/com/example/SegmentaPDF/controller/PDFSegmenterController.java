package com.example.SegmentaPDF.controller;

import com.example.SegmentaPDF.model.PDFMetadata;
import com.example.SegmentaPDF.service.PDFSegmenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PDFSegmenterController {

    @Autowired
    private PDFSegmenterService pdfSegmenterService;

    @PostMapping("/segment-pdf")
    public ResponseEntity<InputStreamResource> segmentPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("cuts") int cuts) {
        try {
            // Call the service method to segment and zip the PDF, and get the ID
            String id = pdfSegmenterService.segmentAndZipPdf(file, cuts);

            // Retrieve metadata to get the path of the generated zip file
            PDFMetadata metadata = pdfSegmenterService.getPdfMetadata(id);
            if (metadata == null) {
                return ResponseEntity.notFound().build(); // If metadata is not found, return 404
            }

            // Fetch the zip file path from the metadata
            String zipFilePath = metadata.getZipFilePath();
            File zipFile = new File(zipFilePath);

            // If the zip file doesn't exist or is invalid, return 404
            if (!zipFile.exists() || !zipFile.isFile()) {
                return ResponseEntity.notFound().build();
            }

            // Use InputStreamResource to stream the zip file for download
            InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));

            // Set headers for file download, including the correct MIME type
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFile.getName() + "\"");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(zipFile.length());  // Set the correct content length

            // Return the file with appropriate headers
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            // Log the error and return 500 status for internal server error
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping(value = "/download-pdf/{id}", produces = "application/json")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id) {
        byte[] zipBytes = pdfSegmenterService.getZipFileById(id);
        if (zipBytes == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=segmented_pdfs.zip");

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }


    @GetMapping("/pdf-metadata/{id}")
    public ResponseEntity<PDFMetadata> getPdfMetadata(@PathVariable String id) {
        PDFMetadata metadata = pdfSegmenterService.getPdfMetadata(id);
        if (metadata == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(metadata, HttpStatus.OK);
    }


    @PutMapping("/update-segmentation/{id}")
    public ResponseEntity<byte[]> updateSegmentation(
            @PathVariable String id,
            @RequestParam("cuts") int newCuts) {
        try {
            byte[] zipBytes = pdfSegmenterService.updateSegmentation(id, newCuts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=updated_segmented_pdfs.zip");

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/modify-segmentation/{id}")
    public ResponseEntity<byte[]> modifySegmentation(
            @PathVariable String id,
            @RequestBody List<Integer> newPositions) {
        try {
            byte[] zipBytes = pdfSegmenterService.modifySegmentation(id, newPositions);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modified_segmented_pdfs.zip");

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete-pdf/{id}")
    public ResponseEntity<Void> deletePdf(@PathVariable String id) {
        pdfSegmenterService.deletePdf(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
