# Movie Ticket Booking System

Ứng dụng desktop Java Swing dùng OOP, Collections, xử lý file UTF-8, sự kiện và kiểm tra dữ liệu.

## Chạy trên Windows

Mở thư mục dự án trong VS Code hoặc Command Prompt, sau đó chạy:

```bat
.\run.bat
```

Script tự biên dịch toàn bộ source và chạy `movieticketbooking.Main`, kể cả khi đường dẫn dự án có khoảng trắng hoặc ký tự tiếng Việt.

## Cấu trúc chính

- `movieticketbooking.Main`: entry point.
- `MainFrame`: cửa sổ chính và navigation.
- `MoviePanel`: giao diện quản lý phim.
- `ScreeningPanel`: giao diện quản lý lịch chiếu.
- `RevenueReportPanel`: báo cáo doanh thu (Phase 6), chỉ đọc dữ liệu.
- `data/*.txt`: dữ liệu cục bộ của ứng dụng.

## Dashboard và Báo cáo doanh thu (Phase 6)

Dashboard hiển thị 6 số liệu thực tế lấy trực tiếp từ dữ liệu hiện có: tổng số phim,
tổng số suất chiếu, số suất chiếu sắp diễn ra (thời điểm bắt đầu sau thời điểm hiện tại -
suất chiếu hôm nay chưa bắt đầu vẫn được tính là "sắp diễn ra"), số đơn đặt vé đã xác nhận,
số vé đã bán, và **Gross Revenue** (tổng doanh thu). Không có số liệu tăng trưởng, phần trăm,
hay biểu đồ giả lập nào được hiển thị.

Vào mục **Revenue Reports** trên thanh điều hướng bên trái để xem báo cáo chi tiết theo
từng suất chiếu, có thể lọc theo phim và theo khoảng ngày chiếu ("Screening date").

Quy tắc doanh thu:

- Chỉ những đơn đặt vé có trạng thái `CONFIRMED` mới được tính vào Gross Revenue.
- Đơn có trạng thái `CANCELLED` không được tính.
- Trạng thái không xác định (khác `CONFIRMED`/`CANCELLED`) sẽ bị loại khỏi doanh thu và có
  cảnh báo rõ ràng, không bao giờ âm thầm tính là doanh thu.
- Doanh thu dùng đúng số tiền đã lưu trong từng đơn đặt vé tại thời điểm đặt, không tính lại
  theo giá vé hiện tại.
- Lọc theo ngày dựa trên **ngày chiếu (screening date)** vì Booking hiện chưa có mốc thời
  gian đặt vé đảm bảo.

Lưu ý quan trọng: hệ thống **chưa có** chức năng đặt vé, chọn ghế, hay quy trình thanh toán.
`Booking.java` và `data/bookings.txt` đã tồn tại để phục vụ báo cáo, nhưng việc tạo/sửa/hủy
đơn đặt vé sẽ được triển khai ở các phase sau. Báo cáo hoạt động đúng với file rỗng và sẽ tự
động hiển thị dữ liệu thật khi các phase sau tạo ra các đơn đặt vé.

Định dạng lưu trữ TXT (`data/movies.txt`, `data/screenings.txt`, `data/bookings.txt`) không
thay đổi ở Phase 6 - `ReportService` chỉ đọc dữ liệu, không bao giờ ghi hay sắp xếp lại các
file này.
