package screens;

import model.*;
import model.Event;
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
    private String currentView = "feed";
    private User currentProfileTarget = null;
    private Event currentEventTarget = null;
    private MessagingPanel messagingPanel;
    private JButton messagesNavBtn;

    public HomeScreen() {
        setTitle("League of Bilkent - " + MainFile.currentUser.getDisplayName());
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppConstants.BG_MAIN);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSideNav(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
        cardLayout.show(contentPanel, "feed");
        startPolling();
    }

    private void startPolling() {
        new Thread(() -> {
            int lastHash = Database.getDbStateHash();
            while (true) {
                try { Thread.sleep(1000); } catch(Exception ignored){}
                if (!isVisible()) continue;
                int newHash = Database.getDbStateHash();
                if (newHash != -1 && newHash != lastHash) {
                    lastHash = newHash;
                    SwingUtilities.invokeLater(() -> {
                        refreshCurrentView();
                        updateBadges();
                    });
                }
            }
        }).start();
    }

    private void updateBadges() {
        if (messagesNavBtn != null) {
            int unread = Database.getUnreadMessageCount(MainFile.currentUser.getUsername());
            if (unread > 0) {
                messagesNavBtn.setIcon(new BadgeIcon(unread > 9 ? "9+" : String.valueOf(unread)));
                messagesNavBtn.setHorizontalTextPosition(SwingConstants.LEFT);
                messagesNavBtn.setIconTextGap(10);
            } else {
                messagesNavBtn.setIcon(null);
            }
        }
    }

    private void refreshCurrentView() {
        if(currentView.equals("feed")) showFeed();
        else if(currentView.equals("discover")) showPanel(new DiscoverPanel(this), "discover");
        else if(currentView.equals("calendar")) showPanel(new CalendarPanel(this), "calendar");
        else if(currentView.equals("messages")) {
            if (messagingPanel == null) {
                messagingPanel = new MessagingPanel(this);
                showPanel(messagingPanel, "messages");
            } else {
                messagingPanel.refreshConversations();
                messagingPanel.loadChat();
            }
        }
        else if(currentView.equals("leaderboard")) showPanel(new LeaderboardPanel(this), "leaderboard");
        else if(currentView.equals("notif")) showPanel(new NotificationsPanel(this), "notif");
        else if(currentView.equals("myProfile")) showMyProfile();
        else if(currentView.equals("viewProfile")) {
            if(!profileStack.isEmpty()) {
                currentProfileTarget = profileStack.peek();
                currentProfileTarget = Database.getUserWithUsername(currentProfileTarget.getUsername());
                viewedProfilePanel = new ProfilePanel(currentProfileTarget, this, true);
                contentPanel.add(viewedProfilePanel, "viewProfile");
                cardLayout.show(contentPanel, "viewProfile");
            }
        }
        else if(currentView.equals("detail")) {
            if(currentEventTarget != null) {
                for(Event e : Database.getAllEvents()) {
                    if(e.getId() == currentEventTarget.getId()) {
                       currentEventTarget = e;
                       break;
                    }
                }
                showEventDetail(currentEventTarget);
            }
        }
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        bar.setPreferredSize(new Dimension(0, 48));

        // Brand
        JLabel brand = new JLabel("League of Bilkent");
        brand.setFont(new Font("SansSerif", Font.BOLD, 15));
        brand.setForeground(AppConstants.TEXT_PRI);
        brand.setMaximumSize(brand.getPreferredSize());
        bar.add(brand);

        bar.add(Box.createHorizontalStrut(15));
        String connTxt = Database.customDbUrl == null ? " [Local] " : " [Connected: " + Database.customDbUrl.split("//")[1].split(":")[0] + "] ";
        JLabel statusLbl = new JLabel(connTxt);
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLbl.setForeground(Database.customDbUrl == null ? AppConstants.TEXT_LIGHT : AppConstants.SUCCESS);
        bar.add(statusLbl);

        bar.add(Box.createHorizontalGlue());

        // Search
        JTextField searchField = UIHelper.createPlaceholderField("Search...");
        searchField.setPreferredSize(new Dimension(160, 32));
        searchField.setMaximumSize(new Dimension(160, 32));
        searchField.addActionListener(e -> {
            String q = UIHelper.getFieldText(searchField, "Search...");
            if (!q.isEmpty()) {
                SearchPanel sp = new SearchPanel(this);
                sp.searchFor(q);
                showPanel(sp, "search");
            }
        });
        bar.add(searchField);
        bar.add(Box.createHorizontalStrut(8));

        // Create
        JButton btnCreate = new JButton("+ Create");
        btnCreate.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setBackground(AppConstants.TEAL);
        btnCreate.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnCreate.setFocusPainted(false);
        btnCreate.setOpaque(true);
        btnCreate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCreate.setMaximumSize(new Dimension(100, 32));
        btnCreate.addActionListener(e -> showPanel(new CreateEventPanel(this), "create"));
        bar.add(btnCreate);
        bar.add(Box.createHorizontalStrut(8));

        // Profile
        String displayName = MainFile.currentUser.getDisplayName();
        JButton btnProfile = new JButton(displayName);
        btnProfile.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnProfile.setForeground(AppConstants.TEXT_PRI);
        btnProfile.setBorderPainted(false);
        btnProfile.setContentAreaFilled(false);
        btnProfile.setFocusPainted(false);
        btnProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnProfile.setMaximumSize(btnProfile.getPreferredSize());
        btnProfile.addActionListener(e -> showMyProfile());
        bar.add(btnProfile);
        bar.add(Box.createHorizontalStrut(8));

        // Logout
        JButton btnLogout = new JButton("Log out");
        btnLogout.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnLogout.setForeground(AppConstants.DANGER);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setMaximumSize(btnLogout.getPreferredSize());
        btnLogout.addActionListener(e -> {
            dispose();
            MainFile.currentUser = null;
            MainFile.loginScreen.setVisible(true);
        });
        bar.add(btnLogout);

        return bar;
    }

    private static class BadgeIcon implements Icon {
        private String text;
        public BadgeIcon(String text) { this.text = text; }
        public int getIconWidth() { return 24; }
        public int getIconHeight() { return 24; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppConstants.DANGER);
            g2.fillOval(x, y + 2, 20, 20);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text);
            g2.drawString(text, x + 10 - w / 2, y + 15);
        }
    }

    private JPanel buildSideNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(16, 14, 16, 14)));
        nav.setPreferredSize(new Dimension(180, 0));

        String[] tabs = {"Feed", "Discover", "Calendar", "Messages", "Leaderboard", "Notifications"};
        String[] actions = {"feed", "discover", "calendar", "messages", "leaderboard", "notif"};
        for (int i = 0; i < tabs.length; i++) {
            JButton btn = createNavLink(tabs[i], i == 0);
            if (tabs[i].equals("Messages")) messagesNavBtn = btn;
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setAlignmentX(LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            final String action = actions[i];
            btn.addActionListener(e -> {
                switch (action) {
                    case "feed": showFeed(); break;
                    case "discover": showPanel(new DiscoverPanel(this), "discover"); break;
                    case "calendar": showPanel(new CalendarPanel(this), "calendar"); break;
                    case "messages":
                        messagingPanel.refreshConversations();
                        showPanel(messagingPanel, "messages");
                        break;
                    case "leaderboard": showPanel(new LeaderboardPanel(this), "leaderboard"); break;
                    case "notif": showPanel(new NotificationsPanel(this), "notif"); break;
                }
            });
            nav.add(btn);
        }

        return nav;
    }

    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppConstants.BG_MAIN);
        messagingPanel = new MessagingPanel(this);
        contentPanel.add(new FeedPanel(this), "feed");
        contentPanel.add(new SearchPanel(this), "search");
        contentPanel.add(new DiscoverPanel(this), "discover");
        contentPanel.add(new CalendarPanel(this), "calendar");
        contentPanel.add(new LeaderboardPanel(this), "leaderboard");
        contentPanel.add(new CreateEventPanel(this), "create");
        contentPanel.add(messagingPanel, "messages");
        contentPanel.add(new NotificationsPanel(this), "notif");
        return contentPanel;
    }

    private void showPanel(JPanel panel, String name) {
        currentView = name;
        contentPanel.add(panel, name);
        cardLayout.show(contentPanel, name);
    }

    public void showFeed() { showPanel(new FeedPanel(this), "feed"); }

    public void showMyProfile() {
        myProfilePanel = new ProfilePanel(MainFile.currentUser, this, false);
        showPanel(myProfilePanel, "myProfile");
    }

    public void navigateToProfile(User user) {
        currentProfileTarget = user;
        if (user.getUsername().equals(MainFile.currentUser.getUsername())) { showMyProfile(); return; }
        // If not already on top of stack, push it
        if (profileStack.isEmpty() || !profileStack.peek().getUsername().equals(user.getUsername())) {
            profileStack.push(user);
        }
        viewedProfilePanel = new ProfilePanel(user, this, true);
        showPanel(viewedProfilePanel, "viewProfile");
    }

    public void goBackFromProfile() {
        if (!profileStack.isEmpty()) profileStack.pop();
        if (!profileStack.isEmpty()) {
            User prev = profileStack.peek();
            currentProfileTarget = prev;
            viewedProfilePanel = new ProfilePanel(prev, this, true);
            showPanel(viewedProfilePanel, "viewProfile");
        } else { showFeed(); }
    }

    public void showEventDetail(Event event) {
        currentEventTarget = event;
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
        b.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        b.setForeground(active ? AppConstants.TEAL : AppConstants.TEXT_SEC);
        b.setBackground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(b.getPreferredSize().width, 38));
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
