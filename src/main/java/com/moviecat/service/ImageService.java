package com.moviecat.service;

import com.moviecat.dto.ImageDownloadRequest;
import com.moviecat.dto.ImageResponse;
import com.moviecat.model.Image;
import com.moviecat.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.UUID;

/**
 * Service for managing image downloads and storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    
    private final ImageRepository imageRepository;
    
    @Value("${image.storage.path:images}")
    private String imageStoragePath;
    
    static {
        // Disable SSL certificate validation for image downloads
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // Disable hostname verification
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
    }
    
    /**
     * Download an image from a URL and save it to the file system.
     * 
     * @param request the image download request
     * @return the saved image metadata
     * @throws IOException if download or file operation fails
     */
    public ImageResponse downloadAndSaveImage(ImageDownloadRequest request) throws IOException {
        log.info("Downloading image from URL: {}", request.getImageUrl());
        
        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get(imageStoragePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        
        // Download the image
        URI uri = URI.create(request.getImageUrl());
        String contentType;
        byte[] imageBytes;
        
        try (InputStream inputStream = uri.toURL().openStream()) {
            imageBytes = inputStream.readAllBytes();
            
            // Try to determine content type from URL connection
            contentType = uri.toURL().openConnection().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                // Fallback to detecting from file extension
                contentType = detectContentType(request.getImageUrl());
            }
        }
        
        // Generate unique filename
        String fileExtension = getFileExtension(request.getImageUrl(), contentType);
        String filename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = storageDir.resolve(filename);
        
        // Save file to disk
        Files.write(filePath, imageBytes);
        log.info("Image saved to: {}", filePath);
        
        // Create and save metadata
        Image image = Image.builder()
                .originalUrl(request.getImageUrl())
                .filename(filename)
                .contentType(contentType)
                .fileSize((long) imageBytes.length)
                .build();
        
        image = imageRepository.save(image);
        log.info("Image metadata saved with ID: {}", image.getId());
        
        return toResponse(image);
    }
    
    /**
     * Get image metadata by ID.
     * 
     * @param id the image ID
     * @return the image metadata
     */
    public ImageResponse getImageById(String id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
        return toResponse(image);
    }
    
    /**
     * Get the actual image file as a Resource.
     * 
     * @param id the image ID
     * @return the image file as a Resource
     * @throws MalformedURLException if the file path is invalid
     */
    public Resource getImageFile(String id) throws MalformedURLException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
        
        Path filePath = Paths.get(imageStoragePath).resolve(image.getFilename());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Image file not found or not readable: " + image.getFilename());
        }
        
        return resource;
    }
    
    /**
     * Delete an image by ID.
     * 
     * @param id the image ID
     */
    public void deleteImage(String id) throws IOException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
        
        // Delete file from disk
        Path filePath = Paths.get(imageStoragePath).resolve(image.getFilename());
        Files.deleteIfExists(filePath);
        
        // Delete metadata from database
        imageRepository.deleteById(id);
        log.info("Image deleted: {}", id);
    }
    
    /**
     * Convert Image entity to ImageResponse DTO.
     */
    private ImageResponse toResponse(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .originalUrl(image.getOriginalUrl())
                .filename(image.getFilename())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .uploadedAt(image.getUploadedAt())
                .imageUrl("/api/images/" + image.getId())
                .build();
    }
    
    /**
     * Detect content type from URL.
     */
    private String detectContentType(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.endsWith(".png")) {
            return "image/png";
        } else if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerUrl.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg"; // default
    }
    
    /**
     * Get file extension based on URL and content type.
     */
    private String getFileExtension(String url, String contentType) {
        // Try to get extension from URL first
        if (url.contains(".")) {
            String urlExtension = url.substring(url.lastIndexOf("."));
            if (urlExtension.length() <= 5 && urlExtension.matches("\\.[a-zA-Z0-9]+")) {
                return urlExtension;
            }
        }
        
        // Fallback to content type
        if (contentType != null) {
            if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                return ".jpg";
            } else if (contentType.contains("png")) {
                return ".png";
            } else if (contentType.contains("gif")) {
                return ".gif";
            } else if (contentType.contains("webp")) {
                return ".webp";
            }
        }
        
        return ".jpg"; // default
    }
}
