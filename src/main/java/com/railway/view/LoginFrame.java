package com.railway.view;

import com.railway.controller.LoginController;
import com.railway.booking.backend.model.User;

import javax.swing.*;
import java.awt.*;
import com.railway.util.ApplicationContextProvider;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private LoginController loginController;

    public LoginFrame() {
        loginController = ApplicationContextProvider.getBean(LoginController.class);

        setTitle("Login - Railway Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 300); // Increased window size
        setLocationRelativeTo(null);
        setResizable(false);

        initializeComponents();
        layoutComponents();
        registerEventHandlers();
    }

    private void initializeComponents() {
        txtUsername = new JTextField(20); // Set preferred columns
        txtPassword = new JPasswordField(20); // Set preferred columns
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");
        
        // Set preferred sizes for better visibility
        txtUsername.setPreferredSize(new Dimension(200, 30));
        txtPassword.setPreferredSize(new Dimension(200, 30));
        btnLogin.setPreferredSize(new Dimension(200, 35));
        btnRegister.setPreferredSize(new Dimension(200, 35));
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12); // Increased padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow horizontal expansion

        // Username row
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtUsername, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtPassword, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 2; 
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnLogin, gbc);

        // Register button
        gbc.gridy = 3;
        panel.add(btnRegister, gbc);

        // Add padding around the panel
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    private void registerEventHandlers() {
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.");
                return;
            }
            
            // Disable login button during operation
            btnLogin.setEnabled(false);
            
            // Show loading dialog
            com.railway.util.LoadingDialog loadingDialog = 
                new com.railway.util.LoadingDialog(this, "Logging in...");
            
            // Execute login in background thread
            SwingWorker<com.railway.booking.backend.model.Session, Void> loginWorker = 
                new SwingWorker<com.railway.booking.backend.model.Session, Void>() {
                
                @Override
                protected com.railway.booking.backend.model.Session doInBackground() throws Exception {
                    // This runs in background thread - safe for DB operations
                    return loginController.login(username, password);
                }
                
                @Override
                protected void done() {
                    // This runs on EDT - safe for UI updates
                    loadingDialog.close();
                    btnLogin.setEnabled(true);
                    
                    try {
                        com.railway.booking.backend.model.Session session = get();
                        if (session != null) {
                            // Successful login - open main frame
                            SwingUtilities.invokeLater(() -> {
                                new MainFrame(session).setVisible(true);
                                dispose();
                            });
                        } else {
                            // Invalid credentials
                            JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Invalid username or password.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception ex) {
                        // Handle any errors
                        com.railway.util.SwingWorkerUtils.handleWorkerError(
                            ex, LoginFrame.this, "login"
                        );
                    }
                }
            };
            
            // Start the worker and show loading dialog
            loginWorker.execute();
            loadingDialog.showNonBlocking();
        });

        btnRegister.addActionListener(e -> {
            // Open registration frame
            SwingUtilities.invokeLater(() -> {
                new RegistrationFrame().setVisible(true);
            });
        });
    }

    // For quick testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
