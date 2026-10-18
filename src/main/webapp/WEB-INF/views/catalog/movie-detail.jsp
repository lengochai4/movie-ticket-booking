<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:set var="pageTitle" value="${movie.title} - Cinetik" scope="request"/>
<c:set var="extraCss" value="catalog.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main style="max-width: 1200px; margin: 40px auto; padding: 0 24px;">
    <div style="display: flex; flex-wrap: wrap; gap: 40px; margin-bottom: 48px;">
        <%---- Cột trái: Poster phim ----%>
        <aside style="flex: 1; min-width: 280px; max-width: 380px;">
            <img src="${not empty movie.posterURL ? movie.posterURL : 'https://via.placeholder.com/400x600?text=No+Poster'}"
                 alt="Poster phim ${movie.title}"
                 style="width: 100%; border-radius: 8px; border: 1px solid #2a2220; box-shadow: 0 8px 24px rgba(0,0,0,0.7);">
        </aside>

        <%---- Cột phải: Thông tin chi tiết ----%>
        <section style="flex: 2; min-width: 320px;">
            <h1 style="font-family: 'Playfair Display', serif; color: #fcca46; font-size: 36px; margin-bottom: 16px;">
                ${movie.title}
            </h1>
            <div style="color: #d9c9a8; font-size: 15px; margin-bottom: 24px; line-height: 1.8;">
                <p><strong>Đạo diễn:</strong> ${movie.director}</p>
                <p><strong>Thời lượng:</strong> ${movie.duration} phút</p>
                <c:if test="${not empty movie.genre}">
                    <p><strong>Thể loại:</strong> ${movie.genre}</p>
                </c:if>
                <c:if test="${not empty movie.releaseDate}">
                    <p><strong>Ngày khởi chiếu:</strong> ${movie.releaseDate}</p>
                </c:if>
            </div>

            <div style="color: #f6ead2; font-size: 15px; line-height: 1.8; margin-bottom: 32px; background: #1a1614; padding: 20px; border-radius: 6px; border-left: 4px solid #c4161f;">
                <h3 style="color: #fcca46; margin-bottom: 8px; font-size: 18px;">Nội dung phim</h3>
                <p>${movie.description}</p>
            </div>
        </section>
    </div>

    <%---- Lịch chiếu & Đặt vé ----%>
    <section style="margin-bottom: 48px;">
        <h2 style="color: #fcca46; border-bottom: 2px solid #c4161f; padding-bottom: 8px; margin-bottom: 24px;">
            Lịch Chiếu & Đặt Vé
        </h2>

        <c:choose>
            <c:when test="${empty movie.showtimes}">
                <div class="empty-state">
                    Hiện tại chưa có suất chiếu nào cho bộ phim này. Vui lòng quay lại sau!
                </div>
            </c:when>
            <c:otherwise>
                <div style="overflow-x: auto;">
                    <table style="width: 100%; border-collapse: collapse; background: #1a1614; border-radius: 8px; overflow: hidden;">
                        <thead>
                            <tr style="background: #780000; color: #fcca46; text-align: left;">
                                <th style="padding: 14px 18px;">Rạp Chiếu</th>
                                <th style="padding: 14px 18px;">Phòng</th>
                                <th style="padding: 14px 18px;">Thời Gian Bắt Đầu</th>
                                <th style="padding: 14px 18px; text-align: center;">Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="showtime" items="${movie.showtimes}">
                                <tr style="border-bottom: 1px solid #2a2220;">
                                    <td style="padding: 14px 18px; font-weight: bold; color: #f6ead2;">
                                        ${showtime.room.cinema.name}
                                    </td>
                                    <td style="padding: 14px 18px; color: #d9c9a8;">
                                        ${showtime.room.name}
                                    </td>
                                    <td style="padding: 14px 18px;">
                                        <div style="font-weight: 700; color: #fcca46; font-size: 15px;">🕒 ${showtime.formattedStartTime}</div>
                                        <div style="font-size: 12px; color: #94a3b8;">Ngày ${showtime.formattedDate}</div>
                                    </td>
                                    <td style="padding: 14px 18px; text-align: center;">
                                        <%---- Link sang module đặt vé của Hải ----%>
                                        <a href="${pageContext.request.contextPath}/booking/seats?showtimeId=${showtime.showtimeID}"
                                           class="btn-ticket" style="padding: 6px 16px; font-size: 13px;">
                                            Đặt Vé
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <%---- Khu vực Đánh giá phim ----%>
    <section id="review-container" style="margin-top: 48px;">
        <c:catch var="reviewErr">
            <jsp:include page="/WEB-INF/views/review/review-section.jsp"/>
        </c:catch>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
