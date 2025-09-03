# Shipment API Documentation

This document describes the Shipment API endpoints for creating and managing shipments with image uploads using MongoDB GridFS.

## Overview

The Shipment API allows users to:
- Create shipments with comprehensive details
- Upload multiple images along with shipment data
- Store images in MongoDB GridFS for efficient storage
- **Automatic tracking number generation** with custom incremental format
- Retrieve shipments by ID or tracking number
- Update shipment status
- Delete shipments and associated images

## API Endpoints

### Base URL
```
http://localhost:8080/api/shipments
```

### 1. Create Shipment with Images (Multipart Form Data)

**Endpoint:** `POST /api/shipments/with-images`

**Content-Type:** `multipart/form-data`

**Request Parts:**
- `shipmentData`: **String** containing JSON shipment information (required)
- `images`: Array of image files (optional)

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/shipments/with-images \
  -F "shipmentData={\"basicInformation\":{\"shipmentTitle\":\"Electronics Package\",\"destination\":\"New York\",\"barcode\":\"123456789\"},\"notes\":\"Handle with care\",\"createdBy\":\"user123\"}" \
  -F "images=@image1.jpg" \
  -F "images=@image2.jpg"
```

**Important Notes:**
- `shipmentData` must be sent as a **string parameter**, not as a file
- The JSON string should be properly escaped if using cURL
- Include the `createdBy` field in the JSON string
- Images are optional - you can send just the shipment data without images

**Common Mistakes to Avoid:**
- ❌ Don't send `shipmentData` as a file upload
- ❌ Don't use `@RequestPart` in your client code
- ✅ Send `shipmentData` as a string parameter
- ✅ Use `@RequestParam` in your client code

**Example Response:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "basicInformation": {
    "shipmentTitle": "Electronics Package",
    "destination": "New York",
    "barcode": "123456789"
  },
  "trackingNumber": "DKIQ20240115000001",
  "notes": "Handle with care",
  "imageIds": ["507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013"],
  "status": "CREATED",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "createdBy": "user123",
  "lastModifiedBy": "user123"
}
```

**Note:** If `trackingNumber` is not provided in `basicInformation`, the system will automatically generate one.

### 2. Create Shipment without Images (JSON)

**Endpoint:** `POST /api/shipments`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "basicInformation": {
    "shipmentTitle": "Documents Package",
    "destination": "Los Angeles",
    "barcode": "987654321",
    "origin": "San Francisco",
    "carrier": "FedEx",
    "trackingNumber": "FX123456789",
    "weight": 2.5,
    "weightUnit": "kg",
    "priority": "Standard"
  },
  "customerFields": {
    "customerId": "CUST001",
    "accountType": "Premium"
  },
  "notes": "Urgent delivery required",
  "deviceInformation": "Mobile app - iOS 17.0",
  "createdBy": "user123"
}
```

**Note:** If `trackingNumber` is not provided in `basicInformation`, the system will automatically generate one.

### 3. Get Shipment by ID

**Endpoint:** `GET /api/shipments/{id}`

**Example:**
```bash
curl http://localhost:8080/api/shipments/507f1f77bcf86cd799439011
```

### 4. Get Shipment by Tracking Number

**Endpoint:** `GET /api/shipments/tracking/{trackingNumber}`

**Example:**
```bash
curl http://localhost:8080/api/shipments/tracking/DKIQ20240115000001
```

### 5. Get All Shipments

**Endpoint:** `GET /api/shipments`

**Example:**
```bash
curl http://localhost:8080/api/shipments
```

### 6. Update Shipment Status

**Endpoint:** `PUT /api/shipments/{id}/status`

**Request Body:** Status string (e.g., "IN_TRANSIT", "DELIVERED")

**Example:**
```bash
curl -X PUT http://localhost:8080/api/shipments/507f1f77bcf86cd799439011/status \
  -H "Content-Type: application/json" \
  -d "IN_TRANSIT"
```

### 7. Delete Shipment

**Endpoint:** `DELETE /api/shipments/{id}`

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/shipments/507f1f77bcf86cd799439011
```

## Data Models

### AddShipment (Request Model)
```json
{
  "basicInformation": {
    "shipmentTitle": "string (required, max 100 chars)",
    "destination": "string (required, max 200 chars)",
    "barcode": "string (required, max 50 chars)",
    "origin": "string (optional)",
    "carrier": "string (optional)",
    "trackingNumber": "string (optional - will auto-generate if not provided)",
    "weight": "number (optional)",
    "weightUnit": "string (optional)",
    "dimensions": "number (optional)",
    "dimensionUnit": "string (optional)",
    "priority": "string (optional)",
    "estimatedDeliveryDate": "string (optional)"
  },
  "customerFields": "object (optional)",
  "imageIds": "array of strings (optional, max 10 images)",
  "notes": "string (optional, max 1000 chars)",
  "deviceInformation": "string (optional, max 500 chars)",
  "createdBy": "string (required)"
}
```

### Shipment (Response Model)
```json
{
  "id": "string",
  "barcode": "string",
  "trackingNumber": "string (auto-generated if not provided)",
  "basicInformation": "BasicInformation object",
  "customerFields": "object",
  "imageIds": "array of strings",
  "notes": "string",
  "deviceInformation": "string",
  "status": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "createdBy": "string",
  "lastModifiedBy": "string",
  "tags": "array of strings"
}
```

## Tracking Number Generation

### Automatic Generation
The system automatically generates tracking numbers for shipments when none is provided. The format follows a specific pattern:

**Format:** `PREFIX + YYYYMMDD + 6-digit sequence`

**Example:** `DKIQ20240115000001`

Where:
- `DKIQ` = Default prefix (configurable)
- `20240115` = Date in YYYYMMDD format
- `000001` = Daily sequence number (6 digits, zero-padded)

### Custom Tracking Numbers
You can still provide your own tracking number in the `basicInformation.trackingNumber` field. The system will use your custom number instead of generating one.

### Sequence Logic
- Each day starts with sequence 000001
- Sequence increments for each shipment created that day
- Sequence resets to 000001 each new day
- Maximum 999,999 shipments per day

### Custom Prefixes
The system supports custom prefixes for different types of shipments:
- Default: `DKIQ` (DockerIQ)
- Can be configured for different carriers or regions
- Format: `CUSTOM_PREFIX + YYYYMMDD + 6-digit sequence`

## Image Upload Guidelines

### Supported Formats
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

### File Size Limits
- **Maximum file size per image: 10MB**
- **Maximum request size (total): 50MB**
- **Maximum images per shipment: 10**
- **File size threshold: 2KB**

### Storage
- Images are stored in MongoDB GridFS
- Each image gets a unique ObjectId
- Images are stored in chunks for efficient retrieval
- Metadata includes content type and upload timestamp

## Error Handling

### Common HTTP Status Codes
- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data or missing required fields
- `404 Not Found`: Resource not found
- `413 Payload Too Large`: File upload size exceeded
- `500 Internal Server Error`: Server error

### Error Response Format
```json
{
  "error": "Error message description"
}
```

### Validation Errors
The API will return `400 Bad Request` for:
- Missing basic information
- Missing createdBy field
- Invalid data format
- Validation constraint violations
- Individual file size exceeds 10MB

### File Upload Errors
The API will return `413 Payload Too Large` for:
- Total request size exceeds 50MB
- Individual file size exceeds 10MB

## Troubleshooting

### Common Issues

#### 1. "Maximum upload size exceeded" Error
**Problem:** File upload fails with size exceeded error.

**Solutions:**
- Ensure individual image files are under 10MB
- Ensure total request size (all files + data) is under 50MB
- Compress large images before upload
- Use image optimization tools to reduce file size

**Configuration Check:**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

#### 2. File Upload Fails
**Problem:** Images are not being uploaded successfully.

**Solutions:**
- Check file format (only JPEG, PNG, GIF, WebP supported)
- Verify Content-Type is `multipart/form-data` for image uploads
- Ensure `shipmentData` and `images` parts are properly named
- Check server logs for detailed error messages

#### 3. Content-Type Not Supported Error
**Problem:** Getting "Content-Type not supported" error.

**Solutions:**
- For JSON requests: Use `POST /api/shipments` with `Content-Type: application/json`
- For image uploads: Use `POST /api/shipments/with-images` with `Content-Type: multipart/form-data`
- Ensure proper request format for each type

#### 4. Multipart Endpoint Content-Type Error
**Problem:** Getting "Content-Type 'multipart/form-data' is not supported" error on `/with-images` endpoint.

**Solutions:**
- Ensure you're using `POST /api/shipments/with-images` (not `/api/shipments`)
- Send `shipmentData` as a **string parameter**, not as a file
- Use `@RequestParam` in your client code, not `@RequestPart`
- Check that your client is properly setting `Content-Type: multipart/form-data`

**Correct Client Implementation:**
```java
// ✅ CORRECT - Send as string parameter
@RequestParam("shipmentData") String shipmentDataJson

// ❌ WRONG - Don't send as file or use @RequestPart
@RequestPart("shipmentData") AddShipment addShipment
```

### Debug Steps
1. **Check File Sizes:** Verify all images are under 10MB
2. **Check Request Format:** Ensure proper multipart form data for image uploads
3. **Check Content-Type:** Use appropriate Content-Type header
4. **Check Server Logs:** Look for detailed error messages
5. **Test with Small Files:** Start with small images to verify functionality
6. **Check Configuration:** Verify file upload settings in application.yml

## Authentication & Security

Currently, the API endpoints are publicly accessible. In production, consider implementing:
- JWT token authentication
- Role-based access control
- Rate limiting
- Input validation and sanitization

## Testing

### Using Postman

#### For Image Uploads:
1. Set request type to POST
2. Set URL to `http://localhost:8080/api/shipments/with-images`
3. Set body type to `form-data`
4. Add `shipmentData` as text with JSON content (include `createdBy` field)
5. Add `images` as file(s) - ensure each is under 10MB
6. Send request

#### For JSON Requests:
1. Set request type to POST
2. Set URL to `http://localhost:8080/api/shipments`
3. Set body type to `raw` and select `JSON`
4. Add JSON payload with `createdBy` field
5. Send request

**Note:** The response will include an auto-generated `trackingNumber` if none was provided in the request.

### Using cURL
See examples above for each endpoint.

**Important:** When testing with cURL, ensure file sizes are within limits and use proper Content-Type headers.

## Dependencies

- Spring Boot 3.2.0
- Spring Data MongoDB
- MongoDB GridFS
- Lombok
- Jackson for JSON processing

## Configuration

The API uses MongoDB GridFS for image storage. Ensure MongoDB is running and accessible at the configured URI in `application.yml`.

GridFS bucket name: `shipment_images`
Database: `dockeriq`

### File Upload Configuration
```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
      file-size-threshold: 2KB
      location: ${java.io.tmpdir}
```

## Architecture Notes

The API follows a clean separation of concerns:
- **Controller**: Handles HTTP requests/responses and delegates to service layer
- **Service**: Contains business logic, validation, and orchestrates operations
- **Repository**: Handles data persistence operations
- **GridFS Service**: Manages file storage operations
- **Tracking Number Generator**: Handles automatic tracking number generation

All business logic has been moved from the controller to the service layer for better maintainability and testability.

## Tracking Number Examples

Here are some examples of auto-generated tracking numbers:

```
DKIQ20240115000001  // First shipment on Jan 15, 2024
DKIQ20240115000002  // Second shipment on Jan 15, 2024
DKIQ20240115000003  // Third shipment on Jan 15, 2024
DKIQ20240116000001  // First shipment on Jan 16, 2024 (sequence resets)
DKIQ20240116000002  // Second shipment on Jan 16, 2024
```

The system ensures each tracking number is unique and follows a predictable, sortable format.
