package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LeftSidebar with a gradient background, logo with gradient text icon,
 * and better-spaced navigation buttons.
 */
public class LeftSidebar extends JPanel {
    private RoundedTextField numBlocksField;
    private RoundedTextField numProcessesField;
    private JPanel blocksContainer;
    private JPanel processesContainer;

    private RoundedButton runBtn;
    private RoundedButton clearBtn;
    private RoundedButton exampleBtn;

    // Nav buttons – exposed so the controller can wire navigation
    private SidebarButton navDash;
    private SidebarButton navResults;

    public LeftSidebar() {
        setPreferredSize(new Dimension(280, 0));
        setOpaque(false); // We paint our own gradient bg
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        // ── Logo area ────────────────────────────────────────────────────
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                // Accent circle behind icon
                g2.setColor(new Color(91, 134, 182, 60));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(new Color(91, 134, 182, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, 39, 39);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(240, 52));

        JLabel logoIcon = new JLabel("\uD83D\uDDA5"); // 🖥
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        logoIcon.setPreferredSize(new Dimension(40, 40));
        logoIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel logoText = new JLabel("Memory Allocator");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoText.setForeground(ModernUI.TEXT_PRIMARY);

        logoPanel.add(logoIcon);
        logoPanel.add(logoText);

        // Divider under logo
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 20));

        // ── Nav items ────────────────────────────────────────────────────
        navDash = new SidebarButton("Dashboard", "\u25A0"); // Square
        navResults = new SidebarButton("Results", "\u25CF"); // Circle
        navDash.setActive(true);

        // ── Input sections ───────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(navDash);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(navResults);
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        content.add(makeDividerLabel("MEMORY CONFIG"));
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        numBlocksField = new RoundedTextField("5");
        RoundedButton setMemBtn = new RoundedButton("Set", "", ModernUI.ACCENT_BLUE);
        setMemBtn.setPreferredSize(new Dimension(60, 32));
        content.add(makeFieldRow(numBlocksField, setMemBtn, "\u25A4 Blocks:"));

        content.add(Box.createRigidArea(new Dimension(0, 8)));
        blocksContainer = makeContainer();
        content.add(makeScrollWrapper(blocksContainer));

        content.add(Box.createRigidArea(new Dimension(0, 16)));
        content.add(makeDividerLabel("PROCESS CONFIG"));
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        numProcessesField = new RoundedTextField("4");
        RoundedButton setProcBtn = new RoundedButton("Set", "", ModernUI.ACCENT_BLUE);
        setProcBtn.setPreferredSize(new Dimension(60, 32));
        content.add(makeFieldRow(numProcessesField, setProcBtn, "\u25A5 Procs:"));

        content.add(Box.createRigidArea(new Dimension(0, 8)));
        processesContainer = makeContainer();
        content.add(makeScrollWrapper(processesContainer));

        // Wire Set buttons
        setMemBtn.addActionListener(e -> generateFields(numBlocksField.getText(), blocksContainer, "Block"));
        setProcBtn.addActionListener(e -> generateFields(numProcessesField.getText(), processesContainer, "Process"));

        // Seed default fields
        generateFields("5", blocksContainer, "Block");
        generateFields("4", processesContainer, "Process");

        // ── Top wrapper ──────────────────────────────────────────────────
        JPanel topWrapper = new JPanel(new BorderLayout(0, 10));
        topWrapper.setOpaque(false);
        topWrapper.add(logoPanel, BorderLayout.NORTH);
        topWrapper.add(sep, BorderLayout.CENTER);
        topWrapper.add(content, BorderLayout.SOUTH);

        add(topWrapper, BorderLayout.CENTER);

        // ── Action buttons ───────────────────────────────────────────────
        JPanel actionsPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        runBtn = new RoundedButton("Run Simulation", "\u25B6", ModernUI.SUCCESS); // Play
        clearBtn = new RoundedButton("Clear All", "\u2716", ModernUI.ORANGE); // X
        exampleBtn = new RoundedButton("Load Example", "\u25C6", ModernUI.ACCENT_BLUE); // Diamond

        actionsPanel.add(runBtn);
        actionsPanel.add(clearBtn);
        actionsPanel.add(exampleBtn);
        add(actionsPanel, BorderLayout.SOUTH);
    }

    // ── Gradient sidebar background ──────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);
        g2.setPaint(new GradientPaint(0, 0, ModernUI.BG_SECONDARY,
                0, getHeight(), new Color(10, 28, 54)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Right-edge separator line
        g2.setColor(new Color(255, 255, 255, 15));
        g2.fillRect(getWidth() - 1, 0, 1, getHeight());

        g2.dispose();
        super.paintComponent(g);
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private JLabel makeDividerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(128, 170, 211, 160));
        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        return l;
    }

    private JPanel makeFieldRow(RoundedTextField field, RoundedButton btn, String labelText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernUI.FONT_LABEL);
        lbl.setForeground(ModernUI.TEXT_SECONDARY);

        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.add(btn, BorderLayout.EAST);
        return row;
    }

    private JPanel makeContainer() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private JScrollPane makeScrollWrapper(JPanel container) {
        JScrollPane scroll = new JScrollPane(container);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(240, 140));
        return scroll;
    }

    private void generateFields(String countStr, JPanel container, String prefix) {
        container.removeAll();
        try {
            int n = Integer.parseInt(countStr.trim());
            String[] defaults = prefix.equals("Block")
                    ? new String[] { "100", "500", "200", "300", "600" }
                    : new String[] { "212", "417", "112", "426", "50" };
            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new BorderLayout(6, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

                JLabel l = new JLabel(prefix + " " + (i + 1));
                l.setForeground(ModernUI.TEXT_PRIMARY);
                l.setFont(ModernUI.FONT_LABEL);
                l.setPreferredSize(new Dimension(76, 24));

                String def = i < defaults.length ? defaults[i] : (prefix.equals("Block") ? "100" : "50");
                RoundedTextField tf = new RoundedTextField(def);

                row.add(l, BorderLayout.WEST);
                row.add(tf, BorderLayout.CENTER);
                container.add(row);
            }
        } catch (Exception ignored) {
        }
        container.revalidate();
        container.repaint();
    }

    // ── Public accessors ──────────────────────────────────────────────────
    public java.util.List<Integer> getBlockSizes() {
        return getValuesFrom(blocksContainer);
    }

    public java.util.List<Integer> getProcessSizes() {
        return getValuesFrom(processesContainer);
    }

    private java.util.List<Integer> getValuesFrom(JPanel c) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (Component child : c.getComponents()) {
            if (child instanceof JPanel) {
                JPanel row = (JPanel) child;
                for (Component comp : row.getComponents()) {
                    if (comp instanceof RoundedTextField) {
                        try {
                            list.add(Integer.parseInt(((RoundedTextField) comp).getText().trim()));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return list;
    }

    public RoundedButton getRunBtn() {
        return runBtn;
    }

    public RoundedButton getClearBtn() {
        return clearBtn;
    }

    public RoundedButton getExampleBtn() {
        return exampleBtn;
    }

    public JPanel getBlocksContainer() {
        return blocksContainer;
    }

    public JPanel getProcessesContainer() {
        return processesContainer;
    }

    public RoundedTextField getNumBlocksField() {
        return numBlocksField;
    }

    public RoundedTextField getNumProcessesField() {
        return numProcessesField;
    }

    public SidebarButton getNavDash() {
        return navDash;
    }

    public SidebarButton getNavResults() {
        return navResults;
    }

    /** Deactivates all nav buttons and activates only the specified one. */
    public void setActiveNav(SidebarButton active) {
        for (SidebarButton btn : new SidebarButton[] { navDash, navResults }) {
            btn.setActive(btn == active);
        }
    }
}
