<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Bảng Điều Khiển - Cinetik Admin" scope="request"/>
<c:set var="extraCss" value="admin-users.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%---- Nhúng thư viện biểu đồ chuyên nghiệp Chart.js qua CDN ----%>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>

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
        text-decoration: none;
        display: inline-block;
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
                    <a href="<c:url value='/admin/dashboard'><c:param name='period' value='today'/></c:url>" class="dashboard-filter-btn ${period == 'today' ? 'active' : ''}">Hôm nay</a>
                    <a href="<c:url value='/admin/dashboard'><c:param name='period' value='7days'/></c:url>" class="dashboard-filter-btn ${period == '7days' ? 'active' : ''}">7 ngày qua</a>
                    <a href="<c:url value='/admin/dashboard'><c:param name='period' value='month'/></c:url>" class="dashboard-filter-btn ${period == 'month' ? 'active' : ''}">Tháng này</a>
                    <a href="<c:url value='/admin/dashboard'><c:param name='period' value='year'/></c:url>" class="dashboard-filter-btn ${period == 'year' ? 'active' : ''}">Năm ${currentYear}</a>
                </div>
            </div>
        </header>

        <c:if test="${not empty errorMessage}">
            <div style="background: rgba(120, 0, 0, 0.4); border: 1px solid #c92a2a; color: #ff8787; padding: 12px 18px; border-radius: 8px; font-size: 14px;">
                <span>⚠️ <c:out value="${errorMessage}"/></span>
            </div>
        </c:if>

        <%---- ================= 2. 4 THẺ THỐNG KÊ NHANH (KPI CARDS) ================= ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon" style="background: rgba(229, 169, 59, 0.15); color: #fcca46;">💰</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiRevenue">${not empty totalRevenueFormatted ? totalRevenueFormatted : '0đ'}</span>
                    <span class="stat-label">Tổng doanh thu vé</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon total">🎟️</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiTickets">${not empty totalTicketsFormatted ? totalTicketsFormatted : '0'}</span>
                    <span class="stat-label">Vé đã bán ra</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer">🍿</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiCombos">${not empty comboRevenueFormatted ? comboRevenueFormatted : '0đ'}</span>
                    <span class="stat-label">Doanh thu bắp nước</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon locked" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8;">📈</div>
                <div class="stat-info">
                    <span class="stat-value" id="kpiFillRate">${not empty fillRateFormatted ? fillRateFormatted : '0%'}</span>
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
                        <c:out value="${chartTitle}"/>
                    </h3>
                    <span id="chartPeriodGrowth" style="font-size: 12px; color: ${growthPositive ? '#4ade80' : '#f87171'}; font-weight: 600;"><c:out value="${growthText}"/></span>
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
                    <c:choose>
                        <c:when test="${not empty cinemaRevenue}">
                            <canvas id="cinemaShareChart"></canvas>
                        </c:when>
                        <c:otherwise>
                            <span style="color: #94a3b8; font-size: 13px;">Chưa có doanh thu trong kỳ này.</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

        </section>

        <%---- ================= 4. BẢNG BẢNG XẾP HẠNG TOP PHIM ĂN KHÁCH ================= ----%>
        <section class="table-card">
            <div style="padding: 16px 20px; border-bottom: 1px solid rgba(255, 255, 255, 0.08); display: flex; justify-content: space-between; align-items: center;">
                <h3 style="margin: 0; font-size: 17px; color: #fcca46; font-weight: 700;">
                    🏆 Top 5 Phim Đạt Doanh Thu Cao Nhất
                </h3>
                <span style="font-size: 13px; color: #94a3b8;">Theo kỳ đang chọn, chỉ tính đơn đã thanh toán</span>
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
                                            <img src="<c:out value='${not empty movie.posterUrl ? movie.posterUrl : "https://placehold.co/42x56/23211f/fcca46?text=POSTER"}'/>" 
                                                 style="width: 42px; height: 56px; border-radius: 4px; object-fit: cover;" alt="<c:out value='${movie.title}'/>">
                                            <div>
                                                <div style="font-weight: 700; color: #ffffff;"><c:out value='${movie.title}'/></div>
                                                <span style="font-size: 11px; color: #fcca46;"><c:out value='${movie.genre}'/> · ${movie.duration} phút</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td>${movie.showtimesCount} suất</td>
                                    <td><strong><fmt:formatNumber value="${movie.ticketsSold}"/> vé</strong></td>
                                    <td><strong style="color: #fcca46;"><fmt:formatNumber value="${movie.revenue}" type="number" maxFractionDigits="0"/> đ</strong></td>
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
                                    Chưa có đơn đặt vé đã thanh toán trong kỳ này.
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

<%---- ================= 5. DỮ LIỆU BIỂU ĐỒ (server render, JS chỉ đọc lại để vẽ) ================= ----%>
<ul id="revenueSeriesData" hidden>
    <c:forEach items="${chartLabels}" var="label" varStatus="loop">
        <li data-value="${chartValues[loop.index]}"><c:out value="${label}"/></li>
    </c:forEach>
</ul>
<ul id="cinemaShareData" hidden>
    <c:forEach items="${cinemaRevenue}" var="row">
        <li data-value="${row[1]}"><c:out value="${row[0]}"/></li>
    </c:forEach>
</ul>

<script>
    function readSeries(listId) {
        const items = document.querySelectorAll('#' + listId + ' li');
        return {
            labels: Array.from(items, li => li.textContent.trim()),
            values: Array.from(items, li => parseFloat(li.dataset.value) || 0)
        };
    }

    // 1. Biểu đồ cột Doanh thu theo giờ / ngày / tháng
    const revenueSeries = readSeries('revenueSeriesData');
    new Chart(document.getElementById('revenueChart').getContext('2d'), {
        type: 'bar',
        data: {
            labels: revenueSeries.labels,
            datasets: [{
                label: 'Doanh thu (triệu VNĐ)',
                data: revenueSeries.values,
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
                    beginAtZero: true,
                    grid: { color: 'rgba(255, 255, 255, 0.05)' },
                    ticks: {
                        color: '#94a3b8',
                        callback: function(value) { return value + 'tr'; }
                    }
                }
            }
        }
    });

    // 2. Biểu đồ Donut Doanh thu theo rạp
    const cinemaCanvas = document.getElementById('cinemaShareChart');
    if (cinemaCanvas) {
        const cinemaShare = readSeries('cinemaShareData');
        new Chart(cinemaCanvas.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: cinemaShare.labels,
                datasets: [{
                    data: cinemaShare.values,
                    backgroundColor: ['#fcca46', '#38bdf8', '#4ade80', '#f87171', '#a855f7', '#fb923c', '#94a3b8'],
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
                    },
                    tooltip: {
                        callbacks: {
                            label: ctx => ctx.label + ': ' + ctx.parsed.toLocaleString('vi-VN') + 'đ'
                        }
                    }
                },
                cutout: '72%'
            }
        });
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
