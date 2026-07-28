package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * RoundedButton with linear gradient fill, icon support, and smooth
 * hover/press colour transitions via repaint().
 */
public class RoundedButton extends JButton {
    private final Color normalColor;
    private final Color hoverColor;
    private final Color pressedColor;

    public RoundedButton(String text, String iconStr, Color bg) {
        super();
        this.normalColor  = bg;
        this.hoverColor   = brighten(bg, 30);
        this.pressedColor = darken(bg, 30);

        String label = (iconStr != null && !iconStr.isEmpty()) ? iconStr + "  " + text : text;
        setText(label);

        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Repaint on hover so gradient updates
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

        Color top, bot;
        if (getModel().isPressed()) {
            top = pressedColor; bot = darken(pressedColor, 20);
        } else if (getModel().isRollover()) {
            top = hoverColor;   bot = darken(hoverColor,   20);
        } else {
            top = normalColor;  bot = darken(normalColor,  20);
        }

        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Top highlight
        g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,50), 0, h/2, new Color(255,255,255,0)));
        g2.fill(new RoundRectangle2D.Float(1, 1, w-2, h/2, arc, arc));

        // Border
        g2.setColor(new Color(255, 255, 255, 25));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w-1, h-1, arc, arc));

        super.paintComponent(g);
        g2.dispose();
    }

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
