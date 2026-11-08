# Railway Booking System - Testing Guide

## Setup Instructions

### 1. Database Setup
1. **Create Database**: Run the unified schema
   ```sql
   mysql -u root -p < src/main/resources/database/unified_schema.sql
   ```

2. **Load Test Data**: Insert sample data
   ```sql
   mysql -u root -p railway_booking < src/main/resources/database/test_data.sql
   ```

3. **Verify Database**: Check that all tables are created with sample data
   ```sql
   USE railway_booking;
   SHOW TABLES;
   SELECT COUNT(*) FROM users;
   SELECT COUNT(*) FROM trains;
   SELECT COUNT(*) FROM stations;
   SELECT COUNT(*) FROM routes;
   SELECT COUNT(*) FROM bookings;
   ```

### 2. Application Setup
1. **Compile Application**:
   ```bash
   cd railway-booking-system
   mvn compile
   ```

2. **Run Application**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.railway.Main"
   ```

## Test Accounts

### Admin Account
- **Username**: admin
- **Password**: admin123
- **Access**: Full admin panel with train, booking, and user management

### Regular User Accounts
- **Username**: john_doe, **Password**: password123
- **Username**: jane_smith, **Password**: password456
- **Username**: mike_wilson, **Password**: password789
- **Username**: sarah_jones, **Password**: password101

## Testing Workflows

### 1. Authentication Flow Testing

#### Test Case 1.1: Valid Login
1. Launch application
2. Enter valid credentials (admin/admin123)
3. **Expected**: Login successful, MainFrame opens
4. **Verify**: User name displayed in title bar

#### Test Case 1.2: Invalid Login
1. Enter invalid credentials (wrong password)
2. **Expected**: Error message displayed
3. **Verify**: Login frame remains open

#### Test Case 1.3: User Registration
1. Click "Register" button
2. Fill registration form with valid data
3. **Expected**: Registration successful, can login with new credentials

### 2. User Workflow Testing

#### Test Case 2.1: Train Search by Number/Name
1. Login as regular user
2. Click "Book Ticket" or "Book New Ticket"
3. Go to "Search by Train" tab
4. Enter train number "12345" or name "Express"
5. **Expected**: Search results show matching trains
6. **Verify**: Train details, fare, and availability displayed

#### Test Case 2.2: Train Search by Route
1. Go to "Search by Route" tab
2. Select source station (e.g., Mumbai Central)
3. Select destination station (e.g., New Delhi)
4. Enter journey date (e.g., 2024-12-15)
5. **Expected**: Routes between selected stations displayed
6. **Verify**: Departure/arrival times, fares, seat availability

#### Test Case 2.3: Ticket Booking
1. Select a train from search results
2. Click "Book Selected Train"
3. Fill passenger details:
   - Journey Date: 2024-12-15
   - Passenger Name: Test User
   - Age: 25
   - Gender: MALE
4. Click "Book Ticket"
5. **Expected**: Booking successful with confirmation message
6. **Verify**: Booking ID generated, seat assigned

#### Test Case 2.4: View Bookings
1. Click "My Bookings" or "View Bookings"
2. **Expected**: List of user's bookings displayed
3. **Verify**: Booking details, status, dates shown correctly

#### Test Case 2.5: Cancel Booking
1. In My Bookings, select a confirmed booking
2. Click "Cancel Booking"
3. Confirm cancellation
4. **Expected**: Booking status changed to CANCELLED
5. **Verify**: Booking list updated

#### Test Case 2.6: Profile Management
1. Click "My Profile" or "Update Profile"
2. Click "Edit Profile"
3. Update email, phone, address
4. Click "Save Changes"
5. **Expected**: Profile updated successfully
6. **Verify**: Changes reflected in profile view

#### Test Case 2.7: Change Password
1. In Profile frame, click "Change Password"
2. Enter current password, new password, confirm password
3. **Expected**: Password changed successfully
4. **Verify**: Can login with new password

### 3. Admin Workflow Testing

#### Test Case 3.1: Admin Login
1. Login with admin credentials
2. **Expected**: Admin panel accessible
3. **Verify**: Admin-specific menu items visible

#### Test Case 3.2: Train Management
1. Go to "Train Management" tab
2. **Verify**: List of all trains displayed
3. Click "Add Train":
   - Train Number: TEST123
   - Train Name: Test Train
   - Type: EXPRESS
   - Total Seats: 100
4. **Expected**: Train added successfully
5. **Verify**: New train appears in list

#### Test Case 3.3: Edit Train
1. Select a train from the list
2. Click "Edit Train"
3. **Expected**: Edit dialog opens (functionality placeholder)

#### Test Case 3.4: Delete Train
1. Select a train (preferably one without bookings)
2. Click "Delete Train"
3. Confirm deletion
4. **Expected**: Train deleted successfully
5. **Verify**: Train removed from list

#### Test Case 3.5: View All Bookings
1. Go to "Bookings Management" tab
2. **Expected**: All system bookings displayed
3. **Verify**: User names, train details, status shown
4. Test status filter dropdown
5. **Expected**: Filtered results displayed

#### Test Case 3.6: User Management
1. Go to "Users Management" tab
2. **Expected**: All users displayed
3. **Verify**: User details, registration dates shown
4. Test "Edit User" functionality (placeholder)

### 4. Navigation Testing

#### Test Case 4.1: Main Menu Navigation
1. Test all sidebar buttons:
   - Dashboard (default view)
   - Book Ticket → TrainSearchFrame
   - My Bookings → MyBookingsFrame
   - My Profile → ProfileFrame
2. **Expected**: Correct frames open, previous frames close

#### Test Case 4.2: Menu Bar Navigation
1. Test File menu:
   - Logout → Returns to LoginFrame
   - Exit → Closes application
2. Test Booking menu:
   - New Booking → TrainSearchFrame
   - View My Bookings → MyBookingsFrame
3. Test Admin menu (admin only):
   - All admin functions → AdminFrame

#### Test Case 4.3: Back Navigation
1. From any sub-frame, click "Back" button
2. **Expected**: Returns to MainFrame
3. **Verify**: Previous frame closed properly

### 5. Error Handling Testing

#### Test Case 5.1: Invalid Input Validation
1. Try booking with empty passenger name
2. Try booking with invalid age (negative or >120)
3. Try booking with invalid date format
4. **Expected**: Appropriate error messages displayed

#### Test Case 5.2: Database Connection Issues
1. Stop MySQL service
2. Try to login or perform operations
3. **Expected**: Graceful error handling

#### Test Case 5.3: No Results Scenarios
1. Search for non-existent train
2. Search for route with no trains
3. **Expected**: "No results found" message

### 6. Edge Cases Testing

#### Test Case 6.1: Seat Availability
1. Book all available seats for a route
2. Try to book one more seat
3. **Expected**: Booking goes to waiting list or error message

#### Test Case 6.2: Date Validation
1. Try to book for past dates
2. **Expected**: Appropriate validation message

#### Test Case 6.3: Concurrent Bookings
1. Open multiple booking windows
2. Try to book same seat simultaneously
3. **Expected**: Proper handling of conflicts

## Expected Results Summary

### Successful Test Outcomes
- ✅ All navigation buttons work correctly
- ✅ Train search returns accurate results
- ✅ Booking process completes successfully
- ✅ Admin panel manages trains, bookings, users
- ✅ Profile management works correctly
- ✅ Error messages are user-friendly
- ✅ Database operations complete without errors

### Known Limitations
- Edit train functionality is placeholder
- Edit user functionality is placeholder
- Payment integration not implemented
- Advanced filtering options limited
- No email notifications
- No print ticket functionality

## Performance Testing

### Load Testing
1. Create multiple user accounts
2. Perform simultaneous bookings
3. **Expected**: System handles concurrent operations

### Data Volume Testing
1. Add large number of trains and routes
2. Create many bookings
3. **Expected**: System performance remains acceptable

## Security Testing

### Access Control
1. Try to access admin functions as regular user
2. **Expected**: Access denied messages
3. **Verify**: Regular users cannot see admin menu items

### Input Validation
1. Try SQL injection in text fields
2. **Expected**: Input properly sanitized
3. **Verify**: No database errors

## Bug Reporting Template

When reporting bugs, include:
1. **Test Case**: Which test case failed
2. **Steps**: Exact steps to reproduce
3. **Expected**: What should happen
4. **Actual**: What actually happened
5. **Environment**: OS, Java version, database version
6. **Screenshots**: If applicable

## Conclusion

This testing guide covers all major workflows and edge cases. The application should handle all test cases gracefully with appropriate user feedback and error handling.

