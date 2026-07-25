# =============================================================================
# Multi-Stage Dockerfile for Aura Backend (Spring Boot + Java 21)
# Production-ready for Render deployment with active Swagger UI
# =============================================================================

# --- Stage 1: Build JAR with Maven ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and download dependencies to optimize Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production artifact
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Minimal Runtime Container ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security best practices
RUN addgroup -S auragroup && adduser -S aurauser -G auragroup

# Copy built JAR artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R aurauser:auragroup /app

USER aurauser

# Render dynamically passes PORT env var (defaults to 8080)
ENV PORT=8080
EXPOSE ${PORT}

# JVM tuning options for cloud deployment (Virtual Threads enabled via application.yml)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
