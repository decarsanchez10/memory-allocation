package view;

import javax.swing.*;
import java.awt.*;

/**
 * LeftSidebar – redesigned with a clean BoxLayout top-to-bottom flow.
 * The logo, nav section, config inputs, and action buttons all sit in
 * a single vertical BoxLayout column so nothing gets clipped or mis-sized.
 */
public final class LeftSidebar extends JPanel {
    private static final long serialVersionUID = 1L;

    private RoundedTextField numBlocksField;
    private RoundedTextField numProcessesField;
    private JPanel blocksContainer;
    private JPanel processesContainer;

    private RoundedButton runBtn;
    private RoundedButton clearBtn;
    private RoundedButton exampleBtn;

    private SidebarButton    navDash;
    private SidebarButton    navResults;
    private AlgorithmToggle  algoToggle;

    public LeftSidebar() {
        setPreferredSize(new Dimension(268, 0));
        setOpaque(false);
        setLayout(new BorderLayout());

        // ── Scrollable centre column ──────────────────────────────────────
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(BorderFactory.createEmptyBorder(24, 14, 14, 14));

        // ── Logo ─────────────────────────────────────────────────────────
        col.add(buildLogoRow());
        col.add(vgap(18));

        // ── Thin divider ─────────────────────────────────────────────────
        col.add(buildDivider());
        col.add(vgap(12));

        // ── Nav section label ─────────────────────────────────────────────
        col.add(sectionLabel("NAVIGATION"));
        col.add(vgap(6));

        // ── Nav buttons ───────────────────────────────────────────────────
        navDash    = new SidebarButton("Dashboard", "\uD83D\uDDC2"); // 🗂
        navResults = new SidebarButton("Results",   "\uD83D\uDCCA"); // 📊
        navDash.setActive(true);
        navDash.setAlignmentX(LEFT_ALIGNMENT);
        navResults.setAlignmentX(LEFT_ALIGNMENT);
        col.add(navDash);
        col.add(vgap(4));
        col.add(navResults);
        col.add(vgap(18));

        // ── Algorithm selector ────────────────────────────────────────────
        col.add(sectionLabel("ALGORITHM"));
        col.add(vgap(8));
        algoToggle = new AlgorithmToggle();
        algoToggle.setAlignmentX(LEFT_ALIGNMENT);
        col.add(algoToggle);
        col.add(vgap(20));

        // ── Memory config ─────────────────────────────────────────────────
        col.add(sectionLabel("MEMORY BLOCKS"));
        col.add(vgap(8));

        numBlocksField = new RoundedTextField("5");
        RoundedButton setMemBtn = new RoundedButton("Set", "", ModernUI.ACCENT_BLUE);
        setMemBtn.setPreferredSize(new Dimension(52, 30));
        setMemBtn.setMaximumSize(new Dimension(52, 30));
        JPanel memRow = makeFieldRow(numBlocksField, setMemBtn, "Blocks:");
        memRow.setAlignmentX(LEFT_ALIGNMENT);
        col.add(memRow);
        col.add(vgap(8));

        blocksContainer = makeContainer();
        JScrollPane blocksScroll = makeScrollWrapper(blocksContainer);
        blocksScroll.setAlignmentX(LEFT_ALIGNMENT);
        col.add(blocksScroll);
        col.add(vgap(18));

        // ── Process config ────────────────────────────────────────────────
        col.add(sectionLabel("PROCESSES"));
        col.add(vgap(8));

        numProcessesField = new RoundedTextField("4");
        RoundedButton setProcBtn = new RoundedButton("Set", "", ModernUI.ACCENT_BLUE);
        setProcBtn.setPreferredSize(new Dimension(52, 30));
        setProcBtn.setMaximumSize(new Dimension(52, 30));
        JPanel procRow = makeFieldRow(numProcessesField, setProcBtn, "Procs:");
        procRow.setAlignmentX(LEFT_ALIGNMENT);
        col.add(procRow);
        col.add(vgap(8));

        processesContainer = makeContainer();
        JScrollPane procsScroll = makeScrollWrapper(processesContainer);
        procsScroll.setAlignmentX(LEFT_ALIGNMENT);
        col.add(procsScroll);

        // Wire Set buttons
        setMemBtn.addActionListener(e -> generateFields(numBlocksField.getText(), blocksContainer, "Block"));
        setProcBtn.addActionListener(e -> generateFields(numProcessesField.getText(), processesContainer, "Process"));

        // Seed defaults
        generateFields("5", blocksContainer, "Block");
        generateFields("4", processesContainer, "Process");

        add(col, BorderLayout.CENTER);

        // ── Action buttons (pinned to bottom) ─────────────────────────────
        JPanel actions = new JPanel(new GridLayout(3, 1, 0, 8));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(10, 14, 20, 14));

        runBtn     = new RoundedButton("Run Simulation", "▶", ModernUI.SUCCESS);
        clearBtn   = new RoundedButton("Clear All",      "✕", ModernUI.ORANGE);
        exampleBtn = new RoundedButton("Load Example",   "★", ModernUI.ACCENT_BLUE);

        actions.add(runBtn);
        actions.add(clearBtn);
        actions.add(exampleBtn);
        add(actions, BorderLayout.SOUTH);
    }

    // ── Gradient sidebar background ──────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        // Deep navy gradient
        g2.setPaint(new GradientPaint(
            0, 0,            new Color(15, 33, 60),
            0, getHeight(),  new Color(8, 18, 38)
        ));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Subtle right edge line
        g2.setColor(new Color(91, 134, 182, 30));
        g2.fillRect(getWidth() - 1, 0, 1, getHeight());

        g2.dispose();
        super.paintComponent(g);
    }

    // ── Logo row ─────────────────────────────────────────────────────────
    private JPanel buildLogoRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setAlignmentX(LEFT_ALIGNMENT);

        // Icon badge
        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient circle
                g2.setPaint(new GradientPaint(0, 0, new Color(63, 101, 147),
                                               getWidth(), getHeight(), new Color(30, 60, 110)));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(new Color(91, 134, 182, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, 38, 38);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(40, 40));
        badge.setLayout(new BorderLayout());
        JLabel badgeIcon = new JLabel("\uD83D\uDDA5", SwingConstants.CENTER); // 🖥
        badgeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        badge.add(badgeIcon, BorderLayout.CENTER);

        // Title text
        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Memory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ModernUI.TEXT_PRIMARY);
        JLabel sub = new JLabel("Allocator");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(128, 170, 211));
        textCol.add(title);
        textCol.add(sub);

        row.add(badge);
        row.add(textCol);
        return row;
    }

    // ── Thin custom divider ───────────────────────────────────────────────
    private JPanel buildDivider() {
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(91, 134, 182, 0),
                    getWidth() / 2f, 0, new Color(91, 134, 182, 60)));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        div.setOpaque(false);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div.setPreferredSize(new Dimension(0, 1));
        div.setAlignmentX(LEFT_ALIGNMENT);
        return div;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(new Color(128, 170, 211, 140));
        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel makeFieldRow(RoundedTextField field, RoundedButton btn, String labelText) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ModernUI.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(50, 30));

        row.add(lbl,   BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.add(btn,   BorderLayout.EAST);
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
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        scroll.setPreferredSize(new Dimension(0, 130));
        return scroll;
    }

    private void generateFields(String countStr, JPanel container, String prefix) {
        container.removeAll();
        try {
            int n = Integer.parseInt(countStr.trim());
            String[] defaults = prefix.equals("Block")
                    ? new String[]{"100", "500", "200", "300", "600"}
                    : new String[]{"212", "417", "112", "426", "50"};
            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new BorderLayout(6, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                JLabel l = new JLabel(prefix + " " + (i + 1));
                l.setForeground(ModernUI.TEXT_PRIMARY);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                l.setPreferredSize(new Dimension(72, 24));

                String def = i < defaults.length ? defaults[i] : (prefix.equals("Block") ? "100" : "50");
                RoundedTextField tf = new RoundedTextField(def);

                row.add(l, BorderLayout.WEST);
                row.add(tf, BorderLayout.CENTER);
                container.add(row);
            }
        } catch (Exception ignored) {}
        container.revalidate();
        container.repaint();
    }

    // ── Public accessors ──────────────────────────────────────────────────
    public java.util.List<Integer> getBlockSizes()    { return getValuesFrom(blocksContainer); }
    public java.util.List<Integer> getProcessSizes()  { return getValuesFrom(processesContainer); }

    private java.util.List<Integer> getValuesFrom(JPanel c) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (Component child : c.getComponents()) {
            if (child instanceof JPanel) {
                for (Component comp : ((JPanel) child).getComponents()) {
                    if (comp instanceof RoundedTextField) {
                        try { list.add(Integer.parseInt(((RoundedTextField) comp).getText().trim())); }
                        catch (Exception ignored) {}
                    }
                }
            }
        }
        return list;
    }

    public RoundedButton    getRunBtn()            { return runBtn; }
    public RoundedButton    getClearBtn()           { return clearBtn; }
    public RoundedButton    getExampleBtn()         { return exampleBtn; }
    public JPanel           getBlocksContainer()    { return blocksContainer; }
    public JPanel           getProcessesContainer() { return processesContainer; }
    public RoundedTextField getNumBlocksField()     { return numBlocksField; }
    public RoundedTextField getNumProcessesField()  { return numProcessesField; }
    public SidebarButton    getNavDash()            { return navDash; }
    public SidebarButton    getNavResults()         { return navResults; }

    /** Returns "First Fit" or "Best Fit" based on the toggle selection. */
    public String getSelectedAlgorithm()            { return algoToggle.getSelected(); }

    public void setActiveNav(SidebarButton active) {
        for (SidebarButton btn : new SidebarButton[]{navDash, navResults}) {
            btn.setActive(btn == active);
        }
    }

    // ── Algorithm Toggle ─────────────────────────────────────────────────
    /**
     * Pill-style two-option toggle: [First Fit] [Best Fit].
     * The selected option gets a gradient fill; the other is ghost.
     */
    private static final class AlgorithmToggle extends JPanel {
        private static final String OPT_FF = "First Fit";
        private static final String OPT_BF = "Best Fit";
        private String selected = OPT_FF;

        AlgorithmToggle() {
            setOpaque(false);
            setLayout(new GridLayout(1, 2, 0, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

            JButton ff = makeChip(OPT_FF);
            JButton bf = makeChip(OPT_BF);

            ff.addActionListener(e -> { selected = OPT_FF; ff.repaint(); bf.repaint(); });
            bf.addActionListener(e -> { selected = OPT_BF; ff.repaint(); bf.repaint(); });

            add(ff);
            add(bf);
        }

        private JButton makeChip(String label) {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    boolean active = label.equals(selected);
                    int w = getWidth(), h = getHeight();

                    if (active) {
                        // Gradient accent fill
                        g2.setPaint(new GradientPaint(
                            0, 0, new Color(63, 101, 147),
                            0, h, new Color(30, 70, 120)
                        ));
                        g2.fillRoundRect(0, 0, w, h, 10, 10);
                        // Top sheen
                        g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,40), 0, h/2, new Color(255,255,255,0)));
                        g2.fillRoundRect(1, 1, w-2, h/2, 10, 10);
                    } else {
                        // Ghost background
                        g2.setColor(new Color(255, 255, 255, 8));
                        g2.fillRoundRect(0, 0, w, h, 10, 10);
                    }

                    // Border
                    g2.setColor(active
                        ? new Color(91, 134, 182, 180)
                        : new Color(91, 134, 182, 50));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, w-1, h-1, 10, 10);

                    // Text
                    Font font = active
                        ? new Font("Segoe UI", Font.BOLD,  12)
                        : new Font("Segoe UI", Font.PLAIN, 12);
                    g2.setFont(font);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(active ? Color.WHITE : new Color(160, 195, 230));
                    int tx = (w - fm.stringWidth(label)) / 2;
                    int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(label, tx, ty);

                    g2.dispose();
                }
            };
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.repaint(); }
                @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.repaint(); }
            });
            return btn;
        }

        public String getSelected() { return selected; }
    }
}
