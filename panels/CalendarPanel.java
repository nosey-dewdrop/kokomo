package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class CalendarPanel extends JPanel {

    private HomeScreen home;
    private YearMonth currentMonth;
    private JPanel calGrid;
    private JLabel monthLabel;

    public CalendarPanel(HomeScreen home) {
        this.home = home;
        this.currentMonth = YearMonth.now();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 48, 20, 48));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Calendar");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(12));

        // Month nav
        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navRow.setBackground(Color.WHITE);
        navRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton prev = new JButton("<");
        prev.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); refreshCal(); });
        JButton next = new JButton(">");
        next.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); refreshCal(); });
        monthLabel = new JLabel();
        monthLabel.setFont(AppConstants.F_TITLE);
        navRow.add(prev); navRow.add(monthLabel); navRow.add(next);
        header.add(navRow);
        header.add(Box.createVerticalStrut(12));

        add(header, BorderLayout.NORTH);

        calGrid = new JPanel(new GridLayout(0, 7, 2, 2));
        calGrid.setBackground(AppConstants.BORDER);
        JScrollPane scroll = new JScrollPane(calGrid);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        refreshCal();
    }

    private void refreshCal() {
        calGrid.removeAll();
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());

        // Day headers
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, JLabel.CENTER);
            lbl.setFont(AppConstants.F_TINY);
            lbl.setForeground(AppConstants.TEXT_SEC);
            lbl.setOpaque(true);
            lbl.setBackground(Color.WHITE);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            calGrid.add(lbl);
        }

        // Get events for this month
        ArrayList<Event> allEvents = Database.getAllEvents();
        Map<Integer, ArrayList<Event>> eventsByDay = new HashMap<>();
        for (Event ev : allEvents) {
            if (ev.getDateTime().getYear() == currentMonth.getYear() &&
                ev.getDateTime().getMonthValue() == currentMonth.getMonthValue()) {
                int day = ev.getDateTime().getDayOfMonth();
                eventsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(ev);
            }
        }

        // Empty cells before first day
        LocalDate first = currentMonth.atDay(1);
        int startDow = first.getDayOfWeek().getValue(); // Mon=1
        for (int i = 1; i < startDow; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(new Color(0xFB, 0xFB, 0xFA));
            calGrid.add(empty);
        }

        // Days
        for (int d = 1; d <= currentMonth.lengthOfMonth(); d++) {
            JPanel cell = new JPanel();
            cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
            cell.setBackground(Color.WHITE);
            cell.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            boolean isToday = currentMonth.atDay(d).equals(LocalDate.now());
            JLabel dayLbl = new JLabel(String.valueOf(d));
            dayLbl.setFont(isToday ? AppConstants.F_SECTION : AppConstants.F_SMALL);
            dayLbl.setForeground(isToday ? AppConstants.ACCENT : AppConstants.TEXT_PRI);
            dayLbl.setAlignmentX(LEFT_ALIGNMENT);
            cell.add(dayLbl);

            if (eventsByDay.containsKey(d)) {
                for (Event ev : eventsByDay.get(d)) {
                    JLabel evLbl = new JLabel(ev.getTitle());
                    evLbl.setFont(AppConstants.F_TINY);
                    evLbl.setForeground(Color.WHITE);
                    evLbl.setOpaque(true);
                    evLbl.setBackground(AppConstants.ACCENT);
                    evLbl.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
                    evLbl.setAlignmentX(LEFT_ALIGNMENT);
                    evLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    final Event clicked = ev;
                    evLbl.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent e) { home.showEventDetail(clicked); }
                    });
                    cell.add(Box.createVerticalStrut(1));
                    cell.add(evLbl);
                }
            }

            calGrid.add(cell);
        }

        calGrid.revalidate();
        calGrid.repaint();
    }
}
