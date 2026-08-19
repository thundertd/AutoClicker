# Xây dựng tính năng lưu trữ tọa độ bấm như sau:
## Màn hình `danh sách hành động`
### Có 3 button trên cùng:
- Button ghép hành động
    - Khi bấm vào button này sẽ hiện ra màn hình thứ tự các hành động. Có thể chọn hành động theo thứ tự 1,2,3,4, ... Giữa các hành động có thể thiếp lập thời gian delay. (Gợi ý là dạng kéo thả, bấm thêm thời gian delay ở các hành động sau đó kéo vào giữa) Sau đó có thể quay về `danh sách hành động`
- Button xuất các hành động đã chọn ra file json.
    - Bấm vào button này thì sẽ xuất ra file json các hành động được chọn. Muốn chọn hành động cần xuất thì phải giữa chặt hành động ở dưới danh sách, các hành động sẽ nhảy ra checkbox để chọn. Giao diện bổ sung thêm số hành động được chọn và nút xuất. Khi bấm xuất thì sẽ lấy dữ liệu các hành động được chọn từ database sqlite. Sau đó cấu trúc các hành động được chọn vào cùng 1 file json và đảm bảo có thể import ngược trở lại vào database sqlite.
- Button chạy 1 loạt hành động. Khi bấm vào sẽ hiển thị ra bong bóng với các tíng năng sau
    - Hiển thị số thứ tự hành động đang chạy.
    - Bộ đếm các click trong từng hành động.
    - Thời gian deplay nếu có.
    - Nút Play (tam giác): Bắt đầu chu kỳ tự động click vào vị trí đã chọn.
    - Nút Stop (hình ô vuông): Dừng ngay lập tức quá trình tự động click.
    - Nút Cài đặt (hình bánh răng): Quay lại màn hình `danh sách hành động`
### Danh sách hành động bên dưới gồm các cột:
- Tên hành động, button xem chi tiết, button xuất file, button chạy hành động
#### Bấm vào button chi tiết: Vào `màn hình chi tiết`. Trong `màn hình chi tiết` cho phép thiết lập nhiều điểm nhấn tự động khác nhau theo trình tự. Tiết lập tọa độ bấm theo thứ tự 1-2-3-4-5-... `chi tiết` là 1 dạng bong bóng nổi có 4 lựa chọn:
- Nút Thêm (dấu cộng): một thanh công cụ nổi cùng biểu tượng hình tròn (điểm chạm) sẽ xuất hiện trên màn hình điện thoại. Định vị: Bạn có thể di chuyển biểu tượng hình tròn này đến bất kỳ vị trí nào trên ứng dụng hoặc màn hình chính mà bạn muốn hệ thống tự động nhấn
- Nút Play (tam giác): Bắt đầu chu kỳ tự động click vào vị trí đã chọn.
- Nút Stop (hình ô vuông): Dừng ngay lập tức quá trình tự động click.
- Nút Cài đặt (hình bánh răng): Quay lại màn hình `danh sách hành động`database sqlite.
#### Bấm vào button xuất file thì xuất các thiết lập ra file json chỉ 1 hành động
#### Bấm vào button chạy hành động, ứng dụng sẽ vẽ ra 1 khung bong bóng có 3 action
- Nút Play (tam giác): Bắt đầu chu kỳ tự động click vào vị trí đã chọn.
- Nút Stop (hình ô vuông): Dừng ngay lập tức quá trình tự động click.
- Nút Cài đặt (hình bánh răng): Quay lại màn hình `danh sách hành động`