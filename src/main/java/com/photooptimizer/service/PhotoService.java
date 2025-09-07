package com.photooptimizer.service;

import com.photooptimizer.model.Photo;
import com.photooptimizer.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Simple photo service for basic operations
 */
@Service
public class PhotoService {
    
    @Autowired
    private PhotoRepository photoRepository;
    
    private final String uploadDir = "./uploads/";
    
    public List<Photo> getAllPhotos() {
        return photoRepository.findByIsActiveTrueOrderByUploadedAtDesc();
    }
    
    public Photo uploadPhoto(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
        
        // Generate unique filename
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // Create photo record
        Photo photo = new Photo();
        photo.setOriginalName(originalName);
        photo.setFileName(fileName);
        photo.setFilePath(filePath.toString());
        photo.setFileSize(file.getSize());
        photo.setFormat(extension.substring(1).toLowerCase());
        photo.setUploadedAt(LocalDateTime.now());
        
        return photoRepository.save(photo);
    }
    
    public void deletePhoto(Long id) {
        Photo photo = photoRepository.findById(id).orElse(null);
        if (photo != null) {
            // Delete file from filesystem
            try {
                Files.deleteIfExists(Paths.get(photo.getFilePath()));
            } catch (IOException e) {
                // Log error but continue with database deletion
            }
            
            // Mark as inactive in database
            photo.setIsActive(false);
            photoRepository.save(photo);
        }
    }
    
    public Photo getPhotoById(Long id) {
        return photoRepository.findById(id).orElse(null);
    }
    
    public Photo savePhoto(Photo photo) {
        return photoRepository.save(photo);
    }
}
