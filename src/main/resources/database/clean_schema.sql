-- Database schema for Railway Booking System
-- Create database
CREATE DATABASE IF NOT EXISTS railway_db;
USE railway_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    user_type ENUM('CUSTOMER', 'ADMIN') DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trains table
CREATE TABLE IF NOT EXISTS trains (
    train_id INT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(10) UNIQUE NOT NULL,
    train_name VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL,
    train_type ENUM('EXPRESS', 'PASSENGER', 'SUPERFAST') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
);

-- Stations table
CREATE TABLE IF NOT EXISTS stations (
    station_id INT AUTO_INCREMENT PRIMARY KEY,
    station_name VARCHAR(100) NOT NULL,
    station_code VARCHAR(10) UNIQUE NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL
);

-- Routes table
CREATE TABLE IF NOT EXISTS routes (
    route_id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    source_station_id INT NOT NULL,
    destination_station_id INT NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    distance_km DECIMAL(8,2) NOT NULL,
    fare DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE,
    FOREIGN KEY (source_station_id) REFERENCES stations(station_id) ON DELETE CASCADE,
    FOREIGN KEY (destination_station_id) REFERENCES stations(station_id) ON DELETE CASCADE
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id VARCHAR(20) PRIMARY KEY,
    user_id INT NOT NULL,
    train_id INT NOT NULL,
    route_id INT NOT NULL,
    journey_date DATE NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    passenger_gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    seat_number VARCHAR(10),
    booking_status ENUM('CONFIRMED', 'CANCELLED', 'WAITING') DEFAULT 'CONFIRMED',
    total_fare DECIMAL(10,2) NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CARD', 'UPI', 'CASH') NOT NULL,
    payment_status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL,
    transaction_id VARCHAR(50) UNIQUE,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

