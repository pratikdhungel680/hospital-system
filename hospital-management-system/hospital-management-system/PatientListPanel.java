import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Browsable, searchable, editable table of all patients. */
public class PatientListPanel extends JPanel {
    private final PatientDAO dao = new PatientDAO();
    private DefaultTableModel model;
    private JTable table;
    private RoundedTextField searchField;
    private JLabel statusLabel;

    public PatientListPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        JLabel title = new JLabel("Patients");
        title.setFont(UITheme.display());
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("View, search, update, or remove patient records");
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

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        searchField = new RoundedTextField(20);
        searchField.setPreferredSize(new Dimension(240, 38));
        searchField.addActionListener(e -> doSearch());

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        RoundedButton searchBtn = new RoundedButton("Search", UITheme.PRIMARY, UITheme.PRIMARY_DARK);
        searchBtn.addActionListener(e -> doSearch());
        RoundedButton clearBtn = new RoundedButton("Clear", UITheme.TEXT_SECONDARY, UITheme.TEXT_PRIMARY);
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchRow.add(searchField);
        searchRow.add(searchBtn);
        searchRow.add(clearBtn);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        RoundedButton editBtn = new RoundedButton("Edit Selected", UITheme.ACCENT, UITheme.ACCENT_HOVER);
        editBtn.addActionListener(e -> editSelected());
        RoundedButton deleteBtn = new RoundedButton("Delete Selected", UITheme.DANGER, UITheme.DANGER_HOVER);
        deleteBtn.addActionListener(e -> deleteSelected());
        actionRow.add(editBtn);
        actionRow.add(deleteBtn);

        toolbar.add(searchRow, BorderLayout.WEST);
        toolbar.add(actionRow, BorderLayout.EAST);

        model = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "Gender", "Contact", "Diagnosis"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setFont(UITheme.body());
        table.setRowHeight(30);
        table.getTableHeader().setFont(UITheme.bodyBold());
        table.getTableHeader().setBackground(UITheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0xDDEFEB));
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setShowVerticalLines(false);

        RoundedCard tableCard = new RoundedCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        tableCard.add(sp, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.small());
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(tableCard, BorderLayout.CENTER);
        centerWrap.add(statusLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(centerWrap, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            List<Patient> patients = dao.getAllPatients();
            populate(patients);
            statusLabel.setForeground(UITheme.TEXT_SECONDARY);
            statusLabel.setText(patients.size() + " patient(s) on record.");
        } catch (HospitalException e) {
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText(e.getMessage());
        }
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { refresh(); return; }
        try {
            List<Patient> results;
            if (q.matches("\\d+")) {
                Patient p = dao.searchPatient(Integer.parseInt(q));
                results = List.of(p);
            } else {
                results = dao.searchByName(q);
            }
            populate(results);
            statusLabel.setForeground(UITheme.TEXT_SECONDARY);
            statusLabel.setText(results.size() + " result(s) found.");
        } catch (HospitalException e) {
            model.setRowCount(0);
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText(e.getMessage());
        }
    }

    private void populate(List<Patient> patients) {
        model.setRowCount(0);
        for (Patient p : patients) {
            model.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(), p.getContact(), p.getDisease()});
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText("Select a patient row first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete patient #" + id + " and all their appointments? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            dao.deletePatient(id);
            statusLabel.setForeground(UITheme.ACCENT);
            statusLabel.setText("Patient #" + id + " deleted.");
            refresh();
        } catch (HospitalException e) {
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText(e.getMessage());
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText("Select a patient row first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            Patient p = dao.searchPatient(id);
            EditPatientDialog dialog = new EditPatientDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), p, updated -> {
                        try {
                            dao.updatePatient(updated);
                            statusLabel.setForeground(UITheme.ACCENT);
                            statusLabel.setText("Patient #" + updated.getId() + " updated.");
                            refresh();
                        } catch (HospitalException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
            dialog.setVisible(true);
        } catch (HospitalException e) {
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText(e.getMessage());
        }
    }
}
