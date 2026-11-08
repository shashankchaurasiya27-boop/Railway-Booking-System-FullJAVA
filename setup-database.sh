#!/bin/bash

# Railway Booking System - Database Setup Script
# This script helps set up the MySQL database for the application

echo "======================================"
echo "Railway Booking System - Database Setup"
echo "======================================"
echo ""

# Check if MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo "❌ MySQL is not installed or not in PATH"
    echo "Please install MySQL first: https://dev.mysql.com/downloads/mysql/"
    exit 1
fi

echo "✓ MySQL found"
echo ""

# Get MySQL credentials
read -p "Enter MySQL username [root]: " DB_USER
DB_USER=${DB_USER:-root}

read -sp "Enter MySQL password: " DB_PASS
echo ""
echo ""

# Test MySQL connection
echo "Testing MySQL connection..."
mysql -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &> /dev/null

if [ $? -ne 0 ]; then
    echo "❌ Failed to connect to MySQL. Please check your credentials."
    exit 1
fi

echo "✓ MySQL connection successful"
echo ""

# Update database.properties file
PROPERTIES_FILE="src/main/resources/config/database.properties"

if [ -f "$PROPERTIES_FILE" ]; then
    echo "Updating database.properties..."
    
    # Backup existing file
    cp "$PROPERTIES_FILE" "$PROPERTIES_FILE.backup"
    
    # Update username and password
    sed -i.tmp "s/^db.username=.*/db.username=$DB_USER/" "$PROPERTIES_FILE"
    sed -i.tmp "s/^db.password=.*/db.password=$DB_PASS/" "$PROPERTIES_FILE"
    rm -f "$PROPERTIES_FILE.tmp"
    
    echo "✓ Updated database.properties"
else
    echo "⚠️  database.properties not found at $PROPERTIES_FILE"
fi

echo ""
echo "======================================"
echo "Setup Complete!"
echo "======================================"
echo ""
echo "Next steps:"
echo "1. Build the project: mvn clean package"
echo "2. Run the application: java -jar target/railway-booking-system-1.0.0.jar"
echo "   Or: mvn exec:java -Dexec.mainClass=\"com.railway.Main\""
echo ""
echo "The application will automatically:"
echo "  - Create the 'railway_booking' database"
echo "  - Create all required tables"
echo "  - Load test data"
echo ""
echo "Test Login Credentials:"
echo "  Admin: admin / admin123"
echo "  User: john_doe / password123"
echo ""
