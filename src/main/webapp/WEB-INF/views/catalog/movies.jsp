<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="${selectedTab == 'coming_soon' ? 'Phim Sắp Chiếu' : 'Phim Đang Chiếu'} - Cinetik" scope="request"/>
<c:set var="extraCss" value="catalog.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main>
    <%---- 1. MỤC PHIM NỔI BẬT (THEATRICAL SHOWCASE TỪ MAIN.DC.HTML) ----%>
    <section id="noi-bat">
        <!-- Chùm đèn rọi Spotlight từ trần sân khấu -->
        <svg aria-hidden="true" viewBox="0 0 1440 1000" preserveAspectRatio="none" class="featured-spotlight-svg">
            <defs>
                <filter id="bl" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="7"></feGaussianBlur></filter>
                <filter id="bl2" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="18"></feGaussianBlur></filter>
                <linearGradient id="beam" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0" stop-color="#fff1d0" stop-opacity=".6"></stop>
                    <stop offset=".55" stop-color="#ffd9a0" stop-opacity=".14"></stop>
                    <stop offset="1" stop-color="#ffd9a0" stop-opacity=".05"></stop>
                </linearGradient>
                <radialGradient id="glow" cx="720" cy="0" r="280" gradientUnits="userSpaceOnUse">
                    <stop offset="0" stop-color="#fff" stop-opacity=".95"></stop>
                    <stop offset=".3" stop-color="#ffe9bf" stop-opacity=".45"></stop>
                    <stop offset="1" stop-color="#ffe9bf" stop-opacity="0"></stop>
                </radialGradient>
            </defs>
            <g filter="url(#bl2)"><path d="M690,0H750L1140,1000H300Z" fill="url(#beam)"></path></g>
            <circle cx="720" cy="0" r="280" fill="url(#glow)"></circle>
            <ellipse cx="720" cy="4" rx="46" ry="11" fill="#fffaf0" filter="url(#bl)"></ellipse>
        </svg>

        <!-- Màn nhung đỏ sân khấu hai bên -->
        <svg aria-hidden="true" viewBox="0 0 260 1000" preserveAspectRatio="none" class="featured-curtain-svg featured-curtain-left">
            <defs>
                <linearGradient id="v1" x1="0" y1="0" x2="62" y2="0" gradientUnits="userSpaceOnUse" spreadMethod="repeat">
                    <stop offset="0" stop-color="#3a050a"></stop>
                    <stop offset=".32" stop-color="#8a0e19"></stop>
                    <stop offset=".5" stop-color="#b3182a"></stop>
                    <stop offset=".7" stop-color="#8a0e19"></stop>
                    <stop offset="1" stop-color="#3a050a"></stop>
                </linearGradient>
                <linearGradient id="v2" x1="0" y1="0" x2="97" y2="0" gradientUnits="userSpaceOnUse" spreadMethod="repeat">
                    <stop offset="0" stop-color="#000" stop-opacity=".35"></stop>
                    <stop offset=".5" stop-color="#000" stop-opacity="0"></stop>
                    <stop offset="1" stop-color="#000" stop-opacity=".35"></stop>
                </linearGradient>
                <linearGradient id="sh" x1="0" y1="0" x2="1" y2="0">
                    <stop offset="0" stop-color="#000" stop-opacity="0"></stop>
                    <stop offset="1" stop-color="#000" stop-opacity=".5"></stop>
                </linearGradient>
                <linearGradient id="tp" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0" stop-color="#000" stop-opacity=".7"></stop>
                    <stop offset="1" stop-color="#000" stop-opacity="0"></stop>
                </linearGradient>
                <pattern id="fr" width="7" height="30" patternUnits="userSpaceOnUse">
                    <rect x="2" width="3" height="30" rx="1.5" fill="#b8821c"></rect>
                    <rect x="2" width="1" height="30" fill="#ffe08a"></rect>
                </pattern>
                <clipPath id="cc"><path d="M0,0H250C246,200 262,420 252,640 S258,860 250,946H0Z"></path></clipPath>
                <g id="cur">
                    <g clip-path="url(#cc)">
                        <rect width="260" height="1000" fill="url(#v1)"></rect>
                        <rect width="260" height="1000" fill="url(#v2)"></rect>
                        <g filter="url(#bl)" fill="none" stroke="#1a0205" stroke-width="20" opacity=".5">
                            <path d="M30,0C26,300 36,620 28,950"></path>
                            <path d="M92,0C88,300 98,620 90,950"></path>
                            <path d="M154,0C150,300 160,620 152,950"></path>
                            <path d="M216,0C212,300 222,620 214,950"></path>
                        </g>
                        <g filter="url(#bl)" fill="none" stroke="#e83f4c" stroke-width="8" opacity=".35">
                            <path d="M62,0C59,300 67,620 61,950"></path>
                            <path d="M124,0C121,300 129,620 123,950"></path>
                            <path d="M186,0C183,300 191,620 185,950"></path>
                            <path d="M240,0C237,300 245,620 239,950"></path>
                        </g>
                        <rect width="260" height="1000" fill="url(#sh)"></rect>
                        <rect width="260" height="300" fill="url(#tp)"></rect>
                    </g>
                    <rect x="0" y="952" width="252" height="30" fill="url(#fr)"></rect>
                    <rect x="0" y="938" width="252" height="14" rx="3" fill="#a87a1a"></rect>
                    <rect x="0" y="941" width="252" height="3" fill="#ffe08a" opacity=".7"></rect>
                </g>
            </defs>
            <use href="#cur"></use>
        </svg>
        <svg aria-hidden="true" viewBox="0 0 260 1000" preserveAspectRatio="none" class="featured-curtain-svg featured-curtain-right"><use href="#cur"></use></svg>

        <!-- Sàn gỗ vòm cong sân khấu phản chiếu ánh đèn rực rỡ -->
        <div class="featured-stage-base"></div>
        <div class="featured-stage-floor"></div>

        <!-- Khung nội dung chính -->
        <div class="featured-container">
            <div class="featured-marquee">
                <h2 class="featured-heading">★ Phim nổi bật ★</h2>
            </div>
            <p class="featured-subtitle">Ba thước phim được mong chờ nhất tuần này, chiếu trên màn ảnh lớn như thuở xưa.</p>

            <c:choose>
                <%-- Khi có ít nhất 3 bộ phim từ cơ sở dữ liệu --%>
                <c:when test="${not empty featuredMovies && featuredMovies.size() >= 3}">
                    <div class="featured-grid">
                        <%-- Phim 1 (Bên trái) --%>
                        <div>
                            <div class="featured-pst" style="height:340px;background:#0d0b0a;color:#f6ead2;border-color:#c4161f">
                                <c:choose>
                                    <c:when test="${not empty featuredMovies[0].posterURL}">
                                        <img src="${featuredMovies[0].posterURL}" alt="${featuredMovies[0].title}" loading="lazy">
                                        <div class="featured-pst-overlay"></div>
                                        <div class="featured-pst-text">${featuredMovies[0].title}</div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="featured-pst-text">${featuredMovies[0].title}</div>
                                    </c:otherwise>
                                </c:choose>
                                <span class="featured-bd">${not empty featuredMovies[0].ageRating ? featuredMovies[0].ageRating : 'T18'}</span>
                            </div>
                            <div class="featured-card-info" style="border-top:2px solid #c4161f">
                                <h3 title="${featuredMovies[0].title}">${featuredMovies[0].title}</h3>
                                <div class="featured-meta">${featuredMovies[0].genre} · ${featuredMovies[0].duration} phút</div>
                                <a class="btn-featured" href="${pageContext.request.contextPath}/movie-detail?id=${featuredMovies[0].movieID}">Mua vé</a>
                            </div>
                        </div>

                        <%-- Phim 2 (Chính giữa - Suất đặc biệt nổi bật nhất) --%>
                        <div>
                            <div class="featured-badge-special">Suất đặc biệt</div>
                            <div class="featured-pst" style="height:440px;background:#c4161f;color:#f6ead2;font-size:42px;box-shadow:0 0 40px rgba(196,22,31,.6);border-color:#fcca46">
                                <c:choose>
                                    <c:when test="${not empty featuredMovies[1].posterURL}">
                                        <img src="${featuredMovies[1].posterURL}" alt="${featuredMovies[1].title}" loading="lazy">
                                        <div class="featured-pst-overlay"></div>
                                        <div class="featured-pst-text">${featuredMovies[1].title}</div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="featured-pst-text">${featuredMovies[1].title}</div>
                                    </c:otherwise>
                                </c:choose>
                                <span class="featured-bd">${not empty featuredMovies[1].ageRating ? featuredMovies[1].ageRating : 'T13'}</span>
                            </div>
                            <div class="featured-card-info" style="border-top:2px solid #fcca46">
                                <h3 style="font-size:24px" title="${featuredMovies[1].title}">${featuredMovies[1].title}</h3>
                                <div class="featured-meta">${featuredMovies[1].genre} · ${featuredMovies[1].duration} phút · Suất chiếu HOT</div>
                                <a class="btn-featured" href="${pageContext.request.contextPath}/movie-detail?id=${featuredMovies[1].movieID}">Mua vé ngay</a>
                            </div>
                        </div>

                        <%-- Phim 3 (Bên phải) --%>
                        <div>
                            <div class="featured-pst" style="height:340px;background:#f6ead2;color:#c4161f;border-color:#c4161f">
                                <c:choose>
                                    <c:when test="${not empty featuredMovies[2].posterURL}">
                                        <img src="${featuredMovies[2].posterURL}" alt="${featuredMovies[2].title}" loading="lazy">
                                        <div class="featured-pst-overlay"></div>
                                        <div class="featured-pst-text" style="color:#fff">${featuredMovies[2].title}</div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="featured-pst-text">${featuredMovies[2].title}</div>
                                    </c:otherwise>
                                </c:choose>
                                <span class="featured-bd">${not empty featuredMovies[2].ageRating ? featuredMovies[2].ageRating : 'T13'}</span>
                            </div>
                            <div class="featured-card-info" style="border-top:2px solid #c4161f">
                                <h3 title="${featuredMovies[2].title}">${featuredMovies[2].title}</h3>
                                <div class="featured-meta">${featuredMovies[2].genre} · ${featuredMovies[2].duration} phút</div>
                                <a class="btn-featured" href="${pageContext.request.contextPath}/movie-detail?id=${featuredMovies[2].movieID}">Mua vé</a>
                            </div>
                        </div>
                    </div>
                </c:when>

                <%-- Fallback hiển thị 3 bộ phim chuẩn mockup từ Main.dc.html --%>
                <c:otherwise>
                    <div class="featured-grid">
                        <div>
                            <div class="featured-pst" style="height:340px;background:#0d0b0a;color:#f6ead2;border-color:#c4161f">
                                Bóng Ma<br>Rạp Hát
                                <span class="featured-bd">T18</span>
                            </div>
                            <div class="featured-card-info" style="border-top:2px solid #c4161f">
                                <h3>Bóng Ma Rạp Hát</h3>
                                <div class="featured-meta">Kinh dị · 98 phút</div>
                                <a class="btn-featured" href="#lich-chieu">Mua vé</a>
                            </div>
                        </div>
                        <div>
                            <div class="featured-badge-special">Suất đặc biệt</div>
                            <div class="featured-pst" style="height:440px;background:#c4161f;color:#f6ead2;font-size:46px;box-shadow:0 0 40px rgba(196,22,31,.6);border-color:#fcca46">
                                Đêm Cuối<br>Ở Sài Gòn
                                <span class="featured-bd">T13</span>
                            </div>
                            <div class="featured-card-info" style="border-top:2px solid #fcca46">
                                <h3 style="font-size:24px">Đêm Cuối Ở Sài Gòn</h3>
                                <div class="featured-meta">Tâm lý · 120 phút · 19:30 tối T7</div>
                                <a class="btn-featured" href="#lich-chieu">Mua vé ngay</a>
                            </div>
                        </div>
                        <div>
                            <div class="featured-pst" style="height:340px;background:#f6ead2;color:#c4161f;border-color:#c4161f">
                                Mùa Hè<br>Năm Ấy
                                <span class="featured-bd">T13</span>
                            </div>
                            <div class="featured-card-info" style="border-top:2px solid #c4161f">
                                <h3>Mùa Hè Năm Ấy</h3>
                                <div class="featured-meta">Tình cảm · 112 phút</div>
                                <a class="btn-featured" href="#lich-chieu">Mua vé</a>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <%---- 2. DANH SÁCH PHIM ĐANG CHIẾU / SẮP CHIẾU ----%>
    <section id="dang-chieu">
        <div class="section-inner">
            
            <h2 style="margin-bottom: 24px;">
                <c:choose>
                    <c:when test="${selectedTab == 'coming_soon'}">Phim Sắp Chiếu</c:when>
                    <c:otherwise>Phim Đang Chiếu</c:otherwise>
                </c:choose>
            </h2>

            <%---- 2. BỘ LỌC ĐA NĂNG: TÌM KIẾM, THỂ LOẠI, RẠP, NGÀY ----%>
            <div class="catalog-filter-card" id="lich-chieu">
                <form action="${pageContext.request.contextPath}/movies" method="GET" class="catalog-filter-form" id="movieFilterForm">
                    <input type="hidden" name="tab" value="${selectedTab}">

                    <%---- Tìm kiếm theo tên phim ----%>
                    <div class="filter-input-group">
                        <input type="text" name="keyword" class="filter-ctrl" 
                               placeholder="🔍 Nhập tên phim hoặc diễn viên..." 
                               value="${keyword}">
                    </div>

                    <%---- Lọc theo thể loại ----%>
                    <div class="filter-input-group">
                        <select name="genre" class="filter-ctrl" onchange="document.getElementById('movieFilterForm').submit()">
                            <option value="">-- Tất cả thể loại --</option>
                            <c:choose>
                                <c:when test="${not empty genreList}">
                                    <c:forEach var="g" items="${genreList}">
                                        <option value="${g}" ${selectedGenre == g ? 'selected' : ''}>${g}</option>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:forTokens var="g" items="Hành Động,Phiêu Lưu,Hoạt Hình,Hài,Kinh Dị,Khoa Học Viễn Tưởng,Lãng Mạn,Chính Kịch,Gia Đình,Gây Cấn,Tội Phạm" delims=",">
                                        <option value="${g}" ${selectedGenre == g ? 'selected' : ''}>${g}</option>
                                    </c:forTokens>
                                </c:otherwise>
                            </c:choose>
                        </select>
                    </div>

                    <%---- Lọc theo cụm rạp ----%>
                    <div class="filter-input-group">
                        <c:choose>
                            <c:when test="${selectedTab == 'coming_soon'}">
                                <select class="filter-ctrl filter-disabled" disabled title="Phim sắp chiếu chưa mở bán suất chiếu tại rạp">
                                    <option value="">🏛️ Suất chiếu chưa mở bán</option>
                                </select>
                            </c:when>
                            <c:otherwise>
                                <select name="cinemaId" class="filter-ctrl" onchange="document.getElementById('movieFilterForm').submit()">
                                    <option value="">-- Tất cả cụm rạp --</option>
                                    <c:choose>
                                        <c:when test="${not empty cinemaList}">
                                            <c:forEach var="c" items="${cinemaList}">
                                                <option value="${c.cinemaID}" ${selectedCinemaId == c.cinemaID ? 'selected' : ''}>
                                                    🏛️ ${c.name}
                                                </option>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="">🏛️ Cinetik Landmark 81</option>
                                            <option value="">🏛️ Cinetik Nguyễn Du</option>
                                            <option value="">🏛️ Cinetik Crescent Mall</option>
                                        </c:otherwise>
                                    </c:choose>
                                </select>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <%---- Lọc theo ngày chiếu ----%>
                    <div class="filter-input-group">
                        <c:choose>
                            <c:when test="${selectedTab == 'coming_soon'}">
                                <input type="text" class="filter-ctrl filter-disabled" value="📅 Chưa có lịch chiếu" disabled title="Phim sắp chiếu chưa có lịch chiếu">
                            </c:when>
                            <c:otherwise>
                                <input type="date" name="date" class="filter-ctrl" value="${selectedDate}" 
                                       title="Chọn ngày xem phim" onchange="document.getElementById('movieFilterForm').submit()">
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <%---- Nút lọc & Đặt lại ----%>
                    <div style="display: flex; gap: 8px;">
                        <button type="submit" class="btn-filter-action">
                            Lọc Phim
                        </button>
                        <a href="${pageContext.request.contextPath}/movies?tab=${selectedTab}" class="btn-filter-clear" title="Xóa bộ lọc">
                            ✕
                        </a>
                    </div>
                </form>
            </div>

            <%---- 3. DANH SÁCH THẺ PHIM ----%>
            <c:choose>
                <c:when test="${not empty movieList}">
                    <div class="movie-grid">
                        <c:forEach var="movie" items="${movieList}">
                            <div class="movie-card">
                                <div class="movie-poster">
                                    <c:choose>
                                        <c:when test="${not empty movie.posterURL}">
                                            <img src="${movie.posterURL}" alt="${movie.title}" loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="poster-title">${movie.title}</div>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:if test="${not empty movie.ageRating}">
                                        <span class="movie-badge">${movie.ageRating}</span>
                                    </c:if>
                                </div>
                                <div class="movie-info">
                                    <h3>${movie.title}</h3>
                                    <div class="movie-meta">
                                        <c:if test="${not empty movie.genre}">${movie.genre} · </c:if>${movie.duration} phút
                                    </div>
                                    <c:choose>
                                        <c:when test="${selectedTab == 'coming_soon'}">
                                            <a class="btn-ticket" style="background: #2a2220; color: #fcca46; border: 1px solid #fcca46;"
                                               href="${pageContext.request.contextPath}/movie-detail?id=${movie.movieID}">
                                                Thông tin phim
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="btn-ticket"
                                               href="${pageContext.request.contextPath}/movie-detail?id=${movie.movieID}">
                                                Chọn suất chiếu
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>

                <%---- Trường hợp không tìm thấy phim nào khớp bộ lọc ----%>
                <c:otherwise>
                    <div class="empty-state" style="background: #191614; border: 1px solid #2d2824; border-radius: 12px; padding: 48px 24px; max-width: 600px; margin: 0 auto;">
                        <span style="font-size: 40px; display: block; margin-bottom: 12px;">🎭</span>
                        <h3 style="color: #fcca46; margin-bottom: 8px;">Không tìm thấy phim phù hợp</h3>
                        <p style="color: #b5a995; font-size: 14px; margin-bottom: 20px;">
                            Hiện tại không có bộ phim nào khớp với tiêu chí tìm kiếm hoặc rạp/ngày bạn đã chọn.
                        </p>
                        <a href="${pageContext.request.contextPath}/movies?tab=${selectedTab}" class="btn-ticket">
                            Xem tất cả phim
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
