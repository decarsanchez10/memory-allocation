package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ModernUI {
    public static final Color BG_PRIMARY = Color.decode("#000F22");
    public static final Color BG_SECONDARY = Color.decode("#132847");
    public static final Color CARD_BG = Color.decode("#1B3554");
    
    public static final Color ACCENT_BLUE = Color.decode("#3F6593");
    public static final Color LIGHT_BLUE = Color.decode("#5B86B6");
    public static final Color HIGHLIGHT = Color.decode("#80AAD3");
    
    public static final Color TEXT_PRIMARY = Color.decode("#FFFFFF");
    public static final Color TEXT_SECONDARY = Color.decode("#C0E6FD");
    
    public static final Color SUCCESS = Color.decode("#22C55E");
    public static final Color WARNING = Color.decode("#F59E0B");
    public static final Color DANGER = Color.decode("#EF4444");
    public static final Color PURPLE = Color.decode("#8B5CF6");
    public static final Color ORANGE = Color.decode("#F97316");
    public static final Color GRAY = Color.decode("#4B5563");

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 14);
    
    public static final int MARGIN_OUTER = 20;
    public static final int PADDING_CARD = 20;
    public static final int GAP_CARDS = 16;
    public static final int RADIUS_ROUND = 18;
    public static final int RADIUS_INPUT = 12;
    
    public static void applyQualityRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
