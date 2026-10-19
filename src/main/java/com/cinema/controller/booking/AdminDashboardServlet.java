package com.cinema.controller.booking;

import com.cinema.dao.admin.StatisticsDAO;
import com.cinema.dto.ChartPointDTO;
import com.cinema.dto.CinemaRevenueDTO;
import com.cinema.dto.DashboardSummaryDTO;
import com.cinema.dto.MovieRevenueDTO;
import com.cinema.dto.RevenueRecordDTO;
import com.cinema.dto.SeatFillDTO;
import com.cinema.enums.BookingStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
                compareLabel = "so với cùng giờ hôm qua";
                break;
            case "month":
                from = today.withDayOfMonth(1).atStartOfDay();
                to = from.plusMonths(1);
                prevFrom = from.minusMonths(1);
                bucketUnit = ChronoUnit.DAYS;
                chartTitle = "📈 Doanh Thu Theo Ngày (Tháng " + today.getMonthValue() + "/" + today.getYear() + ")";
                compareLabel = "so với cùng kỳ tháng trước";
                break;
            case "year":
                from = today.withDayOfYear(1).atStartOfDay();
                to = from.plusYears(1);
                prevFrom = from.minusYears(1);
                bucketUnit = ChronoUnit.MONTHS;
                chartTitle = "📈 Doanh Thu Theo Tháng (Năm " + today.getYear() + ")";
                compareLabel = "so với cùng kỳ năm " + (today.getYear() - 1);
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

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        double prevRevenue = 0;
        SeatFillDTO seatFill = new SeatFillDTO();
        List<CinemaRevenueDTO> cinemaRevenue = new ArrayList<>();
        List<MovieRevenueDTO> topMovies = new ArrayList<>();

        try {
            // 3. Đọc số liệu từ bookings (chỉ đọc) qua DTO
            summary = statisticsDAO.getSummary(PAID, from, to);
            // Kỳ trước chỉ lấy đúng khoảng thời gian bằng phần đã trôi qua của kỳ này
            LocalDateTime cutoff = LocalDateTime.now().isBefore(to) ? LocalDateTime.now() : to;
            LocalDateTime prevTo = prevFrom.plus(java.time.Duration.between(from, cutoff));
            prevRevenue = statisticsDAO.getSummary(PAID, prevFrom, prevTo).getTotalRevenue();
            seatFill = statisticsDAO.getSeatFill(PAID, from, to);

            for (RevenueRecordDTO record : statisticsDAO.getPaidBookingRecords(PAID, from, to)) {
                LocalDateTime key = truncate(record.getCreatedAt(), bucketUnit);
                buckets.merge(key, record.getTotalPrice(), Double::sum);
            }

            cinemaRevenue = statisticsDAO.getRevenueByCinema(PAID, from, to);

            topMovies = statisticsDAO.getTopMovies(PAID, from, to, 5);
            for (MovieRevenueDTO m : topMovies) {
                double percent = summary.getTotalRevenue() > 0 ? m.getRevenue() * 100.0 / summary.getTotalRevenue() : 0.0;
                m.setContributionPercent(String.format(Locale.US, "%.1f", percent));
            }
        } catch (RuntimeException e) {
            System.err.println("[ADMIN DASHBOARD] Lỗi khi đọc số liệu thống kê: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Không thể tải số liệu thống kê. Vui lòng thử lại sau.");
        }

        // 4. Định dạng dữ liệu cho JSP
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        df.setGroupingUsed(true);

        List<ChartPointDTO> chartPoints = new ArrayList<>();
        for (Map.Entry<LocalDateTime, Double> e : buckets.entrySet()) {
            // Biểu đồ hiển thị theo triệu VNĐ
            chartPoints.add(new ChartPointDTO(bucketLabel(e.getKey(), bucketUnit),
                    String.format(Locale.US, "%.2f", e.getValue() / 1_000_000)));
        }

        String growthText;
        boolean growthPositive = true;
        if (prevRevenue > 0) {
            double pct = (summary.getTotalRevenue() - prevRevenue) * 100.0 / prevRevenue;
            growthPositive = pct >= 0;
            growthText = String.format(Locale.US, "%+.1f%% ", pct) + compareLabel;
        } else {
            growthText = summary.getTotalRevenue() > 0 ? "Kỳ trước chưa có doanh thu" : "Chưa có doanh thu trong kỳ này";
        }

        request.setAttribute("period", period);
        request.setAttribute("currentYear", today.getYear());
        request.setAttribute("totalRevenueFormatted", df.format(Math.round(summary.getTicketRevenue())) + "đ");
        request.setAttribute("totalTicketsFormatted", df.format(summary.getTicketCount()));
        request.setAttribute("comboRevenueFormatted", df.format(Math.round(summary.getComboRevenue())) + "đ");
        request.setAttribute("fillRateFormatted", seatFill.getFillRateText());
        request.setAttribute("chartTitle", chartTitle);
        request.setAttribute("growthText", growthText);
        request.setAttribute("growthPositive", growthPositive);
        request.setAttribute("chartPoints", chartPoints);
        request.setAttribute("cinemaRevenue", cinemaRevenue);
        request.setAttribute("topMovies", topMovies);

        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
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
