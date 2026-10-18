<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Bảng Điều Khiển - Cinetik Admin" scope="request"/>
<c:set var="extraCss" value="admin-users.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%---- Nhúng thư viện biểu đồ chuyên nghiệp Chart.js qua CDN ----%>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<style>
    /* Styling bộ lọc thời gian thanh lịch */
    .dashboard-filter-group {
        display: inline-flex;
        background: #171b24;
        padding: 4px;
        border-radius: 30px;
        border: 1px solid rgba(255, 255, 255, 0.12);
        gap: 4px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
    }

    .dashboard-filter-btn {
        background: transparent;
        border: none;
        color: #94a3b8;
        padding: 6px 16px;
        border-radius: 20px;
        font-size: 13px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.25s ease;
        font-family: inherit;
        outline: none;
    }

    .dashboard-filter-btn:hover {
        color: #ffffff;
        background: rgba(255, 255, 255, 0.08);
    }

    .dashboard-filter-btn.active {
        background: #fcca46;
        color: #0f1217;
        font-weight: 700;
        box-shadow: 0 2px 10px rgba(252, 202, 70, 0.35);
    }
</style>

<main class="admin-container">
    <div class="admin-layout" style="max-width: 1200px;">

        <%---- ================= 0. THANH ĐIỀU HƯỚNG QUẢN TRỊ ADMIN ================= ----%>
        <nav style="display: flex; gap: 8px; border-bottom: 1px solid #2d2824; padding-bottom: 12px; overflow-x: auto;">
            <a href="${pageContext.request.contextPath}/admin/dashboard" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%); border: 1px solid #fcca46; color: #fcca46; font-size: 13px; font-weight: 700; text-decoration: none;">
                📊 Thống Kê
            </a>
            <a href="${pageContext.request.contextPath}/admin/movies" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🎬 Quản Lý Phim
            </a>
            <a href="${pageContext.request.contextPath}/admin/showtimes" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                📅 Quản Lý Lịch Chiếu
            </a>
            <a href="${pageContext.request.contextPath}/admin/cinemas" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🏛️ Quản Lý Rạp & Phòng
            </a>
            <a href="${pageContext.request.contextPath}/admin/combos" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🍿 Quản Lý Bắp Nước
            </a>
            <a href="${pageContext.request.contextPath}/admin/bookings" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🎟️ Quản Lý Đặt Vé
            </a>
            <a href="${pageContext.request.contextPath}/admin/users" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                👥 Quản Lý Người Dùng
            </a>
        </nav>

        <%---- ================= 1. HEADER DASHBOARD ================= ----%>
        <header class="admin-header" style="align-items: center;">
            <div class="admin-header-title">
                <h1>Bảng Điều Khiển Thống Kê</h1>
                <p>Tổng quan tình hình kinh doanh, số liệu vé bán và doanh thu rạp chiếu phim</p>
            </div>
            <%---- Bộ lọc thời gian tương tác ----%>
            <div class="admin-header-actions">
                <div class="dashboard-filter-group">
                    <button type="button" class="dashboard-filter-btn" onclick="filterByPeriod('today', this)">Hôm nay</button>
                    <button type="button" class="dashboard-filter-btn active" onclick="filterByPeriod('7days', this)">7 ngày qua</button>
                    <button type="button" class="dashboard-filter-btn" onclick="filterByPeriod('month', this)">Tháng này</button>
                    <button type="button" class="dashboard-filter-btn" onclick="filterByPeriod('year', this)">Năm 2026</button>
                </div>
            </div>
        </header>

        <%---- ================= 2. 4 THẺ THỐNG KÊ NHANH (KPI CARDS) ================= ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon" style="background: rgba(229, 169, 59, 0.15); color: #fcca46;">💰</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiRevenue">${not empty totalRevenueFormatted ? totalRevenueFormatted : '184.250.000đ'}</span>
                    <span class="stat-label">Tổng doanh thu vé</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon total">🎟️</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiTickets">${not empty totalTicketsFormatted ? totalTicketsFormatted : '2.140'}</span>
                    <span class="stat-label">Vé đã bán ra</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer">🍿</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiCombos">${not empty comboRevenueFormatted ? comboRevenueFormatted : '46.800.000đ'}</span>
                    <span class="stat-label">Doanh thu bắp nước</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon locked" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8;">📈</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiFillRate">${not empty fillRateFormatted ? fillRateFormatted : '78.5%'}</span>
                    <span class="stat-label">Tỷ lệ lấp đầy ghế</span>
                </div>
            </div>
        </section>

        <%---- ================= 3. KHU VỰC 2 BIỂU ĐỒ (CHARTS) ================= ----%>
        <section style="display: grid; grid-template-columns: 2fr 1.1fr; gap: 20px; margin-bottom: 24px;">
            
            <%---- Biểu đồ cột: Doanh thu theo ngày/tháng ----%>
            <div class="table-card" style="padding: 20px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <h3 id="chartPeriodTitle" style="margin: 0; font-size: 16px; color: #f8fafc; font-weight: 700;">
                        📈 Xu Hướng Doanh Thu (7 Ngày Gần Nhất)
                    </h3>
                    <span id="chartPeriodGrowth" style="font-size: 12px; color: #4ade80; font-weight: 600;">+14.8% so với tuần trước</span>
                </div>
                <div style="height: 280px; position: relative;">
                    <canvas id="revenueChart"></canvas>
                </div>
            </div>

            <%---- Biểu đồ Donut: Tỷ lệ phân bổ theo rạp chiếu ----%>
            <div class="table-card" style="padding: 20px;">
                <h3 style="margin: 0 0 16px 0; font-size: 16px; color: #f8fafc; font-weight: 700;">
                    📍 Doanh Thu Theo Rạp
                </h3>
                <div style="height: 280px; position: relative; display: flex; align-items: center; justify-content: center;">
                    <canvas id="cinemaShareChart"></canvas>
                </div>
            </div>

        </section>

        <%---- ================= 4. BẢNG BẢNG XẾP HẠNG TOP PHIM ĂN KHÁCH ================= ----%>
        <section class="table-card">
            <div style="padding: 16px 20px; border-bottom: 1px solid rgba(255, 255, 255, 0.08); display: flex; justify-content: space-between; align-items: center;">
                <h3 style="margin: 0; font-size: 17px; color: #fcca46; font-weight: 700;">
                    🏆 Top 5 Phim Đạt Doanh Thu Cao Nhất
                </h3>
                <span style="font-size: 13px; color: #94a3b8;">Dữ liệu tính đến thời điểm hiện tại</span>
            </div>

            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width: 70px; text-align: center;">Hạng</th>
                            <th>Thông tin bộ phim</th>
                            <th>Suất chiếu</th>
                            <th>Số vé bán</th>
                            <th>Doanh thu</th>
                            <th style="width: 180px;">Tỷ lệ đóng góp</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty topMovies}">
                            <c:forEach items="${topMovies}" var="movie" varStatus="loop">
                                <tr>
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${loop.index == 0}"><span style="font-size: 18px;">🥇</span></c:when>
                                            <c:when test="${loop.index == 1}"><span style="font-size: 18px;">🥈</span></c:when>
                                            <c:when test="${loop.index == 2}"><span style="font-size: 18px;">🥉</span></c:when>
                                            <c:otherwise><span style="font-weight: 700; color: #94a3b8;">#${loop.index + 1}</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 12px;">
                                            <img src="${not empty movie.posterUrl ? movie.posterUrl : 'https://placehold.co/42x56/23211f/fcca46?text=POSTER'}" 
                                                 style="width: 42px; height: 56px; border-radius: 4px; object-fit: cover;" alt="${movie.title}">
                                            <div>
                                                <div style="font-weight: 700; color: #ffffff;">${movie.title}</div>
                                                <span style="font-size: 11px; color: #fcca46;">${movie.genre} · ${movie.duration} phút</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td>${movie.showtimesCount} suất</td>
                                    <td><strong><fmt:formatNumber value="${movie.ticketsSold}"/> vé</strong></td>
                                    <td><strong style="color: #fcca46;"><fmt:formatNumber value="${movie.revenue}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></strong></td>
                                    <td>
                                        <div style="background: rgba(255,255,255,0.1); border-radius: 10px; height: 8px; overflow: hidden;">
                                            <div style="width: ${movie.contributionPercent}%; background: ${loop.index == 0 ? '#fcca46' : (loop.index == 1 ? '#38bdf8' : (loop.index == 2 ? '#4ade80' : '#a855f7'))}; height: 100%;"></div>
                                        </div>
                                        <span style="font-size: 11px; color: #94a3b8; margin-top: 4px; display: block;">${movie.contributionPercent}% tổng doanh thu</span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" style="text-align: center; padding: 30px; color: #94a3b8;">
                                    Chưa có dữ liệu phim để xếp hạng.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

    </div>
</main>

<%---- ================= 5. KHỞI TẠO BIỂU ĐỒ & XỬ LÝ LỌC THỜI GIAN ================= ----%>
<script>
    // Dữ liệu mô phỏng tương ứng từng mốc thời gian
    const periodData = {
        'today': {
            revenue: '32.500.000đ',
            tickets: '385',
            combos: '8.200.000đ',
            fillRate: '82.4%',
            chartTitle: '📈 Xu Hướng Doanh Thu Theo Giờ (Hôm Nay)',
            growth: '+8.5% so với hôm qua',
            labels: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
            revenueData: [2.5, 3.8, 4.2, 3.1, 5.0, 7.2, 8.4, 4.5],
            cinemaShare: [38, 30, 20, 12]
        },
        '7days': {
            revenue: '${not empty totalRevenueFormatted ? totalRevenueFormatted : "184.250.000đ"}',
            tickets: '${not empty totalTicketsFormatted ? totalTicketsFormatted : "2.140"}',
            combos: '${not empty comboRevenueFormatted ? comboRevenueFormatted : "46.800.000đ"}',
            fillRate: '${not empty fillRateFormatted ? fillRateFormatted : "78.5%"}',
            chartTitle: '📈 Xu Hướng Doanh Thu (7 Ngày Gần Nhất)',
            growth: '+14.8% so với tuần trước',
            labels: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ Nhật'],
            revenueData: [18.5, 22.0, 19.8, 26.4, 35.0, 48.2, 42.5],
            cinemaShare: [42, 28, 18, 12]
        },
        'month': {
            revenue: '742.600.000đ',
            tickets: '8.950',
            combos: '185.400.000đ',
            fillRate: '81.2%',
            chartTitle: '📈 Xu Hướng Doanh Thu Theo Tuần (Tháng Này)',
            growth: '+19.2% so với tháng trước',
            labels: ['Tuần 1', 'Tuần 2', 'Tuần 3', 'Tuần 4'],
            revenueData: [165.0, 192.4, 184.2, 201.0],
            cinemaShare: [40, 29, 19, 12]
        },
        'year': {
            revenue: '8.450.000.000đ',
            tickets: '102.400',
            combos: '2.150.000.000đ',
            fillRate: '79.0%',
            chartTitle: '📈 Doanh Thu Theo Tháng (Năm 2026)',
            growth: '+24.5% so với năm 2025',
            labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
            revenueData: [620, 890, 710, 680, 750, 820, 790, 690, 740, 742, 650, 708],
            cinemaShare: [44, 27, 17, 12]
        }
    };

    let revenueChartInstance = null;
    let cinemaShareChartInstance = null;

    // 1. Cấu hình Biểu đồ cột Doanh thu (Revenue Chart)
    const ctxRevenue = document.getElementById('revenueChart').getContext('2d');
    revenueChartInstance = new Chart(ctxRevenue, {
        type: 'bar',
        data: {
            labels: periodData['7days'].labels,
            datasets: [{
                label: 'Doanh thu (triệu VNĐ)',
                data: periodData['7days'].revenueData,
                backgroundColor: 'rgba(252, 202, 70, 0.75)',
                borderColor: '#fcca46',
                borderWidth: 1.5,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255, 255, 255, 0.05)' },
                    ticks: { color: '#94a3b8' }
                },
                y: {
                    grid: { color: 'rgba(255, 255, 255, 0.05)' },
                    ticks: {
                        color: '#94a3b8',
                        callback: function(value) { return value + 'tr'; }
                    }
                }
            }
        }
    });

    // 2. Cấu hình Biểu đồ Donut Tỷ lệ theo rạp (Cinema Share)
    const ctxCinema = document.getElementById('cinemaShareChart').getContext('2d');
    cinemaShareChartInstance = new Chart(ctxCinema, {
        type: 'doughnut',
        data: {
            labels: ['Nguyễn Huệ', 'Landmark 81', 'Thảo Điền', 'Đan Phượng'],
            datasets: [{
                data: periodData['7days'].cinemaShare,
                backgroundColor: [
                    '#fcca46',
                    '#38bdf8',
                    '#4ade80',
                    '#f87171'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: '#cbd5e1', font: { size: 11 }, padding: 12 }
                }
            },
            cutout: '72%'
        }
    });

    // 3. Hàm xử lý khi người dùng click vào các mốc thời gian
    function filterByPeriod(period, btn) {
        // Cập nhật trạng thái active của nút
        document.querySelectorAll('.dashboard-filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        const data = periodData[period];
        if (!data) return;

        // Cập nhật số liệu 4 thẻ KPI
        document.getElementById('kpiRevenue').textContent = data.revenue;
        document.getElementById('kpiTickets').textContent = data.tickets;
        document.getElementById('kpiCombos').textContent = data.combos;
        document.getElementById('kpiFillRate').textContent = data.fillRate;

        // Cập nhật tiêu đề & % tăng trưởng biểu đồ
        document.getElementById('chartPeriodTitle').textContent = data.chartTitle;
        document.getElementById('chartPeriodGrowth').textContent = data.growth;

        // Cập nhật dữ liệu và vẽ lại Biểu đồ cột Doanh thu
        if (revenueChartInstance) {
            revenueChartInstance.data.labels = data.labels;
            revenueChartInstance.data.datasets[0].data = data.revenueData;
            revenueChartInstance.update();
        }

        // Cập nhật dữ liệu và vẽ lại Biểu đồ Donut Rạp
        if (cinemaShareChartInstance) {
            cinemaShareChartInstance.data.datasets[0].data = data.cinemaShare;
            cinemaShareChartInstance.update();
        }
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
