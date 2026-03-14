package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class EventDetailPanel extends JPanel {

    private Event event;
    private HomeScreen homeScreen;

    public EventDetailPanel(Event event, HomeScreen homeScreen) {
        this.event = event;
        this.homeScreen = homeScreen;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(createBody(), BorderLayout.CENTER);
    }

    private JScrollPane createBody() {
        JPanel body = UIHelper.createPagePanel();

        JButton btnBack = new JButton(AppConstants.BTN_BACK);
        btnBack.setFont(AppConstants.F_SMALL);
        btnBack.setForeground(AppConstants.TEXT_SEC);
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setAlignmentX(LEFT_ALIGNMENT);
        btnBack.addActionListener(e -> homeScreen.showFeed());
        body.add(btnBack);
        body.add(Box.createVerticalStrut(20));

        JLabel titleLbl = new JLabel(event.getTitle());
        titleLbl.setFont(AppConstants.F_HERO);
        titleLbl.setForeground(AppConstants.TEXT_PRI);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);
        body.add(titleLbl);
        body.add(Box.createVerticalStrut(16));

        body.add(createProp("Created by", event.getCreatorUsername()));
        if (!event.getDescription().isEmpty())
            body.add(createProp("Description", event.getDescription()));
        body.add(createProp("Location", event.getLocation()));
        body.add(createProp("Start", event.getDateStr()));
        if (event.getEndDateTime() != null)
            body.add(createProp("End", event.getEndDateStr()));
        if (event.getRegistrationDeadline() != null) {
            String dl = event.getDeadlineStr() + (event.isDeadlinePassed() ? "  (EXPIRED)" : "");
            body.add(createProp("Deadline", dl));
        }
        body.add(createProp("Capacity", event.getGoingCount() + " / " + event.getCapacity()));
        body.add(createProp("XP Reward", "+" + event.getXpReward() + " XP"));
        if (event.getMinTierIndex() > 0)
            body.add(createProp("Min. Tier", event.getMinTierName()));

        if (!event.getTags().isEmpty()) {
            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(LEFT_ALIGNMENT);
            tagRow.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            for (String tag : event.getTags()) {
                JLabel tl = new JLabel("#" + tag);
                tl.setFont(AppConstants.F_SMALL);
                tl.setForeground(AppConstants.ACCENT);
                tl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x23, 0x83, 0xE2, 40), 1, true),
                    BorderFactory.createEmptyBorder(3, 10, 3, 10)));
                tagRow.add(tl);
            }
            body.add(tagRow);
        }
        body.add(Box.createVerticalStrut(10));

        if (event.getCreatorUsername().equals(MainFile.currentUser.getUsername())) {
            JButton btnDel = UIHelper.createOutlineButton(AppConstants.BTN_DELETE, AppConstants.DANGER);
            btnDel.setAlignmentX(LEFT_ALIGNMENT);
            btnDel.addActionListener(ev -> {
                if (UIHelper.showConfirm(this, AppConstants.CONFIRM_DELETE)) {
                    Database.deleteFromDatabase(event);
                    Database.addXP(MainFile.currentUser.getUsername(), AppConstants.XP_CANCEL_EVENT);
                    UIHelper.showSuccess(this, AppConstants.SUC_EVENT_DELETED);
                    homeScreen.showFeed();
                }
            });
            body.add(btnDel);
            body.add(Box.createVerticalStrut(10));
        }

        body.add(UIHelper.createSeparator());
        body.add(Box.createVerticalStrut(20));
        body.add(buildAttendanceSection());
        body.add(Box.createVerticalStrut(20));
        body.add(buildAttendeesSection());
        body.add(Box.createVerticalStrut(24));
        body.add(UIHelper.createSeparator());
        body.add(Box.createVerticalStrut(20));
        body.add(buildCommentsSection());

        return UIHelper.wrapInScroll(body);
    }

    private JPanel createProp(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel keyLbl = new JLabel(label);
        keyLbl.setFont(AppConstants.F_SMALL);
        keyLbl.setForeground(AppConstants.TEXT_SEC);
        keyLbl.setPreferredSize(new Dimension(120, 22));
        row.add(keyLbl, BorderLayout.WEST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(AppConstants.F_NORMAL);
        valLbl.setForeground(AppConstants.TEXT_PRI);
        row.add(valLbl, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildAttendanceSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        section.add(UIHelper.createSectionLabel(AppConstants.SEC_YOUR_STATUS));
        section.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        String me = MainFile.currentUser.getUsername();
        AttendanceStatus current = event.getAttendanceStatus(me);
        boolean deadlinePassed = event.isDeadlinePassed();

        JButton btnGo = createStatusBtn(AppConstants.BTN_GOING, AppConstants.COLOR_GOING, current == AttendanceStatus.GOING);
        btnGo.addActionListener(e -> {
            if (deadlinePassed) { UIHelper.showError(this, AppConstants.ERR_DEADLINE_PASSED); return; }
            if (!event.canJoin(Database.getUserXP(me))) { UIHelper.showError(this, "You need " + event.getMinTierName() + " tier!"); return; }
            homeScreen.changeAttendance(event, AttendanceStatus.GOING);
            homeScreen.showEventDetail(event);
        });

        JButton btnInt = createStatusBtn(AppConstants.BTN_INTERESTED, AppConstants.COLOR_INTERESTED, current == AttendanceStatus.INTERESTED);
        btnInt.addActionListener(e -> { homeScreen.changeAttendance(event, AttendanceStatus.INTERESTED); homeScreen.showEventDetail(event); });

        JButton btnMay = createStatusBtn(AppConstants.BTN_MAYBE, AppConstants.COLOR_MAYBE, current == AttendanceStatus.MAYBE);
        btnMay.addActionListener(e -> { homeScreen.changeAttendance(event, AttendanceStatus.MAYBE); homeScreen.showEventDetail(event); });

        btnRow.add(btnGo);
        btnRow.add(btnInt);
        btnRow.add(btnMay);

        if (current != null) {
            JButton btnCancel = UIHelper.createOutlineButton(AppConstants.BTN_CANCEL, AppConstants.DANGER);
            btnCancel.addActionListener(e -> { homeScreen.changeAttendance(event, null); homeScreen.showEventDetail(event); });
            btnRow.add(btnCancel);
        }
        section.add(btnRow);
        return section;
    }

    private JButton createStatusBtn(String text, Color color, boolean selected) {
        return selected ? UIHelper.createButton(text + " \u2713", color, Color.WHITE)
                        : UIHelper.createOutlineButton(text, color);
    }

    private JPanel buildAttendeesSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(LEFT_ALIGNMENT);
        headerRow.add(UIHelper.createSectionLabel(AppConstants.SEC_ATTENDEES));
        headerRow.add(UIHelper.createBadgeLabel(event.getGoingCount() + " " + AppConstants.BTN_GOING, AppConstants.COLOR_GOING, Color.WHITE));
        if (event.getInterestedCount() > 0)
            headerRow.add(UIHelper.createBadgeLabel(event.getInterestedCount() + " " + AppConstants.BTN_INTERESTED, AppConstants.COLOR_INTERESTED, Color.WHITE));
        section.add(headerRow);
        section.add(Box.createVerticalStrut(10));

        JPanel list = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        list.setOpaque(false);
        list.setAlignmentX(LEFT_ALIGNMENT);
        if (event.getAttendees().isEmpty()) {
            list.add(UIHelper.createSmallLabel(AppConstants.EMPTY_ATTENDEES));
        } else {
            for (String uname : event.getAttendees()) {
                User u = Database.getUserWithUsername(uname);
                if (u != null) list.add(UIHelper.createClickableUsername(u, homeScreen));
            }
        }
        section.add(list);
        return section;
    }

    private JPanel buildCommentsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        section.add(UIHelper.createSectionLabel(AppConstants.SEC_COMMENTS + " (" + event.getComments().size() + ")"));
        section.add(Box.createVerticalStrut(12));
        section.add(createCommentInput(0));
        section.add(Box.createVerticalStrut(14));

        ArrayList<Comment> topLevel = event.getComments().stream()
            .filter(c -> !c.isReply()).collect(Collectors.toCollection(ArrayList::new));

        if (topLevel.isEmpty()) {
            section.add(UIHelper.createSmallLabel(AppConstants.EMPTY_COMMENTS));
        } else {
            for (Comment c : topLevel) {
                section.add(createCommentRow(c, 0));
                for (Comment reply : event.getComments()) {
                    if (reply.getParentId() == c.getId()) section.add(createCommentRow(reply, 1));
                }
            }
        }
        return section;
    }

    private JPanel createCommentRow(Comment comment, int indent) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(indent > 0 ? AppConstants.PRIMARY_LIGHT : Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstants.BORDER),
            BorderFactory.createEmptyBorder(14, 14 + indent * 32, 14, 14)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        row.setAlignmentX(LEFT_ALIGNMENT);

        User commenter = Database.getUserWithUsername(comment.getUsername());
        if (commenter != null) {
            int xp = Database.getUserXP(comment.getUsername());
            row.add(UIHelper.createAvatar(commenter.getDisplayName(), AppConstants.getTierColor(xp), 34), BorderLayout.WEST);
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        if (commenter != null) {
            JLabel nameLbl = new JLabel(commenter.getDisplayName());
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            nameLbl.setForeground(AppConstants.TEXT_PRI);
            nameLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            nameLbl.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) { homeScreen.navigateToProfile(commenter); }
            });
            topRow.add(nameLbl);
        }
        JLabel timeLbl = new JLabel(comment.getTime());
        timeLbl.setFont(AppConstants.F_TINY);
        timeLbl.setForeground(AppConstants.TEXT_LIGHT);
        topRow.add(timeLbl);
        content.add(topRow);

        JLabel textLbl = new JLabel("<html>" + comment.getText() + "</html>");
        textLbl.setFont(AppConstants.F_NORMAL);
        textLbl.setForeground(AppConstants.TEXT_PRI);
        textLbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        content.add(textLbl);
        row.add(content, BorderLayout.CENTER);

        if (indent == 0) {
            JButton btnReply = UIHelper.createOutlineButton(AppConstants.BTN_REPLY, AppConstants.TEXT_SEC);
            btnReply.addActionListener(e -> {
                String reply = JOptionPane.showInputDialog(this,
                    "Reply to @" + comment.getUsername() + ":", AppConstants.DLG_REPLY, JOptionPane.PLAIN_MESSAGE);
                if (reply != null && !reply.trim().isEmpty()) {
                    homeScreen.addComment(event, reply.trim(), comment.getId());
                    homeScreen.showEventDetail(event);
                }
            });
            row.add(btnReply, BorderLayout.EAST);
        }
        return row;
    }

    private JPanel createCommentInput(int parentId) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JTextField field = UIHelper.createStyledField();
        JButton btnSend = UIHelper.createButton(AppConstants.BTN_SEND, AppConstants.PRIMARY, Color.WHITE);

        Runnable send = () -> {
            String text = field.getText().trim();
            if (!text.isEmpty()) {
                homeScreen.addComment(event, text, parentId);
                homeScreen.showEventDetail(event);
            }
        };
        btnSend.addActionListener(e -> send.run());
        field.addActionListener(e -> send.run());

        panel.add(field, BorderLayout.CENTER);
        panel.add(btnSend, BorderLayout.EAST);
        return panel;
    }
}
