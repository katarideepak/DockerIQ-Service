package com.dockeriq.service.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GridFSService {
    
    @Autowired
    private GridFsTemplate gridFsTemplate;
    
    @Autowired
    private GridFSBucket gridFSBucket;
    
    /**
     * Store an image in GridFS
     * @param imageData byte array of the image
     * @param filename name of the file
     * @param contentType MIME type of the image
     * @return GridFS file ID
     */
    public String storeImage(byte[] imageData, String filename, String contentType) {
        log.debug("Storing image in GridFS. Filename: {}, Content-Type: {}, Size: {} bytes", 
                filename, contentType, imageData.length);
        try {
            InputStream inputStream = new ByteArrayInputStream(imageData);
            
            GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1024 * 1024) // 1MB chunks
                .metadata(new Document("contentType", contentType)
                    .append("uploadedAt", System.currentTimeMillis()));
            
            ObjectId objectId = gridFSBucket.uploadFromStream(filename, inputStream, options);
            String imageId = objectId.toHexString();
            log.info("Successfully stored image in GridFS. Filename: {}, Image ID: {}", filename, imageId);
            return imageId;
        } catch (Exception e) {
            log.error("Failed to store image in GridFS. Filename: {}. Error: {}", filename, e.getMessage(), e);
            throw new RuntimeException("Failed to store image: " + filename, e);
        }
    }
    
    /**
     * Store multiple images in GridFS
     * @param images list of image data
     * @param filenames list of filenames
     * @param contentTypes list of MIME types
     * @return list of GridFS file IDs
     */
    public List<String> storeMultipleImages(List<byte[]> images, List<String> filenames, List<String> contentTypes) {
        log.info("Storing {} images in GridFS", images.size());
        List<String> imageIds = new ArrayList<>();
        
        for (int i = 0; i < images.size(); i++) {
            log.debug("Storing image {}/{}: {}", i + 1, images.size(), filenames.get(i));
            String imageId = storeImage(images.get(i), filenames.get(i), contentTypes.get(i));
            imageIds.add(imageId);
        }
        
        log.info("Successfully stored {} images in GridFS", images.size());
        return imageIds;
    }
    
    /**
     * Retrieve an image from GridFS
     * @param imageId GridFS file ID
     * @return byte array of the image
     */
    public byte[] retrieveImage(String imageId) {
        log.debug("Retrieving image from GridFS. Image ID: {}", imageId);
        try {
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(new ObjectId(imageId));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytes = 0;
            while ((bytesRead = downloadStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            downloadStream.close();
            byte[] imageData = outputStream.toByteArray();
            log.info("Successfully retrieved image from GridFS. Image ID: {}, Size: {} bytes", 
                    imageId, totalBytes);
            return imageData;
        } catch (Exception e) {
            log.error("Failed to retrieve image from GridFS. Image ID: {}. Error: {}", imageId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve image: " + imageId, e);
        }
    }
    
    /**
     * Delete an image from GridFS
     * @param imageId GridFS file ID
     */
    public void deleteImage(String imageId) {
        log.debug("Deleting image from GridFS. Image ID: {}", imageId);
        try {
            gridFSBucket.delete(new ObjectId(imageId));
            log.info("Successfully deleted image from GridFS. Image ID: {}", imageId);
        } catch (Exception e) {
            log.error("Failed to delete image from GridFS. Image ID: {}. Error: {}", imageId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete image: " + imageId, e);
        }
    }
    
    /**
     * Get image metadata
     * @param imageId GridFS file ID
     * @return GridFS file information
     */
    public GridFSFile getImageInfo(String imageId) {
        log.debug("Retrieving image metadata from GridFS. Image ID: {}", imageId);
        GridFSFile fileInfo = gridFSBucket.find(new Document("_id", new ObjectId(imageId))).first();
        if (fileInfo != null) {
            log.debug("Image metadata found. Image ID: {}, Filename: {}, Size: {} bytes", 
                    imageId, fileInfo.getFilename(), fileInfo.getLength());
        } else {
            log.debug("Image metadata not found. Image ID: {}", imageId);
        }
        return fileInfo;
    }
}
