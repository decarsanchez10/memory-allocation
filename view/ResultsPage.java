package view;

import model.AllocationResult;
import javax.swing.*;
import java.awt.*;

/**
 * ResultsPage – a full-page view of the comparison tables,
 * shown when the "Results" sidebar nav button is clicked.
 */
public class ResultsPage extends RoundedPanel {

    private ResultsTabPane resultsTabPane;

    public ResultsPage() {
        super(ModernUI.RADIUS_ROUND, ModernUI.CARD_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ── Page header ──────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel("\uD83D\uDCCA");   // 📊
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

        // ── Shared results pane (same object exposed to controller) ──────
        resultsTabPane = new ResultsTabPane();
        add(resultsTabPane, BorderLayout.CENTER);
    }

    public ResultsTabPane getResultsTabPane() { return resultsTabPane; }

    public void updateResults(AllocationResult ff, AllocationResult bf) {
        resultsTabPane.updateResults(ff, bf);
    }
}
