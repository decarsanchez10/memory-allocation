package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MainDashboardView – root JFrame.
 *
 * The centre area uses a CardLayout so that clicking the sidebar nav buttons
 * properly swaps between "DASHBOARD", "RESULTS", and "SETTINGS" pages.
 * The controller wires the buttons; the view just exposes navigateTo(String).
 */
public class MainDashboardView extends JFrame {

    // ── Page keys (used with CardLayout) ─────────────────────────────────
    public static final String PAGE_DASHBOARD = "DASHBOARD";
    public static final String PAGE_RESULTS   = "RESULTS";
    public static final String PAGE_SETTINGS  = "SETTINGS";

    // ── Components ────────────────────────────────────────────────────────
    private LeftSidebar           sidebar;
    private SummaryCardsPanel     summaryCards;
    private MemoryVisualizerPanel visualizerPanel;
    private ResultsTabPane        resultsPanel;      // on Dashboard page
    private ResultsPage           resultsPage;       // standalone Results page
    private SettingsPage          settingsPage;

    private JPanel      contentArea;
    private CardLayout  cardLayout;

    public MainDashboardView() {
        setTitle("Memory Allocation Simulator \u2014 Pro Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 1000);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        // ── Gradient root ─────────────────────────────────────────────────
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

        // ── Sidebar ───────────────────────────────────────────────────────
        sidebar = new LeftSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // ── CardLayout content area ───────────────────────────────────────
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // ── Dashboard page ────────────────────────────────────────────────
        JPanel dashboardPage = buildDashboardPage();
        contentArea.add(dashboardPage, PAGE_DASHBOARD);

        // ── Results page ──────────────────────────────────────────────────
        resultsPage = new ResultsPage();
        JPanel resultsWrapper = wrapPage(resultsPage);
        contentArea.add(resultsWrapper, PAGE_RESULTS);

        // ── Settings page ─────────────────────────────────────────────────
        settingsPage = new SettingsPage();
        JPanel settingsWrapper = wrapPage(settingsPage);
        contentArea.add(settingsWrapper, PAGE_SETTINGS);

        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Dashboard page assembly ───────────────────────────────────────────
    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER,
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER));

        // Top stat cards
        summaryCards = new SummaryCardsPanel();
        summaryCards.setPreferredSize(new Dimension(0, 135));
        page.add(summaryCards, BorderLayout.NORTH);

        // Visualizer (top) + Results table (bottom), draggable
        visualizerPanel = new MemoryVisualizerPanel();
        resultsPanel    = new ResultsTabPane();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visualizerPanel, resultsPanel);
        split.setDividerLocation(370);
        split.setResizeWeight(0.45);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(6);

        page.add(split, BorderLayout.CENTER);
        return page;
    }

    /** Wraps a page panel with outer margin padding. */
    private JPanel wrapPage(JPanel inner) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER,
            ModernUI.MARGIN_OUTER, ModernUI.MARGIN_OUTER));
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Navigation ────────────────────────────────────────────────────────
    public void navigateTo(String pageKey) {
        cardLayout.show(contentArea, pageKey);
    }

    // ── Accessors for controller ──────────────────────────────────────────
    public LeftSidebar           getSidebar()         { return sidebar; }
    public SummaryCardsPanel     getSummaryCards()    { return summaryCards; }
    public MemoryVisualizerPanel getVisualizerPanel() { return visualizerPanel; }
    public ResultsTabPane        getResultsPanel()    { return resultsPanel; }
    public ResultsPage           getResultsPage()     { return resultsPage; }
    public SettingsPage          getSettingsPage()    { return settingsPage; }
}
