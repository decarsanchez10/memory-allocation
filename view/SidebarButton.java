package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class SidebarButton extends JButton {
    private boolean isActive = false;
    
    public SidebarButton(String text, String iconStr) {
        super(iconStr + "  " + text);
        setFont(ModernUI.FONT_LABEL.deriveFont(Font.BOLD, 14f));
        setForeground(ModernUI.TEXT_SECONDARY);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActive) setForeground(ModernUI.TEXT_PRIMARY);
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActive) setForeground(ModernUI.TEXT_SECONDARY);
                repaint();
            }
        });
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        if (active) {
            setForeground(ModernUI.TEXT_PRIMARY);
        } else {
            setForeground(ModernUI.TEXT_SECONDARY);
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);
        
        if (isActive) {
            g2.setColor(ModernUI.BG_PRIMARY);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            // Indicator
            g2.setColor(ModernUI.HIGHLIGHT);
            g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
        }
        
        super.paintComponent(g);
        g2.dispose();
    }
}
