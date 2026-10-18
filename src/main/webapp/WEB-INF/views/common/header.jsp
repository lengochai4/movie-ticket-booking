<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty pageTitle ? pageTitle : 'Cinetik - Đặt Vé Xem Phim'}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Limelight&family=Playfair+Display:wght@700;900&family=Be+Vietnam+Pro:wght@400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/common.css" type="text/css">
    <c:if test="${not empty extraCss}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/${extraCss}" type="text/css">
    </c:if>
</head>
<body>

<%---- HEADER ----%>
<header>
    <%---- 1. Bên trái: Logo ----%>
    <a href="${pageContext.request.contextPath}/movies" class="logo">Cinetik</a>

    <%---- 2. Chính giữa: Menu điều hướng ----%>
    <nav class="header-center">
        <a href="${pageContext.request.contextPath}/movies#noi-bat" id="navFeaturedBtn">Phim Nổi Bật</a>
        <a href="${pageContext.request.contextPath}/movies#lich-chieu" id="navScheduleBtn">Lịch Chiếu</a>
        
        <div class="movie-dropdown" id="movieNavDropdown">
            <a href="javascript:void(0)" class="movie-dropdown-btn" id="movieDropdownBtn">Phim ▾</a>
            <div class="dropdown-content">
                <a href="${pageContext.request.contextPath}/movies?tab=now_showing" class="${selectedTab != 'coming_soon' ? 'active-item' : ''}">🎬 Phim đang chiếu</a>
                <a href="${pageContext.request.contextPath}/movies?tab=coming_soon" class="${selectedTab == 'coming_soon' ? 'active-item' : ''}">⏳ Phim sắp chiếu</a>
            </div>
        </div>

        <div class="cinema-dropdown" id="cinemaNavDropdown">
            <a href="javascript:void(0)" class="cinema-btn" id="cinemaDropdownBtn">Rạp ▾</a>
            <div class="cinema-menu" id="cinemaDropdownMenu">
                <div class="cinema-filter-bar">
                    <input type="text" id="headerCinemaSearch" class="search-cinema-input" placeholder="🔍 Tìm theo tên rạp, đường..." autocomplete="off">
                    <select id="headerCitySelect" class="city-select">
                        <option value="">Tất cả khu vực</option>
                        <option value="TP. Hồ Chí Minh">TP. Hồ Chí Minh</option>
                        <option value="Hà Nội">Hà Nội</option>
                        <option value="Đà Nẵng">Đà Nẵng</option>
                        <option value="Bình Dương">Bình Dương</option>
                        <option value="Đồng Nai">Đồng Nai</option>
                        <option value="Cần Thơ">Cần Thơ</option>
                        <option value="Khánh Hòa">Khánh Hòa</option>
                        <option value="Quảng Ninh">Quảng Ninh</option>
                        <option value="Thái Nguyên">Thái Nguyên</option>
                        <option value="Bến Tre">Bến Tre</option>
                    </select>
                </div>
                <div class="cinema-list" id="headerCinemaList">
                    <div class="cinema-empty-tip">Đang tải danh sách 32 cụm rạp...</div>
                </div>
                <div class="cinema-menu-footer">
                    <a href="${pageContext.request.contextPath}/cinemas">Xem danh sách tất cả 32 cụm rạp trên toàn quốc ➔</a>
                </div>
            </div>
        </div>
    </nav>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            var contextPath = '${pageContext.request.contextPath}';
            
            // ── 0. Nút Phim Nổi Bật ──
            var navFeaturedBtn = document.getElementById('navFeaturedBtn');
            if (navFeaturedBtn) {
                navFeaturedBtn.addEventListener('click', function(e) {
                    var currentPath = window.location.pathname;
                    if (currentPath.indexOf('/movies') !== -1) {
                        var target = document.getElementById('noi-bat');
                        if (target) {
                            e.preventDefault();
                            target.scrollIntoView({ behavior: 'smooth' });
                        }
                    }
                });
            }

            // ── 1. Nút Lịch Chiếu ──
            var navScheduleBtn = document.getElementById('navScheduleBtn');
            if (navScheduleBtn) {
                navScheduleBtn.addEventListener('click', function(e) {
                    var currentPath = window.location.pathname;
                    if (currentPath.indexOf('/movies') !== -1) {
                        var target = document.getElementById('lich-chieu') || document.getElementById('dang-chieu');
                        if (target) {
                            e.preventDefault();
                            target.scrollIntoView({ behavior: 'smooth' });
                        }
                    }
                });
            }

            // ── 2. Dropdown Phim ──
            var movieDropdown = document.getElementById('movieNavDropdown');
            var movieBtn = document.getElementById('movieDropdownBtn');
            if (movieDropdown && movieBtn) {
                movieBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    if (cinemaDropdown) cinemaDropdown.classList.remove('open');
                    movieDropdown.classList.toggle('open');
                });
            }

            // ── 3. Dropdown Rạp với tìm kiếm & lọc khu vực ──
            var cinemaDropdown = document.getElementById('cinemaNavDropdown');
            var cinemaBtn = document.getElementById('cinemaDropdownBtn');
            var cinemaSearch = document.getElementById('headerCinemaSearch');
            var citySelect = document.getElementById('headerCitySelect');
            var cinemaListEl = document.getElementById('headerCinemaList');
            var cachedCinemas = null;

            function renderCinemas(cinemas) {
                if (!cinemaListEl) return;
                var kw = (cinemaSearch ? cinemaSearch.value : '').toLowerCase().trim();
                var city = cinemaSelectCity = (citySelect ? citySelect.value : '').trim();

                var filtered = cinemas.filter(function(c) {
                    var matchKw = !kw || (c.name && c.name.toLowerCase().indexOf(kw) !== -1) ||
                                  (c.address && c.address.toLowerCase().indexOf(kw) !== -1);
                    var matchCity = !city || (c.city && c.city.indexOf(city) !== -1);
                    return matchKw && matchCity;
                });

                if (filtered.length === 0) {
                    cinemaListEl.innerHTML = '<div class="cinema-empty-tip">Không tìm thấy rạp nào phù hợp với tìm kiếm.</div>';
                    return;
                }

                var html = '';
                filtered.forEach(function(c) {
                    html += '<a href="' + contextPath + '/movies?cinemaId=' + encodeURIComponent(c.id) + '" class="cinema-item">' +
                                '<span class="cinema-logo">' + (c.brandIcon || '🏛️') + '</span>' +
                                '<div class="cinema-info">' +
                                    '<h4>' + c.name +
                                        ' <span class="badge-tag ' + (c.brandBadge || 'badge-cinetik') + '">' + (c.brand || 'Rạp') + '</span>' +
                                        ' <span class="badge-city">' + (c.city || 'Toàn quốc') + '</span>' +
                                    '</h4>' +
                                    '<p title="' + (c.address || '') + '">📍 ' + (c.address || '') + '</p>' +
                                '</div>' +
                            '</a>';
                });
                cinemaListEl.innerHTML = html;
            }

            function loadCinemas() {
                if (cachedCinemas) {
                    renderCinemas(cachedCinemas);
                    return;
                }
                fetch(contextPath + '/api/cinemas')
                    .then(function(res) { return res.json(); })
                    .then(function(data) {
                        cachedCinemas = data;
                        renderCinemas(cachedCinemas);
                    })
                    .catch(function(err) {
                        console.error('Lỗi tải danh sách rạp:', err);
                        if (cinemaListEl) {
                            cinemaListEl.innerHTML = '<div class="cinema-empty-tip">Có lỗi khi tải danh sách rạp. Vui lòng thử lại sau.</div>';
                        }
                    });
            }

            if (cinemaDropdown && cinemaBtn) {
                cinemaBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    if (movieDropdown) movieDropdown.classList.remove('open');
                    var isOpen = cinemaDropdown.classList.toggle('open');
                    if (isOpen) {
                        loadCinemas();
                        if (cinemaSearch) setTimeout(function() { cinemaSearch.focus(); }, 100);
                    }
                });

                // Tải trước danh sách rạp khi rê chuột vào nút Rạp
                cinemaDropdown.addEventListener('mouseenter', function() {
                    if (!cachedCinemas) loadCinemas();
                });
            }

            if (cinemaSearch) {
                cinemaSearch.addEventListener('input', function() {
                    if (cachedCinemas) renderCinemas(cachedCinemas);
                });
                cinemaSearch.addEventListener('click', function(e) { e.stopPropagation(); });
            }

            if (citySelect) {
                citySelect.addEventListener('change', function() {
                    if (cachedCinemas) renderCinemas(cachedCinemas);
                });
                citySelect.addEventListener('click', function(e) { e.stopPropagation(); });
            }

            // Đóng menu khi click ra ngoài
            document.addEventListener('click', function(e) {
                if (movieDropdown && !movieDropdown.contains(e.target)) {
                    movieDropdown.classList.remove('open');
                }
                if (cinemaDropdown && !cinemaDropdown.contains(e.target)) {
                    cinemaDropdown.classList.remove('open');
                }
            });
        });
    </script>

    <%---- 3. Bên phải: Tìm kiếm & Đăng nhập ----%>
    <div class="header-right">
        <form action="${pageContext.request.contextPath}/movies" method="GET" class="header-search-form">
            <input type="text" name="keyword" placeholder="Tìm tên phim, thể loại..." class="header-search-input" value="${param.keyword}">
        </form>

        <c:choose>
            <c:when test="${not empty sessionScope.loginUser}">
                <a href="${pageContext.request.contextPath}/auth/profile" 
                   class="nav-header-avatar" 
                   title="${sessionScope.loginUser.fullName} - Xem hồ sơ">
                    <span>${not empty sessionScope.loginUser.fullName ? sessionScope.loginUser.fullName.substring(0,1).toUpperCase() : 'U'}</span>
                </a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/auth/login" class="nav-btn-login">Đăng Nhập</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>
