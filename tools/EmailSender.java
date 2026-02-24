package tools;

import model.*;
import model.Event;
import model.Event;

import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileInputStream;

/**
 * Email sender - reads credentials from credentials.properties
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
        String subject = "Squirrel - Email Verification";
        String body = "Welcome to Squirrel!\n\n"
                + "Your verification code: " + code + "\n\n"
                + "Enter this code on the registration screen.";
        return sendEmail(toEmail, subject, body);
    }

    public static boolean sendPasswordResetEmail(String toEmail, int code) {
        String subject = "Squirrel - Password Reset";
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
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Squirrel"));
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
