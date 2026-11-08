package com.railway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Utility class for SwingWorker operations
 */
public class SwingWorkerUtils {
    private static final Logger logger = LoggerFactory.getLogger(SwingWorkerUtils.class);
    
    /**
     * Show a loading dialog
     */
    public static LoadingDialog showLoading(JFrame parent, String message) {
        LoadingDialog dialog = new LoadingDialog(parent, message);
        // Show in separate thread to avoid blocking
        new Thread(() -> dialog.setVisible(true)).start();
        return dialog;
    }
    
    /**
     * Handle worker errors with user-friendly messages
     */
    public static void handleWorkerError(Exception e, JFrame parent, String operation) {
        logger.error("Error during {}: {}", operation, e.getMessage(), e);
        
        String userMessage = "An error occurred during " + operation + ".\nPlease try again.";
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                parent,
                userMessage,
                "Operation Error",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * Disable a button during operation
     */
    public static void disableButton(JButton button) {
        SwingUtilities.invokeLater(() -> button.setEnabled(false));
    }
    
    /**
     * Enable a button after operation
     */
    public static void enableButton(JButton button) {
        SwingUtilities.invokeLater(() -> button.setEnabled(true));
    }
    
    /**
     * Show success message
     */
    public static void showSuccess(JFrame parent, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                parent,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
    
    /**
     * Show error message
     */
    public static void showError(JFrame parent, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                parent,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
}
