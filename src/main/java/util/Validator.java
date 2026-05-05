package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;


public class Validator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ISO_LOCAL_DATE;

    private Validator() {}

    public static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isPositiveInteger(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.isBlank()) return false;
        try {
            LocalDate.parse(date.trim(), DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isDueDateAfterBorrowDate(String borrowDate, String dueDate) {
        if (!isValidDate(borrowDate) || !isValidDate(dueDate)) return false;
        LocalDate borrow = LocalDate.parse(borrowDate.trim(), DATE_FORMATTER);
        LocalDate due   = LocalDate.parse(dueDate.trim(), DATE_FORMATTER);
        return due.isAfter(borrow);
    }

    public static boolean isValidMembershipType(String type) {
        return "Student".equalsIgnoreCase(type) || "Staff".equalsIgnoreCase(type);
    }

    public static boolean isValidAvailabilityStatus(String status) {
        return "Available".equalsIgnoreCase(status) || "Borrowed".equalsIgnoreCase(status);
    }

    public static boolean isValidReturnStatus(String status) {
        return "Borrowed".equalsIgnoreCase(status)
            || "Returned".equalsIgnoreCase(status)
            || "Overdue".equalsIgnoreCase(status);
    }
}

