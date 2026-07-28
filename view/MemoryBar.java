package view;

import model.MemoryBlock;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MemoryBar renders a single memory block as a horizontal bar with:
 *  - Gradient-filled allocated segment (blue gradient)
 *  - Gradient-filled free segment (green gradient)
 *  - Animated internal-frag segment (yellow)
 *  - Centred text overlay with shadow for readability
 */
public class MemoryBar extends JPanel {
    private MemoryBlock block;
    private float animationProgress = 1.0f;

    // Per-segment gradient colour pairs
    private static final Color ALLOC_TOP  = new Color(91, 134, 182);   // #5B86B6
    private static final Color ALLOC_BOT  = new Color(40,  90, 140);
    private static final Color FREE_TOP   = new Color(52, 211, 153);
    private static final Color FREE_BOT   = new Color(22, 163,  74);   // #22C55E
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

        // ── 1. Background (free / empty base) ───────────────────────────
        if (!block.isAllocated) {
            // Entirely free → green gradient
            g2.setPaint(new GradientPaint(0, 0, FREE_TOP, 0, h, FREE_BOT));
        } else {
            // Allocated → dark gray base first (will be covered by segments)
            g2.setColor(new Color(30, 50, 70));
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        if (block.isAllocated) {
            int allocatedKB = block.size - block.internalFragmentation;
            float fillRatio  = (float) allocatedKB / block.size;
            int   fillWidth  = (int) (w * fillRatio * animationProgress);

            // ── 2. Allocated segment (blue gradient) ───────────────────
            if (fillWidth > 0) {
                g2.setPaint(new GradientPaint(0, 0, ALLOC_TOP, 0, h, ALLOC_BOT));
                g2.fill(new RoundRectangle2D.Float(0, 0, fillWidth, h, arc, arc));

                // Subtle shimmer stripe: transparent → white gloss → transparent (2-stop)
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 0),
                                               fillWidth, 0, new Color(255, 255, 255, 25)));
                g2.fill(new RoundRectangle2D.Float(0, 0, fillWidth, h / 2, arc, arc));
            }

            // ── 3. Internal-fragmentation segment (yellow gradient) ────
            if (block.internalFragmentation > 0 && animationProgress > 0.7f) {
                float alpha = Math.min(1.0f, (animationProgress - 0.7f) / 0.3f);
                int   fragW = (int) (w * ((float) block.internalFragmentation / block.size));

                GradientPaint fragGrad = new GradientPaint(
                    fillWidth, 0, blend(FRAG_TOP, alpha),
                    fillWidth, h, blend(FRAG_BOT, alpha)
                );
                g2.setPaint(fragGrad);
                g2.fill(new RoundRectangle2D.Float(fillWidth, 0, fragW + arc, h, arc, arc));
                // Re-clip the left edge square
                g2.setColor(blend(FRAG_TOP, alpha));
                g2.fillRect(fillWidth, 0, arc, h);
            }
        }

        // ── 4. Inner top-highlight (glassy feel) ────────────────────────
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 40), 0, h / 3, new Color(255, 255, 255, 0)));
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h / 3, arc, arc));

        // ── 5. Border ────────────────────────────────────────────────────
        g2.setColor(new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));

        // ── 6. Text overlay ───────────────────────────────────────────────
        String sizeText  = block.size + " KB";
        String procText  = block.isAllocated ? "P" + block.processId : "Free";

        Font boldFont   = ModernUI.FONT_LABEL.deriveFont(Font.BOLD, 13f);
        Font smallFont  = ModernUI.FONT_LABEL.deriveFont(Font.PLAIN, 11f);

        // Shadow pass
        g2.setFont(boldFont);
        FontMetrics fm = g2.getFontMetrics();
        int tx = 12, ty = h / 2;

        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(sizeText, tx + 1, ty);
        g2.setFont(smallFont);
        g2.drawString(procText, tx + 1, ty + 15);

        // Actual text
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
