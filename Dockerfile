# ===================================================================
# Stage 1: Build Application with Maven & OpenJDK 21
# ===================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ===================================================================
# Stage 2: Production JRE 21 Container
# ===================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create unprivileged application user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy built artifact from builder stage
COPY --from=builder /build/target/railway-booking-system-1.0.0.jar /app/app.jar

# Set container user & permissions
RUN chown -R appuser:appgroup /app
USER appuser

# Default HTTP server port
EXPOSE 8080

# Environment defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    HEADLESS_MODE=true

# Container healthcheck
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Execute Spring Boot fat JAR in headless mode
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "/app/app.jar"]
