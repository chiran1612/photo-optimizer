# Multi-stage build for Photo Optimizer
FROM maven:3.8.6-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Install system dependencies for image processing
RUN apt-get update && apt-get install -y \
    libtesseract-dev \
    tesseract-ocr \
    tesseract-ocr-eng \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

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
