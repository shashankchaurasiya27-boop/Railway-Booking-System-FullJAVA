-- Unified Database schema for Railway Booking System
-- Create database
CREATE DATABASE IF NOT EXISTS railway_booking;
USE railway_booking;

-- Users table (using schema.sql structure with id)
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

-- Stations table (from clean_schema.sql)
CREATE TABLE IF NOT EXISTS stations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    station_name VARCHAR(100) NOT NULL,
    station_code VARCHAR(10) UNIQUE NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trains table (hybrid approach - supports both simple and complex routes)
CREATE TABLE IF NOT EXISTS trains (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(20) UNIQUE NOT NULL,
    train_name VARCHAR(100) NOT NULL,
    train_type ENUM('EXPRESS', 'PASSENGER', 'SUPERFAST') NOT NULL,
    total_seats INT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Routes table (from clean_schema.sql but with id instead of route_id)
CREATE TABLE IF NOT EXISTS routes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    source_station_id INT NOT NULL,
    destination_station_id INT NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    distance_km DECIMAL(8,2) NOT NULL,
    fare DECIMAL(10,2) NOT NULL,
    available_seats INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    FOREIGN KEY (source_station_id) REFERENCES stations(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_station_id) REFERENCES stations(id) ON DELETE CASCADE
);

-- Bookings table (hybrid approach) with PNR support
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id VARCHAR(20) UNIQUE NOT NULL,
    pnr VARCHAR(20) UNIQUE,  -- Passenger Name Record for ticket tracking
    user_id INT NOT NULL,
    train_id INT NOT NULL,
    route_id INT NOT NULL,
    journey_date DATE NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    number_of_passengers INT NOT NULL DEFAULT 1,
    total_fare DECIMAL(10,2) NOT NULL,
    booking_status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'WAITING', 'RAC') DEFAULT 'PENDING',
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE
);

-- Index for fast PNR lookup
CREATE INDEX idx_bookings_pnr ON bookings(pnr);

-- Tickets table (individual passenger details)
CREATE TABLE IF NOT EXISTS tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    booking_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    passenger_gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    seat_number VARCHAR(10),
    ticket_status ENUM('ACTIVE', 'CANCELLED') DEFAULT 'ACTIVE',
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
    coach_type VARCHAR(20) NOT NULL,  -- Supports: GENERAL, SL, SL_AC, AC_3TIER, AC_2TIER, AC_1TIER, AC_CHAIR_CAR, EXECUTIVE_CHAIR_CAR
    total_seats INT NOT NULL,
    fare_multiplier DECIMAL(3,2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE KEY unique_train_coach (train_id, coach_type)
);

-- SeatAvailability table - real-time availability tracking per journey date
CREATE TABLE IF NOT EXISTS seat_availability (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    coach_type VARCHAR(20) NOT NULL,  -- Supports all coach types from Java enum
    journey_date DATE NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    booked_seats INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE KEY unique_availability (train_id, coach_type, journey_date)
);

-- Add coach_type to bookings table for tracking
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS coach_type VARCHAR(20) DEFAULT 'GENERAL';

-- Add coach_type to tickets table for tracking
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS coach_type VARCHAR(20) DEFAULT 'GENERAL';

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_stations_code ON stations(station_code);
CREATE INDEX idx_trains_number ON trains(train_number);
CREATE INDEX idx_routes_train ON routes(train_id);
CREATE INDEX idx_routes_stations ON routes(source_station_id, destination_station_id);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_train ON bookings(train_id);
CREATE INDEX idx_bookings_route ON bookings(route_id);
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