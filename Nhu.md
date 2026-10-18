# Như — Module lịch sử vé, hủy vé, vé điện tử, điểm thưởng

Người phụ trách: **Như** (Backend, Nhóm 3 — Cinetik).

Phạm vi: lịch sử đặt vé, hủy booking, vé điện tử (QR), điểm thưởng, admin quản lý trạng thái đơn vé.

`persistence.xml`, `pom.xml`, `web.xml` và code của Phương, Ân, Hải, Vinh **không bị sửa**.

---

## 1. Như đã làm

### URL

| Việc | URL |
|---|---|
| Lịch sử vé của khách đang đăng nhập | `GET /order/history` và `GET /booking/history` |
| Vé điện tử + QR | `GET /order/ticket-detail?id={orderCode}` hoặc `?bookingId={orderCode}` |
| Khách hủy vé | `POST /order/cancel` (field `orderCode`) |
| Admin danh sách / lọc | `GET /admin/bookings?keyword=&status=` |
| Admin hủy vé | `POST /admin/bookings/cancel` (`orderCode`, `cancelReason`) |
| Admin xác nhận đã thanh toán | `POST /admin/bookings/status` (`orderCode`, `status=PAID`) |

Chưa đăng nhập: chuyển về trang đăng nhập, `errorMessage` = cần đăng nhập. Admin chỉ cho `role` là `ADMIN` hoặc `STAFF`.

### Trang JSP đã gắn dữ liệu

- `src/main/webapp/WEB-INF/views/order/history.jsp`
- `src/main/webapp/WEB-INF/views/order/ticket-detail.jsp`
- `src/main/webapp/WEB-INF/views/order/admin/bookings.jsp`

Layout và class CSS của Phương giữ nguyên. Servlet set `ordersBound=true` thì JSP dùng dữ liệu thật (kể cả danh sách rỗng). Mở file JSP không qua servlet thì vẫn còn dữ liệu mẫu để Phương soi giao diện.

Biến request chính:

- Lịch sử / admin: `orderList` (`List<OrderView>`), `ordersBound`, `orderCount`, `loyaltyPoints`
- Vé điện tử: `order` (`OrderView`), `ticketBound`, `loyaltyPoints`
- Admin: `totalOrders`, `paidOrders`, `cancelledOrders`, `totalRevenue` (chuỗi dạng `0đ`)
- Thông báo: `errorMessage`, `successMessage` (request hoặc session)

`OrderView` dùng các field JSP đang có: `orderCode`, `status` (`PENDING` / `PAID` / `CANCELLED`), `createdDate`, `posterURL`, `movieTitle`, `movieFormat`, `ageRating`, `startTime`, `showDate`, `cinemaName`, `cinemaAddress`, `roomName`, `roomNumber`, `seats`, `seatPills`, `combos`, `totalPrice`, `customerName`, `phone`, `qrImageUrl`, `pointsEarned`, `cancellable`.

### Bảng PostgreSQL mới

EclipseLink tự tạo khi app kết nối (`create-or-extend-tables`):

- `bookings` — đơn vé, mã `CNK-năm-00001`, snapshot tên phim / rạp / phòng / ghế
- `tickets` — từng ghế; hủy vé thì `status = CANCELLED` để nhả ghế
- `loyalty_accounts` — điểm thưởng theo `user_id` (String)

Entity: `com.cinema.entity.Booking`, `Ticket`, `LoyaltyAccount`.  
Đăng ký trong `src/main/resources/META-INF/orm.xml`, **không** thêm `<class>` vào `persistence.xml`.

### Quy tắc nghiệp vụ

- Đơn `PAID`: 1 điểm cho mỗi 10.000đ (làm tròn xuống).
- Dùng điểm lúc thanh toán: 1 điểm trừ 10.000đ. Hải set `pointsToUse` trên `PlaceBookingRequest` rồi gọi `placeBooking` (trừ điểm và lưu đơn trong cùng một giao dịch). Không gọi `usePoints` riêng rồi `placeBooking` trong cùng một lượt, kẻo trừ điểm hai lần.
- Hủy đơn thì hoàn điểm đã dùng. Nếu đơn đã thanh toán thì trừ lại điểm đã cộng. Điểm không xuống dưới 0. Đơn `PENDING` có dùng điểm vẫn được hoàn điểm khi hủy.
- Khách hủy được `PENDING` hoặc `PAID` **trước giờ chiếu**. Sau giờ chiếu thì không hủy.
- Admin hủy được cả sau giờ chiếu, và bấm xác nhận để đưa `PENDING` → `PAID` (lúc đó mới cộng điểm). `CONFIRMED` được hiểu là `PAID`.
- QR là ảnh từ mã đơn: `https://api.qrserver.com/v1/create-qr-code/?size=190x190&color=110e0c&data={orderCode}`.
- Email vé / email hủy: `TicketMail` gọi `com.cinema.util.MailUtil.send(to, subject, body)` nếu class đó có sẵn. Chưa có thì bỏ qua, đơn vẫn lưu. Như không tạo `MailUtil`.

---

## 2. Lưu ý cho từng người

### Hải — đặt ghế / thanh toán

`BookingFlowServlet` chưa ghi đơn xuống database. Lịch sử sẽ trống cho đến khi thanh toán xong gọi:

```java
OrderService orderService = new OrderService();
PlaceBookingRequest req = new PlaceBookingRequest();
req.setUserId(...);          // String, lấy từ sessionScope.loginUser
req.setCustomerName(...);
req.setPhone(...);
req.setEmail(...);
req.setShowtimeId(...);      // showtimeID đang có trong DB
req.setSelectedSeats(...);   // "E5,E6" hoặc seatID, cách nhau bởi dấu phẩy
req.setCombosRaw(...);       // chuỗi hiển thị, hoặc JSON [{name, qty}]
req.setTicketTotal(...);
req.setComboTotal(...);
req.setFinalTotal(...);
req.setMovieFormat(...);     // ví dụ "2D Phụ Đề"; bỏ trống thì mặc định "2D Phụ Đề"
req.setPaid(true);           // false = PENDING, admin xác nhận sau
req.setPointsToUse(0);       // số điểm khách muốn trừ; 1 điểm = 10.000đ
OrderActionResult result = orderService.placeBooking(req);
```

API điểm, gọi trực tiếp khi cần cộng/trừ ngoài lúc tạo đơn:

```java
LoyaltyService loyalty = new LoyaltyService();
int balance = loyalty.getPoints(userId);
balance = loyalty.addPoints(userId, 10);
balance = loyalty.usePoints(userId, 5); // thiếu điểm thì ném IllegalStateException
```

- Lúc checkout chỉ dùng `setPointsToUse`. Đừng gọi thêm `usePoints` cho cùng đơn đó.
- Không tạo entity `Booking` / `Ticket` khác. Dùng class sẵn trong `com.cinema.entity`.
- Không sửa `OrderServlet`, `BookingDAO`, `LoyaltyService`, ba trang `WEB-INF/views/order/**`.
- Ghế đã nằm trong đơn chưa hủy thì `placeBooking` trả lỗi, không đặt trùng.

### Ân — đăng nhập / user / mail

Trang của Như đọc `sessionScope.loginUser`. Cần có các getter (tên nào cũng được, thử lần lượt):

- Mã thành viên: `getUserId()`, `getUserID()` hoặc `getId()` — **kiểu String**
- `getFullName()`, `getEmail()`, `getPhone()`, `getRole()` (`ADMIN` / `STAFF` / `CUSTOMER`)
- Điểm trên session: `getLoyaltyPoints()` và `setLoyaltyPoints(Integer)` hoặc `setLoyaltyPoints(int)`

Nếu chưa có `setLoyaltyPoints`, điểm vẫn lưu ở `loyalty_accounts`. Header chỉ hiện đúng điểm khi session được cập nhật.

Chưa có entity `User` trong repo. Như **chưa** sửa file user của Ân và **chưa** ghi điểm vào bảng `users`. Khi Ân thêm `User.loyaltyPoints`, báo Như để nối một lần, tránh hai sổ điểm.

Mail xác nhận vé: Như gọi nếu có class `com.cinema.util.MailUtil` với đúng một trong các hàm static:

```java
public static void send(String to, String subject, String body)
```

`sendMail` hoặc `sendEmail` cùng 3 tham số String cũng được. Chưa merge class này thì app không lỗi, chỉ không gửi mail. Như không tạo file trong `com.cinema.util`.

### Phương — giao diện

Không đổi tên biến EL ở ba trang order (`orderList`, `order`, `ordersBound`, `ticketBound`, `loyaltyPoints`, …). Đổi tên là lịch sử / vé / admin mất dữ liệu.

CSS trong `assets/css/**`, `header.jsp`, `footer.jsp` Như không sửa. Nút Hủy vé và dòng điểm dùng class sẵn (`btn-view-ticket`, `order-combos-text`, `order-empty-state`).

### Vinh — phim / suất chiếu

Như chỉ **đọc** `Movie` và `Showtime` (id kiểu String). Vé lưu thêm bản snapshot (tên phim, poster, rạp, phòng, giờ chiếu) để vé cũ vẫn mở được nếu suất chiếu đổi tên.

`Showtime` hiện không có field `price`. Giá trên vé là số tiền lúc đặt (`totalPrice`), không lấy từ suất chiếu.

### Cả nhóm — tránh conflict

Không sửa, không đổi tên, không tạo class trùng:

- `src/main/java/com/cinema/controller/booking/OrderServlet.java`
- `src/main/java/com/cinema/controller/booking/AdminBookingServlet.java`
- `src/main/java/com/cinema/entity/Booking.java`
- `src/main/java/com/cinema/entity/Ticket.java`
- `src/main/java/com/cinema/entity/LoyaltyAccount.java`
- `src/main/java/com/cinema/dao/order/**`
- `src/main/java/com/cinema/service/order/**` (`OrderService`, `LoyaltyService`, `TicketMail`)
- `src/main/java/com/cinema/order/**`
- `src/main/java/com/cinema/dto/OrderView.java`, `PlaceBookingRequest.java`, `OrderActionResult.java`, `BookingStats.java`
- `src/main/webapp/WEB-INF/views/order/**`
- `src/main/resources/META-INF/orm.xml`

`orm.xml` chỉ liệt kê 3 entity của Như. Nếu người khác cần file này, **thêm dòng `<entity>`**, không xóa 3 dòng sẵn có. Không cần sửa `persistence.xml` cho phần của Như.

Khóa chính toàn hệ thống là `String`. Thông báo lỗi / thành công dùng `errorMessage` và `successMessage`. Danh sách rỗng là `new ArrayList<>()`, không trả `null`.
