<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Chọn Bắp Nước - Cinetik" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="booking-page-container">
    <div class="booking-main-layout">

        <%---- 2. FILM REEL TICKET BANNER ----%>
        <jsp:include page="/WEB-INF/views/booking/movie-banner.jsp" />

        <%---- 3. BƯỚC TIẾN TRÌNH (STEPPER) ----%>
        <nav class="booking-stepper">
            <div class="step-item completed">
                <span class="step-circle">✓</span>
                <span>Chọn ghế</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item active">
                <span class="step-circle">2</span>
                <span>Bắp nước</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
                <span class="step-circle">3</span>
                <span>Thanh toán</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
                <span class="step-circle">4</span>
                <span>Thông tin vé</span>
            </div>
        </nav>

        <%---- 4. NỘI DUNG CHÍNH: DANH SÁCH BẮP NƯỚC + TỔNG TIỀN ----%>
        <div class="booking-content-grid">

            <%---- CỘT TRÁI: DANH SÁCH COMBO ----%>
            <section class="booking-left-panel">
                <div style="border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 14px; margin-bottom: 24px;">
                    <h2 style="font-family: 'Playfair Display', serif; font-size: 22px; color: #fcca46; margin: 0 0 6px 0;">
                        Combo Bắp Nước Ưu Đãi
                    </h2>
                    <p style="font-size: 13.5px; color: #b5a995; margin: 0;">
                        Tiết kiệm đến 20% khi mua kèm combo ăn uống online trước khi vào rạp
                    </p>
                </div>

                <%---- KHUNG DANH SÁCH COMBO ĐỘNG ----%>
                <div class="combos-grid" id="combosList">
                    <%-- KHI CÓ DỮ LIỆU ĐỘNG TỪ JAVA BACKEND (HỖ TRỢ CẢ COMBOS VÀ COMBOLIST) --%>
                    <c:set var="activeCombos" value="${not empty combos ? combos : comboList}" />
                    <c:if test="${not empty activeCombos}">
                        <c:forEach items="${activeCombos}" var="c" varStatus="st">
                            <c:set var="cid" value="${not empty c.comboID ? c.comboID : (not empty c.id ? c.id : c.comboid)}" />
                            <c:set var="cimg" value="${not empty c.imageURL ? c.imageURL : (not empty c.imageUrl ? c.imageUrl : c.img)}" />
                            <div class="combo-card" data-id="${cid}" data-name="${c.name}" data-price="${c.price}">
                                <c:choose>
                                    <c:when test="${not empty cimg}">
                                        <img src="${cimg}" alt="${c.name}" class="combo-img">
                                    </c:when>
                                    <c:when test="${st.index % 5 == 0}">
                                        <img src="https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=200&auto=format&fit=crop" alt="${c.name}" class="combo-img">
                                    </c:when>
                                    <c:when test="${st.index % 5 == 1}">
                                        <img src="https://images.unsplash.com/photo-1585647347384-2593bc35786b?w=200&auto=format&fit=crop" alt="${c.name}" class="combo-img">
                                    </c:when>
                                    <c:when test="${st.index % 5 == 2}">
                                        <img src="https://images.unsplash.com/photo-1512149177596-f817c7ef5d4c?w=200&auto=format&fit=crop" alt="${c.name}" class="combo-img">
                                    </c:when>
                                    <c:when test="${st.index % 5 == 3}">
                                        <img src="https://images.unsplash.com/photo-1505686994434-e3cc5abf1330?w=200&auto=format&fit=crop" alt="${c.name}" class="combo-img">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=200&auto=format&fit=crop" alt="${c.name}" class="combo-img">
                                    </c:otherwise>
                                </c:choose>
                                <div class="combo-info">
                                    <span class="combo-name">${c.name}</span>
                                    <span class="combo-desc">${c.description}</span>
                                    <span class="combo-price">
                                        <fmt:formatNumber value="${c.price}" type="number" groupingUsed="true"/>đ
                                    </span>
                                </div>
                                <div class="quantity-control">
                                    <button type="button" class="btn-qty" onclick="changeQty('${cid}', -1)">−</button>
                                    <span class="qty-number" id="qty-${cid}">0</span>
                                    <button type="button" class="btn-qty" onclick="changeQty('${cid}', 1)">+</button>
                                </div>
                            </div>
                        </c:forEach>
                    </c:if>
                </div>
            </section>

            <%---- CỘT PHẢI: KHUNG VÉ XEM PHIM CỔ ĐIỂN (ADMIT ONE CINEMA TICKET) ----%>
            <aside class="cinema-ticket-card">
                <div class="ticket-cutout-left"></div>
                <div class="ticket-cutout-right"></div>

                <%---- 1. Header vé ----%>
                <div class="ticket-header-admit">
                    <span class="ticket-badge-pill">★ Cinetik Booking ★</span>
                    <h2 class="ticket-title-admit">ADMIT ONE</h2>
                    <div class="ticket-subtitle-theater">
                        ${not empty cinema.name ? cinema.name : 'Cinetik Theater'}
                    </div>
                </div>

                <hr class="ticket-perforation">

                <%---- 2. Thông tin phim ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">PHIM CHIẾU</span>
                    <div class="ticket-movie-title">${not empty movie.title ? movie.title : (not empty sessionScope.currentMovieTitle ? sessionScope.currentMovieTitle : 'Suất Chiếu Cinetik')}</div>
                </div>

                <%---- 3. Suất chiếu, Ngày, Phòng ----%>
                <div class="ticket-meta-box">
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">SUẤT</span>
                        <div class="ticket-meta-val">${not empty formattedTime ? formattedTime : 'Hôm nay'}</div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">NGÀY</span>
                        <div class="ticket-meta-val">${not empty formattedDate ? formattedDate : '2026'}</div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">PHÒNG</span>
                        <div class="ticket-meta-val">${not empty roomNumber ? roomNumber : '01'}</div>
                    </div>
                </div>

                <%---- 4. Ghế đã chọn ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">MÃ ĐƠN HÀNG</span>
                    <div style="font-family: monospace; font-size: 14.5px; font-weight: 700; color: #fcca46;">
                        ${not empty bookingId ? bookingId : param.bookingId}
                    </div>
                </div>

                <%---- Chi tiết bắp nước đã chọn ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">BẮP NƯỚC ĐÃ CHỌN</span>
                    <div id="selectedCombosSummary" style="font-size: 13px; line-height: 1.5; color: #e9ecef;">
                        <span style="color: rgba(255, 255, 255, 0.55);">Chưa chọn bắp nước kèm theo</span>
                    </div>
                </div>

                <%---- Chi tiết tạm tính ----%>
                <div class="ticket-pricing-list">
                    <div class="ticket-pricing-row">
                        <span>Tiền vé ghế:</span>
                        <strong id="seatTotalText">0đ</strong>
                    </div>
                    <div class="ticket-pricing-row">
                        <span>Tiền bắp nước:</span>
                        <strong id="comboTotalText">0đ</strong>
                    </div>
                </div>

                <%---- 5. Tổng thanh toán ----%>
                <div class="ticket-total-box">
                    <span class="ticket-total-label">TỔNG CỘNG</span>
                    <span class="ticket-total-amount" id="finalTotalDisplay">0đ</span>
                </div>

                <%---- 6. Form gửi sang Bước 3: Lưu combo & Checkout ----%>
                <form action="${pageContext.request.contextPath}/booking/saveCombos" method="POST" id="comboForm">
                    <input type="hidden" name="bookingId" value="${not empty bookingId ? bookingId : param.bookingId}">
                    <input type="hidden" name="showtimeId" value="${not empty showtimeId ? showtimeId : param.showtimeId}">
                    <input type="hidden" name="totalSeatPrice" id="inputSeatPrice" value="${not empty rawTotal ? rawTotal : 0}">
                    <input type="hidden" name="selectedCombos" id="selectedCombosInput">
                    <input type="hidden" name="totalComboPrice" id="inputComboPrice" value="0">
                    <input type="hidden" name="finalTotalPrice" id="inputFinalPrice">
                    <div id="hiddenCombosInputs"></div>

                    <button type="submit" class="btn-ticket-submit">
                        TIẾP TỤC · THANH TOÁN →
                    </button>
                    <a href="${pageContext.request.contextPath}/booking/seats?showtimeId=${not empty showtimeId ? showtimeId : param.showtimeId}" class="btn-ticket-back">
                        ← Quay lại chọn ghế
                    </a>
                </form>

                <%---- 7. Barcode vé ----%>
                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ CNK-${not empty bookingId ? bookingId : 'BOOKING'}</div>
                </div>
            </aside>

        </div>

    </div>
</main>

<script>
    // 1. Hàm định dạng tiền tệ VNĐ
    function formatVND(n) {
        if (!n || n === 0) return '0đ';
        return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".") + "đ";
    }

    // 2. TỰ ĐỘNG ĐỌC DỮ LIỆU TỪ ADMIN (LOCALSTORAGE) HOẶC DÙNG MẪU MẶC ĐỊNH NẾU KHÔNG CÓ TỪ BACKEND
    var container = document.getElementById('combosList');
    if (container && !container.querySelector('.combo-card')) {
        var defaultCombos = [
            { id: 'CB_01', name: 'Combo Solo', price: 65000, img: 'https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=200&auto=format&fit=crop', desc: '1 Bắp ngọt bơ thơm giòn (60oz) + 1 Ly nước ngọt mát lạnh tự chọn (32oz)' },
            { id: 'CB_02', name: 'Combo Đôi (Couple)', price: 95000, img: 'https://images.unsplash.com/photo-1585647347384-2593bc35786b?w=200&auto=format&fit=crop', desc: '1 Bắp lớn khổng lồ (vị phô mai hoặc caramel) + 2 Ly nước ngọt mát lạnh (32oz)' },
            { id: 'CB_03', name: 'Combo Gia Đình (Family)', price: 145000, img: 'https://images.unsplash.com/photo-1512149177596-f817c7ef5d4c?w=200&auto=format&fit=crop', desc: '2 Hộp bắp ngọt lớn + 3 Ly nước ngọt + 1 Gói Snack khoai tây chiên giòn Lay\'s' },
            { id: 'CB_04', name: 'Nước Ngọt Siêu Lớn', price: 35000, img: 'https://images.unsplash.com/photo-1505686994434-e3cc5abf1330?w=200&auto=format&fit=crop', desc: '1 Ly nước ngọt có gas mát lạnh cỡ lớn 32oz' },
            { id: 'CB_05', name: 'Snack Khoai Tây Chiên', price: 30000, img: 'https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=200&auto=format&fit=crop', desc: 'Snack khoai tây giòn rụm thơm ngon ăn kèm khi xem phim' }
        ];

        var storedCombos = [];
        try {
            storedCombos = JSON.parse(localStorage.getItem('cinema_admin_combos'));
        } catch (e) {}

        var displayList = (storedCombos && storedCombos.length > 0) ? storedCombos : defaultCombos;

        var html = '';
        for (var i = 0; i < displayList.length; i++) {
            var item = displayList[i];
            var imgUrl = item.img || item.imageUrl || item.imageURL || '';
            var desc = item.desc || item.description || '';

            html += '<div class="combo-card" data-id="' + item.id + '" data-name="' + item.name + '" data-price="' + item.price + '">'
                  + '    <img src="' + imgUrl + '" alt="' + item.name + '" class="combo-img">'
                  + '    <div class="combo-info">'
                  + '        <span class="combo-name">' + item.name + '</span>'
                  + '        <span class="combo-desc">' + desc + '</span>'
                  + '        <span class="combo-price">' + formatVND(item.price) + '</span>'
                  + '    </div>'
                  + '    <div class="quantity-control">'
                  + '        <button type="button" class="btn-qty" onclick="changeQty(\'' + item.id + '\', -1)">−</button>'
                  + '        <span class="qty-number" id="qty-' + item.id + '">0</span>'
                  + '        <button type="button" class="btn-qty" onclick="changeQty(\'' + item.id + '\', 1)">+</button>'
                  + '    </div>'
                  + '</div>';
        }
        container.innerHTML = html;
    }

    // 3. Khởi tạo đối tượng lưu trữ số lượng các món bắp nước
    const combosData = {};
    document.querySelectorAll('.combo-card').forEach(function(card) {
        const id = card.getAttribute('data-id');
        const name = card.getAttribute('data-name');
        const price = parseInt(card.getAttribute('data-price'), 10) || 0;
        if (id) {
            combosData[id] = { name: name, price: price, qty: 0 };
        }
    });

    const seatBasePrice = parseInt(document.getElementById('inputSeatPrice').value, 10) || 0;
    if (document.getElementById('seatTotalText')) {
        document.getElementById('seatTotalText').textContent = formatVND(seatBasePrice);
    }

    // 4. Hàm tăng giảm số lượng
    function changeQty(comboId, delta) {
        if (!combosData[comboId]) return;

        let newQty = combosData[comboId].qty + delta;
        if (newQty < 0) newQty = 0;
        if (newQty > 10) newQty = 10;

        combosData[comboId].qty = newQty;
        const el = document.getElementById('qty-' + comboId);
        if (el) el.textContent = newQty;

        updateComboSummary();
    }

    // 5. Cập nhật tổng tiền và thông tin vé
    function updateComboSummary() {
        const summaryBox = document.getElementById('selectedCombosSummary');
        const hiddenContainer = document.getElementById('hiddenCombosInputs');
        if (hiddenContainer) hiddenContainer.innerHTML = '';

        let comboTotal = 0;
        const chosenList = [];

        Object.keys(combosData).forEach(id => {
            const item = combosData[id];
            if (item.qty > 0) {
                const sub = item.qty * item.price;
                comboTotal += sub;
                chosenList.push(item.name + ' × ' + item.qty + ' (' + formatVND(sub) + ')');

                if (hiddenContainer) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'combo_' + id;
                    input.value = item.qty;
                    hiddenContainer.appendChild(input);
                }
            }
        });

        if (chosenList.length === 0) {
            summaryBox.innerHTML = '<span style="color: rgba(255, 255, 255, 0.55);">Chưa chọn bắp nước kèm theo</span>';
        } else {
            summaryBox.innerHTML = '<strong style="color: #fcca46;">Bắp nước đã chọn:</strong><br>' + chosenList.join('<br>');
        }

        const finalTotal = seatBasePrice + comboTotal;

        if (document.getElementById('comboTotalText')) document.getElementById('comboTotalText').textContent = formatVND(comboTotal);
        if (document.getElementById('finalTotalDisplay')) document.getElementById('finalTotalDisplay').textContent = formatVND(finalTotal);

        if (document.getElementById('inputComboPrice')) document.getElementById('inputComboPrice').value = comboTotal;
        if (document.getElementById('inputFinalPrice')) document.getElementById('inputFinalPrice').value = finalTotal;
        if (document.getElementById('selectedCombosInput')) document.getElementById('selectedCombosInput').value = JSON.stringify(combosData);
    }

    // Khởi chạy tính toán ban đầu
    updateComboSummary();
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
