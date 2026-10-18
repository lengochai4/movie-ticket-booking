# VIEW CONTRACT - QUY ƯỚC DỮ LIỆU JSP (NHÓM 3)
*Người phụ trách: Phương (`@phuong`)*
*Tài liệu thống nhất các `request.setAttribute`, `sessionScope` và tham số truyền sang từng trang JSP.*

---

## 1. Dùng chung (Common Views - Thư mục: `WEB-INF/views/common/`)

### `header.jsp` & `footer.jsp`
* **Session Scope:**
  * `sessionScope.loginUser`: Đối tượng User đang đăng nhập (`fullName`, `role`,...). Nếu null hiển thị nút Đăng Nhập.
* **Request Scope:**
  * `pageTitle` *(String)*: Tiêu đề trang hiển thị trên thẻ `<title>`.
  * `extraCss` *(String)*: Tên file CSS bổ sung của từng module (vd: `catalog.css`, `booking.css`,...).
  * `extraJs` *(String)*: Tên file JS bổ sung nếu có.

---

## 2. Module Phim & Rạp (Catalog - Thư mục: `WEB-INF/views/catalog/`)

### `movies.jsp` (Trang chủ & Danh sách phim)
* **URL:** `/` hoặc `/movies`
* **Controller:** `com.cinema.controller.catalog.MovieListServlet`
* **Request Attributes:**
  * `movieList` *(List<Movie>)*: Danh sách các phim đang chiếu / tìm kiếm.
  * `keyword` *(String, optional)*: Từ khóa tìm kiếm nếu có.

### `movie-detail.jsp` (Chi tiết phim & Suất chiếu)
* **URL:** `/movie-detail?id={movieID}`
* **Controller:** `com.cinema.controller.catalog.MovieDetailServlet`
* **Request Attributes:**
  * `movie` *(Movie)*: Đối tượng phim chứa đầy đủ thông tin (title, director, duration, description, posterURL, showtimes,...).
  * `showtimes` *(List<Showtime>)*: Danh sách suất chiếu theo phim (nằm trong `movie.getShowtimes()`).
* **Sub-views nhúng kèm:**
  * Nhúng `review-section.jsp` của Ân (`/WEB-INF/views/review/review-section.jsp`).

### `cinemas.jsp` (Danh sách cụm rạp)
* **URL:** `/cinemas`
* **Controller:** `com.cinema.controller.catalog.CinemaListServlet`
* **Request Attributes:**
  * `cinemas` *(List<Cinema>)*: Danh sách tất cả các rạp chiếu phim trong hệ thống.

---

## 3. Module Admin Rạp - Phòng - Ghế (Thư mục: `WEB-INF/views/catalog/admin/`)

### `cinemas.jsp` (Quản lý Rạp)
* **URL:** `/admin/cinemas`
* **Request Attributes:**
  * `cinemaList` *(List<Cinema>)*: Danh sách rạp.
  * `cinema` *(Cinema, optional)*: Dữ liệu rạp đang được chọn để chỉnh sửa (khi modal/form edit mở).

### `rooms.jsp` (Quản lý Phòng chiếu)
* **URL:** `/admin/rooms`
* **Request Attributes:**
  * `roomList` *(List<Room>)*: Danh sách phòng chiếu.
  * `cinemaList` *(List<Cinema>)*: Danh sách rạp để chọn dropdown khi tạo phòng.

### `seats.jsp` (Quản lý Ghế)
* **URL:** `/admin/seats`
* **Request Attributes:**
  * `room` *(Room)*: Phòng chiếu đang xem sơ đồ ghế.
  * `seatList` *(List<Seat>)*: Danh sách ghế trong phòng.
  * `roomList` *(List<Room>)*: Danh sách phòng để lọc chọn.

---

## 4. Module Đặt vé & Ghế (Booking - Của Hải)
* **URL:** `/booking/seats?showtimeId={id}`
* **Contract:** Hải nhận `showtimeId` từ link ở `movie-detail.jsp`, hiển thị sơ đồ ghế theo `Room` và `Seat`.

---

## 5. Module Đánh giá (Review - Của Ân)
* **File:** `/WEB-INF/views/review/review-section.jsp`
* **Contract:** Phương gọi `jsp:include`. Ân tự lấy `movie.movieID` từ request cha để hiển thị và gửi form đánh giá.
