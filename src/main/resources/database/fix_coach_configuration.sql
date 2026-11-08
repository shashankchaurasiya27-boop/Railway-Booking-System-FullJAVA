-- Fix: Add missing coach type configurations for all trains
-- This script adds AC_2TIER and AC_1TIER coach types that were missing

USE railway_booking;

-- =====================================================
-- ADD MISSING COACH TYPES FOR ALL TRAINS
-- =====================================================

-- Train 1: Mumbai-Delhi Express (100 total seats)
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
(1, 'AC_2TIER', 10, 2.0),
(1, 'AC_1TIER', 5, 2.5)
ON DUPLICATE KEY UPDATE fare_multiplier = VALUES(fare_multiplier), total_seats = VALUES(total_seats);

-- Train 2: Delhi-Bangalore Superfast (150 total seats)
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
(2, 'AC_2TIER', 15, 2.0),
(2, 'AC_1TIER', 5, 2.5)
ON DUPLICATE KEY UPDATE fare_multiplier = VALUES(fare_multiplier), total_seats = VALUES(total_seats);

-- Train 3: Chennai-Hyderabad Passenger (200 total seats)
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
(3, 'AC_2TIER', 10, 2.0),
(3, 'AC_1TIER', 5, 2.5)
ON DUPLICATE KEY UPDATE fare_multiplier = VALUES(fare_multiplier), total_seats = VALUES(total_seats);

-- Train 4: Kolkata-Delhi Express (120 total seats)
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
(4, 'AC_2TIER', 10, 2.0),
(4, 'AC_1TIER', 5, 2.5)
ON DUPLICATE KEY UPDATE fare_multiplier = VALUES(fare_multiplier), total_seats = VALUES(total_seats);

-- Train 5: Mumbai-Pune Local (300 total seats)
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
(5, 'AC_2TIER', 10, 2.0),
(5, 'AC_1TIER', 5, 2.5)
ON DUPLICATE KEY UPDATE fare_multiplier = VALUES(fare_multiplier), total_seats = VALUES(total_seats);

-- Train 6: Delhi-Ahmedabad Superfast (80 total seats)
INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES
(6, 'AC_2TIER', 10, 2.0),
(6, 'AC_1TIER', 5, 2.5)
ON DUPLICATE KEY UPDATE fare_multiplier = VALUES(fare_multiplier), total_seats = VALUES(total_seats);

-- =====================================================
-- INITIALIZE SEAT AVAILABILITY FOR FUTURE DATES
-- This pre-populates availability records for next 30 days
-- =====================================================

-- Create a procedure to initialize availability for all trains and coach types
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS InitializeAvailabilityForDate(IN target_date DATE)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_train_id INT;
    DECLARE v_coach_type VARCHAR(20);
    DECLARE v_total_seats INT;
    
    DECLARE coach_cursor CURSOR FOR
        SELECT train_id, coach_type, total_seats FROM train_seats;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN coach_cursor;
    
    read_loop: LOOP
        FETCH coach_cursor INTO v_train_id, v_coach_type, v_total_seats;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Insert availability record if not exists
        INSERT IGNORE INTO seat_availability 
            (train_id, coach_type, journey_date, total_seats, available_seats, booked_seats)
        VALUES 
            (v_train_id, v_coach_type, target_date, v_total_seats, v_total_seats, 0);
    END LOOP;
    
    CLOSE coach_cursor;
END //

DELIMITER ;

-- Initialize availability for next 30 days
-- Call this manually or schedule it
-- CALL InitializeAvailabilityForDate(CURDATE() + INTERVAL 1 DAY);
-- CALL InitializeAvailabilityForDate(CURDATE() + INTERVAL 2 DAY);
-- etc...

-- For now, just initialize for a specific date if needed
-- Uncomment and modify the date as needed:
-- CALL InitializeAvailabilityForDate('2026-12-11');

-- Show current train seat configuration
SELECT 
    t.train_number,
    t.train_name,
    ts.coach_type,
    ts.total_seats,
    ts.fare_multiplier
FROM train_seats ts
JOIN trains t ON ts.train_id = t.id
ORDER BY t.train_id, ts.coach_type;
