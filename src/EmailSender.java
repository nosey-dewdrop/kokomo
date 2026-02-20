import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Bilkent SMTP uzerinden gercek email gonderen sinif.
 */
public class EmailSender {

    private static final String SENDER_EMAIL    = AppConstants.EMAIL_SENDER;
    private static final String SENDER_PASSWORD = AppConstants.EMAIL_PASSWORD;
    private static final Random random = new Random();

    public static int generateCode() {
        return AppConstants.VERIFICATION_CODE_MIN +
            random.nextInt(AppConstants.VERIFICATION_CODE_MAX - AppConstants.VERIFICATION_CODE_MIN + 1);
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
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", AppConstants.EMAIL_SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(AppConstants.EMAIL_SMTP_PORT));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Email sent: " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.err.println("Email failed: " + e.getMessage());
            return false;
        }
    }
}
