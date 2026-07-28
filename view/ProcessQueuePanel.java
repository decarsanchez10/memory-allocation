package view;

import model.Process;
import model.AllocationResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * ProcessQueuePanel – animates process dequeue + allocation/rejection steps.
 *
 * Layout (horizontal):
 *   [QUEUE AREA]  →  [FLYING CHIP]  →  [RESULT LANE]
 *
 * On "Run Simulation", call startAnimation(allResult).
 * The panel will automatically step through each process with smooth easing.
 */
public class ProcessQueuePanel extends RoundedPanel {

    // ── Colours ──────────────────────────────────────────────────────────
    private static final Color COL_CHIP_BORD = new Color(91, 134, 182, 160);
    private static final Color COL_ALLOC     = new Color(34, 197, 94,  210);
    private static final Color COL_REJECT    = new Color(239, 68,  68,  210);
    private static final Color COL_WAITING   = new Color(63, 101, 147, 200);
    private static final Color COL_ARROW     = new Color(128, 170, 211, 180);

    // ── Sizes ────────────────────────────────────────────────────────────
    private static final int CHIP_W  = 82;
    private static final int CHIP_H  = 38;
    private static final int CHIP_GAP = 10;
    private static final int STEP_MS  = 950;
    private static final int FPS      = 60;

    // ── State ────────────────────────────────────────────────────────────
    private List<Process> processes  = new ArrayList<>();
    private java.util.Set<Integer> allocatedIds = new java.util.HashSet<>();

    private int   currentStep   = -1;
    private float stepProgress  = 1.0f;
    private Timer animTimer;

    // ── Canvas ───────────────────────────────────────────────────────────
    private final Canvas canvas;

    // ── Constructor ──────────────────────────────────────────────────────
    public ProcessQueuePanel() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel icon = new JLabel("\u23F1");  // ⏱
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

    // ── Public API ───────────────────────────────────────────────────────

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

                // Cubic ease-in-out
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

    // ── Legend ───────────────────────────────────────────────────────────
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

    // ── Inner drawing canvas ──────────────────────────────────────────────
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

            // ── Zones ────────────────────────────────────────────────────
            int qW    = (int)(W * 0.36);
            int resX  = (int)(W * 0.64);
            int resW  = W - resX - 4;

            // Zone backgrounds
            drawZoneBg(g2, 0,    0, qW,   H);
            drawZoneBg(g2, resX, 0, resW, H);

            // Zone labels
            drawLabel(g2, "QUEUE",  12,       10, ModernUI.HIGHLIGHT);
            drawLabel(g2, "RESULT", resX + 8, 10, ModernUI.HIGHLIGHT);

            // ── Steps state ───────────────────────────────────────────────
            int stepsDone = Math.min(currentStep, total);
            boolean animating = (currentStep < total && stepProgress < 1.0f);

            // Queue: chips from index = stepsDone (+ 1 if currently flying)
            int queueFrom = stepsDone + (animating ? 1 : 0);
            drawQueue(g2, queueFrom, total, 8, 26, qW - 12, H - 30);

            // Arrow
            drawArrow(g2, qW + 6, H / 2, resX - 6, H / 2);

            // Flying chip
            if (animating && currentStep < total) {
                drawFlying(g2, currentStep, stepProgress, qW, H / 2, resX, H / 2, H);
            }

            // Result lane
            drawResults(g2, stepsDone, resX + 8, 26, resW - 14, H - 30);

            g2.dispose();
        }

        // ── Helpers ───────────────────────────────────────────────────────

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
                // Empty queue indicator
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
                Process p = processes.get(idx);
                int cx = zx + (zw - CHIP_W) / 2;
                int cy = startY + i * (CHIP_H + CHIP_GAP);
                float alpha = Math.max(0.35f, 1.0f - i * 0.14f);
                drawChip(g2, cx, cy, p, COL_WAITING, alpha, i == 0);
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
            // show the latest results at top (most recent = last in list)
            int startIdx = Math.max(0, count - maxVis);
            int visible  = count - startIdx;
            int totalH   = visible * CHIP_H + (visible - 1) * CHIP_GAP;
            int startY   = zy + (zh - totalH) / 2;

            for (int i = startIdx; i < count; i++) {
                Process p    = processes.get(i);
                boolean alloc = allocatedIds.contains(p.id);
                Color col    = alloc ? COL_ALLOC : COL_REJECT;
                int cx = zx + (zw - CHIP_W) / 2;
                int cy = startY + (i - startIdx) * (CHIP_H + CHIP_GAP);
                drawChip(g2, cx, cy, p, col, 0.9f, false);

                // Status glyph
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(col);
                String sym = alloc ? "\u2713" : "\u2717";
                g2.drawString(sym, cx + CHIP_W + 5, cy + CHIP_H / 2 + 5);
            }
        }

        private void drawFlying(Graphics2D g2, int procIdx, float t,
                                int fromX, int fromY,
                                int toX,   int toY, int H) {
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

            // Glow halo
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

            // Process label
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            String lbl = "P" + p.id;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, x + (CHIP_W - fm.stringWidth(lbl)) / 2, y + CHIP_H / 2 - 1);

            // Size sub-label
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
