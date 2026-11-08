#!/bin/bash

# Railway Booking System - Run Script

echo "=========================================="
echo "Starting Railway Booking System..."
echo "=========================================="
echo ""

# Set Java path if Homebrew OpenJDK 21 exists
if [ -d "/opt/homebrew/opt/openjdk@21/bin" ]; then
    export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
elif [ -d "/usr/local/opt/openjdk@21/bin" ]; then
    export PATH="/usr/local/opt/openjdk@21/bin:$PATH"
fi

# Check if JAR exists
if [ ! -f "target/railway-booking-system-1.0.0.jar" ]; then
    echo "Application JAR not found. Building project with Maven..."
    mvn clean package
    if [ $? -ne 0 ]; then
        echo "Error: Maven build failed!"
        exit 1
    fi
fi

# Check if MySQL is running
if command -v brew &> /dev/null && brew services list 2>/dev/null | grep -q "mysql.*started"; then
    echo "✓ MySQL service is running"
else
    echo "ℹ MySQL status check: ensure MySQL is running on localhost:3306"
fi

# Run the application
echo ""
echo "Launching Railway Booking System..."
echo "=========================================="
echo ""
java -jar target/railway-booking-system-1.0.0.jar "$@"

