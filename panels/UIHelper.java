package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class UIHelper {

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker()
                        : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(fg);
        b.setFont(AppConstants.F_SECTION);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 24, 40));
        return b;
    }

    public static JButton createOutlineButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(AppConstants.F_SMALL);
        b.setForeground(color);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1, true),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 15));
            }
            public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
        return b;
    }

    public static JButton createTagButton(String text, Color color, boolean selected) {
        JButton b = new JButton(text);
        b.setFont(AppConstants.F_TINY);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        if (selected) {
            b.setBackground(color);
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(color);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                BorderFactory.createEmptyBorder(3, 11, 3, 11)));
        }
        return b;
    }

    public static JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_NORMAL);
        l.setForeground(AppConstants.TEXT_PRI);
        return l;
    }

    public static JLabel createSmallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_SMALL);
        l.setForeground(AppConstants.TEXT_SEC);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel createSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_TITLE);
        l.setForeground(AppConstants.TEXT_PRI);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel createPageTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_BIG);
        l.setForeground(AppConstants.TEXT_PRI);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel createSubtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_SMALL);
        l.setForeground(AppConstants.TEXT_SEC);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel createBadgeLabel(String text, Color bg, Color fg) {
        JLabel l = new JLabel(" " + text + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(AppConstants.F_TINY);
        l.setForeground(fg);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    public static JTextField createStyledField() {
        JTextField f = new JTextField(20);
        f.setFont(AppConstants.F_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        return f;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField(20);
        f.setFont(AppConstants.F_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        return f;
    }

    public static JTextField createPlaceholderField(String placeholder) {
        JTextField f = createStyledField();
        f.setForeground(AppConstants.TEXT_LIGHT);
        f.setText(placeholder);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(AppConstants.TEXT_PRI);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(AppConstants.TEXT_LIGHT);
                }
            }
        });
        return f;
    }

    public static String getFieldText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new SoftShadowBorder(),
            BorderFactory.createEmptyBorder(
                AppConstants.CARD_PADDING, AppConstants.CARD_PADDING,
                AppConstants.CARD_PADDING, AppConstants.CARD_PADDING)));
        return card;
    }

    static class SoftShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int R = AppConstants.CARD_RADIUS;
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 2, w - 2, h - 1, R, R));
            g2.setColor(AppConstants.BORDER);
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 2, R, R));
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1, 1, 3, 1); }
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static JPanel createAvatar(String letter, Color color, int size) {
        JPanel av = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, color, size, size, color.darker()));
                g2.fillOval(1, 1, size - 2, size - 2);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, size / 2));
                FontMetrics fm = g2.getFontMetrics();
                String ch = letter.substring(0, 1).toUpperCase();
                g2.drawString(ch, size / 2 - fm.stringWidth(ch) / 2, size / 2 + fm.getAscent() / 3);
            }
        };
        av.setPreferredSize(new Dimension(size, size));
        av.setMinimumSize(new Dimension(size, size));
        av.setMaximumSize(new Dimension(size, size));
        av.setOpaque(false);
        return av;
    }

    public static JLabel createClickableUsername(User user, HomeScreen homeScreen) {
        if (user == null) return new JLabel("@?");
        String display = user.getProfileBadge();
        JLabel lbl = new JLabel(display);
        lbl.setFont(AppConstants.F_SMALL);
        lbl.setForeground(AppConstants.ACCENT);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e)  { lbl.setText("<html><u>" + display + "</u></html>"); }
            public void mouseExited(MouseEvent e)   { lbl.setText(display); }
            public void mouseClicked(MouseEvent e)  { homeScreen.navigateToProfile(user); }
        });
        return lbl;
    }

    public static GridBagConstraints createFullWidthGBC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 0, 3, 0);
        gc.gridx = 0;
        gc.weightx = 1;
        return gc;
    }

    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppConstants.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }


    public static JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_SMALL);
        l.setForeground(AppConstants.TEXT_SEC);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        return l;
    }

    public static JPanel createPagePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(
            AppConstants.PAGE_PAD_Y, AppConstants.PAGE_PAD_X,
            20, AppConstants.PAGE_PAD_X));
        return p;
    }

    public static JScrollPane wrapInScroll(JPanel content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }
}
