package backup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class ControlPanel extends RoundedPanel {

    private JTextField numBlocksField;
    private JTextField numProcessesField;
    
    private JPanel blocksContainer;
    private JPanel processesContainer;
    
    private JButton runButton;
    private JButton clearButton;
    private JButton exampleButton;

    public ControlPanel() {
        super(ModernUI.CORNER_RADIUS, ModernUI.CARD_BACKGROUND);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);
        
        // Memory Configuration
        JLabel memConfigLabel = new JLabel("Memory Configuration");
        memConfigLabel.setFont(ModernUI.FONT_MEDIUM);
        memConfigLabel.setForeground(ModernUI.TEXT_PRIMARY);
        inputPanel.add(memConfigLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel memInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        memInputRow.setOpaque(false);
        numBlocksField = createStyledTextField("3");
        JButton setBlocksBtn = createStyledButton("Set", ModernUI.SECONDARY, e -> buildBlockFields());
        memInputRow.add(new JLabel("Blocks:"));
        memInputRow.add(numBlocksField);
        memInputRow.add(setBlocksBtn);
        inputPanel.add(memInputRow);
        
        blocksContainer = new JPanel();
        blocksContainer.setLayout(new BoxLayout(blocksContainer, BoxLayout.Y_AXIS));
        blocksContainer.setOpaque(false);
        JScrollPane blocksScroll = new JScrollPane(blocksContainer);
        blocksScroll.setPreferredSize(new Dimension(250, 150));
        blocksScroll.setOpaque(false);
        blocksScroll.getViewport().setOpaque(false);
        blocksScroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        inputPanel.add(blocksScroll);
        
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Process Configuration
        JLabel procConfigLabel = new JLabel("Process Configuration");
        procConfigLabel.setFont(ModernUI.FONT_MEDIUM);
        procConfigLabel.setForeground(ModernUI.TEXT_PRIMARY);
        inputPanel.add(procConfigLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel procInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        procInputRow.setOpaque(false);
        numProcessesField = createStyledTextField("4");
        JButton setProcessesBtn = createStyledButton("Set", ModernUI.SECONDARY, e -> buildProcessFields());
        procInputRow.add(new JLabel("Processes:"));
        procInputRow.add(numProcessesField);
        procInputRow.add(setProcessesBtn);
        inputPanel.add(procInputRow);
        
        processesContainer = new JPanel();
        processesContainer.setLayout(new BoxLayout(processesContainer, BoxLayout.Y_AXIS));
        processesContainer.setOpaque(false);
        JScrollPane processesScroll = new JScrollPane(processesContainer);
        processesScroll.setPreferredSize(new Dimension(250, 150));
        processesScroll.setOpaque(false);
        processesScroll.getViewport().setOpaque(false);
        processesScroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        inputPanel.add(processesScroll);
        
        add(inputPanel, BorderLayout.CENTER);
        
        // Bottom Controls
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        runButton = createStyledButton("Run Simulation", new Color(39, 174, 96), null);
        clearButton = createStyledButton("Clear", new Color(230, 126, 34), null);
        exampleButton = createStyledButton("Load Example", ModernUI.ACCENT, null);
        
        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exampleButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize with default example structure
        buildBlockFields();
        buildProcessFields();
    }
    
    private void buildBlockFields() {
        blocksContainer.removeAll();
        try {
            int n = Integer.parseInt(numBlocksField.getText());
            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setOpaque(false);
                JLabel l = new JLabel("Block " + (i+1) + " (KB):");
                l.setForeground(ModernUI.TEXT_SECONDARY);
                row.add(l);
                row.add(createStyledTextField("100"));
                blocksContainer.add(row);
            }
        } catch(Exception ignored) {}
        blocksContainer.revalidate();
        blocksContainer.repaint();
    }
    
    private void buildProcessFields() {
        processesContainer.removeAll();
        try {
            int n = Integer.parseInt(numProcessesField.getText());
            for (int i = 0; i < n; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setOpaque(false);
                JLabel l = new JLabel("Process " + (i+1) + " (KB):");
                l.setForeground(ModernUI.TEXT_SECONDARY);
                row.add(l);
                row.add(createStyledTextField("50"));
                processesContainer.add(row);
            }
        } catch(Exception ignored) {}
        processesContainer.revalidate();
        processesContainer.repaint();
    }

    private JTextField createStyledTextField(String defaultText) {
        JTextField field = new JTextField(defaultText, 5) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                g2.setColor(ModernUI.BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 8, 8));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        field.setForeground(ModernUI.TEXT_PRIMARY);
        field.setCaretColor(ModernUI.TEXT_PRIMARY);
        field.setFont(ModernUI.FONT_REGULAR);
        return field;
    }
    
    private JButton createStyledButton(String text, Color bg, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernUI.applyQualityRenderingHints(g2);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(ModernUI.FONT_MEDIUM);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 40));
        if (action != null) {
            btn.addActionListener(action);
        }
        return btn;
    }

    public JButton getRunButton() { return runButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getExampleButton() { return exampleButton; }
    public JPanel getBlocksContainer() { return blocksContainer; }
    public JPanel getProcessesContainer() { return processesContainer; }
    public JTextField getNumBlocksField() { return numBlocksField; }
    public JTextField getNumProcessesField() { return numProcessesField; }
}
