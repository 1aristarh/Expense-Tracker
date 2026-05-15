package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Expense {
    private final LocalDate date;
    private final String category;
    private final BigDecimal amount;
    private final String note;

    public Expense(LocalDate date, String category, BigDecimal amount, String note) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        this.date = date;
        this.category = category.trim();
        this.amount = amount;
        this.note = note == null ? "" : note.trim();
    }

    public static Expense fromInput(String dateText, String categoryText, String amountText, String noteText) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(dateText == null ? "" : dateText.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format.");
        }

        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amountText == null ? "" : amountText.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }

        return new Expense(parsedDate, categoryText, parsedAmount, noteText);
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }
}
