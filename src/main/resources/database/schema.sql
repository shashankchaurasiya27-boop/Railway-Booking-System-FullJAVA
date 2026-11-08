-- Database schema for Railway Booking System
-- Create database
CREATE DATABASE IF NOT EXISTS railway_booking;
USE railway_booking;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Trains table
CREATE TABLE IF NOT EXISTS trains (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(20) UNIQUE NOT NULL,
    train_name VARCHAR(100) NOT NULL,
    source_station VARCHAR(100) NOT NULL,
    destination_station VARCHAR(100) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    fare_per_seat DECIMAL(10,2) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bookings table with full lifecycle support
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id VARCHAR(20) UNIQUE NOT NULL,
    pnr VARCHAR(20) UNIQUE,
    user_id INT NOT NULL,
    train_id INT NOT NULL,
    booking_date DATE NOT NULL,
    journey_date DATE NOT NULL,
    number_of_seats INT NOT NULL,
    total_fare DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'RAC', 'WAITING', 'CANCELLED') DEFAULT 'PENDING',
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE
);

-- Index for fast status-based queries
CREATE INDEX idx_bookings_status ON bookings(train_id, journey_date, status, priority);

-- Index for fast PNR lookup
CREATE INDEX idx_bookings_pnr ON bookings(pnr);

-- Tickets table
CREATE TABLE IF NOT EXISTS tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    booking_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    passenger_gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    seat_number VARCHAR(10),
    status ENUM('ACTIVE', 'CANCELLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(20) UNIQUE NOT NULL,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'UPI', 'NET_BANKING') NOT NULL,
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- =====================================================
-- SEAT AVAILABILITY ENGINE TABLES
-- =====================================================

-- TrainSeats table - defines seat configuration per coach type
-- Uses VARCHAR for coach_type to support all Java enum values (SL, AC_3TIER, GENERAL, etc.)
CREATE TABLE IF NOT EXISTS train_seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    coach_type VARCHAR(20) NOT NULL,
    total_seats INT NOT NULL,
    fare_multiplier DECIMAL(3,2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE KEY unique_train_coach (train_id, coach_type)
);

-- SeatAvailability table - real-time availability tracking
CREATE TABLE IF NOT EXISTS seat_availability (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    coach_type VARCHAR(20) NOT NULL,
    journey_date DATE NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    booked_seats INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE KEY unique_availability (train_id, coach_type, journey_date)
);

-- Update Bookings table to track coach type (MySQL compatible)
ALTER TABLE bookings ADD COLUMN coach_type VARCHAR(20) DEFAULT 'GENERAL';

-- Update Tickets table to track coach type and seat (MySQL compatible)
ALTER TABLE tickets ADD COLUMN coach_type VARCHAR(20) DEFAULT 'GENERAL';

-- Insert sample data
INSERT INTO users (username, password, email, first_name, last_name, phone, role) VALUES
('admin', 'admin123', 'admin@railway.com', 'Admin', 'User', '1234567890', 'ADMIN'),
('john_doe', 'password123', 'john@example.com', 'John', 'Doe', '9876543210', 'USER'),
('jane_smith', 'password456', 'jane@example.com', 'Jane', 'Smith', '9876543211', 'USER');

INSERT INTO trains (train_number, train_name, source_station, destination_station, departure_time, arrival_time, total_seats, available_seats, fare_per_seat) VALUES
('12345', 'Express Train', 'Mumbai', 'Delhi', '08:00:00', '20:00:00', 100, 100, 500.00),
('12346', 'Superfast Train', 'Delhi', 'Bangalore', '10:00:00', '22:00:00', 150, 150, 800.00),
('12347', 'Local Train', 'Chennai', 'Hyderabad', '06:00:00', '12:00:00', 200, 200, 200.00);

-- Insert train seat configuration with proper coach types and fare multipliers
-- Fare multipliers: GENERAL=0.8, SL=1.0, AC_3TIER=1.5, AC_2TIER=2.0, AC_1TIER=2.5
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
-- Train 1: Express Train (Mumbai-Delhi, Base fare: 500, 105 total)
(1, 'GENERAL', 20, 0.80),
(1, 'SL', 40, 1.00),
(1, 'AC_3TIER', 20, 1.50),
(1, 'AC_2TIER', 15, 2.00),
(1, 'AC_1TIER', 5, 2.50),
-- Train 2: Superfast Train (Delhi-Bangalore, Base fare: 800, 160 total)
(2, 'GENERAL', 30, 0.80),
(2, 'SL', 60, 1.00),
(2, 'AC_3TIER', 30, 1.50),
(2, 'AC_2TIER', 20, 2.00),
(2, 'AC_1TIER', 10, 2.50),
-- Train 3: Local Train (Chennai-Hyderabad, Base fare: 200, 200 total)
(3, 'GENERAL', 80, 0.80),
(3, 'SL', 70, 1.00),
(3, 'AC_3TIER', 30, 1.50),
(3, 'AC_2TIER', 15, 2.00),
(3, 'AC_1TIER', 5, 2.50),
-- Train 4: Kolkata-Delhi Express (120 total)
(4, 'GENERAL', 40, 0.80),
(4, 'SL', 40, 1.00),
(4, 'AC_3TIER', 30, 1.50),
(4, 'AC_2TIER', 15, 2.00),
(4, 'AC_1TIER', 5, 2.50),
-- Train 5: Mumbai-Pune Local (300 total)
(5, 'GENERAL', 180, 0.80),
(5, 'SL', 100, 1.00),
(5, 'AC_3TIER', 25, 1.50),
(5, 'AC_2TIER', 10, 2.00),
(5, 'AC_1TIER', 5, 2.50),
-- Train 6: Delhi-Ahmedabad Superfast (80 total)
(6, 'GENERAL', 20, 0.80),
(6, 'SL', 35, 1.00),
(6, 'AC_3TIER', 15, 1.50),
(6, 'AC_2TIER', 10, 2.00),
(6, 'AC_1TIER', 5, 2.50);

-- =====================================================
-- AUDIT LOGGING TABLE
-- =====================================================

-- BookingLogs table - audit trail for all booking status changes
CREATE TABLE IF NOT EXISTS booking_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id VARCHAR(20) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    message TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Index for fast log retrieval by booking
CREATE INDEX idx_booking_logs_booking ON booking_logs(booking_id);
CREATE INDEX idx_booking_logs_timestamp ON booking_logs(timestamp);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_trains_number ON trains(train_number);
CREATE INDEX idx_trains_route ON trains(source_station, destination_station);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_train ON bookings(train_id);
CREATE INDEX idx_bookings_date ON bookings(journey_date);
CREATE INDEX idx_bookings_coach ON bookings(coach_type);
CREATE INDEX idx_tickets_booking ON tickets(booking_id);
CREATE INDEX idx_tickets_coach ON tickets(coach_type);
CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_train_seats_train ON train_seats(train_id);
CREATE INDEX idx_train_seats_type ON train_seats(coach_type);
CREATE INDEX idx_seat_availability_train ON seat_availability(train_id);
CREATE INDEX idx_seat_availability_date ON seat_availability(journey_date);
CREATE INDEX idx_seat_availability_type ON seat_availability(coach_type);
CREATE INDEX idx_seat_availability_lookup ON seat_availability(train_id, coach_type, journey_date);
