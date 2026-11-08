package com.railway.view;

import com.railway.controller.TrainController;
import com.railway.booking.backend.model.Route;
import com.railway.booking.backend.model.Station;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.service.SeatAvailabilityService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.util.List;
import com.railway.util.ApplicationContextProvider;

public class TrainSearchFrame extends JFrame {
    private User user;
    private TrainController trainController;
    
    // Search components
    private JTabbedPane searchTabs;
    private JPanel simpleSearchPanel;
    private JPanel routeSearchPanel;
    
    // Simple search components
    private JTextField txtTrainNumber;
    private JTextField txtTrainName;
    private JTextField txtSimpleJourneyDate;
    private JButton btnSimpleSearch;
    
    // Route search components
    private JComboBox<Station> cmbSourceStation;
    private JComboBox<Station> cmbDestinationStation;
    private JTextField txtJourneyDate;
    private JButton btnRouteSearch;
    
    // Results components
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton btnBookSelected;
    private JButton btnBack;
    private SeatAvailabilityService seatAvailabilityService;
    private Date currentSearchDate;
    
    // Real-time refresh
    private Timer refreshTimer;
    private List<Route> lastSearchResults;
    
    public TrainSearchFrame(User user) {
        this.user = user;
        this.trainController = new TrainController();
        this.seatAvailabilityService = ApplicationContextProvider.getBean(SeatAvailabilityService.class);
        
        setTitle("Search Trains - Railway Booking System");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        loadStations();
        
        // Initialize real-time refresh timer (every 5 seconds)
        refreshTimer = new Timer(5000, e -> refreshAvailabilityDisplay());
    }
    
    private void initializeComponents() {
        // Initialize search tabs
        searchTabs = new JTabbedPane();
        
        // Simple search components
        txtTrainNumber = new JTextField(15);
        txtTrainName = new JTextField(15);
        btnSimpleSearch = new JButton("Search by Train");
        
        // Route search components
        cmbSourceStation = new JComboBox<>();
        cmbDestinationStation = new JComboBox<>();
        txtJourneyDate = new JTextField(10);
        txtJourneyDate.setText("YYYY-MM-DD");
        btnRouteSearch = new JButton("Search by Route");
        
        // Results table - Updated with coach type availability columns
        String[] columnNames = {"Route ID", "Train Number", "Train Name", "Source", "Destination", 
                              "Departure", "Arrival", "Base Fare", "GEN", "SL", "3AC", "2AC", "1AC"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        
        // Action buttons
        btnBookSelected = new JButton("Book Selected Train");
        btnBack = new JButton("Back to Main Menu");
        
        // Style buttons
        btnBookSelected.setEnabled(false);
        btnBookSelected.setPreferredSize(new Dimension(150, 30));
        btnBack.setPreferredSize(new Dimension(150, 30));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create search panels
        createSimpleSearchPanel();
        createRouteSearchPanel();
        
        // Add tabs
        searchTabs.addTab("Search by Train", simpleSearchPanel);
        searchTabs.addTab("Search by Route", routeSearchPanel);
        
        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));
        resultsPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnBookSelected);
        buttonPanel.add(btnBack);
        
        // Add components to main frame
        add(searchTabs, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add padding
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void createSimpleSearchPanel() {
        simpleSearchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Train number
        gbc.gridx = 0; gbc.gridy = 0;
        simpleSearchPanel.add(new JLabel("Train Number:"), gbc);
        gbc.gridx = 1;
        simpleSearchPanel.add(txtTrainNumber, gbc);
        
        // Train name
        gbc.gridx = 0; gbc.gridy = 1;
        simpleSearchPanel.add(new JLabel("Train Name:"), gbc);
        gbc.gridx = 1;
        simpleSearchPanel.add(txtTrainName, gbc);
        
        // Journey date
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        simpleSearchPanel.add(new JLabel("Journey Date:"), gbc);
        gbc.gridx = 1;
        txtSimpleJourneyDate = new JTextField("YYYY-MM-DD");
        simpleSearchPanel.add(txtSimpleJourneyDate, gbc);
        
        // Search button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        simpleSearchPanel.add(btnSimpleSearch, gbc);
        
        simpleSearchPanel.setBorder(BorderFactory.createTitledBorder("Search by Train Details"));
    }
    
    private void createRouteSearchPanel() {
        routeSearchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Source station
        gbc.gridx = 0; gbc.gridy = 0;
        routeSearchPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1;
        routeSearchPanel.add(cmbSourceStation, gbc);
        
        // Destination station
        gbc.gridx = 0; gbc.gridy = 1;
        routeSearchPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 1;
        routeSearchPanel.add(cmbDestinationStation, gbc);
        
        // Journey date
        gbc.gridx = 0; gbc.gridy = 2;
        routeSearchPanel.add(new JLabel("Journey Date:"), gbc);
        gbc.gridx = 1;
        routeSearchPanel.add(txtJourneyDate, gbc);
        
        // Search button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        routeSearchPanel.add(btnRouteSearch, gbc);
        
        routeSearchPanel.setBorder(BorderFactory.createTitledBorder("Search by Route"));
    }
    
    private void registerEventHandlers() {
        btnSimpleSearch.addActionListener(e -> performSimpleSearch());
        btnRouteSearch.addActionListener(e -> performRouteSearch());
        btnBookSelected.addActionListener(e -> bookSelectedTrain());
        btnBack.addActionListener(e -> goBack());
        
        // Enable book button when row is selected
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            btnBookSelected.setEnabled(resultsTable.getSelectedRow() != -1);
        });
    }
    
    private void loadStations() {
        SwingWorker<List<Station>, Void> worker = new SwingWorker<List<Station>, Void>() {
            @Override
            protected List<Station> doInBackground() {
                return trainController.getAllStations();
            }
            
            @Override
            protected void done() {
                try {
                    List<Station> stations = get();
                    cmbSourceStation.removeAllItems();
                    cmbDestinationStation.removeAllItems();
                    for (Station station : stations) {
                        cmbSourceStation.addItem(station);
                        cmbDestinationStation.addItem(station);
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, TrainSearchFrame.this, "load stations");
                }
            }
        };
        worker.execute();
    }
    
    private void performSimpleSearch() {
        String trainNumber = txtTrainNumber.getText().trim();
        String trainName = txtTrainName.getText().trim();
        String journeyDateStr = txtSimpleJourneyDate.getText().trim();
        
        if (trainNumber.isEmpty() && trainName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter train number or name to search.");
            return;
        }
        
        if (journeyDateStr.equals("YYYY-MM-DD") || journeyDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid journey date (YYYY-MM-DD).");
            return;
        }
        
        try {
            Date journeyDate = Date.valueOf(journeyDateStr);
            this.currentSearchDate = journeyDate;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date format (YYYY-MM-DD).");
            return;
        }
        
        btnSimpleSearch.setEnabled(false);
        com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Searching trains...");
        
        SwingWorker<List<Route>, Void> worker = new SwingWorker<List<Route>, Void>() {
            @Override
            protected List<Route> doInBackground() {
                return trainController.searchRoutesByTrain(trainNumber, trainName);
            }
            
            @Override
            protected void done() {
                loading.close();
                btnSimpleSearch.setEnabled(true);
                try {
                    List<Route> routes = get();
                    displaySearchResults(routes);
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, TrainSearchFrame.this, "search trains");
                }
            }
        };
        worker.execute();
        loading.showNonBlocking();
    }
    
    private void performRouteSearch() {
        Station sourceStation = (Station) cmbSourceStation.getSelectedItem();
        Station destStation = (Station) cmbDestinationStation.getSelectedItem();
        String journeyDateStr = txtJourneyDate.getText().trim();
        
        if (sourceStation == null || destStation == null) {
            JOptionPane.showMessageDialog(this, "Please select both source and destination stations.");
            return;
        }
        
        if (sourceStation.getId() == destStation.getId()) {
            JOptionPane.showMessageDialog(this, "Source and destination stations cannot be the same.");
            return;
        }
        
        if (journeyDateStr.equals("YYYY-MM-DD") || journeyDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid journey date (YYYY-MM-DD).");
            return;
        }
        
        try {
            Date journeyDate = Date.valueOf(journeyDateStr);
            this.currentSearchDate = journeyDate; // Store for availability lookup
            final int sourceId = sourceStation.getId();
            final int destId = destStation.getId();
            
            btnRouteSearch.setEnabled(false);
            com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Searching routes...");
            
            SwingWorker<List<Route>, Void> worker = new SwingWorker<List<Route>, Void>() {
                @Override
                protected List<Route> doInBackground() {
                    return trainController.searchRoutesByStations(sourceId, destId);
                }
                
                @Override
                protected void done() {
                    loading.close();
                    btnRouteSearch.setEnabled(true);
                    try {
                        List<Route> routes = get();
                        displaySearchResults(routes);
                    } catch (Exception e) {
                        com.railway.util.SwingWorkerUtils.handleWorkerError(e, TrainSearchFrame.this, "search routes");
                    }
                }
            };
            worker.execute();
            loading.showNonBlocking();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date format (YYYY-MM-DD).");
        }
    }
    
    private void displaySearchResults(List<Route> routes) {
        // Store results for refresh
        lastSearchResults = routes;
        
        tableModel.setRowCount(0);
        
        if (routes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trains found matching your criteria.");
            refreshTimer.stop();
            return;
        }
        
        // Start real-time refresh
        refreshTimer.start();
        
        for (Route route : routes) {
            // Get coach type availability for this route
            int[] coachAvailability = getCoachTypeAvailability(route.getTrainId());
            
            Object[] row = {
                route.getId(),
                route.getTrainNumber(),
                route.getTrainName(),
                route.getSourceStationName(),
                route.getDestinationStationName(),
                route.getDepartureTime(),
                route.getArrivalTime(),
                String.format("₹%.2f", route.getFare()),
                coachAvailability[0], // GENERAL
                coachAvailability[1], // SL
                coachAvailability[2], // AC_3TIER
                coachAvailability[3], // AC_2TIER
                coachAvailability[4]  // AC_1TIER
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Get availability by coach type for a train
     * Returns array: [GENERAL, SL, 3AC, 2AC, 1AC]
     */
    private int[] getCoachTypeAvailability(int trainId) {
        int[] availability = new int[5];
        
        if (seatAvailabilityService == null || currentSearchDate == null) {
            return availability;
        }
        
        try {
            availability[0] = seatAvailabilityService.getAvailableSeats(trainId, 
                com.railway.booking.backend.model.TrainSeat.CoachType.GENERAL, currentSearchDate);
            availability[1] = seatAvailabilityService.getAvailableSeats(trainId, 
                com.railway.booking.backend.model.TrainSeat.CoachType.SL, currentSearchDate);
            availability[2] = seatAvailabilityService.getAvailableSeats(trainId, 
                com.railway.booking.backend.model.TrainSeat.CoachType.AC_3TIER, currentSearchDate);
            availability[3] = seatAvailabilityService.getAvailableSeats(trainId, 
                com.railway.booking.backend.model.TrainSeat.CoachType.AC_2TIER, currentSearchDate);
            availability[4] = seatAvailabilityService.getAvailableSeats(trainId, 
                com.railway.booking.backend.model.TrainSeat.CoachType.AC_1TIER, currentSearchDate);
        } catch (Exception e) {
            // Return zeros if error
        }
        
        return availability;
    }
    
    private void bookSelectedTrain() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a train to book.");
            return;
        }
        
        final int routeId = (Integer) tableModel.getValueAt(selectedRow, 0);
        btnBookSelected.setEnabled(false);
        com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Loading route details...");
        
        SwingWorker<Route, Void> worker = new SwingWorker<Route, Void>() {
            @Override
            protected Route doInBackground() {
                return trainController.getRouteById(routeId);
            }
            
            @Override
            protected void done() {
                loading.close();
                btnBookSelected.setEnabled(true);
                try {
                    Route selectedRoute = get();
                    if (selectedRoute != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            new BookingFrame(user, selectedRoute).setVisible(true);
                        });
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, TrainSearchFrame.this, "load route details");
                }
            }
        };
        worker.execute();
        loading.showNonBlocking();
    }
    
    /**
     * Real-time refresh of seat availability display
     * Updates only the availability columns without re-running the full search
     */
    private void refreshAvailabilityDisplay() {
        if (lastSearchResults == null || lastSearchResults.isEmpty() || currentSearchDate == null) {
            return;
        }
        
        // Update availability for each row
        for (int i = 0; i < lastSearchResults.size() && i < tableModel.getRowCount(); i++) {
            Route route = lastSearchResults.get(i);
            int[] coachAvailability = getCoachTypeAvailability(route.getTrainId());
            
            // Update availability columns (indices 8-12: GEN, SL, 3AC, 2AC, 1AC)
            for (int j = 0; j < 5; j++) {
                tableModel.setValueAt(coachAvailability[j], i, 8 + j);
            }
        }
        
        // Visual feedback - briefly highlight the table
        resultsTable.repaint();
    }
    
    private void goBack() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new MainFrame(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user)).setVisible(true);
        });
    }
}
