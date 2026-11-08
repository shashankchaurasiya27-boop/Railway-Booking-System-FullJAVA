-- PostgreSQL Database schema for Railway Booking System
-- Compatible with PostgreSQL 12+

-- Create database (run separately as superuser)
-- CREATE DATABASE railway_booking;

-- Connect to database
\c railway_booking;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    role VARCHAR(10) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trains table
CREATE TABLE IF NOT EXISTS trains (
    id SERIAL PRIMARY KEY,
    train_number VARCHAR(20) UNIQUE NOT NULL,
    train_name VARCHAR(100) NOT NULL,
    source_station VARCHAR(100) NOT NULL,
    destination_station VARCHAR(100) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    fare_per_seat DECIMAL(10,2) NOT NULL,
    status VARCHAR(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_trains_updated_at BEFORE UPDATE ON trains
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Bookings table with full lifecycle support
CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    booking_id VARCHAR(20) UNIQUE NOT NULL,
    pnr VARCHAR(20) UNIQUE,
    user_id INTEGER NOT NULL,
    train_id INTEGER NOT NULL,
    booking_date DATE NOT NULL,
    journey_date DATE NOT NULL,
    number_of_seats INTEGER NOT NULL,
    total_fare DECIMAL(10,2) NOT NULL,
    status VARCHAR(15) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'RAC', 'WAITING', 'CANCELLED')),
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE
);

-- Index for fast status-based queries
CREATE INDEX idx_bookings_status ON bookings(train_id, journey_date, status, priority);

-- Index for fast PNR lookup
CREATE INDEX idx_bookings_pnr ON bookings(pnr);

CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Tickets table
CREATE TABLE IF NOT EXISTS tickets (
    id SERIAL PRIMARY KEY,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    booking_id INTEGER NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INTEGER NOT NULL,
    passenger_gender VARCHAR(10) CHECK (passenger_gender IN ('MALE', 'FEMALE', 'OTHER')),
    seat_number VARCHAR(10),
    status VARCHAR(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TRIGGER update_tickets_updated_at BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    payment_id VARCHAR(20) UNIQUE NOT NULL,
    booking_id INTEGER NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) CHECK (payment_method IN ('CASH', 'CARD', 'UPI', 'NET_BANKING')),
    payment_status VARCHAR(15) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- =====================================================
-- SEAT AVAILABILITY ENGINE TABLES
-- =====================================================

-- TrainSeats table - defines seat configuration per coach type
CREATE TABLE IF NOT EXISTS train_seats (
    id SERIAL PRIMARY KEY,
    train_id INTEGER NOT NULL,
    coach_type VARCHAR(20) CHECK (coach_type IN ('GENERAL', 'SL', 'SL_AC', 'AC_3TIER', 'AC_2TIER', 'AC_1TIER', 'AC_CHAIR_CAR', 'EXECUTIVE_CHAIR_CAR', 'SLEEPER', 'AC')) NOT NULL,
    total_seats INTEGER NOT NULL,
    fare_multiplier DECIMAL(3,2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE (train_id, coach_type)
);

CREATE TRIGGER update_train_seats_updated_at BEFORE UPDATE ON train_seats
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- SeatAvailability table - real-time availability tracking
CREATE TABLE IF NOT EXISTS seat_availability (
    id SERIAL PRIMARY KEY,
    train_id INTEGER NOT NULL,
    coach_type VARCHAR(20) CHECK (coach_type IN ('GENERAL', 'SL', 'SL_AC', 'AC_3TIER', 'AC_2TIER', 'AC_1TIER', 'AC_CHAIR_CAR', 'EXECUTIVE_CHAIR_CAR', 'SLEEPER', 'AC')) NOT NULL,
    journey_date DATE NOT NULL,
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    booked_seats INTEGER DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE (train_id, coach_type, journey_date)
);

-- Add coach_type to existing tables if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='bookings' AND column_name='coach_type') THEN
        ALTER TABLE bookings ADD COLUMN coach_type VARCHAR(20) DEFAULT 'GENERAL';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='tickets' AND column_name='coach_type') THEN
        ALTER TABLE tickets ADD COLUMN coach_type VARCHAR(20) DEFAULT 'GENERAL';
    END IF;
END $$;

-- Insert sample data
INSERT INTO users (username, password, email, first_name, last_name, phone, role) VALUES
('admin', 'admin123', 'admin@railway.com', 'Admin', 'User', '1234567890', 'ADMIN'),
('john_doe', 'password123', 'john@example.com', 'John', 'Doe', '9876543210', 'USER'),
('jane_smith', 'password456', 'jane@example.com', 'Jane', 'Smith', '9876543211', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO trains (train_number, train_name, source_station, destination_station, departure_time, arrival_time, total_seats, available_seats, fare_per_seat) VALUES
('12345', 'Express Train', 'Mumbai', 'Delhi', '08:00:00', '20:00:00', 100, 100, 500.00),
('12346', 'Superfast Train', 'Delhi', 'Bangalore', '10:00:00', '22:00:00', 150, 150, 800.00),
('12347', 'Local Train', 'Chennai', 'Hyderabad', '06:00:00', '12:00:00', 200, 200, 200.00)
ON CONFLICT (train_number) DO NOTHING;

-- Insert train seat configuration with detailed coach types
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
(1, 'GENERAL', 20, 0.80),
(1, 'SL', 40, 1.00),
(1, 'AC_3TIER', 20, 1.50),
(1, 'AC_2TIER', 10, 2.00),
(1, 'AC_1TIER', 10, 2.50),
(2, 'GENERAL', 30, 0.80),
(2, 'SL', 50, 1.00),
(2, 'AC_3TIER', 30, 1.50),
(2, 'AC_2TIER', 20, 2.00),
(2, 'AC_1TIER', 20, 2.50),
(3, 'GENERAL', 40, 0.80),
(3, 'SL', 60, 1.00),
(3, 'AC_3TIER', 40, 1.50),
(3, 'AC_2TIER', 30, 2.00),
(3, 'AC_1TIER', 30, 2.50)
ON CONFLICT (train_id, coach_type) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_trains_number ON trains(train_number);
CREATE INDEX IF NOT EXISTS idx_trains_route ON trains(source_station, destination_station);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_train ON bookings(train_id);
CREATE INDEX IF NOT EXISTS idx_bookings_date ON bookings(journey_date);
CREATE INDEX IF NOT EXISTS idx_bookings_coach ON bookings(coach_type);
CREATE INDEX IF NOT EXISTS idx_tickets_booking ON tickets(booking_id);
CREATE INDEX IF NOT EXISTS idx_tickets_coach ON tickets(coach_type);
CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_train_seats_train ON train_seats(train_id);
CREATE INDEX IF NOT EXISTS idx_train_seats_type ON train_seats(coach_type);
CREATE INDEX IF NOT EXISTS idx_seat_availability_train ON seat_availability(train_id);
CREATE INDEX IF NOT EXISTS idx_seat_availability_date ON seat_availability(journey_date);
CREATE INDEX IF NOT EXISTS idx_seat_availability_type ON seat_availability(coach_type);
CREATE INDEX IF NOT EXISTS idx_seat_availability_lookup ON seat_availability(train_id, coach_type, journey_date);
