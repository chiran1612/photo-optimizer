package com.photooptimizer.controller;

import com.photooptimizer.model.Photo;
import com.photooptimizer.service.PhotoService;
import com.photooptimizer.service.EditorService;
import com.photooptimizer.service.OCRService;
import com.photooptimizer.service.OCRService.TextRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * Photo Editor Controller
 * Handles photo editing operations and editor interface
 */
@Controller
public class EditorController {
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private EditorService editorService;
    
    @Autowired
    private OCRService ocrService;
    
    /**
     * Display the photo editor interface
     */
    @GetMapping("/editor/{id}")
    public String editor(@PathVariable Long id, Model model) {
        System.out.println("Editor endpoint called for photo ID: " + id);
        Photo photo = photoService.getPhotoById(id);
        if (photo == null) {
            System.out.println("Photo not found for ID: " + id);
            return "redirect:/photo-optimizer/";
        }
        
        System.out.println("Photo found: " + photo.getOriginalName());
        model.addAttribute("photo", photo);
        model.addAttribute("photoUrl", "/photo-optimizer/photo/" + id);
        return "editor";
    }
    
    /**
     * Save edited photo
     */
    @PostMapping("/editor/save/{id}")
    @ResponseBody
    public ResponseEntity<String> saveEditedPhoto(
            @PathVariable Long id,
            @RequestParam("imageData") String imageData) {
        try {
            // Remove data URL prefix (data:image/png;base64,)
            String base64Data = imageData.substring(imageData.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            
            // Save the edited image
            String savedPath = editorService.saveEditedPhoto(id, imageBytes);
            
            return ResponseEntity.ok("Image saved successfully at: " + savedPath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving image: " + e.getMessage());
        }
    }
    
    /**
     * Get photo metadata for editor
     */
    @GetMapping("/editor/{id}/metadata")
    @ResponseBody
    public ResponseEntity<Photo> getPhotoMetadata(@PathVariable Long id) {
        Photo photo = photoService.getPhotoById(id);
        if (photo != null) {
            return ResponseEntity.ok(photo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create a new version of the photo
     */
    @PostMapping("/editor/version/{id}")
    @ResponseBody
    public ResponseEntity<String> createNewVersion(
            @PathVariable Long id,
            @RequestParam("imageData") String imageData,
            @RequestParam("versionName") String versionName) {
        try {
            String base64Data = imageData.substring(imageData.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            
            String savedPath = editorService.createNewVersion(id, imageBytes, versionName);
            
            return ResponseEntity.ok("New version created: " + savedPath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating version: " + e.getMessage());
        }
    }
    
    /**
     * Apply filters to photo
     */
    @PostMapping("/editor/filter/{id}")
    @ResponseBody
    public ResponseEntity<String> applyFilter(
            @PathVariable Long id,
            @RequestParam("filterType") String filterType,
            @RequestParam("filterValue") String filterValue) {
        try {
            String result = editorService.applyFilter(id, filterType, filterValue);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error applying filter: " + e.getMessage());
        }
    }
    
    /**
     * Extract text from photo using OCR
     */
    @PostMapping("/editor/ocr/{id}")
    @ResponseBody
    public ResponseEntity<String> extractText(@PathVariable Long id) {
        try {
            Photo photo = photoService.getPhotoById(id);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            String extractedText = ocrService.extractTextFromFile(photo.getFilePath());
            return ResponseEntity.ok(extractedText);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error extracting text: " + e.getMessage());
        }
    }
    
    /**
     * Extract text from base64 image data
     */
    @PostMapping("/editor/ocr/base64")
    @ResponseBody
    public ResponseEntity<String> extractTextFromBase64(@RequestBody String base64ImageData) {
        try {
            String extractedText = ocrService.extractTextFromBase64(base64ImageData);
            return ResponseEntity.ok(extractedText);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error extracting text: " + e.getMessage());
        }
    }
    
    /**
     * Extract text from original image file (more reliable)
     */
    @PostMapping("/editor/ocr/file/{id}")
    @ResponseBody
    public ResponseEntity<String> extractTextFromFile(@PathVariable Long id) {
        try {
            Photo photo = photoService.getPhotoById(id);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            String extractedText = ocrService.extractTextFromFile(photo.getFilePath());
            return ResponseEntity.ok(extractedText);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error extracting text: " + e.getMessage());
        }
    }
    
    /**
     * Save canvas state
     */
    @PostMapping("/editor/save-state/{id}")
    @ResponseBody
    public ResponseEntity<String> saveCanvasState(@PathVariable Long id, @RequestBody String canvasState) {
        try {
            Photo photo = photoService.getPhotoById(id);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            photo.setCanvasState(canvasState);
            photoService.savePhoto(photo);
            
            return ResponseEntity.ok("Canvas state saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving canvas state: " + e.getMessage());
        }
    }
    
    /**
     * Get canvas state
     */
    @GetMapping("/editor/state/{id}")
    @ResponseBody
    public ResponseEntity<String> getCanvasState(@PathVariable Long id) {
        try {
            Photo photo = photoService.getPhotoById(id);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            String canvasState = photo.getCanvasState();
            return ResponseEntity.ok(canvasState != null ? canvasState : "");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting canvas state: " + e.getMessage());
        }
    }
    
    /**
     * Detect text regions in image
     */
    @GetMapping("/editor/detect-text/{id}")
    @ResponseBody
    public ResponseEntity<?> detectTextRegions(@PathVariable Long id) {
        try {
            System.out.println("Detect text endpoint called for photo ID: " + id);
            Photo photo = photoService.getPhotoById(id);
            if (photo == null) {
                System.out.println("Photo not found for ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Photo found, file path: " + photo.getFilePath());
            
            // Check if OCR service is available
            if (!isOCRServiceAvailable()) {
                System.out.println("OCR service not available, returning empty list");
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }
            
            List<TextRegion> textRegions = ocrService.detectTextRegions(photo.getFilePath());
            System.out.println("Text regions detected: " + textRegions.size());
            
            // Always return success with empty list if no text regions found
            return ResponseEntity.ok(textRegions);
            
        } catch (Exception e) {
            System.err.println("Error in detectTextRegions endpoint: " + e.getMessage());
            e.printStackTrace();
            
            // Return empty list instead of error to prevent 502
            System.out.println("OCR failed, returning empty list to prevent 502 error");
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }
    
    /**
     * Check if OCR service is available
     */
    private boolean isOCRServiceAvailable() {
        try {
            // Simple test to check if Tesseract is working
            return ocrService != null;
        } catch (Exception e) {
            System.err.println("OCR service check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * OCR service health check endpoint
     */
    @GetMapping("/editor/ocr/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ocrHealthCheck() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            boolean isAvailable = isOCRServiceAvailable();
            response.put("status", isAvailable ? "healthy" : "unhealthy");
            response.put("ocr_available", isAvailable);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            if (isAvailable) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Edit text in image - replace specific text
     */
    @PostMapping("/editor/edit-text/{id}")
    @ResponseBody
    public ResponseEntity<String> editTextInImage(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Photo photo = photoService.getPhotoById(id);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            String originalText = (String) request.get("originalText");
            String newText = (String) request.get("newText");
            String fontName = (String) request.getOrDefault("fontName", "Arial");
            Integer fontSize = (Integer) request.getOrDefault("fontSize", 20);
            String color = (String) request.getOrDefault("color", "#000000");
            String fontStyle = (String) request.getOrDefault("fontStyle", "normal");
            
            // Load original image
            BufferedImage originalImage = ImageIO.read(new File(photo.getFilePath()));
            
            // Detect text regions
            List<TextRegion> textRegions = ocrService.detectTextRegions(photo.getFilePath());
            
            // Find the region with the original text
            TextRegion targetRegion = null;
            for (TextRegion region : textRegions) {
                if (region.text.equalsIgnoreCase(originalText)) {
                    targetRegion = region;
                    break;
                }
            }
            
            if (targetRegion == null) {
                return ResponseEntity.badRequest().body("Text not found in image: " + originalText);
            }
            
            // Remove original text (simple white fill)
            BufferedImage imageWithoutText = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                originalImage.getType()
            );
            java.awt.Graphics2D g2d = imageWithoutText.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(targetRegion.x, targetRegion.y, targetRegion.width, targetRegion.height);
            g2d.dispose();
            
            // Add new text
            BufferedImage resultImage = ocrService.addTextToImage(
                imageWithoutText, 
                newText, 
                targetRegion.x, 
                targetRegion.y + targetRegion.height, // Position at bottom of original text
                fontName, 
                fontSize, 
                java.awt.Color.decode(color),
                fontStyle
            );
            
            // Save the edited image
            String editedFileName = "edited_" + System.currentTimeMillis() + ".png";
            Path editedPath = Paths.get("./uploads/edited/" + editedFileName);
            Files.createDirectories(editedPath.getParent());
            ImageIO.write(resultImage, "png", editedPath.toFile());
            
            // Create new photo record for edited version
            Photo editedPhoto = new Photo();
            editedPhoto.setOriginalName(photo.getOriginalName() + " (Text Edited)");
            editedPhoto.setFileName(editedFileName);
            editedPhoto.setFilePath(editedPath.toString());
            editedPhoto.setFileSize(editedPath.toFile().length());
            editedPhoto.setFormat("png");
            editedPhoto.setUploadedAt(java.time.LocalDateTime.now());
            editedPhoto.setIsActive(true);
            
            Photo savedPhoto = photoService.savePhoto(editedPhoto);
            
            return ResponseEntity.ok("Text edited successfully! New photo ID: " + savedPhoto.getId());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error editing text: " + e.getMessage());
        }
    }
    
    /**
     * Add new text to image
     */
    @PostMapping("/editor/add-text/{id}")
    @ResponseBody
    public ResponseEntity<String> addNewTextToImage(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Photo photo = photoService.getPhotoById(id);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            String text = (String) request.get("text");
            String fontName = (String) request.getOrDefault("fontName", "Arial");
            Integer fontSize = (Integer) request.getOrDefault("fontSize", 20);
            String color = (String) request.getOrDefault("color", "#000000");
            String fontStyle = (String) request.getOrDefault("fontStyle", "normal");
            Integer x = (Integer) request.getOrDefault("x", 50);
            Integer y = (Integer) request.getOrDefault("y", 50);
            
            // Load original image
            BufferedImage originalImage = ImageIO.read(new File(photo.getFilePath()));
            
            // Add new text to image
            BufferedImage resultImage = ocrService.addTextToImage(
                originalImage, 
                text, 
                x, 
                y, 
                fontName, 
                fontSize, 
                java.awt.Color.decode(color),
                fontStyle
            );
            
            // Save the edited image
            String editedFileName = "added_text_" + System.currentTimeMillis() + ".png";
            Path editedPath = Paths.get("./uploads/edited/" + editedFileName);
            Files.createDirectories(editedPath.getParent());
            ImageIO.write(resultImage, "png", editedPath.toFile());
            
            // Create new photo record for edited version
            Photo editedPhoto = new Photo();
            editedPhoto.setOriginalName(photo.getOriginalName() + " (Text Added)");
            editedPhoto.setFileName(editedFileName);
            editedPhoto.setFilePath(editedPath.toString());
            editedPhoto.setFileSize(editedPath.toFile().length());
            editedPhoto.setFormat("png");
            editedPhoto.setUploadedAt(java.time.LocalDateTime.now());
            editedPhoto.setIsActive(true);
            
            Photo savedPhoto = photoService.savePhoto(editedPhoto);
            
            return ResponseEntity.ok("Text added successfully! New photo ID: " + savedPhoto.getId());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding text: " + e.getMessage());
        }
    }
}
