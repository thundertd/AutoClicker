# Tài liệu Hướng dẫn Sử dụng - AutoClicker

## Tổng quan các tính năng mới

Ứng dụng AutoClicker đã được bổ sung các tính năng quản lý và thực thi hành động tự động click phức tạp theo yêu cầu trong file Update.md.

## Cấu trúc Dự án

### 1. **Database Layer** (SQLite)

#### Models (`bean/Action.kt`):
- `Action`: Đại diện cho một hành động với nhiều điểm click
- `ClickPoint`: Một điểm click cụ thể trong hành động
- `ActionSequence`: Chuỗi các hành động (dành cho tính năng tương lai)
- `SequenceItem`: Một item trong chuỗi hành động

#### Database Helper (`database/ActionDatabaseHelper.kt`):
- Quản lý SQLite database
- CRUD operations cho actions và click points
- Export/Import JSON functionality
- 4 bảng chính:
  - `actions`: Lưu thông tin hành động
  - `click_points`: Lưu tọa độ các điểm click
  - `action_sequences`: Lưu chuỗi hành động (để phát triển sau)
  - `sequence_items`: Lưu chi tiết chuỗi hành động

### 2. **UI Layer**

#### ActionListActivity:
**Màn hình danh sách hành động** với các tính năng:

**3 Button trên cùng:**
1. **Ghép hành động** (`btnCombineActions`): 
   - Tính năng đang phát triển
   - Sẽ cho phép tạo chuỗi hành động theo thứ tự

2. **Xuất JSON** (`btnExportSelected`):
   - Giữ lâu một hành động để vào chế độ chọn
   - Checkbox xuất hiện cho phép chọn nhiều hành động
   - Click button này để xuất các hành động đã chọn ra file JSON
   - File được lưu trong thư mục Downloads

3. **Chạy loạt** (`btnRunSequence`):
   - Tính năng đang phát triển
   - Sẽ cho phép chạy một chuỗi hành động

**Danh sách hành động:**
- Mỗi item hiển thị:
  - Tên hành động
  - Số điểm click và số lần lặp
  - 3 buttons:
    - **Chi tiết** (🛈): Mở màn hình chi tiết để thêm/sửa điểm click
    - **Xuất** (💾): Xuất hành động này ra file JSON
    - **Chạy** (▶): Chạy hành động này

**FAB (+):** Tạo hành động mới

#### ActionDetailActivity:
Màn hình chi tiết - Khởi động FloatingActionEditorService

### 3. **Service Layer**

#### FloatingActionEditorService:
**Dịch vụ nổi để chỉnh sửa hành động** với bong bóng điều khiển có 4 nút:

1. **Thêm (+)**: 
   - Thêm điểm click mới
   - Xuất hiện marker hình tròn với số thứ tự
   - Kéo marker đến vị trí mong muốn

2. **Play (▶)**:
   - Chạy thử hành động với các điểm đã đặt
   - Tự động click theo thứ tự các điểm

3. **Stop (⏸)**:
   - Dừng chạy thử

4. **Cài đặt (⚙)**:
   - Lưu các điểm click vào database
   - Quay về màn hình danh sách

**Tính năng:**
- Hiển thị marker đánh số (1, 2, 3...) tại các điểm click
- Kéo thả marker để thay đổi vị trí
- Chạy thử ngay lập tức để kiểm tra

#### FloatingActionRunnerService:
**Dịch vụ chạy hành động** với bong bóng hiển thị thông tin:

**Hiển thị:**
- Tên hành động
- Điểm đang chạy (VD: "Điểm: 2/5")
- Tổng số clicks đã thực hiện
- Thời gian delay giữa các click

**Buttons:**
1. **Play (▶)**: Bắt đầu chạy hành động
2. **Stop (⏸)**: Dừng hành động
3. **Cài đặt (⚙)**: Đóng bubble và quay về

## Hướng dẫn Sử dụng

### Bước 1: Khởi động ứng dụng
1. Mở ứng dụng AutoClicker
2. Cấp quyền Accessibility Service (nếu chưa)
3. Cấp quyền System Alert Window (hiển thị trên các ứng dụng khác)

### Bước 2: Tạo hành động mới
1. Từ màn hình chính, click "Danh sách hành động"
2. Click nút FAB (+) ở góc dưới bên phải
3. Nhập tên cho hành động (VD: "Click vào nút OK")
4. Click "Tạo"

### Bước 3: Thêm điểm click
1. Màn hình sẽ tự động mở chi tiết hành động
2. Ứng dụng chuyển sang background, bubble điều khiển xuất hiện
3. Click nút **Thêm (+)** trên bubble
4. Marker hình tròn với số "1" xuất hiện
5. **Kéo marker** đến vị trí cần click
6. Lặp lại để thêm nhiều điểm (2, 3, 4...)

### Bước 4: Kiểm tra
1. Click nút **Play (▶)** trên bubble
2. Xem hành động chạy thử
3. Nếu cần điều chỉnh, kéo lại marker
4. Click **Stop (⏸)** để dừng

### Bước 5: Lưu và sử dụng
1. Click nút **Cài đặt (⚙)** trên bubble
2. Hành động được lưu tự động
3. Quay về danh sách hành động

### Bước 6: Chạy hành động
1. Trong danh sách, click nút **Chạy (▶)** của hành động
2. Bubble runner xuất hiện với thông tin
3. Click **Play (▶)** để bắt đầu
4. Theo dõi tiến trình trên bubble
5. Click **Stop (⏸)** để dừng bất cứ lúc nào

### Bước 7: Xuất/Chia sẻ hành động

**Xuất một hành động:**
1. Click nút **Xuất (💾)** của hành động
2. File JSON được lưu trong Downloads
3. Tên file: `TênHànhĐộng_timestamp.json`

**Xuất nhiều hành động:**
1. Giữ lâu một hành động trong danh sách
2. Checkbox xuất hiện cho tất cả items
3. Chọn các hành động cần xuất
4. Click button **Xuất JSON** ở trên cùng
5. File JSON chứa tất cả hành động được lưu

## Cấu trúc File JSON

```json
[
  {
    "id": 1,
    "name": "Click vào nút OK",
    "repeatCount": 1,
    "delayBetweenClicks": 200,
    "clickPoints": [
      {
        "sequence": 1,
        "x": 500,
        "y": 1000,
        "clickCount": 1,
        "delayAfter": 0
      },
      {
        "sequence": 2,
        "x": 600,
        "y": 1200,
        "clickCount": 2,
        "delayAfter": 500
      }
    ]
  }
]
```

## Quyền Yêu Cầu

1. **SYSTEM_ALERT_WINDOW**: Hiển thị bubble và marker
2. **WRITE_EXTERNAL_STORAGE**: Xuất file JSON
3. **READ_EXTERNAL_STORAGE**: Đọc file JSON (cho import sau này)
4. **Accessibility Service**: Thực hiện auto click

## Tính Năng Đang Phát Triển

1. **Ghép hành động**: 
   - Tạo chuỗi hành động theo thứ tự
   - Thêm delay giữa các hành động
   - Giao diện kéo thả để sắp xếp

2. **Chạy chuỗi hành động**:
   - Chạy nhiều hành động liên tiếp
   - Theo dõi tiến trình từng hành động

3. **Import JSON**:
   - Nhập hành động từ file JSON
   - Chia sẻ hành động giữa các thiết bị

4. **Chỉnh sửa nâng cao**:
   - Chỉnh sửa tọa độ bằng số
   - Thêm/sửa delay cho từng điểm
   - Đặt số lần click cho mỗi điểm

## Lưu Ý Kỹ Thuật

### Database:
- SQLite với 4 bảng
- Foreign key constraints với CASCADE
- Auto-increment primary keys

### Threading:
- UI updates trên Main/UI thread (Handler)
- Click execution trên background thread
- Timer cho repeat actions

### Permissions:
- Runtime permission request cho Android 6+
- Accessibility service cần được enable thủ công
- System Alert Window cần approval

### Layout:
- RecyclerView cho danh sách hiệu quả
- CardView cho items đẹp
- FloatingActionButton cho thêm nhanh
- WindowManager cho floating views

## Troubleshooting

**Không click được:**
- Kiểm tra Accessibility Service đã bật chưa
- Android 7+ mới hỗ trợ gesture dispatch

**Không thấy bubble:**
- Kiểm tra quyền System Alert Window
- Settings > Apps > AutoClicker > Display over other apps

**Không xuất được file:**
- Cấp quyền Storage
- Kiểm tra thư mục Downloads

**Marker không hiển thị:**
- Restart service
- Kiểm tra quyền overlay

## Credits

Based on the original AutoClicker by nestorm001
Enhanced with action management features as per Update.md specifications.
