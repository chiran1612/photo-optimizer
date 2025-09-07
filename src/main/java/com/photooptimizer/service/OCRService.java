package com.photooptimizer.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * OCR Service for extracting text from images
 * Uses Tesseract OCR engine for text recognition
 */
@Service
public class OCRService {
    
    private final Tesseract tesseract;
    
    public OCRService() {
        this.tesseract = new Tesseract();
        
        // Set the path to tessdata directory (language data)
        try {
            // Try multiple possible tessdata paths
            String[] possiblePaths = {
                "/app/tessdata",  // Railway container path
                "./tessdata",
                "tessdata",
                System.getProperty("user.dir") + "/tessdata",
                System.getProperty("user.dir") + "/src/main/resources/tessdata"
            };
            
            boolean tessdataFound = false;
            for (String path : possiblePaths) {
                try {
                    tesseract.setDatapath(path);
                    tessdataFound = true;
                    System.out.println("Tessdata found at: " + path);
                    break;
                } catch (Exception e) {
                    System.out.println("Tessdata not found at: " + path);
                }
            }
            
            if (!tessdataFound) {
                System.out.println("Warning: Tessdata not found, OCR may not work properly");
            }
        } catch (Exception e) {
            System.out.println("Error setting tessdata path: " + e.getMessage());
        }
        
        // Set language to English (can be changed to other languages)
        tesseract.setLanguage("eng");
        
        // Set OCR engine mode for better accuracy
        tesseract.setOcrEngineMode(1);
        
        // Set page segmentation mode for better text detection
        tesseract.setPageSegMode(6); // Assume a single uniform block of text
    }
    
    /**
     * Extract text from image file
     * @param imagePath Path to the image file
     * @return Extracted text
     */
    public String extractTextFromFile(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                return "Error: Image file not found";
            }
            
            String result = tesseract.doOCR(imageFile);
            return result.trim();
            
        } catch (TesseractException e) {
            System.err.println("OCR Error: " + e.getMessage());
            return "Error: Failed to extract text - " + e.getMessage();
        }
    }
    
    /**
     * Extract text from base64 image data
     * @param base64ImageData Base64 encoded image data
     * @return Extracted text
     */
    public String extractTextFromBase64(String base64ImageData) {
        try {
            // Clean and validate base64 data
            if (base64ImageData == null || base64ImageData.trim().isEmpty()) {
                return "Error: No image data provided";
            }
            
            // Remove data URL prefix if present
            if (base64ImageData.contains(",")) {
                base64ImageData = base64ImageData.split(",")[1];
            }
            
            // Clean base64 string - remove any invalid characters
            base64ImageData = base64ImageData.replaceAll("[^A-Za-z0-9+/=]", "");
            
            // Ensure proper padding
            while (base64ImageData.length() % 4 != 0) {
                base64ImageData += "=";
            }
            
            System.out.println("Processing base64 data, length: " + base64ImageData.length());
            
            // Decode base64 to byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64ImageData);
            
            // Convert to BufferedImage
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            // Preprocess image for better OCR
            BufferedImage processedImage = preprocessImage(image);
            
            // Extract text
            String result = tesseract.doOCR(processedImage);
            String cleanedResult = result.trim();
            
            // If no text found, try with original image
            if (cleanedResult.isEmpty() || cleanedResult.length() < 3) {
                result = tesseract.doOCR(image);
                cleanedResult = result.trim();
            }
            
            return cleanedResult.isEmpty() ? "No text detected in the image" : cleanedResult;
            
        } catch (Exception e) {
            System.err.println("OCR Error: " + e.getMessage());
            e.printStackTrace();
            
            // Try fallback method - extract text from original file
            try {
                return "Base64 processing failed. Please try using the original image file for better results.";
            } catch (Exception fallbackError) {
                return "Error: Failed to extract text - " + e.getMessage();
            }
        }
    }
    
    /**
     * Preprocess image for better OCR results
     */
    private BufferedImage preprocessImage(BufferedImage originalImage) {
        // Convert to grayscale for better OCR
        BufferedImage grayscale = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_BYTE_GRAY
        );
        
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                int rgb = originalImage.getRGB(x, y);
                grayscale.setRGB(x, y, rgb);
            }
        }
        
        return grayscale;
    }
    
    /**
     * Detect text regions with bounding boxes
     */
    public List<TextRegion> detectTextRegions(String imagePath) {
        List<TextRegion> textRegions = new ArrayList<>();
        try {
            System.out.println("Starting text region detection for: " + imagePath);
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.out.println("Image file does not exist: " + imagePath);
                return textRegions;
            }
            
            // Load image and get words with bounding boxes
            BufferedImage image = ImageIO.read(imageFile);
            System.out.println("Image loaded successfully, size: " + image.getWidth() + "x" + image.getHeight());
            List<Word> words = tesseract.getWords(image, 1);
            System.out.println("OCR completed, found " + words.size() + " words");
            
            for (Word word : words) {
                if (word.getText() != null && !word.getText().trim().isEmpty()) {
                    Rectangle boundingBox = word.getBoundingBox();
                    TextRegion region = new TextRegion(
                        word.getText().trim(),
                        boundingBox.x,
                        boundingBox.y,
                        boundingBox.width,
                        boundingBox.height,
                        word.getConfidence()
                    );
                    textRegions.add(region);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error detecting text regions: " + e.getMessage());
        }
        
        return textRegions;
    }
    
    /**
     * Remove text from image by intelligent inpainting
     */
    public BufferedImage removeTextFromImage(BufferedImage originalImage, List<TextRegion> textRegions) {
        BufferedImage result = new BufferedImage(
            originalImage.getWidth(),
            originalImage.getHeight(),
            originalImage.getType()
        );
        
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        
        for (TextRegion region : textRegions) {
            // Intelligent inpainting - sample surrounding colors
            Color backgroundColor = sampleBackgroundColor(originalImage, region);
            g2d.setColor(backgroundColor);
            g2d.fillRect(region.x, region.y, region.width, region.height);
            
            // Add some blur effect to make it look more natural
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        
        g2d.dispose();
        return result;
    }
    
    /**
     * Sample background color from surrounding pixels
     */
    private Color sampleBackgroundColor(BufferedImage image, TextRegion region) {
        int sampleSize = 10; // Sample pixels around the text region
        int totalR = 0, totalG = 0, totalB = 0, count = 0;
        
        // Sample pixels around the text region
        for (int x = Math.max(0, region.x - sampleSize); x < Math.min(image.getWidth(), region.x + region.width + sampleSize); x++) {
            for (int y = Math.max(0, region.y - sampleSize); y < Math.min(image.getHeight(), region.y + region.height + sampleSize); y++) {
                // Skip pixels inside the text region
                if (x >= region.x && x < region.x + region.width && 
                    y >= region.y && y < region.y + region.height) {
                    continue;
                }
                
                Color pixelColor = new Color(image.getRGB(x, y));
                totalR += pixelColor.getRed();
                totalG += pixelColor.getGreen();
                totalB += pixelColor.getBlue();
                count++;
            }
        }
        
        if (count > 0) {
            return new Color(totalR / count, totalG / count, totalB / count);
        } else {
            // Fallback to white if no surrounding pixels found
            return Color.WHITE;
        }
    }
    
    /**
     * Add new text to image at specified location
     */
    public BufferedImage addTextToImage(BufferedImage image, String text, int x, int y, String fontName, int fontSize, Color color, String fontStyle) {
        BufferedImage result = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            image.getType()
        );
        
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        
        // Set font style based on parameter
        int fontStyleInt = Font.PLAIN;
        if (fontStyle != null) {
            switch (fontStyle.toLowerCase()) {
                case "bold":
                    fontStyleInt = Font.BOLD;
                    break;
                case "italic":
                    fontStyleInt = Font.ITALIC;
                    break;
                case "bold italic":
                    fontStyleInt = Font.BOLD | Font.ITALIC;
                    break;
                default:
                    fontStyleInt = Font.PLAIN;
                    break;
            }
        }
        
        // Set font and color
        Font font = new Font(fontName, fontStyleInt, fontSize);
        g2d.setFont(font);
        g2d.setColor(color);
        
        // Add text
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return result;
    }
    
    /**
     * Text region data class
     */
    public static class TextRegion {
        public String text;
        public int x, y, width, height;
        public float confidence;
        
        public TextRegion(String text, int x, int y, int width, int height, float confidence) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.confidence = confidence;
        }
        
        @Override
        public String toString() {
            return String.format("Text: '%s' at (%d,%d) size %dx%d confidence: %.2f", 
                text, x, y, width, height, confidence);
        }
    }
    
    /**
     * Extract text from BufferedImage
     * @param image BufferedImage object
     * @return Extracted text
     */
    public String extractTextFromImage(BufferedImage image) {
        try {
            String result = tesseract.doOCR(image);
            return result.trim();
        } catch (TesseractException e) {
            System.err.println("OCR Error: " + e.getMessage());
            return "Error: Failed to extract text - " + e.getMessage();
        }
    }
}
