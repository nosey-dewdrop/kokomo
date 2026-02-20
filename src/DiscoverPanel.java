import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DiscoverPanel extends JPanel {

    private HomeScreen home;

    public DiscoverPanel(HomeScreen home) {
        this.home = home;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 48, 20, 48));
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Discover");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Trending events and personalized recommendations.");
        sub.setFont(AppConstants.F_SMALL);
        sub.setForeground(AppConstants.TEXT_SEC);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(16));

        // Trending
        content.add(UIHelper.createSectionLabel("Trending"));
        content.add(Box.createVerticalStrut(6));
        ArrayList<Integer> popularIds = Database.getPopularEventIds(AppConstants.DISCOVER_LIMIT);
        if (popularIds.isEmpty()) {
            content.add(UIHelper.createSmallLabel("No trending events yet."));
        } else {
            ArrayList<Event> allEvents = Database.getAllEvents();
            for (int id : popularIds) {
                for (Event ev : allEvents) {
                    if (ev.getId() == id) {
                        content.add(createEventRow(ev));
                        break;
                    }
                }
            }
        }

        content.add(Box.createVerticalStrut(16));
        JSeparator sep = new JSeparator();
        sep.setForeground(AppConstants.BORDER);
        sep.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sep);
        content.add(Box.createVerticalStrut(12));

        // For You
        content.add(UIHelper.createSectionLabel("For You"));
        content.add(Box.createVerticalStrut(6));
        ArrayList<Integer> recIds = Database.getRecommendedEventIds(
            MainFile.currentUser.getUsername(), AppConstants.DISCOVER_LIMIT);
        if (recIds.isEmpty()) {
            content.add(UIHelper.createSmallLabel("No recommendations. Try adding more interests!"));
        } else {
            ArrayList<Event> allEvents = Database.getAllEvents();
            for (int id : recIds) {
                for (Event ev : allEvents) {
                    if (ev.getId() == id) {
                        content.add(createEventRow(ev));
                        break;
                    }
                }
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createEventRow(Event ev) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(8, 4, 8, 4)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel name = new JLabel(ev.getTitle() + "  by @" + ev.getCreatorUsername());
        name.setFont(AppConstants.F_NORMAL);
        info.add(name);
        JLabel detail = new JLabel(ev.getLocation() + "  |  " + ev.getDateStr() + "  |  " + ev.getGoingCount() + "/" + ev.getCapacity() + " going");
        detail.setFont(AppConstants.F_TINY);
        detail.setForeground(AppConstants.TEXT_SEC);
        info.add(detail);
        row.add(info, BorderLayout.CENTER);

        JButton view = UIHelper.createOutlineButton("View", AppConstants.ACCENT);
        view.addActionListener(e -> home.showEventDetail(ev));
        row.add(view, BorderLayout.EAST);

        return row;
    }
}
