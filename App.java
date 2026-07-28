import com.formdev.flatlaf.FlatDarkLaf;
import controller.MainController;
import view.MainDashboardView;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        
        SwingUtilities.invokeLater(() -> {
            MainDashboardView view = new MainDashboardView();
            new MainController(view);
            view.setVisible(true);
        });
    }






}
