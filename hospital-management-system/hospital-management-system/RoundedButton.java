import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A flat, rounded button with a hover state — used instead of the default
 * Swing button chrome throughout the app.
 */
public class RoundedButton extends JButton {
    private Color baseColor;
    private Color hoverColor;
    private boolean hovering = false;

    public RoundedButton(String text, Color baseColor, Color hoverColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        setFont(UITheme.bodyBold());
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? (hovering ? hoverColor : baseColor) : baseColor.darker());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS));
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 100), Math.max(d.height, 38));
    }
}
