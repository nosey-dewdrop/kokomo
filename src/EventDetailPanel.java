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
        setBorder(BorderFactory.createEmptyBorder(24, 36, 15, 36));
        add(createTopBar(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JButton btnBack = UIHelper.createOutlineButton("< Back", AppConstants.ACCENT);
        btnBack.addActionListener(e -> homeScreen.showFeed());
        top.add(btnBack, BorderLayout.WEST);

        JLabel title = new JLabel(event.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(AppConstants.TEXT_PRI);
        title.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        top.add(title, BorderLayout.CENTER);

        // DELETE BUTTON (only for event creator)
        if (event.getCreatorUsername().equals(MainFile.currentUser.getUsername())) {
            JButton btnDelete = UIHelper.createOutlineButton("Delete Event", AppConstants.DANGER);
            btnDelete.addActionListener(ev -> {
                if (UIHelper.showConfirm(this, "Are you sure you want to delete this event?")) {
                    Database.deleteFromDatabase(event);
                    Database.addXP(MainFile.currentUser.getUsername(), AppConstants.XP_CANCEL_EVENT);
                    UIHelper.showSuccess(this, "Event deleted.");
                    homeScreen.showFeed();
                }
            });
            top.add(btnDelete, BorderLayout.EAST);
        }

        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return top;
    }

    private JScrollPane createBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);

        body.add(createInfoCard());
        body.add(Box.createVerticalStrut(10));
        body.add(createAttendanceCard());
        body.add(Box.createVerticalStrut(10));
        body.add(createAttendeesCard());
        body.add(Box.createVerticalStrut(10));
        body.add(createCommentsCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createInfoCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        User creator = Database.getUserWithUsername(event.getCreatorUsername());
        JPanel creatorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        creatorRow.setOpaque(false);
        creatorRow.add(UIHelper.createSectionLabel("Created by: "));
        if (creator != null) creatorRow.add(UIHelper.createClickableUsername(creator, homeScreen));
        card.add(creatorRow);
        card.add(Box.createVerticalStrut(8));

        if (!event.getDescription().isEmpty())
            card.add(createInfoRow("Description", event.getDescription()));
        card.add(createInfoRow("Location", event.getLocation()));
        card.add(createInfoRow("Start", event.getDateStr()));
        if (event.getEndDateTime() != null)
            card.add(createInfoRow("End", event.getEndDateStr()));
        if (event.getRegistrationDeadline() != null) {
            JPanel deadlineRow = createInfoRow("Deadline", event.getDeadlineStr());
            if (event.isDeadlinePassed())
                deadlineRow.add(UIHelper.createBadgeLabel("EXPIRED", AppConstants.DANGER, Color.WHITE));
            card.add(deadlineRow);
        }
        card.add(createInfoRow("Capacity", event.getGoingCount() + " / " + event.getCapacity()));
        card.add(createInfoRow("XP Reward", "+" + event.getXpReward() + " XP"));
        if (event.getMinTierIndex() > 0)
            card.add(createInfoRow("Min. Tier", event.getMinTierName()));

        if (!event.getTags().isEmpty()) {
            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            tagRow.setOpaque(false);
            tagRow.add(UIHelper.createSectionLabel("Tags: "));
            for (String tag : event.getTags())
                tagRow.add(UIHelper.createBadgeLabel("#" + tag, new Color(230, 240, 255), AppConstants.ACCENT));
            card.add(tagRow);
        }

        return card;
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        row.setOpaque(false);
        row.add(UIHelper.createSectionLabel(label + ": "));
        JLabel valLbl = UIHelper.createLabel(value);
        valLbl.setForeground(AppConstants.TEXT_SEC);
        row.add(valLbl);
        return row;
    }

    private JPanel createAttendanceCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel title = new JLabel("Your Status");
        title.setFont(AppConstants.F_SECTION);
        title.setForeground(AppConstants.TEXT_PRI);
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        String me = MainFile.currentUser.getUsername();
        AttendanceStatus current = event.getAttendanceStatus(me);
        boolean deadlinePassed = event.isDeadlinePassed();

        JButton btnGoing = createStatusButton("Going", AppConstants.SUCCESS, current == AttendanceStatus.GOING);
        btnGoing.addActionListener(e -> {
            if (deadlinePassed) { UIHelper.showError(this, "Registration closed!"); return; }
            int myXP = Database.getUserXP(me);
            if (!event.canJoin(myXP)) { UIHelper.showError(this, "You need " + event.getMinTierName() + " tier!"); return; }
            homeScreen.changeAttendance(event, AttendanceStatus.GOING);
            homeScreen.showEventDetail(event);
        });

        JButton btnInterested = createStatusButton("Interested", AppConstants.INTERESTED, current == AttendanceStatus.INTERESTED);
        btnInterested.addActionListener(e -> {
            homeScreen.changeAttendance(event, AttendanceStatus.INTERESTED);
            homeScreen.showEventDetail(event);
        });

        JButton btnMaybe = createStatusButton("Maybe", AppConstants.MAYBE_COLOR, current == AttendanceStatus.MAYBE);
        btnMaybe.addActionListener(e -> {
            homeScreen.changeAttendance(event, AttendanceStatus.MAYBE);
            homeScreen.showEventDetail(event);
        });

        btnRow.add(btnGoing); btnRow.add(btnInterested); btnRow.add(btnMaybe);

        if (current != null) {
            JButton btnCancel = UIHelper.createOutlineButton("Cancel", AppConstants.DANGER);
            btnCancel.addActionListener(e -> {
                homeScreen.changeAttendance(event, null);
                homeScreen.showEventDetail(event);
            });
            btnRow.add(btnCancel);
        }

        card.add(btnRow);
        return card;
    }

    private JButton createStatusButton(String text, Color color, boolean selected) {
        return selected ? UIHelper.createButton(text + " \u2713", color, Color.WHITE)
                        : UIHelper.createOutlineButton(text, color);
    }

    private JPanel createAttendeesCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);
        header.add(UIHelper.createSectionLabel("Attendees"));
        header.add(UIHelper.createBadgeLabel(event.getGoingCount() + " Going", AppConstants.SUCCESS, Color.WHITE));
        if (event.getInterestedCount() > 0)
            header.add(UIHelper.createBadgeLabel(event.getInterestedCount() + " Interested", AppConstants.INTERESTED, Color.WHITE));
        if (event.getMaybeCount() > 0)
            header.add(UIHelper.createBadgeLabel(event.getMaybeCount() + " Maybe", AppConstants.MAYBE_COLOR, Color.WHITE));
        card.add(header);
        card.add(Box.createVerticalStrut(8));

        JPanel list = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        list.setOpaque(false);
        if (event.getAttendees().isEmpty()) {
            list.add(UIHelper.createSmallLabel("No attendees yet"));
        } else {
            for (String uname : event.getAttendees()) {
                User u = Database.getUserWithUsername(uname);
                if (u != null) list.add(UIHelper.createClickableUsername(u, homeScreen));
            }
        }
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(0, 80));
        card.add(scroll);
        return card;
    }

    private JPanel createCommentsCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));

        JLabel title = new JLabel("Comments (" + event.getComments().size() + ")");
        title.setFont(AppConstants.F_SECTION);
        title.setForeground(AppConstants.TEXT_PRI);
        card.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        ArrayList<Comment> topLevel = event.getComments().stream()
            .filter(c -> !c.isReply()).collect(Collectors.toCollection(ArrayList::new));

        if (topLevel.isEmpty()) {
            list.add(Box.createVerticalStrut(8));
            list.add(UIHelper.createSmallLabel("No comments yet. Be the first!"));
        } else {
            for (Comment c : topLevel) {
                list.add(Box.createVerticalStrut(6));
                list.add(createCommentRow(c, 0));
                ArrayList<Comment> replies = event.getComments().stream()
                    .filter(r -> r.getParentId() == c.getId())
                    .collect(Collectors.toCollection(ArrayList::new));
                for (Comment reply : replies) list.add(createCommentRow(reply, 1));
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(0, 200));
        card.add(scroll, BorderLayout.CENTER);
        card.add(createCommentInput(0), BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCommentRow(Comment comment, int indent) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, indent * 24, 2, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        topRow.setOpaque(false);
        User commenter = Database.getUserWithUsername(comment.getUsername());
        if (commenter != null) topRow.add(UIHelper.createClickableUsername(commenter, homeScreen));
        topRow.add(UIHelper.createSmallLabel(" \u2022 " + comment.getTime()));
        if (indent > 0) topRow.add(UIHelper.createBadgeLabel("reply", new Color(240,240,245), AppConstants.TEXT_SEC));
        content.add(topRow);

        JLabel textLbl = new JLabel("<html>" + comment.getText() + "</html>");
        textLbl.setFont(AppConstants.F_NORMAL);
        textLbl.setForeground(AppConstants.TEXT_PRI);
        content.add(textLbl);
        row.add(content, BorderLayout.CENTER);

        if (indent == 0) {
            JButton btnReply = UIHelper.createOutlineButton("Reply", AppConstants.TEXT_SEC);
            btnReply.setFont(AppConstants.F_TINY);
            btnReply.setPreferredSize(new Dimension(55, 22));
            btnReply.addActionListener(e -> {
                String reply = JOptionPane.showInputDialog(this,
                    "Reply to @" + comment.getUsername() + ":", "Reply", JOptionPane.PLAIN_MESSAGE);
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
        JPanel inputPanel = new JPanel(new BorderLayout(6, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextField commentField = UIHelper.createStyledField();
        commentField.setFont(AppConstants.F_NORMAL);
        JButton btnSend = UIHelper.createButton("Send", AppConstants.ACCENT, Color.WHITE);

        Runnable sendAction = () -> {
            String text = commentField.getText().trim();
            if (!text.isEmpty()) {
                homeScreen.addComment(event, text, parentId);
                homeScreen.showEventDetail(event);
            }
        };
        btnSend.addActionListener(e -> sendAction.run());
        commentField.addActionListener(e -> sendAction.run());

        inputPanel.add(commentField, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        return inputPanel;
    }
}
