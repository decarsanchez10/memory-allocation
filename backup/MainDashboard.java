package backup;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainDashboard extends JFrame {
    
    private ControlPanel controlPanel;
    private MemoryPanel memoryVisualizationPanel;
    private ResultsPanel resultsPanel;
    
    private DashboardCard memUtilCard;
    private DashboardCard extFragCard;
    private DashboardCard intFragCard;
    private DashboardCard totFragCard;
    private DashboardCard allocBlocksCard;
    private DashboardCard unallocProcCard;
    
    public MainDashboard() {
        setTitle("Memory Allocation Simulator Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 950);
        setLocationRelativeTo(null);
        
        // Root panel
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(ModernUI.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Left side: Controls
        controlPanel = new ControlPanel();
        controlPanel.setPreferredSize(new Dimension(320, 0));
        root.add(controlPanel, BorderLayout.WEST);
        
        // Right side: Main Content
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);
        
        // Dashboard Stats Grid (Top of right side)
        JPanel statsGrid = new JPanel(new GridLayout(1, 6, 15, 0));
        statsGrid.setOpaque(false);
        statsGrid.setPreferredSize(new Dimension(0, 100));
        
        memUtilCard = new DashboardCard("Mem Utilization", "0%");
        extFragCard = new DashboardCard("Ext Frag", "0 KB");
        intFragCard = new DashboardCard("Int Frag", "0 KB");
        totFragCard = new DashboardCard("Total Frag", "0 KB");
        allocBlocksCard = new DashboardCard("Alloc Blocks", "0");
        unallocProcCard = new DashboardCard("Unalloc Proc", "0");
        
        statsGrid.add(memUtilCard);
        statsGrid.add(extFragCard);
        statsGrid.add(intFragCard);
        statsGrid.add(totFragCard);
        statsGrid.add(allocBlocksCard);
        statsGrid.add(unallocProcCard);
        
        mainContent.add(statsGrid, BorderLayout.NORTH);
        
        // Center: Memory Visualization & Results
        JPanel centerContent = new JPanel(new BorderLayout(15, 15));
        centerContent.setOpaque(false);
        
        // Memory Panel
        memoryVisualizationPanel = new MemoryPanel();
        JScrollPane memScroll = new JScrollPane(memoryVisualizationPanel);
        memScroll.setOpaque(false);
        memScroll.getViewport().setOpaque(false);
        memScroll.setBorder(BorderFactory.createEmptyBorder());
        memScroll.setPreferredSize(new Dimension(0, 350));
        
        RoundedPanel memContainer = new RoundedPanel(ModernUI.CORNER_RADIUS, ModernUI.CARD_BACKGROUND);
        memContainer.setLayout(new BorderLayout(10, 10));
        memContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel vizTitle = new JLabel("Memory Visualization");
        vizTitle.setFont(ModernUI.FONT_LARGE);
        vizTitle.setForeground(ModernUI.TEXT_PRIMARY);
        memContainer.add(vizTitle, BorderLayout.NORTH);
        memContainer.add(memScroll, BorderLayout.CENTER);
        
        centerContent.add(memContainer, BorderLayout.NORTH);
        
        // Results Panel
        resultsPanel = new ResultsPanel();
        centerContent.add(resultsPanel, BorderLayout.CENTER);
        
        mainContent.add(centerContent, BorderLayout.CENTER);
        
        root.add(mainContent, BorderLayout.CENTER);
        setContentPane(root);
        
        // Wire actions
        controlPanel.getRunButton().addActionListener(e -> runSimulation());
        controlPanel.getClearButton().addActionListener(e -> clearAll());
        controlPanel.getExampleButton().addActionListener(e -> loadExample());
    }
    
    private void runSimulation() {
        try {
            // Read Blocks
            List<MemoryBlock> blocks = new ArrayList<>();
            Component[] blockFields = controlPanel.getBlocksContainer().getComponents();
            int bId = 1;
            for (Component c : blockFields) {
                if (c instanceof JPanel) {
                    JPanel row = (JPanel) c;
                    JTextField tf = (JTextField) row.getComponent(1);
                    int size = Integer.parseInt(tf.getText().trim());
                    blocks.add(new MemoryBlock(bId++, size));
                }
            }
            
            // Read Processes
            List<Process> processes = new ArrayList<>();
            Component[] procFields = controlPanel.getProcessesContainer().getComponents();
            int pId = 1;
            for (Component c : procFields) {
                if (c instanceof JPanel) {
                    JPanel row = (JPanel) c;
                    JTextField tf = (JTextField) row.getComponent(1);
                    int size = Integer.parseInt(tf.getText().trim());
                    processes.add(new Process(pId++, size));
                }
            }
            
            if (blocks.isEmpty() || processes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please define blocks and processes.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Execute Algorithms
            AllocationResult ffResult = Simulator.firstFit(blocks, processes);
            AllocationResult bfResult = Simulator.bestFit(blocks, processes);
            
            // Currently visualizing Best Fit as it's often more optimized, but could allow user to toggle.
            // For now, let's visualize First Fit
            memoryVisualizationPanel.updateBlocks(ffResult.memoryBlocks, true);
            
            // Update Results Tables
            resultsPanel.updateResults(ffResult, bfResult);
            
            // Update Dashboard Cards (using First Fit as baseline display here)
            double util = ffResult.totalMemory > 0 ? (ffResult.usedMemory * 100.0 / ffResult.totalMemory) : 0;
            memUtilCard.setValue(String.format("%.1f%%", util));
            extFragCard.setValue(ffResult.totalExternalFragmentation + " KB");
            intFragCard.setValue(ffResult.totalInternalFragmentation + " KB");
            totFragCard.setValue((ffResult.totalExternalFragmentation + ffResult.totalInternalFragmentation) + " KB");
            
            long alloc = ffResult.processes.stream().filter(p -> p.isAllocated).count();
            allocBlocksCard.setValue(alloc + "");
            unallocProcCard.setValue(ffResult.unallocatedProcesses.size() + "");
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format in inputs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearAll() {
        controlPanel.getNumBlocksField().setText("");
        controlPanel.getNumProcessesField().setText("");
        controlPanel.getBlocksContainer().removeAll();
        controlPanel.getProcessesContainer().removeAll();
        controlPanel.getBlocksContainer().repaint();
        controlPanel.getProcessesContainer().repaint();
        
        memoryVisualizationPanel.updateBlocks(new ArrayList<>(), false);
        resultsPanel.updateResults(new AllocationResult(), new AllocationResult());
        
        memUtilCard.setValue("0%");
        extFragCard.setValue("0 KB");
        intFragCard.setValue("0 KB");
        totFragCard.setValue("0 KB");
        allocBlocksCard.setValue("0");
        unallocProcCard.setValue("0");
    }
    
    private void loadExample() {
        controlPanel.getNumBlocksField().setText("5");
        controlPanel.getNumProcessesField().setText("4");
        
        // This simulates clicking "Set"
        controlPanel.getBlocksContainer().removeAll();
        int[] blockSizes = {100, 500, 200, 300, 600};
        for (int i = 0; i < 5; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setOpaque(false);
            JLabel l = new JLabel("Block " + (i+1) + " (KB):");
            l.setForeground(ModernUI.TEXT_SECONDARY);
            row.add(l);
            JTextField tf = createTransparentTextField(String.valueOf(blockSizes[i]));
            row.add(tf);
            controlPanel.getBlocksContainer().add(row);
        }
        
        controlPanel.getProcessesContainer().removeAll();
        int[] procSizes = {212, 417, 112, 426};
        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setOpaque(false);
            JLabel l = new JLabel("Process " + (i+1) + " (KB):");
            l.setForeground(ModernUI.TEXT_SECONDARY);
            row.add(l);
            JTextField tf = createTransparentTextField(String.valueOf(procSizes[i]));
            row.add(tf);
            controlPanel.getProcessesContainer().add(row);
        }
        
        controlPanel.getBlocksContainer().revalidate();
        controlPanel.getProcessesContainer().revalidate();
        controlPanel.repaint();
    }
    
    private JTextField createTransparentTextField(String text) {
        JTextField field = new JTextField(text, 5) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                g2.setColor(ModernUI.BACKGROUND);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 8, 8));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        field.setForeground(ModernUI.TEXT_PRIMARY);
        field.setCaretColor(ModernUI.TEXT_PRIMARY);
        field.setFont(ModernUI.FONT_REGULAR);
        return field;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainDashboard app = new MainDashboard();
            app.setVisible(true);
        });
    }
}
