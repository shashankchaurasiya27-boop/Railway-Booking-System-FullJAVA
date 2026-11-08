package com.railway.view;

import com.railway.controller.AdminController;
import com.railway.controller.TrainController;
import com.railway.controller.UserController;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.Train;
import com.railway.booking.backend.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import com.railway.util.ApplicationContextProvider;

public class AdminFrame extends JFrame {
    private User user;
    private AdminController adminController;
    private TrainController trainController;
    private UserController userController;
    
    // Main components
    private JTabbedPane tabbedPane;
    private JButton btnBack;
    
    // Train management components
    private JTable trainsTable;
    private DefaultTableModel trainsTableModel;
    private JButton btnAddTrain;
    private JButton btnEditTrain;
    private JButton btnDeleteTrain;
    
    // Bookings management components
    private JTable bookingsTable;
    private DefaultTableModel bookingsTableModel;
    private JButton btnRefreshBookings;
    private JComboBox<String> cmbBookingStatusFilter;
    
    // Users management components
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton btnRefreshUsers;
    private JButton btnEditUser;
    
    public AdminFrame(User user) {
        this(user, 0); // Default to first tab (Train Management)
    }
    
    public AdminFrame(User user, int initialTab) {
        this.user = user;
        this.adminController = ApplicationContextProvider.getBean(AdminController.class);
        this.trainController = new TrainController();
        this.userController = ApplicationContextProvider.getBean(UserController.class);
        
        setTitle("Admin Panel - Railway Booking System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        loadData();
        
        // Set initial tab
        if (initialTab >= 0 && initialTab < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(initialTab);
        }
    }
    
    private void initializeComponents() {
        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Initialize train management components
        String[] trainColumns = {"ID", "Train Number", "Train Name", "Type", "Total Seats", "Status"};
        trainsTableModel = new DefaultTableModel(trainColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        trainsTable = new JTable(trainsTableModel);
        trainsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        btnAddTrain = new JButton("Add Train");
        btnEditTrain = new JButton("Edit Train");
        btnDeleteTrain = new JButton("Delete Train");
        
        // Initialize bookings management components
        String[] bookingColumns = {"Booking ID", "User", "Train", "Route", "Journey Date", "Passengers", "Fare", "Status"};
        bookingsTableModel = new DefaultTableModel(bookingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable = new JTable(bookingsTableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        btnRefreshBookings = new JButton("Refresh");
        cmbBookingStatusFilter = new JComboBox<>(new String[]{"All", "PENDING", "CONFIRMED", "CANCELLED", "WAITING"});
        
        // Initialize users management components
        String[] userColumns = {"ID", "Username", "Email", "Name", "Phone", "User Type", "Created At"};
        usersTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        btnRefreshUsers = new JButton("Refresh");
        btnEditUser = new JButton("Edit User");
        
        // Back button
        btnBack = new JButton("Back to Main Menu");
        
        // Style buttons
        btnAddTrain.setPreferredSize(new Dimension(100, 30));
        btnEditTrain.setPreferredSize(new Dimension(100, 30));
        btnDeleteTrain.setPreferredSize(new Dimension(100, 30));
        btnRefreshBookings.setPreferredSize(new Dimension(100, 30));
        btnRefreshUsers.setPreferredSize(new Dimension(100, 30));
        btnEditUser.setPreferredSize(new Dimension(100, 30));
        btnBack.setPreferredSize(new Dimension(150, 30));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create tabs
        tabbedPane.addTab("Train Management", createTrainManagementTab());
        tabbedPane.addTab("Bookings Management", createBookingsManagementTab());
        tabbedPane.addTab("Users Management", createUsersManagementTab());
        
        // Back button panel
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.add(btnBack);
        
        // Add components
        add(tabbedPane, BorderLayout.CENTER);
        add(backPanel, BorderLayout.SOUTH);
        
        // Add padding
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private JPanel createTrainManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Trains"));
        tablePanel.add(new JScrollPane(trainsTable), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnAddTrain);
        buttonPanel.add(btnEditTrain);
        buttonPanel.add(btnDeleteTrain);
        
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBookingsManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(cmbBookingStatusFilter);
        filterPanel.add(btnRefreshBookings);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("All Bookings"));
        tablePanel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUsersManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnRefreshUsers);
        buttonPanel.add(btnEditUser);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("All Users"));
        tablePanel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void registerEventHandlers() {
        btnBack.addActionListener(e -> goBack());
        
        // Train management events
        btnAddTrain.addActionListener(e -> addTrain());
        btnEditTrain.addActionListener(e -> editTrain());
        btnDeleteTrain.addActionListener(e -> deleteTrain());
        
        // Bookings management events
        btnRefreshBookings.addActionListener(e -> loadBookings());
        cmbBookingStatusFilter.addActionListener(e -> loadBookings());
        
        // Users management events
        btnRefreshUsers.addActionListener(e -> loadUsers());
        btnEditUser.addActionListener(e -> editUser());
    }
    
    private void loadData() {
        loadTrains();
        loadBookings();
        loadUsers();
    }
    
    private void loadTrains() {
        SwingWorker<List<Train>, Void> worker = new SwingWorker<List<Train>, Void>() {
            @Override
            protected List<Train> doInBackground() {
                return trainController.getAllTrains();
            }
            
            @Override
            protected void done() {
                try {
                    List<Train> trains = get();
                    trainsTableModel.setRowCount(0);
                    for (Train train : trains) {
                        Object[] row = {train.getTrainId(), train.getTrainNumber(), train.getTrainName(),
                                      train.getTrainType().name(), train.getTotalSeats(), train.getStatus().name()};
                        trainsTableModel.addRow(row);
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, AdminFrame.this, "load trains");
                }
            }
        };
        worker.execute();
    }
    
    private void loadBookings() {
        System.out.println("DEBUG: loadBookings() called");
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() {
                System.out.println("DEBUG: doInBackground() - calling adminController.getAllBookings()");
                List<Booking> bookings = adminController.getAllBookings();
                System.out.println("DEBUG: Retrieved " + (bookings != null ? bookings.size() : 0) + " bookings");
                return bookings;
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    System.out.println("DEBUG: done() - got " + (bookings != null ? bookings.size() : 0) + " bookings");
                    String selectedStatus = (String) cmbBookingStatusFilter.getSelectedItem();
                    if (!"All".equals(selectedStatus)) {
                        bookings = bookings.stream()
                            .filter(booking -> booking.getBookingStatus().name().equals(selectedStatus))
                            .collect(java.util.stream.Collectors.toList());
                    }
                    bookingsTableModel.setRowCount(0);
                    System.out.println("DEBUG: Adding " + bookings.size() + " rows to table");
                    for (Booking booking : bookings) {
                        Object[] row = {booking.getBookingId(), booking.getUserName(), booking.getTrainName(),
                                      booking.getRouteDescription(), booking.getJourneyDate(), booking.getNumberOfPassengers(),
                                      String.format("₹%.2f", booking.getTotalFare()), booking.getBookingStatus().name()};
                        bookingsTableModel.addRow(row);
                        System.out.println("DEBUG: Added booking: " + booking.getBookingId());
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG: Exception in loadBookings: " + e.getMessage());
                    e.printStackTrace();
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, AdminFrame.this, "load bookings");
                }
            }
        };
        worker.execute();
    }
    
    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return userController.getAllUsers(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user));
            }
            
            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    usersTableModel.setRowCount(0);
                    for (User user : users) {
                        Object[] row = {user.getUserId(), user.getUsername(), user.getEmail(),
                                      user.getFirstName() + " " + user.getLastName(),
                                      user.getPhone() != null ? user.getPhone() : "",
                                      user.getUserType().name(),
                                      user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""};
                        usersTableModel.addRow(row);
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, AdminFrame.this, "load users");
                }
            }
        };
        worker.execute();
    }
    
    private void addTrain() {
        // Create add train dialog
        JTextField txtTrainNumber = new JTextField(15);
        JTextField txtTrainName = new JTextField(15);
        JComboBox<String> cmbTrainType = new JComboBox<>(new String[]{"EXPRESS", "PASSENGER", "SUPERFAST"});
        JTextField txtTotalSeats = new JTextField(10);
        
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        addPanel.add(new JLabel("Train Number:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtTrainNumber, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        addPanel.add(new JLabel("Train Name:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtTrainName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        addPanel.add(new JLabel("Train Type:"), gbc);
        gbc.gridx = 1;
        addPanel.add(cmbTrainType, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        addPanel.add(new JLabel("Total Seats:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtTotalSeats, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, addPanel, "Add New Train", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                Train newTrain = new Train();
                newTrain.setTrainNumber(txtTrainNumber.getText().trim());
                newTrain.setTrainName(txtTrainName.getText().trim());
                newTrain.setTrainType(Train.TrainType.valueOf((String) cmbTrainType.getSelectedItem()));
                newTrain.setTotalSeats(Integer.parseInt(txtTotalSeats.getText().trim()));
                newTrain.setStatus(Train.TrainStatus.ACTIVE);
                
                btnAddTrain.setEnabled(false);
                com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Adding train...");
                
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() {
                        return trainController.addTrain(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user), newTrain);
                    }
                    
                    @Override
                    protected void done() {
                        loading.close();
                        btnAddTrain.setEnabled(true);
                        try {
                            Boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(AdminFrame.this, "Train added successfully!");
                                loadTrains();
                            } else {
                                JOptionPane.showMessageDialog(AdminFrame.this, "Failed to add train. Train number might already exist.");
                            }
                        } catch (Exception e) {
                            com.railway.util.SwingWorkerUtils.handleWorkerError(e, AdminFrame.this, "add train");
                        }
                    }
                };
                worker.execute();
                loading.showNonBlocking();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for total seats.");
            }
        }
    }
    
    private void editTrain() {
        int selectedRow = trainsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a train to edit.");
            return;
        }
        
        int trainId = (Integer) trainsTableModel.getValueAt(selectedRow, 0);
        
        btnEditTrain.setEnabled(false);
        com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Loading train details...");
        
        SwingWorker<Train, Void> worker = new SwingWorker<Train, Void>() {
            @Override
            protected Train doInBackground() {
                return trainController.getTrainById(trainId);
            }
            
            @Override
            protected void done() {
                loading.close();
                btnEditTrain.setEnabled(true);
                try {
                    Train train = get();
                    if (train != null) {
                        JTextField txtTrainNumber = new JTextField(train.getTrainNumber(), 15);
                        JTextField txtTrainName = new JTextField(train.getTrainName(), 15);
                        JComboBox<String> cmbTrainType = new JComboBox<>(new String[]{"EXPRESS", "PASSENGER", "SUPERFAST"});
                        cmbTrainType.setSelectedItem(train.getTrainType().name());
                        JTextField txtTotalSeats = new JTextField(String.valueOf(train.getTotalSeats()), 10);
                        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
                        cmbStatus.setSelectedItem(train.getStatus().name());
                        
                        JPanel editPanel = new JPanel(new GridBagLayout());
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.insets = new Insets(5, 5, 5, 5);
                        gbc.anchor = GridBagConstraints.WEST;
                        
                        gbc.gridx = 0; gbc.gridy = 0;
                        editPanel.add(new JLabel("Train Number:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtTrainNumber, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 1;
                        editPanel.add(new JLabel("Train Name:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtTrainName, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 2;
                        editPanel.add(new JLabel("Train Type:"), gbc);
                        gbc.gridx = 1; editPanel.add(cmbTrainType, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 3;
                        editPanel.add(new JLabel("Total Seats:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtTotalSeats, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 4;
                        editPanel.add(new JLabel("Status:"), gbc);
                        gbc.gridx = 1; editPanel.add(cmbStatus, gbc);
                        
                        int result = JOptionPane.showConfirmDialog(AdminFrame.this, editPanel, "Edit Train", JOptionPane.OK_CANCEL_OPTION);
                        
                        if (result == JOptionPane.OK_OPTION) {
                            try {
                                train.setTrainNumber(txtTrainNumber.getText().trim());
                                train.setTrainName(txtTrainName.getText().trim());
                                train.setTrainType(Train.TrainType.valueOf((String) cmbTrainType.getSelectedItem()));
                                train.setTotalSeats(Integer.parseInt(txtTotalSeats.getText().trim()));
                                train.setStatus(Train.TrainStatus.valueOf((String) cmbStatus.getSelectedItem()));
                                
                                com.railway.util.LoadingDialog updateLoading = new com.railway.util.LoadingDialog(AdminFrame.this, "Updating train...");
                                
                                SwingWorker<Boolean, Void> updateWorker = new SwingWorker<Boolean, Void>() {
                                    @Override
                                    protected Boolean doInBackground() {
                                        return trainController.updateTrain(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user), train);
                                    }
                                    
                                    @Override
                                    protected void done() {
                                        updateLoading.close();
                                        try {
                                            Boolean success = get();
                                            if (success) {
                                                JOptionPane.showMessageDialog(AdminFrame.this, "Train updated successfully!");
                                                loadTrains();
                                            } else {
                                                JOptionPane.showMessageDialog(AdminFrame.this, "Failed to update train. Train number might already exist.");
                                            }
                                        } catch (Exception ex) {
                                            com.railway.util.SwingWorkerUtils.handleWorkerError(ex, AdminFrame.this, "update train");
                                        }
                                    }
                                };
                                updateWorker.execute();
                                updateLoading.showNonBlocking();
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(AdminFrame.this, "Please enter valid numbers for total seats.");
                            }
                        }
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, AdminFrame.this, "load train details");
                }
            }
        };
        worker.execute();
        loading.showNonBlocking();
    }
    
    private void deleteTrain() {
        int selectedRow = trainsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a train to delete.");
            return;
        }
        
        int trainId = (Integer) trainsTableModel.getValueAt(selectedRow, 0);
        String trainName = (String) trainsTableModel.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete train: " + trainName + "?", 
            "Delete Train", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            btnDeleteTrain.setEnabled(false);
            com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Deleting train...");
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return trainController.deleteTrain(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user), trainId);
                }
                
                @Override
                protected void done() {
                    loading.close();
                    btnDeleteTrain.setEnabled(true);
                    try {
                        Boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(AdminFrame.this, "Train deleted successfully!");
                            loadTrains();
                        } else {
                            JOptionPane.showMessageDialog(AdminFrame.this, "Failed to delete train. It might have associated bookings.");
                        }
                    } catch (Exception e) {
                        com.railway.util.SwingWorkerUtils.handleWorkerError(e, AdminFrame.this, "delete train");
                    }
                }
            };
            worker.execute();
            loading.showNonBlocking();
        }
    }
    
    private void editUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }
        
        int userId = (Integer) usersTableModel.getValueAt(selectedRow, 0);
        
        btnEditUser.setEnabled(false);
        com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Loading user details...");
        
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return userController.getUserById(userId);
            }
            
            @Override
            protected void done() {
                loading.close();
                btnEditUser.setEnabled(true);
                try {
                    User userToEdit = get();
                    if (userToEdit != null) {
                        JTextField txtFirstName = new JTextField(userToEdit.getFirstName(), 15);
                        JTextField txtLastName = new JTextField(userToEdit.getLastName(), 15);
                        JTextField txtEmail = new JTextField(userToEdit.getEmail(), 20);
                        JTextField txtPhone = new JTextField(userToEdit.getPhone() != null ? userToEdit.getPhone() : "", 15);
                        JTextField txtAddress = new JTextField(userToEdit.getAddress() != null ? userToEdit.getAddress() : "", 25);
                        
                        JPanel editPanel = new JPanel(new GridBagLayout());
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.insets = new Insets(5, 5, 5, 5);
                        gbc.anchor = GridBagConstraints.WEST;
                        
                        gbc.gridx = 0; gbc.gridy = 0;
                        editPanel.add(new JLabel("First Name:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtFirstName, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 1;
                        editPanel.add(new JLabel("Last Name:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtLastName, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 2;
                        editPanel.add(new JLabel("Email:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtEmail, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 3;
                        editPanel.add(new JLabel("Phone:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtPhone, gbc);
                        
                        gbc.gridx = 0; gbc.gridy = 4;
                        editPanel.add(new JLabel("Address:"), gbc);
                        gbc.gridx = 1; editPanel.add(txtAddress, gbc);
                        
                        int result = JOptionPane.showConfirmDialog(AdminFrame.this, editPanel, "Edit User", JOptionPane.OK_CANCEL_OPTION);
                        
                        if (result == JOptionPane.OK_OPTION) {
                            userToEdit.setFirstName(txtFirstName.getText().trim());
                            userToEdit.setLastName(txtLastName.getText().trim());
                            userToEdit.setEmail(txtEmail.getText().trim());
                            userToEdit.setPhone(txtPhone.getText().trim());
                            userToEdit.setAddress(txtAddress.getText().trim());
                            
                            com.railway.util.LoadingDialog updateLoading = new com.railway.util.LoadingDialog(AdminFrame.this, "Updating user...");
                            
                            SwingWorker<Boolean, Void> updateWorker = new SwingWorker<Boolean, Void>() {
                                @Override
                                protected Boolean doInBackground() {
                                    return userController.updateUser(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user), userToEdit);
                                }
                                
                                @Override
                                protected void done() {
                                    updateLoading.close();
                                    try {
                                        Boolean success = get();
                                        if (success) {
                                            JOptionPane.showMessageDialog(AdminFrame.this, "User updated successfully!");
                                            loadUsers();
                                        } else {
                                            JOptionPane.showMessageDialog(AdminFrame.this, "Failed to update user.");
                                        }
                                    } catch (Exception ex) {
                                        com.railway.util.SwingWorkerUtils.handleWorkerError(ex, AdminFrame.this, "update user");
                                    }
                                }
                            };
                            updateWorker.execute();
                            updateLoading.showNonBlocking();
                        }
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, AdminFrame.this, "load user details");
                }
            }
        };
        worker.execute();
        loading.showNonBlocking();
    }
    
    private void goBack() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new MainFrame(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user)).setVisible(true);
        });
    }
}