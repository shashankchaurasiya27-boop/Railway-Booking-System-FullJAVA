# Code Quality Analysis Report
## Railway Booking System

**Analysis Date:** December 18, 2025  
**Project:** Railway Booking System (Java Maven)  
**Total Files Analyzed:** 38 Java files

---

## Executive Summary

✅ **Build Status:** SUCCESSFUL (mvn clean compile)  
⚠️ **Critical Issues Found:** 8  
⚠️ **Medium Issues Found:** 12  
⚠️ **Low Priority Issues:** 7  

---

## 1. CRITICAL ISSUES

### 1.1 Resource Leaks - ResultSet Not Closed
**Severity:** HIGH  
**Impact:** Memory leaks, database connection exhaustion  
**Files Affected:** All DAO files (7 files)

**Problem:**
Multiple DAO files create `ResultSet` objects but don't explicitly close them in try-with-resources blocks. While the PreparedStatement closing will eventually close the ResultSet, this is not guaranteed and can lead to resource leaks.

**Affected Files:**
- `BookingDAO.java` - Lines 59, 92, 169
- `TrainDAO.java` - Lines 47, 70, 181
- `UserDAO.java` - Lines 52, 75, 98, 121, 144
- `RouteDAO.java` - Lines 55, 109, 139
- `StationDAO.java` - Lines 45, 68, 116, 140, 206
- `PaymentDAO.java` - Lines 51, 74, 141
- `TicketDAO.java` - Lines 39, 98

**Example from BookingDAO.java:**
```java
// Line 55-63 - ResultSet not in try-with-resources
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
    pstmt.setString(1, bookingId);
    ResultSet rs = pstmt.executeQuery();  // ❌ Not auto-closed
    
    if (rs.next()) {
        return extractBookingFromResultSet(rs);
    }
}
```

**Recommendation:** Add ResultSet to try-with-resources or explicitly close in finally block.

---

### 1.2 Singleton Connection Pattern - Thread Safety Issue
**Severity:** HIGH  
**Impact:** Race conditions, connection corruption in multi-threaded environment  
**File:** `DatabaseConnection.java`

**Problem:**
The `DatabaseConnection` class uses a singleton pattern with a static `Connection` object, which is NOT thread-safe. Multiple threads can call `getConnection()` simultaneously, leading to race conditions.

**Code Location:** Lines 11-12, 43-53
```java
private static Connection connection = null;  // ❌ Not thread-safe

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
```

**Recommendation:** Implement connection pooling (HikariCP or Apache DBCP2 - already in dependencies) instead of singleton pattern.

---

### 1.3 SQL Injection Vulnerability (Partial)
**Severity:** MEDIUM-HIGH  
**Impact:** Potential SQL injection in dynamic queries  
**File:** `DatabaseInitializer.java`

**Problem:**
Line 144 uses string concatenation for table names in SQL queries, which could be exploited if table names come from user input (currently they don't, but it's a bad practice).

**Code Location:** Line 144
```java
var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);  // ⚠️ String concatenation
```

**Recommendation:** While table names can't be parameterized, validate against a whitelist.

---

### 1.4 Password Storage - Plain Text
**Severity:** CRITICAL  
**Impact:** Security vulnerability - passwords stored in plain text  
**Files:** `UserDAO.java`, `UserService.java`

**Problem:**
Passwords are stored and compared as plain text in the database. No hashing or encryption is used.

**Code Location:** `UserDAO.java` Lines 49-63
```java
public User getUserByUsernameAndPassword(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";  // ❌ Plain text password
    // ...
}
```

**Recommendation:** Implement BCrypt or PBKDF2 password hashing immediately.

---

### 1.5 Missing Input Validation
**Severity:** MEDIUM  
**Impact:** Application crashes, invalid data in database  
**Files:** Multiple controller and service files

**Problem:**
Insufficient input validation in several places:
- `BookingFrame.java` - Basic validation but no sanitization
- `AdminFrame.java` - No validation for train seat numbers (could be negative)
- `RegistrationController.java` - Email format not validated

**Examples:**
```java
// AdminFrame.java Line 329 - No validation for negative seats
newTrain.setTotalSeats(Integer.parseInt(txtTotalSeats.getText().trim()));  // ⚠️ Could be negative
```

**Recommendation:** Add comprehensive validation using the existing `ValidationUtils` class.

---

### 1.6 Exception Handling - Information Disclosure
**Severity:** MEDIUM  
**Impact:** Stack traces exposed to users, security information disclosure  
**Files:** All DAO files

**Problem:**
All DAO files use `e.printStackTrace()` which prints stack traces to console. In production, this can expose sensitive information.

**Example Pattern (found in all DAOs):**
```java
} catch (SQLException e) {
    e.printStackTrace();  // ❌ Exposes stack trace
    return false;
}
```

**Recommendation:** Use proper logging framework (SLF4J is already in dependencies) instead of printStackTrace().

---

### 1.7 UI Layout Bug - Double BorderLayout.SOUTH
**Severity:** LOW  
**Impact:** UI rendering issue - only last component shown  
**File:** `BookingFrame.java`

**Problem:**
Lines 107-108 add two components to `BorderLayout.SOUTH`, which means only the last one (buttonPanel) will be displayed.

**Code Location:** Lines 105-108
```java
add(routeInfoPanel, BorderLayout.NORTH);
add(formPanel, BorderLayout.CENTER);
add(bookingsPanel, BorderLayout.SOUTH);  // ❌ Will be overwritten
add(buttonPanel, BorderLayout.SOUTH);     // ❌ Overwrites previous
```

**Recommendation:** Combine panels or use different layout positions.

---

### 1.8 Hardcoded Credentials in Default Properties
**Severity:** MEDIUM  
**Impact:** Security risk if deployed with defaults  
**File:** `DatabaseConnection.java`

**Problem:**
Default database credentials are hardcoded in the source code.

**Code Location:** Lines 37-40
```java
private static void setDefaultProperties() {
    properties.setProperty("db.url", "jdbc:mysql://localhost:3306/railway_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
    properties.setProperty("db.username", "root");
    properties.setProperty("db.password", "");  // ❌ Empty password
}
```

**Recommendation:** Remove defaults or use environment variables.

---

## 2. CODE GAPS & MISSING FUNCTIONALITY

### 2.1 Missing Transaction Management
**Severity:** HIGH  
**Impact:** Data inconsistency, partial updates  
**Files:** All Service classes

**Problem:**
No transaction management for multi-step operations. For example, booking creation should be atomic (create booking + update seat count + create payment).

**Example:** `BookingService.java` - No transaction wrapping
```java
public String createBooking(Booking booking) {
    // Multiple database operations without transaction
    // If one fails, partial data may be committed
}
```

**Recommendation:** Implement transaction management using Connection.setAutoCommit(false) and commit/rollback.

---

### 2.2 No Connection Pool Implementation
**Severity:** HIGH  
**Impact:** Poor performance, connection exhaustion  
**Files:** `DatabaseConnection.java`

**Problem:**
Despite having Apache DBCP2 in dependencies (pom.xml line 38-42), no connection pooling is implemented.

**Recommendation:** Replace singleton pattern with DBCP2 connection pool.

---

### 2.3 Missing Unit Tests
**Severity:** MEDIUM  
**Impact:** No automated testing, regression risks  
**Files:** Test directory

**Problem:**
Only one test file exists (`LoginTest.java`) and it's in the main source directory instead of test directory. No actual unit tests for services, DAOs, or controllers.

**Recommendation:** Create comprehensive unit tests using JUnit 5 (already in dependencies).

---

### 2.4 No Logging Implementation
**Severity:** MEDIUM  
**Impact:** Difficult debugging, no audit trail  
**Files:** All files

**Problem:**
SLF4J and Logback are in dependencies but not used. All logging is done via System.out.println() and printStackTrace().

**Recommendation:** Implement proper logging with different levels (DEBUG, INFO, WARN, ERROR).

---

### 2.5 Missing Data Validation in Models
**Severity:** MEDIUM  
**Impact:** Invalid data can be set on model objects  
**Files:** All model classes

**Problem:**
Model classes (Booking, Train, User, etc.) have no validation in setters. Hibernate Validator is in dependencies but not used.

**Example:** `Booking.java` - No validation
```java
public void setPassengerAge(int passengerAge) {
    this.passengerAge = passengerAge;  // ❌ Could be negative or > 150
}
```

**Recommendation:** Add @NotNull, @Min, @Max, @Email annotations using Hibernate Validator.

---

### 2.6 No API/REST Endpoints
**Severity:** LOW  
**Impact:** Limited to desktop GUI only  
**Files:** N/A

**Problem:**
Jackson is in dependencies (pom.xml line 87-91) suggesting REST API was planned, but no REST controllers exist.

**Recommendation:** If REST API is needed, implement using Spring Boot or JAX-RS.

---

### 2.7 Missing Concurrency Control
**Severity:** HIGH  
**Impact:** Race conditions in seat booking  
**Files:** `BookingDAO.java`, `BookingService.java`

**Problem:**
No pessimistic or optimistic locking for seat availability checks. Two users could book the same seat simultaneously.

**Code Location:** `BookingService.java` Lines 42-51
```java
int bookedSeats = bookingDAO.getBookedSeatsCount(
    booking.getTrainId(), 
    booking.getRouteId(), 
    booking.getJourneyDate()
);

if (bookedSeats >= train.getTotalSeats()) {  // ❌ Race condition
    // Another booking could happen between check and insert
}
```

**Recommendation:** Use database-level locking (SELECT FOR UPDATE) or optimistic locking with version numbers.

---

## 3. CIRCULAR DEPENDENCIES

### 3.1 No Circular Dependencies Detected ✅

**Analysis:**
The codebase follows a clean layered architecture:
- **View Layer** → **Controller Layer** → **Service Layer** → **DAO Layer** → **Model Layer**
- No reverse dependencies found
- Dependency graph is acyclic (DAG)

**Architecture:**
```
View (GUI Frames)
  ↓
Controller
  ↓
Service
  ↓
DAO
  ↓
Model (POJOs)
  ↓
Util (Database, Validation)
```

---

## 4. HIGH COMPLEXITY FILES

### 4.1 AdminFrame.java
**Lines:** 407  
**Complexity:** HIGH  
**Cyclomatic Complexity:** ~25-30 (estimated)

**Issues:**
- God class - handles train, booking, and user management
- Multiple responsibilities (violates Single Responsibility Principle)
- Long methods (addTrain, editTrain, deleteTrain)
- Deeply nested UI layout code

**Recommendation:** Split into separate frames: TrainManagementFrame, BookingManagementFrame, UserManagementFrame.

---

### 4.2 DatabaseInitializer.java
**Lines:** 239  
**Complexity:** MEDIUM-HIGH  
**Issues:**
- Complex SQL parsing logic
- Multiple responsibilities (schema creation, data loading, stats)
- Long methods (executeSchemaFile)

**Recommendation:** Extract SQL parsing into separate utility class.

---

### 4.3 BookingDAO.java
**Lines:** 214  
**Complexity:** MEDIUM  
**Issues:**
- Complex SQL queries with multiple JOINs
- Repeated code patterns
- No query builder or ORM

**Recommendation:** Consider using JPA/Hibernate or a query builder library.

---

### 4.4 RouteDAO.java
**Lines:** ~250 (estimated from outline)  
**Complexity:** MEDIUM-HIGH  
**Issues:**
- Complex route search logic
- Multiple JOIN queries
- Similar to BookingDAO issues

**Recommendation:** Extract common DAO patterns into base class.

---

## 5. DEPENDENCY ISSUES

### 5.1 Deprecated MySQL Connector
**Severity:** LOW  
**Impact:** Maven warning  
**File:** `pom.xml`

**Problem:**
Maven warning shows MySQL connector artifact has been relocated.

**Warning Message:**
```
[WARNING] The artifact mysql:mysql-connector-java:jar:8.0.33 has been relocated to com.mysql:mysql-connector-j:jar:8.0.33
```

**Recommendation:** Update pom.xml to use new artifact coordinates.

---

### 5.2 Unused Dependencies
**Severity:** LOW  
**Impact:** Bloated JAR file  
**Files:** `pom.xml`

**Unused Dependencies:**
- Jackson (lines 87-91) - No JSON processing found
- Hibernate Validator (lines 101-105) - No validation annotations used
- Mockito (lines 72-77) - No tests using it

**Recommendation:** Remove unused dependencies or implement the features they were intended for.

---

### 5.3 Missing Test Dependencies Scope
**Severity:** LOW  
**Impact:** Test dependencies included in production JAR  
**File:** `pom.xml`

**Problem:**
JUnit and Mockito correctly have `<scope>test</scope>`, but this is good. No issue here.

---

## 6. DESIGN PATTERN ISSUES

### 6.1 No Dependency Injection
**Severity:** MEDIUM  
**Impact:** Tight coupling, difficult testing  
**Files:** All controllers and services

**Problem:**
All dependencies are created with `new` keyword, making unit testing difficult.

**Example:** `BookingController.java` Line 13-14
```java
public BookingController() {
    bookingService = new BookingService();  // ❌ Tight coupling
}
```

**Recommendation:** Implement constructor injection or use a DI framework (Google Guice is already in Maven).

---

### 6.2 No DTO Pattern
**Severity:** LOW  
**Impact:** Domain models exposed to UI layer  
**Files:** All view classes

**Problem:**
Domain models (Booking, Train, User) are directly used in UI layer, exposing internal structure.

**Recommendation:** Create DTO (Data Transfer Object) classes for UI layer.

---

### 6.3 No Repository Pattern
**Severity:** LOW  
**Impact:** DAOs tightly coupled to SQL  
**Files:** All DAO classes

**Problem:**
DAOs contain raw SQL queries, making it difficult to switch databases or add caching.

**Recommendation:** Implement Repository pattern with abstraction layer.

---

## 7. SECURITY ISSUES SUMMARY

1. ❌ **Plain text passwords** (CRITICAL)
2. ❌ **No input sanitization** (HIGH)
3. ❌ **Stack traces exposed** (MEDIUM)
4. ❌ **Hardcoded credentials** (MEDIUM)
5. ❌ **No SQL injection protection in some areas** (MEDIUM)
6. ❌ **No CSRF protection** (LOW - desktop app)
7. ❌ **No rate limiting** (LOW - desktop app)

---

## 8. PERFORMANCE ISSUES

### 8.1 N+1 Query Problem
**Severity:** MEDIUM  
**Impact:** Poor performance with large datasets  
**Files:** Multiple DAO files

**Problem:**
Some queries could trigger N+1 problem when loading related entities.

**Example:** Loading all bookings with train and user details requires multiple queries.

**Recommendation:** Use JOIN queries (already partially implemented) or implement lazy loading.

---

### 8.2 No Caching
**Severity:** MEDIUM  
**Impact:** Repeated database queries for static data  
**Files:** All DAO files

**Problem:**
Static data like stations and train types are queried repeatedly without caching.

**Recommendation:** Implement caching layer (Guava Cache or Caffeine).

---

### 8.3 No Pagination
**Severity:** MEDIUM  
**Impact:** Memory issues with large result sets  
**Files:** `BookingDAO.getAllBookings()`, `UserDAO.getAllUsers()`

**Problem:**
Methods like `getAllBookings()` load all records into memory.

**Recommendation:** Implement pagination with LIMIT and OFFSET.

---

## 9. CODE STYLE ISSUES

### 9.1 Inconsistent Error Handling
**Severity:** LOW  
**Impact:** Unpredictable error behavior  
**Files:** All files

**Problem:**
Some methods return null on error, others return false, others throw exceptions.

**Recommendation:** Standardize error handling strategy.

---

### 9.2 Magic Numbers
**Severity:** LOW  
**Impact:** Reduced code readability  
**Files:** Multiple view classes

**Example:** `AdminFrame.java` Line 115
```java
btnAddTrain.setPreferredSize(new Dimension(100, 30));  // ❌ Magic numbers
```

**Recommendation:** Extract to named constants.

---

### 9.3 Long Parameter Lists
**Severity:** LOW  
**Impact:** Reduced code readability  
**Files:** Model constructors

**Example:** `Booking.java` Line 40-43
```java
public Booking(String bookingId, int userId, int trainId, int routeId, 
               Date journeyDate, String passengerName, int passengerAge, 
               Gender passengerGender, String seatNumber, BookingStatus bookingStatus, 
               BigDecimal totalFare) {  // ❌ 11 parameters
```

**Recommendation:** Use Builder pattern for complex objects.

---

## 10. RECOMMENDATIONS PRIORITY

### IMMEDIATE (Fix within 1 week)
1. ✅ Fix password hashing (CRITICAL)
2. ✅ Fix resource leaks - add ResultSet to try-with-resources
3. ✅ Fix thread safety in DatabaseConnection
4. ✅ Fix UI layout bug in BookingFrame
5. ✅ Add transaction management to booking operations

### SHORT TERM (Fix within 1 month)
1. Implement connection pooling
2. Add comprehensive input validation
3. Implement proper logging (replace printStackTrace)
4. Add concurrency control for seat booking
5. Add unit tests for critical paths

### MEDIUM TERM (Fix within 3 months)
1. Refactor AdminFrame into smaller components
2. Implement dependency injection
3. Add caching layer
4. Add pagination for large result sets
5. Implement DTO pattern

### LONG TERM (Nice to have)
1. Consider migrating to Spring Boot
2. Add REST API layer
3. Implement Repository pattern
4. Add integration tests
5. Add performance monitoring

---

## 11. METRICS SUMMARY

| Metric | Value | Status |
|--------|-------|--------|
| Total Files | 38 | ✅ |
| Build Status | SUCCESS | ✅ |
| Critical Issues | 8 | ❌ |
| Medium Issues | 12 | ⚠️ |
| Low Issues | 7 | ⚠️ |
| Circular Dependencies | 0 | ✅ |
| High Complexity Files | 4 | ⚠️ |
| Test Coverage | ~0% | ❌ |
| Code Duplication | Medium | ⚠️ |

---

## 12. CONCLUSION

The Railway Booking System is **functionally complete** and **compiles successfully**, but has several **critical security and reliability issues** that must be addressed before production deployment.

**Strengths:**
- Clean layered architecture
- No circular dependencies
- Good separation of concerns
- Comprehensive database schema

**Weaknesses:**
- Critical security vulnerabilities (plain text passwords)
- Resource management issues
- Thread safety concerns
- Lack of testing
- Missing transaction management

**Overall Grade:** C+ (Functional but needs significant improvements)

**Recommended Action:** Address all IMMEDIATE priority items before any production deployment.

---

*End of Report*
