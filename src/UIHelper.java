import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UI yardimci metotlari - modern Swing styling.
 * Yuvarlak butonlar, golge efektli kartlar, modern renkler.
 */
public class UIHelper {

    // ==========================================
    // BUTONLAR
    // ==========================================

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
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
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 34));
        return b;
    }

    public static JButton createOutlineButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(AppConstants.F_SMALL);
        b.setForeground(color);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
            }
            public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
        return b;
    }

    /**
     * Kucuk tag/badge butonu.
     */
    public static JButton createTagButton(String text, Color color, boolean selected) {
        JButton b = new JButton(text);
        b.setFont(AppConstants.F_TINY);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);

        if (selected) {
            b.setBackground(color);
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(color);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color),
                BorderFactory.createEmptyBorder(2, 9, 2, 9)));
        }
        return b;
    }

    // ==========================================
    // LABELS
    // ==========================================

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
        return l;
    }

    public static JLabel createSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_SECTION);
        l.setForeground(AppConstants.PRIMARY);
        return l;
    }

    /**
     * Renkli badge label (Going, Interested, Maybe vb.)
     */
    public static JLabel createBadgeLabel(String text, Color bg, Color fg) {
        JLabel l = new JLabel(" " + text + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 4, 4));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(AppConstants.F_TINY);
        l.setForeground(fg);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return l;
    }

    // ==========================================
    // TEXT FIELDS
    // ==========================================

    public static JTextField createStyledField() {
        JTextField f = new JTextField(20);
        f.setFont(AppConstants.F_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField(20);
        f.setFont(AppConstants.F_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    // ==========================================
    // KARTLAR
    // ==========================================

    /**
     * Golge efektli kart paneli.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            BorderFactory.createEmptyBorder(
                AppConstants.CARD_PADDING, AppConstants.CARD_PADDING,
                AppConstants.CARD_PADDING, AppConstants.CARD_PADDING)));
        return card;
    }

    /**
     * Basit golge border.
     */
    static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Subtle shadow
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 2, w - 2, h - 1, 4, 4));
            // Border
            g2.setColor(AppConstants.BORDER);
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 2, 4, 4));
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1, 1, 3, 1); }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

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

    // ==========================================
    // AVATAR
    // ==========================================

    public static JPanel createAvatar(String letter, Color color, int size) {
        JPanel av = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient
                GradientPaint gp = new GradientPaint(0, 0, color, size, size, color.darker());
                g2.setPaint(gp);
                g2.fillOval(1, 1, size - 2, size - 2);
                // Harf
                g2.setColor(Color.WHITE);
                int fontSize = size / 2;
                g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
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

    // ==========================================
    // CLICKABLE USERNAME
    // ==========================================

    public static JLabel createClickableUsername(User user, HomeScreen homeScreen) {
        if (user == null) return new JLabel("@?");

        String display = user.getProfileBadge();
        JLabel lbl = new JLabel(display);
        lbl.setFont(AppConstants.F_SMALL);
        lbl.setForeground(AppConstants.ACCENT);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lbl.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                lbl.setText("<html><u>" + display + "</u></html>");
            }
            public void mouseExited(MouseEvent e) {
                lbl.setText(display);
            }
            public void mouseClicked(MouseEvent e) {
                homeScreen.navigateToProfile(user);
            }
        });
        return lbl;
    }

    // ==========================================
    // GRIDBAG HELPER
    // ==========================================

    public static GridBagConstraints createFullWidthGBC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 0, 3, 0);
        gc.gridx = 0;
        gc.weightx = 1;
        return gc;
    }

    /**
     * Separator (ince cizgi).
     */
    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppConstants.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
