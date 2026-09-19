import javax.swing.*;

/**
 * Application entry point. Launches the Swing GUI, starting with the login screen.
 * (The old console menu has been fully replaced by the GUI — see LoginFrame / MainFrame.)
 */
public class HospitalManagement {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default look and feel if the system L&F isn't available.
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
