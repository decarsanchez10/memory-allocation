package backup;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private boolean drawShadow;

    public RoundedPanel(int radius, Color bgColor, boolean drawShadow) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.drawShadow = drawShadow;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int width = getWidth();
        int height = getHeight();

        // Draw shadow
        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Float(2, 2, width - 4, height - 4, cornerRadius, cornerRadius));
        }

        // Draw background
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, width - 2, height - 2, cornerRadius, cornerRadius));

        g2.dispose();
        super.paintComponent(g);
    }

    public void setBackgroundColor(Color bg) {
        this.backgroundColor = bg;
        repaint();
    }
}
