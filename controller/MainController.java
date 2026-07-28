package controller;

import model.AllocationResult;
import model.MemoryBlock;
import model.Process;
import model.Simulator;
import view.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private final MainDashboardView view;

    // Keep last results so the Results page stays in sync
    private AllocationResult lastFF = new AllocationResult();
    private AllocationResult lastBF = new AllocationResult();

    public MainController(MainDashboardView view) {
        this.view = view;
        initController();
    }

    private void initController() {
        // ── Simulation actions ────────────────────────────────────────────
        view.getSidebar().getRunBtn().addActionListener(e -> runSimulation());
        view.getSidebar().getClearBtn().addActionListener(e -> clearAll());
        view.getSidebar().getExampleBtn().addActionListener(e -> loadExample());

        // ── Sidebar navigation ────────────────────────────────────────────
        LeftSidebar sb = view.getSidebar();

        sb.getNavDash().addActionListener(e -> {
            view.navigateTo(MainDashboardView.PAGE_DASHBOARD);
            sb.setActiveNav(sb.getNavDash());
        });

        sb.getNavResults().addActionListener(e -> {
            // Sync the standalone Results page with the latest simulation data
            view.getResultsPage().updateResults(lastFF, lastBF);
            view.navigateTo(MainDashboardView.PAGE_RESULTS);
            sb.setActiveNav(sb.getNavResults());
        });

        sb.getNavSettings().addActionListener(e -> {
            view.navigateTo(MainDashboardView.PAGE_SETTINGS);
            sb.setActiveNav(sb.getNavSettings());
        });
    }

    // ── Run Simulation ────────────────────────────────────────────────────
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

        // Update Dashboard page
        view.getVisualizerPanel().updateBlocks(lastFF.memoryBlocks, true);
        view.getResultsPanel().updateResults(lastFF, lastBF);

        // Also keep Results page in sync
        view.getResultsPage().updateResults(lastFF, lastBF);

        // Update summary cards
        double util  = lastFF.totalMemory > 0
                       ? (lastFF.usedMemory * 100.0 / lastFF.totalMemory) : 0;
        int alloc    = (int) lastFF.processes.stream().filter(p -> p.isAllocated).count();
        view.getSummaryCards().updateStats(
            util,
            lastFF.totalExternalFragmentation,
            lastFF.totalInternalFragmentation,
            lastFF.totalExternalFragmentation + lastFF.totalInternalFragmentation,
            alloc,
            lastFF.unallocatedProcesses.size()
        );
    }

    // ── Clear ─────────────────────────────────────────────────────────────
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
        view.getResultsPanel().updateResults(lastFF, lastBF);
        view.getResultsPage().updateResults(lastFF, lastBF);
        view.getSummaryCards().updateStats(0, 0, 0, 0, 0, 0);
    }

    // ── Load Example ──────────────────────────────────────────────────────
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
        l.setPreferredSize(new java.awt.Dimension(76, 24));
        RoundedTextField tf = new RoundedTextField(value);
        row.add(l, BorderLayout.WEST);
        row.add(tf, BorderLayout.CENTER);
        return row;
    }
}
