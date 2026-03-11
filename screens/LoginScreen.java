package screens;

import model.*;
import model.Event;
import model.Event;
import panels.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LoginScreen extends JFrame {

    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private ArrayList<User> users;

    public LoginScreen() {
        setTitle("League of Bilkent");
        setSize(AppConstants.LOGIN_WIDTH, AppConstants.LOGIN_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        refreshUsers();
        buildUI();
    }

    public void refreshUsers() {
        users = Database.getAllUsers();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // Center content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(60, 60, 40, 60));

        JLabel titleLbl = new JLabel("League of Bilkent");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLbl.setForeground(AppConstants.TEXT_PRI);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);
        center.add(titleLbl);
        center.add(Box.createVerticalStrut(4));

        JLabel subLbl = new JLabel("Sign in to continue");
        subLbl.setFont(AppConstants.F_NORMAL);
        subLbl.setForeground(AppConstants.TEXT_SEC);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);
        center.add(subLbl);
        center.add(Box.createVerticalStrut(32));

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppConstants.F_NORMAL);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Register", new RegisterScreen(this));
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        center.add(tabs);

        main.add(center, BorderLayout.CENTER);
        setContentPane(main);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(AppConstants.F_SMALL);
        userLbl.setForeground(AppConstants.TEXT_SEC);
        userLbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(userLbl);
        panel.add(Box.createVerticalStrut(4));

        loginUsername = UIHelper.createStyledField();
        loginUsername.setAlignmentX(LEFT_ALIGNMENT);
        loginUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        panel.add(loginUsername);
        panel.add(Box.createVerticalStrut(14));

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(AppConstants.F_SMALL);
        passLbl.setForeground(AppConstants.TEXT_SEC);
        passLbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(passLbl);
        panel.add(Box.createVerticalStrut(4));

        loginPassword = UIHelper.createStyledPasswordField();
        loginPassword.setAlignmentX(LEFT_ALIGNMENT);
        loginPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        panel.add(loginPassword);
        panel.add(Box.createVerticalStrut(20));

        JButton btnLogin = UIHelper.createButton("Log in", AppConstants.PRIMARY, Color.WHITE);
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(10));

        JButton btnForgot = new JButton("Forgot Password?");
        btnForgot.setFont(AppConstants.F_SMALL);
        btnForgot.setForeground(AppConstants.ACCENT);
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgot.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(btnForgot);

        btnLogin.addActionListener(e -> handleLogin());
        loginPassword.addActionListener(e -> handleLogin());
        btnForgot.addActionListener(e -> {
            ForgotPasswordDialog dialog = new ForgotPasswordDialog(this);
            dialog.setVisible(true);
        });

        return panel;
    }

    private void handleLogin() {
        String username = loginUsername.getText().trim().toLowerCase();
        String password = new String(loginPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please enter username and password!");
            return;
        }

        refreshUsers();
        User found = null;
        for (User u : users) {
            if (u.getUsername().equals(username)) { found = u; break; }
        }

        if (found == null) {
            UIHelper.showError(this, "User not found!");
            return;
        }

        boolean match;
        if (found.getSalt() != null && !found.getSalt().isEmpty()) {
            match = PasswordUtil.hashPassword(password, found.getSalt()).equals(found.getPassword());
        } else {
            match = password.equals(found.getPassword());
        }

        if (!match) {
            UIHelper.showError(this, "Wrong password!");
            return;
        }

        if (!found.isVerified()) {
            UIHelper.showError(this, "Account not verified. Please check your email.");
            return;
        }

        MainFile.currentUser = found;
        HomeScreen home = new HomeScreen();
        home.setVisible(true);
        setVisible(false);
    }
}
