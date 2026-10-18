<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Chọn Ghế - ${not empty movie.title ? movie.title : 'Cinetik'}" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="booking-page-container">
    <div class="booking-main-layout">


        <%---- 2. FILM REEL TICKET BANNER (BĂNG CUỘN VÉ PHIM CỔ ĐIỂN) ----%>
        <jsp:include page="/WEB-INF/views/booking/movie-banner.jsp" />

        <%---- 3. BƯỚC TIẾN TRÌNH (STEPPER) ----%>
        <nav class="booking-stepper">
            <div class="step-item active">
                <span class="step-circle">1</span>
                <span>Chọn ghế</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
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

        <%---- 4. NỘI DUNG CHÍNH: SƠ ĐỒ GHẾ + TẠM TÍNH ----%>
        <div class="booking-content-grid">
            
            <%---- CỘT TRÁI: SƠ ĐỒ GHẾ ----%>
            <section class="booking-left-panel">
                
                <%---- Chú thích loại ghế ----%>
                <div class="seat-legend">
                    <div class="legend-item">
                        <span class="legend-box selected"></span>
                        <span>Đang chọn</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-box sold"></span>
                        <span>Đã bán</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-box standard"></span>
                        <span>Standard 45.000đ</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-box vip"></span>
                        <span>VIP 55.000đ</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-box couple"></span>
                        <span>Couple 100.000đ</span>
                    </div>
                </div>

                <%---- Màn hình chiếu cong ----%>
                <div class="screen-container">
                    <div class="screen-curve"></div>
                    <span class="screen-label">MÀN HÌNH CHIẾU</span>
                </div>

                <%---- Lưới ghế trực quan ----%>
                <div class="seats-grid-wrapper" id="seatsGrid">
                    
                    <%---- Hàng A (Standard - 45.000đ) ----%>
                    <div class="seat-row">
                        <span class="row-label">A</span>
                        <c:forEach var="c" begin="1" end="12">
                            <div class="seat-cell standard ${c == 3 || c == 4 ? 'sold' : ''}" 
                                 data-id="A${c}" data-type="STANDARD" data-price="45000">A${c}</div>
                        </c:forEach>
                        <span class="row-label">A</span>
                    </div>

                    <%---- Hàng B (Standard - 45.000đ) ----%>
                    <div class="seat-row">
                        <span class="row-label">B</span>
                        <c:forEach var="c" begin="1" end="12">
                            <div class="seat-cell standard" 
                                 data-id="B${c}" data-type="STANDARD" data-price="45000">B${c}</div>
                        </c:forEach>
                        <span class="row-label">B</span>
                    </div>

                    <%---- Hàng C (Standard - 45.000đ) ----%>
                    <div class="seat-row">
                        <span class="row-label">C</span>
                        <c:forEach var="c" begin="1" end="12">
                            <div class="seat-cell standard ${c == 6 || c == 7 ? 'sold' : ''}" 
                                 data-id="C${c}" data-type="STANDARD" data-price="45000">C${c}</div>
                        </c:forEach>
                        <span class="row-label">C</span>
                    </div>

                    <%---- Hàng D (VIP - 55.000đ) ----%>
                    <div class="seat-row">
                        <span class="row-label">D</span>
                        <c:forEach var="c" begin="1" end="12">
                            <div class="seat-cell vip" 
                                 data-id="D${c}" data-type="VIP" data-price="55000">D${c}</div>
                        </c:forEach>
                        <span class="row-label">D</span>
                    </div>

                    <%---- Hàng E (VIP - 55.000đ) ----%>
                    <div class="seat-row">
                        <span class="row-label">E</span>
                        <c:forEach var="c" begin="1" end="12">
                            <div class="seat-cell vip ${c == 5 || c == 6 ? 'sold' : ''}" 
                                 data-id="E${c}" data-type="VIP" data-price="55000">E${c}</div>
                        </c:forEach>
                        <span class="row-label">E</span>
                    </div>

                    <%---- Hàng F (VIP - 55.000đ) ----%>
                    <div class="seat-row">
                        <span class="row-label">F</span>
                        <c:forEach var="c" begin="1" end="12">
                            <div class="seat-cell vip" 
                                 data-id="F${c}" data-type="VIP" data-price="55000">F${c}</div>
                        </c:forEach>
                        <span class="row-label">F</span>
                    </div>

                    <%---- Hàng G (VIP - 55.000đ) ----%>
                    <div class="seat-row">
                        <span class="row-label">G</span>
                        <c:forEach var="c" begin="1" end="12">
                            <div class="seat-cell vip" 
                                 data-id="G${c}" data-type="VIP" data-price="55000">G${c}</div>
                        </c:forEach>
                        <span class="row-label">G</span>
                    </div>

                    <%---- Hàng H (Couple Đôi Sweetbox - 100.000đ) ----%>
                    <div class="seat-row" style="margin-top: 8px;">
                        <span class="row-label">H</span>
                        <div class="seat-cell couple" data-id="H1-H2" data-type="COUPLE" data-price="100000">H1-H2</div>
                        <div class="seat-cell couple" data-id="H3-H4" data-type="COUPLE" data-price="100000">H3-H4</div>
                        <div class="seat-cell couple sold" data-id="H5-H6" data-type="COUPLE" data-price="100000">H5-H6</div>
                        <div class="seat-cell couple" data-id="H7-H8" data-type="COUPLE" data-price="100000">H7-H8</div>
                        <span class="row-label">H</span>
                    </div>

                </div>
            </section>

            <%---- CỘT PHẢI: GIỎ HÀNG TẠM TÍNH ----%>
            <%---- CỘT PHẢI: KHUNG VÉ XEM PHIM CỔ ĐIỂN TINH TẾ (ADMIT ONE CINEMA TICKET) ----%>
            <aside class="cinema-ticket-card">
                <%---- Hai vết cắt khuyết bán nguyệt 2 bên mép vé ----%>
                <div class="ticket-cutout-left"></div>
                <div class="ticket-cutout-right"></div>

                <%---- 1. Header vé tinh tế ----%>
                <div class="ticket-header-admit">
                    <span class="ticket-badge-pill">★ Cinetik Booking ★</span>
                    <h2 class="ticket-title-admit">ADMIT ONE</h2>
                    <div class="ticket-subtitle-theater">
                        ${not empty cinema.name ? cinema.name : 'Cinetik'}
                    </div>
                </div>

                <%---- Đường đứt nét xé vé nối 2 vết cắt bán nguyệt ----%>
                <hr class="ticket-perforation">

                <%---- 2. Thông tin phim ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">PHIM CHIẾU</span>
                    <div class="ticket-movie-title">${movie.title}</div>
                </div>

                <%---- 3. Bảng Suất chiếu, Ngày, Phòng chiếu (3 cột riêng biệt) ----%>
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

                <%---- 4. Ghế đã chọn dạng thẻ nổi bật ----%>
                <div class="ticket-info-group">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <span class="ticket-field-label" id="selectedCountText" style="margin-bottom: 0;">GHẾ CỦA BẠN (0)</span>
                        <button type="button" class="btn-clear-seats" id="btnClearSeats" onclick="clearAllSeats()" style="color: #fcca46; font-size: 11.5px;">Xoá hết</button>
                    </div>
                    <div class="ticket-seats-list" id="selectedSeatsPills">
                        <span style="color: rgba(255, 255, 255, 0.6); font-size: 13px;">Chưa có ghế nào được chọn</span>
                    </div>
                </div>

                <%---- Chi tiết tạm tính ----%>
                <div class="ticket-pricing-list">
                    <div class="ticket-pricing-row">
                        <span>Số lượng vé:</span>
                        <strong id="ticketQuantityText">0 vé</strong>
                    </div>
                    <div class="ticket-pricing-row">
                        <span>Giá vé cơ bản:</span>
                        <strong id="seatPriceDetail">0đ</strong>
                    </div>
                </div>

                <%---- 5. Khung tổng cộng tiền thanh toán sang trọng ----%>
                <div class="ticket-total-box">
                    <span class="ticket-total-label">TẠM TÍNH</span>
                    <span class="ticket-total-amount" id="totalPriceDisplay">0đ</span>
                </div>

                <%---- 6. Form và Nút tiếp tục ----%>
                <form action="${pageContext.request.contextPath}/booking/combo" method="POST" id="seatSelectionForm">
                    <input type="hidden" name="showtimeId" value="${not empty showtime.showtimeID ? showtime.showtimeID : param.showtimeId}">
                    <input type="hidden" name="selectedSeats" id="selectedSeatsInput">
                    <input type="hidden" name="totalSeatPrice" id="totalSeatPriceInput">

                    <button type="submit" class="btn-ticket-submit" id="btnProceedCombo" disabled>
                        TIẾP TỤC · BẮP NƯỚC →
                    </button>
                    <a href="${pageContext.request.contextPath}/movie-detail?id=${not empty movie.movieID ? movie.movieID : param.showtimeId}" class="btn-ticket-back">
                        ← Quay lại chọn suất chiếu
                    </a>
                </form>

                <%---- 7. Mã vạch Barcode & Mã số vé ----%>
                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ CNK-2026-88921-VIP</div>
                </div>
            </aside>

        </div>

    </div>
</main>

<script>
    // Hàm định dạng tiền tệ Việt Nam thống nhất với dấu chấm ngăn cách
    function formatVND(n) {
        if (!n || n === 0) return '0đ';
        return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".") + "đ";
    }

    // Quản lý ghế đã chọn trong bộ nhớ
    const selectedSeats = new Map(); // key: seatId, value: { price, type }

    const seatsGrid = document.getElementById('seatsGrid');
    const pillsContainer = document.getElementById('selectedSeatsPills');
    const countText = document.getElementById('selectedCountText');
    const qtyText = document.getElementById('ticketQuantityText');
    const priceDisplay = document.getElementById('totalPriceDisplay');
    const seatPriceDetail = document.getElementById('seatPriceDetail');
    const proceedBtn = document.getElementById('btnProceedCombo');
    const seatsInput = document.getElementById('selectedSeatsInput');
    const priceInput = document.getElementById('totalSeatPriceInput');

    // Bắt sự kiện click chọn ghế
    seatsGrid.addEventListener('click', function(e) {
        const cell = e.target.closest('.seat-cell');
        if (!cell || cell.classList.contains('sold')) return;

        const id = cell.dataset.id;
        const price = parseInt(cell.dataset.price, 10);
        const type = cell.dataset.type;

        if (cell.classList.contains('selected')) {
            cell.classList.remove('selected');
            selectedSeats.delete(id);
        } else {
            // Giới hạn tối đa 8 ghế / lần đặt
            if (selectedSeats.size >= 8) {
                alert('Mỗi lần đặt vé bạn chỉ có thể chọn tối đa 8 ghế!');
                return;
            }
            cell.classList.add('selected');
            selectedSeats.set(id, { price, type });
        }

        updateUI();
    });

    function updateUI() {
        pillsContainer.innerHTML = '';
        let totalPrice = 0;
        const seatIds = [];

        if (selectedSeats.size === 0) {
            pillsContainer.innerHTML = '<span style="color: rgba(255, 255, 255, 0.6); font-size: 13px;">Chưa có ghế nào được chọn</span>';
            proceedBtn.disabled = true;
        } else {
            selectedSeats.forEach((data, id) => {
                totalPrice += data.price;
                seatIds.push(id);

                const pill = document.createElement('span');
                pill.className = 'ticket-seat-tag';
                pill.innerHTML = 'Ghế ' + id + ' <span style="cursor: pointer; margin-left: 4px;" onclick="removeSeat(\'' + id + '\')">&times;</span>';
                pillsContainer.appendChild(pill);
            });
            proceedBtn.disabled = false;
        }

        const count = selectedSeats.size;
        countText.textContent = 'GHẾ CỦA BẠN (' + count + ')';
        qtyText.textContent = count + ' vé';
        seatPriceDetail.textContent = formatVND(totalPrice);
        priceDisplay.textContent = formatVND(totalPrice);

        // Gán vào form hidden
        seatsInput.value = seatIds.join(',');
        priceInput.value = totalPrice;
    }

    function removeSeat(id) {
        const cell = document.querySelector('.seat-cell[data-id="' + id + '"]');
        if (cell) cell.classList.remove('selected');
        selectedSeats.delete(id);
        updateUI();
    }

    function clearAllSeats() {
        document.querySelectorAll('.seat-cell.selected').forEach(c => c.classList.remove('selected'));
        selectedSeats.clear();
        updateUI();
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
