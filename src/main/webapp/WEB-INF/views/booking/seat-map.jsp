<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Sơ Đồ Ghế - ${not empty movie.title ? movie.title : 'Cinetik'}" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="booking-page-container">
    <div class="booking-main-layout">

        <%---- 1. THÔNG BÁO LỖI (NẾU CÓ) ----%>
        <c:if test="${param.error == 'occupied'}">
            <div style="background: rgba(196, 22, 31, 0.2); border: 1px solid #c4161f; color: #ffb4b4; padding: 12px 20px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; text-align: center;">
                ⚠️ <strong>Ghế bạn vừa chọn đã có người khác đặt hoặc đang được giữ chỗ!</strong> Vui lòng chọn ghế khác.
            </div>
        </c:if>
        <c:if test="${param.error == 'no_seats_selected'}">
            <div style="background: rgba(252, 202, 70, 0.2); border: 1px solid #fcca46; color: #fcca46; padding: 12px 20px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; text-align: center;">
                ⚠️ Vui lòng chọn ít nhất một ghế để tiếp tục đặt vé!
            </div>
        </c:if>

        <%---- 2. FILM REEL TICKET BANNER ----%>
        <c:catch var="bannerErr">
            <jsp:include page="/WEB-INF/views/booking/movie-banner.jsp" />
        </c:catch>

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
                        <span>Đã bán / Đang giữ</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-box standard"></span>
                        <span>Standard 45.000đ</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-box vip"></span>
                        <span>VIP 65.000đ</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-box couple"></span>
                        <span>Couple 95.000đ</span>
                    </div>
                </div>

                <%---- Màn hình chiếu cong ----%>
                <div class="screen-container">
                    <div class="screen-curve"></div>
                    <span class="screen-label">MÀN HÌNH CHIẾU</span>
                </div>

                <%---- Lưới ghế trực quan render động từ List<Seat> ----%>
                <div class="seats-grid-wrapper" id="seatsGrid">
                    <c:choose>
                        <%-- Trường hợp có dữ liệu ghế từ Controller (request.getAttribute("seats")) --%>
                        <c:when test="${not empty seats}">
                            <c:set var="currentRow" value="" />
                            <c:forEach var="seat" items="${seats}" varStatus="status">
                                <c:if test="${seat.row != currentRow}">
                                    <c:if test="${not empty currentRow}">
                                        <span class="row-label">${currentRow}</span>
                                        </div>
                                    </c:if>
                                    <c:set var="currentRow" value="${seat.row}" />
                                    <div class="seat-row">
                                        <span class="row-label">${currentRow}</span>
                                </c:if>

                                <c:set var="basePrice" value="${seat.type == 'COUPLE' ? 95000 : (seat.type == 'VIP' ? 65000 : 45000)}" />
                                <c:set var="computedPrice" value="${seat.extraPrice > 0 ? (45000 + seat.extraPrice) : basePrice}" />

                                <div class="seat-cell ${seat.type.toLowerCase()} ${!seat.available ? 'sold' : ''}" 
                                     data-id="${seat.seatID}" 
                                     data-type="${seat.type}" 
                                     data-price="${computedPrice}">
                                    ${seat.seatID}
                                </div>

                                <c:if test="${status.last}">
                                    <span class="row-label">${currentRow}</span>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </c:when>
                        <%-- Dự phòng sơ đồ mặc định nếu chưa truyền seats --%>
                        <c:otherwise>
                            <c:forEach var="rowLetter" items="${['A', 'B', 'C', 'D']}">
                                <div class="seat-row">
                                    <span class="row-label">${rowLetter}</span>
                                    <c:forEach var="c" begin="1" end="5">
                                        <c:set var="sType" value="${rowLetter == 'D' ? 'couple' : (rowLetter == 'C' ? 'vip' : 'standard')}" />
                                        <c:set var="sPrice" value="${rowLetter == 'D' ? 95000 : (rowLetter == 'C' ? 65000 : 45000)}" />
                                        <div class="seat-cell ${sType}" 
                                             data-id="${rowLetter}${c}" 
                                             data-type="${sType.toUpperCase()}" 
                                             data-price="${sPrice}">
                                            ${rowLetter}${c}
                                        </div>
                                    </c:forEach>
                                    <span class="row-label">${rowLetter}</span>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <%---- CỘT PHẢI: KHUNG VÉ VÀ NÚT TIẾP TỤC ----%>
            <aside class="cinema-ticket-card">
                <div class="ticket-cutout-left"></div>
                <div class="ticket-cutout-right"></div>

                <div class="ticket-header-admit">
                    <span class="ticket-badge-pill">★ Cinetik Booking ★</span>
                    <h2 class="ticket-title-admit">ADMIT ONE</h2>
                    <div class="ticket-subtitle-theater">
                        ${not empty cinema.name ? cinema.name : 'N3 Cinema Center'}
                    </div>
                </div>

                <hr class="ticket-perforation">

                <div class="ticket-info-group">
                    <span class="ticket-field-label">PHIM CHIẾU</span>
                    <div class="ticket-movie-title">${not empty movie.title ? movie.title : 'Avengers: Doomsday'}</div>
                </div>

                <div class="ticket-meta-box">
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">SUẤT</span>
                        <div class="ticket-meta-val">${not empty formattedTime ? formattedTime : '19:00'}</div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">NGÀY</span>
                        <div class="ticket-meta-val">${not empty formattedDate ? formattedDate : 'Hôm nay'}</div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">PHÒNG</span>
                        <div class="ticket-meta-val">${not empty roomNumber ? roomNumber : '01'}</div>
                    </div>
                </div>

                <div class="ticket-info-group">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <span class="ticket-field-label" id="selectedCountText" style="margin-bottom: 0;">GHẾ CỦA BẠN (0)</span>
                        <button type="button" class="btn-clear-seats" id="btnClearSeats" onclick="clearAllSeats()" style="color: #fcca46; font-size: 11.5px; background: none; border: none; cursor: pointer;">Xoá hết</button>
                    </div>
                    <div class="ticket-seats-list" id="selectedSeatsPills">
                        <span style="color: rgba(255, 255, 255, 0.6); font-size: 13px;">Chưa có ghế nào được chọn</span>
                    </div>
                </div>

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

                <div class="ticket-total-box">
                    <span class="ticket-total-label">TẠM TÍNH</span>
                    <span class="ticket-total-amount" id="totalPriceDisplay">0đ</span>
                </div>

                <%---- FORM SUBMIT SANG /booking/hold ĐỂ GIỮ CHỖ ----%>
                <form action="${pageContext.request.contextPath}/booking/hold" method="POST" id="seatSelectionForm">
                    <input type="hidden" name="showtimeId" value="${not empty showtimeId ? showtimeId : param.showtimeId}">
                    <div id="hiddenSeatsContainer"></div>
                    <input type="hidden" name="selectedSeats" id="selectedSeatsInput">
                    <input type="hidden" name="totalSeatPrice" id="totalSeatPriceInput">

                    <button type="submit" class="btn-ticket-submit" id="btnProceedCombo" disabled>
                        TIẾP TỤC · BẮP NƯỚC →
                    </button>
                    <a href="${pageContext.request.contextPath}/movie-detail?id=${not empty movie.movieID ? movie.movieID : param.showtimeId}" class="btn-ticket-back">
                        ← Quay lại chọn suất chiếu
                    </a>
                </form>

                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ CNK-2026-88921-VIP</div>
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

    const selectedSeats = new Map();
    const seatsGrid = document.getElementById('seatsGrid');
    const pillsContainer = document.getElementById('selectedSeatsPills');
    const countText = document.getElementById('selectedCountText');
    const qtyText = document.getElementById('ticketQuantityText');
    const priceDisplay = document.getElementById('totalPriceDisplay');
    const seatPriceDetail = document.getElementById('seatPriceDetail');
    const proceedBtn = document.getElementById('btnProceedCombo');
    const seatsInput = document.getElementById('selectedSeatsInput');
    const priceInput = document.getElementById('totalSeatPriceInput');
    const hiddenSeatsContainer = document.getElementById('hiddenSeatsContainer');

    if (seatsGrid) {
        seatsGrid.addEventListener('click', function(e) {
            const cell = e.target.closest('.seat-cell');
            if (!cell || cell.classList.contains('sold')) return;

            const id = cell.dataset.id;
            const price = parseInt(cell.dataset.price, 10) || 45000;
            const type = cell.dataset.type;

            if (cell.classList.contains('selected')) {
                cell.classList.remove('selected');
                selectedSeats.delete(id);
            } else {
                if (selectedSeats.size >= 8) {
                    alert('Mỗi lần đặt vé bạn chỉ có thể chọn tối đa 8 ghế!');
                    return;
                }
                cell.classList.add('selected');
                selectedSeats.set(id, { price, type });
            }

            updateUI();
        });
    }

    function updateUI() {
        pillsContainer.innerHTML = '';
        if (hiddenSeatsContainer) hiddenSeatsContainer.innerHTML = '';
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

                // Tạo thẻ hidden input cho từng phần tử selectedSeats (request.getParameterValues)
                if (hiddenSeatsContainer) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'selectedSeats';
                    input.value = id;
                    hiddenSeatsContainer.appendChild(input);
                }
            });
            proceedBtn.disabled = false;
        }

        const count = selectedSeats.size;
        countText.textContent = 'GHẾ CỦA BẠN (' + count + ')';
        qtyText.textContent = count + ' vé';
        seatPriceDetail.textContent = formatVND(totalPrice);
        priceDisplay.textContent = formatVND(totalPrice);

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
