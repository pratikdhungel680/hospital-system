import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/** Modal dialog for editing an existing patient's details. */
public class EditPatientDialog extends JDialog {
    public EditPatientDialog(Frame owner, Patient patient, Consumer<Patient> onSave) {
        super(owner, "Edit Patient #" + patient.getId(), true);
        setSize(400, 420);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG);

        JPanel panel = new JPanel();
        panel.setBackground(UITheme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Edit Patient #" + patient.getId());
        title.setFont(UITheme.heading());
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        RoundedTextField nameField = new RoundedTextField(18);
        nameField.setText(patient.getName());
        RoundedTextField ageField = new RoundedTextField(18);
        ageField.setText(String.valueOf(patient.getAge()));
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderBox.setSelectedItem(patient.getGender());
        genderBox.setFont(UITheme.body());
        RoundedTextField contactField = new RoundedTextField(18);
        contactField.setText(patient.getContact());
        RoundedTextField diseaseField = new RoundedTextField(18);
        diseaseField.setText(patient.getDisease());

        JLabel status = new JLabel(" ");
        status.setFont(UITheme.small());
        status.setForeground(UITheme.DANGER);
        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent[] fields = {nameField, ageField, genderBox, contactField, diseaseField};
        String[] labels = {"Full Name", "Age", "Gender", "Contact Number", "Diagnosis / Disease"};
        for (int i = 0; i < fields.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(UITheme.bodyBold());
            l.setForeground(UITheme.TEXT_PRIMARY);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            fields[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            fields[i].setMaximumSize(new Dimension(320, 38));
            panel.add(l);
            panel.add(Box.createVerticalStrut(4));
            panel.add(fields[i]);
            panel.add(Box.createVerticalStrut(10));
        }
        panel.add(status);
        panel.add(Box.createVerticalStrut(8));

        RoundedButton saveBtn = new RoundedButton("Save Changes", UITheme.ACCENT, UITheme.ACCENT_HOVER);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(320, 42));
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty() || !name.matches("[A-Za-z ]+")) {
                    status.setText("Name must contain only letters and spaces.");
                    return;
                }
                int age;
                try {
                    age = Integer.parseInt(ageField.getText().trim());
                } catch (NumberFormatException ex) {
                    status.setText("Age must be a whole number.");
                    return;
                }
                if (age < 1 || age > 120) {
                    status.setText("Age must be between 1 and 120.");
                    return;
                }
                String disease = diseaseField.getText().trim();
                if (disease.isEmpty()) {
                    status.setText("Diagnosis cannot be empty.");
                    return;
                }
                patient.setName(name);
                patient.setAge(age);
                patient.setGender((String) genderBox.getSelectedItem());
                patient.setContact(contactField.getText().trim());
                patient.setDisease(disease);
                onSave.accept(patient);
                dispose();
            } catch (Exception ex) {
                status.setText("Unexpected error: " + ex.getMessage());
            }
        });
        panel.add(saveBtn);

        setContentPane(panel);
    }
}
