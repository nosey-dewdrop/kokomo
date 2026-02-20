import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LoginScreen extends JFrame {

    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private ArrayList<User> users;

    public LoginScreen() {
        setTitle("League of Bilkent - Login");
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

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JLabel titleLbl = new JLabel("League of Bilkent");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLbl.setForeground(AppConstants.TEXT_PRI);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);
        header.add(titleLbl);

        JLabel subLbl = new JLabel("Campus Event Platform");
        subLbl.setFont(AppConstants.F_SMALL);
        subLbl.setForeground(AppConstants.TEXT_SEC);
        subLbl.setAlignmentX(CENTER_ALIGNMENT);
        header.add(subLbl);
        main.add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppConstants.F_NORMAL);
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Register", new RegisterScreen(this));
        main.add(tabs, BorderLayout.CENTER);

        setContentPane(main);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));
        GridBagConstraints gc = UIHelper.createFullWidthGBC();

        gc.gridy = 0; panel.add(UIHelper.createLabel("Username"), gc);
        loginUsername = UIHelper.createStyledField();
        gc.gridy = 1; panel.add(loginUsername, gc);

        gc.gridy = 2; panel.add(UIHelper.createLabel("Password"), gc);
        loginPassword = UIHelper.createStyledPasswordField();
        gc.gridy = 3; panel.add(loginPassword, gc);

        JButton btnLogin = UIHelper.createButton("Log in", AppConstants.ACCENT, Color.WHITE);
        gc.gridy = 4; gc.insets = new Insets(15, 0, 6, 0);
        panel.add(btnLogin, gc);

        JButton btnForgot = new JButton("Forgot Password?");
        btnForgot.setFont(AppConstants.F_SMALL);
        btnForgot.setForeground(AppConstants.ACCENT);
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gc.gridy = 5; gc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnForgot, gc);

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
