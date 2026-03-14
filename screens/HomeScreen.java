package screens;

import model.*;
import panels.*;
import tools.*;

import javax.swing.*;
import java.awt.*;

/*
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                    <<class>> HomeScreen                            │
 * │                     extends JFrame                                │
 * │                Main application window                             │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ - cardLayout, contentPanel -> CardLayout-based navigation          │
 * │ - profileStack: Stack<User> -> navigation history for profiles    │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ NAVIGATION:                                                        │
 * │  + showFeed() -> displays FeedPanel                                │
 * │  + showMyProfile() -> displays own ProfilePanel                   │
 * │  + navigateToProfile(user) -> displays other user's profile       │
 * │  + goBackFromProfile() -> pops profile stack                      │
 * │  + showEventDetail(event) -> displays EventDetailPanel            │
 * │                                                                     │
 * │ BUSINESS LOGIC:                                                     │
 * │  + changeAttendance(event, status) -> RSVP + XP award             │
 * │  + addComment(event, text, parentId) -> comment + notifications   │
 * │  + followUser(target) / unfollowUser(target) -> follow + XP       │
 * │  + createEvent(event) -> save + notify followers                  │
 * │                                                                     │
 * │ UI BUILDERS:                                                        │
 * │  - buildTopNav() -> top navigation bar with search, create, etc.  │
 * │  - buildContent() -> initializes all panels in CardLayout         │
 * │  - createNavLink() / makeIconBtn() -> nav button factories        │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ USES:    All panels (Feed, Discover, Calendar, Messaging, etc.),   │
 * │          Database, UIHelper, MainFile, AppConstants                 │
 * │ USED BY: LoginScreen (creates on login), all panels (navigation)  │
 * └─────────────────────────────────────────────────────────────────────┘
 */
public class HomeScreen extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private ProfilePanel myProfilePanel;
    private ProfilePanel viewedProfilePanel;
    private java.util.Stack<User> profileStack = new java.util.Stack<>();

    public HomeScreen() {
        setTitle("League of Bilkent - " + MainFile.currentUser.getDisplayName());
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppConstants.BG_MAIN);

        add(buildTopNav(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        cardLayout.show(contentPanel, "feed");
    }

    private JPanel buildTopNav() {
        JPanel nav = new JPanel(new BorderLayout(0, 0));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)));
        nav.setPreferredSize(new Dimension(0, 54));

        // Left: brand
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        left.setOpaque(false);
        JLabel dropIcon = new JLabel("\uD83D\uDCA7");
        dropIcon.setFont(new Font("SansSerif", Font.PLAIN, 22));
        left.add(dropIcon);
        JLabel brand = new JLabel("League of Bilkent");
        brand.setFont(new Font("SansSerif", Font.BOLD, 16));
        brand.setForeground(AppConstants.TEXT_PRI);
        left.add(brand);
        nav.add(left, BorderLayout.WEST);

        // Center: navigation links
        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        links.setOpaque(false);
        String[] tabs = {"Feed", "Discover", "Calendar", "Messages", "Leaderboard"};
        String[] actions = {"feed", "discover", "calendar", "messages", "leaderboard"};
        for (int i = 0; i < tabs.length; i++) {
            JButton btn = createNavLink(tabs[i], i == 0);
            final String action = actions[i];
            btn.addActionListener(e -> {
                switch (action) {
                    case "feed": showFeed(); break;
                    case "discover": showPanel(new DiscoverPanel(this), "discover"); break;
                    case "calendar": showPanel(new CalendarPanel(this), "calendar"); break;
                    case "messages": showPanel(new MessagingPanel(this), "messages"); break;
                    case "leaderboard": showPanel(new LeaderboardPanel(this), "leaderboard"); break;
                }
            });
            links.add(btn);
        }
        nav.add(links, BorderLayout.CENTER);

        // Right: search + create + notif + avatar + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setOpaque(false);

        JTextField searchField = UIHelper.createPlaceholderField("Search...");
        searchField.setPreferredSize(new Dimension(160, 34));
        searchField.addActionListener(e -> {
            String q = UIHelper.getFieldText(searchField, "Search...");
            if (!q.isEmpty()) {
                SearchPanel sp = new SearchPanel(this);
                sp.searchFor(q);
                showPanel(sp, "search");
            }
        });
        right.add(searchField);

        JButton btnNotif = makeIconBtn("\uD83D\uDD14");
        btnNotif.addActionListener(e -> showPanel(new NotificationsPanel(this), "notif"));
        right.add(btnNotif);

        JButton btnCreate = new JButton("\u2728 Create");
        btnCreate.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setBackground(AppConstants.TEAL);
        btnCreate.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        btnCreate.setFocusPainted(false);
        btnCreate.setOpaque(true);
        btnCreate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCreate.addActionListener(e -> showPanel(new CreateEventPanel(this), "create"));
        right.add(btnCreate);

        // Avatar
        String initials = MainFile.currentUser.getDisplayName().substring(0, Math.min(2, MainFile.currentUser.getDisplayName().length())).toUpperCase();
        JButton avatar = new JButton(initials);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 11));
        avatar.setForeground(Color.WHITE);
        avatar.setBackground(AppConstants.TEAL_DARK);
        avatar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        avatar.setFocusPainted(false);
        avatar.setOpaque(true);
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.addActionListener(e -> showMyProfile());
        right.add(avatar);

        JButton btnLogout = makeIconBtn("\uD83D\uDEAA");
        btnLogout.setForeground(AppConstants.DANGER);
        btnLogout.addActionListener(e -> {
            dispose();
            MainFile.currentUser = null;
            MainFile.loginScreen.setVisible(true);
        });
        right.add(btnLogout);

        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppConstants.BG_MAIN);
        contentPanel.add(new FeedPanel(this), "feed");
        contentPanel.add(new SearchPanel(this), "search");
        contentPanel.add(new DiscoverPanel(this), "discover");
        contentPanel.add(new CalendarPanel(this), "calendar");
        contentPanel.add(new LeaderboardPanel(this), "leaderboard");
        contentPanel.add(new CreateEventPanel(this), "create");
        contentPanel.add(new MessagingPanel(this), "messages");
        contentPanel.add(new NotificationsPanel(this), "notif");
        return contentPanel;
    }

    private void showPanel(JPanel panel, String name) {
        contentPanel.add(panel, name);
        cardLayout.show(contentPanel, name);
    }

    public void showFeed() { showPanel(new FeedPanel(this), "feed"); }

    public void showMyProfile() {
        myProfilePanel = new ProfilePanel(MainFile.currentUser, this, false);
        showPanel(myProfilePanel, "myProfile");
    }

    public void navigateToProfile(User user) {
        if (user.getUsername().equals(MainFile.currentUser.getUsername())) { showMyProfile(); return; }
        profileStack.push(user);
        viewedProfilePanel = new ProfilePanel(user, this, true);
        showPanel(viewedProfilePanel, "viewProfile");
    }

    public void goBackFromProfile() {
        if (!profileStack.isEmpty()) profileStack.pop();
        if (!profileStack.isEmpty()) {
            User prev = profileStack.peek();
            viewedProfilePanel = new ProfilePanel(prev, this, true);
            showPanel(viewedProfilePanel, "viewProfile");
        } else { showFeed(); }
    }

    public void showEventDetail(Event event) {
        showPanel(new EventDetailPanel(event, this), "detail");
    }

    public void changeAttendance(Event event, AttendanceStatus status) {
        String me = MainFile.currentUser.getUsername();
        if (status == null) {
            event.removeAttendance(me);
            Database.removeAttendance(event.getId(), me);
        } else {
            if (status == AttendanceStatus.GOING && event.isFull()) { UIHelper.showError(this, "Event is full!"); return; }
            event.setAttendance(me, status);
            Database.setAttendance(event.getId(), me, status);
            Database.addXP(me, AppConstants.XP_ATTEND_EVENT);
            if (event.getXpReward() > 0) Database.addXP(me, event.getXpReward());
        }
    }

    public void addComment(Event event, String text, int parentId) {
        String me = MainFile.currentUser.getUsername();
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        Comment comment = new Comment(0, me, text, time, parentId);
        int cid = Database.addToDatabase(comment, event.getId());
        comment = new Comment(cid, me, text, time, parentId);
        event.addComment(comment);
        Database.addXP(me, AppConstants.XP_COMMENT);
        if (!me.equals(event.getCreatorUsername()))
            Database.addNotification(event.getCreatorUsername(), MainFile.currentUser.getDisplayName() + " commented on: " + event.getTitle());
        if (parentId > 0) {
            for (Comment c : event.getComments()) {
                if (c.getId() == parentId && !c.getUsername().equals(me)) {
                    Database.addNotification(c.getUsername(), MainFile.currentUser.getDisplayName() + " replied to your comment on: " + event.getTitle());
                    break;
                }
            }
        }
    }

    public void followUser(String target) {
        String me = MainFile.currentUser.getUsername();
        if (MainFile.currentUser.getFollowing().contains(target)) return;
        Database.addFollow(me, target);
        MainFile.currentUser.getFollowing().add(target);
        Database.addXP(target, AppConstants.XP_GAIN_FOLLOWER);
        Database.addNotification(target, MainFile.currentUser.getDisplayName() + " started following you!");
    }

    public void unfollowUser(String target) {
        String me = MainFile.currentUser.getUsername();
        Database.deleteFollow(me, target);
        MainFile.currentUser.getFollowing().remove(target);
        Database.addXP(target, -AppConstants.XP_GAIN_FOLLOWER);
    }

    public void createEvent(Event event) {
        int id = Database.addToDatabase(event);
        event.setId(id);
        Database.addXP(MainFile.currentUser.getUsername(), AppConstants.XP_CREATE_EVENT);
        for (String follower : MainFile.currentUser.getFollowers())
            Database.addNotification(follower, MainFile.currentUser.getDisplayName() + " created a new event: " + event.getTitle());
    }

    private JButton createNavLink(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
        b.setForeground(active ? AppConstants.TEAL : AppConstants.TEXT_SEC);
        b.setBackground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        if (active) b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, AppConstants.TEAL),
            BorderFactory.createEmptyBorder(16, 14, 14, 14)));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { if (!active) b.setForeground(AppConstants.TEAL); }
            public void mouseExited(java.awt.event.MouseEvent e) { if (!active) b.setForeground(AppConstants.TEXT_SEC); }
        });
        return b;
    }

    private JButton makeIconBtn(String emoji) {
        JButton b = new JButton(emoji);
        b.setFont(new Font("SansSerif", Font.PLAIN, 16));
        b.setBackground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return b;
    }
}
