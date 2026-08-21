# Các tính năng trên thanh điều khiển nổi và cập nhật biểu tượng: CHI TIẾT NGHIỆP VỤ

## Biểu tượng ▶️: 
- Chạy (Play) nút này thường biến thành nút Tạm dừng (Pause) ⏸️. Ẩn tất cả các hành động khác chi để lại ↔️ (Biểu tượng Di chuyển bốn hướng).
- Bấm vào Tạm dừng (Pause) ⏸️ thì trở lại như cũ.
- Chạy (Play) thanh điều khiển sẽ khóa các vòng tròn hiển thị và làm mờ thành màu xám và gửi các lệnh giả lập chạm (`Dispatch Gesture`) xuống tọa độ `(X, Y)` hoặc giả lập kéo (`Swipe action`) xuống tọa độ `(x1,y1)` và kéo sang tọa độ `(x2,y2)` của ứng dụng bên dưới theo thứ tự. Khi chạy đến số nào thì số đó sẽ sáng lên và đổi màu với hành động kéo thì toàn bộ hành động sẽ sáng lên và đổi màu.


## Biểu tượng 👆: 
- Thêm điểm nhấn 🔵 - Add target point. Mỗi lần nhấn, một vòng tròn màu đỏ đánh số mới (như số 1, 2, 3, 4 và các số khác sẽ tạo theo thứ tự) sẽ xuất hiện ở giữa màn hình , phía trên các ứng dụng được hiển thị.
### Hình dáng & Thiết kế:
- Vòng tròn màu đỏ, Tâm - bên trong chính giữa là dấu +, và hiển thị số thứ tự tương ứng màu xanh lá cây
- Kích thước vòng tròn có thể thay đổi, màu sắc vòng tròn cũng có thể thay đổi, Tâm cũng có thể thay đổi, màu sắc số thứ tự cũng có thể thay đổi, màu sắc khi chạy cũng có thể thay đổi. Hãy thiết kế trước config tổng để sau có thể áp dụng cho toàn bộ.
### Cấu hình độc lập (Click-to-Configure):
- Khi người dùng nhấn (tap) vào từng item (1, 2, 3...), hệ thống sẽ hiển thị một cửa sổ Pop-up - Tiêu đề là số thứ tự item. Cài đặt riêng cho điểm đó bao gồm:
#### Thời gian chờ trước khi bấm - `delay` tap
- Là thời gian chờ trước khi bấm ứng dụng. Mặc định nhập là: 0, có thể chọn dropdown bên cạnh là: Mili giây (Millisecond), s: Giây (Second), m: Phút (Minute), h: Giờ (Hour)
#### Số lần bấm liên tục
- Bạn muốn bấm tại vị trí đó bao nhiêu lần. Mặc định cho phép nhập là 1
#### Hẹn giờ bấm
- Là thời gian hẹn giờ để bấm ứng dụng. Có thể chọn ngày tháng năm giờ phút giây. Thiết kế sao cho dễ chọn, có thể dùng dropdown cho từng thành phần (sử dụng dropdown thì cho phép cả nhập dữ liệu nhưng bắt chặt giá trị có thể sai. luôn bắt nhập tháng trước ngày sau) hoặc nhập vào ngày tháng năm và chọn giờ phút giây theo từng ô.

#### Ảnh so sánh - Mã hiệu tính năng: 9e68b3fc-e04e-4ecd-a969-c30029ccc972 - Sẽ được mô tả chi tiết sau
- Cho phép upload ảnh và hiển thị ảnh vừa upload.

## Biểu tượng 👋: 
- Thêm hành động vuốt 🔵➖🔵 - Add swipe action có số thứ tự là số tiếp theo (Ví dụ bấm 👆 3 lần thì khi 👋 số thứ tự phải là 4 sau khi bấm tiếp 👆 thì tạo ra điểm nhấn mới số thứ tự là 5 vì 2 nút này số thứ tự sẽ luôn liên tiếp nhau)
- xuất hiện ở giữa màn hình , phía trên các ứng dụng được hiển thị. Mặc định ban đầu có mũi tên từ trái sang phải và có 1 độ dài vừa phải
- Script sẽ giả lập thao tác vuốt từ điểm bắt đầu và điểm kết thúc.
- Điểm bắt đầu và điểm kết thúc có thể di chuyển tự do trong màn hình nhưng không được lồng vào nhau.
### Hình dáng & Thiết kế:
- Cấu trúc: Gồm hai đầu hình tròn nổi nối với nhau bằng một mũi tên màu đỏ.
- Điểm bắt đầu (Điểm gốc): Chấm tròn màu xanh lá cây đậm ở một đầu đại diện cho điểm chạm đầu tiên.
- Điểm kết thúc (Điểm đích): Chấm tròn lớn hơn có viền đậm và dán nhãn màu xanh lá cây, đại diện cho điểm kết thúc vuốt. Bên trong có Tâm - chính giữa là dấu +, và hiển thị số thứ tự tương ứng màu cam.
- Mũi tên có màu đỏ. là 1 thánh nối nhỏ và mảnh.
- Kích thước vòng tròn có thể thay đổi, màu sắc vòng tròn cũng có thể thay đổi, Tâm cũng có thể thay đổi, màu sắc số thứ tự cũng có thể thay đổi, màu sắc khi chạy cũng có thể thay đổi, màu và kiểu dáng thanh nối cũng có thể thay đổi. Hãy thiết kế trước config tổng để sau có thể áp dụng cho toàn bộ.
### Cấu hình độc lập (Click-to-Configure):
- Khi người dùng nhấn (tap) vào điểm kết thúc, hệ thống sẽ hiển thị một cửa sổ Pop-up - Tiêu đề là số thứ tự item. Cài đặt riêng cho tính năng vuốt bao gồm:
#### Thời gian chờ trước khi vuốt - `delay` swipe
- Là thời gian chờ trước khi vuốt. Mặc định nhập là: 0, có thể chọn dropdown bên cạnh là: Mili giây (Millisecond), s: Giây (Second), m: Phút (Minute), h: Giờ (Hour)
#### Số lần vuốt liên tục
- Bạn muốn vuốt lại bao nhiêu lần. Mặc định cho phép nhập là 1
#### Hẹn giờ vuốt
- Là thời gian hẹn giờ để vuốt. Có thể chọn ngày tháng năm giờ phút giây. Thiết kế sao cho dễ chọn, có thể dùng dropdown cho từng thành phần (sử dụng dropdown thì cho phép cả nhập dữ liệu nhưng bắt chặt giá trị có thể sai. luôn bắt nhập tháng trước ngày sau) hoặc nhập vào ngày tháng năm và chọn giờ phút giây theo từng ô.
#### Thời gian vuốt
- Thời gian vuốt tính bằng Mili giây (Millisecond). Mặc định cho phép nhập là 200 Mili giây (Millisecond).
- Ghi chú vào tính năng: Thời gian này càng lớn thì tốc độ vuốt càng chậm và từ từ.


## Biểu tượng ➖: 
- Xóa điểm (Remove target). Xóa điểm nhấn hoặc đường vuốt cuối cùng được thêm vào.
- Khi xóa thì số thứ tự cũng nhảy về số trước đó. Khi xóa hết thì trở về STT ban đầu


## Biểu tượng ⚙️: 
- Cài đặt chung (Settings) khi bấm vào hiển thị một cửa sổ Pop-up - Tiêu đề là `Cài đặt thông số cho kịch bản`. Cài đặt các thông số cho cả kịch bản bao gồm:
### Chạy lặp lại bao nhiêu lần (dạng radio)
- Nhập số lần chạy lặp lại
### Chạy liên tục trong bao lâu. (dạng radio)
- Nhập thời gian chạy liên tục: nhập giờ phút giây
### Chạy vô hạn (dạng radio)
- Khi nào bấm dừng thì dừng. mặc định luôn chọn
### Hẹn giờ chạy kịch bản
- Là thời gian hẹn giờ để chạy kịch bản. Có thể chọn ngày tháng năm giờ phút giây. Thiết kế sao cho dễ chọn, có thể dùng dropdown cho từng thành phần (sử dụng dropdown thì cho phép cả nhập dữ liệu nhưng bắt chặt giá trị có thể sai. luôn bắt nhập tháng trước ngày sau) hoặc nhập vào ngày tháng năm và chọn giờ phút giây theo từng ô.
### Thời gian chờ cho từng lần chạy kịch bản - `delay` kịch bản
- Là thời gian chờ trước khi chạy kịch bản. Mặc định nhập là: 0, có thể chọn dropdown bên cạnh là: Mili giây (Millisecond), s: Giây (Second), m: Phút (Minute), h: Giờ (Hour).
### Chống phát hiện (checkbox)
- Khi tích chọn vào đây. Khóa toàn bộ các ô thiết lập `delay`.
- Khi tính năng này được khóa, tất cả các thời gian `delay` được thiết lập sẽ random thêm số giây từ 1 tới 200 Mili giây (Millisecond)


## 💾 (Biểu tượng Đĩa mềm / Save): 
- Lưu cấu hình (Save script). Lưu lại toàn bộ vị trí các điểm tap và swipe và các thông số đã thiết lập thành một kịch bản để tái sử dụng cho các lần sau mà không cần cài đặt lại từ đầu.

## ↕️ (Biểu tượng Di chuyển bốn hướng): 
- Ẩn/Hiện thanh menu. Cho phép di chuyển thanh menu sang vị trí khác hoặc ẩn nó đi để không che màn hình.
- Khi ẩn menu thì hiện thêm 1 button X để đóng `Thanh điều khiển nổi (Floating Control Panel)`

# Thanh điều khiển nổi (Floating Control Panel)
- Thu nhỏ thanh điều khiển nổi bằng 1/2 hiện tại