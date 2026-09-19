import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Landing screen: quick counts of patients and upcoming appointments.
 */
public class DashboardPanel extends JPanel {
    private final PatientDAO patientDAO = new PatientDAO();
    private JLabel patientCountValue;
    private JLabel appointmentCountValue;
    private JPanel statsRow;

    public DashboardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Dashboard");
        title.setFont(UITheme.display());
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("An overview of current patient activity");
        sub.setFont(UITheme.body());
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 20, 0));

        header.add(title);
        header.add(sub);

        statsRow = new JPanel(new GridLayout(1, 2, UITheme.GAP, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(600, 130));

        patientCountValue = new JLabel("0");
        appointmentCountValue = new JLabel("0");

        statsRow.add(statCard("Total Patients", patientCountValue, UITheme.PRIMARY));
        statsRow.add(statCard("Booked Appointments", appointmentCountValue, UITheme.ACCENT));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(header);
        top.add(statsRow);

        add(top, BorderLayout.NORTH);

        refresh();
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new RoundedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setMaximumSize(new Dimension(36, 4));
        stripe.setPreferredSize(new Dimension(36, 4));
        stripe.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 34));
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));

        JLabel captionLabel = new JLabel(label);
        captionLabel.setFont(UITheme.body());
        captionLabel.setForeground(UITheme.TEXT_SECONDARY);
        captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(stripe);
        card.add(valueLabel);
        card.add(captionLabel);
        return card;
    }

    public void refresh() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            patientCountValue.setText(String.valueOf(patients.size()));
        } catch (HospitalException e) {
            patientCountValue.setText("—");
        }
        try {
            List<Appointment> appts = patientDAO.getAllAppointments();
            appointmentCountValue.setText(String.valueOf(appts.size()));
        } catch (HospitalException e) {
            appointmentCountValue.setText("—");
        }
    }
}
