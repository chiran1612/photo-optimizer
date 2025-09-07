# 🚀 CI/CD Pipeline Setup Guide for Photo Optimizer

## 🎯 **What is CI/CD?**

**CI/CD** stands for **Continuous Integration** and **Continuous Deployment/Delivery**. It's a set of practices that automate the software development lifecycle to deliver code changes more frequently and reliably.

### **Continuous Integration (CI)**
- **Automated Testing**: Run tests every time code is committed
- **Code Quality Checks**: Static analysis, linting, security scans
- **Build Automation**: Compile and package your application
- **Early Bug Detection**: Catch issues before they reach production

### **Continuous Deployment/Delivery (CD)**
- **Automated Deployment**: Deploy to staging/production environments
- **Environment Management**: Consistent deployments across environments
- **Rollback Capability**: Quick recovery from failed deployments
- **Zero-Downtime Deployments**: Update applications without service interruption

## 🏗️ **CI/CD Pipeline Architecture for Photo Optimizer**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Developer     │    │   Git Repository│    │   CI/CD Server  │    │   Production    │
│   Commits Code  │───▶│   (GitHub/GitLab│───▶│   (GitHub Actions│───▶│   Environment   │
│                 │    │    /Jenkins)    │    │    /Jenkins)    │    │   (Docker/AWS)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ **Recommended CI/CD Tools**

### **Option 1: GitHub Actions (Recommended)**
- **Free for public repositories**
- **Native GitHub integration**
- **Easy YAML configuration**
- **Built-in marketplace for actions**

### **Option 2: GitLab CI/CD**
- **Integrated with GitLab**
- **Powerful pipeline features**
- **Good for private repositories**

### **Option 3: Jenkins**
- **Self-hosted solution**
- **Highly customizable**
- **Requires server setup**

## 📋 **Step-by-Step Setup Guide**

### **Phase 1: Prepare Your Repository**

#### **1.1 Create GitHub Repository**
```bash
# If you haven't already, create a GitHub repository
git init
git add .
git commit -m "Initial commit: Photo Optimizer project"
git branch -M main
git remote add origin https://github.com/yourusername/photo-optimizer.git
git push -u origin main
```

#### **1.2 Add Required Files**

Create these files in your project root:

**`.gitignore`**
```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iws
*.iml
*.ipr
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Application specific
logs/
data/
uploads/
tessdata/
*.log

# Environment files
.env
application-local.yml
application-dev.yml
```

**`Dockerfile`**
```dockerfile
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
```

**`docker-compose.yml`**
```yaml
version: '3.8'

services:
  photo-optimizer:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./data:/app/data
      - ./logs:/app/logs
      - ./uploads:/app/uploads
    environment:
      - SPRING_PROFILES_ACTIVE=production
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/photo-optimizer/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  # Optional: Add database service
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: photo_optimizer
      POSTGRES_USER: photo_user
      POSTGRES_PASSWORD: secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    restart: unless-stopped

volumes:
  postgres_data:
```

### **Phase 2: GitHub Actions CI/CD Pipeline**

#### **2.1 Create GitHub Actions Workflow**

Create `.github/workflows/ci-cd.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # Job 1: Code Quality and Testing
  test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run tests
      run: mvn clean test
      
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
        
    - name: Code coverage
      run: mvn jacoco:report
      
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: target/site/jacoco/jacoco.xml
        
    - name: SonarCloud Scan
      uses: SonarSource/sonarcloud-github-action@master
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

  # Job 2: Build and Package
  build:
    needs: test
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Build application
      run: mvn clean package -DskipTests
      
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: jar-artifact
        path: target/*.jar

  # Job 3: Security Scanning
  security:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'
        
    - name: Upload Trivy scan results
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'

  # Job 4: Build Docker Image
  docker:
    needs: [test, build, security]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3
      
    - name: Log in to Container Registry
      uses: docker/login-action@v3
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
        
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v5
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix={{branch}}-
          type=raw,value=latest,enable={{is_default_branch}}
          
    - name: Build and push Docker image
      uses: docker/build-push-action@v5
      with:
        context: .
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max

  # Job 5: Deploy to Staging
  deploy-staging:
    needs: docker
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: staging
    
    steps:
    - name: Deploy to staging
      run: |
        echo "Deploying to staging environment..."
        # Add your staging deployment commands here
        # Example: kubectl apply -f k8s/staging/
        # Example: aws ecs update-service --cluster staging --service photo-optimizer

  # Job 6: Deploy to Production
  deploy-production:
    needs: docker
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production
    
    steps:
    - name: Deploy to production
      run: |
        echo "Deploying to production environment..."
        # Add your production deployment commands here
        # Example: kubectl apply -f k8s/production/
        # Example: aws ecs update-service --cluster production --service photo-optimizer
        
    - name: Run smoke tests
      run: |
        echo "Running smoke tests..."
        # Add smoke tests here
        # Example: curl -f http://your-production-url/photo-optimizer/actuator/health
```

#### **2.2 Add Testing Dependencies**

Update your `pom.xml` to include testing dependencies:

```xml
<dependencies>
    <!-- Existing dependencies -->
    
    <!-- Testing Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Code Coverage -->
    <dependency>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.8</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Existing plugins -->
        
        <!-- JaCoCo Code Coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.8</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- Surefire Plugin for Tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### **2.3 Create Test Classes**

**`src/test/java/com/photooptimizer/PhotoOptimizerApplicationTests.java`**
```java
package com.photooptimizer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PhotoOptimizerApplicationTests {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}
```

**`src/test/java/com/photooptimizer/controller/PhotoControllerTest.java`**
```java
package com.photooptimizer.controller;

import com.photooptimizer.service.PhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotoController.class)
class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoService photoService;

    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testPhotoUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/upload")
                .file(file))
                .andExpect(status().isOk());
    }
}
```

### **Phase 3: Environment Configuration**

#### **3.1 Create Environment-Specific Configurations**

**`src/main/resources/application-test.yml`**
```yaml
server:
  port: 0  # Random port for tests

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: false
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

logging:
  level:
    com.photooptimizer: DEBUG
    org.springframework.web: WARN
```

**`src/main/resources/application-staging.yml`**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://staging-db:5432/photo_optimizer
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:photo_user}
    password: ${DB_PASSWORD:staging_password}
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

photo:
  optimization:
    upload-path: /app/uploads/staging
    max-file-size: 25MB

logging:
  level:
    com.photooptimizer: INFO
    org.springframework.web: WARN
  file:
    name: /app/logs/photo-optimizer-staging.log
```

**`src/main/resources/application-production.yml`**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://production-db:5432/photo_optimizer
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

photo:
  optimization:
    upload-path: /app/uploads/production
    max-file-size: 50MB

logging:
  level:
    com.photooptimizer: WARN
    org.springframework.web: ERROR
  file:
    name: /app/logs/photo-optimizer-production.log

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### **Phase 4: Kubernetes Deployment (Optional)**

#### **4.1 Create Kubernetes Manifests**

**`k8s/namespace.yaml`**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: photo-optimizer
```

**`k8s/configmap.yaml`**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: photo-optimizer-config
  namespace: photo-optimizer
data:
  SPRING_PROFILES_ACTIVE: "production"
  SERVER_PORT: "8080"
```

**`k8s/deployment.yaml`**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: photo-optimizer
  namespace: photo-optimizer
spec:
  replicas: 3
  selector:
    matchLabels:
      app: photo-optimizer
  template:
    metadata:
      labels:
        app: photo-optimizer
    spec:
      containers:
      - name: photo-optimizer
        image: ghcr.io/yourusername/photo-optimizer:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: photo-optimizer-config
              key: SPRING_PROFILES_ACTIVE
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: photo-optimizer-secrets
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: photo-optimizer-secrets
              key: db-password
        volumeMounts:
        - name: uploads
          mountPath: /app/uploads
        - name: logs
          mountPath: /app/logs
        livenessProbe:
          httpGet:
            path: /photo-optimizer/actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /photo-optimizer/actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: uploads
        persistentVolumeClaim:
          claimName: photo-optimizer-uploads
      - name: logs
        persistentVolumeClaim:
          claimName: photo-optimizer-logs
```

**`k8s/service.yaml`**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: photo-optimizer-service
  namespace: photo-optimizer
spec:
  selector:
    app: photo-optimizer
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

## 🔧 **Setting Up GitHub Secrets**

### **Required Secrets**
1. **SONAR_TOKEN**: For code quality analysis
2. **DB_USERNAME**: Database username for production
3. **DB_PASSWORD**: Database password for production
4. **AWS_ACCESS_KEY_ID**: For AWS deployment (if using)
5. **AWS_SECRET_ACCESS_KEY**: For AWS deployment (if using)

### **How to Add Secrets**
1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret with its value

## 🚀 **Deployment Strategies**

### **1. Blue-Green Deployment**
```yaml
# Deploy new version alongside old version
# Switch traffic when new version is healthy
# Rollback by switching traffic back
```

### **2. Rolling Deployment**
```yaml
# Gradually replace old instances with new ones
# Zero downtime deployment
# Automatic rollback on failure
```

### **3. Canary Deployment**
```yaml
# Deploy to small percentage of users first
# Monitor metrics and gradually increase
# Full rollback if issues detected
```

## 📊 **Monitoring and Observability**

### **Health Checks**
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check database connectivity
        // Check file system access
        // Check external dependencies
        return Health.up()
            .withDetail("database", "Connected")
            .withDetail("storage", "Available")
            .build();
    }
}
```

### **Metrics Collection**
```yaml
# Add to application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## 🔍 **Troubleshooting Common Issues**

### **Build Failures**
```bash
# Check Maven dependencies
mvn dependency:tree

# Run tests locally
mvn clean test

# Check for code style issues
mvn checkstyle:check
```

### **Docker Build Issues**
```bash
# Build Docker image locally
docker build -t photo-optimizer .

# Test Docker image
docker run -p 8080:8080 photo-optimizer

# Check Docker logs
docker logs <container-id>
```

### **Deployment Issues**
```bash
# Check Kubernetes pods
kubectl get pods -n photo-optimizer

# Check pod logs
kubectl logs -f deployment/photo-optimizer -n photo-optimizer

# Check service status
kubectl get svc -n photo-optimizer
```

## 📈 **Best Practices**

### **1. Branch Strategy**
- **main**: Production-ready code
- **develop**: Integration branch
- **feature/***: Feature development
- **hotfix/***: Critical fixes

### **2. Commit Messages**
```
feat: add OCR text extraction feature
fix: resolve image upload validation issue
docs: update API documentation
test: add unit tests for PhotoService
```

### **3. Code Quality**
- **Unit Tests**: Minimum 80% coverage
- **Integration Tests**: Test critical workflows
- **Code Reviews**: All changes reviewed
- **Static Analysis**: SonarQube integration

### **4. Security**
- **Dependency Scanning**: Regular vulnerability checks
- **Secrets Management**: Use GitHub Secrets
- **Image Scanning**: Scan Docker images
- **Access Control**: Principle of least privilege

## 🎯 **Next Steps**

1. **Set up GitHub repository** with the provided files
2. **Configure GitHub Actions** with the workflow file
3. **Add required secrets** to your repository
4. **Test the pipeline** with a small change
5. **Set up monitoring** and alerting
6. **Document deployment procedures** for your team

## 📚 **Additional Resources**

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Kubernetes Deployment Guide](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [Spring Boot Production Ready](https://spring.io/guides/gs/spring-boot-for-azure/)

---

**Last Updated**: December 2024  
**Version**: 1.0.0  
**Author**: Photo Optimizer Development Team

---

*This guide provides a complete CI/CD setup for your Photo Optimizer project. Start with the basic setup and gradually add more advanced features as your needs grow.*
