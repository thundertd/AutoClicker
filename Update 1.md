# Bấm vào button chạy hành động:
- Hiển thị lớp giao diện phủ (Floating Widget / System Alert Window)
## Lớp giao diện nổi (Floating Overlay Widget)
- Cung cấp cho người dùng một công cụ điều khiển linh hoạt, hiển thị xuyên suốt trên mọi ứng dụng để người dùng có thể tự do định vị các điểm thao tác (click/swipe) và kích hoạt kịch bản tự động hóa mà không bị gián đoạn trải nghiệm sử dụng app đích.
- Cơ chế Kỹ thuật & Quyền hệ thống (System Level Requirements)##
- Cấp quyền hiển thị: Hệ thống yêu cầu người dùng cấp quyền `SYSTEM_ALERT_WINDOW` (Hiển thị trên các ứng dụng khác) và `ACCESSIBILITY_SERVICE` (Dịch vụ hỗ trợ để gửi tọa độ chạm/vuốt đến hệ thống). Nếu đã có rồi thì thôi.
- Cơ chế lớp (Z-Index Rendering): Lớp giao diện nổi luôn có chỉ số `Z-Index` cao nhất để nằm trên bề mặt của ứng dụng hệ thống cũng như các ứng dụng bên thứ ba.

## Nghiệp vụ Chi tiết từng Thành phần

### Thanh điều khiển nổi (Floating Control Panel)
- Gắn cố định lớp trên (Always-on-top): Luôn hiển thị ở cạnh màn hình và có thể kéo thả khắp màn hình nhưng luôn giữ vị trí đã kéo đó kể cả khi người dùng chuyển sang ứng dụng khác (mở Game, Facebook, TikTok...).
- Kéo thả di chuyển: Người dùng có thể chạm giữ vào biểu tượng di chuyển (mũi tên 4 hướng) để kéo thanh điều khiển đến bất kỳ vị trí nào trên màn hình nhằm tránh che khuất tầm nhìn.
- Tương tác xuyên qua (Touch passthrough):Những khu vực không thuộc thanh điều khiển hoặc các điểm mục tiêu sẽ không cản trở thao tác chạm của người dùng vào ứng dụng bên dưới.
### Các tính năng trên thanh điều khiển nổi

- ▶️ (Biểu tượng Play màu xanh): Chạy (Play) script (danh sách các hành động) đã thiết lập. Khi đang chạy, nút này thường biến thành nút Tạm dừng (Pause) ⏸️.

- ➕ (Biểu tượng Dấu cộng màu xanh lá): Thêm điểm nhấn (Add target point). Mỗi lần nhấn, một vòng tròn màu đỏ đánh số mới (như số 1, 2, 3, 4 và các số khác sẽ tạo theo thứ tự) sẽ xuất hiện ở giữa màn hình , phía trên các ứng dụng được hiển thị.

- ➡️ (Biểu tượng Mũi tên sang phải màu cam): Thêm hành động vuốt (Add swipe action). Hình dáng & Thiết kế:

    - Cấu trúc: Gồm hai đầu hình tròn nổi màu xanh lá cây kết nối với nhau bằng một mũi tên màu đỏ.
    - Đầu bắt đầu (Điểm gốc): Chấm tròn màu xanh lá cây đậm ở một đầu đại diện cho điểm chạm đầu tiên.
    - Đầu kết thúc (Điểm đích): Chấm tròn lớn hơn có viền đậm và dán nhãn số thứ tự là số tiếp theo (Ví dụ bấm ➕ 3 lần thì khi ➡️ số thứ tự phải là 4 sau khi bấm tiếp ➕ thì tạo ra điểm nhấn mới số thứ tự là 5 vì 2 nút này số thứ tự sẽ luôn liên tiếp nhau) màu xanh lá cây, đại diện cho điểm kết thúc vuốt.
    - xuất hiện ở giữa màn hình , phía trên các ứng dụng được hiển thị. luôn có hướng mũi tên từ trái sang phải. Và có 1 độ dài vừa phải

- ➖ (Biểu tượng Dấu trừ màu cam): Xóa điểm (Remove target). Xóa điểm nhấn hoặc đường vuốt cuối cùng được thêm vào.

- ⚙️ (Biểu tượng Bánh răng): Cài đặt chung (Settings). Cài đặt các thông số cho cả kịch bản, ví dụ: chạy lặp lại bao nhiêu lần, chạy liên tục trong bao lâu.

- 💾 (Biểu tượng Đĩa mềm / Save): Lưu cấu hình (Save script). Lưu lại toàn bộ vị trí các điểm (1, 2, 3...) và thông số thời gian đã thiết lập thành một kịch bản để tái sử dụng cho các lần sau mà không cần cài đặt lại từ đầu.

- ↔️ (Biểu tượng Di chuyển bốn hướng): Ẩn/Hiện thanh menu. Cho phép di chuyển thanh menu sang vị trí khác hoặc ẩn nó đi để không che màn hình.