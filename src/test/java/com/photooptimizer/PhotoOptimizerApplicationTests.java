package com.photooptimizer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic test class for Photo Optimizer Application
 */
@SpringBootTest
@ActiveProfiles("test")
class PhotoOptimizerApplicationTests {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
        // This is a basic smoke test to ensure the application starts
    }
    
    @Test
    void applicationStarts() {
        // Test that the main application class can be instantiated
        PhotoOptimizerApplication app = new PhotoOptimizerApplication();
        assert app != null;
    }
}
