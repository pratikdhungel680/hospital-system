import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Book appointments and browse the schedule.
 *
 * Left:  booking form - search and pick a patient, choose (or type) a doctor.
 * Right: a month calendar for picking the date (days with appointments are marked),
 *        and the schedule for the selected day (or all dates).
 */
public class AppointmentPanel extends JPanel {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");
    private static final Pattern DOCTOR_NAME = Pattern.compile("\\p{L}[\\p{L} .'\\-]*");
    private static final int MAX_DOCTOR_LENGTH = 100;
    private static final int LIST_ROW_HEIGHT = 32;
    private static final Color SELECTION_BG = new Color(0xDDEFEB);

    private final PatientDAO dao = new PatientDAO();
    private final Runnable onChanged;

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Appointment> allAppointments = new ArrayList<>();
    private Patient selectedPatient;

    // Booking form
    private RoundedTextField patientSearch;
    private final DefaultListModel<Patient> patientModel = new DefaultListModel<>();
    private JList<Patient> patientList;
    private CardLayout listCards;
    private JPanel listHolder;
    private JLabel emptyListLabel;
    private JLabel selectedLabel;
    private LinkButton clearPatientBtn;
    private JComboBox<String> doctorBox;
    private JLabel dateLabel;
    private JLabel formStatus;
    private boolean updatingList; // true while we change the list selection ourselves

    // Calendar + schedule
    private final CalendarPicker calendar = new CalendarPicker();
    private JLabel scheduleTitle;
    private JCheckBox showAll;
    private DefaultTableModel model;

    public AppointmentPanel(Runnable onChanged) {
        this.onChanged = onChanged;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel right = new JPanel(new BorderLayout(0, UITheme.GAP));
        right.setOpaque(false);
        right.add(buildCalendarCard(), BorderLayout.NORTH);
        right.add(buildScheduleCard(), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(UITheme.PAD, 0));
        body.setOpaque(false);
        body.add(buildBookingCard(), BorderLayout.WEST);
        body.add(right, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        calendar.addDateChangeListener(d -> onDateChanged());
        updateSelectedLabel();
        updateDateLabel();
        refresh();
    }

    /** Reloads patients, appointments and doctor suggestions from the database. */
    public void refresh() {
        loadPatients();
        loadAppointments();
    }

    // ------------------------------------------------------------------ layout

    private JPanel buildHeader() {
        JLabel title = new JLabel("Appointments");
        title.setFont(UITheme.display());
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Find a patient, pick a date on the calendar, and book \u2014 or review the schedule");
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
        return header;
    }

    private JPanel buildBookingCard() {
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        int row = 0;

        JLabel formTitle = new JLabel("Book Appointment");
        formTitle.setFont(UITheme.heading());
        formTitle.setForeground(UITheme.TEXT_PRIMARY);
        gc.gridy = row++;
        gc.insets = new Insets(0, 0, 12, 0);
        card.add(formTitle, gc);

        // --- Patient: search box, results list, current selection
        gc.gridy = row++;
        gc.insets = new Insets(0, 0, 4, 0);
        card.add(fieldLabel("Patient"), gc);

        patientSearch = new RoundedTextField(14);
        patientSearch.setPlaceholder("Search by name, ID or phone");
        patientSearch.setPreferredSize(new Dimension(200, 36));
        patientSearch.getDocument().addDocumentListener(onAnyChange(this::applyFilter));
        patientSearch.addActionListener(e -> pickFirstMatch());
        patientSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && !patientModel.isEmpty()) {
                    patientList.requestFocusInWindow();
                    if (patientList.getSelectedIndex() < 0) patientList.setSelectedIndex(0);
                }
            }
        });
        gc.gridy = row++;
        gc.insets = new Insets(0, 0, 0, 0);
        card.add(patientSearch, gc);

        gc.gridy = row++;
        gc.insets = new Insets(6, 0, 0, 0);
        card.add(buildPatientList(), gc);

        selectedLabel = new JLabel(" ");
        clearPatientBtn = new LinkButton("Clear", UITheme.TEXT_SECONDARY, UITheme.DANGER);
        clearPatientBtn.setFont(UITheme.small().deriveFont(Font.BOLD));
        clearPatientBtn.addActionListener(e -> {
            setSelectedPatient(null);
            patientSearch.requestFocusInWindow();
        });
        JPanel selectedRow = new JPanel(new BorderLayout());
        selectedRow.setOpaque(false);
        selectedRow.setPreferredSize(new Dimension(10, 26));
        selectedRow.add(selectedLabel, BorderLayout.CENTER);
        selectedRow.add(clearPatientBtn, BorderLayout.EAST);
        gc.gridy = row++;
        gc.insets = new Insets(4, 0, 0, 0);
        card.add(selectedRow, gc);

        // --- Doctor
        gc.gridy = row++;
        gc.insets = new Insets(10, 0, 4, 0);
        card.add(fieldLabel("Doctor"), gc);

        doctorBox = new JComboBox<>();
        doctorBox.setEditable(true);
        doctorBox.setFont(UITheme.body());
        doctorBox.setPreferredSize(new Dimension(200, 36));
        doctorBox.setToolTipText("Type a doctor's name, or pick one that already has appointments");
        gc.gridy = row++;
        gc.insets = new Insets(0, 0, 0, 0);
        card.add(doctorBox, gc);

        // --- Spacer pushes the summary + button to the bottom of the card
        gc.gridy = row++;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        card.add(Box.createVerticalGlue(), gc);
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;

        dateLabel = new JLabel(" ");
        dateLabel.setFont(UITheme.body());
        gc.gridy = row++;
        gc.insets = new Insets(14, 0, 0, 0);
        card.add(dateLabel, gc);

        formStatus = new JLabel(" ");
        formStatus.setFont(UITheme.small());
        formStatus.setVerticalAlignment(SwingConstants.TOP);
        formStatus.setPreferredSize(new Dimension(10, 34));
        gc.gridy = row++;
        gc.insets = new Insets(4, 0, 6, 0);
        card.add(formStatus, gc);

        RoundedButton bookBtn = new RoundedButton("Book Appointment", UITheme.ACCENT, UITheme.ACCENT_HOVER);
        bookBtn.addActionListener(e -> book());
        gc.gridy = row++;
        gc.insets = new Insets(0, 0, 0, 0);
        card.add(bookBtn, gc);

        // Fixed width; height follows the content (and stretches to match the right column).
        card.setPreferredSize(new Dimension(340, card.getPreferredSize().height));
        return card;
    }

    private JPanel buildPatientList() {
        patientList = new JList<>(patientModel);
        patientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientList.setFixedCellHeight(LIST_ROW_HEIGHT);
        patientList.setCellRenderer(new PatientCellRenderer());
        patientList.setBackground(Color.WHITE);
        patientList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updatingList) return;
            Patient p = patientList.getSelectedValue();
            if (p != null) setSelectedPatient(p);
        });
        patientList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // After picking a patient with the mouse, jump straight to the doctor field.
                if (patientList.locationToIndex(e.getPoint()) >= 0 && patientList.getSelectedValue() != null) focusDoctor();
            }
        });

        JScrollPane scroll = new JScrollPane(patientList);
        scroll.setBorder(null);

        emptyListLabel = new JLabel("", SwingConstants.CENTER);
        emptyListLabel.setFont(UITheme.body());
        emptyListLabel.setForeground(UITheme.TEXT_SECONDARY);

        listCards = new CardLayout();
        listHolder = new JPanel(listCards);
        listHolder.setBackground(Color.WHITE);
        listHolder.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        listHolder.setPreferredSize(new Dimension(10, 5 * LIST_ROW_HEIGHT + 2));
        listHolder.add(scroll, "list");
        listHolder.add(emptyListLabel, "empty");
        return listHolder;
    }

    private JPanel buildCalendarCard() {
        RoundedCard card = new RoundedCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        card.add(calendar, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildScheduleCard() {
        RoundedCard card = new RoundedCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        scheduleTitle = new JLabel(" ");
        scheduleTitle.setFont(UITheme.subheading());
        scheduleTitle.setForeground(UITheme.TEXT_PRIMARY);

        showAll = new JCheckBox("Show all dates");
        showAll.setOpaque(false);
        showAll.setFont(UITheme.body());
        showAll.setForeground(UITheme.TEXT_SECONDARY);
        showAll.setFocusPainted(false);
        showAll.addActionListener(e -> rebuildTable());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(scheduleTitle, BorderLayout.CENTER);
        top.add(showAll, BorderLayout.EAST);

        model = new DefaultTableModel(new Object[]{"Appt ID", "Patient ID", "Patient", "Doctor", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.body());
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.bodyBold());
        table.getTableHeader().setBackground(UITheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setShowVerticalLines(false);
        table.setPreferredScrollableViewportSize(new Dimension(300, 150));
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);

        card.add(top, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.bodyBold());
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    // ------------------------------------------------------------- patient list

    private void applyFilter() {
        String q = patientSearch.getText().trim().toLowerCase();
        updatingList = true;
        try {
            patientModel.clear();
            for (Patient p : allPatients) {
                if (!matches(p, q)) continue;
                // An exact ID match jumps to the top so "type ID, press Enter" works.
                if (!q.isEmpty() && String.valueOf(p.getId()).equals(q)) patientModel.add(0, p);
                else patientModel.addElement(p);
            }
            int idx = indexOfSelected();
            if (idx >= 0) {
                patientList.setSelectedIndex(idx);
                patientList.ensureIndexIsVisible(idx);
            }
        } finally {
            updatingList = false;
        }

        if (patientModel.isEmpty()) {
            emptyListLabel.setText(allPatients.isEmpty()
                    ? "No patients yet \u2014 add one first."
                    : "No patient matches \"" + patientSearch.getText().trim() + "\".");
            listCards.show(listHolder, "empty");
        } else {
            listCards.show(listHolder, "list");
        }
    }

    private static boolean matches(Patient p, String q) {
        if (q.isEmpty()) return true;
        if (p.getName() != null && p.getName().toLowerCase().contains(q)) return true;
        if (String.valueOf(p.getId()).startsWith(q)) return true;
        return p.getContact() != null && p.getContact().toLowerCase().contains(q);
    }

    private int indexOfSelected() {
        if (selectedPatient == null) return -1;
        for (int i = 0; i < patientModel.size(); i++) {
            if (patientModel.get(i).getId() == selectedPatient.getId()) return i;
        }
        return -1;
    }

    /** Enter in the search box: choose the top match (an exact ID match is always on top). */
    private void pickFirstMatch() {
        if (patientModel.isEmpty()) {
            showError("No patient matches that search.");
            return;
        }
        setSelectedPatient(patientModel.get(0));
        focusDoctor();
    }

    private void setSelectedPatient(Patient p) {
        selectedPatient = p;
        updatingList = true;
        try {
            if (p == null) {
                patientList.clearSelection();
            } else {
                int idx = indexOfSelected();
                if (idx >= 0) {
                    patientList.setSelectedIndex(idx);
                    patientList.ensureIndexIsVisible(idx);
                }
            }
        } finally {
            updatingList = false;
        }
        updateSelectedLabel();
        clearStatus();
    }

    private void updateSelectedLabel() {
        if (selectedPatient == null) {
            selectedLabel.setText("No patient selected");
            selectedLabel.setFont(UITheme.body());
            selectedLabel.setForeground(UITheme.TEXT_SECONDARY);
            clearPatientBtn.setVisible(false);
        } else {
            selectedLabel.setText("\u2713  " + selectedPatient.getName() + "  \u00B7  ID " + selectedPatient.getId());
            selectedLabel.setFont(UITheme.bodyBold());
            selectedLabel.setForeground(UITheme.ACCENT);
            clearPatientBtn.setVisible(true);
        }
    }

    private static String describe(Patient p) {
        StringBuilder sb = new StringBuilder("ID ").append(p.getId());
        if (p.getAge() > 0) sb.append(" \u00B7 ").append(p.getAge());
        if (p.getGender() != null && !p.getGender().isBlank()) sb.append(" \u00B7 ").append(p.getGender());
        return sb.toString();
    }

    // ----------------------------------------------------------------- booking

    private void book() {
        if (selectedPatient == null) {
            showError("Select a patient from the list first.");
            patientSearch.requestFocusInWindow();
            return;
        }
        String doctor = doctorName();
        if (doctor.isEmpty()) {
            showError("Enter the doctor's name.");
            focusDoctor();
            return;
        }
        if (doctor.length() > MAX_DOCTOR_LENGTH) {
            showError("Doctor name is too long (max " + MAX_DOCTOR_LENGTH + " characters).");
            focusDoctor();
            return;
        }
        if (!DOCTOR_NAME.matcher(doctor).matches()) {
            showError("Doctor name can only contain letters, spaces, . ' and -");
            focusDoctor();
            return;
        }
        LocalDate date = calendar.getSelectedDate();
        if (date.isBefore(LocalDate.now())) {
            showError("That date has passed \u2014 pick today or a later date.");
            return;
        }

        try {
            dao.bookAppointment(selectedPatient.getId(), doctor, date);
        } catch (HospitalException e) {
            showError(e.getMessage());
            return;
        }

        String patientName = selectedPatient.getName();
        setSelectedPatient(null);
        patientSearch.setText("");
        showSuccess("Booked " + patientName + " with " + doctor + " on " + date.format(DATE_FMT) + ".");
        loadAppointments();
        if (onChanged != null) onChanged.run();
        patientSearch.requestFocusInWindow(); // ready for the next patient; doctor and date are kept
    }

    private String doctorName() {
        Object item = doctorBox.getEditor().getItem();
        return item == null ? "" : item.toString().trim().replaceAll("\\s+", " ");
    }

    private void focusDoctor() {
        doctorBox.getEditor().getEditorComponent().requestFocusInWindow();
    }

    // -------------------------------------------------------------------- data

    private void loadPatients() {
        try {
            allPatients = dao.getAllPatients();
        } catch (HospitalException e) {
            allPatients = new ArrayList<>();
            showError(e.getMessage());
        }
        // Pick up edits to the selected patient, or drop the selection if they were deleted.
        if (selectedPatient != null) {
            Patient fresh = null;
            for (Patient p : allPatients) if (p.getId() == selectedPatient.getId()) fresh = p;
            selectedPatient = fresh;
            updateSelectedLabel();
        }
        applyFilter();
    }

    private void loadAppointments() {
        List<String> doctors = new ArrayList<>();
        try {
            allAppointments = dao.getAllAppointments();
            doctors = dao.getDoctors();
        } catch (HospitalException e) {
            allAppointments = new ArrayList<>();
            showError(e.getMessage());
        }

        Map<LocalDate, Integer> counts = new HashMap<>();
        for (Appointment a : allAppointments) counts.merge(a.getLocalDate(), 1, Integer::sum);
        calendar.setAppointmentCounts(counts);

        String typed = doctorName(); // keep whatever the user has already typed
        doctorBox.setModel(new DefaultComboBoxModel<>(doctors.toArray(new String[0])));
        doctorBox.setSelectedItem(typed);

        rebuildTable();
    }

    private void onDateChanged() {
        updateDateLabel();
        rebuildTable();
        clearStatus();
    }

    private void rebuildTable() {
        LocalDate day = calendar.getSelectedDate();
        boolean all = showAll.isSelected();
        model.setRowCount(0);
        int count = 0;
        for (Appointment a : allAppointments) {
            if (all || a.getLocalDate().equals(day)) {
                model.addRow(new Object[]{a.getAppointmentId(), a.getPatientId(), a.getPatientName(), a.getDoctor(), a.getDate()});
                count++;
            }
        }
        if (all) {
            scheduleTitle.setText("All appointments \u00B7 " + count);
        } else if (count == 0) {
            scheduleTitle.setText(day.format(DATE_FMT) + " \u00B7 no appointments");
        } else {
            scheduleTitle.setText(day.format(DATE_FMT) + " \u00B7 " + count + (count == 1 ? " appointment" : " appointments"));
        }
    }

    private void updateDateLabel() {
        LocalDate d = calendar.getSelectedDate();
        long diff = ChronoUnit.DAYS.between(LocalDate.now(), d);
        String relative;
        if (diff < 0) relative = "Past date";
        else if (diff == 0) relative = "Today";
        else if (diff == 1) relative = "Tomorrow";
        else relative = "In " + diff + " days";

        String relColor = hex(diff < 0 ? UITheme.DANGER : UITheme.ACCENT);
        dateLabel.setText("<html><font color='" + hex(UITheme.TEXT_SECONDARY) + "'>Date&nbsp;&nbsp;</font><b>"
                + d.format(DATE_FMT) + "</b>&nbsp;&nbsp;<font color='" + relColor + "'>" + relative + "</font></html>");
    }

    // ------------------------------------------------------------ status/helpers

    private void showError(String message) {
        formStatus.setForeground(UITheme.DANGER);
        formStatus.setText(statusHtml(message));
    }

    private void showSuccess(String message) {
        formStatus.setForeground(UITheme.ACCENT);
        formStatus.setText(statusHtml(message));
    }

    private void clearStatus() {
        formStatus.setText(" ");
    }

    private static String statusHtml(String message) {
        String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html>" + safe + "</html>"; // plain <html> wraps to the label's width
    }

    private static String hex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }

    private static DocumentListener onAnyChange(Runnable r) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { r.run(); }
            @Override public void removeUpdate(DocumentEvent e) { r.run(); }
            @Override public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    /** One row in the patient results list: name on the left, "ID / age / gender" on the right. */
    private static class PatientCellRenderer extends JPanel implements ListCellRenderer<Patient> {
        private final JLabel name = new JLabel();
        private final JLabel meta = new JLabel();

        PatientCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            name.setFont(UITheme.bodyBold());
            name.setForeground(UITheme.TEXT_PRIMARY);
            meta.setFont(UITheme.small());
            meta.setForeground(UITheme.TEXT_SECONDARY);
            add(name, BorderLayout.CENTER);
            add(meta, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Patient> list, Patient p, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            name.setText(p.getName());
            meta.setText(describe(p));
            setBackground(isSelected ? SELECTION_BG : Color.WHITE);
            return this;
        }
    }
}
