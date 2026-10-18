<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Tự động nạp dữ liệu đánh giá nếu được include trực tiếp từ movie detail --%>
<c:if test="${empty reviews and not empty param.movieId}">
    <%
        String mid = request.getParameter("movieId");
        if (mid != null && !mid.trim().isEmpty()) {
            com.cinema.service.review.ReviewService rs = new com.cinema.service.review.ReviewService();
            request.setAttribute("reviews", rs.getReviewDetailsByMovie(mid));
            request.setAttribute("averageRating", rs.getAverageRating(mid));
            request.setAttribute("reviewCount", rs.getReviewCount(mid));
            request.setAttribute("currentMovieId", mid);
        }
    %>
</c:if>
<c:if test="${empty reviews and not empty movie}">
    <%
        Object mObj = request.getAttribute("movie");
        if (mObj instanceof com.cinema.model.entity.Movie) {
            String mid = ((com.cinema.model.entity.Movie) mObj).getMovieID();
            com.cinema.service.review.ReviewService rs = new com.cinema.service.review.ReviewService();
            request.setAttribute("reviews", rs.getReviewDetailsByMovie(mid));
            request.setAttribute("averageRating", rs.getAverageRating(mid));
            request.setAttribute("reviewCount", rs.getReviewCount(mid));
            request.setAttribute("currentMovieId", mid);
        }
    %>
</c:if>

<c:set var="targetMovieId" value="${not empty currentMovieId ? currentMovieId : (not empty movie ? movie.movieID : param.movieId)}" />

<!-- Review Section Wrapper -->
<section class="review-section" id="movieReviewSection">
    <!-- Section Header -->
    <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.5rem;">
        <div>
            <h2 style="font-size: 1.5rem; font-weight: 700; margin: 0 0 0.25rem 0; color: #fff;">
                ⭐ Đánh giá từ khán giả
            </h2>
            <p style="margin: 0; font-size: 0.9rem; color: #94a3b8;">
                Nhận xét và trải nghiệm xem phim chân thực từ cộng đồng Cinema N3
            </p>
        </div>
    </div>

    <!-- Rating Summary Box -->
    <div class="review-summary-card">
        <div class="review-score-box">
            <div class="review-big-number">
                <fmt:formatNumber value="${not empty averageRating ? averageRating : 0}" maxFractionDigits="1" minFractionDigits="1" />
            </div>
            <div>
                <div class="review-stars-outer">
                    <c:forEach var="i" begin="1" end="5">
                        <c:choose>
                            <c:when test="${(averageRating >= i) or (averageRating >= (i - 0.2))}">★</c:when>
                            <c:otherwise>☆</c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
                <div style="font-size: 0.85rem; color: #94a3b8; margin-top: 0.25rem;">
                    Dựa trên <strong><c:out value="${not empty reviewCount ? reviewCount : 0}" /></strong> lượt đánh giá
                </div>
            </div>
        </div>

        <c:if test="${empty sessionScope.loginUser}">
            <div>
                <a href="${pageContext.request.contextPath}/auth/login?redirect=${pageContext.request.requestURI}?${pageContext.request.queryString}" 
                   class="auth-btn auth-btn-primary auth-btn-sm">
                    ✍️ Đăng nhập để đánh giá
                </a>
            </div>
        </c:if>
    </div>

    <!-- Add Review Form (Cho người dùng đã đăng nhập) -->
    <c:choose>
        <c:when test="${not empty sessionScope.loginUser}">
            <div class="review-form-card">
                <h3 style="margin-top: 0; font-size: 1.1rem; font-weight: 600; color: #f1f5f9; margin-bottom: 1rem;">
                    Viết cảm nhận của bạn
                </h3>
                <form action="${pageContext.request.contextPath}/reviews" method="POST" id="submitReviewForm">
                    <input type="hidden" name="movieId" value="<c:out value='${targetMovieId}' />">

                    <div class="review-rating-select">
                        <span style="font-size: 0.9rem; font-weight: 600; color: #cbd5e1;">Đánh giá của bạn:</span>
                        <div class="review-rating-stars">
                            <input type="radio" id="star5" name="rating" value="5" checked />
                            <label for="star5" title="5 sao - Tuyệt đỉnh">★</label>
                            <input type="radio" id="star4" name="rating" value="4" />
                            <label for="star4" title="4 sao - Rất hay">★</label>
                            <input type="radio" id="star3" name="rating" value="3" />
                            <label for="star3" title="3 sao - Bình thường">★</label>
                            <input type="radio" id="star2" name="rating" value="2" />
                            <label for="star2" title="2 sao - Tạm được">★</label>
                            <input type="radio" id="star1" name="rating" value="1" />
                            <label for="star1" title="1 sao - Kém">★</label>
                        </div>
                    </div>

                    <textarea name="comment" class="review-textarea" 
                              placeholder="Chia sẻ suy nghĩ của bạn về nội dung, diễn xuất, kỹ xảo bộ phim..." required></textarea>

                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 0.8rem; color: #94a3b8;">
                            Đăng dưới tên: <strong><c:out value="${sessionScope.loginUser.fullName}" /></strong>
                        </span>
                        <button type="submit" class="auth-btn auth-btn-primary auth-btn-sm" id="btnSubmitReview">
                            Gửi đánh giá
                        </button>
                    </div>
                </form>
            </div>
        </c:when>
    </c:choose>

    <!-- Reviews List -->
    <div class="review-list" id="reviewContainer">
        <c:forEach var="item" items="${reviews}">
            <div class="review-item" id="review-<c:out value='${item.review.reviewID}' />">
                <div class="review-item-header">
                    <div class="review-user-info">
                        <div class="review-user-avatar">
                            <c:out value="${item.userName.substring(0, 1).toUpperCase()}" default="U" />
                        </div>
                        <div>
                            <div class="review-user-name"><c:out value="${item.userName}" /></div>
                            <div class="review-date">
                                <c:out value="${item.review.createdAt.toLocalDate()}" />
                            </div>
                        </div>
                    </div>

                    <div style="display: flex; align-items: center; gap: 1rem;">
                        <!-- Star rating display -->
                        <div style="color: #f5c518; font-size: 0.95rem; font-weight: 700;">
                            <c:forEach var="s" begin="1" end="5">
                                <c:choose>
                                    <c:when test="${item.review.rating >= s}">★</c:when>
                                    <c:otherwise>☆</c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <span style="margin-left: 0.25rem;"><fmt:formatNumber value="${item.review.rating}" maxFractionDigits="1" /></span>
                        </div>

                        <!-- Admin delete violation button -->
                        <c:if test="${sessionScope.loginUser.role == 1}">
                            <form action="${pageContext.request.contextPath}/reviews/delete" method="POST" style="margin: 0;">
                                <input type="hidden" name="id" value="${item.review.reviewID}">
                                <input type="hidden" name="movieId" value="${targetMovieId}">
                                <button type="submit" class="auth-btn auth-btn-danger auth-btn-sm" 
                                        style="padding: 0.2rem 0.5rem; font-size: 0.75rem;"
                                        onclick="return confirm('Bạn là Admin: Xoá đánh giá vi phạm này?');">
                                    🗑️ Xoá
                                </button>
                            </form>
                        </c:if>
                    </div>
                </div>

                <p class="review-item-comment">
                    <c:out value="${item.review.comment}" />
                </p>
            </div>
        </c:forEach>

        <c:if test="${empty reviews}">
            <div class="review-empty-state">
                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🍿</div>
                <div style="font-weight: 600; color: #e2e8f0;">Chưa có đánh giá nào</div>
                <div style="font-size: 0.85rem; margin-top: 0.25rem;">Hãy là người đầu tiên chia sẻ cảm nhận về bộ phim này!</div>
            </div>
        </c:if>
    </div>
</section>
