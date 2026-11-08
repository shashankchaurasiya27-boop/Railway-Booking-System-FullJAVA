# Railway Booking System - Deployment Guide

## Environment Configuration

The application supports multiple deployment environments using Spring Profiles.

---

## Available Profiles

| Profile   | Purpose     | Database         | Logging | Use Case         |
|-----------|-------------|------------------|---------|------------------|
| **dev**   | Development | Local MySQL      | Verbose | Local development|
| **prod**  | Production  | Production MySQL | Minimal | Production deployment |
| **test**  | Testing     | H2 In-Memory     | Minimal | Automated testing |

---

## Quick Start

### Development Environment

```bash
# 1. Set environment variables (optional, has defaults)
export DB_USERNAME=root
export DB_PASSWORD=your_password

# 2. Run with dev profile (default)
mvn spring-boot:run

# OR explicitly specify dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Environment

```bash
# 1. Set REQUIRED environment variables
export DB_URL=jdbc:mysql://prod-db-host:3306/railway_booking?useSSL=true
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password

# 2. Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# OR build and run JAR
mvn clean package
java -jar -Dspring.profiles.active=prod target/railway-booking-system-1.0.0.jar
```

### Test Environment

```bash
# Run tests with test profile
mvn test -Dspring.profiles.active=test
```

---

## Configuration Files

### Base Configuration
**File:** `application.properties`
- Common settings for all environments
- Default profile selection
- Logging patterns
- Jackson JSON configuration
- Java 21 virtual threads enabled

### Development Configuration
**File:** `application-dev.properties`
- Local MySQL database
- Verbose logging (DEBUG level)
- Smaller connection pool (5 connections)
- Detailed error messages
- Actuator endpoints exposed

### Production Configuration
**File:** `application-prod.properties`
- Production database (via environment variables)
- Minimal logging (WARN level)
- Larger connection pool (20 connections)
- Security hardened
- Limited actuator endpoints

### Test Configuration
**File:** `application-test.properties`
- H2 in-memory database
- Minimal logging
- Fast test execution
- No external dependencies

---

## Environment Variables

### Required for Production

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | Database connection URL | `jdbc:mysql://prod-db:3306/railway_booking?useSSL=true` |
| `DB_USERNAME` | Database username | `railway_user` |
| `DB_PASSWORD` | Database password | `secure_password_here` |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` |
| `SERVER_PORT` | Server port | `8080` |
| `SSL_KEYSTORE_PATH` | SSL keystore path | - |
| `SSL_KEYSTORE_PASSWORD` | SSL keystore password | - |

### Development Defaults

For development, credentials have defaults:
```properties
DB_USERNAME=${DB_USERNAME:root}
DB_PASSWORD=${DB_PASSWORD:}
```

---

## Deployment Methods

### Method 1: Direct Maven

```bash
# Development
mvn spring-boot:run

# Production
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:mysql://prod-db:3306/railway_booking \
DB_USERNAME=user \
DB_PASSWORD=pass \
mvn spring-boot:run
```

### Method 2: JAR File

```bash
# Build
mvn clean package

# Run with environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://prod-db:3306/railway_booking
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_pass

java -jar target/railway-booking-system-1.0.0.jar
```

### Method 3: Using .env File

```bash
# 1. Create .env from template
cp .env.template .env

# 2. Edit .env with your credentials
nano .env

# 3. Source environment variables
source .env

# 4. Run application
mvn spring-boot:run
```

### Method 4: Docker & Docker Compose (Recommended)

The repository includes a production multi-stage `Dockerfile` and `docker-compose.yml` for turnkey container deployment:

```bash
# 1. Start application and MySQL 8.0 together
docker compose up -d

# 2. View logs
docker compose logs -f app

# 3. Stop containers
docker compose down
```

### Method 5: One-Click Cloud Deployment (Railway)

1. Go to [railway.app](https://railway.app) and sign in with GitHub.
2. Click **New Project** ➔ **Deploy from GitHub repo**.
3. Select `shashankchaurasiya27-boop/Railway-Booking-System-FullJAVA`.
4. Click **+ Add Service** ➔ **Database** ➔ **MySQL**.
5. Connect your service to MySQL using Railway's environment variables:
   - `DB_URL`: `jdbc:mysql://${{MYSQL_HOST}}:${{MYSQL_PORT}}/${{MYSQL_DATABASE}}?useSSL=false&allowPublicKeyRetrieval=true`
   - `DB_USERNAME`: `${{MYSQL_USER}}`
   - `DB_PASSWORD`: `${{MYSQL_PASSWORD}}`
   - `HEADLESS_MODE`: `true`
6. Click **Deploy**. Your REST API and database will be live with a public URL!

### Method 6: One-Click Cloud Deployment (Render)

1. Go to [render.com](https://render.com) and create a free account.
2. Click **New** ➔ **Blueprint**.
3. Connect your GitHub repository: `Railway-Booking-System-FullJAVA`.
4. Render will automatically read `render.yaml`, spin up a free PostgreSQL database, build the Docker container, and deploy the application.

---

## Java 21 Compatibility

### Virtual Threads Enabled

The application uses Java 21 Virtual Threads for improved scalability:

```properties
# Enabled in application.properties
spring.threads.virtual.enabled=true
```

**Benefits:**
- Better handling of blocking I/O operations
- Improved throughput for database operations
- Reduced memory footprint compared to platform threads

### Requirements

- **Java Version:** 21 or higher
- **Maven:** 3.8+ recommended
- **Database:** MySQL 8.0+

### Verify Java Version

```bash
java -version
# Should show: openjdk version "21" or higher
```

---

## Security Best Practices

### ✅ DO

1. **Use Environment Variables**
   ```bash
   export DB_PASSWORD=secure_password
   ```

2. **Use Secret Managers** (Production)
   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault
   - Kubernetes Secrets

3. **Enable SSL/TLS** (Production)
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=/path/to/keystore.p12
   ```

4. **Rotate Credentials Regularly**

5. **Use Strong Passwords**

### ❌ DON'T

1. **Don't Hardcode Credentials**
   ```properties
   # BAD - Don't do this!
   spring.datasource.password=mypassword123
   ```

2. **Don't Commit .env Files**
   - Add `.env` to `.gitignore`
   - Use `.env.template` for examples

3. **Don't Use Default Passwords in Production**

4. **Don't Expose Actuator Endpoints Publicly**

---

## Monitoring & Health Checks

### Development

All actuator endpoints exposed:
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/env
```

### Production

Limited endpoints on separate port:
```
http://localhost:8081/actuator/health
http://localhost:8081/actuator/info
http://localhost:8081/actuator/metrics
```

### Health Check Response

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

---

## Logging

### Log Levels by Environment

| Environment | Root | Application | Spring | Database |
|-------------|------|-------------|--------|----------|
| Development | INFO | DEBUG | DEBUG | DEBUG |
| Production | WARN | INFO | WARN | INFO |
| Test | WARN | INFO | WARN | WARN |

### Log Files

- **Development:** `logs/railway-booking-dev.log`
- **Production:** `/var/log/railway-booking/application.log`
- **Test:** Console only

### Log Rotation

Production logs rotate automatically:
- Max size: 50MB
- Max history: 30 days

---

## Troubleshooting

### Issue: Application won't start

**Check:**
1. Java version: `java -version` (must be 21+)
2. Database connectivity: Can you connect to MySQL?
3. Environment variables: Are they set correctly?
4. Port availability: Is port 8080 free?

### Issue: Database connection failed

**Solutions:**
```bash
# Check MySQL is running
mysql -u root -p

# Test connection
mysql -h localhost -u root -p railway_booking

# Verify environment variables
echo $DB_USERNAME
echo $DB_URL
```

### Issue: Wrong profile active

**Check active profile:**
```bash
# Look for this in startup logs:
# "The following 1 profile is active: "dev""

# Force profile:
java -jar app.jar --spring.profiles.active=prod
```

---

## Production Deployment Checklist

- [ ] Set all required environment variables
- [ ] Configure SSL/TLS certificates
- [ ] Set up log rotation
- [ ] Configure firewall rules (allow port 8080)
- [ ] Enable monitoring and alerting
- [ ] Set up database backups
- [ ] Review security headers
- [ ] Test failover scenarios
- [ ] Configure load balancer (if applicable)
- [ ] Set up CI/CD pipeline
- [ ] Document runbook procedures
- [ ] Perform security audit
- [ ] Load testing completed
- [ ] Disaster recovery plan in place

---

## Performance Tuning

### Connection Pool Sizing

**Development:**
- Max: 5 connections
- Min: 1 connection

**Production:**
- Max: 20 connections
- Min: 5 connections

**Formula:**
```
connections = ((core_count * 2) + effective_spindle_count)
```

### JVM Options (Production)

```bash
java -Xms512m -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar app.jar
```

---

## Support

For issues or questions:
1. Check logs: `logs/railway-booking-*.log`
2. Review configuration files
3. Verify environment variables
4. Check database connectivity
5. Review Spring Boot documentation

---

## Summary

✅ **Multi-environment support** via Spring Profiles  
✅ **Externalized credentials** via environment variables  
✅ **Java 21 compatible** with virtual threads  
✅ **Security hardened** for production  
✅ **No secrets in code** - all externalized  
✅ **Production ready** with monitoring and logging
