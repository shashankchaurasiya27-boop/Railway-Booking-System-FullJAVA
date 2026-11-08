# Railway Booking System - Quick Start Guide

## Prerequisites

1. **Java 11 or higher**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **MySQL 8.0+**
   ```bash
   mysql --version
   ```

## Installation Steps

### Step 1: Clone/Download the Project
```bash
cd railway-booking-system
```

### Step 2: Configure Database

#### Option A: Automated Setup (Recommended)
```bash
chmod +x setup-database.sh
./setup-database.sh
```

#### Option B: Manual Setup
1. Edit `src/main/resources/config/database.properties`
2. Update MySQL username and password:
   ```properties
   db.username=your_mysql_username
   db.password=your_mysql_password
   ```

### Step 3: Build the Project
```bash
mvn clean package
```

### Step 4: Run the Application

#### Option A: Using JAR file
```bash
java -jar target/railway-booking-system-1.0.0.jar
```

#### Option B: Using Maven
```bash
mvn exec:java -Dexec.mainClass="com.railway.Main"
```

#### Option C: Using the run script
```bash
chmod +x run.sh
./run.sh
```

## First Time Launch

When you run the application for the first time:

1. **Database Auto-Initialization**
   - The application will automatically detect that the database is not initialized
   - It will create the `railway_booking` database
   - Create all required tables (users, trains, stations, routes, bookings, tickets, payments)
   - Load test data with sample records

2. **Console Output**
   ```
   === Railway Booking System ===
   Starting application...
   
   Testing database connection...
   ✓ Database connection test successful!
   
   Database not initialized. Initializing now...
   Executing: database/unified_schema.sql
   ✓ Executed 15/15 statements from database/unified_schema.sql
   Executing: database/test_data.sql
   ✓ Executed 12/12 statements from database/test_data.sql
   === Database Initialization Complete ===
   
   === Database Statistics ===
   users          : 5 records
   stations       : 8 records
   trains         : 6 records
   routes         : 6 records
   bookings       : 4 records
   tickets        : 7 records
   payments       : 4 records
   ===========================
   ```

3. **Login Screen**
   - The GUI login window will appear
   - Use test credentials to login

## Test Credentials

### Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Access**: Full system administration

### Regular User Accounts
| Username | Password | Description |
|----------|----------|-------------|
| john_doe | password123 | Sample user with bookings |
| jane_smith | password456 | Sample user with bookings |
| mike_wilson | password789 | Sample user with bookings |
| sarah_jones | password101 | Sample user with bookings |

## Features to Test

### As a Regular User:
1. **Search Trains**: Browse available trains and routes
2. **Book Tickets**: Make new bookings for journeys
3. **View Bookings**: See your booking history
4. **Cancel Bookings**: Cancel existing bookings
5. **Profile Management**: Update your profile information

### As an Admin:
1. **Manage Trains**: Add, edit, delete trains
2. **Manage Routes**: Configure train routes
3. **Manage Stations**: Add new stations
4. **View All Bookings**: Monitor system-wide bookings
5. **User Management**: View and manage users

## Sample Data Included

### Stations (8 cities)
- Mumbai Central (MMCT)
- New Delhi (NDLS)
- Bangalore City (SBC)
- Chennai Central (MAS)
- Hyderabad Deccan (HYB)
- Kolkata Howrah (HWH)
- Pune Junction (PUNE)
- Ahmedabad Junction (ADI)

### Trains (6 trains)
- 12345: Mumbai-Delhi Express
- 12346: Delhi-Bangalore Superfast
- 12347: Chennai-Hyderabad Passenger
- 12348: Kolkata-Delhi Express
- 12349: Mumbai-Pune Local
- 12350: Delhi-Ahmedabad Superfast

### Routes (6 active routes)
- Mumbai → Delhi (1384 km, ₹500)
- Delhi → Bangalore (2160 km, ₹800)
- Chennai → Hyderabad (715 km, ₹200)
- Kolkata → Delhi (1447 km, ₹600)
- Mumbai → Pune (192 km, ₹50)
- Delhi → Ahmedabad (928 km, ₹400)

## Troubleshooting

### Issue: "Connection refused"
**Cause**: MySQL server is not running

**Solution**:
```bash
# macOS
brew services start mysql

# Linux
sudo systemctl start mysql

# Windows
net start MySQL80
```

### Issue: "Access denied for user"
**Cause**: Incorrect MySQL credentials

**Solution**:
1. Verify MySQL username and password
2. Update `src/main/resources/config/database.properties`
3. Or run `./setup-database.sh` again

### Issue: "Unknown database 'railway_booking'"
**Cause**: Database not created (should auto-create)

**Solution**:
```bash
# Manually create database
mysql -u root -p
CREATE DATABASE railway_booking;
exit;
```

### Issue: Build fails with "package does not exist"
**Cause**: Dependencies not downloaded

**Solution**:
```bash
mvn clean install -U
```

### Issue: "Table already exists" warnings
**Cause**: Database already initialized (this is normal)

**Solution**: No action needed. To reset:
```bash
mvn exec:java -Dexec.mainClass="com.railway.util.DatabaseInitializer"
# Choose 'yes' when prompted to reset
```

## Database Management

### View Database Statistics
```bash
mvn exec:java -Dexec.mainClass="com.railway.util.DatabaseInitializer"
```

### Reset Database (Fresh Start)
```bash
# Run the initializer and choose 'yes' to reset
mvn exec:java -Dexec.mainClass="com.railway.util.DatabaseInitializer"
```

### Manual Database Access
```bash
mysql -u root -p
USE railway_booking;
SHOW TABLES;
SELECT * FROM users;
```

## Project Structure

```
railway-booking-system/
├── src/main/
│   ├── java/com/railway/
│   │   ├── Main.java                    # Application entry point
│   │   ├── controller/                  # UI controllers
│   │   ├── dao/                         # Database access objects
│   │   │   ├── UserDAO.java
│   │   │   ├── TrainDAO.java
│   │   │   ├── BookingDAO.java
│   │   │   ├── TicketDAO.java
│   │   │   ├── PaymentDAO.java
│   │   │   └── ...
│   │   ├── model/                       # Data models
│   │   ├── service/                     # Business logic
│   │   ├── util/                        # Utilities
│   │   │   ├── DatabaseConnection.java
│   │   │   └── DatabaseInitializer.java
│   │   └── view/                        # GUI frames
│   └── resources/
│       ├── config/
│       │   └── database.properties      # DB configuration
│       └── database/
│           ├── unified_schema.sql       # Database schema
│           └── test_data.sql            # Sample data
├── pom.xml                              # Maven dependencies
├── DATABASE_INTEGRATION.md              # Detailed DB docs
├── QUICKSTART.md                        # This file
└── setup-database.sh                    # Setup script
```

## Next Steps

1. **Explore the Application**: Login and test all features
2. **Review the Code**: Check out the DAO implementations
3. **Read Documentation**: See `DATABASE_INTEGRATION.md` for details
4. **Customize**: Add your own trains, routes, and features
5. **Extend**: Implement additional features like:
   - Waitlist management
   - Dynamic pricing
   - Email notifications
   - Seat selection UI
   - Payment gateway integration

## Support & Documentation

- **Database Integration**: See `DATABASE_INTEGRATION.md`
- **Code Analysis**: See `COMPREHENSIVE_CODE_ANALYSIS.md`
- **Dependencies**: See `DEPENDENCIES.md`
- **Testing**: See `TESTING.md`

## Development

### Running in Development Mode
```bash
# Compile and run
mvn clean compile exec:java -Dexec.mainClass="com.railway.Main"

# Run tests
mvn test

# Package without tests
mvn package -DskipTests
```

### IDE Setup
1. Import as Maven project
2. Set JDK 11+
3. Run `Main.java`

---

**Enjoy using the Railway Booking System!** 🚂

For issues or questions, check the console logs and database statistics.
