package panels;

import model.*;
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

    public FeedPanel(HomeScreen home) {
        this.home = home;
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buildUI();
    }

    private void buildUI() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppConstants.BG_MAIN);
        body.setBorder(BorderFactory.createEmptyBorder(24, 48, 20, 48));

        // Hero
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setOpaque(false);
        hero.setAlignmentX(CENTER_ALIGNMENT);

        JLabel eyebrow = new JLabel("your feed");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        eyebrow.setForeground(AppConstants.TEAL);
        eyebrow.setAlignmentX(CENTER_ALIGNMENT);
        hero.add(eyebrow);
        hero.add(Box.createVerticalStrut(6));

        JLabel heroTitle = new JLabel("What's happening around you.");
        heroTitle.setFont(AppConstants.F_HERO);
        heroTitle.setForeground(AppConstants.TEXT_PRI);
        heroTitle.setAlignmentX(CENTER_ALIGNMENT);
        hero.add(heroTitle);
        hero.add(Box.createVerticalStrut(6));

        JLabel heroSub = new JLabel("Events from people you follow, personalized for you.");
        heroSub.setFont(AppConstants.F_SMALL);
        heroSub.setForeground(AppConstants.TEXT_SEC);
        heroSub.setAlignmentX(CENTER_ALIGNMENT);
        hero.add(heroSub);
        hero.add(Box.createVerticalStrut(16));
        body.add(hero);

        // XP Strip
        int myXP = Database.getUserXP(MainFile.currentUser.getUsername());
        String tierName = AppConstants.getTierName(myXP);
        int nextXP = AppConstants.getNextTierXP(myXP);
        String nextName = AppConstants.getNextTierName(myXP);

        JPanel xpStrip = new JPanel(new BorderLayout(12, 0));
        xpStrip.setBackground(Color.WHITE);
        xpStrip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        xpStrip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        xpStrip.setAlignmentX(LEFT_ALIGNMENT);

        JPanel xpLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        xpLeft.setOpaque(false);
        JLabel tierLbl = new JLabel("\u2728 " + tierName);
        tierLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        tierLbl.setForeground(AppConstants.getTierColor(myXP));
        xpLeft.add(tierLbl);
        JLabel ptsLbl = new JLabel(myXP + " XP");
        ptsLbl.setFont(AppConstants.F_SMALL);
        ptsLbl.setForeground(AppConstants.TEXT_SEC);
        xpLeft.add(ptsLbl);
        xpStrip.add(xpLeft, BorderLayout.WEST);

        if (nextXP > 0) {
            JPanel xpRight = new JPanel();
            xpRight.setLayout(new BoxLayout(xpRight, BoxLayout.Y_AXIS));
            xpRight.setOpaque(false);
            JProgressBar bar = new JProgressBar(0, nextXP);
            bar.setValue(myXP);
            bar.setStringPainted(false);
            bar.setForeground(AppConstants.TEAL);
            bar.setBackground(AppConstants.TEAL_LIGHT);
            bar.setPreferredSize(new Dimension(200, 8));
            bar.setMaximumSize(new Dimension(200, 8));
            xpRight.add(bar);
            JLabel nextLbl = new JLabel((nextXP - myXP) + " XP to " + nextName);
            nextLbl.setFont(AppConstants.F_TINY);
            nextLbl.setForeground(AppConstants.TEXT_LIGHT);
            xpRight.add(nextLbl);
            xpStrip.add(xpRight, BorderLayout.EAST);
        }
        body.add(xpStrip);
        body.add(Box.createVerticalStrut(20));

        // Pills
        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pillRow.setOpaque(false);
        pillRow.setAlignmentX(LEFT_ALIGNMENT);
        String[][] pills = {
            {"\uD83C\uDFB5 All", "All"}, {"\uD83C\uDFB6 Music", "TAG:music"},
            {"\uD83C\uDFC3 Sports", "TAG:sports"}, {"\uD83D\uDCDA Study", "TAG:algorithms"},
            {"\uD83C\uDFA8 Arts", "TAG:art"}, {"\uD83C\uDF55 Food", "TAG:food"},
            {"\uD83D\uDCBB Tech", "TAG:software"}, {"\uD83C\uDF3F Outdoor", "TAG:environment"}
        };
        for (String[] pill : pills) {
            JButton p = createPill(pill[0], currentFilter.equals(pill[1]) || (pill[1].equals("All") && currentFilter.equals("All")));
            p.addActionListener(e -> { currentFilter = pill[1]; refreshGrid(); });
            pillRow.add(p);
        }
        body.add(pillRow);
        body.add(Box.createVerticalStrut(18));

        // Section: Upcoming
        body.add(createSectionHeader("01", "Upcoming events", ""));
        body.add(Box.createVerticalStrut(10));

        gridPanel = new JPanel(new GridLayout(0, AppConstants.FEED_COLUMNS, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(gridPanel);

        // For You section
        ArrayList<Integer> recIds = Database.getRecommendedEventIds(MainFile.currentUser.getUsername(), 3);
        if (!recIds.isEmpty()) {
            body.add(Box.createVerticalStrut(24));
            body.add(createSectionHeader("02", "For you", "Based on your interests"));
            body.add(Box.createVerticalStrut(10));

            ArrayList<Event> allEvts = Database.getAllEvents();
            ArrayList<String> myI = Database.getInterests(MainFile.currentUser.getUsername());
            for (int rid : recIds) {
                for (Event rev : allEvts) {
                    if (rev.getId() == rid) {
                        body.add(createRecRow(rev, myI));
                        break;
                    }
                }
            }
        }

        add(UIHelper.wrapInScroll(body), BorderLayout.CENTER);
        refreshGrid();
    }

    public void refreshGrid() {
        gridPanel.removeAll();
        ArrayList<Event> events = getFilteredEvents();
        sortEvents(events);
        if (events.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel("No events yet. Create one!", JLabel.CENTER);
            empty.setFont(AppConstants.F_NORMAL);
            empty.setForeground(AppConstants.TEXT_MUTED);
            gridPanel.add(empty, BorderLayout.CENTER);
        } else {
            gridPanel.setLayout(new GridLayout(0, AppConstants.FEED_COLUMNS, 16, 16));
            for (Event ev : events) gridPanel.add(createCard(ev));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createCard(Event ev) {
        int R = AppConstants.CARD_RADIUS;
        Color posterBg = PosterGenerator.getColorForEvent(ev.getId());
        String catEmoji = PosterGenerator.getCategoryEmoji(ev.getTags());

        final String emoji = catEmoji;
        final Color bgColor = posterBg;
        String firstTag = ev.getTags().isEmpty() ? "" : ev.getTags().get(0).toLowerCase();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Shadow
                g2.setColor(new Color(0, 100, 120, 14));
                g2.fillRoundRect(2, 4, w - 2, h - 2, R, R);

                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h - 2, R, R));

                // Poster area - top 45%
                int posterH = (int)(h * 0.42);
                BufferedImage img = loadImage(ev.getImagePath());
                if (img != null) {
                    double scale = Math.max((double)w/img.getWidth(), (double)posterH/img.getHeight());
                    int iw=(int)(img.getWidth()*scale), ih=(int)(img.getHeight()*scale);
                    g2.drawImage(img, (w-iw)/2, (posterH-ih)/2, iw, ih, null);
                } else {
                    g2.setColor(bgColor);
                    g2.fillRect(0, 0, w, posterH);
                    // Big emoji
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 48));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(emoji, w/2 - fm.stringWidth(emoji)/2, posterH/2 + 16);
                }

                // White body
                g2.setColor(Color.WHITE);
                g2.fillRect(0, posterH, w, h - posterH);

                g2.setClip(null);
                g2.setColor(AppConstants.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 3, R, R);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 310));

        // Badge
        JPanel badgeRow = new JPanel(new BorderLayout());
        badgeRow.setOpaque(false);
        badgeRow.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        if (!firstTag.isEmpty()) {
            JLabel badge = new JLabel(firstTag.substring(0,1).toUpperCase() + firstTag.substring(1));
            badge.setFont(new Font("SansSerif", Font.BOLD, 10));
            badge.setForeground(AppConstants.TEAL_DARK);
            badge.setOpaque(true);
            badge.setBackground(new Color(255,255,255,200));
            badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            badgeRow.add(badge, BorderLayout.WEST);
        }
        card.add(badgeRow, BorderLayout.NORTH);

        // Body
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));

        JLabel dateLbl = new JLabel(ev.getDateStr());
        dateLbl.setFont(AppConstants.F_TINY);
        dateLbl.setForeground(AppConstants.TEXT_LIGHT);
        dateLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(dateLbl);
        bottom.add(Box.createVerticalStrut(3));

        JLabel titleLbl = new JLabel(ev.getTitle());
        titleLbl.setFont(AppConstants.F_SECTION);
        titleLbl.setForeground(AppConstants.TEXT_PRI);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(titleLbl);
        bottom.add(Box.createVerticalStrut(3));

        JLabel locLbl = new JLabel("\uD83D\uDCCD " + ev.getLocation());
        locLbl.setFont(AppConstants.F_TINY);
        locLbl.setForeground(AppConstants.TEXT_SEC);
        locLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(locLbl);
        bottom.add(Box.createVerticalStrut(8));

        // Footer: going count + RSVP
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(LEFT_ALIGNMENT);

        JLabel goingLbl = new JLabel(ev.getGoingCount() + " going");
        goingLbl.setFont(AppConstants.F_TINY);
        goingLbl.setForeground(AppConstants.TEXT_LIGHT);
        footer.add(goingLbl, BorderLayout.WEST);

        String me = MainFile.currentUser.getUsername();
        AttendanceStatus myStatus = ev.getAttendanceStatus(me);
        JButton rsvp;
        if (myStatus == AttendanceStatus.GOING) {
            rsvp = new JButton("Going \u2713");
            rsvp.setForeground(Color.WHITE);
            rsvp.setBackground(AppConstants.TEAL);
            rsvp.setOpaque(true);
        } else {
            rsvp = new JButton("RSVP");
            rsvp.setForeground(AppConstants.TEAL);
            rsvp.setBackground(Color.WHITE);
        }
        rsvp.setFont(new Font("SansSerif", Font.BOLD, 11));
        rsvp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.TEAL, 1, true),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        rsvp.setFocusPainted(false);
        rsvp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footer.add(rsvp, BorderLayout.EAST);

        bottom.add(footer);
        card.add(bottom, BorderLayout.SOUTH);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { home.showEventDetail(ev); }
        });
        return card;
    }

    private JPanel createRecRow(Event ev, ArrayList<String> interests) {
        String reason = "";
        for (String tag : ev.getTags())
            for (String i : interests)
                if (tag.equalsIgnoreCase(i)) { reason = i; break; }

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel name = new JLabel(ev.getTitle());
        name.setFont(AppConstants.F_SECTION);
        info.add(name);
        String whyText = reason.isEmpty() ? "Recommended for you" : "Because you like " + reason;
        JLabel why = new JLabel(whyText);
        why.setFont(AppConstants.F_TINY);
        why.setForeground(AppConstants.TEAL);
        info.add(why);
        row.add(info, BorderLayout.CENTER);

        JButton vb = UIHelper.createOutlineButton("Details \u2192", AppConstants.TEAL);
        vb.addActionListener(e -> home.showEventDetail(ev));
        row.add(vb, BorderLayout.EAST);
        return row;
    }

    private JPanel createSectionHeader(String num, String title, String sub) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel numLbl = new JLabel(num);
        numLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        numLbl.setForeground(AppConstants.TEAL);
        left.add(numLbl);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppConstants.F_TITLE);
        titleLbl.setForeground(AppConstants.TEXT_PRI);
        left.add(titleLbl);
        header.add(left, BorderLayout.WEST);

        return header;
    }

    private JButton createPill(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        b.setForeground(active ? Color.WHITE : AppConstants.TEXT_SEC);
        b.setBackground(active ? AppConstants.TEAL : Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(active ? AppConstants.TEAL : AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void sortEvents(ArrayList<Event> events) {
        switch (currentSort) {
            case "Location": events.sort(Comparator.comparing(Event::getLocation, String.CASE_INSENSITIVE_ORDER)); break;
            case "XP Reward": events.sort((a, b) -> b.getXpReward() - a.getXpReward()); break;
            case "Popularity": events.sort((a, b) -> b.getGoingCount() - a.getGoingCount()); break;
            default: events.sort(Comparator.comparing(Event::getDateTime)); break;
        }
    }

    private ArrayList<Event> getFilteredEvents() {
        ArrayList<Event> all = Database.getAllEvents();
        return all.stream().filter(ev -> {
            if (currentFilter.equals("All")) return true;
            if (currentFilter.startsWith("TAG:")) {
                String tag = currentFilter.substring(4);
                return ev.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag));
            }
            return true;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private BufferedImage loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        try { return javax.imageio.ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }
}
