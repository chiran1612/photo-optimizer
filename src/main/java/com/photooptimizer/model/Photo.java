package com.photooptimizer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Simple Photo entity for storing photo information
 */
@Entity
@Table(name = "photos")
public class Photo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String originalName;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    private String optimizedPath;
    private String thumbnailPath;
    
    @Column(nullable = false)
    private Long fileSize;
    
    private String format;
    private Integer width;
    private Integer height;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    private LocalDateTime optimizedAt;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private String googleDriveId;
    
    // Constructors
    public Photo() {}
    
    public Photo(String originalName, String fileName, String filePath, Long fileSize) {
        this.originalName = originalName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.uploadedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getOptimizedPath() { return optimizedPath; }
    public void setOptimizedPath(String optimizedPath) { this.optimizedPath = optimizedPath; }
    
    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public LocalDateTime getOptimizedAt() { return optimizedAt; }
    public void setOptimizedAt(LocalDateTime optimizedAt) { this.optimizedAt = optimizedAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getGoogleDriveId() { return googleDriveId; }
    public void setGoogleDriveId(String googleDriveId) { this.googleDriveId = googleDriveId; }
}
