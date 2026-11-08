package com.railway.view;

import com.railway.controller.RegistrationController;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.model.User.UserType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.railway.util.ApplicationContextProvider;

public class RegistrationFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtEmail;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtPhone;
    private JTextArea txtAddress;
    private JComboBox<UserType> cmbUserType;
    private JButton btnRegister;
    private JButton btnCancel;
    private RegistrationController registrationController;

    public RegistrationFrame() {
        registrationController = ApplicationContextProvider.getBean(RegistrationController.class);

        setTitle("User Registration - Railway Booking System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeComponents();
        layoutComponents();
        registerEventHandlers();
    }

    private void initializeComponents() {
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtConfirmPassword = new JPasswordField(20);
        txtEmail = new JTextField(20);
        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtPhone = new JTextField(20);
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);

        cmbUserType = new JComboBox<>(UserType.values());
        cmbUserType.setSelectedItem(UserType.USER); // Default to user

        btnRegister = new JButton("Register");
        btnCancel = new JButton("Cancel");

        // Style the buttons
        btnRegister.setPreferredSize(new Dimension(100, 30));
        btnCancel.setPreferredSize(new Dimension(100, 30));
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Confirm Password *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtConfirmPassword, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtEmail, gbc);

        // First Name
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("First Name *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtFirstName, gbc);

        // Last Name
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Last Name *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtLastName, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPhone, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(txtAddress), gbc);

        // User Type
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(new JLabel("User Type:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbUserType, gbc);

        // Buttons
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnCancel);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add some padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        getContentPane().add(mainPanel);
    }

    private void registerEventHandlers() {
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    User newUser = createUserFromInput();
                    String result = registrationController.registerUser(newUser);
                    boolean success = "SUCCESS".equals(result);

                    if (success) {
                        JOptionPane.showMessageDialog(RegistrationFrame.this,
                                "Registration successful! You can now login with your credentials.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        // Show the specific reason from the service layer
                        String errorMsg = (result != null && !result.isEmpty())
                                ? result
                                : "Registration failed. Username or email might already exist.";
                        JOptionPane.showMessageDialog(RegistrationFrame.this,
                                errorMsg,
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    private boolean validateInput() {
        // Check required fields
        if (txtUsername.getText().trim().isEmpty()) {
            showValidationError("Username is required.");
            txtUsername.requestFocus();
            return false;
        }

        if (txtPassword.getPassword().length == 0) {
            showValidationError("Password is required.");
            txtPassword.requestFocus();
            return false;
        }

        if (txtConfirmPassword.getPassword().length == 0) {
            showValidationError("Please confirm your password.");
            txtConfirmPassword.requestFocus();
            return false;
        }

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

        // Check password match
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        if (!password.equals(confirmPassword)) {
            showValidationError("Passwords do not match.");
            txtPassword.requestFocus();
            return false;
        }

        // Check password length
        if (password.length() < 6) {
            showValidationError("Password must be at least 6 characters long.");
            txtPassword.requestFocus();
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

    private User createUserFromInput() {
        User user = new User();
        user.setUsername(txtUsername.getText().trim());
        user.setPassword(new String(txtPassword.getPassword()));
        user.setEmail(txtEmail.getText().trim());
        user.setFirstName(txtFirstName.getText().trim());
        user.setLastName(txtLastName.getText().trim());
        user.setPhone(txtPhone.getText().trim());
        user.setAddress(txtAddress.getText().trim());
        user.setUserType((UserType) cmbUserType.getSelectedItem());

        return user;
    }

    // For testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationFrame().setVisible(true));
    }
}
