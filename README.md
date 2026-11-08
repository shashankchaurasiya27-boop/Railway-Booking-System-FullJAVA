<p align="center">
  <img src="docs/images/banner.svg" alt="Railway Booking System Banner" width="100%" />
</p>

<p align="center">
  <a href="#-tech-stack"><img src="https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" /></a>
  <a href="#-tech-stack"><img src="https://img.shields.io/badge/Spring%20Boot-3.2.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.2" /></a>
  <a href="#-tech-stack"><img src="https://img.shields.io/badge/GUI-Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java Swing" /></a>
  <a href="#-database-support"><img src="https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" /></a>
  <a href="#-database-support"><img src="https://img.shields.io/badge/PostgreSQL-12%2B-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" /></a>
  <a href="#-concurrency-engine"><img src="https://img.shields.io/badge/Concurrency-Virtual%20Threads-7952B3?style=for-the-badge&logo=fastapi&logoColor=white" alt="Virtual Threads" /></a>
  <a href="#-testing"><img src="https://img.shields.io/badge/Tests-18%20Passing-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5 Tests" /></a>
  <a href="#-license"><img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="MIT License" /></a>
</p>

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [3-Layer Concurrency Engine](#-3-layer-concurrency-engine)
- [Database & ER Diagram](#-database--er-diagram)
- [Default Test Credentials](#-default-test-credentials)
- [Quick Start Guide](#-quick-start-guide)
- [REST API Reference](#-rest-api-reference)
- [Project Structure](#-project-structure)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [Troubleshooting](#-troubleshooting)
- [License](#-license)

---

## 🚄 Overview

The **Railway Booking System** is an enterprise-grade, high-concurrency ticket reservation platform built in pure **Java 21** with a hybrid architectural model:

1. **Rich Desktop Client**: Interactive **Java Swing** graphical user interface featuring asynchronous background processing (`SwingWorker`) and non-freezing UI dialogs.
2. **Spring Boot REST Backend**: High-throughput microservice exposing RESTful endpoints for booking, train route search, PNR lookup, passenger management, and payment processing.
3. **High-Concurrency Engine**: Production-tested 3-layer concurrency control architecture guaranteeing **zero overbooking**, **deadlock-free seat reservations**, and automatic **RAC / Waiting List promotion chains**.

---

## ✨ Key Features

| Category | Capability | Highlight |
|---|---|---|
| 🎫 **Ticketing & Reservation** | Multi-class seat booking | Supports **GENERAL**, **Sleeper (SL)**, **3AC**, **2AC**, and **1AC** with dynamic pricing multipliers |
| 🛡️ **Concurrency Control** | 3-Layer Zero-Overbooking Engine | ReentrantLock fair ordering + `SERIALIZABLE` isolation + row-level `SELECT FOR UPDATE` |
| ⚡ **Java 21 Loom** | Virtual Thread Executor | High-throughput non-blocking I/O execution for asynchronous operations |
| 🔍 **PNR Tracking** | 10-Digit PNR System | Standard alphanumeric Passenger Name Record tracking with instant journey status lookup |
| 🔄 **Auto-Promotion Chain** | RAC & Waiting List Automation | Automatic cascading promotion (`WAITING` ➔ `RAC` ➔ `CONFIRMED`) upon ticket cancellations |
| 💳 **Payment Processing** | Multi-Method Ledger | Secure simulation for **CASH**, **CARD**, **UPI**, and **NET_BANKING** with transaction receipts |
| 🖥️ **Interactive Swing GUI** | Thread-Safe Desktop App | Dedicated frames for Login, Registration, Train Search, Booking, PNR Management, and Admin Panel |
| 🗄️ **Multi-Database Support** | Pluggable Persistence | Tested out-of-the-box with **MySQL 8.0+**, **PostgreSQL 12+**, and **H2** in-memory test profiles |
| 🔐 **Enterprise Security** | BCrypt Authentication | Military-grade 12-round BCrypt salted password hashing and role-based access control (`ADMIN` / `USER`) |

---

## 🏗️ System Architecture

<p align="center">
  <img src="docs/images/architecture.svg" alt="System Architecture Diagram" width="100%" />
</p>

### End-to-End Request Flow (Mermaid)

```mermaid
flowchart TD
    subgraph Clients["Presentation Layer"]
        GUI["🖥️ Java Swing Desktop GUI\n(LoginFrame, TrainSearchFrame, BookingFrame)"]
        REST_CLIENT["🌐 REST Clients / Web / Mobile\n(cURL, Postman, Frontend PWA)"]
    end

    subgraph Controllers["Gateway & Controllers"]
        SPRING_CTRL["⚡ Spring Boot REST Controllers\n(/api/auth, /api/trains, /api/bookings, /api/payments)"]
        SWING_CTRL["🎮 Swing MVC Controllers\n(LoginController, BookingController, TrainController)"]
    end

    subgraph ServiceLayer["Core Services & Concurrency Engine"]
        AUTH_MGR["🔐 AuthorizationManager & SessionManager"]
        BOOKING_SVC["🛡️ ConcurrentBookingService\n(3-Layer Locking & Fair Queue)"]
        SEAT_SVC["💺 SeatAvailabilityService\n(Real-Time Inventory Engine)"]
        PROMOTION_SVC["🔄 BookingLifecycleService\n(PNR Generator & RAC Promotion)"]
        VTHREAD["⚡ VirtualThreadExecutor\n(Java 21 Loom Carrier Threads)"]
    end

    subgraph DataAccess["Persistence Layer"]
        HIKARI["🌊 HikariCP Connection Pool\n(Min: 5, Max: 20 Connections)"]
        DAO["💾 DAO Layer\n(BookingDAO, TrainDAO, UserDAO, TicketDAO, PaymentDAO)"]
        DB[(🗄️ Database\nMySQL 8.0+ / PostgreSQL 12+ / H2)]
    end

    GUI --> SWING_CTRL
    REST_CLIENT --> SPRING_CTRL
    SWING_CTRL --> BOOKING_SVC
    SPRING_CTRL --> BOOKING_SVC
    BOOKING_SVC --> AUTH_MGR
    BOOKING_SVC --> SEAT_SVC
    BOOKING_SVC --> PROMOTION_SVC
    BOOKING_SVC --> VTHREAD
    SEAT_SVC --> DAO
    PROMOTION_SVC --> DAO
    DAO --> HIKARI
    HIKARI --> DB
```

---

## 🛡️ 3-Layer Concurrency Engine

To prevent race conditions, seat double-allocation, and deadlocks during tatkal-style traffic spikes, the system enforces a strict 3-layer protection protocol:

<p align="center">
  <img src="docs/images/concurrency.svg" alt="Concurrency Engine" width="100%" />
</p>

### 1. Application-Level Fair Lock
- Utilizes `ReentrantLock(true)` to guarantee First-In-First-Out (FIFO) request ordering.
- Prevents thread starvation under concurrent burst requests.

### 2. Transaction Isolation
- Runs transactions under JDBC `Connection.TRANSACTION_SERIALIZABLE`.
- Completely prevents dirty reads, non-repeatable reads, and phantom reads across concurrent transactions.

### 3. Row-Level Database Locking (`SELECT FOR UPDATE`)
- **Strict Deadlock-Prevention Order**:
  1. Lock row in `train_seats` table via `SELECT ... FOR UPDATE`
  2. Lock row in `seat_availability` table via `SELECT ... FOR UPDATE`
  3. Perform atomic decrement on `available_seats`
  4. Write `bookings` and `tickets` records with generated 10-digit PNR
  5. Commit transaction atomically

### RAC & Waiting List Auto-Promotion Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User as Passenger
    participant Svc as ConcurrentBookingService
    participant Lifecycle as BookingLifecycleService
    participant Pool as HikariCP (SERIALIZABLE)
    participant DB as Database (Row Locks)

    User->>Svc: cancelTicket(bookingId)
    Svc->>Pool: Begin SERIALIZABLE Transaction
    Svc->>DB: SELECT status FROM bookings WHERE booking_id = ? FOR UPDATE
    Svc->>DB: UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?
    Svc->>DB: UPDATE seat_availability SET available_seats = available_seats + 1
    Svc->>Lifecycle: checkAndPromoteWaitlist(trainId, coachType, date)
    Lifecycle->>DB: SELECT oldest RAC booking FOR UPDATE
    Lifecycle->>DB: Promote RAC ➔ CONFIRMED (Assign Seat)
    Lifecycle->>DB: SELECT oldest WAITING booking FOR UPDATE
    Lifecycle->>DB: Promote WAITING ➔ RAC
    Svc->>Pool: Commit Transaction
    Svc-->>User: Return "SUCCESS"
```

---

## 🗄️ Database & ER Diagram

The database schema is defined in [`src/main/resources/database/unified_schema.sql`](file:///src/main/resources/database/unified_schema.sql) and automatically verified/initialized upon startup by `DatabaseInitializer`.

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : places
    TRAINS ||--o{ ROUTES : operates_on
    TRAINS ||--o{ TRAIN_SEATS : configures
    TRAINS ||--o{ SEAT_AVAILABILITY : tracks
    STATIONS ||--o{ ROUTES : source_of
    STATIONS ||--o{ ROUTES : destination_of
    BOOKINGS ||--|{ TICKETS : contains
    BOOKINGS ||--o| PAYMENTS : settled_by
    TRAINS ||--o{ BOOKINGS : reserves

    USERS {
        int id PK
        string username UK
        string password "BCrypt Hash"
        string email UK
        string first_name
        string last_name
        string phone
        string role "USER | ADMIN"
    }

    TRAINS {
        int id PK
        string train_number UK
        string train_name
        string train_type "EXPRESS | PASSENGER | SUPERFAST"
        int total_seats
        string status "ACTIVE | INACTIVE"
    }

    STATIONS {
        int id PK
        string station_code UK
        string station_name
        string city
        string state
    }

    ROUTES {
        int id PK
        int train_id FK
        int source_station_id FK
        int destination_station_id FK
        time departure_time
        time arrival_time
        decimal distance_km
        decimal fare
        int available_seats
    }

    TRAIN_SEATS {
        int id PK
        int train_id FK
        string coach_type "SL | 3AC | 2AC | 1AC | GENERAL"
        int total_seats
        decimal fare_multiplier
    }

    SEAT_AVAILABILITY {
        int id PK
        int train_id FK
        string coach_type
        date journey_date
        int total_seats
        int available_seats
        int booked_seats
    }

    BOOKINGS {
        int id PK
        string booking_id UK
        string pnr UK "10-Digit PNR"
        int user_id FK
        int train_id FK
        int route_id FK
        date journey_date
        string coach_type
        decimal total_fare
        string booking_status "CONFIRMED | RAC | WAITING | CANCELLED"
    }

    TICKETS {
        int id PK
        string ticket_number UK
        int booking_id FK
        string passenger_name
        int passenger_age
        string passenger_gender
        string seat_number
        string coach_type
    }

    PAYMENTS {
        int id PK
        string payment_id UK
        int booking_id FK
        decimal amount
        string payment_method "CASH | CARD | UPI | NET_BANKING"
        string payment_status "SUCCESS | PENDING | FAILED | REFUNDED"
        string transaction_id
    }
```

---

## 🔑 Default Test Credentials

The database comes pre-seeded with test accounts upon initial startup:

| Account Role | Username | Password | Access Privileges |
|---|---|---|---|
| 👑 **Administrator** | `admin` | `admin123` | Full control: Train Master, Station Management, View All Bookings, Audit Logs |
| 👤 **Standard User** | `john_doe` | `password123` | Book Tickets, Search Routes, View My Bookings, Download Receipts, Cancel Bookings |

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21 (LTS)** or higher (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **MySQL 8.0+** (or PostgreSQL 12+) running on `localhost:3306`

### 1. One-Click Setup & Launch

```bash
# Clone the repository
git clone https://github.com/shashankchaurasiya27-boop/Railway-Booking-System-FullJAVA.git
cd Railway-Booking-System-FullJAVA

# Make launcher scripts executable
chmod +x run.sh setup.sh setup-database.sh

# Run the complete automated setup (checks Java, creates DB, compiles & packages)
./setup.sh
```

### 2. Launch the Application

```bash
# Launch GUI + REST API Server via runner script
./run.sh

# OR run the packaged executable JAR directly:
java -jar target/railway-booking-system-1.0.0.jar
```

The system will start up:
- 🖥️ **Swing GUI**: The Login window will appear automatically.
- ⚡ **REST API Server**: Active on `http://localhost:8080`.
- 🩺 **Health Check**: Available at `http://localhost:8080/api/health`.

### 3. Alternative Database Configuration (PostgreSQL)

To use PostgreSQL instead of MySQL:

```bash
# Set PostgreSQL environment credentials
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export DB_URL="jdbc:postgresql://localhost:5432/railway_booking"

# Run with postgres profile
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
# OR via JAR:
java -jar -Dspring.profiles.active=postgres target/railway-booking-system-1.0.0.jar
```

---

## 🌐 REST API Reference

All REST endpoints accept and return `application/json`.

### Authentication
| Method | Endpoint | Description | Request Payload |
|---|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user and issue session | `{"username": "john_doe", "password": "password123"}` |

### Train & Route Discovery
| Method | Endpoint | Description | Request Payload |
|---|---|---|---|
| `GET` | `/api/trains/stations` | Retrieve list of all available railway stations | _None_ |
| `POST` | `/api/trains/search` | Search trains by route and date | `{"sourceStation": "NDLS", "destinationStation": "BCT", "travelDate": "2026-12-01"}` |

### Bookings & Reservations
| Method | Endpoint | Description | Request Payload |
|---|---|---|---|
| `POST` | `/api/bookings` | Create ticket reservation | `{"userId": 1, "trainId": 101, "routeId": 1, "journeyDate": "2026-12-01", "coachType": "AC_3TIER", "passengerName": "Alex Smith", "passengerAge": 30}` |
| `GET` | `/api/bookings/user/{userId}` | List all reservations for a user | _None_ |
| `PUT` | `/api/bookings/{bookingId}/cancel` | Cancel booking and trigger auto-promotion | _None_ |

### Payments & Diagnostics
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments` | Process booking payment (`CASH`, `CARD`, `UPI`, `NET_BANKING`) |
| `GET` | `/api/payments/booking/{bookingId}` | Query payment transaction status |
| `GET` | `/api/health` | Service health status (`{"status": "UP"}`) |
| `GET` | `/api/health/detailed` | Database connection pool stats, memory usage, and active threads |

---

## 📁 Project Structure

```
Railway-Booking-System-FullJAVA/
├── .github/                       # GitHub workflow and metadata
├── docs/                          # Architecture & design documentation
│   ├── images/                    # Vector illustrations & architecture SVGs
│   │   ├── banner.svg             # Hero header graphic
│   │   ├── architecture.svg       # Full-stack system architecture
│   │   └── concurrency.svg        # 3-Layer concurrency model
│   ├── database-design.md         # Database ER specifications
│   └── user-manual.md             # End-user operations guide
├── src/
│   ├── main/
│   │   ├── java/com/railway/
│   │   │   ├── RailwayBookingApplication.java  # Spring Boot & Swing Application Main
│   │   │   ├── Main.java                       # Alternate Entry Point
│   │   │   ├── controller/                     # Swing MVC Controllers
│   │   │   ├── view/                           # Java Swing GUI Frames
│   │   │   │   ├── LoginFrame.java
│   │   │   │   ├── RegistrationFrame.java
│   │   │   │   ├── MainFrame.java
│   │   │   │   ├── TrainSearchFrame.java
│   │   │   │   ├── BookingFrame.java
│   │   │   │   ├── MyBookingsFrame.java
│   │   │   │   ├── ProfileFrame.java
│   │   │   │   └── AdminFrame.java
│   │   │   ├── util/                           # SwingWorker & Virtual Thread Utilities
│   │   │   └── booking/backend/                # Core Enterprise Engine
│   │   │       ├── api/
│   │   │       │   ├── controller/             # REST Endpoints (Auth, Trains, Bookings, Payments)
│   │   │       │   ├── dto/                    # Request & Response Transfer Objects
│   │   │       │   └── exception/              # Global Exception Handlers
│   │   │       ├── dao/                        # JDBC Data Access Objects
│   │   │       ├── model/                      # Domain Entities (User, Train, Booking, Ticket, etc.)
│   │   │       ├── service/                    # Business Logic (ConcurrentBookingService, etc.)
│   │   │       └── util/                       # DatabaseConnectionPool, PasswordHasher, PNRGenerator
│   │   └── resources/
│   │       ├── application.properties          # Base Configuration
│   │       ├── application-dev.properties      # MySQL Development Profile
│   │       ├── application-prod.properties     # Production Optimizations
│   │       ├── application-postgres.properties # PostgreSQL Profile
│   │       ├── application-test.properties     # H2 Testing Profile
│   │       ├── logback.xml                     # Logging Architecture
│   │       └── database/
│   │           ├── unified_schema.sql          # Master Database Schema
│   │           ├── test_data.sql               # Seed Data (Admin, Users, Trains, Stations)
│   │           └── schema-postgresql.sql       # PostgreSQL DDL
│   └── test/
│       ├── java/com/railway/service/           # Mockito 5 & JUnit 5 Unit Tests
│       │   ├── UserServiceTest.java
│       │   └── BookingServiceTest.java
│       └── resources/mockito-extensions/       # ByteBuddy & MockMaker Configuration
├── pom.xml                                     # Maven Dependencies & Build Lifecycle
├── run.sh                                      # Quick-Run Shell Script
├── setup.sh                                    # Full Automated Setup Script
├── setup-database.sh                           # Interactive Database Configurator
└── README.md                                   # Project Documentation
```

---

## 🧪 Testing & Quality Assurance

The project maintains 100% passing unit tests using **JUnit 5** and **Mockito 5** with zero database dependency required during test execution:

```bash
# Run all unit tests
mvn clean test
```

### Test Results Summary
```
[INFO] Running com.railway.service.UserServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.railway.service.BookingServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
```

---

## 🔧 Troubleshooting

| Symptom | Cause | Solution |
|---|---|---|
| `CommunicationsException: Communications link failure` | MySQL is not running on port 3306 | Run `brew services start mysql` (macOS) or `sudo systemctl start mysql` (Linux) |
| `Access denied for user 'root'@'localhost'` | Incorrect MySQL password | Set environment variable: `export DB_PASSWORD=your_password` or run `./setup-database.sh` |
| `HeadlessException` | Running in headless container without display | The Spring Boot backend runs headlessly, but Swing requires a display. Pass `-Djava.awt.headless=true` if running backend-only |
| `Java version mismatch` | Java version is earlier than Java 21 | Install Java 21 LTS: `brew install openjdk@21` or use SDKMAN (`sdk install java 21.0.2-tem`) |

---

## 📄 License

This project is licensed under the **MIT License** — feel free to use, modify, and distribute it for personal, educational, or commercial purposes.

<p align="center">
  <b>Built with ❤️ and Java 21</b> • Designed for High Scalability &amp; Reliability
</p>
