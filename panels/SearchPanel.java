package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SearchPanel extends JPanel {

    private HomeScreen home;
    private JTextField searchField;
    private JPanel resultsPanel;

    public SearchPanel(HomeScreen home) {
        this.home = home;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(28, 40, 20, 40));
        buildUI();
    }

    /** Called from top search bar */
    public void searchFor(String query) {
        searchField.setText(query);
        performSearch();
    }

    private void buildUI() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Search");
        title.setFont(AppConstants.F_BIG);
        title.setForeground(AppConstants.TEXT_PRI);
        title.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(12));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(Color.WHITE);
        searchRow.setAlignmentX(LEFT_ALIGNMENT);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        searchField = UIHelper.createStyledField();
        searchField.setFont(AppConstants.F_NORMAL);
        JButton btnSearch = UIHelper.createButton("Search", AppConstants.ACCENT, Color.WHITE);
        Runnable doSearch = this::performSearch;
        btnSearch.addActionListener(e -> doSearch.run());
        searchField.addActionListener(e -> doSearch.run());

        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(btnSearch, BorderLayout.EAST);
        header.add(searchRow);
        header.add(Box.createVerticalStrut(12));

        // Tag chips
        JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        tagRow.setBackground(Color.WHITE);
        tagRow.setAlignmentX(LEFT_ALIGNMENT);
        for (String cat : AppConstants.INTEREST_CATEGORIES) {
            JButton chip = new JButton("#" + cat.toLowerCase());
            chip.setFont(AppConstants.F_TINY);
            chip.setBorderPainted(false);
            chip.setBackground(AppConstants.PRIMARY_LIGHT);
            chip.setForeground(AppConstants.TEXT_SEC);
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.addActionListener(e -> { searchField.setText(cat.toLowerCase()); performSearch(); });
            tagRow.add(chip);
        }
        JScrollPane tagScroll = new JScrollPane(tagRow);
        tagScroll.setBorder(null);
        tagScroll.setPreferredSize(new Dimension(0, 56));
        tagScroll.setAlignmentX(LEFT_ALIGNMENT);
        header.add(tagScroll);
        header.add(Box.createVerticalStrut(8));

        add(header, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void performSearch() {
        resultsPanel.removeAll();
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { resultsPanel.revalidate(); resultsPanel.repaint(); return; }

        // Search events
        resultsPanel.add(UIHelper.createSectionLabel("Events"));
        resultsPanel.add(Box.createVerticalStrut(4));
        ArrayList<Event> events = Database.getAllEvents();
        boolean foundEvent = false;
        for (Event ev : events) {
            if (ev.matchesSearch(query)) {
                foundEvent = true;
                JButton btn = new JButton(ev.getTitle() + "  \u2022  " + ev.getLocation() + "  \u2022  " + ev.getDateStr());
                btn.setFont(AppConstants.F_NORMAL);
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setBorderPainted(false);
                btn.setBackground(Color.WHITE);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setAlignmentX(LEFT_ALIGNMENT);
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(AppConstants.PRIMARY_LIGHT); }
                    public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(Color.WHITE); }
                });
                btn.addActionListener(e -> home.showEventDetail(ev));
                resultsPanel.add(btn);
            }
        }
        if (!foundEvent) resultsPanel.add(UIHelper.createSmallLabel("No events found."));

        resultsPanel.add(Box.createVerticalStrut(16));
        resultsPanel.add(UIHelper.createSectionLabel("Users"));
        resultsPanel.add(Box.createVerticalStrut(4));
        ArrayList<User> users = Database.getAllUsers();
        boolean foundUser = false;
        for (User u : users) {
            if (u.matchesSearch(query)) {
                foundUser = true;
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
                row.setBackground(Color.WHITE);
                row.setAlignmentX(LEFT_ALIGNMENT);
                row.add(UIHelper.createClickableUsername(u, home));
                int xp = Database.getUserXP(u.getUsername());
                JLabel tierLbl = new JLabel(AppConstants.getTierName(xp) + " \u2022 " + xp + " XP");
                tierLbl.setFont(AppConstants.F_TINY);
                tierLbl.setForeground(AppConstants.getTierColor(xp));
                row.add(tierLbl);
                resultsPanel.add(row);
            }
        }
        if (!foundUser) resultsPanel.add(UIHelper.createSmallLabel("No users found."));

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
}
