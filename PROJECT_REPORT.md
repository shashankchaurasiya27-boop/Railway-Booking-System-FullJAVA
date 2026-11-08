# Railway Booking System - Project Analysis Report

**Generated:** April 8, 2026  
**Project Path:** `/Users/shashankchaurasia/Downloads/railway-booking-system full java `

---

## 1. Executive Summary

The Railway Booking System is a Java-based desktop application with a hybrid architecture combining:
- **Spring Boot 3.2.1** backend with REST APIs
- **Swing GUI** for the desktop interface
- **MySQL 8.0+/PostgreSQL 12+** database support with connection pooling

The system provides a complete train ticket booking solution with user authentication, train search, booking management, and an admin dashboard.

---

## 2. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.2.1 |
| Build Tool | Maven | - |
| Database | MySQL 8.0+ (default), PostgreSQL 12+ (alt), H2 (test only) | - |
| Connection Pool | HikariCP | 5.1.0 |
| Security | BCrypt (jbcrypt) | 0.4 |
| GUI Framework | Java Swing | - |
| Logging | SLF4J + Logback | 2.0.7 / 1.4.8 |
| Testing | JUnit 5 + Mockito | 5.9.3 / 5.11.0 |

---

## 3. Project Statistics

| Metric | Count |
|--------|-------|
| Total Java Files | 71 |
| Total Lines of Code | ~9,379 |
| Configuration Files | 23 (properties, XML, SQL) |
| Model Classes | 8 |
| View/GUI Classes | 8 |
| REST Controllers | 5 |
| DAO Classes | 7 |
| Service Classes | 7 |
| DTO Classes | 8 |
| Exception Classes | 6 |
| Test Classes | 2 |

---

## 4. Architecture Overview

### 4.1 Hybrid Architecture
The application uses a dual-mode architecture:
- **Backend Mode:** Spring Boot REST API server on port 8080
- **GUI Mode:** Java Swing desktop interface (optional)

### 4.2 Package Structure
```
com.railway/
├── RailwayBookingApplication.java    # Main entry point
├── Main.java                         # Alternative entry
├── booking/backend/                  # Spring Boot backend
│   ├── api/
│   │   ├── controller/              # REST Controllers (5 classes)
│   │   ├── dto/request/             # Request DTOs (4 classes)
│   │   ├── dto/response/            # Response DTOs (4 classes)
│   │   └── exception/               # API Exceptions (5 classes)
│   ├── dao/                         # Data Access Objects (7 classes)
│   ├── model/                       # Domain Models (8 classes)
│   ├── service/                     # Business Logic (7 classes)
│   └── util/                        # Utilities (5 classes)
├── controller/                      # Legacy controllers
├── dao/                            # Legacy DAOs
├── model/                          # Legacy models
├── service/                        # Legacy services
├── util/                           # Legacy utilities
├── view/                           # Swing GUI (8 classes)
│   └── components/                  # Custom UI components
├── exception/                      # Legacy exceptions
└── test/                         # Test classes
```

---

## 5. Domain Models

### 5.1 Core Entities

| Entity | Description | Key Fields |
|--------|-------------|------------|
| `User` | System users (ADMIN, USER) | userId, username, password, email, userType |
| `Train` | Train information | trainId, trainNumber, trainName, totalSeats, trainType, status |
| `Booking` | Ticket bookings | bookingId, userId, trainId, routeId, bookingStatus |
| `Ticket` | Individual tickets | ticketId, bookingId, passengerName, seatNumber, fare |
| `Payment` | Payment transactions | paymentId, bookingId, amount, paymentStatus, paymentMethod |
| `Route` | Train routes | routeId, trainId, sourceStation, destinationStation |
| `Station` | Railway stations | stationId, stationCode, stationName, city |
| `Session` | User sessions | sessionId, userId, token, createdAt, expiresAt |

### 5.2 Enums
- **UserType:** USER, ADMIN
- **TrainType:** EXPRESS, PASSENGER, SUPERFAST
- **TrainStatus:** ACTIVE, INACTIVE
- **BookingStatus:** PENDING, CONFIRMED, CANCELLED
- **PaymentStatus:** PENDING, COMPLETED, FAILED, REFUNDED

---

## 6. API Endpoints

### 6.1 REST Controllers

| Controller | Base Path | Endpoints |
|------------|-----------|-----------|
| `AuthRestController` | `/api/auth` | POST /login, POST /logout, POST /register |
| `BookingRestController` | `/api/bookings` | GET /, POST /, GET /{id}, DELETE /{id} |
| `TrainRestController` | `/api/trains` | GET /search, GET /{id}, GET /routes/{id} |
| `PaymentRestController` | `/api/payments` | POST /process, GET /status/{id} |
| `HealthCheckController` | `/actuator/health` | GET /health |

### 6.2 DTOs

**Request DTOs:**
- `LoginRequest` - username, password
- `BookingRequest` - userId, trainId, routeId, passengers
- `PaymentRequest` - bookingId, amount, paymentMethod
- `TrainSearchRequest` - source, destination, date

**Response DTOs:**
- `BookingResponse` - booking details with tickets
- `PaymentResponse` - payment status and transaction info
- `TrainSearchResponse` - available trains with schedules
- `ErrorResponse` - error code, message, timestamp

---

## 7. GUI Components (Swing)

| Frame | Purpose |
|-------|---------|
| `LoginFrame` | User authentication screen |
| `RegistrationFrame` | New user registration |
| `MainFrame` | Main dashboard after login |
| `TrainSearchFrame` | Search and book trains |
| `BookingFrame` | Booking confirmation and payment |
| `MyBookingsFrame` | View booking history |
| `ProfileFrame` | User profile management |
| `AdminFrame` | Admin dashboard for train/user management |

---

## 8. Database Configuration

### 8.1 Multi-Environment Support (Updated)

| Profile | Database | Connection Pool | Usage |
|---------|----------|-----------------|-------|
| **dev** | MySQL 8.0+ | 5 connections | Development (default) |
| **postgres** | PostgreSQL 12+ | 10 connections | Alternative development |
| **prod** | MySQL/PostgreSQL | 20 connections | Production |
| **test** | H2 In-Memory | 2 connections | Unit testing only |

### 8.2 Schema Files
- `schema.sql` - MySQL database schema (AUTO_INCREMENT)
- `schema-postgresql.sql` - PostgreSQL database schema (SERIAL)
- `test_data.sql` - Sample data for testing
- `clean_schema.sql` - Schema cleanup script
- `unified_schema.sql` - Combined schema with test data

### 8.3 Configuration Files
- `application-dev.properties` - MySQL development configuration
- `application-postgres.properties` - PostgreSQL configuration
- `application-prod.properties` - Production configuration
- `application-test.properties` - H2 test configuration (MODE=MySQL)
- `config/database.properties` - Legacy connection pool config

---

## 9. Security Features

1. **Password Hashing:** BCrypt algorithm (cost factor 10)
2. **Session Management:** Token-based with expiration
3. **Input Validation:** Hibernate Validator integration
4. **Environment Variables:** Credentials externalized (no hardcoded secrets)
5. **SSL/TLS Ready:** Configuration support for production

---

## 10. Deployment Options

### 10.1 Local Development
```bash
mvn clean package -DskipTests
./run.sh
```

### 10.2 Environment Variables
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### 10.3 JAR Deployment
```bash
java -jar -Dspring.profiles.active=prod target/railway-booking-system-1.0.0.jar
```

---

## 11. Testing

### 11.1 Test Coverage
- `BookingServiceTest.java` - Service layer unit tests
- `UserServiceTest.java` - User service unit tests

### 11.2 Test Dependencies
- JUnit Jupiter 5.9.3
- Mockito Core 5.11.0
- Mockito JUnit Jupiter 5.11.0

---

## 12. Key Features

1. **User Management:** Registration, login, profile management
2. **Train Search:** By route or train name/number
3. **Booking System:** Seat selection, multi-passenger support
4. **Payment Processing:** Multiple payment methods
5. **Admin Dashboard:** Train and user management
6. **Session Security:** Secure token-based authentication
7. **Connection Pooling:** HikariCP for performance
8. **Multi-Database:** MySQL, PostgreSQL, H2 (test) support
9. **Multi-Profile:** Dev, postgres, test, prod configurations
10. **Seat Availability Engine:** Real-time seat tracking with coach types (Sleeper/AC/General)
    - Automatic seat allocation on booking
    - Seat release on cancellation
    - Row-level locking prevents overbooking
    - Fare multipliers per coach type

---

## 13. File Structure Summary

```
railway-booking-system/
├── src/
│   ├── main/
│   │   ├── java/com/railway/          # Application code
│   │   └── resources/
│   │       ├── application*.properties # Config files
│   │       ├── database/             # SQL scripts
│   │       └── logback.xml          # Logging config
│   └── test/java/                    # Unit tests
├── docs/                             # Documentation
├── logs/                            # Runtime logs
├── pom.xml                          # Maven configuration
├── README.md                        # Quick start guide
├── DEPLOYMENT.md                    # Deployment guide
├── .env.template                    # Environment template
└── run.sh                          # Startup script
```

---

## 14. Requirements

### 14.1 System Requirements
- **Java:** JDK 21 or higher
- **Maven:** 3.8+ recommended
- **MySQL:** 8.0+ (required for dev/prod)
- **PostgreSQL:** 12+ (alternative for dev/prod)
- **OS:** Cross-platform (Windows, macOS, Linux)

### 14.2 Default Test Accounts
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| User | john_doe | password123 |

---

## 15. Recommendations

1. **Security:** Enable SSL/TLS for production deployment
2. **Monitoring:** Configure actuator endpoints with authentication
3. **Logging:** Set up centralized logging for production
4. **Backup:** Implement database backup strategy
5. **Scaling:** Consider containerization (Docker) for deployment

---

## 16. Seat Availability Engine (NEW)

### 16.1 Overview
Real-time seat tracking system that prevents overbooking and manages seat allocation across different coach types.

---

## 17. PNR System (NEW) 🎟️

### 17.1 Overview
Industry-grade PNR (Passenger Name Record) generation system with:
- **Unique** non-colliding numbers
- **Human-readable** format
- **Trackable** for ticket lookup

### 17.2 PNR Format
```
PNR[TrainID(3)][Time(6)][Random(3)]
Example: PNR123456789012
```

### 17.3 Generation Strategy
| Component | Source | Purpose |
|-----------|--------|---------|
| Timestamp | `System.currentTimeMillis()` | Time-based uniqueness |
| Random | `SecureRandom` (100-999) | Collision resistance |
| Train ID | `trainId % 1000` | Context association |

### 17.4 Concurrency Safety
- **Collision Check Loop:** Up to 5 attempts to generate unique PNR
- **Transaction Context:** Check within booking transaction
- **Fallback:** Extra entropy with user component if needed

### 17.5 Key Methods
| Method | Purpose |
|--------|---------|
| `generatePNR(trainId)` | Generate basic PNR |
| `generatePNRWithUser(trainId, userId)` | Extra entropy version |
| `isPNRExists(pnr)` | Check uniqueness |
| `getBookingByPNR(pnr)` | Track ticket status |
| `trackBooking(pnr)` | Service-level tracking |

### 17.6 Return Format
```
SUCCESS:BookingID:PNR
Example: SUCCESS:RB123456789012:PNR123456789012
```

### 17.7 New Files
- `PNRGenerator.java` - PNR generation utility

### 17.8 Modified Files
- `schema.sql` - Added `pnr` column + index
- `schema-postgresql.sql` - Added `pnr` column + index
- `Booking.java` - Added `pnr` field
- `BookingDAO.java` - Added PNR methods
- `ConcurrentBookingService.java` - Integrated PNR generation

---

## 18. Booking Status Lifecycle (NEW) 🔁

### 18.1 Complete State Machine
```
PENDING → CONFIRMED → CANCELLED
        ↘   RAC   ↗
          ↘ WAITING
```

| State | Meaning |
|-------|---------|
| **PENDING** | Booking initiated, payment pending |
| **CONFIRMED** | Full seat allocated |
| **RAC** | Seat shared (half berth) |
| **WAITING** | No seat yet, in queue |
| **CANCELLED** | Terminal state |

### 18.2 Promotion Rules
```
Cancellation Flow:
IF CONFIRMED cancelled:
    RAC → CONFIRMED
    WAITING → RAC

IF RAC cancelled:
    WAITING → RAC
```

### 18.3 Database Schema Updates
```sql
status ENUM('PENDING', 'CONFIRMED', 'RAC', 'WAITING', 'CANCELLED')
priority INT DEFAULT 0  -- For FIFO ordering
```

### 18.4 BookingLifecycleService (CENTRAL PROMOTION ENGINE)
- **handlePromotion(trainId, journeyDate, coachType)**
  - Promotes RAC → CONFIRMED (if seats available)
  - Promotes WAITING → RAC (or direct to CONFIRMED)
  - Chain promotion until no more seats or candidates

- **determineInitialStatus(availableSeats, racCount, maxRAC)**
  - Returns CONFIRMED, RAC, or WAITING based on availability

- **assignPriority()**
  - Timestamp-based priority for FIFO ordering

### 18.5 Concurrency-Safe Promotions
- Row-level locking (`FOR UPDATE`) on candidate bookings
- Transaction isolation (`SERIALIZABLE`)
- Ordered by priority ASC, created_at ASC (fair FIFO)

### 18.6 New Files
- `BookingLifecycleService.java` - Promotion engine

### 18.7 Modified Files
- `schema.sql` - Added RAC, WAITING to status enum + priority column
- `schema-postgresql.sql` - Same updates
- `Booking.java` - Added RAC to BookingStatus enum + priority field
- `BookingDAO.java` - Added promotion queries with FOR UPDATE
- `ConcurrentBookingService.java` - Integrated priority assignment + triggers promotion on cancellation

---

## 19. Concurrency Control Engine (NEW) 🔒

### 19.1 Problem Statement
**Before:** Multiple users checking same seat → Both book simultaneously → **OVERBOOKING** ❌

**After:** 3-Layer protection ensures only one user gets the seat

### 19.2 Three-Layer Protection Strategy

| Layer | Mechanism | Purpose |
|-------|-----------|---------|
| **1. Application Lock** | `ReentrantLock` | Orders concurrent requests |
| **2. Transaction Isolation** | `SERIALIZABLE` | Prevents dirty/phantom reads |
| **3. Row-Level Locking** | `SELECT FOR UPDATE` | Locks specific seat row |
| **4. Atomic Update** | Row count verification | Prevents double booking |

### 19.3 Deadlock Prevention Ordering
```
1. Lock train_seats row (coach configuration)
2. Lock seat_availability row (current availability)
3. Atomic UPDATE (with available_seats >= 1 check)
4. Create booking record
5. COMMIT
```
**CRITICAL:** Never change this order to prevent deadlocks

### 19.4 Key Components

#### ConcurrentBookingService.java
- **Thread-safe booking** with `ReentrantLock`
- **SERIALIZABLE transaction isolation**
- **Atomic seat allocation** with row count verification
- **Automatic rollback** on booking failure

#### DatabaseConnectionPool.java (Updated)
```java
getTransactionConnection()          // autoCommit = false
getConnectionWithIsolation(level)   // With isolation level
```

### 19.5 Booking Flow (Thread-Safe)
```
User A checks seats → available = 1
User B checks seats → available = 1

User A locks → checks again → available = 1
User A atomic update → rowsUpdated = 1 → BOOKED ✅

User B locks → checks again → available = 0
User B atomic update → rowsUpdated = 0 → WAITING ⏳
```

### 19.6 Return Codes
| Result | Meaning |
|--------|---------|
| `SUCCESS:RB123456` | Booking confirmed with ID |
| `WAITING:No seats` | Added to waiting list |
| `FAILED:message` | Error with details |

### 19.7 New Files Created
- `ConcurrentBookingService.java` - Production-grade thread-safe booking
- `TrainSeatService.java` - Coach configuration service

### 19.8 Modified Files
- `DatabaseConnectionPool.java` - Added transaction support methods

### 19.9 Testing Multi-Thread Scenarios
```java
Runnable r = () -> {
    System.out.println(service.bookTicket(booking, CoachType.AC));
};
new Thread(r).start();  // Thread 1
new Thread(r).start();  // Thread 2

// Expected: Only 1 gets SUCCESS, other gets WAITING
```

---

## 20. Seat Availability Engine (continued)

### 20.1 Coach Types
| Type | Code | Fare Multiplier | Description |
|------|------|-----------------|-------------|
| General | G | 1.0x | Standard seating |
| Sleeper | S | 1.5x | Overnight sleeping berths |
| AC | A | 2.5x | Air-conditioned comfort |

### 20.2 Database Tables
- **train_seats:** Defines seat configuration per coach type per train
- **seat_availability:** Real-time tracking per train/coach/date combination
- **bookings:** Now includes coach_type column
- **tickets:** Now includes coach_type column

### 20.3 Key Components
| Component | Purpose |
|-----------|---------|
| `TrainSeat` | Model for coach configuration |
| `SeatAvailability` | Model for real-time availability |
| `TrainSeatDAO` | CRUD for train seat configuration |
| `SeatAvailabilityDAO` | Real-time seat tracking with `SELECT FOR UPDATE` locking |
| `SeatAvailabilityService` | Business logic for allocation/release |
| Updated `BookingService` | Integrated seat allocation on booking/cancellation |

### 20.4 Seat Allocation Logic
1. **Booking:** Check availability → Lock row → Decrease available_seats → Create booking
2. **Cancellation:** Release seats → Increase available_seats → Mark booking cancelled
3. **Thread Safety:** Row-level database locking prevents race conditions

### 20.5 New Files Created
- `src/main/java/com/railway/booking/backend/model/TrainSeat.java`
- `src/main/java/com/railway/booking/backend/model/SeatAvailability.java`
- `src/main/java/com/railway/booking/backend/dao/TrainSeatDAO.java`
- `src/main/java/com/railway/booking/backend/dao/SeatAvailabilityDAO.java`
- `src/main/java/com/railway/booking/backend/service/SeatAvailabilityService.java`

### 20.6 Modified Files
- `src/main/java/com/railway/booking/backend/model/Booking.java` - Added coach_type field
- `src/main/java/com/railway/booking/backend/dao/BookingDAO.java` - Added coach_type support
- `src/main/java/com/railway/booking/backend/service/BookingService.java` - Integrated seat allocation
- `src/main/resources/database/schema.sql` - Added seat tracking tables
- `src/main/resources/database/schema-postgresql.sql` - Added seat tracking tables

---

## 21. Coach Type-Based Booking (NEW) 🚆

### 21.1 Overview
User can now select specific coach types when booking tickets:
- **GENERAL** - General Class (unreserved/reserved seating)
- **SL** - Sleeper Class
- **AC_3TIER** - 3AC (3 Tier Air-Conditioned)
- **AC_2TIER** - 2AC (2 Tier Air-Conditioned)
- **AC_1TIER** - 1AC (1 Tier Air-Conditioned - Premium)
- **AC_CHAIR_CAR** - AC Chair Car
- **EXECUTIVE_CHAIR_CAR** - Executive Chair Car

### 21.2 Coach Type ENUM
```java
public enum CoachType {
    GENERAL("General", "GEN", 0.8),
    SL("Sleeper", "SL", 1.0),
    AC_3TIER("3AC - 3 Tier", "3AC", 1.5),
    AC_2TIER("2AC - 2 Tier", "2AC", 2.0),
    AC_1TIER("1AC - 1 Tier", "1AC", 2.5),
    AC_CHAIR_CAR("AC Chair Car", "CC", 1.2),
    EXECUTIVE_CHAIR_CAR("Executive Chair Car", "EC", 2.2)
}
```

| Coach Type | Short Code | Fare Multiplier | Description |
|------------|------------|-----------------|-------------|
| **GENERAL** | GEN | 0.8x | Budget seating |
| **SL** | SL | 1.0x | Sleeper berths |
| **AC_3TIER** | 3AC | 1.5x | 3-tier AC |
| **AC_2TIER** | 2AC | 2.0x | 2-tier AC |
| **AC_1TIER** | 1AC | 2.5x | Premium 1st class AC |
| **AC_CHAIR_CAR** | CC | 1.2x | Seated AC |
| **EXECUTIVE_CHAIR_CAR** | EC | 2.2x | Premium seated |

### 21.3 Database Schema Updates
```sql
-- MySQL
ENUM('GENERAL', 'SL', 'SL_AC', 'AC_3TIER', 'AC_2TIER', 'AC_1TIER', 'AC_CHAIR_CAR', 'EXECUTIVE_CHAIR_CAR', 'SLEEPER', 'AC')

-- PostgreSQL
VARCHAR(20) CHECK (coach_type IN ('GENERAL', 'SL', 'AC_3TIER', 'AC_2TIER', 'AC_1TIER', ...))
```

### 21.4 UI Updates

#### BookingFrame.java
- **Coach Type Dropdown:** Extended with detailed tier options
  ```java
  JComboBox<>(new String[]{
      "GENERAL - General Class",
      "SL - Sleeper",
      "AC_3TIER - 3AC (3 Tier)",
      "AC_2TIER - 2AC (2 Tier)",
      "AC_1TIER - 1AC (1 Tier)",
      ...
  })
  ```
- **Real-time Availability Display:** Shows seats available for selected coach type
- **Dynamic Fare Calculation:** Base fare × coach multiplier

#### TrainSearchFrame.java
- **Updated Table Columns:** Now shows availability by coach type
  ```
  | Route ID | Train | Source | Dest | Dep | Arr | Fare | GEN | SL | 3AC | 2AC | 1AC |
  ```
- **Coach Type Availability:** Fetches and displays real-time seat count per coach type

### 21.5 Key Methods Added

| Method | Purpose |
|--------|---------|
| `CoachType.fromString()` | Parse coach type from string/code |
| `getCoachTypeAvailabilitySummary()` | Format availability for display |
| `getAvailableCoachTypes()` | Get all coach types with availability |
| `getCoachTypeAvailability()` | Get per-coach availability for search results |
| `updateAvailabilityDisplay()` | Real-time UI update on selection |

### 21.6 Modified Files
- `TrainSeat.java` - Expanded CoachType enum with detailed tiers
- `schema.sql` - Updated ENUM with all coach types
- `schema-postgresql.sql` - Updated CHECK constraints
- `BookingFrame.java` - Coach type dropdown + availability display
- `TrainSearchFrame.java` - Coach type columns in results table
- `SeatAvailabilityService.java` - Summary methods for UI

### 21.7 Test Cases
```
✅ User selects SL → gets sleeper berth only
✅ User selects 3AC → gets 3-tier AC coach only
✅ User selects 1AC → gets premium 1st class AC
❌ No seat in 1AC → returns WAITLIST for that coach type
✅ Fare calculated correctly per coach type multiplier
```

---

**Report End**
