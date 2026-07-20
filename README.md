# Movie Ticket Booking System

Ứng dụng desktop Java Swing quản lý phim, lịch chiếu, chọn ghế, đặt vé, lịch sử booking và báo cáo doanh thu. Dự án sử dụng OOP, Collections, xử lý file TXT UTF-8, event-driven programming và kiểm tra dữ liệu; không sử dụng database hoặc thư viện ngoài.

## Yêu cầu

- JDK 17 trở lên.
- Khuyến nghị JDK 21.
- Chạy lệnh tại thư mục gốc của dự án.

## Chạy trên Windows

Mở thư mục dự án trong VS Code hoặc Command Prompt, sau đó chạy:

```bat
.\run.bat
```

Script tự xóa build cũ, biên dịch toàn bộ source và chạy `movieticketbooking.Main`, kể cả khi đường dẫn dự án có khoảng trắng hoặc ký tự tiếng Việt.

## Chạy trên Linux hoặc macOS

```bash
chmod +x run.sh
./run.sh
```

## Chạy kiểm thử

### Windows

```bat
.\test.bat
```

### Linux hoặc macOS

```bash
chmod +x test.sh
./test.sh
```

Bộ test không cần JUnit. Khi kiểm thử, `data/screenings.txt` và `data/bookings.txt` được sao lưu rồi khôi phục trong `finally`, nên dữ liệu demo ban đầu không bị mất.

## Cấu trúc chính

- `src/movieticketbooking/Main.java`: entry point.
- `model/`: `Movie`, `Screening`, `Seat`, `Booking`.
- `service/`: xử lý nghiệp vụ phim, lịch chiếu, booking và báo cáo.
- `ui/`: các màn hình Swing và navigation bằng `CardLayout`.
- `util/`: đọc/ghi file, sinh ID và validation dùng chung.
- `exception/`: exception kiểm tra dữ liệu.
- `data/*.txt`: dữ liệu cục bộ UTF-8 của ứng dụng.
- `tests/`: regression test nghiệp vụ và GUI smoke test.

## Chức năng chính

### Movie Management

- Thêm, xem, cập nhật, xóa và tìm kiếm phim.
- Kiểm tra trường bắt buộc, Movie ID trùng và thời lượng hợp lệ.
- Không cho xóa phim đang được sử dụng bởi lịch chiếu.

### Screening Management

- Thêm, xem, cập nhật, xóa và tìm kiếm lịch chiếu.
- Kiểm tra phim tồn tại, ngày giờ, giá vé và xung đột phòng chiếu.
- Không cho sửa hoặc xóa suất chiếu đã xuất hiện trong booking history (kể cả booking đã hủy),
  vì thông tin vé lịch sử phải giữ nguyên phim, ngày giờ, phòng và giá đã đặt.

### Seat Booking

- Sơ đồ 15 ghế: `A1-A5`, `B1-B5`, `C1-C5`.
- Màu xanh: Available; màu vàng: Selected; màu đỏ: Booked.
- Cho phép chọn hoặc bỏ chọn nhiều ghế và tự động tính tổng tiền.
- Không chấp nhận ghế ngoài sơ đồ, ghế bị lặp hoặc ghế đã bán.
- Tạo Booking ID và lưu ngay vào `data/bookings.txt`.

### Booking History

- Tìm theo Booking ID, Customer Name hoặc Phone Number.
- Xem chi tiết, cập nhật thông tin khách hàng và hủy booking.
- Booking bị hủy vẫn được giữ trong lịch sử, ghế được giải phóng và phần chỉnh sửa chuyển sang chỉ đọc.
- Bảng và khung chi tiết tự co theo cửa sổ thường; nội dung dài tự xuống dòng và không tạo thanh cuộn ngang ngoài ý muốn.

### Dashboard và Revenue Reports

Dashboard hiển thị số phim, số suất chiếu, booking đã xác nhận, vé đã bán và Gross Revenue từ dữ liệu thật.

Quy tắc doanh thu:

- Chỉ booking có trạng thái `CONFIRMED` được tính.
- Booking `CANCELLED` không được tính.
- Doanh thu dùng đúng `totalPrice` đã lưu tại thời điểm đặt vé.
- Báo cáo có thể lọc theo phim và ngày chiếu.

## Dữ liệu demo

- `SCR001`: booking `BKG001`, ghế `A1, A2`, trạng thái `CONFIRMED`.
- `SCR002`: booking `BKG002`, ghế `B3`, trạng thái `CONFIRMED`.
- `SCR003`: booking `BKG003`, ghế `C4, C5`, trạng thái `CANCELLED`; hai ghế này có thể đặt lại.

## Nội dung kiểm thử

`BookingSystemRegressionTest` kiểm tra các trường hợp quan trọng như:

- File booking bị thiếu được tạo lại.
- Ghế ngoài `A1-C5`, ghế trùng và ghế đã bán bị từ chối.
- Đặt nhiều ghế và tính tổng tiền chính xác.
- Hủy booking giải phóng ghế.
- Không xóa suất chiếu có booking history.
- Dữ liệu vẫn còn sau khi khởi tạo lại service.
- Dòng booking sai được bỏ qua an toàn.
- Revenue chỉ tính booking đã xác nhận.

`GuiSmokeTest` kiểm tra `MainFrame`, bố cục màn hình đặt ghế và trạng thái Booking History: booking đã hủy phải chỉ đọc, các nút hành động bị vô hiệu hóa, không xuất hiện ký tự biểu tượng lỗi font và màn hình không tràn ngang ở kích thước cửa sổ thường `1280 × 760`.