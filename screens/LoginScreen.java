package screens;

import model.*;
import panels.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │                  <<class>> LoginScreen                       │
 * │                    extends JFrame                            │
 * │              Login + Register tabbed window                  │
 * ├──────────────────────────────────────────────────────────────┤
 * │ - loginUsername, loginPassword -> input fields               │
 * │ - users: ArrayList<User> -> cached user list                │
 * ├──────────────────────────────────────────────────────────────┤
 * │ + refreshUsers() -> reloads users from DB                   │
 * │ - buildUI() -> creates tabbed pane (Login + Register)       │
 * │ - createLoginPanel() -> login form with fields + buttons    │
 * │ - handleLogin() -> validates credentials, opens HomeScreen  │
 * ├──────────────────────────────────────────────────────────────┤
 * │ USES:    Database, UIHelper, PasswordUtil, RegisterScreen,  │
 * │          ForgotPasswordDialog, HomeScreen, MainFile          │
 * │ USED BY: MainFile (entry point), HomeScreen (logout)        │
 * └──────────────────────────────────────────────────────────────┘
 */
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

    public void refreshUsers() { users = Database.getAllUsers(); }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(AppConstants.BG_MAIN);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(50, 60, 40, 60));

        // Water drop + brand
        JLabel dropEmoji = new JLabel("\uD83D\uDCA7");
        dropEmoji.setFont(new Font("SansSerif", Font.PLAIN, 1));
        dropEmoji.setAlignmentX(CENTER_ALIGNMENT);
        center.add(dropEmoji);
        center.add(Box.createVerticalStrut(8));

        JLabel titleLbl = new JLabel("League of Bilkent");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLbl.setForeground(AppConstants.TEXT_PRI);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);
        center.add(titleLbl);
        center.add(Box.createVerticalStrut(4));

        JLabel subLbl = new JLabel("Campus Event Platform");
        subLbl.setFont(AppConstants.F_SMALL);
        subLbl.setForeground(AppConstants.TEXT_LIGHT);
        subLbl.setAlignmentX(CENTER_ALIGNMENT);
        center.add(subLbl);
        center.add(Box.createVerticalStrut(28));

        // Login card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        card.setAlignmentX(CENTER_ALIGNMENT);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppConstants.F_NORMAL);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Register", new RegisterScreen(this));
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        card.add(tabs);

        center.add(card);
        center.add(Box.createVerticalStrut(16));

        JLabel footer = new JLabel("\uD83D\uDCA7 built by damla");
        footer.setFont(AppConstants.F_TINY);
        footer.setForeground(AppConstants.TEXT_MUTED);
        footer.setAlignmentX(CENTER_ALIGNMENT);
        center.add(footer);

        main.add(center, BorderLayout.CENTER);
        setContentPane(main);
    }

    private JPanel createLoginPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

        p.add(UIHelper.createFieldLabel("Username"));
        p.add(Box.createVerticalStrut(4));
        loginUsername = UIHelper.createStyledField();
        loginUsername.setAlignmentX(LEFT_ALIGNMENT);
        loginUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        p.add(loginUsername);
        p.add(Box.createVerticalStrut(14));

        p.add(UIHelper.createFieldLabel("Password"));
        p.add(Box.createVerticalStrut(4));
        loginPassword = UIHelper.createStyledPasswordField();
        loginPassword.setAlignmentX(LEFT_ALIGNMENT);
        loginPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        p.add(loginPassword);
        p.add(Box.createVerticalStrut(20));

        JButton btnLogin = UIHelper.createButton("Log in \u2192", AppConstants.TEAL, Color.WHITE);
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(btnLogin);
        p.add(Box.createVerticalStrut(10));

        JButton btnForgot = new JButton("Forgot Password?");
        btnForgot.setFont(AppConstants.F_SMALL);
        btnForgot.setForeground(AppConstants.TEAL);
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgot.setAlignmentX(CENTER_ALIGNMENT);
        p.add(btnForgot);

        btnLogin.addActionListener(e -> handleLogin());
        loginPassword.addActionListener(e -> handleLogin());
        btnForgot.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));

        return p;
    }

    private void handleLogin() {
        String username = loginUsername.getText().trim().toLowerCase();
        String password = new String(loginPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) { UIHelper.showError(this, AppConstants.ERR_USER_PASS); return; }
        refreshUsers();
        User found = null;
        for (User u : users) if (u.getUsername().equals(username)) { found = u; break; }
        if (found == null) { UIHelper.showError(this, AppConstants.ERR_USER_NOT_FOUND); return; }
        boolean match;
        if (found.getSalt() != null && !found.getSalt().isEmpty())
            match = PasswordUtil.hashPassword(password, found.getSalt()).equals(found.getPassword());
        else match = password.equals(found.getPassword());
        if (!match) { UIHelper.showError(this, AppConstants.ERR_WRONG_PASS); return; }
        if (!found.isVerified()) { UIHelper.showError(this, AppConstants.ERR_NOT_VERIFIED); return; }
        MainFile.currentUser = found;
        new HomeScreen().setVisible(true);
        setVisible(false);
    }
}
