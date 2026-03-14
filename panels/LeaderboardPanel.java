package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LeaderboardPanel extends JPanel {

    private HomeScreen home;

    public LeaderboardPanel(HomeScreen home) {
        this.home = home;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        buildUI();
    }

    private void buildUI() {
        JPanel content = UIHelper.createPagePanel();

        content.add(UIHelper.createPageTitle(AppConstants.PAGE_LEADERBOARD));
        content.add(Box.createVerticalStrut(4));
        content.add(UIHelper.createSubtitle(AppConstants.PAGE_LEADER_SUB));
        content.add(Box.createVerticalStrut(20));

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setBackground(Color.WHITE);
        legend.setAlignmentX(LEFT_ALIGNMENT);
        for (int i = 0; i < AppConstants.TIER_NAMES.length; i++) {
            JLabel tl = new JLabel(AppConstants.TIER_NAMES[i] + " (" + AppConstants.TIER_THRESHOLDS[i] + "+)");
            tl.setFont(AppConstants.F_TINY);
            tl.setForeground(AppConstants.TIER_COLORS[i]);
            tl.setOpaque(true);
            tl.setBackground(new Color(AppConstants.TIER_COLORS[i].getRed(), AppConstants.TIER_COLORS[i].getGreen(),
                AppConstants.TIER_COLORS[i].getBlue(), 20));
            tl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            legend.add(tl);
        }
        content.add(legend);
        content.add(Box.createVerticalStrut(14));
        content.add(UIHelper.createSeparator());
        content.add(Box.createVerticalStrut(14));

        ArrayList<User> leaders = Database.getLeaderboard(50);
        int rank = 1;
        for (User u : leaders) {
            content.add(createRow(rank++, u));
            content.add(Box.createVerticalStrut(4));
        }

        add(UIHelper.wrapInScroll(content), BorderLayout.CENTER);
    }

    private JPanel createRow(int rank, User u) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(rank <= 3 ? new Color(0xFB, 0xFB, 0xFA) : Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(LEFT_ALIGNMENT);

        String rankStr = rank <= 3 ? new String[]{"\uD83E\uDD47","\uD83E\uDD48","\uD83E\uDD49"}[rank-1] : "#" + rank;
        JLabel rankLbl = new JLabel(rankStr);
        rankLbl.setFont(AppConstants.F_TITLE);
        rankLbl.setPreferredSize(new Dimension(44, 34));
        row.add(rankLbl, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel name = new JLabel(u.getDisplayName() + "  @" + u.getUsername());
        name.setFont(AppConstants.F_NORMAL);
        name.setForeground(AppConstants.TEXT_PRI);
        info.add(name);

        int xp = u.getXp();
        JLabel tierLbl = new JLabel(AppConstants.getTierName(xp) + "  |  " + xp + " XP");
        tierLbl.setFont(AppConstants.F_SMALL);
        tierLbl.setForeground(AppConstants.getTierColor(xp));
        info.add(tierLbl);
        row.add(info, BorderLayout.CENTER);

        JPanel xpBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int nextXP = AppConstants.getNextTierXP(xp);
                if (nextXP > 0) {
                    int currThreshold = AppConstants.TIER_THRESHOLDS[AppConstants.getTierIndex(xp)];
                    double pct = (double)(xp - currThreshold) / (nextXP - currThreshold);
                    g.setColor(AppConstants.BORDER);
                    g.fillRect(0, 12, getWidth(), 6);
                    g.setColor(AppConstants.getTierColor(xp));
                    g.fillRect(0, 12, (int)(getWidth() * pct), 6);
                }
            }
        };
        xpBar.setOpaque(false);
        xpBar.setPreferredSize(new Dimension(100, 30));
        row.add(xpBar, BorderLayout.EAST);

        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { home.navigateToProfile(u); }
        });

        return row;
    }
}
