package screens;

import model.*;
import panels.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │                <<class>> RegisterScreen                      │
 * │                   extends JPanel                             │
 * │        Registration form (embedded in LoginScreen tab)       │
 * ├──────────────────────────────────────────────────────────────┤
 * │ - usernameField, emailField, displayNameField, passwordField │
 * │ - clubCheckBox -> club account toggle                       │
 * │ - verificationCode -> email verification code               │
 * ├──────────────────────────────────────────────────────────────┤
 * │ - initComponents() -> builds registration form              │
 * │ - handleRegister() -> validates, sends email verification,  │
 * │   hashes password, saves user, shows interest selection      │
 * ├──────────────────────────────────────────────────────────────┤
 * │ USES:    Database, UIHelper, PasswordUtil, EmailSender,     │
 * │          InterestSelectionDialog, User, ClubUser             │
 * │ USED BY: LoginScreen (register tab)                         │
 * └──────────────────────────────────────────────────────────────┘
 */
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

        // VALIDATION
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
        if (Database.getUserWithUsername(username) != null) {
            UIHelper.showError(this, "This username is already taken! Try another one."); return;
        }
        if (Database.isEmailTaken(email)) {
            UIHelper.showError(this, "This email is already registered!"); return;
        }
        if (displayName.isEmpty()) displayName = username;

        // SEND VERIFICATION EMAIL FIRST (before saving anything to DB)
        verificationCode = EmailSender.generateCode();
        boolean emailSent = false;
        try {
            emailSent = EmailSender.sendVerificationEmail(email, verificationCode);
        } catch (Exception ex) {
            System.err.println("Email error: " + ex.getMessage());
        }

        String input;
        if (emailSent) {
            input = JOptionPane.showInputDialog(this,
                "Verification code sent to " + email + "\nEnter the 6-digit code:",
                "Email Verification", JOptionPane.QUESTION_MESSAGE);
        } else {
            // Email failed — show code in popup for testing
            input = JOptionPane.showInputDialog(this,
                "Email could not be sent.\nYour verification code is: " + verificationCode + "\nEnter it below to confirm:",
                "Email Verification (Fallback)", JOptionPane.WARNING_MESSAGE);
        }

        // VERIFY CODE
        if (input == null || !input.trim().equals(String.valueOf(verificationCode))) {
            UIHelper.showError(this, "Wrong code or cancelled. Registration failed.");
            return;
        }

        // CODE CORRECT — NOW save to DB
        String salt = PasswordUtil.generateSalt();
        String hashed = PasswordUtil.hashPassword(password, salt);

        User newUser;
        if (isClub) {
            newUser = new ClubUser(username, displayName, email, hashed, salt,
                "Bilkent " + displayName + " club");
        } else {
            newUser = new User(username, displayName, email, hashed, salt,
                "League of Bilkent user");
        }
        newUser.setVerified(true);
        Database.addToDatabase(newUser);
        Database.updateUserVerified(username, true);

        // INTEREST SELECTION
        InterestSelectionDialog dialog = new InterestSelectionDialog(
            SwingUtilities.getWindowAncestor(this), new ArrayList<>());
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Database.setInterests(username, dialog.getSelectedInterests());
        }

        loginScreen.refreshUsers();
        UIHelper.showSuccess(this, "Registration successful! You can now log in.");

        // Clear fields
        usernameField.setText("");
        displayNameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        clubCheckBox.setSelected(false);
    }
}
