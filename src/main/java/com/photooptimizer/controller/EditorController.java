package com.photooptimizer.controller;

import com.photooptimizer.model.Photo;
import com.photooptimizer.service.PhotoService;
import com.photooptimizer.service.EditorService;
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
    
    /**
     * Display the photo editor interface
     */
    @GetMapping("/editor/{id}")
    public String editor(@PathVariable Long id, Model model) {
        Photo photo = photoService.getPhotoById(id);
        if (photo == null) {
            return "redirect:/photo-optimizer/";
        }
        
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
}
