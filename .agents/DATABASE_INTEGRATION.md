# Railway Booking System - Database Integration Guide

## Overview
This document describes the database integration for the Railway Booking System, including schema design, data loading, and best practices based on industry standards for ticket booking systems.

## Database Schema

### Entity-Relationship Design
The system uses a normalized relational database design with the following key entities:

#### 1. **Users Table**
- Stores user account information
- Supports both regular users and administrators
- Fields: id, username, password, email, first_name, last_name, phone, address, user_type, timestamps

#### 2. **Stations Table**
- Contains railway station information
- Fields: id, station_name, station_code, city, state, created_at

#### 3. **Trains Table**
- Stores train information
- Fields: id, train_number, train_name, train_type (EXPRESS/PASSENGER/SUPERFAST), total_seats, status

#### 4. **Routes Table**
- Defines train routes between stations
- Fields: id, train_id, source_station_id, destination_station_id, departure_time, arrival_time, distance_km, fare, available_seats

#### 5. **Bookings Table**
- Main booking records
- Fields: id, booking_id, user_id, train_id, route_id, journey_date, number_of_passengers, total_fare, booking_status

#### 6. **Tickets Table**
- Individual passenger tickets (one booking can have multiple tickets)
- Fields: id, ticket_number, booking_id, passenger_name, passenger_age, passenger_gender, seat_number, ticket_status

#### 7. **Payments Table**
- Payment transaction records
- Fields: id, payment_id, booking_id, amount, payment_method, payment_status, transaction_id, payment_date

## Database Configuration

### Connection Settings
Database connection is configured in `src/main/resources/config/database.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/railway_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=
```

### Prerequisites
1. **MySQL Server** must be installed and running
2. **MySQL Connector/J** (included in pom.xml)
3. Default MySQL port: 3306

## Automatic Database Initialization

The application includes automatic database initialization:

### Features
1. **Auto-detection**: Checks if database is initialized on startup
2. **Schema Creation**: Automatically creates all required tables
3. **Test Data Loading**: Loads sample data from `test_data.sql`
4. **Statistics Display**: Shows record counts for all tables

### Manual Initialization
You can also initialize the database manually:

```bash
# Compile the project
mvn clean compile

# Run the database initializer
mvn exec:java -Dexec.mainClass="com.railway.util.DatabaseInitializer"
```

## Test Data

The system comes with pre-populated test data:

### Sample Users
- **Admin**: username=`admin`, password=`admin123`
- **Regular Users**: 
  - john_doe / password123
  - jane_smith / password456
  - mike_wilson / password789
  - sarah_jones / password101

### Sample Data Includes
- 8 Railway Stations (Mumbai, Delhi, Bangalore, Chennai, etc.)
- 6 Trains with different types
- 6 Routes connecting major cities
- 4 Sample Bookings with tickets
- 4 Payment records

## Best Practices Implemented

Based on research of railway booking systems like IRCTC:

### 1. **Normalized Design**
- Separate tables for bookings and tickets (one-to-many relationship)
- Proper foreign key constraints for data integrity
- Indexed columns for faster queries

### 2. **Seat Management**
- `available_seats` in routes table for real-time availability
- Atomic updates to prevent overbooking
- Seat allocation tracked in tickets table

### 3. **Booking Status Management**
- PENDING: Initial booking state
- CONFIRMED: Payment successful
- CANCELLED: User cancelled
- WAITING: Waitlist feature (future enhancement)

### 4. **Payment Integration**
- Separate payments table for transaction tracking
- Multiple payment methods supported (CARD, UPI, NET_BANKING, CASH)
- Transaction ID for reconciliation

### 5. **Audit Trail**
- Timestamps on all major tables (created_at, updated_at)
- Booking history maintained
- Payment history tracked

### 6. **Security Considerations**
- User authentication through username/password
- Role-based access (USER vs ADMIN)
- ⚠️ **Note**: Current implementation uses plain text passwords for testing. Production systems should use bcrypt or similar hashing.

## DAO Layer Architecture

### Data Access Objects (DAOs)
The application uses the DAO pattern for database operations:

- **UserDAO**: User management and authentication
- **StationDAO**: Station information
- **TrainDAO**: Train management
- **RouteDAO**: Route and schedule management
- **BookingDAO**: Booking operations
- **TicketDAO**: Ticket management
- **PaymentDAO**: Payment processing

### Key DAO Methods
```java
// UserDAO
User authenticateUser(String username, String password)
boolean registerUser(User user)

// BookingDAO
boolean createBooking(Booking booking)
List<Booking> getBookingsByUserId(int userId)
boolean cancelBooking(String bookingId)

// PaymentDAO
boolean createPayment(Payment payment)
Payment getPaymentByBookingId(String bookingId)
```

## Database Utilities

### DatabaseConnection
Singleton pattern for database connection management:
```java
Connection conn = DatabaseConnection.getConnection();
DatabaseConnection.testConnection();
DatabaseConnection.closeConnection();
```

### DatabaseInitializer
Utility for database setup and management:
```java
// Initialize database
DatabaseInitializer.initializeDatabase();

// Check if initialized
boolean isReady = DatabaseInitializer.isDatabaseInitialized();

// Print statistics
DatabaseInitializer.printDatabaseStats();

// Reset database (drops all tables)
DatabaseInitializer.resetDatabase();
```

## Running the Application

### Step 1: Start MySQL
Ensure MySQL server is running on localhost:3306

### Step 2: Configure Database
Update `src/main/resources/config/database.properties` if needed

### Step 3: Build and Run
```bash
# Build the project
mvn clean package

# Run the application
java -jar target/railway-booking-system-1.0.0.jar

# Or use Maven
mvn exec:java -Dexec.mainClass="com.railway.Main"
```

### Step 4: Login
Use the test credentials to login and explore the system.

## Troubleshooting

### Common Issues

#### 1. Connection Refused
```
Error: java.sql.SQLException: Connection refused
```
**Solution**: Make sure MySQL is running and accessible on port 3306

#### 2. Access Denied
```
Error: Access denied for user 'root'@'localhost'
```
**Solution**: Update username/password in database.properties

#### 3. Database Not Found
```
Error: Unknown database 'railway_booking'
```
**Solution**: The application will create it automatically. Ensure MySQL user has CREATE DATABASE privileges.

#### 4. Table Already Exists
```
Warning: Table 'users' already exists
```
**Solution**: This is normal if re-running initialization. Use `resetDatabase()` to start fresh.

## Performance Considerations

### Indexes Created
The schema includes indexes on:
- User lookups (username, email)
- Station codes
- Train numbers
- Booking queries (user_id, train_id, route_id, journey_date)
- Ticket lookups (booking_id)
- Payment lookups (booking_id)

### Connection Pooling
The project includes Apache Commons DBCP2 for connection pooling (configured in pom.xml).

### Query Optimization
- JOINs used efficiently in DAO queries
- Prepared statements prevent SQL injection
- Batch operations for bulk inserts

## Future Enhancements

### Recommended Improvements
1. **Password Hashing**: Implement bcrypt for secure password storage
2. **Connection Pool Configuration**: Fine-tune pool settings for production
3. **Caching Layer**: Add Redis/Memcached for frequently accessed data
4. **Read Replicas**: For scaling read operations
5. **Waitlist Management**: Implement automatic confirmation from waitlist
6. **Dynamic Pricing**: Add surge pricing based on demand
7. **Seat Layout**: Detailed seat maps and coach information
8. **Booking History**: Archive old bookings for analytics

## References

This implementation follows best practices from:
- IRCTC (Indian Railway Catering and Tourism Corporation) system design
- GeeksforGeeks database design patterns for reservation systems
- Railway management system standards
- E-commerce payment gateway integration patterns

## Support

For issues or questions:
1. Check the logs in the console output
2. Verify MySQL connection and credentials
3. Review the DAO implementations for query details
4. Use `DatabaseInitializer.printDatabaseStats()` to verify data

---

**Last Updated**: October 2025
**Version**: 1.0.0
