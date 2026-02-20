import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private ProfilePanel myProfilePanel;
    private ProfilePanel viewedProfilePanel;
    private java.util.Stack<User> profileStack = new java.util.Stack<>();

    private JButton btnFeed, btnSearch, btnDiscover, btnCalendar, btnLeaderboard;
    private JButton btnCreate, btnMessages, btnNotif, btnProfile, btnLogout;

    public HomeScreen() {
        setTitle("League of Bilkent - " + MainFile.currentUser.getDisplayName());
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(AppConstants.NAV_WIDTH, 0));
        nav.setBackground(AppConstants.BG_NAV);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppConstants.BORDER));

        // Logo
        JLabel logo = new JLabel("League of Bilkent");
        logo.setFont(new Font("SansSerif", Font.BOLD, 14));
        logo.setForeground(AppConstants.TEXT_PRI);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(16, 0, 2, 0));
        nav.add(logo);

        String badge = MainFile.currentUser.getProfileBadge();
        JLabel userLbl = new JLabel(badge);
        userLbl.setFont(AppConstants.F_TINY);
        userLbl.setForeground(AppConstants.TEXT_SEC);
        userLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(userLbl);
        nav.add(Box.createVerticalStrut(12));

        JSeparator sep = new JSeparator();
        sep.setForeground(AppConstants.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(8));

        btnFeed        = createNavButton("\uD83D\uDCCB  Feed");
        btnSearch      = createNavButton("\uD83D\uDD0D  Search");
        btnDiscover    = createNavButton("\u2728  Discover");
        btnCalendar    = createNavButton("\uD83D\uDCC5  Calendar");
        btnLeaderboard = createNavButton("\uD83C\uDFC6  Leaderboard");
        btnCreate      = createNavButton("\u2795  New Event");
        btnMessages    = createNavButton("\uD83D\uDCAC  Messages");
        btnNotif       = createNavButton("\uD83D\uDD14  Notifications");
        btnProfile     = createNavButton("\uD83D\uDC64  Profile");
        btnLogout      = createNavButton("\uD83D\uDEAA  Log out");
        btnLogout.setForeground(AppConstants.DANGER);

        for (JButton b : new JButton[]{btnFeed, btnSearch, btnDiscover, btnCalendar,
                btnLeaderboard, btnCreate, btnMessages, btnNotif, btnProfile}) {
            nav.add(b);
        }
        nav.add(Box.createVerticalGlue());
        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(12));
        add(nav, BorderLayout.WEST);

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(new FeedPanel(this), "feed");
        contentPanel.add(new SearchPanel(this), "search");
        contentPanel.add(new DiscoverPanel(this), "discover");
        contentPanel.add(new CalendarPanel(this), "calendar");
        contentPanel.add(new LeaderboardPanel(this), "leaderboard");
        contentPanel.add(new CreateEventPanel(this), "create");
        contentPanel.add(new MessagingPanel(this), "messages");
        contentPanel.add(new NotificationsPanel(this), "notif");
        add(contentPanel, BorderLayout.CENTER);

        // Actions
        btnFeed.addActionListener(e -> showFeed());
        btnSearch.addActionListener(e -> cardLayout.show(contentPanel, "search"));
        btnDiscover.addActionListener(e -> { contentPanel.add(new DiscoverPanel(this), "discover"); cardLayout.show(contentPanel, "discover"); });
        btnCalendar.addActionListener(e -> { contentPanel.add(new CalendarPanel(this), "calendar"); cardLayout.show(contentPanel, "calendar"); });
        btnLeaderboard.addActionListener(e -> { contentPanel.add(new LeaderboardPanel(this), "leaderboard"); cardLayout.show(contentPanel, "leaderboard"); });
        btnCreate.addActionListener(e -> { contentPanel.add(new CreateEventPanel(this), "create"); cardLayout.show(contentPanel, "create"); });
        btnMessages.addActionListener(e -> { contentPanel.add(new MessagingPanel(this), "messages"); cardLayout.show(contentPanel, "messages"); });
        btnNotif.addActionListener(e -> { contentPanel.add(new NotificationsPanel(this), "notif"); cardLayout.show(contentPanel, "notif"); });
        btnProfile.addActionListener(e -> showMyProfile());
        btnLogout.addActionListener(e -> {
            dispose();
            MainFile.currentUser = null;
            MainFile.loginScreen.setVisible(true);
        });

        cardLayout.show(contentPanel, "feed");
    }

    public void showFeed() {
        contentPanel.add(new FeedPanel(this), "feed");
        cardLayout.show(contentPanel, "feed");
    }

    public void showMyProfile() {
        myProfilePanel = new ProfilePanel(MainFile.currentUser, this, false);
        contentPanel.add(myProfilePanel, "myProfile");
        cardLayout.show(contentPanel, "myProfile");
    }

    public void navigateToProfile(User user) {
        if (user.getUsername().equals(MainFile.currentUser.getUsername())) { showMyProfile(); return; }
        profileStack.push(user);
        viewedProfilePanel = new ProfilePanel(user, this, true);
        contentPanel.add(viewedProfilePanel, "viewProfile");
        cardLayout.show(contentPanel, "viewProfile");
    }

    public void goBackFromProfile() {
        if (!profileStack.isEmpty()) profileStack.pop();
        if (!profileStack.isEmpty()) {
            User prev = profileStack.peek();
            viewedProfilePanel = new ProfilePanel(prev, this, true);
            contentPanel.add(viewedProfilePanel, "viewProfile");
            cardLayout.show(contentPanel, "viewProfile");
        } else {
            showFeed();
        }
    }

    public void showEventDetail(Event event) {
        EventDetailPanel detail = new EventDetailPanel(event, this);
        contentPanel.add(detail, "detail");
        cardLayout.show(contentPanel, "detail");
    }

    /**
     * Change attendance status (or remove if status is null).
     */
    public void changeAttendance(Event event, AttendanceStatus status) {
        String me = MainFile.currentUser.getUsername();
        if (status == null) {
            event.removeAttendance(me);
            Database.removeAttendance(event.getId(), me);
        } else {
            if (status == AttendanceStatus.GOING && event.isFull()) {
                UIHelper.showError(this, "Event is full!");
                return;
            }
            event.setAttendance(me, status);
            Database.setAttendance(event.getId(), me, status);
            // Award XP
            Database.addXP(me, AppConstants.XP_ATTEND_EVENT);
            // Award event-specific XP
            if (event.getXpReward() > 0) {
                Database.addXP(me, event.getXpReward());
            }
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

        // Notify event creator
        if (!me.equals(event.getCreatorUsername())) {
            Database.addNotification(event.getCreatorUsername(),
                MainFile.currentUser.getDisplayName() + " commented on: " + event.getTitle());
        }
        // Notify parent comment author
        if (parentId > 0) {
            for (Comment c : event.getComments()) {
                if (c.getId() == parentId && !c.getUsername().equals(me)) {
                    Database.addNotification(c.getUsername(),
                        MainFile.currentUser.getDisplayName() + " replied to your comment on: " + event.getTitle());
                    break;
                }
            }
        }
    }

    public void followUser(String targetUsername) {
        String me = MainFile.currentUser.getUsername();
        Database.addFollow(me, targetUsername);
        MainFile.currentUser.getFollowing().add(targetUsername);
        Database.addXP(targetUsername, AppConstants.XP_GAIN_FOLLOWER);
        Database.addNotification(targetUsername, MainFile.currentUser.getDisplayName() + " started following you!");
    }

    public void unfollowUser(String targetUsername) {
        String me = MainFile.currentUser.getUsername();
        Database.deleteFollow(me, targetUsername);
        MainFile.currentUser.getFollowing().remove(targetUsername);
    }

    public void createEvent(Event event) {
        int id = Database.addToDatabase(event);
        event.setId(id);
        Database.addXP(MainFile.currentUser.getUsername(), AppConstants.XP_CREATE_EVENT);
        for (String follower : MainFile.currentUser.getFollowers()) {
            Database.addNotification(follower,
                MainFile.currentUser.getDisplayName() + " created a new event: " + event.getTitle());
        }
    }

    private JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.setFont(AppConstants.F_NORMAL);
        b.setForeground(AppConstants.TEXT_PRI);
        b.setBackground(AppConstants.BG_NAV);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(200, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(AppConstants.PRIMARY_LIGHT); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(AppConstants.BG_NAV); }
        });
        return b;
    }
}
