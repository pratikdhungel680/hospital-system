import java.awt.*;

/**
 * Central design tokens for the app — a calm clinical palette (deep teal + soft neutrals)
 * instead of default Swing gray, applied consistently across every screen.
 */
public class UITheme {
    // Colors
    public static final Color PRIMARY = new Color(0x1B4B5A);       // deep teal
    public static final Color PRIMARY_DARK = new Color(0x123642);
    public static final Color ACCENT = new Color(0x2E8B7A);        // sea green accent (positive actions)
    public static final Color ACCENT_HOVER = new Color(0x267566);
    public static final Color DANGER = new Color(0xC94F4F);
    public static final Color DANGER_HOVER = new Color(0xB03E3E);
    public static final Color BG = new Color(0xF3F6F6);            // soft off-white
    public static final Color CARD_BG = Color.WHITE;
    public static final Color SIDEBAR_BG = new Color(0x123642);
    public static final Color SIDEBAR_HOVER = new Color(0x1B4B5A);
    public static final Color SIDEBAR_ACTIVE = new Color(0x2E8B7A);
    public static final Color TEXT_PRIMARY = new Color(0x22303A);
    public static final Color TEXT_SECONDARY = new Color(0x63757D);
    public static final Color BORDER = new Color(0xDCE3E3);
    public static final Color TEXT_ON_DARK = new Color(0xEAF2F1);

    // Fonts
    public static final String FONT_FAMILY = "Segoe UI";
    public static Font display() { return new Font(FONT_FAMILY, Font.BOLD, 26); }
    public static Font heading() { return new Font(FONT_FAMILY, Font.BOLD, 18); }
    public static Font subheading() { return new Font(FONT_FAMILY, Font.BOLD, 14); }
    public static Font body() { return new Font(FONT_FAMILY, Font.PLAIN, 13); }
    public static Font bodyBold() { return new Font(FONT_FAMILY, Font.BOLD, 13); }
    public static Font small() { return new Font(FONT_FAMILY, Font.PLAIN, 11); }
    public static Font nav() { return new Font(FONT_FAMILY, Font.PLAIN, 14); }

    // Spacing
    public static final int PAD = 20;
    public static final int GAP = 10;
    public static final int RADIUS = 10;
}
