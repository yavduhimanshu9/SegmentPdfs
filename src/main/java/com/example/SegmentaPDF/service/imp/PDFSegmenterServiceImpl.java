package com.example.SegmentaPDF.service.imp;

import com.example.SegmentaPDF.model.PDFMetadata;
import com.example.SegmentaPDF.model.SegmentDetails;
import com.example.SegmentaPDF.service.PDFSegmenterService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PDFSegmenterServiceImpl implements PDFSegmenterService {

    private final Map<String, PDFMetadata> pdfMetadataStore = new HashMap<>();
    private final Map<String, byte[]> zipFileStore = new HashMap<>();

    @Override
    public String segmentAndZipPdf(MultipartFile file, int cuts) throws IOException {
        // Read the PDF file from MultipartFile
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            // Generate a unique ID for the processed PDF
            String id = UUID.randomUUID().toString();

            // Save the original PDF for future updates
            File originalPdfFile=saveFile(id,file);


            // Analyze the PDF to find the cut positions
            List<Integer> cutPositions = findCutPositions(document, cuts);

            // Create temporary files for each segment
            List<File> segmentFiles = createSegments(document, cutPositions);

            // Create a zip file containing all segments
            byte[] zipBytes = createZipFromFiles(segmentFiles);

            // Populate metadata
            PDFMetadata metadata = new PDFMetadata();
            metadata.setNumberOfSegment(segmentFiles.size());
            for (int i = 0; i < segmentFiles.size(); i++) {
                metadata.setSegmentDetails(new SegmentDetails((int) segmentFiles.get(i).length(), i + 1));
            }

            // Save the zip file
            File zipFile = saveZipFile(id,zipBytes);


            // Store metadata and zip file reference
            metadata.setOriginalPdfPath(originalPdfFile.getAbsolutePath());
            metadata.setZipFilePath(zipFile.getAbsolutePath());
            pdfMetadataStore.put(id, metadata);

            // Clean up temporary files (segments)
            for (File segmentFile : segmentFiles) {
                segmentFile.delete();
            }

            return id;
        } catch (IOException e) {
            // Handle and log the exception
            throw new IOException("Error processing PDF file", e);
        }
    }

    @Override
    public byte[] getZipFileById(String id) {
        return zipFileStore.get(id);
    }

    @Override
    public PDFMetadata getPdfMetadata(String id) {
        return pdfMetadataStore.get(id);
    }

    @Override
    public byte[] updateSegmentation(String id, int newCuts) throws IOException {
        // Retrieve existing metadata
        PDFMetadata existingMetadata = pdfMetadataStore.get(id);
        if (existingMetadata == null) {
            throw new IllegalArgumentException("No PDF metadata found for the given ID.");
        }

        // Load the original PDF from the saved path
        File originalPdfFile = new File(existingMetadata.getOriginalPdfPath());
        try (PDDocument document = PDDocument.load(originalPdfFile)) {
            // Find new cut positions
            List<Integer> newCutPositions = findCutPositions(document, newCuts);

            // Create new segments
            List<File> newSegmentFiles = createSegments(document, newCutPositions);

            // Create a new zip file with updated segmentation
            byte[] newZipBytes = createZipFromFiles(newSegmentFiles);

            // Save the new zip file
            File newZipFile = new File("ZippedFile/" + id + ".zip");
            try (FileOutputStream fos = new FileOutputStream(newZipFile)) {
                fos.write(newZipBytes);
            }

            // Update metadata
            existingMetadata.setNumberOfSegment(newCutPositions.size());
            for (int i = 0; i < newSegmentFiles.size(); i++) {
                existingMetadata.setSegmentDetails(new SegmentDetails((int) newSegmentFiles.get(i).length(), i + 1));
            }
            existingMetadata.setZipFilePath(newZipFile.getAbsolutePath());
            pdfMetadataStore.put(id, existingMetadata);

            return newZipBytes;
        } catch (IOException e) {
            // Handle and log the exception
            throw new IOException("Error updating segmentation", e);
        }
    }


    @Override
    public byte[] modifySegmentation(String id, List<Integer> newPositions) throws IOException {
        // Retrieve existing metadata
        PDFMetadata existingMetadata = pdfMetadataStore.get(id);
        if (existingMetadata == null) {
            throw new IllegalArgumentException("No PDF metadata found for the given ID.");
        }

        // Load the original PDF from the saved path
        File originalPdfFile = new File(existingMetadata.getOriginalPdfPath());
        try (PDDocument document = PDDocument.load(originalPdfFile)) {
            // Create new segments based on the new positions
            List<File> newSegmentFiles = createSegments(document, newPositions);

            // Create a new zip file with updated segmentation
            byte[] newZipBytes = createZipFromFiles(newSegmentFiles);

            // Save the new zip file
            File newZipFile = new File("ZippedFile/" + id + ".zip");
            try (FileOutputStream fos = new FileOutputStream(newZipFile)) {
                fos.write(newZipBytes);
            }

            // Update metadata
            existingMetadata.setNumberOfSegment(newPositions.size());
            for (int i = 0; i < newSegmentFiles.size(); i++) {
                existingMetadata.setSegmentDetails(new SegmentDetails((int) newSegmentFiles.get(i).length(), i + 1));
            }
            existingMetadata.setZipFilePath(newZipFile.getAbsolutePath());
            pdfMetadataStore.put(id, existingMetadata);

            return newZipBytes;
        } catch (IOException e) {
            // Handle and log the exception
            throw new IOException("Error modifying segmentation", e);
        }
    }


    @Override
    public void deletePdf(String id) {
        // Remove metadata and ZIP file
        pdfMetadataStore.remove(id);
        zipFileStore.remove(id);
    }

    private List<Integer> findCutPositions(PDDocument document, int cuts) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setSortByPosition(true);

        List<Integer> cutPositions = new ArrayList<>();
        int numberOfPages = document.getNumberOfPages();
        for (int i = 1; i <= cuts && i < numberOfPages; i++) {
            cutPositions.add(i);
        }

        return cutPositions;
    }

    private List<File> createSegments(PDDocument document, List<Integer> cutPositions) throws IOException {
        List<File> segmentFiles = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < cutPositions.size(); i++) {
            int end = cutPositions.get(i);
            try (PDDocument segment = new PDDocument()) {
                for (int j = start; j < end; j++) {
                    PDPage page = document.getPage(j);
                    segment.addPage(page);
                }

                File segmentFile = File.createTempFile("segment_" + i, ".pdf");
                segment.save(new FileOutputStream(segmentFile));
                segmentFiles.add(segmentFile);
                start = end;
            }
        }

        if (start < document.getNumberOfPages()) {
            try (PDDocument lastSegment = new PDDocument()) {
                for (int i = start; i < document.getNumberOfPages(); i++) {
                    PDPage page = document.getPage(i);
                    lastSegment.addPage(page);
                }

                File lastSegmentFile = File.createTempFile("segment_last", ".pdf");
                lastSegment.save(new FileOutputStream(lastSegmentFile));
                segmentFiles.add(lastSegmentFile);
            }
        }

        return segmentFiles;
    }

    private byte[] createZipFromFiles(List<File> files) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
            return baos.toByteArray();
        }
    }
    public File saveFile(String id, MultipartFile file) throws IOException {
        // Use an absolute directory path where you know the application has write access
        String absolutePath = "C:/Users/91969/Downloads";  // Adjust this path as needed
        File uploadDir = new File(absolutePath);

        // Check if directory exists, if not, create it
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();  // Creates the directory if it does not exist
        }

        // Define the path for the new PDF file
        File originalPdfFile = new File(uploadDir, id + ".pdf");

        try {
            // Transfer the uploaded file to the defined location
            file.transferTo(originalPdfFile);
            System.out.println("File saved successfully at: " + originalPdfFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to save the file.", e);
        }

        // Return the saved file
        return originalPdfFile;
    }
    public File saveZipFile(String id, byte[] zipBytes) throws IOException {
        // Define the absolute directory path for saving the zip file
        String absolutePath = "C:/Users/91969/Downloads";  // Adjust this to a valid absolute path
        File zipDir = new File(absolutePath);

        // Check if directory exists, if not, create it
        if (!zipDir.exists()) {
            zipDir.mkdirs();  // Create the directory if it does not exist
        }

        // Define the file path for the new zip file
        File zipFile = new File(zipDir, id + ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile)) {
            fos.write(zipBytes);
            fos.flush();  // Ensure all data is written to the file
            System.out.println("Zip file saved successfully at: " + zipFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to save the zip file.", e);
        }

        // Return the saved zip file
        return zipFile;
    }


}
