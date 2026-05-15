package app;

import javax.swing.SwingUtilities;
import ui.ExpenseTrackerFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpenseTrackerFrame frame = new ExpenseTrackerFrame();
            frame.setVisible(true);
        });
    }
}
