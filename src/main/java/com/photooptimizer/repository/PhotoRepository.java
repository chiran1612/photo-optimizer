package com.photooptimizer.repository;

import com.photooptimizer.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Simple repository for Photo entity
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    List<Photo> findByIsActiveTrueOrderByUploadedAtDesc();
    
    List<Photo> findByOriginalNameContainingIgnoreCase(String name);
    
    Photo findByFileName(String fileName);
}
