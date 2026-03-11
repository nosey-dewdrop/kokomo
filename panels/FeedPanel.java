package panels;

import model.*;
import model.Event;
import model.Event;
import screens.*;
import tools.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FeedPanel extends JPanel {

    private HomeScreen home;
    private JPanel gridPanel;
    private String currentFilter = "All";
    private String currentSort = "Date";
    private ArrayList<String> userTagFilters;

    public FeedPanel(HomeScreen home) {
        this.home = home;
        this.userTagFilters = Database.getUserTagFilters(MainFile.currentUser.getUsername());
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(
            AppConstants.PAGE_PAD_Y, AppConstants.PAGE_PAD_X, 16, AppConstants.PAGE_PAD_X));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        header.add(UIHelper.createPageTitle("Events"));
        header.add(Box.createVerticalStrut(4));
        header.add(UIHelper.createSubtitle("See what\u2019s happening on campus."));
        header.add(Box.createVerticalStrut(18));

        // Filter + Sort row
        JPanel controlRow = new JPanel(new BorderLayout());
        controlRow.setBackground(Color.WHITE);
        controlRow.setAlignmentX(LEFT_ALIGNMENT);

        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chipRow.setBackground(Color.WHITE);
        for (String f : new String[]{"All", "Following", "Clubs", "This Week"}) {
            JButton chip = createChip(f, f.equals(currentFilter));
            chip.addActionListener(e -> { currentFilter = f; refreshGrid(); });
            chipRow.add(chip);
        }
        for (String tag : userTagFilters) {
            JButton chip = createChip("#" + tag, false);
            chip.addActionListener(e -> { currentFilter = "TAG:" + tag; refreshGrid(); });
            chipRow.add(chip);
        }
        JButton editTags = createChip("+ Tags", false);
        editTags.setForeground(AppConstants.ACCENT);
        editTags.addActionListener(e -> editTagFilters());
        chipRow.add(editTags);
        controlRow.add(chipRow, BorderLayout.CENTER);

        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        sortRow.setBackground(Color.WHITE);
        JLabel sortLbl = new JLabel("Sort:");
        sortLbl.setFont(AppConstants.F_TINY);
        sortLbl.setForeground(AppConstants.TEXT_SEC);
        JComboBox<String> sortBox = new JComboBox<>(new String[]{"Date", "Location", "XP Reward", "Popularity"});
        sortBox.setFont(AppConstants.F_TINY);
        sortBox.setSelectedItem(currentSort);
        sortBox.addActionListener(e -> { currentSort = (String) sortBox.getSelectedItem(); refreshGrid(); });
        sortRow.add(sortLbl);
        sortRow.add(sortBox);
        controlRow.add(sortRow, BorderLayout.EAST);

        header.add(controlRow);
        header.add(Box.createVerticalStrut(16));
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);

        // For You section
        ArrayList<Integer> recIds = Database.getRecommendedEventIds(MainFile.currentUser.getUsername(), 4);
        if (!recIds.isEmpty()) {
            ArrayList<Event> allEvts = Database.getAllEvents();
            ArrayList<String> myInterests = Database.getInterests(MainFile.currentUser.getUsername());
            JPanel fy = new JPanel();
            fy.setLayout(new BoxLayout(fy, BoxLayout.Y_AXIS));
            fy.setBackground(Color.WHITE);
            fy.setAlignmentX(LEFT_ALIGNMENT);

            JLabel fyTitle = new JLabel("\u2728  For You");
            fyTitle.setFont(AppConstants.F_TITLE);
            fyTitle.setForeground(AppConstants.TEXT_PRI);
            fyTitle.setAlignmentX(LEFT_ALIGNMENT);
            fy.add(fyTitle);
            fy.add(Box.createVerticalStrut(10));

            for (int rid : recIds) {
                for (Event rev : allEvts) {
                    if (rev.getId() == rid) {
                        String reason = findMatchingInterest(rev, myInterests);
                        fy.add(createRecommendRow(rev, reason));
                        break;
                    }
                }
            }
            fy.add(Box.createVerticalStrut(20));
            body.add(fy);
        }

        gridPanel = new JPanel(new GridLayout(0, AppConstants.FEED_COLUMNS, 18, 18));
        gridPanel.setBackground(Color.WHITE);
        body.add(gridPanel);

        add(UIHelper.wrapInScroll(body), BorderLayout.CENTER);
        refreshGrid();
    }

    public void refreshGrid() {
        gridPanel.removeAll();
        ArrayList<Event> events = getFilteredEvents();
        sortEvents(events);

        if (events.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel("No events match your filter.", JLabel.CENTER);
            empty.setFont(AppConstants.F_NORMAL);
            empty.setForeground(AppConstants.TEXT_LIGHT);
            gridPanel.add(empty, BorderLayout.CENTER);
        } else {
            gridPanel.setLayout(new GridLayout(0, AppConstants.FEED_COLUMNS, 18, 18));
            for (Event ev : events) gridPanel.add(createPosterCard(ev));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private String findMatchingInterest(Event ev, ArrayList<String> interests) {
        for (String tag : ev.getTags())
            for (String interest : interests)
                if (tag.equalsIgnoreCase(interest)) return interest;
        return "";
    }

    private JPanel createRecommendRow(Event ev, String reason) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(AppConstants.PRIMARY_LIGHT);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel name = new JLabel(ev.getTitle());
        name.setFont(AppConstants.F_SECTION);
        info.add(name);
        String whyText = reason.isEmpty() ? "Recommended for you" : "Because you\u2019re interested in " + reason;
        JLabel why = new JLabel(whyText);
        why.setFont(AppConstants.F_TINY);
        why.setForeground(AppConstants.INTERESTED);
        info.add(why);
        row.add(info, BorderLayout.CENTER);

        JButton vb = UIHelper.createOutlineButton("View", AppConstants.ACCENT);
        vb.addActionListener(e -> home.showEventDetail(ev));
        row.add(vb, BorderLayout.EAST);
        return row;
    }

    private void sortEvents(ArrayList<Event> events) {
        switch (currentSort) {
            case "Location":   events.sort(Comparator.comparing(Event::getLocation, String.CASE_INSENSITIVE_ORDER)); break;
            case "XP Reward":  events.sort((a, b) -> b.getXpReward() - a.getXpReward()); break;
            case "Popularity": events.sort((a, b) -> b.getGoingCount() - a.getGoingCount()); break;
            default:           events.sort(Comparator.comparing(Event::getDateTime)); break;
        }
    }

    private ArrayList<Event> getFilteredEvents() {
        ArrayList<Event> all = Database.getAllEvents();
        return all.stream().filter(ev -> {
            switch (currentFilter) {
                case "Following":
                    return MainFile.currentUser.getFollowing().contains(ev.getCreatorUsername());
                case "Clubs":
                    User creator = Database.getUserWithUsername(ev.getCreatorUsername());
                    return creator != null && creator.isClub();
                case "This Week":
                    return ev.getDateTime().isAfter(java.time.LocalDateTime.now()) &&
                           ev.getDateTime().isBefore(java.time.LocalDateTime.now().plusDays(7));
                default:
                    if (currentFilter.startsWith("TAG:")) {
                        String tag = currentFilter.substring(4);
                        return ev.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag));
                    }
                    return true;
            }
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private JPanel createPosterCard(Event ev) {
        int R = AppConstants.CARD_RADIUS;
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(2, 3, w - 2, h - 2, R + 2, R + 2);
                g2.setClip(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, R, R));

                int posterH = (int) (h * 0.52);
                BufferedImage img = loadImage(ev.getImagePath());
                if (img != null) {
                    double scale = Math.max((double) w / img.getWidth(), (double) posterH / img.getHeight());
                    int iw = (int) (img.getWidth() * scale), ih = (int) (img.getHeight() * scale);
                    g2.drawImage(img, (w - iw) / 2, (posterH - ih) / 2, iw, ih, null);
                } else {
                    Color[] pastels = {
                        new Color(173,216,255), new Color(255,182,193), new Color(176,226,172),
                        new Color(200,170,230), new Color(255,200,150), new Color(150,220,210)
                    };
                    g2.setColor(pastels[Math.abs(ev.getId()) % pastels.length]);
                    g2.fillRect(0, 0, w, posterH);
                }

                g2.setColor(Color.WHITE);
                g2.fillRect(0, posterH, w, h - posterH);
                g2.setClip(null);
                g2.setColor(AppConstants.BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, w - 1, h - 1, R, R);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(220, 320));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        if (ev.getMinTierIndex() > 0) topRow.add(makeBadge("Min: " + ev.getMinTierName()), BorderLayout.WEST);
        topRow.add(makeBadge("+" + ev.getXpReward() + " XP"), BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 14, 16));

        JLabel titleLbl = new JLabel(ev.getTitle());
        titleLbl.setFont(AppConstants.F_SECTION);
        titleLbl.setForeground(AppConstants.TEXT_PRI);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(titleLbl);
        bottom.add(Box.createVerticalStrut(3));

        JLabel creatorLbl = new JLabel("@" + ev.getCreatorUsername());
        creatorLbl.setFont(AppConstants.F_TINY);
        creatorLbl.setForeground(AppConstants.TEXT_SEC);
        creatorLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(creatorLbl);
        bottom.add(Box.createVerticalStrut(5));

        JLabel locLbl = new JLabel(ev.getLocation() + "  \u2022  " + ev.getDateStr());
        locLbl.setFont(AppConstants.F_TINY);
        locLbl.setForeground(AppConstants.TEXT_SEC);
        locLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(locLbl);
        bottom.add(Box.createVerticalStrut(4));

        if (!ev.getTags().isEmpty()) {
            StringBuilder tags = new StringBuilder();
            for (String tag : ev.getTags()) tags.append("#").append(tag).append("  ");
            JLabel tagLbl = new JLabel(tags.toString());
            tagLbl.setFont(AppConstants.F_TINY);
            tagLbl.setForeground(AppConstants.ACCENT);
            tagLbl.setAlignmentX(LEFT_ALIGNMENT);
            bottom.add(tagLbl);
            bottom.add(Box.createVerticalStrut(4));
        }

        JLabel capLbl = new JLabel(ev.getGoingCount() + "/" + ev.getCapacity() + " attending");
        capLbl.setFont(AppConstants.F_TINY);
        capLbl.setForeground(AppConstants.TEXT_LIGHT);
        capLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(capLbl);

        card.add(bottom, BorderLayout.SOUTH);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { home.showEventDetail(ev); }
        });
        return card;
    }

    private BufferedImage loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        try { return javax.imageio.ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

    private JLabel makeBadge(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.F_TINY);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(0, 0, 0, 100));
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return lbl;
    }

    private JButton createChip(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        b.setForeground(active ? Color.WHITE : AppConstants.TEXT_SEC);
        b.setBackground(active ? AppConstants.PRIMARY : Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(active ? AppConstants.PRIMARY : AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void editTagFilters() {
        String input = JOptionPane.showInputDialog(this,
            "Enter tags separated by comma (e.g. software,music,sports):",
            "Edit Tag Filters", JOptionPane.PLAIN_MESSAGE);
        if (input != null) {
            ArrayList<String> tags = new ArrayList<>();
            for (String t : input.split(",")) {
                String trimmed = t.trim().toLowerCase();
                if (!trimmed.isEmpty()) tags.add(trimmed);
            }
            Database.setUserTagFilters(MainFile.currentUser.getUsername(), tags);
            userTagFilters = tags;
            removeAll();
            buildUI();
            revalidate();
            repaint();
        }
    }
}
