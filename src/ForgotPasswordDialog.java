import javax.swing.*;
import java.awt.*;

/**
 * Sifre sifirlama dialog'u.
 * Email girer -> dogrulama kodu gonderilir -> yeni sifre girilir.
 */
public class ForgotPasswordDialog extends JDialog {

    private JTextField emailField;
    private JTextField codeField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton btnSendCode, btnReset;
    private JPanel codePanel;
    private int verificationCode;
    private String targetEmail;
    private String targetUsername;

    public ForgotPasswordDialog(Window parent) {
        super(parent, "Reset Password", ModalityType.APPLICATION_MODAL);
        setSize(420, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(AppConstants.PRIMARY);
        header.setPreferredSize(new Dimension(0, 60));
        header.setLayout(new GridBagLayout());
        JLabel title = new JLabel("Reset Password");
        title.setFont(AppConstants.F_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title);
        return header;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = UIHelper.createFullWidthGBC();

        // Email alani
        gc.gridy = 0; form.add(UIHelper.createLabel("Bilkent Email Address"), gc);
        emailField = UIHelper.createStyledField();
        gc.gridy = 1; form.add(emailField, gc);

        btnSendCode = UIHelper.createButton("Send Code", AppConstants.ACCENT, Color.WHITE);
        gc.gridy = 2; gc.insets = new Insets(10, 0, 10, 0);
        form.add(btnSendCode, gc);

        // Kod + yeni sifre alani (baslangicta gizli)
        codePanel = new JPanel(new GridBagLayout());
        codePanel.setBackground(Color.WHITE);
        codePanel.setVisible(false);
        GridBagConstraints gc2 = UIHelper.createFullWidthGBC();

        gc2.gridy = 0; codePanel.add(UIHelper.createLabel("Verification Code"), gc2);
        codeField = UIHelper.createStyledField();
        gc2.gridy = 1; codePanel.add(codeField, gc2);

        gc2.gridy = 2; codePanel.add(UIHelper.createLabel("New Password"), gc2);
        newPasswordField = UIHelper.createStyledPasswordField();
        gc2.gridy = 3; codePanel.add(newPasswordField, gc2);

        gc2.gridy = 4; codePanel.add(UIHelper.createLabel("Confirm Password"), gc2);
        confirmPasswordField = UIHelper.createStyledPasswordField();
        gc2.gridy = 5; codePanel.add(confirmPasswordField, gc2);

        btnReset = UIHelper.createButton("Change Password", AppConstants.SUCCESS, Color.WHITE);
        gc2.gridy = 6; gc2.insets = new Insets(12, 0, 0, 0);
        codePanel.add(btnReset, gc2);

        gc.gridy = 3; gc.insets = new Insets(0, 0, 0, 0);
        form.add(codePanel, gc);

        // Listeners
        btnSendCode.addActionListener(e -> handleSendCode());
        btnReset.addActionListener(e -> handleReset());

        return form;
    }

    private void handleSendCode() {
        String email = emailField.getText().trim().toLowerCase();

        if (email.isEmpty()) {
            UIHelper.showError(this, "Please enter your email!");
            return;
        }
        if (!email.endsWith("@ug.bilkent.edu.tr") && !email.endsWith("@bilkent.edu.tr")) {
            UIHelper.showError(this, "Please enter a valid Bilkent email!");
            return;
        }

        // Kullaniciyi bul
        User user = Database.getUserWithUsername(findUsernameByEmail(email));
        if (user == null) {
            UIHelper.showError(this, "No account found with this email!");
            return;
        }

        targetEmail = email;
        targetUsername = user.getUsername();
        verificationCode = EmailSender.generateCode();

        boolean sent = EmailSender.sendPasswordResetEmail(email, verificationCode);
        if (!sent) {
            System.out.println("[EMAIL FALLBACK] Password reset code: " + verificationCode);
            JOptionPane.showMessageDialog(this,
                "Email failed. Console code: " + verificationCode,
                "Email Error", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Verification code sent to your email.");
        }

        btnSendCode.setEnabled(false);
        emailField.setEnabled(false);
        codePanel.setVisible(true);
    }

    private void handleReset() {
        String code = codeField.getText().trim();
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (!code.equals(String.valueOf(verificationCode))) {
            UIHelper.showError(this, "Wrong verification code!");
            return;
        }
        if (newPass.length() < 4) {
            UIHelper.showError(this, "Password must be at least 4 characters!");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            UIHelper.showError(this, "Passwords do not match!");
            return;
        }

        // Sifre guncelle (hash + salt)
        String salt = PasswordUtil.generateSalt();
        String hashed = PasswordUtil.hashPassword(newPass, salt);
        Database.updateUserPassword(targetUsername, hashed, salt);

        UIHelper.showSuccess(this, "Password changed successfully!");
        dispose();
    }

    private String findUsernameByEmail(String email) {
        for (User u : Database.getAllUsers()) {
            if (u.getEmail().equalsIgnoreCase(email)) return u.getUsername();
        }
        return "";
    }
}
