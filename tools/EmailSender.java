package tools;

import model.*;

import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileInputStream;

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │                  <<class>> EmailSender                       │
 * │         Email verification via SMTP (JavaMail API)          │
 * ├──────────────────────────────────────────────────────────────┤
 * │ - SENDER_EMAIL, SENDER_PASSWORD, SMTP_HOST, SMTP_PORT       │
 * │   -> loaded from credentials.properties at startup          │
 * ├──────────────────────────────────────────────────────────────┤
 * │ + generateCode(): int -> random 6-digit verification code   │
 * │ + sendVerificationEmail(email, code): boolean               │
 * │ + sendPasswordResetEmail(email, code): boolean              │
 * │ - sendEmail(email, subject, body): boolean -> SMTP send     │
 * ├──────────────────────────────────────────────────────────────┤
 * │ USES:    AppConstants (code range)                           │
 * │ USED BY: RegisterScreen, ForgotPasswordDialog               │
 * └──────────────────────────────────────────────────────────────┘
 */
public class EmailSender {

    private static String SENDER_EMAIL;
    private static String SENDER_PASSWORD;
    private static String SMTP_HOST;
    private static int SMTP_PORT;
    private static final Random random = new Random();

    static {
        try {
            Properties creds = new Properties();
            creds.load(new FileInputStream("credentials.properties"));
            SENDER_EMAIL = creds.getProperty("email.sender", "");
            SENDER_PASSWORD = creds.getProperty("email.password", "");
            SMTP_HOST = creds.getProperty("email.smtp.host", "smtp.gmail.com");
            SMTP_PORT = Integer.parseInt(creds.getProperty("email.smtp.port", "587"));
        } catch (Exception e) {
            System.out.println("[EmailSender] credentials.properties not found - email disabled");
            SENDER_EMAIL = "";
            SENDER_PASSWORD = "";
            SMTP_HOST = "smtp.gmail.com";
            SMTP_PORT = 587;
        }
    }

    public static int generateCode() {
        return AppConstants.VERIFICATION_CODE_MIN +
            random.nextInt(AppConstants.VERIFICATION_CODE_MAX - AppConstants.VERIFICATION_CODE_MIN);
    }

    public static boolean sendVerificationEmail(String toEmail, int code) {
        String subject = "League of Bilkent - Email Verification";
        String body = "Welcome to League of Bilkent!\n\n"
                + "Your verification code: " + code + "\n\n"
                + "Enter this code on the registration screen.";
        return sendEmail(toEmail, subject, body);
    }

    public static boolean sendPasswordResetEmail(String toEmail, int code) {
        String subject = "League of Bilkent - Password Reset";
        String body = "Your password reset request has been received.\n\n"
                + "Your reset code: " + code + "\n\n"
                + "Enter this code on the password reset screen.";
        return sendEmail(toEmail, subject, body);
    }

    private static boolean sendEmail(String toEmail, String subject, String body) {
        if (SENDER_EMAIL.isEmpty() || SENDER_PASSWORD.isEmpty()) {
            System.out.println("[EmailSender] No credentials - email disabled");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "League of Bilkent"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Email sent: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
            return false;
        }
    }
}
