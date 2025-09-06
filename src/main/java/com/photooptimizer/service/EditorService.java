package com.photooptimizer.service;

import com.photooptimizer.model.Photo;
import com.photooptimizer.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Photo Editor Service
 * Handles photo editing operations and image processing
 */
@Service
public class EditorService {
    
    @Autowired
    private PhotoRepository photoRepository;
    
    private final String editedDir = "./uploads/edited/";
    private final String versionsDir = "./uploads/versions/";
    
    /**
     * Save edited photo
     */
    public String saveEditedPhoto(Long photoId, byte[] imageBytes) throws IOException {
        Photo originalPhoto = photoRepository.findById(photoId).orElse(null);
        if (originalPhoto == null) {
            throw new RuntimeException("Photo not found");
        }
        
        // Create edited directory if it doesn't exist
        File editedDirFile = new File(editedDir);
        if (!editedDirFile.exists()) {
            editedDirFile.mkdirs();
        }
        
        // Generate new filename for edited version
        String originalName = originalPhoto.getOriginalName();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String fileName = "edited_" + UUID.randomUUID().toString() + extension;
        
        // Save the edited image
        Path filePath = Paths.get(editedDir + fileName);
        Files.write(filePath, imageBytes);
        
        // Update the original photo record
        originalPhoto.setOptimizedPath(filePath.toString());
        originalPhoto.setOptimizedAt(LocalDateTime.now());
        photoRepository.save(originalPhoto);
        
        return filePath.toString();
    }
    
    /**
     * Create a new version of the photo
     */
    public String createNewVersion(Long photoId, byte[] imageBytes, String versionName) throws IOException {
        Photo originalPhoto = photoRepository.findById(photoId).orElse(null);
        if (originalPhoto == null) {
            throw new RuntimeException("Photo not found");
        }
        
        // Create versions directory if it doesn't exist
        File versionsDirFile = new File(versionsDir);
        if (!versionsDirFile.exists()) {
            versionsDirFile.mkdirs();
        }
        
        // Generate filename for new version
        String originalName = originalPhoto.getOriginalName();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String fileName = versionName + "_" + UUID.randomUUID().toString() + extension;
        
        // Save the new version
        Path filePath = Paths.get(versionsDir + fileName);
        Files.write(filePath, imageBytes);
        
        // Create new photo record for the version
        Photo versionPhoto = new Photo();
        versionPhoto.setOriginalName(versionName + "_" + originalName);
        versionPhoto.setFileName(fileName);
        versionPhoto.setFilePath(filePath.toString());
        versionPhoto.setFileSize((long) imageBytes.length);
        versionPhoto.setFormat(extension.substring(1).toLowerCase());
        versionPhoto.setUploadedAt(LocalDateTime.now());
        versionPhoto.setIsActive(true);
        photoRepository.save(versionPhoto);
        
        return filePath.toString();
    }
    
    /**
     * Apply filters to photo
     */
    public String applyFilter(Long photoId, String filterType, String filterValue) throws IOException {
        Photo photo = photoRepository.findById(photoId).orElse(null);
        if (photo == null) {
            throw new RuntimeException("Photo not found");
        }
        
        // Read the original image
        BufferedImage originalImage = ImageIO.read(new File(photo.getFilePath()));
        BufferedImage filteredImage = null;
        
        // Apply different filters based on type
        switch (filterType.toLowerCase()) {
            case "brightness":
                filteredImage = applyBrightness(originalImage, Float.parseFloat(filterValue));
                break;
            case "contrast":
                filteredImage = applyContrast(originalImage, Float.parseFloat(filterValue));
                break;
            case "grayscale":
                filteredImage = applyGrayscale(originalImage);
                break;
            case "sepia":
                filteredImage = applySepia(originalImage);
                break;
            case "blur":
                filteredImage = applyBlur(originalImage, Integer.parseInt(filterValue));
                break;
            default:
                throw new RuntimeException("Unknown filter type: " + filterType);
        }
        
        // Save the filtered image
        String fileName = "filtered_" + UUID.randomUUID().toString() + ".png";
        Path filePath = Paths.get(editedDir + fileName);
        
        // Create edited directory if it doesn't exist
        File editedDirFile = new File(editedDir);
        if (!editedDirFile.exists()) {
            editedDirFile.mkdirs();
        }
        
        ImageIO.write(filteredImage, "png", filePath.toFile());
        
        return "Filter applied successfully. Saved to: " + filePath.toString();
    }
    
    /**
     * Apply brightness filter
     */
    private BufferedImage applyBrightness(BufferedImage image, float brightness) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (int) Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) * brightness));
                int g = (int) Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) * brightness));
                int b = (int) Math.min(255, Math.max(0, (rgb & 0xFF) * brightness));
                
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        
        return result;
    }
    
    /**
     * Apply contrast filter
     */
    private BufferedImage applyContrast(BufferedImage image, float contrast) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (int) Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) * contrast));
                int g = (int) Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) * contrast));
                int b = (int) Math.min(255, Math.max(0, (rgb & 0xFF) * contrast));
                
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        
        return result;
    }
    
    /**
     * Apply grayscale filter
     */
    private BufferedImage applyGrayscale(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                
                result.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        
        return result;
    }
    
    /**
     * Apply sepia filter
     */
    private BufferedImage applySepia(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                int newR = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
                int newG = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
                int newB = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));
                
                result.setRGB(x, y, (newR << 16) | (newG << 8) | newB);
            }
        }
        
        return result;
    }
    
    /**
     * Apply blur filter (simple box blur)
     */
    private BufferedImage applyBlur(BufferedImage image, int radius) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int r = 0, g = 0, b = 0, count = 0;
                
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        
                        if (nx >= 0 && nx < image.getWidth() && ny >= 0 && ny < image.getHeight()) {
                            int rgb = image.getRGB(nx, ny);
                            r += (rgb >> 16) & 0xFF;
                            g += (rgb >> 8) & 0xFF;
                            b += rgb & 0xFF;
                            count++;
                        }
                    }
                }
                
                r /= count;
                g /= count;
                b /= count;
                
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        
        return result;
    }
}
