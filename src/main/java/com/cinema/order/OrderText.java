package com.cinema.order;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderText {

    private OrderText() {
    }

    public static String vnd(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("vi", "VN"));
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        return format.format(Math.round(amount)) + "đ";
    }

    public static String formatCombos(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!trimmed.startsWith("[")) {
            return trimmed;
        }
        String[] chunks = trimmed.split("\\},\\s*\\{");
        List<String> parts = new ArrayList<>();
        for (String chunk : chunks) {
            String name = jsonString(chunk, "name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            String qty = jsonNumber(chunk, "qty");
            if (qty == null || qty.isEmpty()) {
                qty = "1";
            }
            parts.add(name + " x " + qty);
        }
        if (parts.isEmpty()) {
            return trimmed;
        }
        return String.join(", ", parts);
    }

    public static String roomNumber(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            return "";
        }
        String digits = roomName.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return roomName.trim();
        }
        if (digits.length() == 1) {
            return "0" + digits;
        }
        return digits;
    }

    public static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String jsonString(String chunk, String field) {
        String key = "\"" + field + "\"";
        int keyAt = chunk.indexOf(key);
        if (keyAt < 0) {
            return null;
        }
        int colon = chunk.indexOf(':', keyAt + key.length());
        if (colon < 0) {
            return null;
        }
        int firstQuote = chunk.indexOf('"', colon + 1);
        if (firstQuote < 0) {
            return null;
        }
        int secondQuote = chunk.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }
        return chunk.substring(firstQuote + 1, secondQuote);
    }

    private static String jsonNumber(String chunk, String field) {
        String key = "\"" + field + "\"";
        int keyAt = chunk.indexOf(key);
        if (keyAt < 0) {
            return null;
        }
        int colon = chunk.indexOf(':', keyAt + key.length());
        if (colon < 0) {
            return null;
        }
        int start = colon + 1;
        while (start < chunk.length() && Character.isWhitespace(chunk.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < chunk.length() && Character.isDigit(chunk.charAt(end))) {
            end++;
        }
        if (end == start) {
            return null;
        }
        return chunk.substring(start, end);
    }
}
