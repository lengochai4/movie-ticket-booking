<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Danh Sách Cụm Rạp - Cinetik" scope="request"/>
<c:set var="extraCss" value="catalog.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main style="max-width: 1300px; margin: 40px auto; padding: 0 24px;">
    <section>
        <div class="section-inner">
            <h2>Hệ Thống Cụm Rạp Chi Nhánh</h2>
            <p style="color: #d9c9a8; margin-top: 12px; margin-bottom: 28px;">
                Khám phá hệ thống hơn 30 cụm rạp Beta Cinemas, CGV và Galaxy Cinema chất lượng quốc tế trên toàn quốc.
            </p>

            <%---- BỘ LỌC TÌM KIẾM RẠP & KHU VỰC ----%>
            <div class="catalog-filter-card" style="margin-bottom: 32px;">
                <div class="catalog-filter-form" style="grid-template-columns: 2fr 1.3fr 1.3fr auto;">
                    <div class="filter-input-group">
                        <input type="text" id="cinemaPageSearch" class="filter-ctrl" 
                               placeholder="🔍 Tìm tên rạp, đường, địa chỉ chi tiết...">
                    </div>
                    <div class="filter-input-group">
                        <select id="cinemaPageCitySelect" class="filter-ctrl">
                            <option value="">-- Tất cả khu vực --</option>
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
                    <div class="filter-input-group">
                        <select id="cinemaPageBrandSelect" class="filter-ctrl">
                            <option value="">-- Tất cả thương hiệu --</option>
                            <option value="Beta">Beta Cinemas</option>
                            <option value="CGV">CGV Cinemas</option>
                            <option value="Galaxy">Galaxy Cinema</option>
                        </select>
                    </div>
                    <div>
                        <button type="button" class="btn-filter-clear" id="btnResetCinemaFilter" title="Xóa bộ lọc">
                            Đặt lại ✕
                        </button>
                    </div>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty cinemas}">
                    <div id="cinemasGrid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 24px; text-align: left;">
                        <c:forEach var="c" items="${cinemas}">
                            <c:set var="nameLower" value="${c.name.toLowerCase()}"/>
                            <c:set var="brand" value="${nameLower.contains('beta') ? 'Beta' : (nameLower.contains('cgv') ? 'CGV' : (nameLower.contains('galaxy') ? 'Galaxy' : 'Cinetik'))}"/>
                            <c:set var="icon" value="${brand == 'Beta' ? '🍿' : (brand == 'CGV' ? '🎬' : (brand == 'Galaxy' ? '✨' : '🏛️'))}"/>
                            <c:set var="badgeClass" value="${brand == 'Beta' ? 'badge-beta' : (brand == 'CGV' ? 'badge-cgv' : (brand == 'Galaxy' ? 'badge-galaxy' : 'badge-cinetik'))}"/>

                            <div class="cinema-card-item" 
                                 data-name="${c.name.toLowerCase()}" 
                                 data-address="${c.address.toLowerCase()}"
                                 data-brand="${brand}"
                                 style="background: #181412; border: 1px solid #2e2621; border-radius: 10px; padding: 22px; display: flex; flex-direction: column; justify-content: space-between; box-shadow: 0 6px 18px rgba(0,0,0,0.6); transition: transform 0.2s, border-color 0.2s;">
                                <div>
                                    <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 12px;">
                                        <div style="display: flex; align-items: center; gap: 10px;">
                                            <span style="font-size: 26px;">${icon}</span>
                                            <h3 style="color: #fcca46; font-size: 18px; margin: 0; font-family: 'Playfair Display', serif;">${c.name}</h3>
                                        </div>
                                        <span class="badge-tag ${badgeClass}">${brand}</span>
                                    </div>
                                    <p style="color: #d9c9a8; font-size: 13.5px; margin-bottom: 18px; line-height: 1.6;">
                                        📍 <strong>Địa chỉ:</strong> ${c.address}
                                    </p>
                                </div>
                                <div style="display: flex; gap: 10px; margin-top: auto;">
                                    <a href="${pageContext.request.contextPath}/movies?cinemaId=${c.cinemaID}" class="btn-ticket" style="font-size: 13px; padding: 9px 16px; width: 100%; text-align: center;">
                                        Xem lịch chiếu tại rạp ➔
                                    </a>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <div id="noCinemaFound" class="empty-state" style="display: none; padding: 48px 0;">
                        <span style="font-size: 40px; display: block; margin-bottom: 12px;">🏛️</span>
                        <h3 style="color: #fcca46;">Không tìm thấy cụm rạp phù hợp</h3>
                        <p style="color: #b5a995; font-size: 14px;">Vui lòng thử tìm kiếm theo tên khác hoặc đổi khu vực.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        Chưa có dữ liệu rạp chiếu phim.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>

<script>
document.addEventListener('DOMContentLoaded', function() {
    var searchInput = document.getElementById('cinemaPageSearch');
    var citySelect = document.getElementById('cinemaPageCitySelect');
    var brandSelect = document.getElementById('cinemaPageBrandSelect');
    var resetBtn = document.getElementById('btnResetCinemaFilter');
    var cards = document.querySelectorAll('.cinema-card-item');
    var noFound = document.getElementById('noCinemaFound');
    var grid = document.getElementById('cinemasGrid');

    function filterCards() {
        var kw = (searchInput ? searchInput.value : '').toLowerCase().trim();
        var city = (citySelect ? citySelect.value : '').toLowerCase().trim();
        var brand = (brandSelect ? brandSelect.value : '').trim();

        var visibleCount = 0;
        cards.forEach(function(card) {
            var name = card.getAttribute('data-name') || '';
            var address = card.getAttribute('data-address') || '';
            var cardBrand = card.getAttribute('data-brand') || '';

            var matchKw = !kw || name.indexOf(kw) !== -1 || address.indexOf(kw) !== -1;
            var matchCity = !city || address.indexOf(city) !== -1;
            var matchBrand = !brand || cardBrand === brand;

            if (matchKw && matchCity && matchBrand) {
                card.style.display = 'flex';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        if (noFound) {
            noFound.style.display = visibleCount === 0 ? 'block' : 'none';
        }
    }

    if (searchInput) searchInput.addEventListener('input', filterCards);
    if (citySelect) citySelect.addEventListener('change', filterCards);
    if (brandSelect) brandSelect.addEventListener('change', filterCards);
    if (resetBtn) {
        resetBtn.addEventListener('click', function() {
            if (searchInput) searchInput.value = '';
            if (citySelect) citySelect.value = '';
            if (brandSelect) brandSelect.value = '';
            filterCards();
        });
    }
});
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
