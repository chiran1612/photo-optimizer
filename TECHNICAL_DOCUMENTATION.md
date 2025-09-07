# 📸 Photo Optimizer - Technical Documentation

## 🏗️ **System Architecture**

### **Technology Stack**
- **Backend Framework**: Spring Boot 3.2.0 (Java 17)
- **Frontend**: HTML5, CSS3, JavaScript ES6+, Fabric.js 5.3.0
- **Database**: H2 Database (embedded, file-based)
- **Build Tool**: Maven 3.6+
- **Template Engine**: Thymeleaf
- **Image Processing**: Thumbnailator, ImgScalr, Tesseract4J
- **OCR Engine**: Tesseract 4.0

### **Project Structure**
```
photo-optimizer/
├── src/main/java/com/photooptimizer/
│   ├── PhotoOptimizerApplication.java    # Main Spring Boot application
│   ├── controller/
│   │   ├── PhotoController.java          # Photo management endpoints
│   │   └── EditorController.java         # Advanced editing endpoints
│   ├── service/
│   │   ├── PhotoService.java             # Photo business logic
│   │   ├── EditorService.java            # Image editing operations
│   │   └── OCRService.java               # OCR text processing
│   ├── model/
│   │   └── Photo.java                    # JPA entity
│   └── repository/
│       └── PhotoRepository.java          # Data access layer
├── src/main/resources/
│   ├── application.yml                   # Configuration
│   ├── templates/
│   │   ├── index.html                    # Main interface
│   │   └── editor.html                   # Advanced editor
│   └── tessdata/
│       └── eng.traineddata               # OCR language data
├── uploads/                              # File storage
│   ├── edited/                           # Edited versions
│   └── versions/                         # Version control
├── data/                                 # H2 database files
├── logs/                                 # Application logs
└── pom.xml                               # Maven dependencies
```

## 🔧 **Core Components**

### **1. PhotoOptimizerApplication.java**
```java
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PhotoOptimizerApplication {
    // Main application entry point
    // Enables async processing and scheduled tasks
}
```

**Key Features:**
- Spring Boot auto-configuration
- Async processing for background tasks
- Scheduled task support
- Embedded server configuration

### **2. Photo Entity (JPA Model)**
```java
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
    
    @Column(columnDefinition = "TEXT")
    private String canvasState;
}
```

**Database Schema:**
- **Primary Key**: Auto-generated ID
- **File Management**: Original name, generated filename, file paths
- **Metadata**: Size, format, dimensions, timestamps
- **Version Control**: Optimized and thumbnail paths
- **Soft Delete**: `isActive` flag for data retention
- **Future Integration**: Google Drive ID field
- **Editor State**: JSON canvas state for session persistence

### **3. PhotoController.java**
```java
@Controller
public class PhotoController {
    
    @GetMapping("/")
    public String index(Model model) {
        // Main photo gallery interface
    }
    
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        // Multi-file upload handling
    }
    
    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        // Image serving with proper content types
    }
    
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePhoto(@PathVariable Long id) {
        // Soft delete implementation
    }
}
```

**API Endpoints:**
- `GET /` - Main gallery interface
- `POST /upload` - File upload with validation
- `GET /photo/{id}` - Image serving with MIME types
- `POST /delete/{id}` - Soft delete with file cleanup
- `GET /api/photos` - JSON API for photo list
- `GET /photo/{id}/metadata` - Photo metadata API

### **4. EditorController.java**
```java
@Controller
public class EditorController {
    
    @GetMapping("/editor/{id}")
    public String editor(@PathVariable Long id, Model model) {
        // Advanced photo editor interface
    }
    
    @PostMapping("/editor/save/{id}")
    @ResponseBody
    public ResponseEntity<String> saveEditedPhoto(
            @PathVariable Long id,
            @RequestParam("imageData") String imageData) {
        // Save edited image from canvas
    }
    
    @PostMapping("/editor/ocr/{id}")
    @ResponseBody
    public ResponseEntity<String> extractText(@PathVariable Long id) {
        // OCR text extraction
    }
    
    @PostMapping("/editor/edit-text/{id}")
    @ResponseBody
    public ResponseEntity<String> editTextInImage(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        // Intelligent text replacement
    }
}
```

**Advanced Features:**
- **Canvas-based editing** with Fabric.js integration
- **OCR text extraction** using Tesseract
- **Intelligent text editing** with background inpainting
- **Version management** with named versions
- **State persistence** for session recovery
- **Filter application** with real-time preview

### **5. PhotoService.java**
```java
@Service
public class PhotoService {
    
    public Photo uploadPhoto(MultipartFile file) throws IOException {
        // File upload with UUID generation
        // Directory creation and file storage
        // Database record creation
    }
    
    public void deletePhoto(Long id) {
        // Soft delete with file cleanup
        // Database flagging instead of hard delete
    }
    
    public List<Photo> getAllPhotos() {
        // Active photos retrieval with sorting
    }
}
```

**Business Logic:**
- **File Management**: UUID-based naming, directory structure
- **Validation**: File type, size, and format validation
- **Storage**: Organized file system with automatic cleanup
- **Database Operations**: CRUD operations with soft delete

### **6. EditorService.java**
```java
@Service
public class EditorService {
    
    public String saveEditedPhoto(Long photoId, byte[] imageBytes) throws IOException {
        // Save edited version with timestamp
    }
    
    public String createNewVersion(Long photoId, byte[] imageBytes, String versionName) throws IOException {
        // Create named version with new database record
    }
    
    public String applyFilter(Long photoId, String filterType, String filterValue) throws IOException {
        // Apply image filters: brightness, contrast, grayscale, sepia, blur
    }
}
```

**Image Processing:**
- **Filter Implementation**: Custom algorithms for image effects
- **Version Control**: Named versions with metadata
- **File Organization**: Automatic directory management
- **Format Support**: Multiple image format handling

### **7. OCRService.java**
```java
@Service
public class OCRService {
    
    public String extractTextFromFile(String imagePath) {
        // Tesseract OCR with file input
    }
    
    public String extractTextFromBase64(String base64ImageData) {
        // OCR with Base64 image data
    }
    
    public List<TextRegion> detectTextRegions(String imagePath) {
        // Text region detection with bounding boxes
    }
    
    public BufferedImage addTextToImage(BufferedImage image, String text, int x, int y, String fontName, int fontSize, Color color, String fontStyle) {
        // Add text with custom formatting
    }
}
```

**OCR Capabilities:**
- **Text Extraction**: Multiple input methods (file, Base64)
- **Region Detection**: Bounding box coordinates with confidence scores
- **Text Editing**: Intelligent replacement with background inpainting
- **Font Customization**: Multiple fonts, sizes, colors, styles
- **Preprocessing**: Image enhancement for better OCR accuracy

## 🎨 **Frontend Architecture**

### **1. Main Interface (index.html)**
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Photo Optimizer</title>
    <style>
        /* Modern CSS with grid layout */
        .photo-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 20px;
        }
    </style>
</head>
<body>
    <!-- Upload interface with drag-and-drop -->
    <!-- Photo gallery with grid layout -->
    <!-- Action buttons for each photo -->
</body>
</html>
```

**Features:**
- **Responsive Design**: CSS Grid with auto-fill columns
- **Drag-and-Drop**: File upload with visual feedback
- **Real-time Updates**: AJAX-based photo management
- **Modern UI**: Clean, professional interface

### **2. Advanced Editor (editor.html)**
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/fabric.js/5.3.0/fabric.min.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="editor-container">
        <div class="sidebar">
            <!-- Tool panels with organized sections -->
        </div>
        <div class="main-area">
            <div class="toolbar">
                <!-- Action buttons with keyboard shortcuts -->
            </div>
            <div class="canvas-container">
                <canvas id="editor-canvas"></canvas>
            </div>
        </div>
    </div>
</body>
</html>
```

**Advanced Features:**
- **Fabric.js Integration**: Professional canvas manipulation
- **Tool Organization**: Grouped tools with collapsible panels
- **Keyboard Shortcuts**: Ctrl+Z (undo), Ctrl+S (save)
- **Real-time Preview**: Instant visual feedback
- **State Management**: Auto-save with debouncing

### **3. JavaScript Architecture**
```javascript
// Global variables and initialization
let canvas;
let currentTool = 'select';
let photoId = [[${photo.id}]];
let history = [];
let historyIndex = -1;

// Fabric.js canvas initialization
function initializeCanvas() {
    canvas = new fabric.Canvas('editor-canvas', {
        backgroundColor: '#ffffff',
        selection: true,
        preserveObjectStacking: true
    });
    
    // Event listeners for state management
    canvas.on('object:added', saveState);
    canvas.on('object:modified', saveState);
}

// Tool selection and management
function selectTool(tool) {
    currentTool = tool;
    // Update UI and canvas behavior
}

// Undo/Redo system
function saveState() {
    historyIndex++;
    if (historyIndex < history.length) {
        history.length = historyIndex;
    }
    history.push(JSON.stringify(canvas.toJSON()));
    autoSaveToServer();
}
```

**Key JavaScript Features:**
- **State Management**: Complete undo/redo system
- **Auto-save**: Debounced server synchronization
- **Tool System**: Modular tool architecture
- **Event Handling**: Comprehensive canvas event management
- **Error Handling**: Graceful error recovery

## 🗄️ **Database Configuration**

### **H2 Database Setup**
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/photo_optimizer_db
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: false
```

**Database Features:**
- **File-based Storage**: Persistent data across restarts
- **Web Console**: Development and debugging interface
- **Auto-schema Updates**: Automatic table creation and updates
- **Connection Pooling**: Optimized database connections

### **Repository Layer**
```java
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    List<Photo> findByIsActiveTrueOrderByUploadedAtDesc();
    
    Optional<Photo> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT p FROM Photo p WHERE p.format = :format AND p.isActive = true")
    List<Photo> findByFormatAndActive(@Param("format") String format);
}
```

**Data Access Features:**
- **Custom Queries**: Optimized database operations
- **Soft Delete Support**: Active record filtering
- **Sorting and Pagination**: Efficient data retrieval
- **Type Safety**: Strongly typed repository methods

## ⚙️ **Configuration Management**

### **Application Properties**
```yaml
# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /photo-optimizer

# File Upload Configuration
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
      enabled: true

# Photo Optimization Settings
photo:
  optimization:
    jpeg-quality: 85
    png-compression: 6
    webp-quality: 80
    max-width: 1920
    max-height: 1080
    thumbnail-size: 300
    supported-formats: jpg,jpeg,png,gif,bmp,webp
    upload-path: ./uploads/original
    optimized-path: ./uploads/optimized
    thumbnail-path: ./uploads/thumbnails

# Logging Configuration
logging:
  level:
    com.photooptimizer: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
  file:
    name: ./logs/photo-optimizer.log
```

**Configuration Features:**
- **Environment-specific Settings**: Development and production configurations
- **File Upload Limits**: Configurable size and format restrictions
- **Image Processing Parameters**: Quality and dimension settings
- **Comprehensive Logging**: Debug and production log levels

## 🔒 **Security Considerations**

### **File Upload Security**
```java
public Photo uploadPhoto(MultipartFile file) throws IOException {
    // Validate file type
    String originalName = file.getOriginalFilename();
    String extension = originalName.substring(originalName.lastIndexOf("."));
    
    // Check against allowed formats
    if (!ALLOWED_FORMATS.contains(extension.toLowerCase())) {
        throw new IllegalArgumentException("Unsupported file format");
    }
    
    // Generate secure filename
    String fileName = UUID.randomUUID().toString() + extension;
    
    // Validate file size
    if (file.getSize() > MAX_FILE_SIZE) {
        throw new IllegalArgumentException("File too large");
    }
}
```

**Security Measures:**
- **File Type Validation**: Whitelist of allowed formats
- **Size Limits**: Configurable file size restrictions
- **Secure Naming**: UUID-based filenames prevent conflicts
- **Path Traversal Protection**: Safe file path handling
- **Input Sanitization**: XSS and injection prevention

## 🚀 **Performance Optimizations**

### **Image Processing**
```java
// Efficient image loading with buffering
BufferedImage originalImage = ImageIO.read(new File(photo.getFilePath()));

// Optimized filter application
private BufferedImage applyBrightness(BufferedImage image, float brightness) {
    BufferedImage result = new BufferedImage(
        image.getWidth(), 
        image.getHeight(), 
        BufferedImage.TYPE_INT_RGB
    );
    
    // Pixel-by-pixel processing with optimization
    for (int x = 0; x < image.getWidth(); x++) {
        for (int y = 0; y < image.getHeight(); y++) {
            // Efficient color manipulation
        }
    }
    
    return result;
}
```

**Performance Features:**
- **Memory Management**: Efficient BufferedImage handling
- **Lazy Loading**: On-demand image processing
- **Caching**: Thumbnail and processed image caching
- **Async Processing**: Background task execution
- **Connection Pooling**: Database connection optimization

### **Frontend Optimization**
```javascript
// Debounced auto-save to reduce server load
let autoSaveTimeout;
function autoSaveToServer() {
    clearTimeout(autoSaveTimeout);
    autoSaveTimeout = setTimeout(async () => {
        // Save canvas state to server
    }, 2000); // 2-second debounce
}

// Efficient canvas rendering
canvas.renderAll();
canvas.setZoom(zoomLevel);
```

**Frontend Optimizations:**
- **Debounced Operations**: Reduced server requests
- **Canvas Optimization**: Efficient rendering and updates
- **Memory Management**: Proper cleanup and garbage collection
- **Event Throttling**: Optimized user interaction handling

## 📊 **Monitoring and Logging**

### **Application Logging**
```yaml
logging:
  level:
    com.photooptimizer: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ./logs/photo-optimizer.log
```

**Logging Features:**
- **Structured Logging**: Consistent log format
- **Level-based Filtering**: Debug, info, warn, error levels
- **File and Console Output**: Dual logging destinations
- **Thread Information**: Multi-threaded operation tracking
- **Performance Metrics**: Request timing and resource usage

### **Health Monitoring**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**Monitoring Endpoints:**
- **Health Checks**: Application status monitoring
- **Metrics**: Performance and usage statistics
- **Info Endpoint**: Application metadata
- **Custom Metrics**: Business-specific monitoring

## 🔄 **Deployment and Operations**

### **Build Configuration**
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

**Deployment Features:**
- **Fat JAR**: Self-contained executable
- **Embedded Server**: No external server dependencies
- **Configuration Externalization**: Environment-specific settings
- **Health Checks**: Production monitoring
- **Graceful Shutdown**: Clean application termination

### **Docker Support** (Future Enhancement)
```dockerfile
FROM openjdk:17-jre-slim
COPY target/photo-optimizer-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📈 **Scalability Considerations**

### **Horizontal Scaling**
- **Stateless Design**: Session-independent operations
- **File Storage**: External storage for multi-instance deployment
- **Database**: External database for shared state
- **Load Balancing**: Multiple instance support

### **Vertical Scaling**
- **Memory Optimization**: Efficient image processing
- **CPU Utilization**: Multi-threaded operations
- **I/O Optimization**: Async file operations
- **Caching**: Redis integration for session state

## 🛠️ **Development Tools**

### **Maven Dependencies**
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Image Processing -->
    <dependency>
        <groupId>net.coobird</groupId>
        <artifactId>thumbnailator</artifactId>
        <version>0.4.19</version>
    </dependency>
    
    <!-- OCR -->
    <dependency>
        <groupId>net.sourceforge.tess4j</groupId>
        <artifactId>tess4j</artifactId>
        <version>5.8.0</version>
    </dependency>
    
    <!-- Development Tools -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### **Development Features**
- **Hot Reload**: Automatic application restart
- **Debug Support**: Comprehensive debugging capabilities
- **Testing Framework**: Unit and integration test support
- **Code Quality**: Static analysis and formatting tools

---

## 📚 **API Reference**

### **Photo Management Endpoints**
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/` | Main gallery interface | - |
| POST | `/upload` | Upload photo | `file` (MultipartFile) |
| GET | `/photo/{id}` | Get photo image | `id` (Long) |
| POST | `/delete/{id}` | Delete photo | `id` (Long) |
| GET | `/api/photos` | Get photos JSON | - |
| GET | `/photo/{id}/metadata` | Get photo metadata | `id` (Long) |

### **Editor Endpoints**
| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/editor/{id}` | Editor interface | `id` (Long) |
| POST | `/editor/save/{id}` | Save edited photo | `id`, `imageData` |
| POST | `/editor/version/{id}` | Create version | `id`, `imageData`, `versionName` |
| POST | `/editor/ocr/{id}` | Extract text | `id` (Long) |
| POST | `/editor/edit-text/{id}` | Edit text in image | `id`, `originalText`, `newText`, `fontName`, `fontSize`, `color`, `fontStyle` |
| POST | `/editor/add-text/{id}` | Add new text | `id`, `text`, `fontName`, `fontSize`, `color`, `fontStyle`, `x`, `y` |

---

**Last Updated**: December 2024  
**Version**: 1.0.0  
**Author**: Photo Optimizer Development Team

---

*This technical documentation provides comprehensive information for developers, system administrators, and technical stakeholders working with the Photo Optimizer system.*
