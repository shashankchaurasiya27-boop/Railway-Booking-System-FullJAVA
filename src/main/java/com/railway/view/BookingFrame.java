package com.railway.view;

import com.railway.controller.BookingController;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.Route;
import com.railway.booking.backend.model.TrainSeat;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.service.DynamicPricingService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Date;
import java.util.List;
import com.railway.util.ApplicationContextProvider;

public class BookingFrame extends JFrame {
    private User user;
    private Route selectedRoute;
    private BookingController bookingController;
    private DynamicPricingService dynamicPricingService;

    // Form fields
    private JTextField txtJourneyDate;
    private JTextField txtPassengerName;
    private JTextField txtPassengerAge;
    private JComboBox<String> cmbGender;
    private JComboBox<String> cmbCoachType;
    private JLabel lblDynamicFare;  // Shows calculated fare based on coach type
    private JButton btnBook;
    private JButton btnBack;
    private JTextArea txtBookingList;

    public BookingFrame(User user, Route route) {
        this.user = user;
        this.selectedRoute = route;
        this.bookingController = ApplicationContextProvider.getBean(BookingController.class);
        this.dynamicPricingService = ApplicationContextProvider.getBean(DynamicPricingService.class);
        
        setTitle("Book Ticket - Railway Booking System");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        loadBookings();
        
        // Initial fare display update
        SwingUtilities.invokeLater(() -> updateFareDisplay());
    }

    private void initializeComponents() {
        txtJourneyDate = new JTextField("YYYY-MM-DD");
        txtPassengerName = new JTextField();
        txtPassengerAge = new JTextField();
        cmbGender = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        // Coach types matching TrainSeat.CoachType enum values
        cmbCoachType = new JComboBox<>(new String[]{
            "GENERAL",      // General/Unreserved
            "SL",           // Sleeper class
            "3AC",          // 3 Tier AC
            "2AC",          // 2 Tier AC
            "1AC"           // 1 Tier AC
        });
        btnBook = new JButton("Book Ticket");
        btnBack = new JButton("Back to Search");
        txtBookingList = new JTextArea();
        lblDynamicFare = new JLabel("Estimated Fare: -");
        txtBookingList.setEditable(false);

        btnBook.addActionListener(this::handleBookTicket);
        
        // Add coach type change listener to update fare display
        cmbCoachType.addActionListener(e -> updateFareDisplay());
    }
    
    /**
     * Update fare display with dynamic pricing (surge pricing)
     * Shows base fare + coach multiplier + estimated surge factor
     */
    private void updateFareDisplay() {
        if (selectedRoute == null || lblDynamicFare == null) return;
        
        String coachTypeStr = cmbCoachType.getSelectedItem().toString();
        TrainSeat.CoachType coachType = TrainSeat.CoachType.fromString(coachTypeStr);
        
        double baseFare = selectedRoute.getFare();
        double multiplier = coachType.getDefaultFareMultiplier();
        
        // Get estimated fare with dynamic pricing
        // Using 50% as estimated occupancy for preview (actual fare calculated at booking time)
        int estimatedTotalSeats = 40; // Typical coach size
        int estimatedAvailableSeats = 20; // 50% filled
        int passengerCount = 1;
        
        double estimatedFare = baseFare * multiplier;
        double estimatedOccupancy = 0.5; // 50% for preview
        double estimatedSurge = estimatedOccupancy; // Surge = occupancy (capped at 0.8)
        double finalEstimatedFare = estimatedFare * (1 + estimatedSurge);
        
        // Color coding based on demand level
        Color fareColor;
        String demandText;
        if (estimatedSurge <= 0.2) {
            fareColor = new Color(0, 128, 0); // Green - Low demand
            demandText = "Low demand";
        } else if (estimatedSurge <= 0.5) {
            fareColor = new Color(255, 140, 0); // Orange - Moderate demand
            demandText = "Moderate demand";
        } else {
            fareColor = new Color(220, 20, 60); // Red - High demand
            demandText = "High demand";
        }
        
        lblDynamicFare.setForeground(fareColor);
        lblDynamicFare.setText(String.format(
            "<html><b>Estimated Fare: ₹%.2f</b><br/>" +
            "<small>Base: ₹%.2f × %s: %.1fx × Surge: %.1f%% (%s)</small><br/>" +
            "<small><i>Final fare may vary based on real-time availability</i></small></html>",
            finalEstimatedFare, baseFare, coachType.getShortCode(), multiplier, 
            estimatedSurge * 100, demandText));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Route information panel
        JPanel routeInfoPanel = new JPanel(new BorderLayout());
        routeInfoPanel.setBorder(BorderFactory.createTitledBorder("Selected Route"));
        
        JLabel routeLabel = new JLabel();
        if (selectedRoute != null) {
            routeLabel.setText(String.format("<html><b>%s (%s)</b><br>%s to %s<br>Departure: %s | Arrival: %s<br>Base Fare: ₹%.2f</html>",
                selectedRoute.getTrainName(),
                selectedRoute.getTrainNumber(),
                selectedRoute.getSourceStationName(),
                selectedRoute.getDestinationStationName(),
                selectedRoute.getDepartureTime(),
                selectedRoute.getArrivalTime(),
                selectedRoute.getFare()
            ));
        }
        routeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        routeInfoPanel.add(routeLabel, BorderLayout.CENTER);
        routeInfoPanel.setPreferredSize(new Dimension(0, 100));
        
        // Booking form panel with GridBagLayout for proper alignment
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Passenger Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 0: Journey Date and Coach Type
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Journey Date (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1;
        txtJourneyDate.setPreferredSize(new Dimension(150, 28));
        formPanel.add(txtJourneyDate, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Coach Type:"), gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        gbc.weightx = 1;
        cmbCoachType.setPreferredSize(new Dimension(150, 28));
        formPanel.add(cmbCoachType, gbc);
        
        // Row 1: Passenger Name
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Passenger Name:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        txtPassengerName.setPreferredSize(new Dimension(300, 28));
        formPanel.add(txtPassengerName, gbc);
        gbc.gridwidth = 1; // Reset
        
        // Row 2: Passenger Age and Gender
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Passenger Age:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.weightx = 1;
        txtPassengerAge.setPreferredSize(new Dimension(100, 28));
        formPanel.add(txtPassengerAge, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Gender:"), gbc);
        
        gbc.gridx = 3; gbc.gridy = 2;
        gbc.weightx = 1;
        cmbGender.setPreferredSize(new Dimension(150, 28));
        formPanel.add(cmbGender, gbc);
        
        // Row 3: Dynamic Fare Display
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Calculated Fare:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        lblDynamicFare.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblDynamicFare.setForeground(new Color(0, 100, 0));
        lblDynamicFare.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        formPanel.add(lblDynamicFare, gbc);
        gbc.gridwidth = 1; // Reset
        
        // Row 4: Book Button (centered)
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 10, 10, 10);
        btnBook.setPreferredSize(new Dimension(200, 40));
        btnBook.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        formPanel.add(btnBook, gbc);

        // Bookings list panel
        JPanel bookingsPanel = new JPanel(new BorderLayout());
        bookingsPanel.setBorder(BorderFactory.createTitledBorder("Your Recent Bookings"));
        txtBookingList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bookingsPanel.add(new JScrollPane(txtBookingList), BorderLayout.CENTER);
        bookingsPanel.setPreferredSize(new Dimension(0, 120));

        // Button panel for Back button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnBack.setPreferredSize(new Dimension(150, 35));
        buttonPanel.add(btnBack);

        // Main content panel to hold form and bookings
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(bookingsPanel, BorderLayout.SOUTH);
        
        // Add components to frame
        add(routeInfoPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add padding around the frame content
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    private void registerEventHandlers() {
        btnBack.addActionListener(e -> goBack());
    }

    private void handleBookTicket(ActionEvent e) {
        if (selectedRoute == null) {
            JOptionPane.showMessageDialog(this, "No route selected!");
            return;
        }
        
        try {
            Date journeyDate = Date.valueOf(txtJourneyDate.getText().trim());
            String passengerName = txtPassengerName.getText().trim();
            int passengerAge = Integer.parseInt(txtPassengerAge.getText().trim());
            Booking.Gender gender = Booking.Gender.valueOf(cmbGender.getSelectedItem().toString());

            if (passengerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter passenger name!");
                return;
            }
            
            if (passengerAge < 1 || passengerAge > 120) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age (1-120)!");
                return;
            }

            Booking booking = new Booking();
            booking.setUserId(user.getUserId());
            booking.setTrainId(selectedRoute.getTrainId());
            booking.setRouteId(selectedRoute.getId());
            booking.setJourneyDate(journeyDate);
            booking.setPassengerName(passengerName);
            booking.setPassengerAge(passengerAge);
            booking.setPassengerGender(gender);
            booking.setNumberOfPassengers(1);
            // Use fromString to map dropdown values (1AC, 2AC, 3AC, SL) to enum values (AC_1TIER, AC_2TIER, AC_3TIER, SL)
            booking.setCoachType(com.railway.booking.backend.model.TrainSeat.CoachType.fromString(cmbCoachType.getSelectedItem().toString()));
            booking.setTotalFare(java.math.BigDecimal.valueOf(selectedRoute.getFare()));

            String result = bookingController.createBooking(booking);
            if (result != null) {
                JOptionPane.showMessageDialog(this, "Booking successful! Booking ID: " + result);
                loadBookings();
                clearForm();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Please check your input fields! Date format should be YYYY-MM-DD");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage());
        }
    }
    
    private void clearForm() {
        txtJourneyDate.setText("YYYY-MM-DD");
        txtPassengerName.setText("");
        txtPassengerAge.setText("");
        cmbGender.setSelectedIndex(0);
    }

    private void loadBookings() {
        List<Booking> bookings = bookingController.getUserBookings(user.getUserId());
        txtBookingList.setText("");
        
        if (bookings.isEmpty()) {
            txtBookingList.setText("No bookings found.");
            return;
        }
        
        for (Booking booking : bookings) {
            txtBookingList.append(String.format("Booking ID: %s | Train: %s | Date: %s | Status: %s\n",
                booking.getBookingId(),
                booking.getTrainName(),
                booking.getJourneyDate(),
                booking.getBookingStatus().name()
            ));
        }
    }
    
    private void goBack() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new TrainSearchFrame(user).setVisible(true);
        });
    }
}
