package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * RoundedButton – draws the emoji/symbol icon with "Segoe UI Emoji"
 * and the label text with "Segoe UI Bold" so icons always render correctly.
 */
public class RoundedButton extends JButton {
    private final String  labelText;
    private final String  iconStr;
    private final Color   normalColor;
    private final Color   hoverColor;
    private final Color   pressedColor;

    public RoundedButton(String text, String iconStr, Color bg) {
        super();          // NO setText – we paint manually
        this.labelText    = text;
        this.iconStr      = (iconStr != null) ? iconStr : "";
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
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), arc = ModernUI.RADIUS_INPUT;

        // ── Choose colour based on state ──────────────────────────────
        Color top, bot;
        if (getModel().isPressed()) {
            top = pressedColor; bot = darken(pressedColor, 20);
        } else if (getModel().isRollover()) {
            top = hoverColor;   bot = darken(hoverColor,   20);
        } else {
            top = normalColor;  bot = darken(normalColor,  20);
        }

        // ── Background gradient ───────────────────────────────────────
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Top sheen
        g2.setPaint(new GradientPaint(0, 0,   new Color(255, 255, 255, 50),
                                      0, h/2, new Color(255, 255, 255, 0)));
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h / 2f, arc, arc));

        // Subtle border
        g2.setColor(new Color(255, 255, 255, 25));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));

        // ── Pick the right font for the icon glyph ────────────────────
        Font iconFont = pickIconFont(iconStr, 13);
        Font textFont = new Font("Segoe UI", Font.BOLD, 13);

        g2.setFont(iconFont);
        FontMetrics ifm = g2.getFontMetrics();
        int iconW = iconStr.isEmpty() ? 0 : ifm.stringWidth(iconStr);

        g2.setFont(textFont);
        FontMetrics tfm = g2.getFontMetrics();
        int textW = tfm.stringWidth(labelText);

        int gap    = iconStr.isEmpty() ? 0 : 6;
        int totalW = iconW + gap + textW;
        int startX = (w - totalW) / 2;

        g2.setColor(Color.WHITE);

        // ── Draw icon ─────────────────────────────────────────────────
        if (!iconStr.isEmpty()) {
            g2.setFont(iconFont);
            int baseline = (h + ifm.getAscent() - ifm.getDescent()) / 2;
            g2.drawString(iconStr, startX, baseline);
        }

        // ── Draw label ────────────────────────────────────────────────
        g2.setFont(textFont);
        int textBaseline = (h + tfm.getAscent() - tfm.getDescent()) / 2;
        g2.drawString(labelText, startX + iconW + gap, textBaseline);

        g2.dispose();
    }

    /**
     * Picks the first font that can display the given string.
     * Priority: Segoe UI Symbol → Segoe UI Emoji → Segoe UI.
     */
    private static Font pickIconFont(String s, int size) {
        if (s == null || s.isEmpty()) return new Font("Segoe UI", Font.PLAIN, size);
        String[] candidates = {"Segoe UI Symbol", "Segoe UI Emoji", "Segoe UI"};
        int cp = s.codePointAt(0);
        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, size);
            if (f.canDisplay(cp)) return f;
        }
        return new Font("Segoe UI", Font.PLAIN, size);
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
