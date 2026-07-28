package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * DashboardCard with gradient background, accent glow top-border,
 * and a large coloured icon circle.
 */
public class DashboardCard extends RoundedPanel {
    private JLabel valueLabel;
    private Color accentColor;

    public DashboardCard(String title, String initialValue, String iconText, Color accentColor) {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        this.accentColor = accentColor;
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ── Icon circle ──────────────────────────────────────────────────
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                // Soft translucent circle
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 35));
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Slightly stronger ring
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setLayout(new BorderLayout());
        iconCircle.setPreferredSize(new Dimension(32, 32));

        JLabel iconLabel = new JLabel(iconText, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setForeground(accentColor);
        iconCircle.add(iconLabel, BorderLayout.CENTER);

        // ── Title + icon row ─────────────────────────────────────────────
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(iconCircle);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernUI.FONT_LABEL);
        titleLabel.setForeground(ModernUI.TEXT_SECONDARY);
        topRow.add(titleLabel);

        // ── Value ────────────────────────────────────────────────────────
        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(ModernUI.TEXT_PRIMARY);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        add(topRow, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    /**
     * Paint the card with a gradient and a thin coloured top-border accent line.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), r = ModernUI.RADIUS_ROUND;

        // Soft drop shadow
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fill(new RoundRectangle2D.Float(2, 4, w - 4, h - 4, r, r));

        // Gradient background: card-bg → slightly lighter
        GradientPaint grad = new GradientPaint(
            0, 0, ModernUI.CARD_BG,
            0, h, new Color(
                Math.min(255, ModernUI.CARD_BG.getRed() + 18),
                Math.min(255, ModernUI.CARD_BG.getGreen() + 18),
                Math.min(255, ModernUI.CARD_BG.getBlue() + 18)
            )
        );
        g2.setPaint(grad);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 4, r, r));

        // Accent top-border glow (3 px)
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(r / 2, 2, w - 2 - r / 2, 2);

        g2.dispose();
        super.paintComponent(g);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
