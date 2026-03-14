package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class CreateEventPanel extends JPanel {

    private HomeScreen home;
    private JTextField titleField, locationField, tagsField;
    private JTextArea descArea;
    private JSpinner dayS, monthS, yearS, hourS, minS;
    private JSpinner dayE, monthE, yearE, hourE, minE;
    private JSpinner deadlineDay, deadlineMonth, deadlineYear;
    private JSpinner capacitySpin, xpSpin;
    private JComboBox<String> tierCombo;
    private JLabel imageLabel;
    private String selectedImagePath = "";

    public CreateEventPanel(HomeScreen home) {
        this.home = home;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        buildUI();
    }

    private void buildUI() {
        JPanel form = UIHelper.createPagePanel();

        form.add(UIHelper.createPageTitle(AppConstants.PAGE_CREATE));
        form.add(Box.createVerticalStrut(4));
        form.add(UIHelper.createSubtitle(AppConstants.PAGE_CREATE_SUB));
        form.add(Box.createVerticalStrut(24));

        form.add(createFieldLabel(AppConstants.FIELD_EVENT_TITLE));
        titleField = UIHelper.createPlaceholderField(AppConstants.PH_EVENT_TITLE);
        titleField.setAlignmentX(LEFT_ALIGNMENT);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(titleField);
        form.add(Box.createVerticalStrut(16));

        form.add(createFieldLabel(AppConstants.FIELD_DESCRIPTION));
        descArea = new JTextArea(3, 30);
        descArea.setFont(AppConstants.F_NORMAL);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        form.add(descScroll);
        form.add(Box.createVerticalStrut(16));

        form.add(createFieldLabel(AppConstants.FIELD_LOCATION));
        locationField = UIHelper.createPlaceholderField(AppConstants.PH_LOCATION);
        locationField.setAlignmentX(LEFT_ALIGNMENT);
        locationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(locationField);
        form.add(Box.createVerticalStrut(16));

        form.add(createFieldLabel(AppConstants.FIELD_POSTER));
        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        imgRow.setBackground(Color.WHITE);
        imgRow.setAlignmentX(LEFT_ALIGNMENT);
        imageLabel = new JLabel(AppConstants.PH_AUTO_POSTER);
        imageLabel.setFont(AppConstants.F_TINY);
        imageLabel.setForeground(AppConstants.TEXT_LIGHT);
        JButton btnImg = UIHelper.createOutlineButton(AppConstants.BTN_CHOOSE_IMG, AppConstants.TEXT_SEC);
        btnImg.addActionListener(e -> chooseImage());
        imgRow.add(btnImg);
        imgRow.add(imageLabel);
        form.add(imgRow);
        form.add(Box.createVerticalStrut(20));

        form.add(UIHelper.createSeparator());
        form.add(Box.createVerticalStrut(16));

        form.add(UIHelper.createSectionLabel(AppConstants.SEC_DATE_TIME));
        form.add(Box.createVerticalStrut(12));

        LocalDateTime now = LocalDateTime.now().plusDays(1);

        form.add(createFieldLabel(AppConstants.FIELD_START));
        JPanel startRow = createDateRow(now, true);
        dayS = (JSpinner) startRow.getClientProperty("day");
        monthS = (JSpinner) startRow.getClientProperty("month");
        yearS = (JSpinner) startRow.getClientProperty("year");
        hourS = (JSpinner) startRow.getClientProperty("hour");
        minS = (JSpinner) startRow.getClientProperty("min");
        form.add(startRow);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel(AppConstants.FIELD_END));
        JPanel endRow = createDateRow(now, true);
        dayE = (JSpinner) endRow.getClientProperty("day");
        monthE = (JSpinner) endRow.getClientProperty("month");
        yearE = (JSpinner) endRow.getClientProperty("year");
        hourE = (JSpinner) endRow.getClientProperty("hour");
        minE = (JSpinner) endRow.getClientProperty("min");
        ((SpinnerNumberModel) hourE.getModel()).setValue(16);
        form.add(endRow);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel(AppConstants.FIELD_DEADLINE));
        JPanel deadRow = createDateRow(now, false);
        deadlineDay = (JSpinner) deadRow.getClientProperty("day");
        deadlineMonth = (JSpinner) deadRow.getClientProperty("month");
        deadlineYear = (JSpinner) deadRow.getClientProperty("year");
        form.add(deadRow);
        form.add(Box.createVerticalStrut(20));

        form.add(UIHelper.createSeparator());
        form.add(Box.createVerticalStrut(16));

        form.add(UIHelper.createSectionLabel(AppConstants.SEC_SETTINGS));
        form.add(Box.createVerticalStrut(12));

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 24, 0));
        twoCol.setBackground(Color.WHITE);
        twoCol.setAlignmentX(LEFT_ALIGNMENT);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel capPanel = new JPanel();
        capPanel.setLayout(new BoxLayout(capPanel, BoxLayout.Y_AXIS));
        capPanel.setOpaque(false);
        capPanel.add(createFieldLabel(AppConstants.FIELD_CAPACITY));
        capacitySpin = new JSpinner(new SpinnerNumberModel(
            AppConstants.DEFAULT_CAPACITY, AppConstants.MIN_CAPACITY, AppConstants.MAX_CAPACITY, AppConstants.CAPACITY_STEP));
        capacitySpin.setAlignmentX(LEFT_ALIGNMENT);
        capPanel.add(capacitySpin);
        twoCol.add(capPanel);

        JPanel xpPanel = new JPanel();
        xpPanel.setLayout(new BoxLayout(xpPanel, BoxLayout.Y_AXIS));
        xpPanel.setOpaque(false);
        xpPanel.add(createFieldLabel(AppConstants.FIELD_XP_REWARD));
        xpSpin = new JSpinner(new SpinnerNumberModel(
            AppConstants.DEFAULT_EVENT_XP, AppConstants.MIN_EVENT_XP, AppConstants.MAX_EVENT_XP, 5));
        xpSpin.setAlignmentX(LEFT_ALIGNMENT);
        xpPanel.add(xpSpin);
        twoCol.add(xpPanel);

        form.add(twoCol);
        form.add(Box.createVerticalStrut(12));

        form.add(createFieldLabel(AppConstants.FIELD_MIN_TIER));
        String[] tierOptions = new String[AppConstants.TIER_NAMES.length + 1];
        tierOptions[0] = "Anyone";
        for (int i = 0; i < AppConstants.TIER_NAMES.length; i++)
            tierOptions[i + 1] = AppConstants.TIER_NAMES[i] + " (" + AppConstants.TIER_THRESHOLDS[i] + "+ XP)";
        tierCombo = new JComboBox<>(tierOptions);
        tierCombo.setFont(AppConstants.F_NORMAL);
        tierCombo.setAlignmentX(LEFT_ALIGNMENT);
        tierCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(tierCombo);
        form.add(Box.createVerticalStrut(12));

        form.add(createFieldLabel(AppConstants.FIELD_TAGS));
        tagsField = UIHelper.createPlaceholderField(AppConstants.PH_TAGS);
        tagsField.setAlignmentX(LEFT_ALIGNMENT);
        tagsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(tagsField);
        form.add(Box.createVerticalStrut(24));

        JButton btnCreate = UIHelper.createButton(AppConstants.BTN_CREATE, AppConstants.PRIMARY, Color.WHITE);
        btnCreate.setAlignmentX(LEFT_ALIGNMENT);
        btnCreate.setMaximumSize(new Dimension(200, 44));
        btnCreate.addActionListener(e -> handleCreate());
        form.add(btnCreate);
        form.add(Box.createVerticalStrut(20));

        add(UIHelper.wrapInScroll(form), BorderLayout.CENTER);
    }

    private JPanel createDateRow(LocalDateTime now, boolean withTime) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JSpinner day = new JSpinner(new SpinnerNumberModel(now.getDayOfMonth(), 1, 31, 1));
        JSpinner month = new JSpinner(new SpinnerNumberModel(now.getMonthValue(), 1, 12, 1));
        JSpinner year = new JSpinner(new SpinnerNumberModel(now.getYear(), 2025, 2030, 1));

        row.add(day); row.add(new JLabel("/"));
        row.add(month); row.add(new JLabel("/"));
        row.add(year);

        row.putClientProperty("day", day);
        row.putClientProperty("month", month);
        row.putClientProperty("year", year);

        if (withTime) {
            JSpinner hour = new JSpinner(new SpinnerNumberModel(14, 0, 23, 1));
            JSpinner min = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
            row.add(Box.createHorizontalStrut(12));
            row.add(new JLabel("Time:"));
            row.add(hour); row.add(new JLabel(":"));
            row.add(min);
            row.putClientProperty("hour", hour);
            row.putClientProperty("min", min);
        }
        return row;
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fc.getSelectedFile().getAbsolutePath();
            imageLabel.setText(fc.getSelectedFile().getName());
            imageLabel.setForeground(AppConstants.TEXT_PRI);
        }
    }

    private void handleCreate() {
        String t = UIHelper.getFieldText(titleField, AppConstants.PH_EVENT_TITLE);
        if (t.isEmpty()) { UIHelper.showError(this, AppConstants.ERR_TITLE_REQUIRED); return; }
        String loc = UIHelper.getFieldText(locationField, AppConstants.PH_LOCATION);
        if (loc.isEmpty()) { UIHelper.showError(this, AppConstants.ERR_LOC_REQUIRED); return; }

        try {
            LocalDateTime start = LocalDateTime.of((int) yearS.getValue(), (int) monthS.getValue(),
                (int) dayS.getValue(), (int) hourS.getValue(), (int) minS.getValue());
            LocalDateTime end = LocalDateTime.of((int) yearE.getValue(), (int) monthE.getValue(),
                (int) dayE.getValue(), (int) hourE.getValue(), (int) minE.getValue());
            LocalDateTime deadline = LocalDateTime.of((int) deadlineYear.getValue(), (int) deadlineMonth.getValue(),
                (int) deadlineDay.getValue(), 23, 59);

            Event ev = new Event(0, t, descArea.getText().trim(), loc, start, end, deadline,
                (int) capacitySpin.getValue(), MainFile.currentUser.getUsername());
            ev.setImagePath(selectedImagePath);
            ev.setXpReward((int) xpSpin.getValue());
            ev.setMinTierIndex(tierCombo.getSelectedIndex());

            String tags = UIHelper.getFieldText(tagsField, AppConstants.PH_TAGS);
            if (!tags.isEmpty()) {
                for (String tag : tags.split(",")) {
                    String trimmed = tag.trim().toLowerCase();
                    if (!trimmed.isEmpty()) ev.addTag(trimmed);
                }
            }

            if (ev.getImagePath() == null || ev.getImagePath().isEmpty()) {
                String generated = PosterGenerator.generateDefault(ev);
                if (generated != null) ev.setImagePath(generated);
            }

            int id = Database.addToDatabase(ev);
            ev.setId(id);
            Database.addXP(MainFile.currentUser.getUsername(), AppConstants.XP_CREATE_EVENT);

            String notifMsg = String.format(AppConstants.NOTIF_NEW_EVENT,
                MainFile.currentUser.getDisplayName(), t);
            for (String follower : MainFile.currentUser.getFollowers()) {
                Database.addNotification(follower, notifMsg);
            }

            UIHelper.showSuccess(this, AppConstants.SUC_EVENT_CREATED);
            home.showFeed();
        } catch (Exception ex) {
            UIHelper.showError(this, AppConstants.ERR_INVALID_DATE);
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_SMALL);
        l.setForeground(AppConstants.TEXT_SEC);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }
}
