# 📸 Photo Optimizer

A simple Spring Boot application for photo upload, management, and optimization with Google Drive integration.

## Features

- ✅ **Photo Upload**: Upload multiple photos at once
- ✅ **Photo Management**: View, organize, and delete photos
- ✅ **Version Control**: Keep track of photo versions
- ✅ **Simple Interface**: Clean HTML frontend
- ✅ **Database Storage**: H2 database for photo metadata
- 🔄 **Google Drive Sync**: (Coming soon)

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Installation

1. **Clone or download the project**
   ```bash
   cd photo-optimizer
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application**
   - Main app: http://localhost:8080/photo-optimizer/
   - Database console: http://localhost:8080/photo-optimizer/h2-console

### Usage

1. **Upload Photos**
   - Go to the main page
   - Click "Choose Files" and select your photos
   - Click "Upload Photos"

2. **Manage Photos**
   - View all uploaded photos in the grid
   - Delete photos you no longer need
   - Photos are stored in the `./uploads/` directory

3. **Database**
   - Access H2 console to view photo metadata
   - URL: `jdbc:h2:file:./data/photo_optimizer_db`
   - Username: `sa`
   - Password: (leave empty)

## Project Structure

```
photo-optimizer/
├── src/main/java/com/photooptimizer/
│   ├── PhotoOptimizerApplication.java    # Main application class
│   ├── controller/
│   │   └── PhotoController.java          # Web controller
│   ├── model/
│   │   └── Photo.java                    # Photo entity
│   ├── repository/
│   │   └── PhotoRepository.java          # Data repository
│   └── service/
│       └── PhotoService.java             # Business logic
├── src/main/resources/
│   ├── application.yml                   # Configuration
│   └── templates/
│       └── index.html                    # Simple HTML frontend
├── pom.xml                               # Maven dependencies
└── README.md                             # This file
```

## Configuration

Edit `src/main/resources/application.yml` to customize:

- **Upload limits**: Change `max-file-size` and `max-request-size`
- **Storage paths**: Modify upload directories
- **Database settings**: Adjust H2 database configuration

## Development

### Adding New Features

1. **Photo Optimization**: Add image processing libraries
2. **Google Drive**: Implement Google Drive API integration
3. **Batch Operations**: Add bulk photo operations
4. **Search**: Add photo search functionality

### Database Schema

The `Photo` entity includes:
- Basic file information (name, size, format)
- File paths (original, optimized, thumbnail)
- Timestamps (upload, optimization)
- Google Drive integration fields

## Troubleshooting

### Common Issues

1. **Upload fails**: Check file size limits in `application.yml`
2. **Database errors**: Ensure H2 database files are writable
3. **Port conflicts**: Change server port in `application.yml`

### Logs

Check `./logs/photo-optimizer.log` for detailed error information.

## License

This project is open source and available under the MIT License.

---

**Ready to optimize your photos?** 🚀

Start the application and begin uploading your photos!
