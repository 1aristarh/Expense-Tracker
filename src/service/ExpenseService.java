package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Expense;

public class ExpenseService {
    private final List<Expense> expenses = new ArrayList<>();

    public void addExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense is required.");
        }
        expenses.add(expense);
    }

    public void replaceExpenses(List<Expense> newExpenses) {
        if (newExpenses == null) {
            throw new IllegalArgumentException("Expenses are required.");
        }
        expenses.clear();
        expenses.addAll(newExpenses);
    }

    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public List<Expense> getFilteredExpenses(String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isBlank() || "All".equals(categoryFilter)) {
            return new ArrayList<>(expenses);
        }

        List<Expense> filtered = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense.getCategory().equalsIgnoreCase(categoryFilter)) {
                filtered.add(expense);
            }
        }
        return filtered;
    }

    public BigDecimal getTotalAmount(String categoryFilter) {
        BigDecimal total = BigDecimal.ZERO;
        for (Expense expense : getFilteredExpenses(categoryFilter)) {
            total = total.add(expense.getAmount());
        }
        return total;
    }

    public Set<String> getCategories() {
        Set<String> categories = new LinkedHashSet<>();
        for (Expense expense : expenses) {
            categories.add(expense.getCategory());
        }
        return categories;
    }
}
