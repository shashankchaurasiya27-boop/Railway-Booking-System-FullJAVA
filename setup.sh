#!/bin/bash

# Railway Booking System - Setup Script

echo "=========================================="
echo "Railway Booking System - Setup Script"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Java is installed
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✓${NC} Java is installed: $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} Java is not installed!"
    echo "Installing Java 21..."
    brew install openjdk@21
    export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
    echo -e "${GREEN}✓${NC} Java 21 installed successfully"
fi

# Check if Maven is installed
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo -e "${GREEN}✓${NC} Maven is installed: $MVN_VERSION"
else
    echo -e "${RED}✗${NC} Maven is not installed!"
    echo "Installing Maven..."
    brew install maven
    echo -e "${GREEN}✓${NC} Maven installed successfully"
fi

# Check if MySQL is installed and running
echo "Checking MySQL installation..."
if command -v mysql &> /dev/null; then
    echo -e "${GREEN}✓${NC} MySQL is installed"
    
    # Check if MySQL is running
    if brew services list | grep -q "mysql.*started"; then
        echo -e "${GREEN}✓${NC} MySQL is running"
    else
        echo -e "${YELLOW}⚠${NC} MySQL is not running. Starting MySQL..."
        brew services start mysql
        sleep 2
        echo -e "${GREEN}✓${NC} MySQL started successfully"
    fi
else
    echo -e "${RED}✗${NC} MySQL is not installed!"
    echo "Installing MySQL..."
    brew install mysql
    brew services start mysql
    sleep 2
    echo -e "${GREEN}✓${NC} MySQL installed and started successfully"
fi

# Create database
echo ""
echo "Setting up database..."
mysql -u root -e "CREATE DATABASE IF NOT EXISTS railway_booking;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Database 'railway_booking' created successfully"
else
    echo -e "${YELLOW}⚠${NC} Database may already exist or there was an error"
fi

# Import schema
echo "Importing database schema..."
mysql -u root railway_booking < src/main/resources/database/schema.sql 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Database schema imported successfully"
else
    echo -e "${YELLOW}⚠${NC} Schema may already exist or there was an error"
fi

# Clean and build the project
echo ""
echo "Building the project..."
if [ -d "/opt/homebrew/opt/openjdk@21/bin" ]; then
    export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
elif [ -d "/usr/local/opt/openjdk@21/bin" ]; then
    export PATH="/usr/local/opt/openjdk@21/bin:$PATH"
fi
mvn clean package

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Project built successfully"
    echo ""
    echo "=========================================="
    echo "Setup completed successfully!"
    echo "=========================================="
    echo ""
    echo "To run the application:"
    echo "  ./run.sh"
    echo ""
    echo "Or manually:"
    echo "  export PATH=\"/opt/homebrew/opt/openjdk@21/bin:\$PATH\""
    echo "  java -jar target/railway-booking-system-1.0.0.jar"
    echo ""
else
    echo -e "${RED}✗${NC} Build failed! Please check the errors above."
    exit 1
fi

