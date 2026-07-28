package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D;

/**
 * RoundedButton – draws vector-shape icons (play, X, star, etc.)
 * instead of relying on font glyphs. Completely font-independent icons.
 */
public class RoundedButton extends JButton {
    private final String  labelText;
    private final String  iconKey;       // semantic key: "play", "x", "star", etc.
    private final Color   normalColor;
    private final Color   hoverColor;
    private final Color   pressedColor;

    /**
     * @param text    Button label text
     * @param iconKey Icon identifier – use semantic names: "play", "x", "star", "set"
     *                or any string. Unrecognised keys are silently skipped (no icon).
     * @param bg      Background colour
     */
    public RoundedButton(String text, String iconKey, Color bg) {
        super();
        this.labelText    = text;
        this.iconKey      = (iconKey != null) ? iconKey.toLowerCase().trim() : "";
        this.normalColor  = bg;
        this.hoverColor   = brighten(bg, 30);
        this.pressedColor = darken(bg, 30);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { repaint(); }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        return new Dimension(100, 42); // default height for action buttons
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), arc = ModernUI.RADIUS_INPUT;

        // ── Colour ────────────────────────────────────────────────────
        Color top, bot;
        if (getModel().isPressed()) {
            top = pressedColor; bot = darken(pressedColor, 20);
        } else if (getModel().isRollover()) {
            top = hoverColor;   bot = darken(hoverColor,   20);
        } else {
            top = normalColor;  bot = darken(normalColor,  20);
        }

        // ── Background ────────────────────────────────────────────────
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Top sheen
        g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,50),
                                      0, h/2, new Color(255,255,255,0)));
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h / 2f, arc, arc));

        // Border
        g2.setColor(new Color(255, 255, 255, 25));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));

        // ── Measure text ──────────────────────────────────────────────
        Font textFont = new Font("Segoe UI", Font.BOLD, 13);
        g2.setFont(textFont);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(labelText);

        boolean hasIcon = !iconKey.isEmpty() && isKnownIcon(iconKey);
        int iconSize = 12;
        int gap      = hasIcon ? 8 : 0;
        int iconArea = hasIcon ? iconSize : 0;
        int totalW   = iconArea + gap + textW;
        int startX   = (w - totalW) / 2;

        // ── Draw icon shape ───────────────────────────────────────────
        if (hasIcon) {
            g2.setColor(Color.WHITE);
            int iy = (h - iconSize) / 2;
            drawIconShape(g2, iconKey, startX, iy, iconSize);
        }

        // ── Draw text ─────────────────────────────────────────────────
        g2.setColor(Color.WHITE);
        g2.setFont(textFont);
        int textBaseline = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(labelText, startX + iconArea + gap, textBaseline);

        g2.dispose();
    }

    // ── Vector icon drawing ───────────────────────────────────────────────

    private static boolean isKnownIcon(String key) {
        switch (key) {
            case "play": case "x": case "star": case "set": return true;
            default: return false;
        }
    }

    private static void drawIconShape(Graphics2D g2, String key, int x, int y, int s) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (key) {
            case "play": drawPlay(g2, x, y, s); break;
            case "x":    drawX(g2, x, y, s);    break;
            case "star": drawStar(g2, x, y, s);  break;
            case "set":  drawSet(g2, x, y, s);   break;
        }
    }

    /** Filled play triangle ▶ */
    private static void drawPlay(Graphics2D g2, int x, int y, int s) {
        int[] px = {x + 2, x + s, x + 2};
        int[] py = {y, y + s / 2, y + s};
        g2.fillPolygon(px, py, 3);
    }

    /** X mark ✕ */
    private static void drawX(Graphics2D g2, int x, int y, int s) {
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 2, y + 2, x + s - 2, y + s - 2);
        g2.drawLine(x + s - 2, y + 2, x + 2, y + s - 2);
    }

    /** Five-pointed star ★ */
    private static void drawStar(Graphics2D g2, int x, int y, int s) {
        double cx = x + s / 2.0, cy = y + s / 2.0;
        double outer = s / 2.0, inner = s / 5.0;
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? outer : inner;
            double angle = Math.PI / 2 + i * Math.PI / 5;
            double px = cx + r * Math.cos(angle);
            double py = cy - r * Math.sin(angle);
            if (i == 0) star.moveTo(px, py);
            else        star.lineTo(px, py);
        }
        star.closePath();
        g2.fill(star);
    }

    /** Small gear/cog for "set" buttons */
    private static void drawSet(Graphics2D g2, int x, int y, int s) {
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = x + s/2, cy = y + s/2;
        int r = s / 3;
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        // tick marks
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            int x1 = (int)(cx + r * Math.cos(angle));
            int y1 = (int)(cy - r * Math.sin(angle));
            int x2 = (int)(cx + (r + 3) * Math.cos(angle));
            int y2 = (int)(cy - (r + 3) * Math.sin(angle));
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    // ── Colour helpers ────────────────────────────────────────────────
    private static Color brighten(Color c, int amt) {
        return new Color(Math.min(255, c.getRed()   + amt),
                         Math.min(255, c.getGreen() + amt),
                         Math.min(255, c.getBlue()  + amt));
    }
    private static Color darken(Color c, int amt) {
        return new Color(Math.max(0, c.getRed()   - amt),
                         Math.max(0, c.getGreen() - amt),
                         Math.max(0, c.getBlue()  - amt));
    }
}
