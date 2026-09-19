import javax.swing.*;
import java.awt.*;

/** Form panel for registering a new patient, with inline validation. */
public class AddPatientPanel extends JPanel {
    private final PatientDAO dao = new PatientDAO();
    private final Runnable onSaved;

    private RoundedTextField idField, nameField, ageField, contactField;
    private JComboBox<String> genderBox;
    private RoundedTextField diseaseField;
    private JLabel statusLabel;

    public AddPatientPanel(Runnable onSaved) {
        this.onSaved = onSaved;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        JLabel title = new JLabel("Add Patient");
        title.setFont(UITheme.display());
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Register a new patient record");
        sub.setFont(UITheme.body());
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 20, 0));
        header.add(title);
        header.add(sub);

        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setMaximumSize(new Dimension(560, 420));
        card.setPreferredSize(new Dimension(520, 400));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 0, 6, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.weightx = 1;
        int row = 0;

        idField = new RoundedTextField(20);
        nameField = new RoundedTextField(20);
        ageField = new RoundedTextField(20);
        contactField = new RoundedTextField(20);
        diseaseField = new RoundedTextField(20);
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderBox.setFont(UITheme.body());

        addField(card, gc, row++, "Patient ID", idField);
        addField(card, gc, row++, "Full Name", nameField);
        addField(card, gc, row++, "Age", ageField);
        addField(card, gc, row++, "Gender", genderBox);
        addField(card, gc, row++, "Contact Number", contactField);
        addField(card, gc, row++, "Diagnosis / Disease", diseaseField);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.small());
        statusLabel.setForeground(UITheme.DANGER);
        gc.gridy = row++;
        card.add(statusLabel, gc);

        RoundedButton saveBtn = new RoundedButton("Save Patient", UITheme.ACCENT, UITheme.ACCENT_HOVER);
        gc.gridy = row++;
        gc.insets = new Insets(10, 0, 0, 0);
        card.add(saveBtn, gc);
        saveBtn.addActionListener(e -> save());

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(header);
        wrapper.add(card);

        add(wrapper, BorderLayout.NORTH);
    }

    private void addField(JPanel parent, GridBagConstraints gc, int row, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.bodyBold());
        l.setForeground(UITheme.TEXT_PRIMARY);
        gc.gridy = row * 2;
        gc.insets = new Insets(10, 0, 2, 0);
        parent.add(l, gc);

        field.setPreferredSize(new Dimension(200, 38));
        gc.gridy = row * 2 + 1;
        gc.insets = new Insets(0, 0, 0, 0);
        parent.add(field, gc);
    }

    private void save() {
        statusLabel.setForeground(UITheme.DANGER);
        try {
            int id = parsePositiveInt(idField.getText(), "Patient ID");
            String name = requireLetters(nameField.getText(), "Name");
            int age = parseAge(ageField.getText());
            String gender = (String) genderBox.getSelectedItem();
            String contact = contactField.getText().trim();
            String disease = requireNonEmpty(diseaseField.getText(), "Diagnosis");

            dao.addPatient(new Patient(id, name, age, disease, gender, contact));
            statusLabel.setForeground(UITheme.ACCENT);
            statusLabel.setText("Patient added successfully.");
            clearForm();
            if (onSaved != null) onSaved.run();
        } catch (IllegalArgumentException | HospitalException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
        contactField.setText("");
        diseaseField.setText("");
        genderBox.setSelectedIndex(0);
    }

    private int parsePositiveInt(String text, String fieldName) {
        try {
            int v = Integer.parseInt(text.trim());
            if (v <= 0) throw new IllegalArgumentException(fieldName + " must be a positive number.");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    private int parseAge(String text) {
        try {
            int v = Integer.parseInt(text.trim());
            if (v < 1 || v > 120) throw new IllegalArgumentException("Age must be between 1 and 120.");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a whole number.");
        }
    }

    private String requireLetters(String text, String fieldName) {
        String t = text.trim();
        if (t.isEmpty()) throw new IllegalArgumentException(fieldName + " cannot be empty.");
        if (!t.matches("[A-Za-z ]+")) throw new IllegalArgumentException(fieldName + " must contain only letters and spaces.");
        return t;
    }

    private String requireNonEmpty(String text, String fieldName) {
        String t = text.trim();
        if (t.isEmpty()) throw new IllegalArgumentException(fieldName + " cannot be empty.");
        return t;
    }
}
