package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * RoundedPanel with optional gradient fill and layered shadow.
 */
public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private boolean drawShadow;
    private boolean useGradient;

    public RoundedPanel(int radius, Color bgColor, boolean drawShadow) {
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.drawShadow = drawShadow;
        this.useGradient = false;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), r = cornerRadius;

        // Layered drop shadow (two passes for softness)
        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fill(new RoundRectangle2D.Float(3, 6, w - 6, h - 6, r, r));
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fill(new RoundRectangle2D.Float(1, 3, w - 4, h - 4, r, r));
        }

        // Fill
        if (useGradient) {
            Color lighter = new Color(
                Math.min(255, backgroundColor.getRed() + 15),
                Math.min(255, backgroundColor.getGreen() + 15),
                Math.min(255, backgroundColor.getBlue() + 15)
            );
            g2.setPaint(new GradientPaint(0, 0, backgroundColor, 0, h, lighter));
        } else {
            g2.setColor(backgroundColor);
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 4, r, r));

        // Subtle 1-px inner top highlight
        g2.setColor(new Color(255, 255, 255, 12));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(1, 1, w - 4, h - 6, r - 1, r - 1));

        g2.dispose();
        super.paintComponent(g);
    }

    public void setBackgroundColor(Color bg) {
        this.backgroundColor = bg;
        repaint();
    }

    public void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
        repaint();
    }
}
