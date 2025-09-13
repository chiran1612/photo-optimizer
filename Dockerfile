# Multi-stage build for Photo Optimizer
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Copy tessdata to a known location for the runtime stage
RUN mkdir -p /app/tessdata && \
    cp -r src/main/resources/tessdata/* /app/tessdata/ 2>/dev/null || true

# Runtime stage - Use Ubuntu instead of Alpine for better Tesseract support
FROM eclipse-temurin:17-jre

# Install system dependencies for image processing and Tesseract
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    libtesseract-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/photo-optimizer-*.jar app.jar

# Create necessary directories with proper permissions
RUN mkdir -p /app/data /app/logs /app/uploads /app/tessdata && \
    chown -R appuser:appuser /app

# Copy tessdata with proper permissions
COPY --from=build /app/tessdata /app/tessdata
RUN chown -R appuser:appuser /app/tessdata

# Set environment variables for Tesseract
ENV TESSDATA_PREFIX=/app/tessdata
ENV TESSDATA_DIR=/app/tessdata

# Switch to app user
USER appuser

# Expose port
EXPOSE 8080

# Health check with longer timeout for OCR operations
HEALTHCHECK --interval=30s --timeout=15s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application with JVM optimizations for Railway
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8", "-Duser.timezone=UTC", "-Dspring.profiles.active=production", "-jar", "app.jar"]
