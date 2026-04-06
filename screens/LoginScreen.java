package screens;

import model.*;
import panels.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │                  <<class>> LoginScreen                       │
 * │                    extends JFrame                            │
 * │     Split-layout login: brand panel (left) + form (right)   │
 * ├──────────────────────────────────────────────────────────────┤
 * │ - loginUsername, loginPassword -> input fields               │
 * │ - users: ArrayList<User> -> cached user list                │
 * │ - formContainer -> CardLayout for login/register switch     │
 * ├──────────────────────────────────────────────────────────────┤
 * │ + refreshUsers() -> reloads users from DB                   │
 * │ - buildUI() -> creates split layout                         │
 * │ - buildBrandPanel() -> gradient left panel with branding    │
 * │ - buildFormPanel() -> right panel with login/register forms │
 * │ - createLoginForm() -> login fields + buttons               │
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
    private CardLayout formCardLayout;
    private JPanel formContainer;
    private JButton tabLogin, tabRegister;
    private JLabel cLbl;

    public LoginScreen() {
        setTitle("League of Bilkent");
        setSize(AppConstants.LOGIN_WIDTH, AppConstants.LOGIN_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        refreshUsers();
        buildUI();
        startPolling();
    }

    private void startPolling() {
        new Thread(() -> {
            int lastHash = Database.getDbStateHash();
            while (true) {
                try { Thread.sleep(1000); } catch(Exception ignored){}
                if (!isVisible()) continue;
                
                SwingUtilities.invokeLater(() -> {
                    if (cLbl != null) {
                        String status = Database.customDbUrl == null ? "Local Database" : "Connected: " + Database.customDbUrl.split("//")[1].split(":")[0];
                        if (tools.NetworkManager.isClientMode && Database.customDbUrl == null) status = "Searching for Host...";
                        cLbl.setText(status);
                        cLbl.setForeground(Database.customDbUrl == null ? AppConstants.TEXT_LIGHT : AppConstants.SUCCESS);
                        cLbl.repaint();
                    }
                });

                int newHash = Database.getDbStateHash();
                if (newHash != -1 && newHash != lastHash) {
                    lastHash = newHash;
                    SwingUtilities.invokeLater(this::refreshUsers);
                }
            }
        }).start();
    }

    public void refreshUsers() { users = Database.getAllUsers(); }

    private void buildUI() {
        getContentPane().removeAll();
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        main.add(buildFormPanel(), BorderLayout.CENTER);

        setContentPane(main);
        revalidate();
        repaint();
    }

    private JPanel buildBrandPanel() {
        JPanel brand = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Gradient background
                g2.setPaint(new GradientPaint(0, 0, new Color(0x00, 0x96, 0xA6),
                    w, h, new Color(0x00, 0x7D, 0x8C)));
                g2.fillRect(0, 0, w, h);

                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(w - 100, -60, 200, 200);
                g2.fillOval(-70, h - 120, 180, 180);
                g2.fillOval(w / 2 - 50, h / 2 + 40, 120, 120);

                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(20, 60, 100, 100);
                g2.fillOval(w - 60, h - 80, 90, 90);

                g2.dispose();
            }
        };
        brand.setPreferredSize(new Dimension(340, 0));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));

        brand.add(Box.createVerticalGlue());

        // Logo circle
        JPanel logoCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(0, 0, 72, 72);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 34));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "LoB";
                g2.drawString(icon, 36 - fm.stringWidth(icon) / 2, 36 + fm.getAscent() / 3);
                g2.dispose();
            }
        };
        logoCircle.setOpaque(false);
        logoCircle.setPreferredSize(new Dimension(72, 72));
        logoCircle.setMaximumSize(new Dimension(72, 72));
        logoCircle.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(logoCircle);
        brand.add(Box.createVerticalStrut(24));

        JLabel title = new JLabel("League of");
        title.setFont(new Font("SansSerif", Font.PLAIN, 28));
        title.setForeground(new Color(255, 255, 255, 180));
        title.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(title);

        JLabel title2 = new JLabel("Bilkent");
        title2.setFont(new Font("SansSerif", Font.BOLD, 36));
        title2.setForeground(Color.WHITE);
        title2.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(title2);
        brand.add(Box.createVerticalStrut(16));

        JLabel tagline = new JLabel("<html>Discover events, connect<br>with your campus community,<br>and earn XP along the way.</html>");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tagline.setForeground(new Color(255, 255, 255, 160));
        tagline.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(tagline);
        brand.add(Box.createVerticalStrut(32));

        // Feature pills
        String[] features = {"Events", "XP System", "Clubs", "Calendar"};
        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pillRow.setOpaque(false);
        pillRow.setAlignmentX(LEFT_ALIGNMENT);
        for (String feat : features) {
            JLabel pill = new JLabel(feat);
            pill.setFont(new Font("SansSerif", Font.BOLD, 10));
            pill.setForeground(new Color(255, 255, 255, 200));
            pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60), 1, true),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)));
            pillRow.add(pill);
        }
        brand.add(pillRow);

        brand.add(Box.createVerticalGlue());

        // Footer
        JLabel footer = new JLabel("built by team manifest");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(new Color(255, 255, 255, 80));
        footer.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(footer);

        return brand;
    }

    private JPanel buildFormPanel() {
        JPanel right = new JPanel();
        right.setBackground(Color.WHITE);
        right.setLayout(new GridBagLayout());

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.WHITE);
        wrapper.setMaximumSize(new Dimension(340, 500));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        cLbl = new JLabel();
        cLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        headerRow.add(cLbl, BorderLayout.WEST);

        JButton netBtn = new JButton(UIManager.getIcon("FileView.computerIcon"));
        netBtn.setToolTipText("Network Settings");
        netBtn.setBorderPainted(false);
        netBtn.setContentAreaFilled(false);
        netBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        netBtn.addActionListener(e -> {
            new panels.NetworkDialog(this).setVisible(true);
            buildUI(); // Redraw status
        });
        headerRow.add(netBtn, BorderLayout.EAST);

        wrapper.add(headerRow);
        wrapper.add(Box.createVerticalStrut(10));

        JLabel welcomeLbl = new JLabel("Welcome back");
        welcomeLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLbl.setForeground(AppConstants.TEXT_PRI);
        welcomeLbl.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.add(welcomeLbl);
        wrapper.add(Box.createVerticalStrut(4));

        JLabel subLbl = new JLabel("Sign in to your account or create a new one");
        subLbl.setFont(AppConstants.F_SMALL);
        subLbl.setForeground(AppConstants.TEXT_LIGHT);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.add(subLbl);
        wrapper.add(Box.createVerticalStrut(28));

        // Custom tab bar
        JPanel tabBar = new JPanel(new GridLayout(1, 2, 0, 0));
        tabBar.setBackground(AppConstants.BG_MAIN);
        tabBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tabBar.setAlignmentX(LEFT_ALIGNMENT);
        tabBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        tabLogin = createTabButton("Sign In", true);
        tabRegister = createTabButton("Create Account", false);

        tabLogin.addActionListener(e -> switchTab(true));
        tabRegister.addActionListener(e -> switchTab(false));

        tabBar.add(tabLogin);
        tabBar.add(tabRegister);
        wrapper.add(tabBar);
        wrapper.add(Box.createVerticalStrut(24));

        // Form container with CardLayout
        formCardLayout = new CardLayout();
        formContainer = new JPanel(formCardLayout);
        formContainer.setBackground(Color.WHITE);
        formContainer.setAlignmentX(LEFT_ALIGNMENT);

        formContainer.add(createLoginForm(), "login");
        formContainer.add(new RegisterScreen(this), "register");

        wrapper.add(formContainer);

        right.add(wrapper);
        return right;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getClientProperty("active") == Boolean.TRUE) {
                    g2.setColor(Color.WHITE);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.putClientProperty("active", active);
        b.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
        b.setForeground(active ? AppConstants.TEXT_PRI : AppConstants.TEXT_LIGHT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void switchTab(boolean isLogin) {
        tabLogin.putClientProperty("active", isLogin);
        tabLogin.setFont(new Font("SansSerif", isLogin ? Font.BOLD : Font.PLAIN, 13));
        tabLogin.setForeground(isLogin ? AppConstants.TEXT_PRI : AppConstants.TEXT_LIGHT);
        tabLogin.repaint();

        tabRegister.putClientProperty("active", !isLogin);
        tabRegister.setFont(new Font("SansSerif", !isLogin ? Font.BOLD : Font.PLAIN, 13));
        tabRegister.setForeground(!isLogin ? AppConstants.TEXT_PRI : AppConstants.TEXT_LIGHT);
        tabRegister.repaint();

        formCardLayout.show(formContainer, isLogin ? "login" : "register");
    }

    private JPanel createLoginForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        // Username
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        userLbl.setForeground(AppConstants.TEXT_SEC);
        userLbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(userLbl);
        p.add(Box.createVerticalStrut(6));
        loginUsername = createRoundedField("Enter your username");
        p.add(loginUsername);
        p.add(Box.createVerticalStrut(16));

        // Password
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLbl.setForeground(AppConstants.TEXT_SEC);
        passLbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(passLbl);
        p.add(Box.createVerticalStrut(6));
        loginPassword = createRoundedPasswordField("Enter your password");
        p.add(loginPassword);
        p.add(Box.createVerticalStrut(8));

        // Forgot password - right aligned
        JPanel forgotRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotRow.setBackground(Color.WHITE);
        forgotRow.setAlignmentX(LEFT_ALIGNMENT);
        forgotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JButton btnForgot = new JButton("Forgot password?");
        btnForgot.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnForgot.setForeground(AppConstants.TEAL);
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));
        forgotRow.add(btnForgot);
        p.add(forgotRow);
        p.add(Box.createVerticalStrut(20));

        // Login button - full width gradient
        JButton btnLogin = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = AppConstants.TEAL;
                Color c2 = AppConstants.TEAL_DARK;
                if (getModel().isPressed()) { c1 = c1.darker(); c2 = c2.darker(); }
                else if (getModel().isRollover()) { c1 = c1.brighter(); c2 = c2.brighter(); }
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setOpaque(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setPreferredSize(new Dimension(0, 44));
        btnLogin.addActionListener(e -> handleLogin());
        p.add(btnLogin);

        loginPassword.addActionListener(e -> handleLogin());
        loginUsername.addActionListener(e -> loginPassword.requestFocus());

        p.add(Box.createVerticalStrut(24));

        return p;
    }

    private JTextField createRoundedField(String placeholder) {
        JTextField f = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? AppConstants.TEAL : AppConstants.BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        f.setFont(AppConstants.F_NORMAL);
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // Placeholder
        f.setForeground(AppConstants.TEXT_LIGHT);
        f.setText(placeholder);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(AppConstants.TEXT_PRI);
                }
                f.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(AppConstants.TEXT_LIGHT);
                }
                f.repaint();
            }
        });
        return f;
    }

    private JPasswordField createRoundedPasswordField(String placeholder) {
        JPasswordField f = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? AppConstants.TEAL : AppConstants.BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        f.setFont(AppConstants.F_NORMAL);
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setEchoChar((char) 0);
        f.setForeground(AppConstants.TEXT_LIGHT);
        f.setText(placeholder);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(f.getPassword()).equals(placeholder)) {
                    f.setText("");
                    f.setEchoChar('\u2022');
                    f.setForeground(AppConstants.TEXT_PRI);
                }
                f.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getPassword().length == 0) {
                    f.setEchoChar((char) 0);
                    f.setText(placeholder);
                    f.setForeground(AppConstants.TEXT_LIGHT);
                }
                f.repaint();
            }
        });
        return f;
    }

    private void handleLogin() {
        if (Database.databaseConnection == null) {
            UIHelper.showError(this, "Database connection not available!");
            return;
        }
        String username = loginUsername.getText().trim().toLowerCase();
        if (username.equals("enter your username")) username = "";
        String password = new String(loginPassword.getPassword());
        if (password.equals("Enter your password")) password = "";
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
