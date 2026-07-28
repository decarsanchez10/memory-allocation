package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedTextField extends JTextField {
    public RoundedTextField(String text) {
        super(text);
        setOpaque(false);
        setForeground(ModernUI.TEXT_PRIMARY);
        setCaretColor(ModernUI.TEXT_PRIMARY);
        setFont(ModernUI.FONT_LABEL);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);
        g2.setColor(ModernUI.BG_PRIMARY);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ModernUI.RADIUS_INPUT, ModernUI.RADIUS_INPUT));
        super.paintComponent(g);
        g2.dispose();
    }
}
