<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Chọn Bắp Nước - Cinetik" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="booking-page-container">
    <div class="booking-main-layout">

        <%---- 1. FILM REEL TICKET BANNER ----%>
        <jsp:include page="/WEB-INF/views/booking/movie-banner.jsp" />

        <%---- 2. BƯỚC TIẾN TRÌNH (STEPPER) ----%>
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

        <%---- 3. NỘI DUNG CHÍNH: DANH SÁCH BẮP NƯỚC TỪ DATABASE + TỔNG TIỀN ----%>
        <div class="booking-content-grid">

            <%---- CỘT TRÁI: DANH SÁCH COMBO TỪ DATABASE ----%>
            <section class="booking-left-panel">
                <div style="border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 14px; margin-bottom: 24px;">
                    <h2 style="font-family: 'Playfair Display', serif; font-size: 22px; color: #fcca46; margin: 0 0 6px 0;">
                        Combo Bắp Nước Ưu Đãi
                    </h2>
                    <p style="font-size: 13.5px; color: #b5a995; margin: 0;">
                        Tiết kiệm đến 20% khi mua kèm combo ăn uống online trước khi vào rạp
                    </p>
                </div>

                <div class="combos-grid" id="combosList">
                    <c:choose>
                        <c:when test="${not empty combos}">
                            <c:forEach items="${combos}" var="c" varStatus="st">
                                <div class="combo-card" data-id="${c.comboID}" data-name="${c.name}" data-price="${c.price}">
                                    <c:choose>
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
                                        <button type="button" class="btn-qty" onclick="changeQty('${c.comboID}', -1)">−</button>
                                        <span class="qty-number" id="qty-${c.comboID}">0</span>
                                        <button type="button" class="btn-qty" onclick="changeQty('${c.comboID}', 1)">+</button>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <%---- Fallback mặc định nếu chưa nạp được combo ----%>
                            <div class="combo-card" data-id="CB_01" data-name="Combo Solo" data-price="65000">
                                <img src="https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=200&auto=format&fit=crop" alt="Combo Solo" class="combo-img">
                                <div class="combo-info">
                                    <span class="combo-name">Combo Solo</span>
                                    <span class="combo-desc">1 Bắp ngọt bơ thơm giòn + 1 Ly nước ngọt mát lạnh 32oz</span>
                                    <span class="combo-price">65.000đ</span>
                                </div>
                                <div class="quantity-control">
                                    <button type="button" class="btn-qty" onclick="changeQty('CB_01', -1)">−</button>
                                    <span class="qty-number" id="qty-CB_01">0</span>
                                    <button type="button" class="btn-qty" onclick="changeQty('CB_01', 1)">+</button>
                                </div>
                            </div>
                            <div class="combo-card" data-id="CB_02" data-name="Combo Couple" data-price="99000">
                                <img src="https://images.unsplash.com/photo-1585647347384-2593bc35786b?w=200&auto=format&fit=crop" alt="Combo Couple" class="combo-img">
                                <div class="combo-info">
                                    <span class="combo-name">Combo Couple</span>
                                    <span class="combo-desc">1 Bắp phô mai lớn + 2 Ly nước ngọt lớn</span>
                                    <span class="combo-price">99.000đ</span>
                                </div>
                                <div class="quantity-control">
                                    <button type="button" class="btn-qty" onclick="changeQty('CB_02', -1)">−</button>
                                    <span class="qty-number" id="qty-CB_02">0</span>
                                    <button type="button" class="btn-qty" onclick="changeQty('CB_02', 1)">+</button>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <%---- CỘT PHẢI: KHUNG VÉ XEM PHIM CỔ ĐIỂN TINH TẾ (ADMIT ONE CINEMA TICKET) ----%>
            <aside class="cinema-ticket-card">
                <div class="ticket-cutout-left"></div>
                <div class="ticket-cutout-right"></div>

                <div class="ticket-header-admit">
                    <span class="ticket-badge-pill">★ Cinetik Booking ★</span>
                    <h2 class="ticket-title-admit">ADMIT ONE</h2>
                    <div class="ticket-subtitle-theater">
                        ${not empty cinema.name ? cinema.name : 'Cinetik Theater'}
                    </div>
                </div>

                <hr class="ticket-perforation">

                <div class="ticket-info-group">
                    <span class="ticket-field-label">MÃ ĐƠN GIỮ CHỖ</span>
                    <div style="font-family: monospace; font-size: 15px; font-weight: 700; color: #fcca46;">
                        ${bookingId}
                    </div>
                </div>

                <div class="ticket-info-group">
                    <span class="ticket-field-label">PHIM CHIẾU</span>
                    <div class="ticket-movie-title">${not empty movie.title ? movie.title : 'Shaun the Sheep: Quái Lạ Ghé Nhà'}</div>
                </div>

                <%---- Danh sách bắp nước đã chọn ----%>
                <div class="ticket-info-group" style="margin-top: 8px;">
                    <span class="ticket-field-label">BẮP NƯỚC ĐÃ CHỌN</span>
                    <div id="selectedCombosSummary" style="font-size: 13px; color: rgba(255, 255, 255, 0.85); background: rgba(0, 0, 0, 0.25); border-radius: 6px; padding: 8px 10px; min-height: 40px;">
                        <span style="color: rgba(255, 255, 255, 0.55);">Chưa chọn bắp nước kèm theo</span>
                    </div>
                </div>

                <%---- Chi tiết tạm tính combo ----%>
                <div class="ticket-pricing-list">
                    <div class="ticket-pricing-row">
                        <span>Tiền bắp nước tạm tính:</span>
                        <strong id="comboTotalText" style="color: #fcca46;">0đ</strong>
                    </div>
                </div>

                <%---- Form gửi sang /booking/saveCombos ----%>
                <form action="${pageContext.request.contextPath}/booking/saveCombos" method="POST" id="comboForm">
                    <input type="hidden" name="bookingId" value="${bookingId}">

                    <%-- Các input số lượng combo với tiền tố combo_{comboID} --%>
                    <div id="hiddenInputsContainer">
                        <c:forEach items="${combos}" var="c">
                            <input type="hidden" name="combo_${c.comboID}" id="input_${c.comboID}" value="0">
                        </c:forEach>
                    </div>

                    <button type="submit" class="btn-ticket-submit">
                        TIẾP TỤC · THANH TOÁN →
                    </button>
                    <button type="button" onclick="submitNoCombos()" class="btn-ticket-back" style="background: none; border: none; cursor: pointer; text-align: center; width: 100%; display: block; margin-top: 8px;">
                        Bỏ qua bắp nước & Tiếp tục →
                    </button>
                </form>

                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ CNK-${bookingId}</div>
                </div>
            </aside>

        </div>
    </div>
</main>

<script>
    function formatVND(n) {
        if (!n || n === 0) return '0đ';
        return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".") + "đ";
    }

    const combosData = {};
    document.querySelectorAll('.combo-card').forEach(function(card) {
        const id = card.getAttribute('data-id');
        const name = card.getAttribute('data-name');
        const price = parseFloat(card.getAttribute('data-price')) || 0;
        if (id) {
            combosData[id] = { name: name, price: price, qty: 0 };
            let hiddenInput = document.getElementById('input_' + id);
            if (!hiddenInput) {
                hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'combo_' + id;
                hiddenInput.id = 'input_' + id;
                hiddenInput.value = '0';
                document.getElementById('hiddenInputsContainer').appendChild(hiddenInput);
            }
        }
    });

    function changeQty(comboId, delta) {
        if (!combosData[comboId]) return;

        let newQty = combosData[comboId].qty + delta;
        if (newQty < 0) newQty = 0;
        if (newQty > 10) newQty = 10;

        combosData[comboId].qty = newQty;
        const qtySpan = document.getElementById('qty-' + comboId);
        if (qtySpan) qtySpan.textContent = newQty;

        const inputEl = document.getElementById('input_' + comboId);
        if (inputEl) inputEl.value = newQty;

        updateComboSummary();
    }

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
            summaryBox.innerHTML = '<strong style="color: #fcca46;">Đã chọn:</strong><br>' + chosenList.join('<br>');
        }

        document.getElementById('comboTotalText').textContent = formatVND(comboTotal);
    }

    function submitNoCombos() {
        // Reset all quantities to 0
        Object.keys(combosData).forEach(id => {
            const inputEl = document.getElementById('input_' + id);
            if (inputEl) inputEl.value = '0';
        });
        document.getElementById('comboForm').submit();
    }

    updateComboSummary();
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
