package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MessagingPanel extends JPanel {

    private HomeScreen home;
    private JPanel chatPanel;
    private JTextField msgField;
    private String selectedUser = null;
    private JPanel convList;

    public MessagingPanel(HomeScreen home) {
        this.home = home;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        buildUI();
    }

    private void buildUI() {
        // Left: conversation list
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(new Color(0xFB, 0xFB, 0xFA));
        left.setPreferredSize(new Dimension(200, 0));
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppConstants.BORDER));

        JLabel title = new JLabel("  Messages");
        title.setFont(AppConstants.F_TITLE);
        title.setBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8));
        left.add(title);

        // New conversation button
        JButton btnNew = new JButton("+ New Message");
        btnNew.setFont(AppConstants.F_SMALL);
        btnNew.setBorderPainted(false);
        btnNew.addActionListener(e -> newConversation());
        btnNew.setAlignmentX(LEFT_ALIGNMENT);
        btnNew.setMaximumSize(new Dimension(190, 28));
        left.add(btnNew);
        left.add(Box.createVerticalStrut(8));

        convList = new JPanel();
        convList.setLayout(new BoxLayout(convList, BoxLayout.Y_AXIS));
        convList.setBackground(new Color(0xFB, 0xFB, 0xFA));
        refreshConversations();

        JScrollPane convScroll = new JScrollPane(convList);
        convScroll.setBorder(null);
        left.add(convScroll);

        add(left, BorderLayout.WEST);

        // Right: chat area
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);
        JScrollPane chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        right.add(chatScroll, BorderLayout.CENTER);

        // Input
        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.setBackground(Color.WHITE);
        inputRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        msgField = new JTextField();
        msgField.setFont(AppConstants.F_NORMAL);
        msgField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JButton btnSend = UIHelper.createButton("Send", AppConstants.ACCENT, Color.WHITE);
        btnSend.addActionListener(e -> sendMessage());
        msgField.addActionListener(e -> sendMessage());
        inputRow.add(msgField, BorderLayout.CENTER);
        inputRow.add(btnSend, BorderLayout.EAST);
        right.add(inputRow, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);

        JLabel hint = new JLabel("Select a conversation or start a new one.", JLabel.CENTER);
        hint.setForeground(AppConstants.TEXT_LIGHT);
        chatPanel.add(hint);
    }

    private void refreshConversations() {
        convList.removeAll();
        ArrayList<String> partners = Database.getConversationPartners(MainFile.currentUser.getUsername());
        for (String p : partners) {
            JButton btn = new JButton("@" + p);
            btn.setFont(AppConstants.F_NORMAL);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorderPainted(false);
            btn.setBackground(p.equals(selectedUser) ? AppConstants.PRIMARY_LIGHT : new Color(0xFB, 0xFB, 0xFA));
            btn.setMaximumSize(new Dimension(190, 32));
            btn.addActionListener(e -> { selectedUser = p; loadChat(); refreshConversations(); });
            convList.add(btn);
        }
        convList.revalidate(); convList.repaint();
    }

    private void loadChat() {
        chatPanel.removeAll();
        if (selectedUser == null) return;

        JLabel header = new JLabel("Chat with @" + selectedUser);
        header.setFont(AppConstants.F_SECTION);
        header.setAlignmentX(LEFT_ALIGNMENT);
        chatPanel.add(header);
        chatPanel.add(Box.createVerticalStrut(8));

        ArrayList<String[]> msgs = Database.getMessages(MainFile.currentUser.getUsername(), selectedUser);
        for (String[] m : msgs) {
            boolean isMe = m[0].equals(MainFile.currentUser.getUsername());
            JPanel bubble = new JPanel();
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
            bubble.setBackground(isMe ? new Color(0xE8, 0xF0, 0xFE) : new Color(0xF5, 0xF5, 0xF3));
            bubble.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            bubble.setAlignmentX(isMe ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
            bubble.setMaximumSize(new Dimension(400, 100));

            JLabel text = new JLabel("<html>" + m[1] + "</html>");
            text.setFont(AppConstants.F_NORMAL);
            bubble.add(text);
            JLabel time = new JLabel(m[2]);
            time.setFont(AppConstants.F_TINY);
            time.setForeground(AppConstants.TEXT_SEC);
            bubble.add(time);

            JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
            row.setBackground(Color.WHITE);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            row.add(bubble);
            chatPanel.add(row);
        }

        chatPanel.revalidate(); chatPanel.repaint();
    }

    private void sendMessage() {
        if (selectedUser == null || msgField.getText().trim().isEmpty()) return;
        Database.sendMessage(MainFile.currentUser.getUsername(), selectedUser, msgField.getText().trim());
        msgField.setText("");
        loadChat();
    }

    private void newConversation() {
        String user = JOptionPane.showInputDialog(this, "Enter username to message:", "New Message", JOptionPane.PLAIN_MESSAGE);
        if (user != null && !user.trim().isEmpty()) {
            User target = Database.getUserWithUsername(user.trim());
            if (target == null) { UIHelper.showError(this, "User not found!"); return; }
            selectedUser = user.trim();
            loadChat();
            refreshConversations();
        }
    }
}
