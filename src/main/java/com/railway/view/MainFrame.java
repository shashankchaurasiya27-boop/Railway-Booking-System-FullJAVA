package com.railway.view;

import com.railway.booking.backend.model.Session;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private Session session;
    private User user;
    private JPanel mainPanel;
    private JPanel contentPanel;

    public MainFrame(Session session) {
        if (!validateSession(session)) {
            return; // Session invalid, frame won't open
        }
        
        this.session = session;
        this.user = getUserFromSession(session);
        
        setTitle("Railway Booking System - Main Menu (" + user.getFullName() + ")");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        
        logger.info("MainFrame opened for user: {}", user.getUsername());
    }
    
    /**
     * Validate session and show error if invalid
     */
    private boolean validateSession(Session session) {
        if (session == null) {
            logger.warn("MainFrame: null session provided");
            JOptionPane.showMessageDialog(null, "Invalid session. Please login again.");
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            return false;
        }
        
        Session validSession = SessionManager.validateSession(session.getSessionId());
        if (validSession == null) {
            logger.warn("MainFrame: session validation failed for user: {}", session.getUsername());
            JOptionPane.showMessageDialog(null, "Session expired. Please login again.");
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            return false;
        }
        
        return true;
    }
    
    /**
     * Get User object from Session for backward compatibility
     */
    private User getUserFromSession(Session session) {
        User user = new User();
        user.setUserId(session.getUserId());
        user.setUsername(session.getUsername());
        user.setUserType(session.getUserType());
        // Note: Other user fields would need to be loaded from DB if needed
        return user;
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        contentPanel = new JPanel(new BorderLayout());
    }

    private void layoutComponents() {
        // Create menu bar
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);

        // Create sidebar with navigation buttons
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Create main content area
        JPanel welcomePanel = createWelcomePanel();
        contentPanel.add(welcomePanel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Create status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Booking Menu
        JMenu bookingMenu = new JMenu("Booking");
        JMenuItem newBookingItem = new JMenuItem("New Booking");
        JMenuItem viewBookingsItem = new JMenuItem("View My Bookings");
        bookingMenu.add(newBookingItem);
        bookingMenu.add(viewBookingsItem);

        // Admin Menu (only for admin users)
        if (user.getUserType() == User.UserType.ADMIN) {
            JMenu adminMenu = new JMenu("Admin");
            JMenuItem manageTrainsItem = new JMenuItem("Manage Trains");
            JMenuItem manageUsersItem = new JMenuItem("Manage Users");
            JMenuItem viewAllBookingsItem = new JMenuItem("View All Bookings");
            adminMenu.add(manageTrainsItem);
            adminMenu.add(manageUsersItem);
            adminMenu.add(viewAllBookingsItem);
            menuBar.add(adminMenu);
        }

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(bookingMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createTitledBorder("Navigation"));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(240, 240, 240));

        // User info
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel userLabel = new JLabel("<html><b>" + user.getFullName() + "</b><br>" + 
                                    user.getUserType().name() + "</html>");
        userInfoPanel.add(userLabel, BorderLayout.CENTER);
        sidebar.add(userInfoPanel);

        sidebar.add(Box.createVerticalStrut(20));

        // Navigation buttons
        JButton dashboardBtn = new JButton("📊 Dashboard");
        JButton bookTicketBtn = new JButton("🎫 Book Ticket");
        JButton myBookingsBtn = new JButton("📋 My Bookings");
        JButton profileBtn = new JButton("👤 My Profile");

        // Admin buttons
        if (user.getUserType() == User.UserType.ADMIN) {
            JButton manageTrainsBtn = new JButton("🚂 Manage Trains");
            JButton manageUsersBtn = new JButton("👥 Manage Users");
            JButton reportsBtn = new JButton("📈 Reports");
            
            sidebar.add(manageTrainsBtn);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(manageUsersBtn);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(reportsBtn);
            sidebar.add(Box.createVerticalStrut(20));
        }

        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(bookTicketBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(myBookingsBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(profileBtn);

        // Style buttons
        Component[] components = sidebar.getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.setMaximumSize(new Dimension(180, 40));
                button.setPreferredSize(new Dimension(180, 40));
            }
        }

        return sidebar;
    }

    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to Railway Booking System!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setForeground(new Color(0, 100, 200));

        // Quick actions panel
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        JButton quickBookBtn = new JButton("<html><center><b>Book New Ticket</b><br>Search and book train tickets</center></html>");
        JButton quickViewBtn = new JButton("<html><center><b>View Bookings</b><br>Check your existing bookings</center></html>");
        JButton quickProfileBtn = new JButton("<html><center><b>Update Profile</b><br>Manage your account details</center></html>");
        JButton quickHelpBtn = new JButton("<html><center><b>Help & Support</b><br>Get assistance and FAQs</center></html>");

        // Style quick action buttons
        JButton[] quickButtons = {quickBookBtn, quickViewBtn, quickProfileBtn, quickHelpBtn};
        for (JButton button : quickButtons) {
            button.setFont(new Font("Arial", Font.PLAIN, 14));
            button.setPreferredSize(new Dimension(200, 100));
            button.setBackground(new Color(245, 245, 245));
            button.setBorder(BorderFactory.createRaisedBevelBorder());
        }

        quickActionsPanel.add(quickBookBtn);
        quickActionsPanel.add(quickViewBtn);
        quickActionsPanel.add(quickProfileBtn);
        quickActionsPanel.add(quickHelpBtn);

        // Recent activity panel
        JPanel recentActivityPanel = new JPanel(new BorderLayout());
        recentActivityPanel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        JTextArea recentActivity = new JTextArea(8, 40);
        recentActivity.setEditable(false);
        recentActivity.setText("No recent activity to display.\n\n" +
                              "Welcome to the Railway Booking System!\n" +
                              "Use the navigation menu or quick actions to get started.");
        recentActivity.setFont(new Font("Arial", Font.PLAIN, 12));
        recentActivityPanel.add(new JScrollPane(recentActivity), BorderLayout.CENTER);

        welcomePanel.add(welcomeLabel, BorderLayout.NORTH);
        welcomePanel.add(quickActionsPanel, BorderLayout.CENTER);
        welcomePanel.add(recentActivityPanel, BorderLayout.SOUTH);

        return welcomePanel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));

        JLabel statusLabel = new JLabel("Ready");
        JLabel userLabel = new JLabel("Logged in as: " + user.getUsername());
        JLabel timeLabel = new JLabel(new java.util.Date().toString());

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(userLabel, BorderLayout.CENTER);
        statusBar.add(timeLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void registerEventHandlers() {
        // Get all buttons from sidebar
        Component[] components = mainPanel.getComponents();
        JPanel sidebar = null;
        for (Component component : components) {
            if (component instanceof JPanel && component.getBounds().x == 0) {
                sidebar = (JPanel) component;
                break;
            }
        }
        
        if (sidebar != null) {
            Component[] sidebarComponents = sidebar.getComponents();
            for (Component component : sidebarComponents) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    String buttonText = button.getText();
                    
                    if (buttonText.contains("Book Ticket")) {
                        button.addActionListener(e -> openBookingFrame());
                    } else if (buttonText.contains("My Bookings")) {
                        button.addActionListener(e -> openMyBookingsFrame());
                    } else if (buttonText.contains("My Profile")) {
                        button.addActionListener(e -> openProfileFrame());
                    } else if (buttonText.contains("Manage Trains")) {
                        button.addActionListener(e -> openAdminFrame(0)); // Train Management tab
                    } else if (buttonText.contains("Manage Users")) {
                        button.addActionListener(e -> openAdminFrame(2)); // Users Management tab
                    } else if (buttonText.contains("Reports")) {
                        button.addActionListener(e -> openAdminFrame(1)); // Bookings Management tab
                    }
                }
            }
        }
        
        // Menu item event handlers
        JMenuBar menuBar = getJMenuBar();
        if (menuBar != null) {
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                if (menu != null) {
                    for (int j = 0; j < menu.getItemCount(); j++) {
                        JMenuItem item = menu.getItem(j);
                        if (item != null && !item.getText().equals("-")) {
                            String itemText = item.getText();
                            if (itemText.equals("Logout")) {
                                item.addActionListener(e -> logout());
                            } else if (itemText.equals("Exit")) {
                                item.addActionListener(e -> System.exit(0));
                            } else if (itemText.equals("New Booking")) {
                                item.addActionListener(e -> openBookingFrame());
                            } else if (itemText.equals("View My Bookings")) {
                                item.addActionListener(e -> openMyBookingsFrame());
                            } else if (itemText.contains("Manage Trains")) {
                                item.addActionListener(e -> openAdminFrame(0));
                            } else if (itemText.contains("Manage Users")) {
                                item.addActionListener(e -> openAdminFrame(2));
                            } else if (itemText.contains("View All Bookings")) {
                                item.addActionListener(e -> openAdminFrame(1));
                            }
                        }
                    }
                }
            }
        }
        
        // Quick action buttons event handlers
        Component[] allComponents = getContentPane().getComponents();
        for (Component component : allComponents) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                Component[] panelComponents = panel.getComponents();
                for (Component panelComponent : panelComponents) {
                    if (panelComponent instanceof JPanel) {
                        JPanel subPanel = (JPanel) panelComponent;
                        Component[] subComponents = subPanel.getComponents();
                        for (Component subComponent : subComponents) {
                            if (subComponent instanceof JButton) {
                                JButton button = (JButton) subComponent;
                                String buttonText = button.getText();
                                if (buttonText.contains("Book New Ticket")) {
                                    button.addActionListener(e -> openBookingFrame());
                                } else if (buttonText.contains("View Bookings")) {
                                    button.addActionListener(e -> openMyBookingsFrame());
                                } else if (buttonText.contains("Update Profile")) {
                                    button.addActionListener(e -> openProfileFrame());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Railway Booking System", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openBookingFrame() {
        SwingUtilities.invokeLater(() -> {
            new TrainSearchFrame(user).setVisible(true);
        });
    }
    
    private void openMyBookingsFrame() {
        SwingUtilities.invokeLater(() -> {
            new MyBookingsFrame(user).setVisible(true);
        });
    }
    
    private void openProfileFrame() {
        SwingUtilities.invokeLater(() -> {
            new ProfileFrame(user).setVisible(true);
        });
    }
    
    private void openAdminFrame(int tabIndex) {
        if (user.getUserType() == User.UserType.ADMIN) {
            SwingUtilities.invokeLater(() -> {
                new AdminFrame(user, tabIndex).setVisible(true);
            });
        } else {
            showMessage("Access denied. Admin privileges required.");
        }
    }
    
    private void logout() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Logout Confirmation", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // Invalidate session
            SessionManager.logout(session.getSessionId());
            logger.info("User logged out: {}", user.getUsername());
            
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }
}
