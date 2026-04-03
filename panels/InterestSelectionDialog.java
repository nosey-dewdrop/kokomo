package panels;

import model.*;
import screens.*;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class InterestSelectionDialog extends JDialog {

    private boolean confirmed = false;
    private ArrayList<JCheckBox> checkBoxes = new ArrayList<>();

    public InterestSelectionDialog(Window owner, ArrayList<String> currentInterests) {
        super(owner, "Select Your Interests", ModalityType.APPLICATION_MODAL);
        setSize(420, 480);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI(currentInterests);
    }

    private void buildUI(ArrayList<String> current) {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Choose topics you're interested in:");
        title.setFont(AppConstants.F_TITLE);
        title.setForeground(AppConstants.TEXT_PRI);
        main.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 4));
        grid.setBackground(Color.WHITE);
        for (String cat : AppConstants.INTEREST_CATEGORIES) {
            JCheckBox cb = new JCheckBox(cat);
            cb.setFont(AppConstants.F_NORMAL);
            cb.setBackground(Color.WHITE);
            if (current.contains(cat.toLowerCase()) || current.contains(cat)) cb.setSelected(true);
            checkBoxes.add(cb);
            grid.add(cb);
        }
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        main.add(scroll, BorderLayout.CENTER);

        JButton btnOk = UIHelper.createButton("Confirm", AppConstants.ACCENT, Color.WHITE);
        btnOk.addActionListener(e -> { confirmed = true; dispose(); });
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnOk);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    public boolean isConfirmed() { return confirmed; }

    public ArrayList<String> getSelectedInterests() {
        ArrayList<String> selected = new ArrayList<>();
        for (JCheckBox cb : checkBoxes)
            if (cb.isSelected()) selected.add(cb.getText().toLowerCase());
        return selected;
    }
}
