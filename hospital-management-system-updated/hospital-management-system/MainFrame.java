import javax.swing.*;
import java.awt.*;

/**
 * Main application shell: dark sidebar navigation on the left,
 * a CardLayout content area on the right holding each feature panel.
 */
public class MainFrame extends JFrame {
    private final User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel();

    private PatientListPanel patientListPanel;
    private AppointmentPanel appointmentPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Meridian Care — Hospital Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        showPanel("dashboard");
    }

    private JPanel buildContent() {
        content.setLayout(cardLayout);
        content.setBackground(UITheme.BG);

        DashboardPanel dashboard = new DashboardPanel();
        AddPatientPanel addPatient = new AddPatientPanel(() -> {
            if (patientListPanel != null) patientListPanel.refresh();
            dashboard.refresh();
        });
        patientListPanel = new PatientListPanel();
        appointmentPanel = new AppointmentPanel(dashboard::refresh);

        content.add(wrapScrollable(dashboard), "dashboard");
        content.add(wrapScrollable(addPatient), "add");
        content.add(patientListPanel, "list");
        content.add(wrapScrollable(appointmentPanel), "appointments");

        return content;
    }

    private JScrollPane wrapScrollable(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setBackground(UITheme.BG);
        sp.getViewport().setBackground(UITheme.BG);
        return sp;
    }

    private void showPanel(String name) {
        cardLayout.show(content, name);
        if (name.equals("list") && patientListPanel != null) patientListPanel.refresh();
        // Re-load so newly added/edited/deleted patients show up in the booking form.
        if (name.equals("appointments") && appointmentPanel != null) appointmentPanel.refresh();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 20, 0));

        JLabel brand = new JLabel("  Meridian Care");
        brand.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 18));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 20, 4, 0));

        JLabel role = new JLabel("  " + currentUser.getRole() + " · " + currentUser.getUsername());
        role.setFont(UITheme.small());
        role.setForeground(new Color(255, 255, 255, 140));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        role.setBorder(BorderFactory.createEmptyBorder(0, 20, 26, 0));

        sidebar.add(brand);
        sidebar.add(role);

        sidebar.add(navButton("Dashboard", () -> showPanel("dashboard")));
        sidebar.add(navButton("Add Patient", () -> showPanel("add")));
        sidebar.add(navButton("Patients", () -> showPanel("list")));
        sidebar.add(navButton("Appointments", () -> showPanel("appointments")));

        sidebar.add(Box.createVerticalGlue());

        JButton logout = navButton("Log Out", this::logout);
        logout.setForeground(new Color(255, 220, 220));
        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(8));

        return sidebar;
    }

    private JButton navButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(UITheme.nav());
        b.setForeground(UITheme.TEXT_ON_DARK);
        b.setBackground(UITheme.SIDEBAR_BG);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(220, 44));
        b.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(UITheme.SIDEBAR_HOVER); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(UITheme.SIDEBAR_BG); }
        });
        b.addActionListener(e -> action.run());
        return b;
    }

    private void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
