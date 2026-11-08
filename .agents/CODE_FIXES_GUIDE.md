# 🛠️ Code Fixes Guide
## Railway Booking System - Detailed Solutions

---

## 1. Fix Resource Leaks in DAOs

### ❌ Current Code (BookingDAO.java)
```java
public Booking getBookingById(String bookingId) {
    String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                 "CONCAT(s1.station_name, ' to ', s2.station_name) as route_description " +
                 "FROM bookings b " +
                 "JOIN trains t ON b.train_id = t.id " +
                 "JOIN routes r ON b.route_id = r.id " +
                 "JOIN stations s1 ON r.source_station_id = s1.id " +
                 "JOIN stations s2 ON r.destination_station_id = s2.id " +
                 "JOIN users u ON b.user_id = u.id " +
                 "WHERE b.booking_id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, bookingId);
        ResultSet rs = pstmt.executeQuery();  // ❌ Not auto-closed
        
        if (rs.next()) {
            return extractBookingFromResultSet(rs);
        }
        
    } catch (SQLException e) {
        e.printStackTrace();  // ❌ Also bad
    }
    
    return null;
}
```

### ✅ Fixed Code
```java
public Booking getBookingById(String bookingId) {
    String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                 "CONCAT(s1.station_name, ' to ', s2.station_name) as route_description " +
                 "FROM bookings b " +
                 "JOIN trains t ON b.train_id = t.id " +
                 "JOIN routes r ON b.route_id = r.id " +
                 "JOIN stations s1 ON r.source_station_id = s1.id " +
                 "JOIN stations s2 ON r.destination_station_id = s2.id " +
                 "JOIN users u ON b.user_id = u.id " +
                 "WHERE b.booking_id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {  // ✅ Auto-closed
        
        pstmt.setString(1, bookingId);
        
        if (rs.next()) {
            return extractBookingFromResultSet(rs);
        }
        
    } catch (SQLException e) {
        logger.error("Error fetching booking: {}", bookingId, e);  // ✅ Proper logging
    }
    
    return null;
}
```

**Apply this fix to ALL DAO files:**
- BookingDAO.java (7 methods)
- TrainDAO.java (5 methods)
- UserDAO.java (6 methods)
- RouteDAO.java (4 methods)
- StationDAO.java (6 methods)
- PaymentDAO.java (4 methods)
- TicketDAO.java (3 methods)

---

## 2. Fix Thread Safety in DatabaseConnection

### ❌ Current Code (DatabaseConnection.java)
```java
public class DatabaseConnection {
    private static Connection connection = null;  // ❌ Not thread-safe
    private static Properties properties = new Properties();
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {  // ❌ Race condition
            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully!");
        }
        return connection;
    }
}
```

### ✅ Fixed Code with Connection Pooling
```java
package com.railway.util;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static BasicDataSource dataSource;  // ✅ Thread-safe connection pool
    private static Properties properties = new Properties();
    
    static {
        try {
            // Load database properties
            InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config/database.properties");
            
            if (input != null) {
                properties.load(input);
                input.close();
            } else {
                setDefaultProperties();
            }
            
            // Initialize connection pool
            initializeDataSource();
            
        } catch (IOException e) {
            logger.error("Failed to load database configuration", e);
            setDefaultProperties();
            initializeDataSource();
        }
    }
    
    private static void initializeDataSource() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(properties.getProperty("db.url"));
        dataSource.setUsername(properties.getProperty("db.username"));
        dataSource.setPassword(properties.getProperty("db.password"));
        
        // Connection pool settings
        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(20);
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(5);
        dataSource.setMaxWaitMillis(10000);
        
        // Validation
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        
        logger.info("Database connection pool initialized");
    }
    
    private static void setDefaultProperties() {
        properties.setProperty("db.url", 
            "jdbc:mysql://localhost:3306/railway_booking?useSSL=false&serverTimezone=UTC");
        properties.setProperty("db.username", System.getenv("DB_USERNAME") != null ? 
            System.getenv("DB_USERNAME") : "root");
        properties.setProperty("db.password", System.getenv("DB_PASSWORD") != null ? 
            System.getenv("DB_PASSWORD") : "");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();  // ✅ Thread-safe
    }

    public static void closeDataSource() {
        try {
            if (dataSource != null) {
                dataSource.close();
                logger.info("Database connection pool closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing connection pool", e);
        }
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                logger.info("✓ Database connection test successful!");
            }
        } catch (SQLException e) {
            logger.error("✗ Database connection test failed", e);
        }
    }
}
```

---

## 3. Implement Password Hashing

### Step 1: Add BCrypt Dependency to pom.xml
```xml
<!-- Add after line 105 -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

### Step 2: Create Password Utility Class
```java
package com.railway.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Hash a password using BCrypt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Verify a password against a hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
```

### Step 3: Update UserDAO.java

### ❌ Current Code
```java
public boolean createUser(User user) {
    String sql = "INSERT INTO users (username, password, email, first_name, last_name, phone, user_type) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());  // ❌ Plain text
        // ... rest of code
    }
}

public User getUserByUsernameAndPassword(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";  // ❌ Plain text
    // ... rest of code
}
```

### ✅ Fixed Code
```java
import com.railway.util.PasswordUtils;

public boolean createUser(User user) {
    String sql = "INSERT INTO users (username, password, email, first_name, last_name, phone, user_type) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, PasswordUtils.hashPassword(user.getPassword()));  // ✅ Hashed
        pstmt.setString(3, user.getEmail());
        pstmt.setString(4, user.getFirstName());
        pstmt.setString(5, user.getLastName());
        pstmt.setString(6, user.getPhone());
        pstmt.setString(7, user.getUserType().name());
        
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        logger.error("Error creating user: {}", user.getUsername(), e);
        return false;
    }
}

public User getUserByUsernameAndPassword(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ?";  // ✅ Only username
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        
        pstmt.setString(1, username);
        
        if (rs.next()) {
            String hashedPassword = rs.getString("password");
            
            // ✅ Verify password hash
            if (PasswordUtils.verifyPassword(password, hashedPassword)) {
                return extractUserFromResultSet(rs);
            }
        }
        
    } catch (SQLException e) {
        logger.error("Error authenticating user: {}", username, e);
    }
    
    return null;
}
```

---

## 4. Add Transaction Management

### ❌ Current Code (BookingService.java)
```java
public String createBooking(Booking booking) {
    // Multiple database operations without transaction
    Train train = trainDAO.getTrainById(booking.getTrainId());
    int bookedSeats = bookingDAO.getBookedSeatsCount(/*...*/);
    
    // ... validation
    
    boolean success = bookingDAO.createBooking(booking);  // ❌ No transaction
    
    if (success) {
        return "SUCCESS:" + bookingId;
    }
    return "Booking failed.";
}
```

### ✅ Fixed Code with Transactions
```java
public String createBooking(Booking booking) {
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);  // ✅ Start transaction
        
        // Validate passenger
        if (!ValidationUtils.isNotEmpty(booking.getPassengerName())) {
            return "Passenger name is required.";
        }
        
        // Get train info
        Train train = trainDAO.getTrainById(booking.getTrainId(), conn);
        if (train == null) {
            return "Train not found.";
        }
        
        // Check seat availability with locking
        int bookedSeats = bookingDAO.getBookedSeatsCountWithLock(
            booking.getTrainId(), 
            booking.getRouteId(), 
            booking.getJourneyDate(),
            conn
        );
        
        if (bookedSeats >= train.getTotalSeats()) {
            booking.setBookingStatus(Booking.BookingStatus.WAITING);
            conn.rollback();
            return "No seats available. Added to waiting list.";
        }
        
        // Generate booking ID
        String bookingId = generateBookingId();
        booking.setBookingId(bookingId);
        
        // Generate seat number
        String seatNumber = bookingDAO.getNextSeatNumber(
            booking.getTrainId(),
            booking.getRouteId(),
            booking.getJourneyDate(),
            conn
        );
        booking.setSeatNumber(seatNumber);
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        
        // Create booking
        boolean success = bookingDAO.createBooking(booking, conn);
        
        if (success) {
            conn.commit();  // ✅ Commit transaction
            logger.info("Booking created successfully: {}", bookingId);
            return "SUCCESS:" + bookingId;
        } else {
            conn.rollback();  // ✅ Rollback on failure
            return "Booking failed. Please try again.";
        }
        
    } catch (SQLException e) {
        logger.error("Error creating booking", e);
        if (conn != null) {
            try {
                conn.rollback();  // ✅ Rollback on exception
            } catch (SQLException ex) {
                logger.error("Error rolling back transaction", ex);
            }
        }
        return "Booking failed due to system error.";
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);  // ✅ Reset auto-commit
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }
}
```

### Update BookingDAO to support transactions
```java
// Add overloaded method that accepts Connection
public boolean createBooking(Booking booking, Connection conn) throws SQLException {
    String sql = "INSERT INTO bookings (booking_id, user_id, train_id, route_id, journey_date, " +
                 "booking_status, total_fare, number_of_passengers) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, booking.getBookingId());
        pstmt.setInt(2, booking.getUserId());
        pstmt.setInt(3, booking.getTrainId());
        pstmt.setInt(4, booking.getRouteId());
        pstmt.setDate(5, booking.getJourneyDate());
        pstmt.setString(6, booking.getBookingStatus().name());
        pstmt.setBigDecimal(7, booking.getTotalFare());
        pstmt.setInt(8, booking.getNumberOfPassengers());
        
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
    }
}

// Add method with pessimistic locking
public int getBookedSeatsCountWithLock(int trainId, int routeId, Date journeyDate, Connection conn) 
        throws SQLException {
    String sql = "SELECT COUNT(*) FROM bookings " +
                 "WHERE train_id = ? AND route_id = ? AND journey_date = ? " +
                 "AND booking_status = 'CONFIRMED' " +
                 "FOR UPDATE";  // ✅ Pessimistic lock
    
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, trainId);
        pstmt.setInt(2, routeId);
        pstmt.setDate(3, journeyDate);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    }
    
    return 0;
}
```

---

## 5. Fix UI Layout Bug in BookingFrame

### ❌ Current Code (BookingFrame.java Lines 105-108)
```java
// Add components
add(routeInfoPanel, BorderLayout.NORTH);
add(formPanel, BorderLayout.CENTER);
add(bookingsPanel, BorderLayout.SOUTH);  // ❌ Will be overwritten
add(buttonPanel, BorderLayout.SOUTH);     // ❌ Overwrites previous
```

### ✅ Fixed Code
```java
// Add components
add(routeInfoPanel, BorderLayout.NORTH);
add(formPanel, BorderLayout.CENTER);

// Combine bottom panels
JPanel bottomPanel = new JPanel(new BorderLayout());
bottomPanel.add(bookingsPanel, BorderLayout.CENTER);
bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

add(bottomPanel, BorderLayout.SOUTH);  // ✅ Both panels now visible
```

---

## 6. Add Proper Logging

### Step 1: Create logback.xml in src/main/resources
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/railway-booking.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/railway-booking-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.railway" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### Step 2: Add Logger to Classes

### ❌ Current Code
```java
public class BookingDAO {
    // No logger
    
    public boolean createBooking(Booking booking) {
        try {
            // ... code
        } catch (SQLException e) {
            e.printStackTrace();  // ❌ Bad
            return false;
        }
    }
}
```

### ✅ Fixed Code
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookingDAO {
    private static final Logger logger = LoggerFactory.getLogger(BookingDAO.class);  // ✅
    
    public boolean createBooking(Booking booking) {
        logger.debug("Creating booking for user: {}", booking.getUserId());
        
        try {
            // ... code
            logger.info("Booking created successfully: {}", booking.getBookingId());
            return true;
        } catch (SQLException e) {
            logger.error("Error creating booking for user: {}", booking.getUserId(), e);  // ✅
            return false;
        }
    }
}
```

---

## 7. Add Input Validation

### Create Enhanced ValidationUtils
```java
package com.railway.util;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[0-9]{10}$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    
    public static boolean isValidAge(int age) {
        return age > 0 && age <= 120;
    }
    
    public static boolean isValidSeatCount(int seats) {
        return seats > 0 && seats <= 1000;
    }
    
    public static boolean isPositive(int value) {
        return value > 0;
    }
    
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
}
```

### Use in AdminFrame
```java
// ❌ Current Code
newTrain.setTotalSeats(Integer.parseInt(txtTotalSeats.getText().trim()));

// ✅ Fixed Code
try {
    int totalSeats = Integer.parseInt(txtTotalSeats.getText().trim());
    
    if (!ValidationUtils.isValidSeatCount(totalSeats)) {
        JOptionPane.showMessageDialog(this, 
            "Total seats must be between 1 and 1000");
        return;
    }
    
    newTrain.setTotalSeats(totalSeats);
} catch (NumberFormatException e) {
    JOptionPane.showMessageDialog(this, 
        "Please enter a valid number for total seats");
    return;
}
```

---

## 8. Update MySQL Connector in pom.xml

### ❌ Current Code (Lines 24-28)
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### ✅ Fixed Code
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>
```

---

## Summary of Files to Modify

### Immediate Priority
1. ✅ `DatabaseConnection.java` - Connection pooling + env variables
2. ✅ `UserDAO.java` - Password hashing
3. ✅ `UserService.java` - Password hashing
4. ✅ `BookingService.java` - Transactions
5. ✅ `BookingDAO.java` - Transactions + resource leaks
6. ✅ `BookingFrame.java` - UI layout fix
7. ✅ All DAO files - Resource leaks + logging
8. ✅ `pom.xml` - MySQL connector + BCrypt
9. ✅ Create `PasswordUtils.java`
10. ✅ Create `logback.xml`
11. ✅ Update `ValidationUtils.java`

### Apply These Patterns
- **Resource Management:** Add ResultSet to try-with-resources in all DAOs
- **Logging:** Replace printStackTrace() with logger.error() in all files
- **Validation:** Add validation before all database operations
- **Transactions:** Wrap multi-step operations in transactions

---

**Next Steps:**
1. Apply fixes in order of priority
2. Test each fix thoroughly
3. Run full regression test suite
4. Update documentation
5. Deploy to staging environment

