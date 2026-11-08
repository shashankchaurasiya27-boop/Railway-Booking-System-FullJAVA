-- Test data for Railway Booking System (Unified Schema)
-- Use this after running unified_schema.sql

USE railway_booking;

-- Insert sample stations
INSERT INTO stations (station_name, station_code, city, state) VALUES
('Mumbai Central', 'MMCT', 'Mumbai', 'Maharashtra'),
('New Delhi', 'NDLS', 'Delhi', 'Delhi'),
('Bangalore City', 'SBC', 'Bangalore', 'Karnataka'),
('Chennai Central', 'MAS', 'Chennai', 'Tamil Nadu'),
('Hyderabad Deccan', 'HYB', 'Hyderabad', 'Telangana'),
('Kolkata Howrah', 'HWH', 'Kolkata', 'West Bengal'),
('Pune Junction', 'PUNE', 'Pune', 'Maharashtra'),
('Ahmedabad Junction', 'ADI', 'Ahmedabad', 'Gujarat');

-- Insert sample trains
INSERT INTO trains (train_number, train_name, train_type, total_seats, status) VALUES
('12345', 'Mumbai-Delhi Express', 'EXPRESS', 100, 'ACTIVE'),
('12346', 'Delhi-Bangalore Superfast', 'SUPERFAST', 150, 'ACTIVE'),
('12347', 'Chennai-Hyderabad Passenger', 'PASSENGER', 200, 'ACTIVE'),
('12348', 'Kolkata-Delhi Express', 'EXPRESS', 120, 'ACTIVE'),
('12349', 'Mumbai-Pune Local', 'PASSENGER', 300, 'ACTIVE'),
('12350', 'Delhi-Ahmedabad Superfast', 'SUPERFAST', 80, 'ACTIVE');

-- Insert sample routes
INSERT INTO routes (train_id, source_station_id, destination_station_id, departure_time, arrival_time, distance_km, fare, available_seats) VALUES
(1, 1, 2, '08:00:00', '20:00:00', 1384.0, 500.00, 100),
(2, 2, 3, '10:00:00', '22:00:00', 2160.0, 800.00, 150),
(3, 4, 5, '06:00:00', '12:00:00', 715.0, 200.00, 200),
(4, 6, 2, '14:00:00', '08:00:00', 1447.0, 600.00, 120),
(5, 1, 7, '07:30:00', '10:30:00', 192.0, 50.00, 300),
(6, 2, 8, '16:00:00', '06:00:00', 928.0, 400.00, 80);

-- Insert sample users (password is plain text for testing - in production use hashed passwords)
INSERT INTO users (username, password, email, first_name, last_name, phone, address, role) VALUES
('admin', 'admin123', 'admin@railway.com', 'Admin', 'User', '1234567890', 'Admin Office, Railway Station', 'ADMIN'),
('john_doe', 'password123', 'john@example.com', 'John', 'Doe', '9876543210', '123 Main St, Mumbai', 'USER'),
('jane_smith', 'password456', 'jane@example.com', 'Jane', 'Smith', '9876543211', '456 Park Ave, Delhi', 'USER'),
('mike_wilson', 'password789', 'mike@example.com', 'Mike', 'Wilson', '9876543212', '789 Oak St, Bangalore', 'USER'),
('sarah_jones', 'password101', 'sarah@example.com', 'Sarah', 'Jones', '9876543213', '321 Pine St, Chennai', 'USER');

-- Insert sample bookings
INSERT INTO bookings (booking_id, user_id, train_id, journey_date, number_of_seats, total_fare, status) VALUES
('RB20241201120000', 2, 1, '2024-12-15', 2, 1000.00, 'CONFIRMED'),
('RB20241201120001', 3, 2, '2024-12-16', 1, 800.00, 'CONFIRMED'),
('RB20241201120002', 4, 3, '2024-12-17', 3, 600.00, 'PENDING'),
('RB20241201120003', 5, 4, '2024-12-18', 1, 600.00, 'CONFIRMED');

-- Insert sample tickets
INSERT INTO tickets (ticket_number, booking_id, passenger_name, passenger_age, passenger_gender, seat_number, ticket_status) VALUES
('T20241201120000', 1, 'John Doe', 35, 'MALE', 'A1', 'ACTIVE'),
('T20241201120001', 1, 'Jane Doe', 32, 'FEMALE', 'A2', 'ACTIVE'),
('T20241201120002', 2, 'Jane Smith', 28, 'FEMALE', 'B1', 'ACTIVE'),
('T20241201120003', 3, 'Mike Wilson', 40, 'MALE', 'C1', 'ACTIVE'),
('T20241201120004', 3, 'Lisa Wilson', 38, 'FEMALE', 'C2', 'ACTIVE'),
('T20241201120005', 3, 'Tom Wilson', 12, 'MALE', 'C3', 'ACTIVE'),
('T20241201120006', 4, 'Sarah Jones', 29, 'FEMALE', 'D1', 'ACTIVE');

-- Insert sample payments
INSERT INTO payments (payment_id, booking_id, amount, payment_method, payment_status, transaction_id) VALUES
('PAY20241201120000', 1, 1000.00, 'CARD', 'SUCCESS', 'TXN123456789'),
('PAY20241201120001', 2, 800.00, 'UPI', 'SUCCESS', 'TXN123456790'),
('PAY20241201120002', 3, 600.00, 'CARD', 'PENDING', 'TXN123456791'),
('PAY20241201120003', 4, 600.00, 'NET_BANKING', 'SUCCESS', 'TXN123456792');

-- =====================================================
-- SEAT AVAILABILITY ENGINE - Train Seat Configuration
-- =====================================================

-- Insert train seat configuration (5 coach types per train: GENERAL, SL, AC_3TIER, AC_2TIER, AC_1TIER)
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
-- Train 1: Mumbai-Delhi Express (100 total seats distributed)
(1, 'GENERAL', 20, 0.80),
(1, 'SL', 40, 1.00),
(1, 'AC_3TIER', 20, 1.50),
(1, 'AC_2TIER', 15, 2.00),
(1, 'AC_1TIER', 5, 2.50),
-- Train 2: Delhi-Bangalore Superfast (150 total seats distributed)
(2, 'GENERAL', 30, 0.80),
(2, 'SL', 60, 1.00),
(2, 'AC_3TIER', 30, 1.50),
(2, 'AC_2TIER', 20, 2.00),
(2, 'AC_1TIER', 10, 2.50),
-- Train 3: Chennai-Hyderabad Passenger (200 total seats distributed)
(3, 'GENERAL', 80, 0.80),
(3, 'SL', 70, 1.00),
(3, 'AC_3TIER', 30, 1.50),
(3, 'AC_2TIER', 15, 2.00),
(3, 'AC_1TIER', 5, 2.50),
-- Train 4: Kolkata-Delhi Express (120 total seats distributed)
(4, 'GENERAL', 40, 0.80),
(4, 'SL', 40, 1.00),
(4, 'AC_3TIER', 20, 1.50),
(4, 'AC_2TIER', 15, 2.00),
(4, 'AC_1TIER', 5, 2.50),
-- Train 5: Mumbai-Pune Local (300 total seats distributed)
(5, 'GENERAL', 180, 0.80),
(5, 'SL', 80, 1.00),
(5, 'AC_3TIER', 25, 1.50),
(5, 'AC_2TIER', 10, 2.00),
(5, 'AC_1TIER', 5, 2.50),
-- Train 6: Delhi-Ahmedabad Superfast (80 total seats distributed)
(6, 'GENERAL', 20, 0.80),
(6, 'SL', 30, 1.00),
(6, 'AC_3TIER', 15, 1.50),
(6, 'AC_2TIER', 10, 2.00),
(6, 'AC_1TIER', 5, 2.50);

-- Update available seats based on bookings
UPDATE routes SET available_seats = available_seats - 2 WHERE id = 1; -- Mumbai-Delhi route
UPDATE routes SET available_seats = available_seats - 1 WHERE id = 2; -- Delhi-Bangalore route
UPDATE routes SET available_seats = available_seats - 3 WHERE id = 3; -- Chennai-Hyderabad route
UPDATE routes SET available_seats = available_seats - 1 WHERE id = 4; -- Kolkata-Delhi route

