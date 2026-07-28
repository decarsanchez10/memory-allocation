package view;

import model.MemoryBlock;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * MemoryVisualizerPanel with a gradient header, improved typography,
 * and proportional MemoryBar layout with info labels.
 */
public class MemoryVisualizerPanel extends RoundedPanel {
    private List<MemoryBlock> blocks = new ArrayList<>();
    private float animationProgress = 1.0f;
    private Timer timer;
    private JPanel blocksContainer;

    public MemoryVisualizerPanel() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── Header ───────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        // Icon + title
        JLabel icon = new JLabel("\uD83D\uDDC4");   // 🗄
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

        // ── Blocks canvas ────────────────────────────────────────────────
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
                animationProgress = t * t * t + 1;   // cubic ease-out
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

            // ── Bar ──────────────────────────────────────────────────────
            MemoryBar bar = new MemoryBar();
            bar.setBounds(padding, y, barW, barH);
            bar.setBlock(block, animationProgress);
            blocksContainer.add(bar);

            // ── Block name badge on the right ────────────────────────────
            String badgeText = "Block " + block.id;
            JLabel badge = new JLabel(badgeText);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setForeground(ModernUI.HIGHLIGHT);
            badge.setBounds(padding + barW + 10, y + (barH - 16) / 2, 80, 16);
            blocksContainer.add(badge);

            // ── Info label ───────────────────────────────────────────────
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
