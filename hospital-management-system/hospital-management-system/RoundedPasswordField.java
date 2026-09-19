import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPasswordField extends JPasswordField {
    public RoundedPasswordField(int columns) {
        super(columns);
        setOpaque(false);
        setFont(UITheme.body());
        setForeground(UITheme.TEXT_PRIMARY);
        setBorder(new EmptyBorder(9, 12, 9, 12));
        setCaretColor(UITheme.PRIMARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS));
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isFocusOwner() ? UITheme.PRIMARY : UITheme.BORDER);
        g2.setStroke(new BasicStroke(isFocusOwner() ? 1.6f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, UITheme.RADIUS, UITheme.RADIUS));
        g2.dispose();
    }
}
