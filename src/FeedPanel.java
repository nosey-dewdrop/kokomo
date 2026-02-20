import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Feed panel - poster grid layout (like cinema posters).
 */
public class FeedPanel extends JPanel {

    private HomeScreen home;
    private JPanel gridPanel;
    private String currentFilter = "All";
    private ArrayList<String> userTagFilters;

    public FeedPanel(HomeScreen home) {
        this.home = home;
        this.userTagFilters = Database.getUserTagFilters(MainFile.currentUser.getUsername());
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 36, 20, 36));
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Events");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(AppConstants.TEXT_PRI);
        title.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(2));

        JLabel sub = new JLabel("See what's happening on campus.");
        sub.setFont(AppConstants.F_SMALL);
        sub.setForeground(AppConstants.TEXT_SEC);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        header.add(sub);
        header.add(Box.createVerticalStrut(16));

        // Filter chips
        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        chipRow.setBackground(Color.WHITE);
        chipRow.setAlignmentX(LEFT_ALIGNMENT);

        String[] baseFilters = {"All", "Following", "Clubs", "This Week"};
        for (String f : baseFilters) {
            JButton chip = createChip(f, f.equals(currentFilter));
            chip.addActionListener(e -> { currentFilter = f; refreshGrid(); });
            chipRow.add(chip);
        }

        // User custom tag filters
        for (String tag : userTagFilters) {
            JButton chip = createChip("#" + tag, false);
            chip.addActionListener(e -> { currentFilter = "TAG:" + tag; refreshGrid(); });
            chipRow.add(chip);
        }

        // Edit tags button
        JButton editTags = createChip("+ Tags", false);
        editTags.setForeground(AppConstants.ACCENT);
        editTags.addActionListener(e -> editTagFilters());
        chipRow.add(editTags);

        header.add(chipRow);
        header.add(Box.createVerticalStrut(16));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(AppConstants.BORDER);
        sep.setAlignmentX(LEFT_ALIGNMENT);
        header.add(sep);
        header.add(Box.createVerticalStrut(16));

        add(header, BorderLayout.NORTH);

        // Grid
        gridPanel = new JPanel(new GridLayout(0, AppConstants.FEED_COLUMNS, 12, 12));
        gridPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refreshGrid();
    }

    public void refreshGrid() {
        gridPanel.removeAll();
        ArrayList<Event> events = getFilteredEvents();
        if (events.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel("No events match your filter.", JLabel.CENTER);
            empty.setFont(AppConstants.F_NORMAL);
            empty.setForeground(AppConstants.TEXT_LIGHT);
            gridPanel.add(empty, BorderLayout.CENTER);
        } else {
            gridPanel.setLayout(new GridLayout(0, AppConstants.FEED_COLUMNS, 12, 12));
            for (Event ev : events) gridPanel.add(createPosterCard(ev));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private ArrayList<Event> getFilteredEvents() {
        ArrayList<Event> all = Database.getAllEvents();
        String me = MainFile.currentUser.getUsername();
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
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Try load poster image
                BufferedImage img = null;
                String path = ev.getImagePath();
                if (path != null && !path.isEmpty()) {
                    try { img = javax.imageio.ImageIO.read(new File(path)); } catch (Exception ignored) {}
                }

                if (img != null) {
                    // Draw scaled image
                    double scale = Math.max((double)w / img.getWidth(), (double)h / img.getHeight());
                    int iw = (int)(img.getWidth() * scale), ih = (int)(img.getHeight() * scale);
                    g2.drawImage(img, (w-iw)/2, (h-ih)/2, iw, ih, null);
                } else {
                    // Gradient fallback based on event id
                    Color[] gradients = {
                        new Color(0x1a1a2e), new Color(0x0f3460), new Color(0x2d1b4e),
                        new Color(0x0a3d2e), new Color(0x4a2020), new Color(0x3d2e0a)
                    };
                    Color c = gradients[Math.abs(ev.getId()) % gradients.length];
                    GradientPaint gp = new GradientPaint(0, 0, c, w, h, new Color(c.getRed()/2, c.getGreen()/2, c.getBlue()/2));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, w, h);
                }

                // Dark overlay at bottom
                GradientPaint overlay = new GradientPaint(0, h*0.4f, new Color(0,0,0,0), 0, h, new Color(0,0,0,200));
                g2.setPaint(overlay);
                g2.fillRect(0, 0, w, h);
            }
        };
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 280));

        // Top badges (XP + tier requirement)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        if (ev.getMinTierIndex() > 0) {
            JLabel tierBadge = makeBadge("Min: " + ev.getMinTierName());
            topRow.add(tierBadge, BorderLayout.WEST);
        }
        JLabel xpBadge = makeBadge("+" + ev.getXpReward() + " XP");
        topRow.add(xpBadge, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Bottom info
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JLabel titleLbl = new JLabel(ev.getTitle());
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(titleLbl);

        JLabel creatorLbl = new JLabel("@" + ev.getCreatorUsername());
        creatorLbl.setFont(AppConstants.F_TINY);
        creatorLbl.setForeground(new Color(255,255,255,160));
        creatorLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(creatorLbl);
        bottom.add(Box.createVerticalStrut(4));

        String info = ev.getLocation() + "  |  " + ev.getDateStr() + "  |  " + ev.getGoingCount() + "/" + ev.getCapacity();
        JLabel infoLbl = new JLabel(info);
        infoLbl.setFont(AppConstants.F_TINY);
        infoLbl.setForeground(new Color(255,255,255,130));
        infoLbl.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(infoLbl);
        bottom.add(Box.createVerticalStrut(4));

        // Tags
        if (!ev.getTags().isEmpty()) {
            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(LEFT_ALIGNMENT);
            for (String tag : ev.getTags()) {
                JLabel tl = new JLabel("#" + tag);
                tl.setFont(AppConstants.F_TINY);
                tl.setForeground(new Color(255,255,255,100));
                tagRow.add(tl);
            }
            bottom.add(tagRow);
            bottom.add(Box.createVerticalStrut(6));
        }

        // Action button
        String me = MainFile.currentUser.getUsername();
        boolean isGoing = ev.isAttending(me);
        boolean isCreator = ev.getCreatorUsername().equals(me);

        JButton btn;
        if (isCreator) {
            btn = makeCardButton("Your Event", new Color(255,255,255,40), Color.WHITE);
            btn.setEnabled(false);
        } else if (isGoing) {
            btn = makeCardButton("Leave", new Color(235,87,87,40), new Color(0xEB, 0x57, 0x57));
            btn.addActionListener(e -> {
                home.changeAttendance(ev, null);
                refreshGrid();
            });
        } else {
            btn = makeCardButton("Join", new Color(0x23,0x83,0xE2), Color.WHITE);
            btn.addActionListener(e -> {
                int myXP = Database.getUserXP(me);
                if (!ev.canJoin(myXP)) {
                    UIHelper.showError(this, "You need " + ev.getMinTierName() + " tier or above to join!");
                    return;
                }
                home.changeAttendance(ev, AttendanceStatus.GOING);
                refreshGrid();
            });
        }
        btn.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(btn);

        card.add(bottom, BorderLayout.SOUTH);

        // Click to open detail
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() != btn) home.showEventDetail(ev);
            }
        });

        return card;
    }

    private JLabel makeBadge(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.F_TINY);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(0,0,0,128));
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return lbl;
    }

    private JButton makeCardButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(100, 28));
        return b;
    }

    private JButton createChip(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        b.setForeground(active ? Color.WHITE : AppConstants.TEXT_SEC);
        b.setBackground(active ? AppConstants.PRIMARY : Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(active ? AppConstants.PRIMARY : AppConstants.BORDER, 1),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)));
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
            // Rebuild UI
            removeAll();
            buildUI();
            revalidate();
            repaint();
        }
    }
}
