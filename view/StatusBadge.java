package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatusBadge extends JPanel {
    private String text;
    private Color color;

    public StatusBadge(String text, Color color) {
        this.text = text;
        this.color = color;
        setOpaque(false);
        setPreferredSize(new Dimension(100, 24));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);
        
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        
        g2.setColor(color);
        g2.setFont(ModernUI.FONT_LABEL.deriveFont(Font.BOLD));
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        
        // Draw icon dot
        int dotSize = 8;
        int dotX = x - 12;
        int dotY = (getHeight() - dotSize) / 2;
        g2.fillOval(dotX, dotY, dotSize, dotSize);
        
        g2.drawString(text, x, y);
        g2.dispose();
    }
}
