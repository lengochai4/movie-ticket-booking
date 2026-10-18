package com.cinema.controller.booking;

import com.cinema.dao.admin.StatisticsDAO;
import com.cinema.dao.catalog.MovieDAO;
import com.cinema.dao.catalog.ShowtimeDAO;
import com.cinema.enums.BookingStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final String PAID = BookingStatus.PAID.name();
    private static final List<String> PERIODS = Arrays.asList("today", "7days", "month", "year");

    private final StatisticsDAO statisticsDAO = new StatisticsDAO();
    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. Xác định kỳ thống kê [from, to) và kỳ liền trước [prevFrom, from) để so sánh
        String period = request.getParameter("period");
        if (period == null || !PERIODS.contains(period)) {
            period = "7days";
        }

        LocalDate today = LocalDate.now();
        LocalDateTime from;
        LocalDateTime to;
        LocalDateTime prevFrom;
        ChronoUnit bucketUnit;
        String chartTitle;
        String compareLabel;
        switch (period) {
            case "today":
                from = today.atStartOfDay();
                to = from.plusDays(1);
                prevFrom = from.minusDays(1);
                bucketUnit = ChronoUnit.HOURS;
                chartTitle = "📈 Doanh Thu Theo Giờ (Hôm Nay)";
                compareLabel = "so với hôm qua";
                break;
            case "month":
                from = today.withDayOfMonth(1).atStartOfDay();
                to = from.plusMonths(1);
                prevFrom = from.minusMonths(1);
                bucketUnit = ChronoUnit.DAYS;
                chartTitle = "📈 Doanh Thu Theo Ngày (Tháng " + today.getMonthValue() + "/" + today.getYear() + ")";
                compareLabel = "so với tháng trước";
                break;
            case "year":
                from = today.withDayOfYear(1).atStartOfDay();
                to = from.plusYears(1);
                prevFrom = from.minusYears(1);
                bucketUnit = ChronoUnit.MONTHS;
                chartTitle = "📈 Doanh Thu Theo Tháng (Năm " + today.getYear() + ")";
                compareLabel = "so với năm " + (today.getYear() - 1);
                break;
            default: // 7days
                from = today.minusDays(6).atStartOfDay();
                to = today.plusDays(1).atStartOfDay();
                prevFrom = from.minusDays(7);
                bucketUnit = ChronoUnit.DAYS;
                chartTitle = "📈 Doanh Thu 7 Ngày Gần Nhất";
                compareLabel = "so với 7 ngày trước đó";
                break;
        }

        // 2. Khung biểu đồ: mỗi giờ / ngày / tháng là 1 cột, mặc định 0đ
        LinkedHashMap<LocalDateTime, Double> buckets = new LinkedHashMap<>();
        for (LocalDateTime t = from; t.isBefore(to); t = t.plus(1, bucketUnit)) {
            buckets.put(t, 0.0);
        }

        double totalRevenue = 0, ticketRevenue = 0, comboRevenue = 0, tickets = 0;
        double prevRevenue = 0;
        double[] seatFill = {0, 0};
        List<Object[]> cinemaRows = new ArrayList<>();
        List<Map<String, Object>> topMovies = new ArrayList<>();

        try {
            // 3. Đọc số liệu từ bookings (chỉ đọc)
            double[] summary = statisticsDAO.getSummary(PAID, from, to);
            totalRevenue = summary[0];
            ticketRevenue = summary[1];
            comboRevenue = summary[2];
            tickets = summary[4];
            prevRevenue = statisticsDAO.getSummary(PAID, prevFrom, from)[0];
            seatFill = statisticsDAO.getSeatFill(PAID, from, to);

            for (Object[] row : statisticsDAO.getPaidBookingTimes(PAID, from, to)) {
                LocalDateTime createdAt = toLocalDateTime(row[0]);
                if (createdAt == null) continue;
                LocalDateTime key = truncate(createdAt, bucketUnit);
                buckets.merge(key, ((Number) row[1]).doubleValue(), Double::sum);
            }

            cinemaRows = statisticsDAO.getRevenueByCinema(PAID, from, to);

            for (Object[] row : statisticsDAO.getTopMovies(PAID, from, to, 5)) {
                double revenue = ((Number) row[7]).doubleValue();
                Map<String, Object> item = new HashMap<>();
                item.put("movieID", row[0]);
                item.put("title", row[1]);
                item.put("genre", row[2] != null ? row[2] : "Điện ảnh");
                item.put("duration", row[3]);
                item.put("posterUrl", row[4]);
                item.put("showtimesCount", ((Number) row[5]).longValue());
                item.put("ticketsSold", ((Number) row[6]).longValue());
                item.put("revenue", Math.round(revenue));
                double percent = totalRevenue > 0 ? revenue * 100.0 / totalRevenue : 0.0;
                item.put("contributionPercent", String.format(Locale.US, "%.1f", percent));
                topMovies.add(item);
            }
        } catch (RuntimeException e) {
            System.err.println("[ADMIN DASHBOARD] Lỗi khi đọc số liệu thống kê: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Không thể tải số liệu thống kê. Vui lòng thử lại sau.");
        }

        // 4. Định dạng dữ liệu cho JSP
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        df.setGroupingUsed(true);

        List<String> chartLabels = new ArrayList<>();
        List<String> chartValues = new ArrayList<>();
        for (Map.Entry<LocalDateTime, Double> e : buckets.entrySet()) {
            chartLabels.add(bucketLabel(e.getKey(), bucketUnit));
            // Biểu đồ hiển thị theo triệu VNĐ
            chartValues.add(String.format(Locale.US, "%.2f", e.getValue() / 1_000_000));
        }

        String growthText;
        boolean growthPositive = true;
        if (prevRevenue > 0) {
            double pct = (totalRevenue - prevRevenue) * 100.0 / prevRevenue;
            growthPositive = pct >= 0;
            growthText = String.format(Locale.US, "%+.1f%% ", pct) + compareLabel;
        } else {
            growthText = totalRevenue > 0 ? "Kỳ trước chưa có doanh thu" : "Chưa có doanh thu trong kỳ này";
        }

        String fillRate = seatFill[1] > 0
                ? String.format(Locale.US, "%.1f%%", seatFill[0] * 100.0 / seatFill[1])
                : "0%";

        request.setAttribute("period", period);
        request.setAttribute("currentYear", today.getYear());
        request.setAttribute("totalRevenueFormatted", df.format(Math.round(ticketRevenue)) + "đ");
        request.setAttribute("totalTicketsFormatted", df.format(Math.round(tickets)));
        request.setAttribute("comboRevenueFormatted", df.format(Math.round(comboRevenue)) + "đ");
        request.setAttribute("fillRateFormatted", fillRate);
        request.setAttribute("chartTitle", chartTitle);
        request.setAttribute("growthText", growthText);
        request.setAttribute("growthPositive", growthPositive);
        request.setAttribute("chartLabels", chartLabels);
        request.setAttribute("chartValues", chartValues);
        request.setAttribute("cinemaRevenue", cinemaRows);
        request.setAttribute("topMovies", topMovies);
        request.setAttribute("totalMoviesCount", movieDAO.findAll().size());
        request.setAttribute("totalShowtimesCount", showtimeDAO.findAll().size());

        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime();
        return null;
    }

    private LocalDateTime truncate(LocalDateTime t, ChronoUnit unit) {
        switch (unit) {
            case HOURS: return t.truncatedTo(ChronoUnit.HOURS);
            case MONTHS: return t.toLocalDate().withDayOfMonth(1).atStartOfDay();
            default: return t.truncatedTo(ChronoUnit.DAYS);
        }
    }

    private String bucketLabel(LocalDateTime t, ChronoUnit unit) {
        switch (unit) {
            case HOURS: return t.format(DateTimeFormatter.ofPattern("HH'h'"));
            case MONTHS: return "T" + t.getMonthValue();
            default: return t.format(DateTimeFormatter.ofPattern("dd/MM"));
        }
    }
}
