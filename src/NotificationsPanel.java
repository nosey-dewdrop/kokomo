import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class NotificationsPanel extends JPanel {

    public NotificationsPanel(HomeScreen home) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 48, 20, 48));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(16));

        ArrayList<String> notifs = Database.getNotifications(MainFile.currentUser.getUsername());
        if (notifs.isEmpty()) {
            content.add(UIHelper.createSmallLabel("No notifications yet."));
        } else {
            for (int i = notifs.size() - 1; i >= 0; i--) {
                JLabel lbl = new JLabel("\u2022  " + notifs.get(i));
                lbl.setFont(AppConstants.F_NORMAL);
                lbl.setForeground(AppConstants.TEXT_PRI);
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
                lbl.setAlignmentX(LEFT_ALIGNMENT);
                content.add(lbl);
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }
}
