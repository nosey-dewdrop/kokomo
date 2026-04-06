package screens;

import model.*;
import tools.*;
import javax.swing.*;

public class MainFile {

    public static User currentUser;
    public static LoginScreen loginScreen;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception ignored) {}

        NetworkManager.isClientMode = true;

        if (NetworkManager.isClientMode) {
            NetworkManager.startDiscovery();
            try { Thread.sleep(2500); } catch (Exception ignored) {}
            if (!NetworkManager.discoveredHosts.isEmpty()) {
                String ip = NetworkManager.discoveredHosts.get(0).ip;
                Database.customDbUrl = "jdbc:mysql://" + ip + ":3306/league_of_bilkent?createDatabaseIfNotExist=true";
            }
        } else {
            NetworkManager.startBroadcasting();
        }

        Database.createConnection();

        // if remote host not found but local DB connected, keep working locally
        if (Database.databaseConnection == null && Database.customDbUrl == null) {
            Database.createConnection();
        }
        if (Database.isDatabaseEmpty() && Database.customDbUrl == null) {
            SampleData.loadSampleData();
        }

        NetworkManager.onHostFound = () -> {
            if (Database.customDbUrl == null && !NetworkManager.discoveredHosts.isEmpty()) {
                String ip = NetworkManager.discoveredHosts.get(0).ip;
                Database.customDbUrl = "jdbc:mysql://" + ip + ":3306/league_of_bilkent?createDatabaseIfNotExist=true";
                Database.createConnection();
                if (loginScreen != null) loginScreen.refreshUsers();
            }
        };

        SwingUtilities.invokeLater(() -> {
            loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
            loginScreen.refreshUsers();
        });
    }
}
