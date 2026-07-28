package backup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MemoryPanel extends JPanel {
    private List<MemoryBlock> blocks = new ArrayList<>();
    private float animationProgress = 1.0f; // 0.0 to 1.0
    private Timer animationTimer;
    private static final int ANIMATION_DURATION_MS = 600;
    private static final int TIMER_DELAY_MS = 16;
    
    public MemoryPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(800, 300));
    }
    
    public void updateBlocks(List<MemoryBlock> newBlocks, boolean animate) {
        this.blocks = newBlocks;
        if (animate) {
            startAnimation();
        } else {
            animationProgress = 1.0f;
            repaint();
        }
    }
    
    private void startAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationProgress = 0.0f;
        
        animationTimer = new Timer(TIMER_DELAY_MS, new ActionListener() {
            private long startTime = -1;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startTime == -1) {
                    startTime = System.currentTimeMillis();
                }
                long elapsed = System.currentTimeMillis() - startTime;
                animationProgress = (float) elapsed / ANIMATION_DURATION_MS;
                
                // Ease out cubic
                float t = animationProgress - 1;
                animationProgress = t * t * t + 1;
                
                if (elapsed >= ANIMATION_DURATION_MS) {
                    animationProgress = 1.0f;
                    animationTimer.stop();
                }
                repaint();
            }
        });
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (blocks == null || blocks.isEmpty()) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        ModernUI.applyQualityRenderingHints(g2);
        
        int padding = 20;
        int barHeight = 60;
        int verticalSpacing = 20;
        int startY = padding;
        
        int totalWidth = getWidth() - (padding * 2);
        
        // Calculate total memory to scale the blocks properly
        int totalMemorySize = 0;
        for (MemoryBlock b : blocks) {
            totalMemorySize += b.size;
        }
        
        if (totalMemorySize == 0) {
            g2.dispose();
            return;
        }

        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlock block = blocks.get(i);
            
            int y = startY + (i * (barHeight + verticalSpacing));
            
            // Draw background of block (total size)
            g2.setColor(ModernUI.CARD_BACKGROUND);
            g2.fill(new RoundRectangle2D.Float(padding, y, totalWidth, barHeight, 10, 10));
            
            // Calculate width based on proportion of total memory or just take full width 
            // Actually, usually memory visualization either shows all blocks as equal width
            // or proportional. Let's make them proportional to block.size if we want a continuous look,
            // or just full width but showing allocation inside. 
            // The prompt says "Display memory blocks as horizontal colored bars." 
            // Let's use the full width for each block and partition it internally.
            
            // Draw Free Space (Full block size)
            g2.setColor(ModernUI.COLOR_FREE);
            g2.fill(new RoundRectangle2D.Float(padding, y, totalWidth, barHeight, 10, 10));
            
            if (block.isAllocated) {
                int allocatedSize = block.size - block.internalFragmentation;
                
                // Proportion of the block used by the process
                float fillRatio = (float) allocatedSize / block.size;
                int fillWidth = (int) (totalWidth * fillRatio * animationProgress);
                
                // Allocated portion
                g2.setColor(block.color != null ? block.color : ModernUI.ACCENT);
                g2.fill(new RoundRectangle2D.Float(padding, y, fillWidth, barHeight, 10, 10));
                
                // Internal Fragmentation
                if (block.internalFragmentation > 0 && animationProgress > 0.8f) {
                    float fragAlpha = (animationProgress - 0.8f) * 5.0f; // 0 to 1 over the last 20%
                    if (fragAlpha > 1.0f) fragAlpha = 1.0f;
                    
                    int fragWidth = (int) (totalWidth * ((float)block.internalFragmentation / block.size));
                    int fragStartX = padding + fillWidth;
                    
                    Color fragColor = ModernUI.COLOR_INTERNAL_FRAG;
                    g2.setColor(new Color(fragColor.getRed(), fragColor.getGreen(), fragColor.getBlue(), (int)(fragColor.getAlpha() * fragAlpha)));
                    g2.fill(new RoundRectangle2D.Float(fragStartX, y, fragWidth, barHeight, 10, 10));
                }
            }
            
            // Draw Texts
            g2.setColor(ModernUI.TEXT_PRIMARY);
            g2.setFont(ModernUI.FONT_MEDIUM);
            String blockLabel = "Block " + block.id + " (" + block.size + " KB)";
            g2.drawString(blockLabel, padding + 10, y + 25);
            
            g2.setFont(ModernUI.FONT_SMALL);
            g2.setColor(ModernUI.TEXT_SECONDARY);
            if (block.isAllocated) {
                String processLabel = "Allocated: P" + block.processId + " | Rem: " + block.remainingSize + " KB | Int. Frag: " + block.internalFragmentation + " KB";
                g2.drawString(processLabel, padding + 10, y + 45);
            } else {
                g2.drawString("Status: Free", padding + 10, y + 45);
            }
        }
        
        // Adjust preferred size dynamically based on number of blocks
        int requiredHeight = startY + (blocks.size() * (barHeight + verticalSpacing)) + padding;
        if (getPreferredSize().height != requiredHeight) {
            setPreferredSize(new Dimension(800, requiredHeight));
            revalidate();
        }
        
        g2.dispose();
    }
}
