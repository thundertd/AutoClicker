# Xây dựng tính năng lưu trữ tọa độ bấm như sau:
## Màn hình `danh sách kịch bản` là màn hình chính
### Có 3 button trên cùng:
- Button điều chỉnh kịch bản:  Bấm vào có thông báo Chờ phát triển
    - Khi bấm vào button này sẽ hiện ra màn hình `thứ tự các kịch bản`. Có thể chọn kịch bản theo thứ tự 1,2,3,4, ... Giữa các kịch bản có thể thiếp lập thời gian delay. (Gợi ý là dạng kéo thả, bấm thêm thời gian delay ở các kịch bản sau đó kéo vào giữa) Sau đó có thể quay về `danh sách kịch bản`
- Button xuất các kịch bản đã chọn ra file json. Bấm vào có thông báo Chờ phát triển
- Button chạy 1 loạt kịch bản. Bấm vào có thông báo Chờ phát triển
### Danh sách kịch bản bên dưới gồm các cột:
- STT, Tên kịch bản, button active, button xem chi tiết, button xuất file, button chạy kịch bản
#### Bấm vào button chi tiết tương đương với nút start hiện tại, nhưng không đóng ứng dụng mà vẫn hiện màn hinh start.
#### Bấm vào button xuất file có thông báo Chờ phát triển
#### Bấm vào button chạy kịch bản có thông báo Chờ phát triển