package com.railway.util;

import javax.swing.*;
import java.awt.*;

/**
 * A simple loading dialog to show during background operations
 */
public class LoadingDialog extends JDialog {
    private JLabel messageLabel;
    private JProgressBar progressBar;
    
    public LoadingDialog(JFrame parent, String message) {
        super(parent, "Please Wait", true);
        initComponents(message);
    }
    
    private void initComponents(String message) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        
        // Message panel
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messagePanel.add(messageLabel);
        
        // Progress bar (indeterminate)
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 25));
        
        // Add components
        add(messagePanel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
        
        // Set size and position
        setSize(350, 120);
        setLocationRelativeTo(getParent());
    }
    
    public void setMessage(String message) {
        messageLabel.setText(message);
    }
    
    /**
     * Show the loading dialog in a non-blocking way
     */
    public void showNonBlocking() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
    
    /**
     * Close the loading dialog
     */
    public void close() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }
}
