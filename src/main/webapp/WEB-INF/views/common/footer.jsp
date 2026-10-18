<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%---- FOOTER ----%>
<footer>
    <div class="footer-brand">Cinetik</div>
    <div>Hệ thống đặt vé xem phim trực tuyến &copy; 2026. Nhóm 3.</div>
</footer>

<c:if test="${not empty extraJs}">
    <script src="${pageContext.request.contextPath}/assets/js/${extraJs}"></script>
</c:if>

</body>
</html>
