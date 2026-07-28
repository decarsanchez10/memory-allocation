package view;

import model.AllocationResult;
import model.MemoryBlock;
import model.Process;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ResultsTabPane extends RoundedPanel {
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
