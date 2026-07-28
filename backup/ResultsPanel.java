package backup;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;


public class ResultsPanel extends RoundedPanel {
    private JTabbedPane tabbedPane;
    
    private JTable firstFitTable;
    private JTable bestFitTable;
    private JTable comparisonTable;
    
    private DefaultTableModel ffModel;
    private DefaultTableModel bfModel;
    private DefaultTableModel compModel;

    public ResultsPanel() {
        super(ModernUI.CORNER_RADIUS, ModernUI.CARD_BACKGROUND);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(ModernUI.CARD_BACKGROUND);
        tabbedPane.setForeground(ModernUI.TEXT_PRIMARY);
        tabbedPane.setFont(ModernUI.FONT_REGULAR);
        
        // Remove borders from tabbed pane
        UIManager.put("TabbedPane.borderHightlightColor", ModernUI.CARD_BACKGROUND);
        UIManager.put("TabbedPane.darkShadow", ModernUI.CARD_BACKGROUND);
        UIManager.put("TabbedPane.shadow", ModernUI.CARD_BACKGROUND);
        UIManager.put("TabbedPane.light", ModernUI.CARD_BACKGROUND);
        UIManager.put("TabbedPane.highlight", ModernUI.CARD_BACKGROUND);
        UIManager.put("TabbedPane.focus", ModernUI.CARD_BACKGROUND);
        UIManager.put("TabbedPane.selectHighlight", ModernUI.CARD_BACKGROUND);
        UIManager.put("TabbedPane.contentAreaColor", ModernUI.BACKGROUND);
        UIManager.put("TabbedPane.selected", ModernUI.SECONDARY);

        ffModel = createTableModel();
        bfModel = createTableModel();
        compModel = createComparisonTableModel();
        
        firstFitTable = createStyledTable(ffModel);
        bestFitTable = createStyledTable(bfModel);
        comparisonTable = createStyledTable(compModel);
        
        tabbedPane.addTab("First Fit", createTableScrollPane(firstFitTable));
        tabbedPane.addTab("Best Fit", createTableScrollPane(bestFitTable));
        tabbedPane.addTab("Comparison", createTableScrollPane(comparisonTable));
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
            new Object[]{"Process", "Block", "Process Size", "Block Size", "Fragmentation", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    
    private DefaultTableModel createComparisonTableModel() {
        return new DefaultTableModel(
            new Object[]{"Metric", "First Fit", "Best Fit", "Winner"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(ModernUI.CARD_BACKGROUND);
        table.setForeground(ModernUI.TEXT_PRIMARY);
        table.setFont(ModernUI.FONT_REGULAR);
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(ModernUI.SECONDARY);
        table.setSelectionForeground(ModernUI.TEXT_PRIMARY);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(ModernUI.BACKGROUND);
        header.setForeground(ModernUI.HIGHLIGHTS);
        header.setFont(ModernUI.FONT_MEDIUM);
        header.setPreferredSize(new Dimension(0, 40));
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        return table;
    }
    
    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(ModernUI.CARD_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(ModernUI.CARD_BACKGROUND);
        return scrollPane;
    }
    
    public void updateResults(AllocationResult ffResult, AllocationResult bfResult) {
        populateModel(ffModel, ffResult);
        populateModel(bfModel, bfResult);
        populateComparisonModel(compModel, ffResult, bfResult);
    }
    
    private void populateModel(DefaultTableModel model, AllocationResult result) {
        model.setRowCount(0);
        for (Process p : result.processes) {
            if (p.isAllocated) {
                int blockId = result.allocationMap.get(p.id);
                MemoryBlock allocatedBlock = null;
                for (MemoryBlock b : result.memoryBlocks) {
                    if (b.id == blockId) {
                        allocatedBlock = b;
                        break;
                    }
                }
                
                int frag = allocatedBlock != null ? allocatedBlock.internalFragmentation : 0;
                int blockSize = allocatedBlock != null ? allocatedBlock.size : 0;
                
                model.addRow(new Object[]{
                    "P" + p.id,
                    "Block " + blockId,
                    p.size + " KB",
                    blockSize + " KB",
                    frag + " KB",
                    "Allocated"
                });
            } else {
                model.addRow(new Object[]{
                    "P" + p.id,
                    "-",
                    p.size + " KB",
                    "-",
                    "-",
                    "Unallocated"
                });
            }
        }
    }
    
    private void populateComparisonModel(DefaultTableModel model, AllocationResult ff, AllocationResult bf) {
        model.setRowCount(0);
        
        addComparisonRow(model, "Total External Fragmentation", ff.totalExternalFragmentation, bf.totalExternalFragmentation, " KB", true);
        addComparisonRow(model, "Total Internal Fragmentation", ff.totalInternalFragmentation, bf.totalInternalFragmentation, " KB", true);
        
        int ffTotalFrag = ff.totalExternalFragmentation + ff.totalInternalFragmentation;
        int bfTotalFrag = bf.totalExternalFragmentation + bf.totalInternalFragmentation;
        addComparisonRow(model, "Total Fragmentation", ffTotalFrag, bfTotalFrag, " KB", true);
        
        double ffUtil = ff.totalMemory > 0 ? (ff.usedMemory * 100.0 / ff.totalMemory) : 0;
        double bfUtil = bf.totalMemory > 0 ? (bf.usedMemory * 100.0 / bf.totalMemory) : 0;
        
        model.addRow(new Object[]{
            "Memory Utilization",
            String.format("%.2f%%", ffUtil),
            String.format("%.2f%%", bfUtil),
            ffUtil > bfUtil ? "First Fit" : (bfUtil > ffUtil ? "Best Fit" : "Tie")
        });
        
        long ffAlloc = ff.processes.stream().filter(p -> p.isAllocated).count();
        long bfAlloc = bf.processes.stream().filter(p -> p.isAllocated).count();
        model.addRow(new Object[]{
            "Processes Allocated",
            ffAlloc + "/" + ff.processes.size(),
            bfAlloc + "/" + bf.processes.size(),
            ffAlloc > bfAlloc ? "First Fit" : (bfAlloc > ffAlloc ? "Best Fit" : "Tie")
        });
    }
    
    private void addComparisonRow(DefaultTableModel model, String metric, int ffVal, int bfVal, String suffix, boolean lowerIsBetter) {
        String winner = "Tie";
        if (ffVal != bfVal) {
            if (lowerIsBetter) {
                winner = ffVal < bfVal ? "First Fit" : "Best Fit";
            } else {
                winner = ffVal > bfVal ? "First Fit" : "Best Fit";
            }
        }
        
        model.addRow(new Object[]{
            metric,
            ffVal + suffix,
            bfVal + suffix,
            winner
        });
    }
}
