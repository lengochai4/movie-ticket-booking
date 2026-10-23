<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- ================= CSS KHU VỰC ĐÁNH GIÁ (SCOPED SEMANTIC HTML5) ================= --%>
<style>
.review-wrapper {
    background: #141211;
    border: 1px solid rgba(252, 202, 70, 0.2);
    border-radius: 12px;
    padding: 32px 28px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6);
    color: #f6ead2;
    margin-top: 36px;
}
.review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    padding-bottom: 20px;
    margin-bottom: 28px;
    flex-wrap: wrap;
    gap: 16px;
}
.review-title h2 {
    font-family: 'Playfair Display', serif;
    font-size: 26px;
    color: #fcca46;
    margin: 0 0 6px 0;
}
.review-title p {
    font-size: 14px;
    color: #b5a995;
    margin: 0;
}
.review-score-box {
    display: flex;
    align-items: center;
    gap: 14px;
    background: #1d1917;
    padding: 10px 18px;
    border-radius: 10px;
    border: 1px solid rgba(252, 202, 70, 0.25);
    margin: 0;
}
.score-number {
    font-size: 30px;
    font-weight: 800;
    color: #fcca46;
    line-height: 1;
}
.score-details {
    display: flex;
    flex-direction: column;
    gap: 2px;
    margin: 0;
}
.stars-gold {
    color: #fcca46;
    font-size: 15px;
    letter-spacing: 2px;
}
.score-count {
    font-size: 12px;
    color: #8c8275;
}
.review-form-card {
    background: #1b1716;
    border: 1px solid #3c3733;
    border-radius: 10px;
    padding: 20px 24px;
    margin-bottom: 32px;
}
.review-form-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
}
.reviewer-avatar {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    background: linear-gradient(135deg, #780000 0%, #a81c1c 100%);
    color: #fcca46;
    border: 1.5px solid #fcca46;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 15px;
    flex-shrink: 0;
    margin: 0;
}
.reviewer-name {
    font-size: 15px;
    font-weight: 600;
    color: #f6ead2;
}
.star-rating-picker {
    border: none;
    padding: 0;
    margin: 0 0 14px 0;
    display: flex;
    align-items: center;
    gap: 12px;
}
.star-rating-label {
    font-size: 14px;
    color: #b5a995;
    padding: 0;
    margin: 0;
}
.star-group {
    display: inline-flex;
    flex-direction: row-reverse;
    gap: 4px;
    margin: 0;
}
.star-group input[type="radio"] {
    display: none;
}
.star-group label {
    font-size: 22px;
    color: #4a423b;
    cursor: pointer;
    transition: color 0.15s ease, transform 0.15s ease;
}
.star-group label:hover,
.star-group label:hover ~ label,
.star-group input[type="radio"]:checked ~ label {
    color: #fcca46;
    text-shadow: 0 0 10px rgba(252, 202, 70, 0.5);
    transform: scale(1.1);
}
.review-textarea {
    width: 100%;
    min-height: 90px;
    padding: 12px 14px;
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
.review-form-actions {
    display: flex;
    justify-content: flex-end;
    margin: 12px 0 0 0;
}
.btn-submit-review {
    padding: 10px 22px;
    background: #780000;
    color: #fcca46;
    border: 1px solid #fcca46;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    transition: all 0.2s ease;
}
.btn-submit-review:hover {
    background: #920000;
    box-shadow: 0 0 12px rgba(252, 202, 70, 0.4);
    transform: translateY(-1px);
}
.review-login-prompt {
    background: #1b1716;
    border: 1px dashed rgba(252, 202, 70, 0.35);
    border-radius: 10px;
    padding: 22px;
    text-align: center;
    margin-bottom: 32px;
}
.review-login-prompt p {
    font-size: 14.5px;
    color: #b5a995;
    margin: 0 0 12px 0;
}
.btn-login-to-review {
    display: inline-block;
    padding: 9px 20px;
    background: #780000;
    color: #fcca46 !important;
    border: 1px solid #fcca46;
    border-radius: 6px;
    font-size: 13.5px;
    font-weight: 700;
    text-decoration: none;
    transition: all 0.2s;
}
.review-list-title {
    font-size: 16px;
    font-weight: 700;
    color: #fcca46;
    margin-bottom: 18px;
    display: flex;
    align-items: center;
    gap: 8px;
}
.reviews-container {
    display: flex;
    flex-direction: column;
    gap: 14px;
}
.review-item {
    background: #191615;
    border: 1px solid #2a2523;
    border-radius: 8px;
    padding: 18px 20px;
    transition: border-color 0.2s;
}
.review-item:hover {
    border-color: rgba(252, 202, 70, 0.3);
}
.review-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}
.reviewer-meta {
    display: flex;
    align-items: center;
    gap: 10px;
}
.reviewer-meta-text {
    display: flex;
    flex-direction: column;
}
.reviewer-meta-name {
    font-size: 14.5px;
    font-weight: 600;
    color: #f6ead2;
    display: block;
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
    line-height: 1.6;
    color: #d9c9a8;
    margin: 0 0 10px 0;
}
.review-footer {
    display: flex;
    justify-content: flex-end;
}
.btn-helpful {
    background: transparent;
    border: 1px solid #3c3733;
    color: #8c8275;
    border-radius: 14px;
    padding: 3px 10px;
    font-size: 12px;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 5px;
    transition: all 0.2s;
}
.btn-helpful:hover, .btn-helpful.active {
    border-color: #fcca46;
    color: #fcca46;
    background: rgba(252, 202, 70, 0.1);
}
.no-reviews-box {
    text-align: center;
    padding: 32px 16px;
    color: #8c8275;
    font-size: 14px;
    background: #171413;
    border-radius: 8px;
    margin: 0;
}
</style>

<%-- ================= GIAO DIỆN KHU VỰC ĐÁNH GIÁ (CHUẨN HTML5 SEMANTIC) ================= --%>
<section class="review-wrapper" aria-label="Đánh giá phim">

    <%-- 1. Tổng quan điểm số và số lượng đánh giá --%>
    <header class="review-header">
        <div class="review-title">
            <h2>Đánh Giá & Bình Luận</h2>
            <p>Chia sẻ cảm nhận của bạn về bộ phim này cùng cộng đồng khán giả Cinetik</p>
        </div>

        <aside class="review-score-box" aria-label="Điểm trung bình">
            <span class="score-number">${not empty avgRating ? avgRating : '5.0'}</span>
            <p class="score-details">
                <span class="stars-gold">★★★★★</span>
                <span class="score-count">${not empty totalReviews ? totalReviews : '0'} lượt đánh giá</span>
            </p>
        </aside>
    </header>

    <%-- 2. Form gửi đánh giá (chỉ hiển thị khi đã đăng nhập) --%>
    <c:choose>
        <c:when test="${not empty sessionScope.loginUser}">
            <section class="review-form-card" aria-label="Viết đánh giá">
                <header class="review-form-header">
                    <figure class="reviewer-avatar">
                        ${sessionScope.loginUser.fullName.substring(0,1).toUpperCase()}
                    </figure>
                    <span class="reviewer-name">${sessionScope.loginUser.fullName}</span>
                </header>

                <form action="${pageContext.request.contextPath}/review/add" method="POST" onsubmit="return validateReviewForm();">
                    <input type="hidden" name="movieID" value="${not empty movie.movieID ? movie.movieID : param.id}">

                    <fieldset class="star-rating-picker">
                        <legend class="star-rating-label">Bạn chấm phim này mấy sao?</legend>
                        <p class="star-group">
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
                        </p>
                    </fieldset>

                    <textarea name="comment"
                              id="reviewCommentInput"
                              class="review-textarea"
                              placeholder="Chia sẻ cảm nhận chi tiết về kịch bản, âm thanh, diễn xuất..."
                              minlength="5"
                              required></textarea>

                    <p class="review-form-actions">
                        <button type="submit" class="btn-submit-review">Gửi Đánh Giá</button>
                    </p>
                </form>
            </section>
        </c:when>
        <c:otherwise>
            <aside class="review-login-prompt">
                <p>🎬 Bạn muốn chấm điểm và viết cảm nhận cho bộ phim này?</p>
                <a href="${pageContext.request.contextPath}/auth/login" class="btn-login-to-review">
                    Đăng Nhập Để Đánh Giá
                </a>
            </aside>
        </c:otherwise>
    </c:choose>

    <%-- 3. Danh sách các bình luận từ CSDL --%>
    <header class="review-list-title">
        <span>💬 Bình luận từ khán giả (${not empty reviewList ? reviewList.size() : 0})</span>
    </header>

    <section class="reviews-container" aria-label="Danh sách bình luận">
        <c:choose>
            <c:when test="${not empty reviewList}">
                <c:forEach var="r" items="${reviewList}">
                    <article class="review-item">
                        <header class="review-item-header">
                            <span class="reviewer-meta">
                                <figure class="reviewer-avatar" style="width: 34px; height: 34px; font-size: 13px;">
                                    ${not empty r.userName ? r.userName.substring(0,1).toUpperCase() : 'U'}
                                </figure>
                                <span class="reviewer-meta-text">
                                    <strong class="reviewer-meta-name"><c:out value="${r.userName}"/></strong>
                                    <time class="review-time"><c:out value="${r.createdAt}"/></time>
                                </span>
                            </span>

                            <span class="review-item-stars" aria-label="${r.rating} trên 5 sao">
                                <c:forEach begin="1" end="${r.rating}">★</c:forEach><c:forEach begin="${r.rating + 1}" end="5"><span style="color: #4a423b;">★</span></c:forEach>
                            </span>
                        </header>

                        <p class="review-content"><c:out value="${r.comment}"/></p>

                        <footer class="review-footer">
                            <button type="button" class="btn-helpful" onclick="toggleHelpful(this, ${not empty r.helpfulCount ? r.helpfulCount : 0})">
                                👍 Hữu ích (${not empty r.helpfulCount ? r.helpfulCount : 0})
                            </button>
                        </footer>
                    </article>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <p class="no-reviews-box">
                    <span>Chưa có đánh giá nào cho phim này. Hãy là người đầu tiên chia sẻ cảm nhận của bạn!</span>
                </p>
            </c:otherwise>
        </c:choose>
    </section>

</section>

<%-- ================= JAVASCRIPT XỬ LÝ SỰ KIỆN ================= --%>
<script>
    function validateReviewForm() {
        const rating = document.querySelector('input[name="rating"]:checked');
        const commentInput = document.getElementById('reviewCommentInput');
        const comment = commentInput ? commentInput.value.trim() : '';

        if (!rating) {
            alert('Vui lòng chọn số sao đánh giá (từ 1 đến 5 sao) trước khi gửi!');
            return false;
        }
        if (comment.length < 5) {
            alert('Vui lòng nhập nội dung đánh giá tối thiểu 5 ký tự!');
            return false;
        }
        return true;
    }

    function toggleHelpful(button, defaultCount) {
        const isActive = button.classList.toggle('active');
        const count = isActive ? (defaultCount + 1) : defaultCount;
        button.innerHTML = '👍 Hữu ích (' + count + ')';
    }
</script>
