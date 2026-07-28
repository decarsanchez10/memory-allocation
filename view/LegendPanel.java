package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LegendPanel — horizontal colour-key for the memory visualizer.
 * Each item shows a rounded gradient pill beside a label.
 */
public class LegendPanel extends JPanel {

    private static final Object[][] ITEMS = {
        { "Allocated",   new Color(91, 134, 182),  new Color(40, 90, 140)   },
        { "Free",        new Color(52, 211, 153),   new Color(22, 163, 74)   },
        { "Int. Frag",   new Color(251, 191, 36),   new Color(180, 120, 0)   },
        { "Empty",       new Color(80, 100, 120),   new Color(50, 65, 80)    },
    };

    public LegendPanel() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        setPreferredSize(new Dimension(0, 28));

        for (Object[] item : ITEMS) {
            add(makeLegendItem((String) item[0], (Color) item[1], (Color) item[2]));
        }
    }

    private JPanel makeLegendItem(String label, Color top, Color bot) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        container.setOpaque(false);

        // Gradient pill swatch
        JPanel swatch = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
            }
        };
        swatch.setOpaque(false);
        swatch.setPreferredSize(new Dimension(18, 12));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(192, 230, 253));

        container.add(swatch);
        container.add(lbl);
        return container;
    }
}
