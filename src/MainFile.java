import javax.swing.*;

/**
 * Uygulama giris noktasi.
 */
public class MainFile {

    public static User currentUser;
    public static LoginScreen loginScreen;

    public static void main(String[] args) {
        try {
            // Modern gorunum icin system look & feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Anti-aliased text
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception ignored) {}

        Database.createConnection();

        if (Database.isDatabaseEmpty()) {
            SampleData.loadSampleData();
        }

        SwingUtilities.invokeLater(() -> {
            loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });
    }
}
