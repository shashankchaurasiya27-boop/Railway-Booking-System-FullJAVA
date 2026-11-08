# Railway Booking System - Installation Guide

## Prerequisites

This guide assumes you are using macOS with Homebrew package manager installed.

## Automated Installation

The easiest way to get started is using the automated setup script:

```bash
cd railway-booking-system
./setup.sh
```

The setup script will automatically:
- Install Java 11 (if not installed)
- Install Maven (if not installed)
- Install MySQL (if not installed)
- Create the database and schema
- Build the project

## Manual Installation

If you prefer to install dependencies manually, follow these steps:

### 1. Install Java 11

```bash
brew install openjdk@11
```

Add Java to your PATH:
```bash
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
```

For permanent use, add this line to your `~/.zshrc` file:
```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
```

Verify installation:
```bash
java -version
```

### 2. Install Maven

```bash
brew install maven
```

Verify installation:
```bash
mvn -version
```

### 3. Install MySQL

```bash
brew install mysql
```

Start MySQL service:
```bash
brew services start mysql
```

Verify MySQL is running:
```bash
brew services list | grep mysql
```

### 4. Set Up Database

Create the database:
```bash
mysql -u root -e "CREATE DATABASE railway_booking;"
```

Import the schema:
```bash
cd railway-booking-system
mysql -u root railway_booking < src/main/resources/database/schema.sql
```

### 5. Build the Project

```bash
mvn clean package -DskipTests
```

## Running the Application

### Option 1: Using the run script (Recommended)

```bash
./run.sh
```

### Option 2: Manual execution

```bash
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
java -jar target/railway-booking-system-1.0.0.jar
```

### Option 3: Using Maven

```bash
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
mvn exec:java -Dexec.mainClass="com.railway.Main"
```

## Troubleshooting

### Java Version Issues

If you encounter Java version issues, make sure Java 11 is in your PATH:
```bash
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
java -version
```

### MySQL Connection Issues

1. Make sure MySQL is running:
   ```bash
   brew services start mysql
   ```

2. Check database credentials in `src/main/resources/config/database.properties`

3. Test MySQL connection:
   ```bash
   mysql -u root railway_booking
   ```

### Build Errors

If you encounter build errors:

1. Clean the project:
   ```bash
   mvn clean
   ```

2. Rebuild:
   ```bash
   mvn package -DskipTests
   ```

3. If still failing, update Maven dependencies:
   ```bash
   mvn dependency:purge-local-repository
   mvn clean install -DskipTests
   ```

### GUI Not Appearing

If the GUI doesn't appear:

1. Make sure you're running the correct JAR file:
   ```bash
   ls -la target/railway-booking-system-1.0.0.jar
   ```

2. Check for errors in the terminal output

3. Try running with verbose output:
   ```bash
   java -jar target/railway-booking-system-1.0.0.jar -verbose
   ```

## Default Credentials

The system comes with pre-populated sample data:

### Admin Account
- Username: `admin`
- Password: `admin123`

### User Accounts
- Username: `john_doe`
  - Password: `password123`
- Username: `jane_smith`
  - Password: `password456`

## Next Steps

After successful installation, you can:
1. Log in using the default credentials
2. Explore the booking system features
3. Manage trains (admin only)
4. Book tickets
5. View bookings

## Database Configuration

The default database configuration is:
- **Host**: localhost
- **Port**: 3306
- **Database**: railway_booking
- **Username**: root
- **Password**: (empty)

To change these settings, edit:
```
src/main/resources/config/database.properties
```

## Dependencies Installed

The setup process installs:

### Core Dependencies
- **Java Development Kit (JDK) 11**
- **Apache Maven 3.9+**
- **MySQL 9.4+**

### Java Libraries (via Maven)
- MySQL Connector/J 8.0.33
- H2 Database 2.1.214 (for testing)
- Apache Commons DBCP2 2.9.0 (connection pooling)
- SLF4J 2.0.7 + Logback 1.4.8 (logging)
- Jackson 2.15.2 (JSON processing)
- Hibernate Validator 8.0.0
- JUnit Jupiter 5.9.3 (testing)
- Mockito 5.3.1 (testing)

All dependencies are automatically downloaded and managed by Maven.

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the logs in the console output
3. Verify all prerequisites are installed correctly

