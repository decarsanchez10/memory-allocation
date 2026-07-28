package backup;

import javax.swing.*;
import java.awt.*;

public class DashboardCard extends RoundedPanel {
    private JLabel titleLabel;
    private JLabel valueLabel;
    
    public DashboardCard(String title, String initialValue) {
        super(ModernUI.CORNER_RADIUS, ModernUI.CARD_BACKGROUND);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        titleLabel = new JLabel(title);
        titleLabel.setFont(ModernUI.FONT_REGULAR);
        titleLabel.setForeground(ModernUI.TEXT_SECONDARY);
        
        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(ModernUI.FONT_LARGE);
        valueLabel.setForeground(ModernUI.HIGHLIGHTS);
        
        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }
    
    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
