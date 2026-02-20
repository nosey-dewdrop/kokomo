import javax.swing.*;
import java.awt.*;
import java.io.File;
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
        setBorder(BorderFactory.createEmptyBorder(32, 48, 20, 48));
        buildUI();
    }

    private void buildUI() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);

        JLabel title = new JLabel("Create New Event");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(AppConstants.TEXT_PRI);
        title.setAlignmentX(LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(20));

        // Poster image
        form.add(createFieldLabel("Poster Image"));
        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        imgRow.setBackground(Color.WHITE); imgRow.setAlignmentX(LEFT_ALIGNMENT);
        imageLabel = new JLabel("No image selected");
        imageLabel.setFont(AppConstants.F_SMALL); imageLabel.setForeground(AppConstants.TEXT_SEC);
        JButton btnImg = new JButton("Choose Image...");
        btnImg.setFont(AppConstants.F_SMALL);
        btnImg.addActionListener(e -> chooseImage());
        imgRow.add(btnImg); imgRow.add(Box.createHorizontalStrut(8)); imgRow.add(imageLabel);
        form.add(imgRow); form.add(Box.createVerticalStrut(12));

        // Title
        form.add(createFieldLabel("Event Title *"));
        titleField = UIHelper.createStyledField();
        titleField.setAlignmentX(LEFT_ALIGNMENT);
        form.add(titleField); form.add(Box.createVerticalStrut(8));

        // Description
        form.add(createFieldLabel("Description"));
        descArea = new JTextArea(3, 30);
        descArea.setFont(AppConstants.F_NORMAL);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER), BorderFactory.createEmptyBorder(6,8,6,8)));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(LEFT_ALIGNMENT);
        form.add(descScroll); form.add(Box.createVerticalStrut(8));

        // Location
        form.add(createFieldLabel("Location *"));
        locationField = UIHelper.createStyledField();
        locationField.setAlignmentX(LEFT_ALIGNMENT);
        form.add(locationField); form.add(Box.createVerticalStrut(8));

        // Start date
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        form.add(createFieldLabel("Start Date & Time"));
        JPanel startRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        startRow.setBackground(Color.WHITE); startRow.setAlignmentX(LEFT_ALIGNMENT);
        dayS = new JSpinner(new SpinnerNumberModel(now.getDayOfMonth(),1,31,1));
        monthS = new JSpinner(new SpinnerNumberModel(now.getMonthValue(),1,12,1));
        yearS = new JSpinner(new SpinnerNumberModel(now.getYear(),2025,2030,1));
        hourS = new JSpinner(new SpinnerNumberModel(14,0,23,1));
        minS = new JSpinner(new SpinnerNumberModel(0,0,59,5));
        startRow.add(dayS); startRow.add(new JLabel("/")); startRow.add(monthS);
        startRow.add(new JLabel("/")); startRow.add(yearS);
        startRow.add(new JLabel("  Time:")); startRow.add(hourS); startRow.add(new JLabel(":")); startRow.add(minS);
        form.add(startRow); form.add(Box.createVerticalStrut(8));

        // End date
        form.add(createFieldLabel("End Date & Time"));
        JPanel endRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        endRow.setBackground(Color.WHITE); endRow.setAlignmentX(LEFT_ALIGNMENT);
        dayE = new JSpinner(new SpinnerNumberModel(now.getDayOfMonth(),1,31,1));
        monthE = new JSpinner(new SpinnerNumberModel(now.getMonthValue(),1,12,1));
        yearE = new JSpinner(new SpinnerNumberModel(now.getYear(),2025,2030,1));
        hourE = new JSpinner(new SpinnerNumberModel(16,0,23,1));
        minE = new JSpinner(new SpinnerNumberModel(0,0,59,5));
        endRow.add(dayE); endRow.add(new JLabel("/")); endRow.add(monthE);
        endRow.add(new JLabel("/")); endRow.add(yearE);
        endRow.add(new JLabel("  Time:")); endRow.add(hourE); endRow.add(new JLabel(":")); endRow.add(minE);
        form.add(endRow); form.add(Box.createVerticalStrut(8));

        // Registration Deadline
        form.add(createFieldLabel("Registration Deadline"));
        JPanel deadRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        deadRow.setBackground(Color.WHITE); deadRow.setAlignmentX(LEFT_ALIGNMENT);
        deadlineDay = new JSpinner(new SpinnerNumberModel(now.getDayOfMonth(),1,31,1));
        deadlineMonth = new JSpinner(new SpinnerNumberModel(now.getMonthValue(),1,12,1));
        deadlineYear = new JSpinner(new SpinnerNumberModel(now.getYear(),2025,2030,1));
        deadRow.add(deadlineDay); deadRow.add(new JLabel("/")); deadRow.add(deadlineMonth);
        deadRow.add(new JLabel("/")); deadRow.add(deadlineYear);
        form.add(deadRow); form.add(Box.createVerticalStrut(8));

        // Capacity + XP + Tier row
        form.add(createFieldLabel("Capacity"));
        capacitySpin = new JSpinner(new SpinnerNumberModel(
            AppConstants.DEFAULT_CAPACITY, AppConstants.MIN_CAPACITY, AppConstants.MAX_CAPACITY, AppConstants.CAPACITY_STEP));
        capacitySpin.setAlignmentX(LEFT_ALIGNMENT);
        form.add(capacitySpin); form.add(Box.createVerticalStrut(8));

        form.add(createFieldLabel("XP Reward for Attendees"));
        xpSpin = new JSpinner(new SpinnerNumberModel(
            AppConstants.DEFAULT_EVENT_XP, AppConstants.MIN_EVENT_XP, AppConstants.MAX_EVENT_XP, 5));
        xpSpin.setAlignmentX(LEFT_ALIGNMENT);
        form.add(xpSpin); form.add(Box.createVerticalStrut(8));

        form.add(createFieldLabel("Minimum Tier Required"));
        String[] tierOptions = new String[AppConstants.TIER_NAMES.length + 1];
        tierOptions[0] = "Anyone";
        for (int i = 0; i < AppConstants.TIER_NAMES.length; i++)
            tierOptions[i+1] = AppConstants.TIER_NAMES[i] + " (" + AppConstants.TIER_THRESHOLDS[i] + "+ XP)";
        tierCombo = new JComboBox<>(tierOptions);
        tierCombo.setAlignmentX(LEFT_ALIGNMENT);
        form.add(tierCombo); form.add(Box.createVerticalStrut(8));

        // Tags
        form.add(createFieldLabel("Tags (comma separated)"));
        tagsField = UIHelper.createStyledField();
        tagsField.setAlignmentX(LEFT_ALIGNMENT);
        form.add(tagsField); form.add(Box.createVerticalStrut(16));

        // Submit
        JButton btnCreate = UIHelper.createButton("Create Event", AppConstants.ACCENT, Color.WHITE);
        btnCreate.setAlignmentX(LEFT_ALIGNMENT);
        btnCreate.addActionListener(e -> handleCreate());
        form.add(btnCreate);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg","jpeg","png","gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fc.getSelectedFile().getAbsolutePath();
            imageLabel.setText(fc.getSelectedFile().getName());
        }
    }

    private void handleCreate() {
        String t = titleField.getText().trim();
        if (t.isEmpty()) { UIHelper.showError(this, "Event title is required!"); return; }
        String loc = locationField.getText().trim();
        if (loc.isEmpty()) { UIHelper.showError(this, "Location is required!"); return; }

        try {
            LocalDateTime start = LocalDateTime.of((int)yearS.getValue(), (int)monthS.getValue(),
                (int)dayS.getValue(), (int)hourS.getValue(), (int)minS.getValue());
            LocalDateTime end = LocalDateTime.of((int)yearE.getValue(), (int)monthE.getValue(),
                (int)dayE.getValue(), (int)hourE.getValue(), (int)minE.getValue());
            LocalDateTime deadline = LocalDateTime.of((int)deadlineYear.getValue(), (int)deadlineMonth.getValue(),
                (int)deadlineDay.getValue(), 23, 59);

            Event ev = new Event(0, t, descArea.getText().trim(), loc, start, end, deadline,
                (int)capacitySpin.getValue(), MainFile.currentUser.getUsername());
            ev.setImagePath(selectedImagePath);
            ev.setXpReward((int)xpSpin.getValue());
            ev.setMinTierIndex(tierCombo.getSelectedIndex());

            String tags = tagsField.getText().trim();
            if (!tags.isEmpty()) {
                for (String tag : tags.split(",")) {
                    String trimmed = tag.trim().toLowerCase();
                    if (!trimmed.isEmpty()) ev.addTag(trimmed);
                }
            }

            // Auto-generate poster if none selected
            if (ev.getImagePath() == null || ev.getImagePath().isEmpty()) {
                String generated = PosterGenerator.generateDefault(ev);
                if (generated != null) ev.setImagePath(generated);
            }

            int id = Database.addToDatabase(ev);
            ev.setId(id);
            Database.addXP(MainFile.currentUser.getUsername(), AppConstants.XP_CREATE_EVENT);

            // Notify followers
            for (String follower : MainFile.currentUser.getFollowers()) {
                Database.addNotification(follower,
                    MainFile.currentUser.getDisplayName() + " created a new event: " + t);
            }

            UIHelper.showSuccess(this, "Event created!");
            home.showFeed();
        } catch (Exception ex) {
            UIHelper.showError(this, "Invalid date! Please check your input.");
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.F_SMALL);
        l.setForeground(AppConstants.TEXT_SEC);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return l;
    }
}
