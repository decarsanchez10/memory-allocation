package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * SettingsPage – a full-page view shown when the "Settings" sidebar nav
 * button is clicked. Provides algorithm selector, animation speed control,
 * and display preferences.
 */
public class SettingsPage extends RoundedPanel {

    private JComboBox<String> algorithmCombo;
    private JSlider animSpeedSlider;
    private JCheckBox showFragCheckbox;
    private JCheckBox showLabelsCheckbox;

    public SettingsPage() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // ── Page header ──────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel("\u2699\uFE0F");   // ⚙️
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ModernUI.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Configure simulation behaviour and display preferences.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(ModernUI.TEXT_SECONDARY);

        textCol.add(title);
        textCol.add(Box.createRigidArea(new Dimension(0, 4)));
        textCol.add(subtitle);

        header.add(icon);
        header.add(textCol);
        add(header, BorderLayout.NORTH);

        // ── Settings grid ────────────────────────────────────────────────
        JPanel grid = new JPanel();
        grid.setOpaque(false);
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));

        grid.add(makeSectionLabel("SIMULATION"));
        grid.add(Box.createRigidArea(new Dimension(0, 10)));
        grid.add(makeRow("Default Visualized Algorithm",
                makeCombo(new String[]{"First Fit", "Best Fit", "Both"})));
        grid.add(Box.createRigidArea(new Dimension(0, 14)));

        grid.add(makeSectionLabel("ANIMATION"));
        grid.add(Box.createRigidArea(new Dimension(0, 10)));
        animSpeedSlider = new JSlider(100, 1500, 900);
        animSpeedSlider.setOpaque(false);
        animSpeedSlider.setForeground(ModernUI.HIGHLIGHT);
        animSpeedSlider.setPaintTicks(true);
        animSpeedSlider.setMajorTickSpacing(350);
        JLabel sliderLabel = new JLabel("Faster ←─────────────────────── Slower");
        sliderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sliderLabel.setForeground(ModernUI.TEXT_SECONDARY);
        grid.add(makeRow("Animation Duration", animSpeedSlider));
        grid.add(Box.createRigidArea(new Dimension(0, 4)));
        grid.add(sliderLabel);
        grid.add(Box.createRigidArea(new Dimension(0, 14)));

        grid.add(makeSectionLabel("DISPLAY"));
        grid.add(Box.createRigidArea(new Dimension(0, 10)));
        showFragCheckbox  = makeCheckBox("Highlight Fragmentation Segments", true);
        showLabelsCheckbox = makeCheckBox("Show Block Labels on Memory Bars", true);
        grid.add(showFragCheckbox);
        grid.add(Box.createRigidArea(new Dimension(0, 8)));
        grid.add(showLabelsCheckbox);
        grid.add(Box.createRigidArea(new Dimension(0, 24)));

        // Save note
        JLabel note = new JLabel("\u2139\uFE0F  Settings are applied immediately and persist while the application is open.");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        note.setForeground(new Color(128, 170, 211, 180));
        grid.add(note);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(128, 170, 211, 160));
        return l;
    }

    private JPanel makeRow(String labelText, JComponent control) {
        JPanel row = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(ModernUI.TEXT_PRIMARY);

        control.setMaximumSize(new Dimension(260, 36));

        row.add(lbl, BorderLayout.WEST);
        row.add(control, BorderLayout.EAST);
        return row;
    }

    private JComboBox<String> makeCombo(String[] items) {
        algorithmCombo = new JComboBox<>(items);
        algorithmCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        algorithmCombo.setBackground(ModernUI.BG_PRIMARY);
        algorithmCombo.setForeground(ModernUI.TEXT_PRIMARY);
        return algorithmCombo;
    }

    private JCheckBox makeCheckBox(String label, boolean selected) {
        JCheckBox cb = new JCheckBox(label, selected);
        cb.setOpaque(false);
        cb.setForeground(ModernUI.TEXT_PRIMARY);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setFocusPainted(false);
        return cb;
    }

    // ── Public accessors (for controller if needed) ───────────────────────
    public int  getAnimationDurationMs()     { return animSpeedSlider.getValue(); }
    public boolean isShowFragmentation()     { return showFragCheckbox.isSelected(); }
    public boolean isShowLabels()            { return showLabelsCheckbox.isSelected(); }
}
