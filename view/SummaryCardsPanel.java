package view;

import javax.swing.*;
import java.awt.*;

/**
 * SummaryCardsPanel – six DashboardCards with improved icons and accent colours.
 */
public class SummaryCardsPanel extends JPanel {
    private DashboardCard memUtilCard;
    private DashboardCard extFragCard;
    private DashboardCard intFragCard;
    private DashboardCard totFragCard;
    private DashboardCard allocBlocksCard;
    private DashboardCard unallocProcCard;

    public SummaryCardsPanel() {
        setOpaque(false);
        setLayout(new GridLayout(1, 6, ModernUI.GAP_CARDS, 0));

        memUtilCard    = new DashboardCard("Memory Util",  "0%",     "\uD83D\uDCBB", ModernUI.ACCENT_BLUE);  // 💻
        extFragCard    = new DashboardCard("Ext Frag",     "0 KB",   "\uD83D\uDDFA", ModernUI.ORANGE);        // 🗺
        intFragCard    = new DashboardCard("Int Frag",     "0 KB",   "\u26A0",       ModernUI.WARNING);       // ⚠
        totFragCard    = new DashboardCard("Total Frag",   "0 KB",   "\uD83D\uDCCA", ModernUI.SUCCESS);       // 📊
        allocBlocksCard = new DashboardCard("Alloc Blocks","0",      "\u2705",       ModernUI.PURPLE);        // ✅
        unallocProcCard = new DashboardCard("Unalloc Proc","0",      "\u274C",       ModernUI.DANGER);        // ❌

        add(memUtilCard);
        add(extFragCard);
        add(intFragCard);
        add(totFragCard);
        add(allocBlocksCard);
        add(unallocProcCard);
    }

    public void updateStats(double util, int extFrag, int intFrag,
                            int totFrag, int allocBlocks, int unallocProc) {
        memUtilCard.setValue(String.format("%.1f%%", util));
        extFragCard.setValue(extFrag + " KB");
        intFragCard.setValue(intFrag + " KB");
        totFragCard.setValue(totFrag + " KB");
        allocBlocksCard.setValue(String.valueOf(allocBlocks));
        unallocProcCard.setValue(String.valueOf(unallocProc));
    }
}
