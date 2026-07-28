import java.awt.*;

public class ModernUI {
    public static final Color BACKGROUND = Color.decode("#000F22");
    public static final Color CARD_BACKGROUND = Color.decode("#1B3554");
    public static final Color SECONDARY = Color.decode("#3F6593");
    public static final Color ACCENT = Color.decode("#5B86B6");
    public static final Color LIGHT_ACCENT = Color.decode("#80AAD3");
    public static final Color HIGHLIGHTS = Color.decode("#C0E6FD");
    
    public static final Color TEXT_PRIMARY = Color.WHITE;
    public static final Color TEXT_SECONDARY = new Color(200, 200, 220);
    
    public static final Color COLOR_FREE = new Color(46, 64, 83);
    public static final Color COLOR_INTERNAL_FRAG = new Color(241, 196, 15, 180);
    public static final Color COLOR_EXTERNAL_FRAG = new Color(149, 165, 166, 100);

    public static final Font FONT_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_MEDIUM = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    
    public static final int CORNER_RADIUS = 16;
    
    public static void applyQualityRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
