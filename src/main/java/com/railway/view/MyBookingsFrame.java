package com.railway.view;

import com.railway.controller.BookingController;
import com.railway.booking.backend.dao.BookingLogDAO;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.BookingLog;
import com.railway.booking.backend.model.User;
import com.railway.util.ApplicationContextProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MyBookingsFrame extends JFrame {
    private User user;
    private BookingController bookingController;
    
    // Components
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JButton btnCancelBooking;
    private JButton btnViewDetails;
    private JButton btnViewTimeline;
    private JButton btnBack;
    private JComboBox<String> cmbStatusFilter;
    private BookingLogDAO bookingLogDAO;
    
    public MyBookingsFrame(User user) {
        this.user = user;
        this.bookingController = ApplicationContextProvider.getBean(BookingController.class);
        this.bookingLogDAO = ApplicationContextProvider.getBean(BookingLogDAO.class);
        
        setTitle("My Bookings - Railway Booking System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        loadBookings();
    }
    
    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"Booking ID", "Train", "Route", "Journey Date", "Passengers", 
                              "Total Fare", "Status", "Booking Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable = new JTable(tableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.getTableHeader().setReorderingAllowed(false);
        
        // Buttons
        btnRefresh = new JButton("Refresh");
        btnCancelBooking = new JButton("Cancel Booking");
        btnViewDetails = new JButton("View Details");
        btnViewTimeline = new JButton("View Timeline");
        btnBack = new JButton("Back to Main Menu");
        
        // Status filter
        cmbStatusFilter = new JComboBox<>(new String[]{"All", "PENDING", "CONFIRMED", "CANCELLED", "WAITING"});
        
        // Style buttons
        btnCancelBooking.setEnabled(false);
        btnViewDetails.setEnabled(false);
        btnViewTimeline.setEnabled(false);
        btnCancelBooking.setPreferredSize(new Dimension(120, 30));
        btnViewDetails.setPreferredSize(new Dimension(120, 30));
        btnViewTimeline.setPreferredSize(new Dimension(120, 30));
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnBack.setPreferredSize(new Dimension(150, 30));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with filter and refresh
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Filter by Status:"));
        topPanel.add(cmbStatusFilter);
        topPanel.add(btnRefresh);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("My Bookings"));
        tablePanel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnViewDetails);
        buttonPanel.add(btnViewTimeline);
        buttonPanel.add(btnCancelBooking);
        buttonPanel.add(btnBack);
        
        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add padding
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void registerEventHandlers() {
        btnRefresh.addActionListener(e -> loadBookings());
        btnCancelBooking.addActionListener(e -> cancelSelectedBooking());
        btnViewDetails.addActionListener(e -> viewBookingDetails());
        btnViewTimeline.addActionListener(e -> viewBookingTimeline());
        btnBack.addActionListener(e -> goBack());
        
        // Enable buttons when row is selected
        bookingsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = bookingsTable.getSelectedRow() != -1;
            btnCancelBooking.setEnabled(hasSelection);
            btnViewDetails.setEnabled(hasSelection);
            btnViewTimeline.setEnabled(hasSelection);
        });
        
        // Filter change
        cmbStatusFilter.addActionListener(e -> loadBookings());
    }
    
    private void loadBookings() {
        btnRefresh.setEnabled(false);
        
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() {
                return bookingController.getUserBookings(user.getUserId());
            }
            
            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    List<Booking> bookings = get();
                    
                    // Apply status filter
                    String selectedStatus = (String) cmbStatusFilter.getSelectedItem();
                    if (!"All".equals(selectedStatus)) {
                        bookings = bookings.stream()
                            .filter(booking -> booking.getBookingStatus().name().equals(selectedStatus))
                            .collect(java.util.stream.Collectors.toList());
                    }
                    
                    // Clear table
                    tableModel.setRowCount(0);
                    
                    // Populate table
                    for (Booking booking : bookings) {
                        Object[] row = {
                            booking.getBookingId(),
                            booking.getTrainName(),
                            booking.getRouteDescription(),
                            booking.getJourneyDate(),
                            booking.getNumberOfPassengers(),
                            String.format("₹%.2f", booking.getTotalFare()),
                            booking.getBookingStatus().name(),
                            booking.getBookingDate()
                        };
                        tableModel.addRow(row);
                    }
                    
                    if (bookings.isEmpty()) {
                        JOptionPane.showMessageDialog(MyBookingsFrame.this, "No bookings found.");
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, MyBookingsFrame.this, "load bookings");
                }
            }
        };
        worker.execute();
    }
    
    private void cancelSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.");
            return;
        }
        
        String bookingId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 6);
        
        if ("CANCELLED".equals(status)) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel booking " + bookingId + "?", 
            "Cancel Booking", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            String cancelResult = bookingController.cancelBooking(bookingId);
            if ("SUCCESS".equals(cancelResult)) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                loadBookings();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking: " + cancelResult);
            }
        }
    }
    
    private void viewBookingDetails() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to view details.");
            return;
        }
        
        final String bookingId = (String) tableModel.getValueAt(selectedRow, 0);
        btnViewDetails.setEnabled(false);
        com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Loading booking details...");
        
        SwingWorker<Booking, Void> worker = new SwingWorker<Booking, Void>() {
            @Override
            protected Booking doInBackground() {
                return bookingController.getBookingById(bookingId);
            }
            
            @Override
            protected void done() {
                loading.close();
                btnViewDetails.setEnabled(true);
                try {
                    Booking booking = get();
                    if (booking != null) {
                        showBookingDetails(booking);
                    } else {
                        JOptionPane.showMessageDialog(MyBookingsFrame.this, "Booking details not found.");
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, MyBookingsFrame.this, "load booking details");
                }
            }
        };
        worker.execute();
        loading.showNonBlocking();
    }
    
    private void showBookingDetails(Booking booking) {
        StringBuilder details = new StringBuilder();
        details.append("Booking Details\n");
        details.append("==============\n\n");
        details.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        details.append("Train: ").append(booking.getTrainName()).append("\n");
        details.append("Route: ").append(booking.getRouteDescription()).append("\n");
        details.append("Journey Date: ").append(booking.getJourneyDate()).append("\n");
        details.append("Number of Passengers: ").append(booking.getNumberOfPassengers()).append("\n");
        details.append("Total Fare: ₹").append(String.format("%.2f", booking.getTotalFare())).append("\n");
        details.append("Status: ").append(booking.getBookingStatus().name()).append("\n");
        details.append("Booking Date: ").append(booking.getBookingDate()).append("\n");
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Booking Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void viewBookingTimeline() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to view timeline.");
            return;
        }
        
        String bookingId = (String) tableModel.getValueAt(selectedRow, 0);
        
        com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(MyBookingsFrame.this, "Loading timeline...");
        
        SwingWorker<List<BookingLog>, Void> worker = new SwingWorker<List<BookingLog>, Void>() {
            @Override
            protected List<BookingLog> doInBackground() {
                return bookingLogDAO.getLogsByBookingIdWithDetails(bookingId);
            }
            
            @Override
            protected void done() {
                loading.dispose();
                try {
                    List<BookingLog> logs = get();
                    showBookingTimeline(bookingId, logs);
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, MyBookingsFrame.this, "load booking timeline");
                }
            }
        };
        worker.execute();
        loading.showNonBlocking();
    }
    
    private void showBookingTimeline(String bookingId, List<BookingLog> logs) {
        StringBuilder timeline = new StringBuilder();
        timeline.append("Booking Timeline - ").append(bookingId).append("\n");
        timeline.append("============================\n\n");
        
        if (logs == null || logs.isEmpty()) {
            timeline.append("No timeline events found.\n");
            timeline.append("(Booking was created before audit logging was enabled)\n");
        } else {
            for (BookingLog log : logs) {
                timeline.append("[").append(log.getTimestamp()).append("] ");
                if (log.getOldStatus() != null) {
                    timeline.append(log.getOldStatus()).append(" → ");
                } else {
                    timeline.append("NEW → ");
                }
                timeline.append(log.getNewStatus()).append("\n");
                timeline.append("    ").append(log.getMessage()).append("\n\n");
            }
        }
        
        JTextArea textArea = new JTextArea(timeline.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0); // Scroll to top
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Booking Timeline", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void goBack() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new MainFrame(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user)).setVisible(true);
        });
    }
}
