import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RegisterScreen extends JPanel {

    private LoginScreen loginScreen;
    private JTextField usernameField, emailField, displayNameField;
    private JPasswordField passwordField;
    private JCheckBox clubCheckBox;
    private int verificationCode;

    public RegisterScreen(LoginScreen loginScreen) {
        this.loginScreen = loginScreen;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gc = UIHelper.createFullWidthGBC();

        usernameField = UIHelper.createStyledField();
        displayNameField = UIHelper.createStyledField();
        emailField = UIHelper.createStyledField();
        passwordField = UIHelper.createStyledPasswordField();
        clubCheckBox = new JCheckBox("Club account");
        clubCheckBox.setFont(AppConstants.F_SMALL);
        clubCheckBox.setBackground(Color.WHITE);

        gc.gridy = 0; add(UIHelper.createLabel("Username"), gc);
        gc.gridy = 1; add(usernameField, gc);
        gc.gridy = 2; add(UIHelper.createLabel("Display Name"), gc);
        gc.gridy = 3; add(displayNameField, gc);
        gc.gridy = 4; add(UIHelper.createLabel("Email (@bilkent.edu.tr)"), gc);
        gc.gridy = 5; add(emailField, gc);
        gc.gridy = 6; add(UIHelper.createLabel("Password"), gc);
        gc.gridy = 7; add(passwordField, gc);
        gc.gridy = 8; gc.insets = new Insets(8, 0, 3, 0); add(clubCheckBox, gc);

        JButton btnRegister = UIHelper.createButton("Register", AppConstants.ACCENT, Color.WHITE);
        gc.gridy = 9; gc.insets = new Insets(12, 0, 6, 0);
        add(btnRegister, gc);

        btnRegister.addActionListener(e -> handleRegister());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim().toLowerCase();
        String displayName = displayNameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());
        boolean isClub = clubCheckBox.isSelected();

        // ===== VALIDATION =====
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please fill in all required fields!"); return;
        }
        if (username.length() < AppConstants.MIN_USERNAME_LENGTH) {
            UIHelper.showError(this, "Username must be at least " + AppConstants.MIN_USERNAME_LENGTH + " characters!"); return;
        }
        if (!email.endsWith("@ug.bilkent.edu.tr") && !email.endsWith("@bilkent.edu.tr")) {
            UIHelper.showError(this, "Only Bilkent email addresses are accepted!"); return;
        }
        if (password.length() < AppConstants.MIN_PASSWORD_LENGTH) {
            UIHelper.showError(this, "Password must be at least " + AppConstants.MIN_PASSWORD_LENGTH + " characters!"); return;
        }

        // ===== DUPLICATE CHECKS =====
        if (Database.getUserWithUsername(username) != null) {
            UIHelper.showError(this, "This username is already taken! Try another one."); return;
        }
        if (Database.isEmailTaken(email)) {
            UIHelper.showError(this, "This email is already registered! Try logging in or use a different email."); return;
        }

        if (displayName.isEmpty()) displayName = username;

        // ===== HASH PASSWORD =====
        String salt = PasswordUtil.generateSalt();
        String hashed = PasswordUtil.hashPassword(password, salt);

        // ===== CREATE USER =====
        User newUser;
        if (isClub) {
            newUser = new ClubUser(username, displayName, email, hashed, salt,
                "Bilkent " + displayName + " club");
        } else {
            newUser = new User(username, displayName, email, hashed, salt,
                "League of Bilkent user");
        }

        Database.addToDatabase(newUser);

        // ===== EMAIL VERIFICATION =====
        verificationCode = EmailSender.generateCode();
        boolean emailSent = EmailSender.sendVerificationEmail(email, verificationCode);

        if (!emailSent) {
            System.out.println("[EMAIL FALLBACK] Verification code: " + verificationCode);
        }

        String input = JOptionPane.showInputDialog(this,
            emailSent ? "Verification code sent to " + email + ".\nEnter the 6-digit code:"
                      : "Email could not be sent.\nVerification code (check console): " + verificationCode + "\nEnter code:",
            "Email Verification", JOptionPane.QUESTION_MESSAGE);

        if (input != null && input.trim().equals(String.valueOf(verificationCode))) {
            newUser.setVerified(true);
            Database.updateUserVerified(username, true);

            // ===== INTEREST SELECTION =====
            InterestSelectionDialog dialog = new InterestSelectionDialog(
                SwingUtilities.getWindowAncestor(this), new ArrayList<>());
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                ArrayList<String> interests = dialog.getSelectedInterests();
                Database.setInterests(username, interests);
            }

            loginScreen.refreshUsers();
            UIHelper.showSuccess(this, "Registration successful! You can now log in.");

            // Clear fields
            usernameField.setText("");
            displayNameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            clubCheckBox.setSelected(false);
        } else {
            // Wrong code or cancelled — delete the unverified user
            Database.deleteFromDatabase(newUser);
            UIHelper.showError(this, "Wrong code! Registration cancelled.");
        }
    }
}
