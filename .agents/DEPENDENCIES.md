# Railway Booking System - Dependencies Documentation

## Overview

This document lists all dependencies required to run the Railway Booking System, including system requirements and Java libraries.

## System Dependencies

### 1. Java Development Kit (JDK)
- **Version**: OpenJDK 11.0.28 or higher
- **Installation**: `brew install openjdk@11`
- **Purpose**: Runtime environment for Java applications
- **Environment Setup**:
  ```bash
  export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
  ```

### 2. Apache Maven
- **Version**: 3.9.11 or higher
- **Installation**: `brew install maven`
- **Purpose**: Build automation and dependency management
- **Verify**: `mvn -version`

### 3. MySQL Database
- **Version**: 9.4.0 or higher
- **Installation**: `brew install mysql`
- **Purpose**: Relational database for data persistence
- **Start Service**: `brew services start mysql`
- **Verify**: `mysql -u root`

## Java Dependencies (Maven)

All Java dependencies are managed through Maven and automatically downloaded when building the project.

### Core Database Dependencies

#### MySQL Connector/J
- **Artifact**: `com.mysql:mysql-connector-j`
- **Version**: 8.0.33
- **Purpose**: JDBC driver for MySQL database connectivity
- **Maven**:
  ```xml
  <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.0.33</version>
  </dependency>
  ```

#### H2 Database
- **Artifact**: `com.h2database:h2`
- **Version**: 2.1.214
- **Purpose**: Embedded in-memory database for testing and development
- **Maven**:
  ```xml
  <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <version>2.1.214</version>
  </dependency>
  ```

#### Apache Commons DBCP2
- **Artifact**: `org.apache.commons:commons-dbcp2`
- **Version**: 2.9.0
- **Purpose**: Database connection pooling for improved performance
- **Maven**:
  ```xml
  <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-dbcp2</artifactId>
      <version>2.9.0</version>
  </dependency>
  ```

#### Apache Commons Pool2
- **Artifact**: `org.apache.commons:commons-pool2`
- **Version**: 2.11.1
- **Purpose**: Object pooling framework (used by DBCP2)
- **Maven**:
  ```xml
  <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-pool2</artifactId>
      <version>2.11.1</version>
  </dependency>
  ```

### Logging Dependencies

#### SLF4J API
- **Artifact**: `org.slf4j:slf4j-api`
- **Version**: 2.0.7
- **Purpose**: Simple Logging Facade for Java (abstraction layer)
- **Maven**:
  ```xml
  <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>2.0.7</version>
  </dependency>
  ```

#### Logback Classic
- **Artifact**: `ch.qos.logback:logback-classic`
- **Version**: 1.4.8
- **Purpose**: Logging implementation for SLF4J
- **Maven**:
  ```xml
  <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.4.8</version>
  </dependency>
  ```

#### Logback Core
- **Artifact**: `ch.qos.logback:logback-core`
- **Version**: 1.4.8
- **Purpose**: Core logging functionality for Logback
- **Note**: Automatically included with logback-classic

### Utility Dependencies

#### Apache Commons Lang3
- **Artifact**: `org.apache.commons:commons-lang3`
- **Version**: 3.12.0
- **Purpose**: Utility classes for Java (String manipulation, date handling, etc.)
- **Maven**:
  ```xml
  <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.12.0</version>
  </dependency>
  ```

#### Jackson Databind
- **Artifact**: `com.fasterxml.jackson.core:jackson-databind`
- **Version**: 2.15.2
- **Purpose**: JSON serialization/deserialization
- **Maven**:
  ```xml
  <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.15.2</version>
  </dependency>
  ```

#### Jackson Annotations
- **Artifact**: `com.fasterxml.jackson.core:jackson-annotations`
- **Version**: 2.15.2
- **Purpose**: JSON annotations for Jackson
- **Note**: Automatically included with jackson-databind

#### Jackson Core
- **Artifact**: `com.fasterxml.jackson.core:jackson-core`
- **Version**: 2.15.2
- **Purpose**: Core Jackson functionality
- **Note**: Automatically included with jackson-databind

### Validation Dependencies

#### Validation API
- **Artifact**: `javax.validation:validation-api`
- **Version**: 2.0.1.Final
- **Purpose**: Bean Validation API specification
- **Maven**:
  ```xml
  <dependency>
      <groupId>javax.validation</groupId>
      <artifactId>validation-api</artifactId>
      <version>2.0.1.Final</version>
  </dependency>
  ```

#### Hibernate Validator
- **Artifact**: `org.hibernate.validator:hibernate-validator`
- **Version**: 8.0.0.Final
- **Purpose**: Reference implementation of Bean Validation
- **Maven**:
  ```xml
  <dependency>
      <groupId>org.hibernate.validator</groupId>
      <artifactId>hibernate-validator</artifactId>
      <version>8.0.0.Final</version>
  </dependency>
  ```

#### Jakarta Validation API
- **Artifact**: `jakarta.validation:jakarta.validation-api`
- **Version**: 3.0.2
- **Purpose**: Jakarta Bean Validation API
- **Note**: Automatically included with hibernate-validator

### Testing Dependencies

#### JUnit Jupiter
- **Artifact**: `org.junit.jupiter:junit-jupiter`
- **Version**: 5.9.3
- **Scope**: test
- **Purpose**: Unit testing framework
- **Maven**:
  ```xml
  <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.9.3</version>
      <scope>test</scope>
  </dependency>
  ```

#### Mockito Core
- **Artifact**: `org.mockito:mockito-core`
- **Version**: 5.3.1
- **Scope**: test
- **Purpose**: Mocking framework for unit tests
- **Maven**:
  ```xml
  <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <version>5.3.1</version>
      <scope>test</scope>
  </dependency>
  ```

## Build Plugins

### Maven Compiler Plugin
- **Version**: 3.11.0
- **Purpose**: Compiles Java source code
- **Configuration**: Java 11 source and target

### Maven Surefire Plugin
- **Version**: 3.1.2
- **Purpose**: Runs unit tests during Maven build

### Maven Shade Plugin
- **Version**: 3.4.1
- **Purpose**: Creates executable JAR with all dependencies
- **Output**: `railway-booking-system-1.0.0.jar` (12 MB)
- **Main Class**: `com.railway.Main`

### Maven Resources Plugin
- **Version**: 3.3.1
- **Purpose**: Copies resources to output directory

## Dependency Tree Summary

```
railway-booking-system
├── Database
│   ├── MySQL Connector/J 8.0.33
│   ├── H2 Database 2.1.214
│   ├── Apache Commons DBCP2 2.9.0
│   └── Apache Commons Pool2 2.11.1
├── Logging
│   ├── SLF4J API 2.0.7
│   ├── Logback Classic 1.4.8
│   └── Logback Core 1.4.8
├── Utilities
│   ├── Apache Commons Lang3 3.12.0
│   ├── Jackson Databind 2.15.2
│   ├── Jackson Annotations 2.15.2
│   └── Jackson Core 2.15.2
├── Validation
│   ├── Validation API 2.0.1.Final
│   ├── Hibernate Validator 8.0.0.Final
│   └── Jakarta Validation API 3.0.2
└── Testing (scope: test)
    ├── JUnit Jupiter 5.9.3
    └── Mockito Core 5.3.1
```

## Installation Commands

### Quick Install (All Dependencies)
```bash
# Install system dependencies
brew install openjdk@11 maven mysql

# Start MySQL
brew services start mysql

# Configure Java path
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"

# Clone and build project (Maven will download Java dependencies)
cd railway-booking-system
mvn clean package -DskipTests
```

### Verify Installation

```bash
# Check Java
java -version
# Expected: openjdk version "11.0.28" ...

# Check Maven
mvn -version
# Expected: Apache Maven 3.9.11 ...

# Check MySQL
mysql --version
# Expected: mysql  Ver 9.4.0 ...

# Check if MySQL is running
brew services list | grep mysql
# Expected: mysql ... started ...
```

## Dependency Management

### Update Dependencies
To update to latest versions:
```bash
mvn versions:display-dependency-updates
mvn versions:use-latest-versions
```

### Security Vulnerabilities
Check for security issues:
```bash
mvn dependency:analyze
mvn org.owasp:dependency-check-maven:check
```

### Resolve Conflicts
If dependency conflicts occur:
```bash
mvn dependency:tree
mvn dependency:analyze-duplicate
```

## File Sizes

- **Total JAR Size**: ~12 MB (includes all dependencies)
- **Source Code**: ~24 KB
- **Maven Dependencies Download**: ~15 MB (cached in `~/.m2/repository`)

## Compatibility

### Java Version Compatibility
- **Minimum**: Java 11
- **Recommended**: Java 11 LTS
- **Maximum Tested**: Java 17

### Operating System Compatibility
- **macOS**: ✅ Fully supported
- **Linux**: ✅ Fully supported
- **Windows**: ✅ Supported (adjust paths accordingly)

### Database Compatibility
- **MySQL**: 8.0+ (Recommended)
- **H2**: 2.1+ (For testing only)
- **MariaDB**: Compatible (alternative to MySQL)

## License Information

All dependencies are open source with permissive licenses:
- **Apache License 2.0**: Most Apache Commons libraries, Jackson, Hibernate Validator
- **MIT License**: SLF4J
- **EPL 1.0**: Logback
- **GPL v2 with FOSS Exception**: MySQL Connector/J
- **EPL 2.0 / MPL 2.0**: H2 Database

## Support and Updates

For dependency issues:
1. Check Maven Central for latest versions: https://search.maven.org/
2. Review dependency documentation
3. Check for security advisories
4. Update pom.xml and rebuild

For system dependency issues:
1. Update Homebrew: `brew update`
2. Upgrade packages: `brew upgrade`
3. Check Homebrew doctor: `brew doctor`

