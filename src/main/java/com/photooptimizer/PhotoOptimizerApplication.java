package com.photooptimizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Photo Optimizer Application
 * 
 * A comprehensive tool for photo optimization, version management, and Google Drive synchronization.
 * 
 * Features:
 * - Photo upload and optimization
 * - Version management with history tracking
 * - Multiple format support (JPEG, PNG, WebP, etc.)
 * - Thumbnail generation
 * - Google Drive integration for cloud storage
 * - Simple HTML frontend interface
 * 
 * @author Photo Optimizer Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PhotoOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoOptimizerApplication.class, args);
        System.out.println("🚀 Photo Optimizer Application Started!");
        System.out.println("📸 Access the application at: http://localhost:8080/photo-optimizer");
        System.out.println("📊 Database console at: http://localhost:8080/photo-optimizer/h2-console");
    }
}
