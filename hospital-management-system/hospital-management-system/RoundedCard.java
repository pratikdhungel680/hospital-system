import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** A white rounded card container used to group content on every panel. */
public class RoundedCard extends JPanel {
    public RoundedCard() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fill(new RoundRectangle2D.Float(1, 2, getWidth() - 2, getHeight() - 2, UITheme.RADIUS, UITheme.RADIUS));
        g2.setColor(UITheme.CARD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 3, UITheme.RADIUS, UITheme.RADIUS));
        g2.setColor(UITheme.BORDER);
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 3, getHeight() - 4, UITheme.RADIUS, UITheme.RADIUS));
        g2.dispose();
        super.paintComponent(g);
    }
}
