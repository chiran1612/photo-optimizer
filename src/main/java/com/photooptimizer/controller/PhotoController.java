package com.photooptimizer.controller;

import com.photooptimizer.model.Photo;
import com.photooptimizer.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Simple photo controller for web interface
 */
@Controller
public class PhotoController {
    
    @Autowired
    private PhotoService photoService;
    
    @GetMapping("/")
    public String index(Model model) {
        List<Photo> photos = photoService.getAllPhotos();
        model.addAttribute("photos", photos);
        return "index";
    }
    
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }
            
            photoService.uploadPhoto(file);
            return ResponseEntity.ok("Photo uploaded successfully!");
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading photo: " + e.getMessage());
        }
    }
    
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePhoto(@PathVariable Long id) {
        try {
            photoService.deletePhoto(id);
            return ResponseEntity.ok("Photo deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting photo: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/photos")
    @ResponseBody
    public ResponseEntity<List<Photo>> getAllPhotosApi() {
        List<Photo> photos = photoService.getAllPhotos();
        return ResponseEntity.ok(photos);
    }
    
    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        Photo photo = photoService.getPhotoById(id);
        if (photo != null) {
            try {
                Path imagePath = Paths.get(photo.getFilePath());
                byte[] imageBytes = Files.readAllBytes(imagePath);
                
                String contentType = "image/" + photo.getFormat();
                if (photo.getFormat().equalsIgnoreCase("jpg")) {
                    contentType = "image/jpeg";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(imageBytes);
            } catch (IOException e) {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/photo/{id}/metadata")
    @ResponseBody
    public ResponseEntity<Photo> getPhotoMetadata(@PathVariable Long id) {
        Photo photo = photoService.getPhotoById(id);
        if (photo != null) {
            return ResponseEntity.ok(photo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
