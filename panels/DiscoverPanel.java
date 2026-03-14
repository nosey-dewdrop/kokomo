package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DiscoverPanel extends JPanel {

    private HomeScreen home;

    public DiscoverPanel(HomeScreen home) {
        this.home = home;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createPagePanel();

        content.add(UIHelper.createPageTitle(AppConstants.PAGE_DISCOVER));
        content.add(Box.createVerticalStrut(4));
        content.add(UIHelper.createSubtitle(AppConstants.PAGE_DISCOVER_SUB));
        content.add(Box.createVerticalStrut(20));

        content.add(UIHelper.createSectionLabel(AppConstants.SEC_TRENDING));
        content.add(Box.createVerticalStrut(8));
        ArrayList<Integer> popularIds = Database.getPopularEventIds(AppConstants.DISCOVER_LIMIT);
        if (popularIds.isEmpty()) {
            content.add(UIHelper.createSmallLabel(AppConstants.EMPTY_TRENDING));
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

        content.add(Box.createVerticalStrut(20));
        content.add(UIHelper.createSeparator());
        content.add(Box.createVerticalStrut(16));

        content.add(UIHelper.createSectionLabel(AppConstants.SEC_FOR_YOU));
        content.add(Box.createVerticalStrut(8));
        ArrayList<Integer> recIds = Database.getRecommendedEventIds(
            MainFile.currentUser.getUsername(), AppConstants.DISCOVER_LIMIT);
        if (recIds.isEmpty()) {
            content.add(UIHelper.createSmallLabel(AppConstants.EMPTY_RECOMMEND));
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

        add(UIHelper.wrapInScroll(content), BorderLayout.CENTER);
    }

    private JPanel createEventRow(Event ev) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(10, 4, 10, 4)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
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

        JButton view = UIHelper.createOutlineButton(AppConstants.BTN_VIEW, AppConstants.ACCENT);
        view.addActionListener(e -> home.showEventDetail(ev));
        row.add(view, BorderLayout.EAST);

        return row;
    }
}
