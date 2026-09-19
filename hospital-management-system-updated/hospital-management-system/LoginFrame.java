import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;

/**
 * Login / registration screen. Split layout: a branded teal panel on the left
 * with a simple cross-motif mark, and the credential form on the right.
 */
public class LoginFrame extends JFrame {
    private final UserDAO userDAO = new UserDAO();

    private RoundedTextField usernameField;
    private RoundedPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Hospital Management System — Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 560);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);

        // Fail fast with a clear message if the DB isn't reachable.
        SwingUtilities.invokeLater(() -> {
            if (!DBConnection.testConnection()) {
                JOptionPane.showMessageDialog(this,
                        "Could not connect to the database.\n\n" +
                        "Check that:\n" +
                        "  • MySQL is running\n" +
                        "  • hospital_db.sql has been imported\n" +
                        "  • DBConnection.java has the correct credentials\n" +
                        "  • MySQL Connector/J is on the classpath",
                        "Database Connection Error", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY, 0, getHeight(), UITheme.PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative cross / plus motif (medical mark), drawn not imported.
                g2.setColor(new Color(255, 255, 255, 25));
                int cx = getWidth() / 2, cy = 160, arm = 46, thick = 16;
                g2.fillRoundRect(cx - thick / 2, cy - arm, thick, arm * 2, 6, 6);
                g2.fillRoundRect(cx - arm, cy - thick / 2, arm * 2, thick, 6, 6);

                g2.setColor(new Color(255, 255, 255, 12));
                g2.fill(new Ellipse2D.Float(-60, getHeight() - 140, 260, 260));
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Meridian Care");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Hospital Management System");
        subtitle.setFont(UITheme.body());
        subtitle.setForeground(UITheme.TEXT_ON_DARK);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><div style='text-align:center; width:220px;'>" +
                "Patient records, appointments, and care coordination — in one place.</div></html>");
        tagline.setFont(UITheme.small());
        tagline.setForeground(new Color(255, 255, 255, 170));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setBorder(BorderFactory.createEmptyBorder(210, 0, 0, 0));

        inner.add(title);
        inner.add(Box.createVerticalStrut(6));
        inner.add(subtitle);
        inner.add(tagline);

        panel.add(inner);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(320, 340));
        form.setMaximumSize(new Dimension(320, 400));

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(UITheme.display());
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue");
        sub.setFont(UITheme.body());
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 22, 0));

        JLabel userLabel = fieldLabel("Username");
        usernameField = new RoundedTextField(20);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(320, 40));

        JLabel passLabel = fieldLabel("Password");
        passwordField = new RoundedPasswordField(20);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(320, 40));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.small());
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        RoundedButton loginBtn = new RoundedButton("Sign In", UITheme.PRIMARY, UITheme.PRIMARY_DARK);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(320, 44));
        loginBtn.addActionListener(this::handleLogin);

        JButton registerLink = new JButton("New here? Create an account");
        styleLinkButton(registerLink);
        registerLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerLink.addActionListener(e -> showRegisterDialog());

        passwordField.addActionListener(this::handleLogin);

        form.add(heading);
        form.add(sub);
        form.add(userLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(passwordField);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(registerLink);

        outer.add(form);
        return outer;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.bodyBold());
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleLinkButton(JButton b) {
        b.setFont(UITheme.body());
        b.setForeground(UITheme.PRIMARY);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMargin(new Insets(0, 0, 0, 0));
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            statusLabel.setForeground(UITheme.ACCENT);
            statusLabel.setText("Signed in.");
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(user).setVisible(true));
        } catch (HospitalException ex) {
            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText(ex.getMessage());
        }
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Create Account", true);
        dialog.setSize(360, 320);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG);

        JPanel panel = new JPanel();
        panel.setBackground(UITheme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Create a staff account");
        title.setFont(UITheme.heading());
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        RoundedTextField newUser = new RoundedTextField(16);
        RoundedPasswordField newPass = new RoundedPasswordField(16);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        roleBox.setFont(UITheme.body());
        roleBox.setMaximumSize(new Dimension(300, 36));

        JLabel status = new JLabel(" ");
        status.setFont(UITheme.small());
        status.setForeground(UITheme.DANGER);
        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (JComponent c : new JComponent[]{newUser, newPass, roleBox}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(300, 40));
        }

        RoundedButton createBtn = new RoundedButton("Create Account", UITheme.ACCENT, UITheme.ACCENT_HOVER);
        createBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(300, 42));
        createBtn.addActionListener(e -> {
            try {
                userDAO.registerUser(newUser.getText(), new String(newPass.getPassword()), (String) roleBox.getSelectedItem());
                JOptionPane.showMessageDialog(dialog, "Account created. You can now sign in.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (HospitalException ex) {
                status.setText(ex.getMessage());
            }
        });

        panel.add(title);
        panel.add(fieldLabel("Username"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(newUser);
        panel.add(Box.createVerticalStrut(12));
        panel.add(fieldLabel("Password"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(newPass);
        panel.add(Box.createVerticalStrut(12));
        panel.add(fieldLabel("Role"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(roleBox);
        panel.add(status);
        panel.add(Box.createVerticalStrut(10));
        panel.add(createBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
