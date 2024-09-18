# SegmentaPDF

SegmentaPDF is a Spring Boot application designed for segmenting PDF files and managing the segmented files. The application supports uploading PDF files, segmenting them into multiple parts, zipping these parts, and managing metadata related to the segments.

## Features

- **Segment PDF**: Upload a PDF file and segment it into multiple parts based on the number of cuts specified.
- **Download Segmented PDF**: Download the segmented PDF as a zip file.
- **Update Segmentation**: Update the segmentation of an existing PDF by changing the number of cuts.
- **Modify Segmentation**: Modify the segmentation of an existing PDF based on new cut positions.
- **Delete PDF**: Delete a PDF and its associated metadata.

## API Endpoints

### 1. Segment PDF

- **Endpoint**: `POST /api/segment-pdf`
- **Parameters**:
    - `file`: The PDF file to be segmented (multipart file).
    - `cuts`: The number of segments to create.
- **Response**: Returns a unique ID for the segmented PDF.

### 2. Download Segmented PDF

- **Endpoint**: `GET /api/download-pdf/{id}`
- **Path Variable**:
    - `id`: The unique ID of the segmented PDF.
- **Response**: Returns the segmented PDF as a zip file.

### 3. Get PDF Metadata

- **Endpoint**: `GET /api/pdf-metadata/{id}`
- **Path Variable**:
    - `id`: The unique ID of the PDF.
- **Response**: Returns metadata about the segmented PDF.

### 4. Update Segmentation

- **Endpoint**: `PUT /api/update-segmentation/{id}`
- **Path Variable**:
    - `id`: The unique ID of the segmented PDF.
- **Parameters**:
    - `cuts`: The new number of segments.
- **Response**: Returns the updated segmented PDF as a zip file.

### 5. Modify Segmentation

- **Endpoint**: `PATCH /api/modify-segmentation/{id}`
- **Path Variable**:
    - `id`: The unique ID of the segmented PDF.
- **Request Body**: JSON array of new cut positions.
- **Response**: Returns the modified segmented PDF as a zip file.

### 6. Delete PDF

- **Endpoint**: `DELETE /api/delete-pdf/{id}`
- **Path Variable**:
    - `id`: The unique ID of the PDF.
- **Response**: Returns a status of 204 No Content.

## API Documentation

The API documentation is available via Swagger UI. To view the documentation:

1. **Start the Application**: Ensure your application is running.
2. **Access Swagger UI**: Navigate to `http://localhost:8080/swagger-ui.html` in your web browser.

This will open the Swagger UI interface, where you can explore and interact with the API endpoints.



