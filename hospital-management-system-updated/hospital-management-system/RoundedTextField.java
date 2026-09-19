import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A text field with rounded, bordered chrome matching the app theme.
 * Renders on a transparent parent so it looks like a proper input card.
 */
public class RoundedTextField extends JTextField {
    private String placeholder;

    public RoundedTextField(int columns) {
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

        if (placeholder != null && getText().isEmpty()) {
            Graphics2D hint = (Graphics2D) g.create();
            hint.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hint.setFont(getFont());
            hint.setColor(new Color(0x9AA9AE));
            FontMetrics fm = hint.getFontMetrics();
            Insets in = getInsets();
            hint.drawString(placeholder, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            hint.dispose();
        }
    }

    /** Grey hint text shown while the field is empty. */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
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
