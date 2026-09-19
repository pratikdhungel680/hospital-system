import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A flat, text-only button (no chrome, no fill) that changes color on hover.
 * Used for inline actions like "Clear" and for the calendar's month/year arrows.
 */
public class LinkButton extends JButton {
    public LinkButton(String text, Color normal, Color hover) {
        super(text);
        setFont(UITheme.bodyBold());
        setForeground(normal);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { setForeground(hover); }
            @Override public void mouseExited(MouseEvent e) { setForeground(normal); }
        });
    }
}
