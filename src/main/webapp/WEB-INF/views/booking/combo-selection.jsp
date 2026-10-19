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
                    <%-- KHI CÓ DỮ LIỆU ĐỘNG TỪ JAVA BACKEND --%>
                    <c:if test="${not empty comboList}">
                        <c:forEach items="${comboList}" var="c">
                            <div class="combo-card" data-id="${c.id}" data-name="${c.name}" data-price="${c.price}">
                                <img src="${c.imageUrl}" alt="${c.name}" class="combo-img">
                                <div class="combo-info">
                                    <span class="combo-name">${c.name}</span>
                                    <span class="combo-desc">${c.description}</span>
                                    <span class="combo-price">
                                        <fmt:formatNumber value="${c.price}" type="number" groupingUsed="true"/>đ
                                    </span>
                                </div>
                                <div class="quantity-control">
                                    <button type="button" class="btn-qty" onclick="changeQty('${c.id}', -1)">−</button>
                                    <span class="qty-number" id="qty-${c.id}">0</span>
                                    <button type="button" class="btn-qty" onclick="changeQty('${c.id}', 1)">+</button>
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
                        ${not empty cinema.name ? cinema.name : 'Cinetik'}
                    </div>
                </div>

                <hr class="ticket-perforation">

                <%---- 2. Thông tin phim ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">PHIM CHIẾU</span>
                    <div class="ticket-movie-title">${movie.title}</div>
                </div>

                <%---- 3. Suất chiếu, Ngày, Phòng ----%>
                <div class="ticket-meta-box">
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">SUẤT</span>
                        <div class="ticket-meta-val">
                            ${not empty formattedTime ? formattedTime : '09:15'}
                        </div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">NGÀY</span>
                        <div class="ticket-meta-val">
                            ${not empty formattedDate ? formattedDate : '16/10/2026'}
                        </div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">PHÒNG</span>
                        <div class="ticket-meta-val">
                            ${not empty roomNumber ? roomNumber : '05'}
                        </div>
                    </div>
                </div>

                <%---- 4. Ghế đã chọn ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">GHẾ ĐÃ CHỌN</span>
                    <div class="ticket-seats-list">
                        <c:choose>
                            <c:when test="${not empty param.selectedSeats}">
                                <c:forTokens items="${param.selectedSeats}" delims="," var="s">
                                    <span class="ticket-seat-tag">Ghế ${s}</span>
                                </c:forTokens>
                            </c:when>
                            <c:otherwise>
                                <span class="ticket-seat-tag">Ghế F5</span>
                                <span class="ticket-seat-tag">Ghế F6</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <%---- Danh sách bắp nước đã chọn ----%>
                <div class="ticket-info-group" style="margin-top: 8px;">
                    <div id="selectedCombosSummary" style="font-size: 13px; color: rgba(255, 255, 255, 0.85); background: rgba(0, 0, 0, 0.25); border-radius: 6px; padding: 8px 10px;">
                        <span style="color: rgba(255, 255, 255, 0.55);">Chưa chọn bắp nước kèm theo</span>
                    </div>
                </div>

                <%---- Chi tiết chi phí ----%>
                <div class="ticket-pricing-list">
                    <div class="ticket-pricing-row">
                        <span>Tiền vé phim:</span>
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

                <%---- 6. Form gửi sang Bước 3: Checkout ----%>
                <form action="${pageContext.request.contextPath}/booking/checkout" method="POST" id="comboForm">
                    <input type="hidden" name="showtimeId" value="${not empty param.showtimeId ? param.showtimeId : 'st-01'}">
                    <input type="hidden" name="selectedSeats" value="${not empty param.selectedSeats ? param.selectedSeats : 'F5,F6'}">
                    <input type="hidden" name="totalSeatPrice" id="inputSeatPrice" value="${not empty param.totalSeatPrice ? param.totalSeatPrice : '110000'}">
                    <input type="hidden" name="selectedCombosData" id="selectedCombosInput">
                    <input type="hidden" name="totalComboPrice" id="inputComboPrice" value="0">
                    <input type="hidden" name="finalTotalPrice" id="inputFinalPrice">

                    <button type="submit" class="btn-ticket-submit">
                        TIẾP TỤC · THANH TOÁN →
                    </button>
                    <a href="${pageContext.request.contextPath}/booking/seats?showtimeId=${not empty param.showtimeId ? param.showtimeId : 'st-01'}" class="btn-ticket-back">
                        ← Quay lại chọn ghế
                    </a>
                </form>

                <%---- 7. Barcode vé ----%>
                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ CNK-2026-88921-VIP</div>
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

    // 2. TỰ ĐỘNG ĐỌC DỮ LIỆU TỪ ADMIN (LOCALSTORAGE) HOẶC DÙNG MẪU MẶC ĐỊNH
    var container = document.getElementById('combosList');
    if (container && !container.querySelector('.combo-card')) {
        var defaultCombos = [
            { id: 'CB01', name: 'Combo Solo', price: 65000, img: 'https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=200&auto=format&fit=crop', desc: '1 Bắp ngọt bơ thơm giòn (60oz) + 1 Ly nước ngọt mát lạnh tự chọn (32oz)' },
            { id: 'CB02', name: 'Combo Đôi (Couple)', price: 89000, img: 'https://images.unsplash.com/photo-1585647347384-2593bc35786b?w=200&auto=format&fit=crop', desc: '1 Bắp lớn khổng lồ (vị phô mai hoặc caramel) + 2 Ly nước ngọt mát lạnh (32oz)' },
            { id: 'CB03', name: 'Combo Gia Đình (Family)', price: 139000, img: 'https://images.unsplash.com/photo-1512149177596-f817c7ef5d4c?w=200&auto=format&fit=crop', desc: '2 Hộp bắp ngọt lớn + 3 Ly nước ngọt + 1 Gói Snack khoai tây chiên giòn Lay\'s' },
            { id: 'CB04', name: 'Combo Party 4 Bạn', price: 185000, img: 'https://images.unsplash.com/photo-1505686994434-e3cc5abf1330?w=200&auto=format&fit=crop', desc: '2 Bắp khổng lồ (bơ & phô mai) + 4 Ly nước ngọt + 2 Xúc xích nướng phô mai thơm ngon' },
            { id: 'CB05', name: 'Bắp Rang Bơ Vàng (60oz)', price: 45000, img: 'https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=200&auto=format&fit=crop', desc: 'Hạt ngô Mỹ nổ vàng ươm, thơm lừng vị bơ béo ngậy giòn tan' }
        ];

        var storedCombos = [];
        try {
            storedCombos = JSON.parse(localStorage.getItem('cinema_admin_combos'));
        } catch (e) {}

        var displayList = (storedCombos && storedCombos.length > 0) ? storedCombos : defaultCombos;

        var html = '';
        for (var i = 0; i < displayList.length; i++) {
            var item = displayList[i];
            var imgUrl = item.img || item.imageUrl || '';
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

    const seatBasePrice = parseInt(document.getElementById('inputSeatPrice').value, 10) || 110000;
    document.getElementById('seatTotalText').textContent = formatVND(seatBasePrice);

    // 4. Hàm tăng giảm số lượng
    function changeQty(comboId, delta) {
        if (!combosData[comboId]) return;

        let newQty = combosData[comboId].qty + delta;
        if (newQty < 0) newQty = 0;
        if (newQty > 10) newQty = 10;

        combosData[comboId].qty = newQty;
        document.getElementById('qty-' + comboId).textContent = newQty;

        updateComboSummary();
    }

    // 5. Cập nhật tổng tiền và thông tin vé
    function updateComboSummary() {
        const summaryBox = document.getElementById('selectedCombosSummary');
        let comboTotal = 0;
        const chosenList = [];

        Object.keys(combosData).forEach(id => {
            const item = combosData[id];
            if (item.qty > 0) {
                const sub = item.qty * item.price;
                comboTotal += sub;
                chosenList.push(item.name + ' × ' + item.qty + ' (' + formatVND(sub) + ')');
            }
        });

        if (chosenList.length === 0) {
            summaryBox.innerHTML = '<span style="color: rgba(255, 255, 255, 0.55);">Chưa chọn bắp nước kèm theo</span>';
        } else {
            summaryBox.innerHTML = '<strong style="color: #fcca46;">Bắp nước đã chọn:</strong><br>' + chosenList.join('<br>');
        }

        const finalTotal = seatBasePrice + comboTotal;

        document.getElementById('comboTotalText').textContent = formatVND(comboTotal);
        document.getElementById('finalTotalDisplay').textContent = formatVND(finalTotal);

        document.getElementById('inputComboPrice').value = comboTotal;
        document.getElementById('inputFinalPrice').value = finalTotal;
        document.getElementById('selectedCombosInput').value = JSON.stringify(combosData);
    }

    // Khởi chạy tính toán ban đầu
    updateComboSummary();
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />