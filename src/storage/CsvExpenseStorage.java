package storage;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Expense;

public class CsvExpenseStorage {
    private static final String HEADER = "date,category,amount,note";

    public void save(Path path, List<Expense> expenses) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (Expense expense : expenses) {
            lines.add(
                escape(expense.getDate().toString()) + "," +
                escape(expense.getCategory()) + "," +
                escape(expense.getAmount().toPlainString()) + "," +
                escape(expense.getNote())
            );
        }

        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public List<Expense> load(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Expense> expenses = new ArrayList<>();

        if (lines.isEmpty()) {
            return expenses;
        }

        int startLine = 0;
        if (HEADER.equalsIgnoreCase(lines.get(0).trim())) {
            startLine = 1;
        }

        for (int i = startLine; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }

            List<String> values = parseCsvLine(line);
            if (values.size() != 4) {
                throw new IOException("Malformed CSV at line " + (i + 1) + ": expected 4 columns.");
            }

            LocalDate date;
            BigDecimal amount;
            try {
                date = LocalDate.parse(values.get(0).trim());
            } catch (DateTimeParseException ex) {
                throw new IOException("Malformed CSV at line " + (i + 1) + ": invalid date.", ex);
            }

            try {
                amount = new BigDecimal(values.get(2).trim());
            } catch (NumberFormatException ex) {
                throw new IOException("Malformed CSV at line " + (i + 1) + ": invalid amount.", ex);
            }

            try {
                expenses.add(new Expense(date, values.get(1), amount, values.get(3)));
            } catch (IllegalArgumentException ex) {
                throw new IOException("Malformed CSV at line " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }

        return expenses;
    }

    private String escape(String value) {
        String safeValue = value == null ? "" : value;
        boolean mustQuote = safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n");
        String escaped = safeValue.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }

    private List<String> parseCsvLine(String line) throws IOException {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (inQuotes) {
            throw new IOException("Malformed CSV: unclosed quote.");
        }

        fields.add(current.toString());
        return fields;
    }
}
