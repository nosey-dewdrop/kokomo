package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ProfilePanel extends JPanel {

    private User user;
    private HomeScreen home;
    private boolean isOtherUser;

    public ProfilePanel(User user, HomeScreen home, boolean isOtherUser) {
        this.user = user;
        this.home = home;
        this.isOtherUser = isOtherUser;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 48, 20, 48));
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        if (isOtherUser) {
            JButton back = UIHelper.createOutlineButton("< Back", AppConstants.ACCENT);
            back.setAlignmentX(LEFT_ALIGNMENT);
            back.addActionListener(e -> home.goBackFromProfile());
            content.add(back);
            content.add(Box.createVerticalStrut(12));
        }

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0xFB, 0xFB, 0xFA));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        header.setAlignmentX(LEFT_ALIGNMENT);

        // Left: avatar + name
        JPanel nameCol = new JPanel();
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        nameCol.setOpaque(false);

        JLabel nameLbl = new JLabel(user.getDisplayName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLbl.setForeground(AppConstants.TEXT_PRI);
        nameCol.add(nameLbl);

        JLabel userLbl = new JLabel("@" + user.getUsername() + (user.isVerified() ? " \u2713" : "")
            + (user.isClub() ? " [CLUB]" : ""));
        userLbl.setFont(AppConstants.F_SMALL);
        userLbl.setForeground(AppConstants.TEXT_SEC);
        nameCol.add(userLbl);
        nameCol.add(Box.createVerticalStrut(6));

        // XP + Tier
        int xp = Database.getUserXP(user.getUsername());
        String tierName = AppConstants.getTierName(xp);
        Color tierColor = AppConstants.getTierColor(xp);
        JLabel tierLbl = new JLabel(tierName + "  |  " + xp + " XP");
        tierLbl.setFont(AppConstants.F_SECTION);
        tierLbl.setForeground(tierColor);
        nameCol.add(tierLbl);

        // Progress bar
        int nextXP = AppConstants.getNextTierXP(xp);
        if (nextXP > 0) {
            int currThreshold = AppConstants.TIER_THRESHOLDS[AppConstants.getTierIndex(xp)];
            double pct = (double)(xp - currThreshold) / (nextXP - currThreshold);
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue((int)(pct * 100));
            bar.setStringPainted(true);
            bar.setString(xp + " / " + nextXP + " XP to " + AppConstants.getNextTierName(xp));
            bar.setFont(AppConstants.F_TINY);
            bar.setMaximumSize(new Dimension(250, 16));
            nameCol.add(Box.createVerticalStrut(4));
            nameCol.add(bar);
        }

        header.add(nameCol, BorderLayout.CENTER);

        // Right: stats + follow
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        ArrayList<String> followers = Database.getFollowers(user.getUsername());
        ArrayList<String> following = Database.getFollowing(user.getUsername());
        JLabel statsLbl = new JLabel("<html><u>" + followers.size() + " Followers</u>  |  <u>" + following.size() + " Following</u></html>");
        statsLbl.setFont(AppConstants.F_SMALL);
        statsLbl.setForeground(AppConstants.ACCENT);
        statsLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statsLbl.setAlignmentX(RIGHT_ALIGNMENT);
        statsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { showFollowDialog(followers, following); }
        });
        rightCol.add(statsLbl);

        if (isOtherUser) {
            rightCol.add(Box.createVerticalStrut(8));
            boolean isFollowing = MainFile.currentUser.getFollowing().contains(user.getUsername());
            JButton followBtn;
            if (isFollowing) {
                followBtn = UIHelper.createOutlineButton("Unfollow", AppConstants.DANGER);
                followBtn.addActionListener(e -> {
                    home.unfollowUser(user.getUsername());
                    home.navigateToProfile(user);
                });
            } else {
                followBtn = UIHelper.createButton("Follow", AppConstants.ACCENT, Color.WHITE);
                followBtn.addActionListener(e -> {
                    home.followUser(user.getUsername());
                    home.navigateToProfile(user);
                });
            }
            followBtn.setAlignmentX(RIGHT_ALIGNMENT);
            rightCol.add(followBtn);

            // Message button
            rightCol.add(Box.createVerticalStrut(4));
            JButton msgBtn = UIHelper.createOutlineButton("Message", AppConstants.TEXT_SEC);
            msgBtn.setAlignmentX(RIGHT_ALIGNMENT);
            msgBtn.addActionListener(e -> {
                String text = JOptionPane.showInputDialog(this,
                    "Send message to @" + user.getUsername() + ":", "Message", JOptionPane.PLAIN_MESSAGE);
                if (text != null && !text.trim().isEmpty()) {
                    Database.sendMessage(MainFile.currentUser.getUsername(), user.getUsername(), text.trim());
                    UIHelper.showSuccess(this, "Message sent!");
                }
            });
            rightCol.add(msgBtn);
        }

        header.add(rightCol, BorderLayout.EAST);
        content.add(header);
        content.add(Box.createVerticalStrut(12));

        // Bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            JLabel bioLbl = new JLabel("<html>" + user.getBio() + "</html>");
            bioLbl.setFont(AppConstants.F_NORMAL);
            bioLbl.setForeground(AppConstants.TEXT_SEC);
            bioLbl.setAlignmentX(LEFT_ALIGNMENT);
            content.add(bioLbl);
            content.add(Box.createVerticalStrut(8));
        }

        // Edit bio (own profile)
        if (!isOtherUser) {
            JButton editBio = UIHelper.createOutlineButton("Edit Bio", AppConstants.TEXT_SEC);
            editBio.setAlignmentX(LEFT_ALIGNMENT);
            editBio.addActionListener(e -> {
                String newBio = JOptionPane.showInputDialog(this, "Enter new bio:", user.getBio());
                if (newBio != null) {
                    Database.updateUserBio(user.getUsername(), newBio);
                    user.setBio(newBio);
                    home.showMyProfile();
                }
            });
            content.add(editBio);

            // Edit Interests button
            JButton editInterests = UIHelper.createOutlineButton("Edit Interests", AppConstants.ACCENT);
            editInterests.setAlignmentX(LEFT_ALIGNMENT);
            editInterests.addActionListener(e -> {
                ArrayList<String> current = Database.getInterests(user.getUsername());
                InterestSelectionDialog dialog = new InterestSelectionDialog(
                    SwingUtilities.getWindowAncestor(this), current);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Database.setInterests(user.getUsername(), dialog.getSelectedInterests());
                    home.showMyProfile();
                }
            });
            content.add(editInterests);
            content.add(Box.createVerticalStrut(12));
        }

        // Interests
        ArrayList<String> interests = Database.getInterests(user.getUsername());
        if (!interests.isEmpty()) {
            content.add(UIHelper.createSectionLabel("Interests"));
            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(LEFT_ALIGNMENT);
            for (String i : interests)
                tagRow.add(UIHelper.createBadgeLabel(i, new Color(230,240,255), AppConstants.ACCENT));
            content.add(tagRow);
            content.add(Box.createVerticalStrut(12));
        }

        // Followers list
        content.add(UIHelper.createSectionLabel("Followers"));
        content.add(Box.createVerticalStrut(4));
        if (followers.isEmpty()) {
            content.add(UIHelper.createSmallLabel("No followers yet."));
        } else {
            JPanel fRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            fRow.setOpaque(false); fRow.setAlignmentX(LEFT_ALIGNMENT);
            for (String f : followers) {
                User u = Database.getUserWithUsername(f);
                if (u != null) fRow.add(UIHelper.createClickableUsername(u, home));
            }
            content.add(fRow);
        }
        content.add(Box.createVerticalStrut(12));

        // Following list
        content.add(UIHelper.createSectionLabel("Following"));
        content.add(Box.createVerticalStrut(4));
        if (following.isEmpty()) {
            content.add(UIHelper.createSmallLabel("Not following anyone yet."));
        } else {
            JPanel fRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            fRow.setOpaque(false); fRow.setAlignmentX(LEFT_ALIGNMENT);
            for (String f : following) {
                User u = Database.getUserWithUsername(f);
                if (u != null) fRow.add(UIHelper.createClickableUsername(u, home));
            }
            content.add(fRow);
        }
        content.add(Box.createVerticalStrut(12));

        // Events created by this user
        content.add(UIHelper.createSectionLabel("Events"));
        content.add(Box.createVerticalStrut(4));
        ArrayList<Event> allEvents = Database.getAllEvents();
        boolean hasEvents = false;
        for (Event ev : allEvents) {
            if (ev.getCreatorUsername().equals(user.getUsername())) {
                hasEvents = true;
                JButton evBtn = new JButton(ev.getTitle() + "  |  " + ev.getDateStr());
                evBtn.setFont(AppConstants.F_SMALL);
                evBtn.setHorizontalAlignment(SwingConstants.LEFT);
                evBtn.setBorderPainted(false);
                evBtn.setBackground(Color.WHITE);
                evBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                evBtn.setAlignmentX(LEFT_ALIGNMENT);
                evBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                evBtn.addActionListener(e -> home.showEventDetail(ev));
                content.add(evBtn);
            }
        }
        if (!hasEvents) content.add(UIHelper.createSmallLabel("No events created yet."));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void showFollowDialog(ArrayList<String> followers, ArrayList<String> following) {
        JDialog dlg = new JDialog(
            SwingUtilities.getWindowAncestor(this), "Connections",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(380, 480);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Tab buttons
        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabRow.setBackground(Color.WHITE);
        JButton tabFollowers = UIHelper.createButton(
            "Followers (" + followers.size() + ")", AppConstants.ACCENT, Color.WHITE);
        JButton tabFollowing = UIHelper.createOutlineButton(
            "Following (" + following.size() + ")", AppConstants.TEXT_SEC);
        tabRow.add(tabFollowers);
        tabRow.add(tabFollowing);

        // Search field
        JTextField search = UIHelper.createStyledField();
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        // Top panel
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        tabRow.setAlignmentX(LEFT_ALIGNMENT);
        search.setAlignmentX(LEFT_ALIGNMENT);
        top.add(tabRow);
        top.add(Box.createVerticalStrut(8));
        top.add(search);
        root.add(top, BorderLayout.NORTH);

        // User list
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Color.WHITE);
        JScrollPane sc = new JScrollPane(list);
        sc.setBorder(null);
        root.add(sc, BorderLayout.CENTER);

        final boolean[] showFollowers = {true};

        Runnable refresh = () -> {
            list.removeAll();
            String query = search.getText().trim().toLowerCase();
            ArrayList<String> source = showFollowers[0] ? followers : following;

            for (String username : source) {
                if (!query.isEmpty() && !username.toLowerCase().contains(query)) continue;
                User u = Database.getUserWithUsername(username);
                if (u == null) continue;
                int xp = Database.getUserXP(username);

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                row.add(UIHelper.createAvatar(u.getDisplayName(),
                    AppConstants.getTierColor(xp), 28), BorderLayout.WEST);

                JPanel nameCol = new JPanel();
                nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
                nameCol.setOpaque(false);
                nameCol.add(new JLabel(u.getDisplayName() + (u.isVerified() ? " \u2713" : "")));
                JLabel sub = new JLabel("@" + username + "  " + AppConstants.getTierName(xp));
                sub.setFont(AppConstants.F_TINY);
                sub.setForeground(AppConstants.getTierColor(xp));
                nameCol.add(sub);
                row.add(nameCol, BorderLayout.CENTER);

                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                row.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        row.setBackground(AppConstants.PRIMARY_LIGHT);
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        row.setBackground(Color.WHITE);
                    }
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        dlg.dispose();
                        home.navigateToProfile(u);
                    }
                });
                list.add(row);
            }
            list.revalidate();
            list.repaint();
        };

        tabFollowers.addActionListener(e -> { showFollowers[0] = true; refresh.run(); });
        tabFollowing.addActionListener(e -> { showFollowers[0] = false; refresh.run(); });

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh.run(); }
        });

        refresh.run();
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }
}
