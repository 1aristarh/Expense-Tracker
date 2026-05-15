package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Expense;
import service.ExpenseService;
import storage.CsvExpenseStorage;

public class ExpenseTrackerFrame extends JFrame {
    private final ExpenseService expenseService = new ExpenseService();
    private final CsvExpenseStorage csvExpenseStorage = new CsvExpenseStorage();

    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField categoryField = new JTextField(12);
    private final JTextField amountField = new JTextField(10);
    private final JTextField noteField = new JTextField(18);
    private final JComboBox<String> categoryFilterCombo = new JComboBox<>(new String[] {"All"});
    private final JLabel totalLabel = new JLabel("Total: 0.00");

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[] {"Date", "Category", "Amount", "Note"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final Path csvPath = Paths.get("expenses.csv");

    public ExpenseTrackerFrame() {
        setTitle("Expense Tracker");
        setSize(820, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        inputPanel.add(dateField, gbc);

        gbc.gridx = 2;
        inputPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 3;
        inputPanel.add(categoryField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(amountField, gbc);

        gbc.gridx = 2;
        inputPanel.add(new JLabel("Note:"), gbc);

        gbc.gridx = 3;
        inputPanel.add(noteField, gbc);

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(e -> addExpense());

        JButton seedButton = new JButton("Add Sample Data");
        seedButton.addActionListener(e -> addSampleData());

        JButton saveButton = new JButton("Save CSV");
        saveButton.addActionListener(e -> saveCsv());

        JButton loadButton = new JButton("Load CSV");
        loadButton.addActionListener(e -> loadCsv());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(addButton);
        buttonsPanel.add(seedButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(loadButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        inputPanel.add(buttonsPanel, gbc);

        return inputPanel;
    }

    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by category:"));
        categoryFilterCombo.addActionListener(e -> refreshTableAndTotal());
        filterPanel.add(categoryFilterCombo);

        bottomPanel.add(filterPanel, BorderLayout.WEST);
        bottomPanel.add(totalLabel, BorderLayout.EAST);
        return bottomPanel;
    }

    private void addExpense() {
        try {
            Expense expense = Expense.fromInput(
                dateField.getText(),
                categoryField.getText(),
                amountField.getText(),
                noteField.getText()
            );
            expenseService.addExpense(expense);
            amountField.setText("");
            noteField.setText("");
            refreshFilterOptions();
            refreshTableAndTotal();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void addSampleData() {
        try {
            expenseService.addExpense(Expense.fromInput(LocalDate.now().minusDays(1).toString(), "Food", "12.50", "Lunch"));
            expenseService.addExpense(Expense.fromInput(LocalDate.now().toString(), "Transport", "3.20", "Bus ticket"));
            expenseService.addExpense(Expense.fromInput(LocalDate.now().minusDays(2).toString(), "Study", "25.00", "Book"));
            refreshFilterOptions();
            refreshTableAndTotal();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void saveCsv() {
        try {
            csvExpenseStorage.save(csvPath, expenseService.getAllExpenses());
            JOptionPane.showMessageDialog(this, "Saved to " + csvPath.toAbsolutePath());
        } catch (IOException ex) {
            showError("Failed to save CSV: " + ex.getMessage());
        }
    }

    private void loadCsv() {
        try {
            List<Expense> loadedExpenses = csvExpenseStorage.load(csvPath);
            expenseService.replaceExpenses(loadedExpenses);
            refreshFilterOptions();
            refreshTableAndTotal();
            JOptionPane.showMessageDialog(this, "Loaded from " + csvPath.toAbsolutePath());
        } catch (IOException ex) {
            showError("Failed to load CSV: " + ex.getMessage());
        }
    }

    private void refreshFilterOptions() {
        String selected = (String) categoryFilterCombo.getSelectedItem();
        categoryFilterCombo.removeAllItems();
        categoryFilterCombo.addItem("All");
        for (String category : expenseService.getCategories()) {
            categoryFilterCombo.addItem(category);
        }
        categoryFilterCombo.setSelectedItem(selected == null ? "All" : selected);
        if (categoryFilterCombo.getSelectedItem() == null) {
            categoryFilterCombo.setSelectedItem("All");
        }
    }

    private void refreshTableAndTotal() {
        String filter = (String) categoryFilterCombo.getSelectedItem();
        List<Expense> rows = expenseService.getFilteredExpenses(filter);

        tableModel.setRowCount(0);
        for (Expense expense : rows) {
            tableModel.addRow(
                new Object[] {
                    expense.getDate(),
                    expense.getCategory(),
                    expense.getAmount().toPlainString(),
                    expense.getNote()
                }
            );
        }

        BigDecimal total = expenseService.getTotalAmount(filter);
        totalLabel.setText("Total: " + total.toPlainString());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
