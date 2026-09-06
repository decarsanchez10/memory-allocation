import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        
        SwingUtilities.invokeLater(() -> {
            MainDashboardView view = new MainDashboardView();
            new MainController(view);
            view.setVisible(true);
        });
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MODEL CLASSES
// ─────────────────────────────────────────────────────────────────────────────

class AllocationResult {
    public String algorithm;
    public List<MemoryBlock> memoryBlocks;
    public List<Process> processes;
    public int totalExternalFragmentation;
    public int totalInternalFragmentation;
    public int totalMemory;
    public int usedMemory;
    public Map<Integer, Integer> allocationMap;
    public List<Integer> unallocatedProcesses;

    public AllocationResult() {
        this.memoryBlocks = new ArrayList<>();
        this.processes = new ArrayList<>();
        this.allocationMap = new HashMap<>();
        this.unallocatedProcesses = new ArrayList<>();
    }
}

class MemoryBlock {
    public int id;
    public int size;
    public int remainingSize;
    public boolean isAllocated;
    public int processId;
    public int internalFragmentation;
    public Color color;

    public MemoryBlock(int id, int size) {
        this.id = id;
        this.size = size;
        this.remainingSize = size;
        this.isAllocated = false;
        this.processId = -1;
        this.internalFragmentation = 0;
        this.color = null;
    }

    public MemoryBlock copy() {
        MemoryBlock copy = new MemoryBlock(this.id, this.size);
        copy.remainingSize = this.remainingSize;
        copy.isAllocated = this.isAllocated;
        copy.processId = this.processId;
        copy.internalFragmentation = this.internalFragmentation;
        copy.color = this.color;
        return copy;
    }
}

class Process {
    public int id;
    public int size;
    public boolean isAllocated;

    public Process(int id, int size) {
        this.id = id;
        this.size = size;
        this.isAllocated = false;
    }

    public Process copy() {
        Process copy = new Process(this.id, this.size);
        copy.isAllocated = this.isAllocated;
        return copy;
    }
}

class Simulator {
    public static AllocationResult firstFit(List<MemoryBlock> blocks, List<Process> processes) {
        AllocationResult result = new AllocationResult();
        result.algorithm = "First Fit";
        result.memoryBlocks = copyBlocks(blocks);
        result.processes = copyProcesses(processes);

        for (Process process : result.processes) {
            boolean allocated = false;
            for (MemoryBlock block : result.memoryBlocks) {
                if (!block.isAllocated && block.remainingSize >= process.size) {
                    block.isAllocated = true;
                    block.processId = process.id;
                    block.internalFragmentation = block.remainingSize - process.size;
                    block.remainingSize = 0;
                    process.isAllocated = true;
                    result.allocationMap.put(process.id, block.id);
                    allocated = true;
                    break;
                }
            }
            if (!allocated) {
                result.unallocatedProcesses.add(process.id);
            }
        }
        calculateFragmentation(result);
        return result;
    }

    public static AllocationResult bestFit(List<MemoryBlock> blocks, List<Process> processes) {
        AllocationResult result = new AllocationResult();
        result.algorithm = "Best Fit";
        result.memoryBlocks = copyBlocks(blocks);
        result.processes = copyProcesses(processes);

        for (Process process : result.processes) {
            int bestBlockIndex = -1;
            int minWastage = Integer.MAX_VALUE;

            for (int i = 0; i < result.memoryBlocks.size(); i++) {
                MemoryBlock block = result.memoryBlocks.get(i);
                if (!block.isAllocated && block.remainingSize >= process.size) {
                    int wastage = block.remainingSize - process.size;
                    if (wastage < minWastage) {
                        minWastage = wastage;
                        bestBlockIndex = i;
                    }
                }
            }

            if (bestBlockIndex != -1) {
                MemoryBlock bestBlock = result.memoryBlocks.get(bestBlockIndex);
                bestBlock.isAllocated = true;
                bestBlock.processId = process.id;
                bestBlock.internalFragmentation = bestBlock.remainingSize - process.size;
                bestBlock.remainingSize = 0;
                process.isAllocated = true;
                result.allocationMap.put(process.id, bestBlock.id);
            } else {
                result.unallocatedProcesses.add(process.id);
            }
        }
        calculateFragmentation(result);
        return result;
    }

    private static void calculateFragmentation(AllocationResult result) {
        result.totalExternalFragmentation = 0;
        result.totalInternalFragmentation = 0;
        result.totalMemory = 0;
        result.usedMemory = 0;

        for (MemoryBlock block : result.memoryBlocks) {
            result.totalMemory += block.size;
            if (!block.isAllocated) {
                result.totalExternalFragmentation += block.remainingSize;
            } else {
                result.totalInternalFragmentation += block.internalFragmentation;
                result.usedMemory += (block.size - block.internalFragmentation);
            }
        }
    }

    public static List<MemoryBlock> copyBlocks(List<MemoryBlock> original) {
        List<MemoryBlock> copy = new ArrayList<>();
        for (MemoryBlock block : original) {
            copy.add(block.copy());
        }
        return copy;
    }

    public static List<Process> copyProcesses(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process process : original) {
            copy.add(process.copy());
        }
        return copy;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CONTROLLER CLASS
// ─────────────────────────────────────────────────────────────────────────────

class MainController {
    private final MainDashboardView view;

    private AllocationResult lastFF = new AllocationResult();
    private AllocationResult lastBF = new AllocationResult();

    public MainController(MainDashboardView view) {
        this.view = view;
        initController();
    }

    private void initController() {
        view.getSidebar().getRunBtn().addActionListener(e -> runSimulation());
        view.getSidebar().getClearBtn().addActionListener(e -> clearAll());
        view.getSidebar().getExampleBtn().addActionListener(e -> loadExample());

        LeftSidebar sb = view.getSidebar();

        sb.getNavDash().addActionListener(e -> {
            view.navigateTo(MainDashboardView.PAGE_DASHBOARD);
            sb.setActiveNav(sb.getNavDash());
        });

        sb.getNavResults().addActionListener(e -> {
            view.getResultsPage().updateResults(lastFF, lastBF);
            view.navigateTo(MainDashboardView.PAGE_RESULTS);
            sb.setActiveNav(sb.getNavResults());
        });
    }

    private void runSimulation() {
        List<Integer> blockSizes = view.getSidebar().getBlockSizes();
        List<Integer> procSizes  = view.getSidebar().getProcessSizes();

        if (blockSizes.isEmpty() || procSizes.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                "Please provide valid block and process sizes.", "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<MemoryBlock> blocks = new ArrayList<>();
        for (int i = 0; i < blockSizes.size(); i++) {
            blocks.add(new MemoryBlock(i + 1, blockSizes.get(i)));
        }

        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < procSizes.size(); i++) {
            processes.add(new Process(i + 1, procSizes.get(i)));
        }

        lastFF = Simulator.firstFit(blocks, processes);
        lastBF = Simulator.bestFit(blocks, processes);

        String algo = view.getSidebar().getSelectedAlgorithm();
        AllocationResult active = algo.equals("Best Fit") ? lastBF : lastFF;

        view.getVisualizerPanel().updateBlocks(active.memoryBlocks, true);
        view.getResultsPage().updateResults(lastFF, lastBF);

        double util = active.totalMemory > 0
                      ? (active.usedMemory * 100.0 / active.totalMemory) : 0;
        int alloc   = (int) active.processes.stream().filter(p -> p.isAllocated).count();
        view.getSummaryCards().updateStats(
            util,
            active.totalExternalFragmentation,
            active.totalInternalFragmentation,
            active.totalExternalFragmentation + active.totalInternalFragmentation,
            alloc,
            active.unallocatedProcesses.size()
        );

        view.getQueuePanel().startAnimation(active);
    }

    private void clearAll() {
        view.getSidebar().getNumBlocksField().setText("");
        view.getSidebar().getNumProcessesField().setText("");
        view.getSidebar().getBlocksContainer().removeAll();
        view.getSidebar().getProcessesContainer().removeAll();
        view.getSidebar().getBlocksContainer().repaint();
        view.getSidebar().getProcessesContainer().repaint();

        lastFF = new AllocationResult();
        lastBF = new AllocationResult();

        view.getVisualizerPanel().updateBlocks(new ArrayList<>(), false);
        view.getResultsPage().updateResults(lastFF, lastBF);
        view.getSummaryCards().updateStats(0, 0, 0, 0, 0, 0);
        view.getQueuePanel().reset();
    }

    private void loadExample() {
        view.getSidebar().getNumBlocksField().setText("5");
        view.getSidebar().getNumProcessesField().setText("4");

        JPanel blocksContainer = view.getSidebar().getBlocksContainer();
        blocksContainer.removeAll();
        int[] bSizes = {100, 500, 200, 300, 600};
        for (int i = 0; i < 5; i++) {
            blocksContainer.add(makeRow("Block " + (i + 1), String.valueOf(bSizes[i])));
        }

        JPanel processesContainer = view.getSidebar().getProcessesContainer();
        processesContainer.removeAll();
        int[] pSizes = {212, 417, 112, 426};
        for (int i = 0; i < 4; i++) {
            processesContainer.add(makeRow("Process " + (i + 1), String.valueOf(pSizes[i])));
        }

        blocksContainer.revalidate();
        processesContainer.revalidate();
        view.repaint();
    }

    private JPanel makeRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JLabel l = new JLabel(label);
        l.setForeground(ModernUI.TEXT_PRIMARY);
        l.setFont(ModernUI.FONT_LABEL);
        l.setPreferredSize(new Dimension(76, 24));
        RoundedTextField tf = new RoundedTextField(value);
        row.add(l, BorderLayout.WEST);
        row.add(tf, BorderLayout.CENTER);
        return row;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VIEW CLASSES
// ─────────────────────────────────────────────────────────────────────────────

class ModernUI {
    public static final Color BG_PRIMARY = Color.decode("#000F22");
    public static final Color BG_SECONDARY = Color.decode("#132847");
    public static final Color CARD_BG = Color.decode("#1B3554");
    
    public static final Color ACCENT_BLUE = Color.decode("#3F6593");
    public static final Color LIGHT_BLUE = Color.decode("#5B86B6");
    public static final Color HIGHLIGHT = Color.decode("#80AAD3");
    
    public static final Color TEXT_PRIMARY = Color.decode("#FFFFFF");
    public static final Color TEXT_SECONDARY = Color.decode("#C0E6FD");
    
    public static final Color SUCCESS = Color.decode("#22C55E");
    public static final Color WARNING = Color.decode("#F59E0B");
    public static final Color DANGER = Color.decode("#EF4444");
    public static final Color PURPLE = Color.decode("#8B5CF6");
    public static final Color ORANGE = Color.decode("#F97316");
    public static final Color GRAY = Color.decode("#4B5563");

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 14);
    
    public static final int MARGIN_OUTER = 20;
    public static final int PADDING_CARD = 20;
    public static final int GAP_CARDS = 16;
    public static final int RADIUS_ROUND = 18;
    public static final int RADIUS_INPUT = 12;
    
    public static void applyQualityRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}

class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private boolean drawShadow;
    private boolean useGradient;

    public RoundedPanel(int radius, Color bgColor, boolean drawShadow) {
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.drawShadow = drawShadow;
        this.useGradient = false;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), r = cornerRadius;

        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fill(new RoundRectangle2D.Float(3, 6, w - 6, h - 6, r, r));
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fill(new RoundRectangle2D.Float(1, 3, w - 4, h - 4, r, r));
        }

        if (useGradient) {
            Color lighter = new Color(
                Math.min(255, backgroundColor.getRed() + 15),
                Math.min(255, backgroundColor.getGreen() + 15),
                Math.min(255, backgroundColor.getBlue() + 15)
            );
            g2.setPaint(new GradientPaint(0, 0, backgroundColor, 0, h, lighter));
        } else {
            g2.setColor(backgroundColor);
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 4, r, r));

        g2.setColor(new Color(255, 255, 255, 12));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(1, 1, w - 4, h - 6, r - 1, r - 1));

        g2.dispose();
        super.paintComponent(g);
    }

    public void setBackgroundColor(Color bg) {
        this.backgroundColor = bg;
        repaint();
    }

    public void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
        repaint();
    }
}

class DashboardCard extends RoundedPanel {
    private JLabel valueLabel;
    private Color accentColor;

    public DashboardCard(String title, String initialValue, String iconText, Color accentColor) {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        this.accentColor = accentColor;
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 35));
                g2.fillOval(0, 0, getWidth(), getHeight());
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

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(iconCircle);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernUI.FONT_LABEL);
        titleLabel.setForeground(ModernUI.TEXT_SECONDARY);
        topRow.add(titleLabel);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(ModernUI.TEXT_PRIMARY);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        add(topRow, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), r = ModernUI.RADIUS_ROUND;

        g2.setColor(new Color(0, 0, 0, 55));
        g2.fill(new RoundRectangle2D.Float(2, 4, w - 4, h - 4, r, r));

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

class RoundedButton extends JButton {
    private final String  labelText;
    private final String  iconKey;
    private final Color   normalColor;
    private final Color   hoverColor;
    private final Color   pressedColor;

    public RoundedButton(String text, String iconKey, Color bg) {
        super();
        this.labelText    = text;
        this.iconKey      = (iconKey != null) ? iconKey.toLowerCase().trim() : "";
        this.normalColor  = bg;
        this.hoverColor   = brighten(bg, 30);
        this.pressedColor = darken(bg, 30);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited (MouseEvent e) { repaint(); }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        return new Dimension(100, 42);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), arc = ModernUI.RADIUS_INPUT;

        Color top, bot;
        if (getModel().isPressed()) {
            top = pressedColor; bot = darken(pressedColor, 20);
        } else if (getModel().isRollover()) {
            top = hoverColor;   bot = darken(hoverColor,   20);
        } else {
            top = normalColor;  bot = darken(normalColor,  20);
        }

        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,50),
                                      0, h/2, new Color(255,255,255,0)));
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h / 2f, arc, arc));

        g2.setColor(new Color(255, 255, 255, 25));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));

        Font textFont = new Font("Segoe UI", Font.BOLD, 13);
        g2.setFont(textFont);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(labelText);

        boolean hasIcon = !iconKey.isEmpty() && isKnownIcon(iconKey);
        int iconSize = 12;
        int gap      = hasIcon ? 8 : 0;
        int iconArea = hasIcon ? iconSize : 0;
        int totalW   = iconArea + gap + textW;
        int startX   = (w - totalW) / 2;

        if (hasIcon) {
            g2.setColor(Color.WHITE);
            int iy = (h - iconSize) / 2;
            drawIconShape(g2, iconKey, startX, iy, iconSize);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(textFont);
        int textBaseline = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(labelText, startX + iconArea + gap, textBaseline);

        g2.dispose();
    }

    private static boolean isKnownIcon(String key) {
        switch (key) {
            case "play": case "x": case "star": case "set": return true;
            default: return false;
        }
    }

    private static void drawIconShape(Graphics2D g2, String key, int x, int y, int s) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (key) {
            case "play": drawPlay(g2, x, y, s); break;
            case "x":    drawX(g2, x, y, s);    break;
            case "star": drawStar(g2, x, y, s);  break;
            case "set":  drawSet(g2, x, y, s);   break;
        }
    }

    private static void drawPlay(Graphics2D g2, int x, int y, int s) {
        int[] px = {x + 2, x + s, x + 2};
        int[] py = {y, y + s / 2, y + s};
        g2.fillPolygon(px, py, 3);
    }

    private static void drawX(Graphics2D g2, int x, int y, int s) {
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 2, y + 2, x + s - 2, y + s - 2);
        g2.drawLine(x + s - 2, y + 2, x + 2, y + s - 2);
    }

    private static void drawStar(Graphics2D g2, int x, int y, int s) {
        double cx = x + s / 2.0, cy = y + s / 2.0;
        double outer = s / 2.0, inner = s / 5.0;
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? outer : inner;
            double angle = Math.PI / 2 + i * Math.PI / 5;
            double px = cx + r * Math.cos(angle);
            double py = cy - r * Math.sin(angle);
            if (i == 0) star.moveTo(px, py);
            else        star.lineTo(px, py);
        }
        star.closePath();
        g2.fill(star);
    }

    private static void drawSet(Graphics2D g2, int x, int y, int s) {
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = x + s/2, cy = y + s/2;
        int r = s / 3;
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            int x1 = (int)(cx + r * Math.cos(angle));
            int y1 = (int)(cy - r * Math.sin(angle));
            int x2 = (int)(cx + (r + 3) * Math.cos(angle));
            int y2 = (int)(cy - (r + 3) * Math.sin(angle));
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private static Color brighten(Color c, int amt) {
        return new Color(Math.min(255, c.getRed()   + amt),
                         Math.min(255, c.getGreen() + amt),
                         Math.min(255, c.getBlue()  + amt));
    }
    private static Color darken(Color c, int amt) {
        return new Color(Math.max(0, c.getRed()   - amt),
                         Math.max(0, c.getGreen() - amt),
                         Math.max(0, c.getBlue()  - amt));
    }
}

class RoundedTextField extends JTextField {
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

class StatusBadge extends JPanel {
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
        
        int dotSize = 8;
        int dotX = x - 12;
        int dotY = (getHeight() - dotSize) / 2;
        g2.fillOval(dotX, dotY, dotSize, dotSize);
        
        g2.drawString(text, x, y);
        g2.dispose();
    }
}

class SidebarButton extends JButton {
    private final String iconStr;
    private final String labelText;
    private boolean isActive = false;

    public SidebarButton(String text, String iconStr) {
        super();
        this.iconStr   = iconStr;
        this.labelText = text;

        setForeground(new Color(160, 195, 230));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(0, 44));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!isActive) setForeground(Color.WHITE);
                repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!isActive) setForeground(new Color(160, 195, 230));
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.isActive = active;
        setForeground(active ? Color.WHITE : new Color(160, 195, 230));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight();

        if (isActive) {
            g2.setPaint(new GradientPaint(
                0, 0, new Color(63, 101, 147, 95),
                w, 0, new Color(91, 134, 182, 45)
            ));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

            g2.setPaint(new GradientPaint(
                0, 0, new Color(128, 190, 255),
                0, h, new Color(63, 130, 210)
            ));
            g2.fill(new RoundRectangle2D.Float(0, 5, 3, h - 10, 3, 3));

            g2.setColor(new Color(255, 255, 255, 12));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 10, 10));

        } else if (getModel().isRollover()) {
            g2.setColor(new Color(91, 134, 182, 22));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));
        }

        int padL = 16;
        Font iconFont = pickIconFont(iconStr, 15);
        g2.setFont(iconFont);
        FontMetrics iconFm = g2.getFontMetrics();
        int baseline = (h + iconFm.getAscent() - iconFm.getDescent()) / 2;
        g2.setColor(getForeground());
        g2.drawString(iconStr, padL, baseline);
        int iconW = iconFm.stringWidth(iconStr);

        Font textFont = isActive
            ? new Font("Segoe UI", Font.BOLD,  13)
            : new Font("Segoe UI", Font.PLAIN, 13);
        g2.setFont(textFont);
        FontMetrics textFm = g2.getFontMetrics();
        int textBaseline = (h + textFm.getAscent() - textFm.getDescent()) / 2;
        g2.drawString(labelText, padL + iconW + 9, textBaseline);

        g2.dispose();
    }

    private static Font pickIconFont(String s, int size) {
        if (s == null || s.isEmpty()) return new Font("Segoe UI", Font.PLAIN, size);
        String[] candidates = {"Segoe UI Symbol", "Segoe UI Emoji", "Segoe UI"};
        int cp = s.codePointAt(0);
        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, size);
            if (f.canDisplay(cp)) return f;
        }
        return new Font("Segoe UI", Font.PLAIN, size);
    }
}

class LegendPanel extends JPanel {
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

class MemoryBar extends JPanel {
    private MemoryBlock block;
    private float animationProgress = 1.0f;

    private static final Color ALLOC_TOP  = new Color(91, 134, 182);
    private static final Color ALLOC_BOT  = new Color(40,  90, 140);
    private static final Color FREE_TOP   = new Color(52, 211, 153);
    private static final Color FREE_BOT   = new Color(22, 163,  74);
    private static final Color FRAG_TOP   = new Color(251, 191,  36);
    private static final Color FRAG_BOT   = new Color(180, 120,   0);

    public MemoryBar() {
        setOpaque(false);
    }

    public void setBlock(MemoryBlock block, float animationProgress) {
        this.block = block;
        this.animationProgress = animationProgress;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (block == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        int w = getWidth(), h = getHeight(), arc = 12;

        if (!block.isAllocated) {
            g2.setPaint(new GradientPaint(0, 0, FREE_TOP, 0, h, FREE_BOT));
        } else {
            g2.setColor(new Color(30, 50, 70));
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        if (block.isAllocated) {
            int allocatedKB = block.size - block.internalFragmentation;
            float fillRatio  = (float) allocatedKB / block.size;
            int   fillWidth  = (int) (w * fillRatio * animationProgress);

            if (fillWidth > 0) {
                g2.setPaint(new GradientPaint(0, 0, ALLOC_TOP, 0, h, ALLOC_BOT));
                g2.fill(new RoundRectangle2D.Float(0, 0, fillWidth, h, arc, arc));

                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 0),
                                               fillWidth, 0, new Color(255, 255, 255, 25)));
                g2.fill(new RoundRectangle2D.Float(0, 0, fillWidth, h / 2, arc, arc));
            }

            if (block.internalFragmentation > 0 && animationProgress > 0.7f) {
                float alpha = Math.min(1.0f, (animationProgress - 0.7f) / 0.3f);
                int   fragW = (int) (w * ((float) block.internalFragmentation / block.size));

                GradientPaint fragGrad = new GradientPaint(
                    fillWidth, 0, blend(FRAG_TOP, alpha),
                    fillWidth, h, blend(FRAG_BOT, alpha)
                );
                g2.setPaint(fragGrad);
                g2.fill(new RoundRectangle2D.Float(fillWidth, 0, fragW + arc, h, arc, arc));
                g2.setColor(blend(FRAG_TOP, alpha));
                g2.fillRect(fillWidth, 0, arc, h);
            }
        }

        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 40), 0, h / 3, new Color(255, 255, 255, 0)));
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h / 3, arc, arc));

        g2.setColor(new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));

        String sizeText  = block.size + " KB";
        String procText  = block.isAllocated ? "P" + block.processId : "Free";

        Font boldFont   = ModernUI.FONT_LABEL.deriveFont(Font.BOLD, 13f);
        Font smallFont  = ModernUI.FONT_LABEL.deriveFont(Font.PLAIN, 11f);

        int tx = 12, ty = h / 2;

        g2.setFont(boldFont);
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(sizeText, tx + 1, ty);
        g2.setFont(smallFont);
        g2.drawString(procText, tx + 1, ty + 15);

        g2.setColor(Color.WHITE);
        g2.setFont(boldFont);
        g2.drawString(sizeText, tx, ty - 1);
        g2.setFont(smallFont);
        g2.setColor(new Color(220, 240, 255, 200));
        g2.drawString(procText, tx, ty + 14);

        g2.dispose();
    }

    private static Color blend(Color base, float alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), (int)(255 * alpha));
    }
}

class MemoryVisualizerPanel extends RoundedPanel {
    private List<MemoryBlock> blocks = new ArrayList<>();
    private float animationProgress = 1.0f;
    private Timer timer;
    private JPanel blocksContainer;

    public MemoryVisualizerPanel() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel("\uD83D\uDDC4");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel title = new JLabel(" Memory Visualization");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ModernUI.TEXT_PRIMARY);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(icon);
        titleRow.add(title);

        header.add(titleRow,       BorderLayout.WEST);
        header.add(new LegendPanel(), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        blocksContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                g2.setPaint(new GradientPaint(0, 0, ModernUI.BG_PRIMARY,
                                              0, getHeight(), new Color(10, 28, 54)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        blocksContainer.setOpaque(false);
        blocksContainer.setLayout(null);

        JScrollPane scroll = new JScrollPane(blocksContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void updateBlocks(List<MemoryBlock> newBlocks, boolean animate) {
        this.blocks = newBlocks;
        if (animate) {
            startAnimation();
        } else {
            animationProgress = 1.0f;
            renderBlocks();
        }
    }

    private void startAnimation() {
        if (timer != null && timer.isRunning()) timer.stop();
        animationProgress = 0.0f;

        timer = new Timer(16, new ActionListener() {
            long startTime = -1;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startTime == -1) startTime = System.currentTimeMillis();
                long elapsed = System.currentTimeMillis() - startTime;
                float raw = elapsed / 900.0f;
                float t = raw - 1;
                animationProgress = t * t * t + 1;
                if (elapsed >= 900) {
                    animationProgress = 1.0f;
                    timer.stop();
                }
                renderBlocks();
            }
        });
        timer.start();
    }

    private void renderBlocks() {
        blocksContainer.removeAll();
        if (blocks.isEmpty()) { blocksContainer.repaint(); return; }

        int maxBlockSize = blocks.stream().mapToInt(b -> b.size).max().orElse(1);
        int containerW   = Math.max(blocksContainer.getWidth() - 40, 800);
        int padding      = 16;
        int barH         = 60;
        int gap          = 38;
        int y            = padding;

        for (MemoryBlock block : blocks) {
            int barW = (int)(((double) block.size / maxBlockSize) * (containerW - padding * 2));
            barW = Math.max(barW, 60);

            MemoryBar bar = new MemoryBar();
            bar.setBounds(padding, y, barW, barH);
            bar.setBlock(block, animationProgress);
            blocksContainer.add(bar);

            String badgeText = "Block " + block.id;
            JLabel badge = new JLabel(badgeText);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setForeground(ModernUI.HIGHLIGHT);
            badge.setBounds(padding + barW + 10, y + (barH - 16) / 2, 80, 16);
            blocksContainer.add(badge);

            String info = String.format(
                "Size: %d KB  |  Process: %s  |  Remaining: %d KB  |  Status: %s  |  Int.Frag: %d KB",
                block.size,
                block.isAllocated ? "P" + block.processId : "—",
                block.remainingSize,
                block.isAllocated ? "Allocated" : "Free",
                block.internalFragmentation
            );
            JLabel infoLbl = new JLabel(info);
            infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            infoLbl.setForeground(new Color(128, 170, 211, 180));
            infoLbl.setBounds(padding, y + barH + 4, containerW, 18);
            blocksContainer.add(infoLbl);

            y += barH + gap;
        }

        blocksContainer.setPreferredSize(new Dimension(containerW + 40, y + padding));
        blocksContainer.revalidate();
        blocksContainer.repaint();
    }
}

class ModernTable extends JTable {
    public ModernTable(DefaultTableModel model) {
        super(model);
        setBackground(ModernUI.CARD_BG);
        setForeground(ModernUI.TEXT_PRIMARY);
        setFont(ModernUI.FONT_TABLE);
        setRowHeight(40);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionBackground(ModernUI.BG_PRIMARY);
        setSelectionForeground(ModernUI.TEXT_PRIMARY);
        
        JTableHeader header = getTableHeader();
        header.setBackground(ModernUI.BG_SECONDARY);
        header.setForeground(ModernUI.HIGHLIGHT);
        header.setFont(ModernUI.FONT_SECTION);
        header.setPreferredSize(new Dimension(0, 45));
        
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ModernUI.CARD_BG : new Color(30, 60, 95));
                }
                ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }
}

class ProcessQueuePanel extends RoundedPanel {
    private static final Color COL_CHIP_BORD = new Color(91, 134, 182, 160);
    private static final Color COL_ALLOC     = new Color(34, 197, 94,  210);
    private static final Color COL_REJECT    = new Color(239, 68,  68,  210);
    private static final Color COL_WAITING   = new Color(63, 101, 147, 200);
    private static final Color COL_ARROW     = new Color(128, 170, 211, 180);

    private static final int CHIP_W  = 82;
    private static final int CHIP_H  = 38;
    private static final int CHIP_GAP = 10;
    private static final int STEP_MS  = 950;
    private static final int FPS      = 60;

    private List<Process> processes  = new ArrayList<>();
    private Set<Integer> allocatedIds = new HashSet<>();

    private int   currentStep   = -1;
    private float stepProgress  = 1.0f;
    private Timer animTimer;

    private final Canvas canvas;

    public ProcessQueuePanel() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel icon = new JLabel("\u23F1");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JLabel title = new JLabel("  Process Queue Animation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ModernUI.TEXT_PRIMARY);
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(icon);
        titleRow.add(title);
        header.add(titleRow, BorderLayout.WEST);
        header.add(buildLegend(), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        canvas = new Canvas();
        add(canvas, BorderLayout.CENTER);
    }

    public void startAnimation(AllocationResult allResult) {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();

        processes = new ArrayList<>(allResult.processes);
        allocatedIds.clear();
        for (Process p : allResult.processes) {
            if (p.isAllocated) allocatedIds.add(p.id);
        }

        currentStep  = 0;
        stepProgress = 0.0f;

        long[] startTime = { -1L };

        animTimer = new Timer(1000 / FPS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startTime[0] == -1) startTime[0] = System.currentTimeMillis();
                long elapsed = System.currentTimeMillis() - startTime[0];

                int step   = (int)(elapsed / STEP_MS);
                float raw  = (elapsed % STEP_MS) / (float) STEP_MS;

                float t = raw < 0.5f
                        ? 4 * raw * raw * raw
                        : 1 - (float)Math.pow(-2 * raw + 2, 3) / 2;

                currentStep  = step;
                stepProgress = t;

                if (step >= processes.size()) {
                    currentStep  = processes.size();
                    stepProgress = 1.0f;
                    animTimer.stop();
                }

                canvas.repaint();
            }
        });
        animTimer.start();
    }

    public void reset() {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        processes.clear();
        allocatedIds.clear();
        currentStep  = -1;
        stepProgress = 1.0f;
        canvas.repaint();
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        p.add(legendDot(COL_ALLOC,   "Allocated"));
        p.add(legendDot(COL_REJECT,  "Rejected"));
        p.add(legendDot(COL_WAITING, "Waiting"));
        return p;
    }

    private JPanel legendDot(Color c, String label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(0, 4, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(12, 18));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(ModernUI.TEXT_SECONDARY);
        row.add(dot);
        row.add(lbl);
        return row;
    }

    private class Canvas extends JPanel {
        Canvas() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            ModernUI.applyQualityRenderingHints(g2);

            int W = getWidth();
            int H = getHeight();

            if (processes.isEmpty()) {
                drawEmpty(g2, W, H);
                g2.dispose();
                return;
            }

            int total = processes.size();

            int qW    = (int)(W * 0.36);
            int resX  = (int)(W * 0.64);
            int resW  = W - resX - 4;

            drawZoneBg(g2, 0,    0, qW,   H);
            drawZoneBg(g2, resX, 0, resW, H);

            drawLabel(g2, "QUEUE",  12,       10, ModernUI.HIGHLIGHT);
            drawLabel(g2, "RESULT", resX + 8, 10, ModernUI.HIGHLIGHT);

            int stepsDone = Math.min(currentStep, total);
            boolean animating = (currentStep < total && stepProgress < 1.0f);

            int queueFrom = stepsDone + (animating ? 1 : 0);
            drawQueue(g2, queueFrom, total, 8, 26, qW - 12, H - 30);

            drawArrow(g2, qW + 6, H / 2, resX - 6, H / 2);

            if (animating && currentStep < total) {
                drawFlying(g2, currentStep, stepProgress, qW, H / 2, resX, H / 2, H);
            }

            drawResults(g2, stepsDone, resX + 8, 26, resW - 14, H - 30);

            g2.dispose();
        }

        private void drawEmpty(Graphics2D g2, int W, int H) {
            g2.setColor(new Color(255, 255, 255, 30));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            String msg = "Run Simulation to see the queue animation \u25B6";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2 + 4);
        }

        private void drawZoneBg(Graphics2D g2, int x, int y, int w, int h) {
            g2.setColor(new Color(0, 12, 30, 130));
            g2.fill(new RoundRectangle2D.Float(x + 2, y + 2, w - 4, h - 4, 12, 12));
            g2.setColor(new Color(91, 134, 182, 45));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(x + 2, y + 2, w - 4, h - 4, 12, 12));
        }

        private void drawLabel(Graphics2D g2, String text, int x, int y, Color c) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(c);
            g2.drawString(text, x, y + 10);
        }

        private void drawQueue(Graphics2D g2, int from, int to,
                               int zx, int zy, int zw, int zh) {
            int count = to - from;
            if (count <= 0) {
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                g2.setColor(new Color(128, 170, 211, 80));
                String empty = "empty";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(empty, zx + (zw - fm.stringWidth(empty)) / 2, zy + zh / 2);
                return;
            }

            int maxVis = Math.max(1, zh / (CHIP_H + CHIP_GAP));
            maxVis = Math.min(maxVis, count);
            int totalH = maxVis * CHIP_H + (maxVis - 1) * CHIP_GAP;
            int startY = zy + (zh - totalH) / 2;

            for (int i = 0; i < maxVis; i++) {
                int idx = from + i;
                if (idx >= 0 && idx < processes.size()) {
                    Process p = processes.get(idx);
                    int cx = zx + (zw - CHIP_W) / 2;
                    int cy = startY + i * (CHIP_H + CHIP_GAP);
                    float alpha = Math.max(0.35f, 1.0f - i * 0.14f);
                    drawChip(g2, cx, cy, p, COL_WAITING, alpha, i == 0);
                }
            }

            if (count > maxVis) {
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                g2.setColor(new Color(128, 170, 211, 130));
                String more = "+" + (count - maxVis) + " more";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(more, zx + (zw - fm.stringWidth(more)) / 2,
                              zy + zh - 2);
            }
        }

        private void drawResults(Graphics2D g2, int count,
                                 int zx, int zy, int zw, int zh) {
            if (count <= 0) return;
            int maxVis = Math.max(1, zh / (CHIP_H + CHIP_GAP));
            int startIdx = Math.max(0, count - maxVis);
            int visible  = count - startIdx;
            int totalH   = visible * CHIP_H + (visible - 1) * CHIP_GAP;
            int startY   = zy + (zh - totalH) / 2;

            for (int i = startIdx; i < count; i++) {
                if (i >= 0 && i < processes.size()) {
                    Process p    = processes.get(i);
                    boolean alloc = allocatedIds.contains(p.id);
                    Color col    = alloc ? COL_ALLOC : COL_REJECT;
                    int cx = zx + (zw - CHIP_W) / 2;
                    int cy = startY + (i - startIdx) * (CHIP_H + CHIP_GAP);
                    drawChip(g2, cx, cy, p, col, 0.9f, false);

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g2.setColor(col);
                    String sym = alloc ? "\u2713" : "\u2717";
                    g2.drawString(sym, cx + CHIP_W + 5, cy + CHIP_H / 2 + 5);
                }
            }
        }

        private void drawFlying(Graphics2D g2, int procIdx, float t,
                                int fromX, int fromY,
                                int toX,   int toY, int H) {
            if (procIdx < 0 || procIdx >= processes.size()) return;
            Process p   = processes.get(procIdx);
            boolean alloc = allocatedIds.contains(p.id);
            Color col   = alloc ? COL_ALLOC : COL_REJECT;

            float lerpX = fromX + (toX - fromX) * t;
            float arc   = -(H * 0.28f) * (float)Math.sin(Math.PI * t);
            float lerpY = fromY + (toY - fromY) * t + arc;

            float scale = 1.0f + 0.20f * (float)Math.sin(Math.PI * t);
            int cw = (int)(CHIP_W * scale);
            int ch = (int)(CHIP_H * scale);

            float alpha = t < 0.15f ? t / 0.15f
                         : t > 0.85f ? (1.0f - t) / 0.15f : 1.0f;
            alpha = Math.max(0.1f, alpha);

            int cx = (int)(lerpX - cw / 2f);
            int cy = (int)(lerpY - ch / 2f);

            Color glowCol = alloc ? new Color(34, 197, 94, 55)
                                  : new Color(239, 68, 68, 55);
            for (int g = 4; g > 0; g--) {
                Composite old2 = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
                g2.setColor(glowCol);
                g2.fillRoundRect(cx - g * 3, cy - g * 2, cw + g * 6, ch + g * 4, 14, 14);
                g2.setComposite(old2);
            }

            drawChip(g2, cx, cy, p, col, alpha, true);
        }

        private void drawChip(Graphics2D g2, int x, int y, Process p,
                              Color bg, float alpha, boolean highlight) {
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g2.setColor(bg);
            g2.fillRoundRect(x, y, CHIP_W, CHIP_H, 10, 10);

            g2.setStroke(highlight ? new BasicStroke(1.8f) : new BasicStroke(1f));
            g2.setColor(highlight ? new Color(200, 220, 255, 200) : COL_CHIP_BORD);
            g2.drawRoundRect(x, y, CHIP_W, CHIP_H, 10, 10);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            String lbl = "P" + p.id;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, x + (CHIP_W - fm.stringWidth(lbl)) / 2, y + CHIP_H / 2 - 1);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(new Color(220, 235, 255, 200));
            String sz = p.size + " KB";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(sz, x + (CHIP_W - fm2.stringWidth(sz)) / 2, y + CHIP_H / 2 + 11);

            g2.setComposite(old);
        }

        private void drawArrow(Graphics2D g2, int x1, int y, int x2, int y2) {
            g2.setColor(COL_ARROW);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                                         BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
            g2.drawLine(x1, y, x2 - 12, y2);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(COL_ARROW);
            int[] xp = {x2 - 12, x2, x2 - 12};
            int[] yp = {y2 - 7,  y2, y2 + 7};
            g2.fillPolygon(xp, yp, 3);
        }
    }
}

class ResultsTabPane extends RoundedPanel {
    private JTabbedPane tabbedPane;
    private ModernTable firstFitTable;
    private ModernTable bestFitTable;
    private ModernTable comparisonTable;
    
    private DefaultTableModel ffModel;
    private DefaultTableModel bfModel;
    private DefaultTableModel compModel;

    public ResultsTabPane() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernUI.FONT_LABEL.deriveFont(Font.BOLD, 14f));
        tabbedPane.setBackground(ModernUI.CARD_BG);
        tabbedPane.setForeground(ModernUI.TEXT_PRIMARY);
        
        UIManager.put("TabbedPane.contentAreaColor", ModernUI.BG_PRIMARY);
        UIManager.put("TabbedPane.selected", ModernUI.ACCENT_BLUE);
        UIManager.put("TabbedPane.borderHightlightColor", ModernUI.CARD_BG);
        UIManager.put("TabbedPane.darkShadow", ModernUI.CARD_BG);
        UIManager.put("TabbedPane.shadow", ModernUI.CARD_BG);
        UIManager.put("TabbedPane.light", ModernUI.CARD_BG);
        UIManager.put("TabbedPane.highlight", ModernUI.CARD_BG);
        UIManager.put("TabbedPane.focus", ModernUI.CARD_BG);

        ffModel = createResultModel();
        bfModel = createResultModel();
        compModel = createComparisonModel();

        firstFitTable = new ModernTable(ffModel);
        bestFitTable = new ModernTable(bfModel);
        comparisonTable = new ModernTable(compModel);

        tabbedPane.addTab("First Fit", createScroll(firstFitTable));
        tabbedPane.addTab("Best Fit", createScroll(bestFitTable));
        tabbedPane.addTab("Comparative Analysis", createScroll(comparisonTable));
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private DefaultTableModel createResultModel() {
        return new DefaultTableModel(
            new Object[]{"Process", "Process Size", "Allocated Block", "Block Size", "Fragmentation", "Status", "Memory Left"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    
    private DefaultTableModel createComparisonModel() {
        return new DefaultTableModel(
            new Object[]{"Metric", "First Fit", "Best Fit", "Winner"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    
    private JScrollPane createScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ModernUI.CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }
    
    public void updateResults(AllocationResult ff, AllocationResult bf) {
        populateResultModel(ffModel, ff);
        populateResultModel(bfModel, bf);
        populateComparisonModel(compModel, ff, bf);
    }
    
    private void populateResultModel(DefaultTableModel model, AllocationResult res) {
        model.setRowCount(0);
        for (Process p : res.processes) {
            if (p.isAllocated) {
                int blockId = res.allocationMap.get(p.id);
                MemoryBlock block = res.memoryBlocks.stream().filter(b -> b.id == blockId).findFirst().orElse(null);
                int frag = block != null ? block.internalFragmentation : 0;
                int bSize = block != null ? block.size : 0;
                int memLeft = block != null ? block.remainingSize : 0;
                model.addRow(new Object[]{
                    "P" + p.id, p.size + " KB", "Block " + blockId, bSize + " KB", frag + " KB", "Allocated", memLeft + " KB"
                });
            } else {
                model.addRow(new Object[]{
                    "P" + p.id, p.size + " KB", "-", "-", "-", "Not Allocated", "-"
                });
            }
        }
    }
    
    private void populateComparisonModel(DefaultTableModel model, AllocationResult ff, AllocationResult bf) {
        model.setRowCount(0);
        
        int ffTotalFrag = ff.totalExternalFragmentation + ff.totalInternalFragmentation;
        int bfTotalFrag = bf.totalExternalFragmentation + bf.totalInternalFragmentation;
        
        addComparison(model, "Total Ext Frag", ff.totalExternalFragmentation, bf.totalExternalFragmentation, " KB", true);
        addComparison(model, "Total Int Frag", ff.totalInternalFragmentation, bf.totalInternalFragmentation, " KB", true);
        addComparison(model, "Total Frag", ffTotalFrag, bfTotalFrag, " KB", true);
        
        double ffUtil = ff.totalMemory > 0 ? (ff.usedMemory * 100.0 / ff.totalMemory) : 0;
        double bfUtil = bf.totalMemory > 0 ? (bf.usedMemory * 100.0 / bf.totalMemory) : 0;
        
        model.addRow(new Object[]{"Memory Utilization", String.format("%.2f%%", ffUtil), String.format("%.2f%%", bfUtil), ffUtil > bfUtil ? "First Fit" : (bfUtil > ffUtil ? "Best Fit" : "Tie")});
    }
    
    private void addComparison(DefaultTableModel model, String metric, int ffVal, int bfVal, String suffix, boolean lowerIsBetter) {
        String winner = "Tie";
        if (ffVal != bfVal) {
            if (lowerIsBetter) {
                winner = ffVal < bfVal ? "First Fit" : "Best Fit";
            } else {
                winner = ffVal > bfVal ? "First Fit" : "Best Fit";
            }
        }
        model.addRow(new Object[]{metric, ffVal + suffix, bfVal + suffix, winner});
    }
}

class ResultsPage extends RoundedPanel {
    private ResultsTabPane resultsTabPane;

    public ResultsPage() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel("\uD83D\uDCCA");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Simulation Results");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ModernUI.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("First Fit vs Best Fit — run a simulation first to populate these tables.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(ModernUI.TEXT_SECONDARY);

        textCol.add(title);
        textCol.add(Box.createRigidArea(new Dimension(0, 4)));
        textCol.add(subtitle);

        header.add(icon);
        header.add(textCol);
        add(header, BorderLayout.NORTH);

        resultsTabPane = new ResultsTabPane();
        add(resultsTabPane, BorderLayout.CENTER);
    }

    public ResultsTabPane getResultsTabPane() { return resultsTabPane; }

    public void updateResults(AllocationResult ff, AllocationResult bf) {
        resultsTabPane.updateResults(ff, bf);
    }
}

class SettingsPage extends RoundedPanel {
    private JComboBox<String> algorithmCombo;
    private JSlider animSpeedSlider;
    private JCheckBox showFragCheckbox;
    private JCheckBox showLabelsCheckbox;

    public SettingsPage() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel("\u2699\uFE0F");
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

    public int  getAnimationDurationMs()     { return animSpeedSlider.getValue(); }
    public boolean isShowFragmentation()     { return showFragCheckbox.isSelected(); }
    public boolean isShowLabels()            { return showLabelsCheckbox.isSelected(); }
}

class SummaryCardsPanel extends JPanel {
    private DashboardCard memUtilCard;
    private DashboardCard extFragCard;
    private DashboardCard intFragCard;
    private DashboardCard totFragCard;
    private DashboardCard allocBlocksCard;
    private DashboardCard unallocProcCard;

    public SummaryCardsPanel() {
        setOpaque(false);
        setLayout(new GridLayout(1, 6, ModernUI.GAP_CARDS, 0));

        memUtilCard    = new DashboardCard("Memory Util",  "0%",     "\uD83D\uDCBB", ModernUI.ACCENT_BLUE);
        extFragCard    = new DashboardCard("Ext Frag",     "0 KB",   "\uD83D\uDDFA", ModernUI.ORANGE);
        intFragCard    = new DashboardCard("Int Frag",     "0 KB",   "\u26A0",       ModernUI.WARNING);
        totFragCard    = new DashboardCard("Total Frag",   "0 KB",   "\uD83D\uDCCA", ModernUI.SUCCESS);
        allocBlocksCard = new DashboardCard("Alloc Blocks","0",      "\u2705",       ModernUI.PURPLE);
        unallocProcCard = new DashboardCard("Unalloc Proc","0",      "\u274C",       ModernUI.DANGER);

        add(memUtilCard);
        add(extFragCard);
        add(intFragCard);
        add(totFragCard);
        add(allocBlocksCard);
        add(unallocProcCard);
    }

    public void updateStats(double util, int extFrag, int intFrag,
                            int totFrag, int allocBlocks, int unallocProc) {
        memUtilCard.setValue(String.format("%.1f%%", util));
        extFragCard.setValue(extFrag + " KB");
        intFragCard.setValue(intFrag + " KB");
        totFragCard.setValue(totFrag + " KB");
        allocBlocksCard.setValue(String.valueOf(allocBlocks));
        unallocProcCard.setValue(String.valueOf(unallocProc));
    }
}

final class LeftSidebar extends JPanel {
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

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(BorderFactory.createEmptyBorder(24, 14, 14, 14));

        col.add(buildLogoRow());
        col.add(vgap(18));

        col.add(buildDivider());
        col.add(vgap(12));

        col.add(sectionLabel("NAVIGATION"));
        col.add(vgap(6));

        navDash    = new SidebarButton("Dashboard", "\uD83D\uDDC2");
        navResults = new SidebarButton("Results",   "\uD83D\uDCCA");
        navDash.setActive(true);
        navDash.setAlignmentX(LEFT_ALIGNMENT);
        navResults.setAlignmentX(LEFT_ALIGNMENT);
        col.add(navDash);
        col.add(vgap(4));
        col.add(navResults);
        col.add(vgap(18));

        col.add(sectionLabel("ALGORITHM"));
        col.add(vgap(8));
        algoToggle = new AlgorithmToggle();
        algoToggle.setAlignmentX(LEFT_ALIGNMENT);
        col.add(algoToggle);
        col.add(vgap(20));

        col.add(sectionLabel("MEMORY BLOCKS"));
        col.add(vgap(8));

        numBlocksField = new RoundedTextField("5");
        RoundedButton setMemBtn = new RoundedButton("Set", "set", ModernUI.ACCENT_BLUE);
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

        col.add(sectionLabel("PROCESSES"));
        col.add(vgap(8));

        numProcessesField = new RoundedTextField("4");
        RoundedButton setProcBtn = new RoundedButton("Set", "set", ModernUI.ACCENT_BLUE);
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

        setMemBtn.addActionListener(e -> generateFields(numBlocksField.getText(), blocksContainer, "Block"));
        setProcBtn.addActionListener(e -> generateFields(numProcessesField.getText(), processesContainer, "Process"));

        generateFields("5", blocksContainer, "Block");
        generateFields("4", processesContainer, "Process");

        add(col, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(3, 1, 0, 8));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(10, 14, 20, 14));

        runBtn     = new RoundedButton("Run Simulation", "play", ModernUI.SUCCESS);
        clearBtn   = new RoundedButton("Clear All",      "x", ModernUI.ORANGE);
        exampleBtn = new RoundedButton("Load Example",   "star", ModernUI.ACCENT_BLUE);

        actions.add(runBtn);
        actions.add(clearBtn);
        actions.add(exampleBtn);
        add(actions, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);

        g2.setPaint(new GradientPaint(
            0, 0,            new Color(15, 33, 60),
            0, getHeight(),  new Color(8, 18, 38)
        ));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(91, 134, 182, 30));
        g2.fillRect(getWidth() - 1, 0, 1, getHeight());

        g2.dispose();
        super.paintComponent(g);
    }

    private JPanel buildLogoRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        JLabel badgeIcon = new JLabel("\uD83D\uDDA5", SwingConstants.CENTER);
        badgeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        badge.add(badgeIcon, BorderLayout.CENTER);

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

    public List<Integer> getBlockSizes()    { return getValuesFrom(blocksContainer); }
    public List<Integer> getProcessSizes()  { return getValuesFrom(processesContainer); }

    private List<Integer> getValuesFrom(JPanel c) {
        List<Integer> list = new ArrayList<>();
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

    public String getSelectedAlgorithm()            { return algoToggle.getSelected(); }

    public void setActiveNav(SidebarButton active) {
        for (SidebarButton btn : new SidebarButton[]{navDash, navResults}) {
            btn.setActive(btn == active);
        }
    }

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
                        g2.setPaint(new GradientPaint(
                            0, 0, new Color(63, 101, 147),
                            0, h, new Color(30, 70, 120)
                        ));
                        g2.fillRoundRect(0, 0, w, h, 10, 10);
                        g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,40), 0, h/2, new Color(255,255,255,0)));
                        g2.fillRoundRect(1, 1, w-2, h/2, 10, 10);
                    } else {
                        g2.setColor(new Color(255, 255, 255, 8));
                        g2.fillRoundRect(0, 0, w, h, 10, 10);
                    }

                    g2.setColor(active
                        ? new Color(91, 134, 182, 180)
                        : new Color(91, 134, 182, 50));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, w-1, h-1, 10, 10);

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

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(80, 32);
                }
            };
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
                @Override public void mouseExited (MouseEvent e) { btn.repaint(); }
            });
            return btn;
        }

        public String getSelected() { return selected; }
    }
}

class MainDashboardView extends JFrame {

    public static final String PAGE_DASHBOARD = "DASHBOARD";
    public static final String PAGE_RESULTS   = "RESULTS";

    private LeftSidebar           sidebar;
    private SummaryCardsPanel     summaryCards;
    private MemoryVisualizerPanel visualizerPanel;
    private ProcessQueuePanel     queuePanel;
    private ResultsPage           resultsPage;

    private JPanel      contentArea;
    private CardLayout  cardLayout;

    public MainDashboardView() {
        setTitle("Memory Allocator \u2014 Pro Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 1000);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setPaint(new GradientPaint(0, 0, ModernUI.BG_PRIMARY,
                                              getWidth(), getHeight(), new Color(5, 20, 45)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);

        sidebar = new LeftSidebar();
        root.add(sidebar, BorderLayout.WEST);

        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        JPanel dashboardPage = buildDashboardPage();
        contentArea.add(dashboardPage, PAGE_DASHBOARD);

        resultsPage = new ResultsPage();
        JPanel resultsWrapper = wrapPage(resultsPage);
        contentArea.add(resultsWrapper, PAGE_RESULTS);

        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER,
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER));

        summaryCards = new SummaryCardsPanel();
        summaryCards.setPreferredSize(new Dimension(0, 135));
        page.add(summaryCards, BorderLayout.NORTH);

        queuePanel      = new ProcessQueuePanel();
        visualizerPanel = new MemoryVisualizerPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queuePanel, visualizerPanel);
        split.setOpaque(false);
        split.setDividerSize(6);
        split.setResizeWeight(0.32);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerLocation(0.32);
        split.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(new Color(91, 134, 182, 60));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
        page.add(split, BorderLayout.CENTER);

        return page;
    }

    private JPanel wrapPage(JPanel inner) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER,
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER));
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    public void navigateTo(String pageKey) {
        cardLayout.show(contentArea, pageKey);
    }

    public LeftSidebar           getSidebar()         { return sidebar; }
    public SummaryCardsPanel     getSummaryCards()    { return summaryCards; }
    public MemoryVisualizerPanel getVisualizerPanel() { return visualizerPanel; }
    public ProcessQueuePanel     getQueuePanel()      { return queuePanel; }
    public ResultsPage           getResultsPage()     { return resultsPage; }
}
