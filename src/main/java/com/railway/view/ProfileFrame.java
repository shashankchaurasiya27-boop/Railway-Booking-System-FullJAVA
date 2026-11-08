package com.railway.view;

import com.railway.controller.UserController;
import com.railway.booking.backend.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.railway.util.ApplicationContextProvider;

public class ProfileFrame extends JFrame {
    private User user;
    private UserController userController;
    
    // Components
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtPhone;
    private JTextArea txtAddress;
    private JLabel lblUserType;
    private JLabel lblCreatedAt;
    
    // Buttons
    private JButton btnEdit;
    private JButton btnSave;
    private JButton btnCancel;
    private JButton btnChangePassword;
    private JButton btnBack;
    
    private boolean isEditing = false;
    
    public ProfileFrame(User user) {
        this.user = user;
        this.userController = ApplicationContextProvider.getBean(UserController.class);
        
        setTitle("My Profile - Railway Booking System");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        loadUserData();
    }
    
    private void initializeComponents() {
        // Text fields
        txtUsername = new JTextField(20);
        txtEmail = new JTextField(20);
        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtPhone = new JTextField(20);
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        
        // Labels
        lblUserType = new JLabel();
        lblCreatedAt = new JLabel();
        
        // Buttons
        btnEdit = new JButton("Edit Profile");
        btnSave = new JButton("Save Changes");
        btnCancel = new JButton("Cancel");
        btnChangePassword = new JButton("Change Password");
        btnBack = new JButton("Back to Main Menu");
        
        // Style buttons
        btnEdit.setPreferredSize(new Dimension(120, 30));
        btnSave.setPreferredSize(new Dimension(120, 30));
        btnCancel.setPreferredSize(new Dimension(100, 30));
        btnChangePassword.setPreferredSize(new Dimension(140, 30));
        btnBack.setPreferredSize(new Dimension(150, 30));
        
        // Initially disable save and cancel
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);
    }
    
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername.setEditable(false);
        txtUsername.setBackground(Color.LIGHT_GRAY);
        formPanel.add(txtUsername, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtEmail, gbc);
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("First Name *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtFirstName, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Last Name *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtLastName, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPhone, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(txtAddress), gbc);
        
        // User Type (read-only)
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("User Type:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lblUserType, gbc);
        
        // Created At (read-only)
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Member Since:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lblCreatedAt, gbc);
        
        // Buttons
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnChangePassword);
        buttonPanel.add(btnBack);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(mainPanel);
    }
    
    private void registerEventHandlers() {
        btnEdit.addActionListener(e -> enableEditing());
        btnSave.addActionListener(e -> saveChanges());
        btnCancel.addActionListener(e -> cancelEditing());
        btnChangePassword.addActionListener(e -> changePassword());
        btnBack.addActionListener(e -> goBack());
    }
    
    private void loadUserData() {
        txtUsername.setText(user.getUsername());
        txtEmail.setText(user.getEmail());
        txtFirstName.setText(user.getFirstName());
        txtLastName.setText(user.getLastName());
        txtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        txtAddress.setText(user.getAddress() != null ? user.getAddress() : "");
        lblUserType.setText(user.getUserType().name());
        lblCreatedAt.setText(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "Unknown");
    }
    
    private void enableEditing() {
        isEditing = true;
        txtEmail.setEditable(true);
        txtFirstName.setEditable(true);
        txtLastName.setEditable(true);
        txtPhone.setEditable(true);
        txtAddress.setEditable(true);
        
        txtEmail.setBackground(Color.WHITE);
        txtFirstName.setBackground(Color.WHITE);
        txtLastName.setBackground(Color.WHITE);
        txtPhone.setBackground(Color.WHITE);
        txtAddress.setBackground(Color.WHITE);
        
        btnEdit.setEnabled(false);
        btnSave.setEnabled(true);
        btnCancel.setEnabled(true);
    }
    
    private void cancelEditing() {
        isEditing = false;
        loadUserData();
        disableEditing();
    }
    
    private void disableEditing() {
        txtEmail.setEditable(false);
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtPhone.setEditable(false);
        txtAddress.setEditable(false);
        
        txtEmail.setBackground(Color.LIGHT_GRAY);
        txtFirstName.setBackground(Color.LIGHT_GRAY);
        txtLastName.setBackground(Color.LIGHT_GRAY);
        txtPhone.setBackground(Color.LIGHT_GRAY);
        txtAddress.setBackground(Color.LIGHT_GRAY);
        
        btnEdit.setEnabled(true);
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);
    }
    
    private void saveChanges() {
        if (!validateInput()) {
            return;
        }
        
        // Update user object
        user.setEmail(txtEmail.getText().trim());
        user.setFirstName(txtFirstName.getText().trim());
        user.setLastName(txtLastName.getText().trim());
        user.setPhone(txtPhone.getText().trim());
        user.setAddress(txtAddress.getText().trim());
        
        btnSave.setEnabled(false);
        com.railway.util.LoadingDialog loading = new com.railway.util.LoadingDialog(this, "Updating profile...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return userController.updateUser(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user), user);
            }
            
            @Override
            protected void done() {
                loading.close();
                btnSave.setEnabled(true);
                try {
                    Boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ProfileFrame.this, "Profile updated successfully!");
                        isEditing = false;
                        disableEditing();
                    } else {
                        JOptionPane.showMessageDialog(ProfileFrame.this, "Failed to update profile. Please try again.");
                    }
                } catch (Exception e) {
                    com.railway.util.SwingWorkerUtils.handleWorkerError(e, ProfileFrame.this, "update profile");
                }
            }
        };
        worker.execute();
        loading.showNonBlocking();
    }
    
    private boolean validateInput() {
        if (txtEmail.getText().trim().isEmpty()) {
            showValidationError("Email is required.");
            txtEmail.requestFocus();
            return false;
        }
        
        if (txtFirstName.getText().trim().isEmpty()) {
            showValidationError("First name is required.");
            txtFirstName.requestFocus();
            return false;
        }
        
        if (txtLastName.getText().trim().isEmpty()) {
            showValidationError("Last name is required.");
            txtLastName.requestFocus();
            return false;
        }
        
        // Basic email validation
        String email = txtEmail.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            showValidationError("Please enter a valid email address.");
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
    
    private void changePassword() {
        JPasswordField currentPassword = new JPasswordField(20);
        JPasswordField newPassword = new JPasswordField(20);
        JPasswordField confirmPassword = new JPasswordField(20);
        
        JPanel passwordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        passwordPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        passwordPanel.add(currentPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        passwordPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        passwordPanel.add(newPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        passwordPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        passwordPanel.add(confirmPassword, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, passwordPanel, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String currentPass = new String(currentPassword.getPassword());
            String newPass = new String(newPassword.getPassword());
            String confirmPass = new String(confirmPassword.getPassword());
            
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.");
                return;
            }
            
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.");
                return;
            }
            
            boolean success = userController.changePassword(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user), user.getUserId(), currentPass, newPass);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change password. Please check your current password.");
            }
        }
    }
    
    private void goBack() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new MainFrame(com.railway.booking.backend.util.SessionHelper.createSessionFromUser(user)).setVisible(true);
        });
    }
}

