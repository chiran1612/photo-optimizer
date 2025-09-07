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

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install system dependencies for image processing
RUN apk add --no-cache \
    tesseract-ocr \
    tesseract-ocr-data-eng \
    curl \
    && rm -rf /var/cache/apk/*

# Create app user
RUN addgroup -S appuser && adduser -S -G appuser appuser

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/photo-optimizer-*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/data /app/logs /app/uploads /app/tessdata && \
    chown -R appuser:appuser /app

# Copy tessdata
COPY --from=build /app/src/main/resources/tessdata /app/tessdata

# Switch to app user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/photo-optimizer/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
