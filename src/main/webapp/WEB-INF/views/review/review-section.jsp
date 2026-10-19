<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%---- ================= CSS CHO KHU VỰC ĐÁNH GIÁ (SELF-CONTAINED) ================= ----%>
<style>
.review-wrapper {
    background: #141211;
    border: 1px solid rgba(252, 202, 70, 0.2);
    border-radius: 12px;
    padding: 36px 32px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6);
    color: #f6ead2;
    font-family: inherit;
    margin-top: 36px;
}

/* 1. Tiêu đề & Tổng quan */
.review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    padding-bottom: 24px;
    margin-bottom: 30px;
    flex-wrap: wrap;
    gap: 20px;
}

.review-title h2 {
    font-family: 'Playfair Display', serif;
    font-size: 26px;
    color: #fcca46;
    margin: 0 0 6px 0;
}

.review-title p {
    font-size: 13.5px;
    color: #b5a995;
    margin: 0;
}

.review-score-box {
    display: flex;
    align-items: center;
    gap: 16px;
    background: #1d1917;
    padding: 12px 20px;
    border-radius: 10px;
    border: 1px solid rgba(252, 202, 70, 0.25);
}

.score-number {
    font-size: 32px;
    font-weight: 800;
    color: #fcca46;
    line-height: 1;
}

.score-details {
    display: flex;
    flex-direction: column;
    gap: 3px;
}

.stars-gold {
    color: #fcca46;
    font-size: 16px;
    letter-spacing: 2px;
}

.score-count {
    font-size: 12px;
    color: #8c8275;
}

/* 2. Form gửi bình luận */
.review-form-card {
    background: #1b1716;
    border: 1px solid #3c3733;
    border-radius: 10px;
    padding: 24px;
    margin-bottom: 36px;
}

.review-form-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 18px;
}

.reviewer-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #780000 0%, #a81c1c 100%);
    color: #fcca46;
    border: 1.5px solid #fcca46;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 16px;
    flex-shrink: 0;
}

.reviewer-name {
    font-size: 15px;
    font-weight: 600;
    color: #f6ead2;
}

/* Bộ chọn số sao tương tác */
.star-rating-picker {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
}

.star-rating-label {
    font-size: 14px;
    color: #b5a995;
}

.star-group {
    display: inline-flex;
    flex-direction: row-reverse;
    gap: 4px;
}

.star-group input[type="radio"] {
    display: none;
}

.star-group label {
    font-size: 24px;
    color: #4a423b;
    cursor: pointer;
    transition: color 0.15s ease, transform 0.15s ease;
}

.star-group label:hover,
.star-group label:hover ~ label,
.star-group input[type="radio"]:checked ~ label {
    color: #fcca46;
    text-shadow: 0 0 10px rgba(252, 202, 70, 0.6);
    transform: scale(1.1);
}

.review-textarea {
    width: 100%;
    min-height: 100px;
    padding: 12px 16px;
    background: #23201e;
    border: 1px solid #3c3733;
    border-radius: 8px;
    color: #f6ead2;
    font-family: inherit;
    font-size: 14px;
    outline: none;
    resize: vertical;
    box-sizing: border-box;
    transition: border-color 0.2s, box-shadow 0.2s;
}

.review-textarea:focus {
    border-color: #fcca46;
    box-shadow: 0 0 0 2px rgba(252, 202, 70, 0.2);
}

.review-textarea::placeholder {
    color: #8c8275;
}

.review-form-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 14px;
}

.btn-submit-review {
    padding: 11px 24px;
    background: #780000;
    color: #fcca46;
    border: 1px solid #fcca46;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    transition: all 0.2s ease;
}

.btn-submit-review:hover {
    background: #920000;
    box-shadow: 0 0 12px rgba(252, 202, 70, 0.4);
    transform: translateY(-1px);
}

/* Hộp thông báo chưa đăng nhập */
.review-login-prompt {
    background: #1b1716;
    border: 1px dashed rgba(252, 202, 70, 0.35);
    border-radius: 10px;
    padding: 24px;
    text-align: center;
    margin-bottom: 36px;
}

.review-login-prompt p {
    font-size: 14.5px;
    color: #b5a995;
    margin: 0 0 14px 0;
}

.btn-login-to-review {
    display: inline-block;
    padding: 10px 22px;
    background: #780000;
    color: #fcca46 !important;
    border: 1px solid #fcca46;
    border-radius: 6px;
    font-size: 13.5px;
    font-weight: 700;
    text-decoration: none;
    transition: all 0.2s;
}

.btn-login-to-review:hover {
    background: #920000;
    box-shadow: 0 0 10px rgba(252, 202, 70, 0.4);
}

/* 3. Danh sách các bình luận */
.review-list-title {
    font-size: 17px;
    font-weight: 700;
    color: #fcca46;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.reviews-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.review-item {
    background: #191615;
    border: 1px solid rgba(255, 255, 255, 0.06);
    border-radius: 10px;
    padding: 20px 24px;
    transition: border-color 0.2s;
}

.review-item:hover {
    border-color: rgba(252, 202, 70, 0.25);
}

.review-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.reviewer-meta {
    display: flex;
    align-items: center;
    gap: 12px;
}

.reviewer-meta-text {
    display: flex;
    flex-direction: column;
}

.reviewer-meta-name {
    font-size: 14.5px;
    font-weight: 600;
    color: #f6ead2;
}

.review-time {
    font-size: 12px;
    color: #8c8275;
}

.review-item-stars {
    color: #fcca46;
    font-size: 14px;
    letter-spacing: 1.5px;
}

.review-content {
    font-size: 14px;
    line-height: 1.65;
    color: #d9c9a8;
    margin: 0 0 12px 0;
}

.review-footer {
    display: flex;
    justify-content: flex-end;
}

.btn-helpful {
    background: transparent;
    border: 1px solid #3c3733;
    color: #8c8275;
    border-radius: 16px;
    padding: 4px 12px;
    font-size: 12px;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: all 0.2s;
}

.btn-helpful:hover {
    border-color: #fcca46;
    color: #fcca46;
}

.btn-helpful.active {
    border-color: #fcca46;
    background: rgba(252, 202, 70, 0.12);
    color: #fcca46;
}

/* Trạng thái chưa có bình luận */
.no-reviews-box {
    text-align: center;
    padding: 36px 16px;
    color: #8c8275;
}
</style>

<%---- ================= GIAO DIỆN KHU VỰC ĐÁNH GIÁ ================= ----%>
<div class="review-wrapper">

    <%---- 1. HEADER: TỔNG QUAN ĐÁNH GIÁ SAO ----%>
    <div class="review-header">
        <div class="review-title">
            <h2>Đánh Giá & Nhận Xét</h2>
            <p>Chia sẻ cảm nghĩ của bạn về bộ phim này cùng cộng đồng khán giả Cinetik</p>
        </div>

        <div class="review-score-box">
            <span class="score-number">${not empty avgRating ? avgRating : '4.8'}</span>
            <div class="score-details">
                <div class="stars-gold">★★★★★</div>
                <span class="score-count">${not empty totalReviews ? totalReviews : '128'} lượt đánh giá</span>
            </div>
        </div>
    </div>

    <%---- 2. FORM GỬI BÌNH LUẬN & CHẤM SAO ----%>
    <c:choose>
        <%-- Khi người dùng ĐÃ ĐĂNG NHẬP --%>
        <c:when test="${not empty sessionScope.loginUser}">
            <div class="review-form-card">
                <div class="review-form-header">
                    <div class="reviewer-avatar">
                        ${sessionScope.loginUser.fullName.substring(0,1).toUpperCase()}
                    </div>
                    <span class="reviewer-name">${sessionScope.loginUser.fullName}</span>
                </div>

                <form action="${pageContext.request.contextPath}/review/add" method="POST" onsubmit="return validateReviewForm();">
                    <%---- ID Phim (lấy từ đối tượng movie cha hoặc tham số param.id) ----%>
                    <input type="hidden" name="movieID" value="${not empty movie.movieID ? movie.movieID : param.id}">

                    <%---- Bộ chọn chấm sao từ 1 đến 5 ----%>
                    <div class="star-rating-picker">
                        <span class="star-rating-label">Bạn chấm phim này mấy sao?</span>
                        <div class="star-group">
                            <input type="radio" id="star5" name="rating" value="5" required>
                            <label for="star5" title="5 sao - Tuyệt đỉnh">★</label>

                            <input type="radio" id="star4" name="rating" value="4">
                            <label for="star4" title="4 sao - Rất hay">★</label>

                            <input type="radio" id="star3" name="rating" value="3">
                            <label for="star3" title="3 sao - Bình thường">★</label>

                            <input type="radio" id="star2" name="rating" value="2">
                            <label for="star2" title="2 sao - Tạm được">★</label>

                            <input type="radio" id="star1" name="rating" value="1">
                            <label for="star1" title="1 sao - Kém">★</label>
                        </div>
                    </div>

                    <%---- Ô viết nhận xét ----%>
                    <textarea name="comment" 
                              id="reviewCommentInput"
                              class="review-textarea" 
                              placeholder="Chia sẻ cảm nhận chi tiết về kịch bản, âm thanh, kỹ xảo hoặc diễn xuất của các diễn viên..." 
                              required></textarea>

                    <div class="review-form-actions">
                        <button type="submit" class="btn-submit-review">Gửi Đánh Giá</button>
                    </div>
                </form>
            </div>
        </c:when>

        <%-- Khi người dùng CHƯA ĐĂNG NHẬP --%>
        <c:otherwise>
            <div class="review-login-prompt">
                <p>🎬 Bạn muốn chấm điểm và viết cảm nhận cho bộ phim này?</p>
                <a href="${pageContext.request.contextPath}/auth/login" class="btn-login-to-review">
                    Đăng Nhập Để Đánh Giá
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <%---- 3. DANH SÁCH BÌNH LUẬN KHÁN GIẢ ----%>
    <div class="review-list-title">
        <span>💬 Bình luận từ khán giả (${not empty reviewList ? reviewList.size() : 3})</span>
    </div>

    <div class="reviews-container">
        <c:choose>
            <c:when test="${not empty reviewList}">
                <c:forEach var="r" items="${reviewList}">
                    <div class="review-item">
                        <div class="review-item-header">
                            <div class="reviewer-meta">
                                <div class="reviewer-avatar" style="width: 36px; height: 36px; font-size: 14px;">
                                    ${not empty r.userName ? r.userName.substring(0,1).toUpperCase() : 'U'}
                                </div>
                                <div class="reviewer-meta-text">
                                    <span class="reviewer-meta-name">${r.userName}</span>
                                    <span class="review-time">${not empty r.createdAt ? r.createdAt : 'Vừa xong'}</span>
                                </div>
                            </div>

                            <div class="review-item-stars">
                                <c:forEach begin="1" end="${r.rating}">★</c:forEach><c:forEach begin="${r.rating + 1}" end="5"><span style="color: #4a423b;">★</span></c:forEach>
                            </div>
                        </div>

                        <p class="review-content">${r.comment}</p>

                        <div class="review-footer">
                            <button type="button" class="btn-helpful" onclick="toggleHelpful(this, ${not empty r.helpfulCount ? r.helpfulCount : 0})">
                                👍 Hữu ích (${not empty r.helpfulCount ? r.helpfulCount : 0})
                            </button>
                        </div>
                    </div>
                </c:forEach>
            </c:when>

            <%-- Dữ liệu hiển thị mẫu khi chưa nối database thực tế --%>
            <c:otherwise>
                <div class="review-item">
                    <div class="review-item-header">
                        <div class="reviewer-meta">
                            <div class="reviewer-avatar" style="width: 36px; height: 36px; font-size: 14px;">T</div>
                            <div class="reviewer-meta-text">
                                <span class="reviewer-meta-name">Trần Hải Đăng</span>
                                <span class="review-time">Hôm qua lúc 20:30</span>
                            </div>
                        </div>
                        <div class="review-item-stars">★★★★★</div>
                    </div>
                    <p class="review-content">Kỹ xảo và âm thanh tại phòng chiếu IMAX của Cinetik thực sự bùng nổ! Đoạn cao trào cuối phim làm mình nổi cả da gà. Rất đáng tiền vé!</p>
                    <div class="review-footer">
                        <button type="button" class="btn-helpful active" onclick="toggleHelpful(this, 18)">
                            👍 Hữu ích (18)
                        </button>
                    </div>
                </div>

                <div class="review-item">
                    <div class="review-item-header">
                        <div class="reviewer-meta">
                            <div class="reviewer-avatar" style="width: 36px; height: 36px; font-size: 14px;">L</div>
                            <div class="reviewer-meta-text">
                                <span class="reviewer-meta-name">Lê Gia Ân</span>
                                <span class="review-time">2 ngày trước</span>
                            </div>
                        </div>
                        <div class="review-item-stars">★★★★<span style="color: #4a423b;">★</span></div>
                    </div>
                    <p class="review-content">Diễn xuất của dàn cast chính xuất sắc, cốt truyện có chiều sâu và nhiều cú twist bất ngờ. Điểm trừ nhẹ là nhịp phim đầu hơi chậm một chút.</p>
                    <div class="review-footer">
                        <button type="button" class="btn-helpful" onclick="toggleHelpful(this, 9)">
                            👍 Hữu ích (9)
                        </button>
                    </div>
                </div>

                <div class="review-item">
                    <div class="review-item-header">
                        <div class="reviewer-meta">
                            <div class="reviewer-avatar" style="width: 36px; height: 36px; font-size: 14px;">P</div>
                            <div class="reviewer-meta-text">
                                <span class="reviewer-meta-name">Phạm Hoàng Nam</span>
                                <span class="review-time">4 ngày trước</span>
                            </div>
                        </div>
                        <div class="review-item-stars">★★★★★</div>
                    </div>
                    <p class="review-content">Phim quá hay, nhạc phim đọng lại nhiều cảm xúc. Cinetik phục vụ bắp nước rất nhanh và ghế ngồi êm ái!</p>
                    <div class="review-footer">
                        <button type="button" class="btn-helpful" onclick="toggleHelpful(this, 5)">
                            👍 Hữu ích (5)
                        </button>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<%---- ================= JAVASCRIPT XỬ LÝ TƯƠNG TÁC ================= ----%>
<script>
    // Kiểm tra xem đã chọn số sao chưa trước khi gửi
    function validateReviewForm() {
        const ratingChecked = document.querySelector('input[name="rating"]:checked');
        const comment = document.getElementById('reviewCommentInput').value.trim();

        if (!ratingChecked) {
            alert('Vui lòng chọn số sao đánh giá (từ 1 đến 5 sao) trước khi gửi!');
            return false;
        }

        if (comment.length < 5) {
            alert('Vui lòng nhập nội dung đánh giá tối thiểu 5 ký tự!');
            return false;
        }

        return true;
    }

    // Nút bấm "Hữu ích"
    function toggleHelpful(button, defaultCount) {
        if (button.classList.contains('active')) {
            button.classList.remove('active');
            button.innerHTML = '👍 Hữu ích (' + defaultCount + ')';
        } else {
            button.classList.add('active');
            button.innerHTML = '👍 Hữu ích (' + (defaultCount + 1) + ')';
        }
    }
</script>
