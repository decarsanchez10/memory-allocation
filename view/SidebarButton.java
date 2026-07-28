package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * SidebarButton – draws the emoji icon and label text separately
 * so the icon always uses "Segoe UI Emoji" and renders correctly.
 */
public class SidebarButton extends JButton {
    private final String iconStr;
    private final String labelText;
    private boolean isActive = false;

    public SidebarButton(String text, String iconStr) {
        super();           // NO setText – we draw everything manually
        this.iconStr   = iconStr;
        this.labelText = text;

        setForeground(new Color(160, 195, 230));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(0, 44));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!isActive) setForeground(Color.WHITE);
                repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!isActive) setForeground(new Color(160, 195, 230));
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.isActive = active;
        setForeground(active ? Color.WHITE : new Color(160, 195, 230));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight();

        // ── Background ────────────────────────────────────────────────
        if (isActive) {
            // Translucent blue gradient pill
            g2.setPaint(new GradientPaint(
                0, 0, new Color(63, 101, 147, 95),
                w, 0, new Color(91, 134, 182, 45)
            ));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

            // Left accent strip
            g2.setPaint(new GradientPaint(
                0, 0, new Color(128, 190, 255),
                0, h, new Color(63, 130, 210)
            ));
            g2.fill(new RoundRectangle2D.Float(0, 5, 3, h - 10, 3, 3));

            // Inner top highlight
            g2.setColor(new Color(255, 255, 255, 12));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 10, 10));

        } else if (getModel().isRollover()) {
            g2.setColor(new Color(91, 134, 182, 22));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));
        }

        // ── Icon (pick best font for the glyph) ─────────────────────
        int padL = 16;
        Font iconFont = pickIconFont(iconStr, 15);
        g2.setFont(iconFont);
        FontMetrics iconFm = g2.getFontMetrics();
        int baseline = (h + iconFm.getAscent() - iconFm.getDescent()) / 2;
        g2.setColor(getForeground());
        g2.drawString(iconStr, padL, baseline);
        int iconW = iconFm.stringWidth(iconStr);

        // ── Label text ────────────────────────────────────────────────
        Font textFont = isActive
            ? new Font("Segoe UI", Font.BOLD,  13)
            : new Font("Segoe UI", Font.PLAIN, 13);
        g2.setFont(textFont);
        FontMetrics textFm = g2.getFontMetrics();
        int textBaseline = (h + textFm.getAscent() - textFm.getDescent()) / 2;
        g2.drawString(labelText, padL + iconW + 9, textBaseline);

        g2.dispose();
    }

    /** Picks the first font that can display the glyph. */
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
}
