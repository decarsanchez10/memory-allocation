package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ModernTable extends JTable {
    public ModernTable(DefaultTableModel model) {
        super(model);
        setBackground(ModernUI.CARD_BG);
        setForeground(ModernUI.TEXT_PRIMARY);
        setFont(ModernUI.FONT_TABLE);
        setRowHeight(40);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionBackground(ModernUI.BG_PRIMARY);
        setSelectionForeground(ModernUI.TEXT_PRIMARY);
        
        JTableHeader header = getTableHeader();
        header.setBackground(ModernUI.BG_SECONDARY);
        header.setForeground(ModernUI.HIGHLIGHT);
        header.setFont(ModernUI.FONT_SECTION);
        header.setPreferredSize(new Dimension(0, 45));
        
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ModernUI.CARD_BG : new Color(30, 60, 95));
                }
                ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }
}
