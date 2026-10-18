<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Quản Lý Phim - Cinetik Admin" scope="request"/>
<c:set var="extraCss" value="admin-users.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="admin-container">
    <div class="admin-layout" style="max-width: 1250px;">

        <%---- ================= 0. THANH ĐIỀU HƯỚNG QUẢN TRỊ ADMIN ================= ----%>
        <nav style="display: flex; gap: 8px; border-bottom: 1px solid #2d2824; padding-bottom: 12px; overflow-x: auto;">
            <a href="${pageContext.request.contextPath}/admin/dashboard" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                📊 Thống Kê
            </a>
            <a href="${pageContext.request.contextPath}/admin/movies" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%); border: 1px solid #fcca46; color: #fcca46; font-size: 13px; font-weight: 700; text-decoration: none;">
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

        <%---- ================= 1. HEADER TRANG QUẢN TRỊ PHIM ================= ----%>
        <header class="admin-header" style="align-items: center;">
            <div class="admin-header-title">
                <h1>Quản Lý Danh Sách Phim</h1>
                <p>Thêm phim mới, upload poster/banner, cập nhật thời lượng và điều chỉnh phân loại độ tuổi</p>
            </div>
            <div class="admin-header-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter-submit" style="display: flex; align-items: center; gap: 6px; padding: 10px 20px; font-weight: 700;" onclick="openAddMovieModal()">
                    ➕ Thêm Phim Mới
                </button>
            </div>
        </header>

        <%---- ================= 2. 4 THẺ THỐNG KÊ NHANH ================= ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon total">🎞️</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty totalMovies ? totalMovies : (not empty movieList ? movieList.size() : 0)}</span>
                    <span class="stat-label">Tổng số phim</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer" style="background: rgba(34, 197, 94, 0.15); color: #4ade80;">🟢</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty nowShowingCount ? nowShowingCount : 0}</span>
                    <span class="stat-label">Phim đang chiếu</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon" style="background: rgba(229, 169, 59, 0.15); color: #fcca46;">🟡</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty upcomingCount ? upcomingCount : 0}</span>
                    <span class="stat-label">Phim sắp chiếu</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon staff" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8;">⏱️</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty avgDuration ? avgDuration : 0} phút</span>
                    <span class="stat-label">Thời lượng trung bình</span>
                </div>
            </div>
        </section>

        <%---- ================= THÔNG BÁO HỆ THỐNG (flash message từ session) ================= ----%>
        <c:set var="msgError" value="${not empty errorMessage ? errorMessage : sessionScope.errorMessage}" />
        <c:set var="msgSuccess" value="${not empty successMessage ? successMessage : sessionScope.successMessage}" />

        <c:if test="${not empty msgError}">
            <div style="background: rgba(120, 0, 0, 0.4); border: 1px solid #c92a2a; color: #ff8787; padding: 12px 18px; border-radius: 8px; font-size: 14px;">
                <span>⚠️ <c:out value="${msgError}"/></span>
            </div>
            <c:remove var="errorMessage" scope="session" />
        </c:if>
        <c:if test="${not empty msgSuccess}">
            <div style="background: rgba(43, 138, 62, 0.25); border: 1px solid #2b8a3e; color: #69db7c; padding: 12px 18px; border-radius: 8px; font-size: 14px;">
                <span>✅ <c:out value="${msgSuccess}"/></span>
            </div>
            <c:remove var="successMessage" scope="session" />
        </c:if>

        <%---- ================= 3. BỘ LỌC & TÌM KIẾM PHIM ================= ----%>
        <section class="filter-card">
            <form action="${pageContext.request.contextPath}/admin/movies" method="GET" class="filter-form">
                <%---- Ô tìm kiếm tên phim hoặc đạo diễn ----%>
                <div class="search-group" style="flex: 2;">
                    <span class="search-icon-inside">🔍</span>
                    <input type="text" 
                           name="keyword" 
                           class="search-input" 
                           placeholder="Tìm kiếm theo tên phim, đạo diễn, diễn viên..." 
                           value="${param.keyword}">
                </div>

                <%---- Lọc theo thể loại ----%>
                <select name="genre" class="filter-select">
                    <option value="">-- Tất cả thể loại --</option>
                    <option value="Hành động" ${param.genre == 'Hành động' ? 'selected' : ''}>Hành động / Phiêu lưu</option>
                    <option value="Khoa học viễn tưởng" ${param.genre == 'Khoa học viễn tưởng' ? 'selected' : ''}>Khoa học viễn tưởng</option>
                    <option value="Kinh dị" ${param.genre == 'Kinh dị' ? 'selected' : ''}>Kinh dị / Giật gân</option>
                    <option value="Hoạt hình" ${param.genre == 'Hoạt hình' ? 'selected' : ''}>Hoạt hình / Gia đình</option>
                    <option value="Tình cảm" ${param.genre == 'Tình cảm' ? 'selected' : ''}>Tình cảm / Lãng mạn</option>
                </select>

                <%---- Lọc theo độ tuổi ----%>
                <select name="ageRating" class="filter-select">
                    <option value="">-- Phân loại tuổi --</option>
                    <option value="P" ${param.ageRating == 'P' ? 'selected' : ''}>P - Mọi lứa tuổi</option>
                    <option value="K" ${param.ageRating == 'K' ? 'selected' : ''}>K - Dưới 13 tuổi có kèm</option>
                    <option value="T13" ${param.ageRating == 'T13' ? 'selected' : ''}>T13 - Từ 13 tuổi</option>
                    <option value="T16" ${param.ageRating == 'T16' ? 'selected' : ''}>T16 - Từ 16 tuổi</option>
                    <option value="T18" ${param.ageRating == 'T18' ? 'selected' : ''}>T18 - Từ 18 tuổi</option>
                </select>

                <button type="submit" class="btn-filter-submit">Tìm Kiếm</button>
                <a href="${pageContext.request.contextPath}/admin/movies" class="btn-filter-reset">Đặt lại</a>
            </form>
        </section>

        <%---- ================= 4. BẢNG DANH SÁCH PHIM ================= ----%>
        <section class="table-card">
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width: 80px; text-align: center;">Poster</th>
                            <th>Tên phim & Thời lượng</th>
                            <th>Đạo diễn</th>
                            <th>Thể loại</th>
                            <th>Khởi chiếu</th>
                            <th>Độ tuổi</th>
                            <th style="text-align: right; width: 140px;">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <%-- Dữ liệu động khi Backend truyền ${movieList} --%>
                            <c:when test="${not empty movieList}">
                                <c:forEach var="m" items="${movieList}">
                                    <tr>
                                        <%---- Poster Thumbnail ----%>
                                        <td style="text-align: center;">
                                            <img src="${m.posterURL}" alt="${m.title}" 
                                                 style="width: 50px; height: 70px; border-radius: 6px; object-fit: cover; border: 1px solid rgba(255,255,255,0.1); box-shadow: 0 4px 10px rgba(0,0,0,0.3);">
                                        </td>
                                        <%---- Tên phim & Thời lượng ----%>
                                        <td>
                                            <div style="font-weight: 700; color: #ffffff; font-size: 15px;">${m.title}</div>
                                            <span style="font-size: 12px; color: #94a3b8;">⏱️ ${m.duration} phút</span>
                                        </td>
                                        <%---- Đạo diễn ----%>
                                        <td>${m.director}</td>
                                        <%---- Thể loại ----%>
                                        <td><span style="color: #cbd5e1; font-size: 13px;">${m.genre}</span></td>
                                        <%---- Ngày khởi chiếu ----%>
                                        <td><span style="color: #94a3b8; font-size: 13px;">${m.releaseDate}</span></td>
                                        <%---- Phân loại độ tuổi ----%>
                                        <td>
                                            <span class="user-role-badge" style="background: rgba(229, 169, 59, 0.15); color: #fcca46; border: 1px solid rgba(229, 169, 59, 0.35); font-weight: 700;">
                                                ${m.ageRating}
                                            </span>
                                        </td>
                                        <%---- Nút hành động ----%>
                                        <td style="text-align: right;">
                                            <button type="button" class="btn-action edit" title="Chỉnh sửa phim"
                                                    data-id="${m.movieID}" data-title="<c:out value='${m.title}'/>"
                                                    data-director="<c:out value='${m.director}'/>" data-genre="<c:out value='${m.genre}'/>"
                                                    data-duration="${m.duration}" data-release="${m.releaseDate}" data-age="<c:out value='${m.ageRating}'/>"
                                                    data-poster="<c:out value='${m.posterURL}'/>" data-trailer="<c:out value='${m.trailerURL}'/>"
                                                    data-desc="<c:out value='${m.description}'/>"
                                                    onclick="const d = this.dataset; openEditMovieModal(d.id, d.title, d.director, d.genre, d.duration, d.release, d.age, d.poster, d.desc, d.trailer)">
                                                ✏️
                                            </button>
                                            <button type="button" class="btn-action lock" title="Xóa phim"
                                                    data-id="${m.movieID}" data-title="<c:out value='${m.title}'/>"
                                                    onclick="openDeleteMovieModal(this.dataset.id, this.dataset.title)">
                                                🗑️
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>

                            <%-- Hiển thị thông báo khi không có phim hoặc kết quả tìm kiếm rỗng --%>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 45px 20px; color: #94a3b8; font-size: 14px;">
                                        🎬 Hiện chưa có bộ phim nào trong hệ thống hoặc không tìm thấy phim phù hợp với bộ lọc.
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

<%---- ================= 5. MODAL THÊM / CHỈNH SỬA PHIM ================= ----%>
<div id="movieModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center; padding: 20px;">
    <div style="background: #181d26; border: 1px solid rgba(252, 202, 70, 0.35); border-radius: 14px; max-width: 650px; width: 100%; color: #fff; box-shadow: 0 16px 40px rgba(0,0,0,0.6); max-height: 90vh; overflow-y: auto; padding: 26px;">
        
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 12px;">
            <h2 id="modalMovieHeading" style="margin: 0; font-size: 19px; color: #fcca46; display: flex; align-items: center; gap: 8px;">
                🎬 Thêm Phim Mới
            </h2>
            <button type="button" onclick="closeMovieModal()" style="background: none; border: none; color: #94a3b8; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <form id="movieForm" action="${pageContext.request.contextPath}/admin/movies/save" method="POST">
            <input type="hidden" name="movieID" id="inputMovieID">

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px;">
                <%---- Tên phim ----%>
                <div style="grid-column: 1 / -1;">
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Tên phim (*):</label>
                    <input type="text" name="title" id="inputTitle" required placeholder="Nhập tên phim chính xác..."
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                </div>

                <%---- Đạo diễn ----%>
                <div>
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Đạo diễn:</label>
                    <input type="text" name="director" id="inputDirector" placeholder="Tên đạo diễn..."
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                </div>

                <%---- Thể loại ----%>
                <div>
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Thể loại:</label>
                    <input type="text" name="genre" id="inputGenre" placeholder="Ví dụ: Hành động, Viễn tưởng..."
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                </div>

                <%---- Thời lượng (phút) ----%>
                <div>
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Thời lượng (phút):</label>
                    <input type="number" name="duration" id="inputDuration" placeholder="120" min="1"
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                </div>

                <%---- Phân loại tuổi ----%>
                <div>
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Độ tuổi:</label>
                    <select name="ageRating" id="inputAgeRating" 
                            style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                        <option value="P">P - Phổ biến cho mọi độ tuổi</option>
                        <option value="K">K - Dưới 13 tuổi có người lớn đi kèm</option>
                        <option value="T13">T13 - Khán giả từ 13 tuổi trở lên</option>
                        <option value="T16" selected>T16 - Khán giả từ 16 tuổi trở lên</option>
                        <option value="T18">T18 - Khán giả từ 18 tuổi trở lên</option>
                    </select>
                </div>

                <%---- Ngày khởi chiếu ----%>
                <div style="grid-column: 1 / -1;">
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Ngày khởi chiếu (*):</label>
                    <input type="date" name="releaseDate" id="inputReleaseDate" required
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                </div>

                <%---- URL Poster & Khung xem trước ảnh ----%>
                <div style="grid-column: 1 / -1;">
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Đường dẫn Poster (URL / Tải ảnh lên):</label>
                    <div style="display: flex; gap: 14px; align-items: flex-start;">
                        <div style="flex: 1;">
                            <input type="text" name="posterURL" id="inputPosterURL" placeholder="https://image.tmdb.org/... hoặc /assets/img/..." 
                                   oninput="updatePosterPreview(this.value)"
                                   style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                            <span style="font-size: 11px; color: #64748b; margin-top: 4px; display: block;">
                                Nhập đường dẫn link ảnh poster hoặc file ảnh tĩnh trong thư mục assets
                            </span>
                        </div>
                        <img id="posterPreviewImg" src="https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=200&auto=format&fit=crop" 
                             alt="Preview" style="width: 60px; height: 85px; border-radius: 6px; object-fit: cover; border: 1px solid rgba(252,202,70,0.4); flex-shrink: 0;">
                    </div>
                </div>

                <%---- Link trailer Youtube ----%>
                <div style="grid-column: 1 / -1;">
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Link trailer (Youtube embed):</label>
                    <input type="text" name="trailerURL" id="inputTrailerURL" placeholder="https://www.youtube.com/embed/..."
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                </div>

                <%---- Mô tả nội dung phim ----%>
                <div style="grid-column: 1 / -1;">
                    <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 5px;">Mô tả tóm tắt nội dung phim:</label>
                    <textarea name="description" id="inputDescription" rows="3" placeholder="Nhập tóm tắt nội dung cốt truyện..."
                              style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box; resize: vertical;"></textarea>
                </div>
            </div>

            <%---- Nút hành động Modal ----%>
            <div style="display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid rgba(255,255,255,0.08); padding-top: 16px;">
                <button type="button" onclick="closeMovieModal()" 
                        style="background: #272f3d; color: #cbd5e1; border: none; padding: 10px 18px; border-radius: 6px; cursor: pointer;">
                    Hủy bỏ
                </button>
                <button type="submit" 
                        style="background: #fcca46; color: #0f1217; border: none; padding: 10px 22px; border-radius: 6px; font-weight: 700; cursor: pointer;">
                    💾 Lưu Thông Tin Phim
                </button>
            </div>
        </form>

    </div>
</div>

<%---- ================= 6. MODAL XÁC NHẬN XÓA PHIM ================= ----%>
<div id="deleteMovieModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center; padding: 20px;">
    <div style="background: #181d26; border: 1px solid rgba(239, 68, 68, 0.4); border-radius: 12px; padding: 24px; max-width: 420px; width: 100%; color: #fff; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
        <h3 style="margin-top: 0; color: #f87171; display: flex; align-items: center; gap: 8px;">
            ⚠️ Xác Nhận Xóa Phim
        </h3>
        <p style="font-size: 14px; color: #cbd5e1; line-height: 1.5;">
            Bạn có chắc chắn muốn xóa bộ phim <strong id="deleteMovieTitle" style="color: #fcca46;"></strong>?
        </p>
        <p style="font-size: 12px; color: #ef4444; background: rgba(239, 68, 68, 0.1); padding: 8px 12px; border-radius: 6px;">
            * Thao tác này sẽ đồng thời xóa hoặc ẩn tất cả các suất chiếu liên quan đến bộ phim này.
        </p>
        <form action="${pageContext.request.contextPath}/admin/movies/delete" method="POST">
            <input type="hidden" name="movieID" id="inputDeleteMovieID">
            <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 18px;">
                <button type="button" onclick="closeDeleteModal()" 
                        style="background: #272f3d; color: #cbd5e1; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer;">
                    Hủy bỏ
                </button>
                <button type="submit" 
                        style="background: #ef4444; color: #fff; border: none; padding: 8px 18px; border-radius: 6px; font-weight: 600; cursor: pointer;">
                    Xác Nhận Xóa
                </button>
            </div>
        </form>
    </div>
</div>

<%---- ================= 7. JAVASCRIPT ĐIỀU KHIỂN ================= ----%>
<script>
    function updatePosterPreview(url) {
        const preview = document.getElementById('posterPreviewImg');
        if (url && url.trim() !== '') {
            preview.src = url;
        } else {
            preview.src = 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=200&auto=format&fit=crop';
        }
    }

    function openAddMovieModal() {
        document.getElementById('modalMovieHeading').textContent = '➕ Thêm Phim Mới';
        document.getElementById('inputMovieID').value = '';
        document.getElementById('inputTitle').value = '';
        document.getElementById('inputDirector').value = '';
        document.getElementById('inputGenre').value = '';
        document.getElementById('inputDuration').value = '120';
        document.getElementById('inputAgeRating').value = 'T16';
        document.getElementById('inputReleaseDate').value = new Date().toISOString().split('T')[0];
        document.getElementById('inputPosterURL').value = '';
        document.getElementById('inputTrailerURL').value = '';
        document.getElementById('inputDescription').value = '';
        document.getElementById('posterPreviewImg').src = 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=200&auto=format&fit=crop';
        document.getElementById('movieModal').style.display = 'flex';
    }

    function openEditMovieModal(id, title, director, genre, duration, releaseDate, ageRating, posterURL, desc, trailerURL) {
        document.getElementById('modalMovieHeading').textContent = '✏️ Chỉnh Sửa Thông Tin Phim';
        document.getElementById('inputMovieID').value = id;
        document.getElementById('inputTitle').value = title;
        document.getElementById('inputDirector').value = director;
        document.getElementById('inputGenre').value = genre;
        document.getElementById('inputDuration').value = duration;
        document.getElementById('inputAgeRating').value = ageRating;
        document.getElementById('inputReleaseDate').value = releaseDate;
        document.getElementById('inputPosterURL').value = posterURL;
        document.getElementById('inputTrailerURL').value = trailerURL || '';
        document.getElementById('inputDescription').value = desc;
        updatePosterPreview(posterURL);
        document.getElementById('movieModal').style.display = 'flex';
    }

    function closeMovieModal() {
        document.getElementById('movieModal').style.display = 'none';
    }

    function openDeleteMovieModal(id, title) {
        document.getElementById('inputDeleteMovieID').value = id;
        document.getElementById('deleteMovieTitle').textContent = '"' + title + '"';
        document.getElementById('deleteMovieModal').style.display = 'flex';
    }

    function closeDeleteModal() {
        document.getElementById('deleteMovieModal').style.display = 'none';
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
