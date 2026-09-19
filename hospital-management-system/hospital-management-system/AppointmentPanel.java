import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Book new appointments and browse the existing appointment schedule. */
public class AppointmentPanel extends JPanel {
    private final PatientDAO dao = new PatientDAO();
    private final Runnable onChanged;

    private RoundedTextField patientIdField, doctorField, dateField;
    private JLabel formStatus;
    private DefaultTableModel model;

    public AppointmentPanel(Runnable onChanged) {
        this.onChanged = onChanged;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        JLabel title = new JLabel("Appointments");
        title.setFont(UITheme.display());
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Book a new appointment or review the schedule");
        sub.setFont(UITheme.body());
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));
        header.add(title);
        header.add(sub);

        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(UITheme.PAD, 0));
        body.setOpaque(false);
        body.add(buildBookingForm(), BorderLayout.WEST);
        body.add(buildTable(), BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(header, BorderLayout.NORTH);
        wrap.add(body, BorderLayout.CENTER);

        removeAll();
        add(wrap, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildBookingForm() {
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        card.setPreferredSize(new Dimension(280, 340));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.weightx = 1;
        int row = 0;

        JLabel formTitle = new JLabel("Book Appointment");
        formTitle.setFont(UITheme.heading());
        formTitle.setForeground(UITheme.TEXT_PRIMARY);
        gc.gridy = row++;
        gc.insets = new Insets(0, 0, 14, 0);
        card.add(formTitle, gc);

        patientIdField = new RoundedTextField(14);
        doctorField = new RoundedTextField(14);
        dateField = new RoundedTextField(14);
        dateField.setToolTipText("YYYY-MM-DD");

        row = addLabeledField(card, gc, row, "Patient ID", patientIdField);
        row = addLabeledField(card, gc, row, "Doctor Name", doctorField);
        row = addLabeledField(card, gc, row, "Date (YYYY-MM-DD)", dateField);

        formStatus = new JLabel(" ");
        formStatus.setFont(UITheme.small());
        formStatus.setForeground(UITheme.DANGER);
        gc.gridy = row++;
        gc.insets = new Insets(4, 0, 8, 0);
        card.add(formStatus, gc);

        RoundedButton bookBtn = new RoundedButton("Book Appointment", UITheme.ACCENT, UITheme.ACCENT_HOVER);
        gc.gridy = row++;
        card.add(bookBtn, gc);
        bookBtn.addActionListener(e -> book());

        return card;
    }

    private int addLabeledField(JPanel parent, GridBagConstraints gc, int row, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.bodyBold());
        l.setForeground(UITheme.TEXT_PRIMARY);
        gc.gridy = row++;
        gc.insets = new Insets(6, 0, 2, 0);
        parent.add(l, gc);

        field.setPreferredSize(new Dimension(200, 36));
        gc.gridy = row++;
        gc.insets = new Insets(0, 0, 0, 0);
        parent.add(field, gc);
        return row;
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(new Object[]{"Appt ID", "Patient ID", "Patient", "Doctor", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.body());
        table.setRowHeight(30);
        table.getTableHeader().setFont(UITheme.bodyBold());
        table.getTableHeader().setBackground(UITheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(UITheme.BORDER);
        table.setShowVerticalLines(false);

        RoundedCard card = new RoundedCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void book() {
        formStatus.setForeground(UITheme.DANGER);
        try {
            int patientId;
            try {
                patientId = Integer.parseInt(patientIdField.getText().trim());
                if (patientId <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                formStatus.setText("Patient ID must be a positive number.");
                return;
            }
            String doctor = doctorField.getText().trim();
            if (doctor.isEmpty() || !doctor.matches("[A-Za-z. ]+")) {
                formStatus.setText("Doctor name must contain only letters.");
                return;
            }
            String date = dateField.getText().trim();
            try {
                LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                formStatus.setText("Date must be in YYYY-MM-DD format.");
                return;
            }

            dao.bookAppointment(patientId, doctor, date);
            formStatus.setForeground(UITheme.ACCENT);
            formStatus.setText("Appointment booked.");
            patientIdField.setText("");
            doctorField.setText("");
            dateField.setText("");
            refresh();
            if (onChanged != null) onChanged.run();
        } catch (HospitalException e) {
            formStatus.setText(e.getMessage());
        }
    }

    public void refresh() {
        try {
            List<Appointment> appts = dao.getAllAppointments();
            model.setRowCount(0);
            for (Appointment a : appts) {
                model.addRow(new Object[]{a.getAppointmentId(), a.getPatientId(), a.getPatientName(), a.getDoctor(), a.getDate()});
            }
        } catch (HospitalException e) {
            // Table stays as-is; booking form will still show its own errors.
        }
    }
}
