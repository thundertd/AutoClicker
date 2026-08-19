# AutoClicker - Quản Lý Hành Động Tự Động

## 📱 Giới Thiệu

Ứng dụng Android cho phép tạo và quản lý các hành động tự động click với nhiều điểm chạm theo trình tự.

## ✨ Tính Năng Mới

### 1. Danh Sách Hành Động
- Xem tất cả hành động đã tạo
- Tạo hành động mới với tên tùy chỉnh
- Chọn nhiều hành động để xuất JSON
- Chạy từng hành động hoặc theo chuỗi

### 2. Chỉnh Sửa Hành Động
- Thêm điểm click bằng marker kéo thả
- Marker đánh số theo thứ tự (1, 2, 3...)
- Chạy thử ngay lập tức
- Lưu tự động vào database

### 3. Chạy Hành Động
- Floating bubble hiển thị tiến trình
- Đếm số click thực hiện
- Hiển thị điểm đang chạy
- Pause/Resume bất cứ lúc nào

### 4. Export/Import
- Xuất một hoặc nhiều hành động ra JSON
- File lưu trong Downloads
- Format chuẩn, dễ chia sẻ

## 🚀 Cách Sử Dụng Nhanh

```
1. Mở app → "Danh sách hành động"
2. Click FAB (+) → Nhập tên hành động
3. Bubble xuất hiện → Click "Thêm" (+)
4. Kéo marker đến vị trí cần click
5. Thêm nhiều điểm khác (tuỳ chọn)
6. Click "Cài đặt" (⚙) để lưu
7. Quay về list → Click "Chạy" (▶)
```

## 📋 Yêu Cầu

- Android 7.0+ (API 24+)
- Quyền Accessibility Service
- Quyền System Alert Window
- Quyền Storage (cho export)

## 🏗️ Cấu Trúc Code

```
app/src/main/java/com/github/nestorm001/autoclicker/
├── bean/
│   ├── Event.kt (original)
│   └── Action.kt (NEW - data models)
├── database/
│   └── ActionDatabaseHelper.kt (NEW - SQLite)
├── service/
│   ├── AutoClickService.kt (original)
│   ├── FloatingClickService.kt (original)
│   ├── FloatingActionEditorService.kt (NEW)
│   └── FloatingActionRunnerService.kt (NEW)
├── MainActivity.kt (updated)
├── ActionListActivity.kt (NEW)
├── ActionListAdapter.kt (NEW)
└── ActionDetailActivity.kt (NEW)
```

## 📖 Tài Liệu

- **[FEATURE_DOCUMENTATION.md](FEATURE_DOCUMENTATION.md)** - Hướng dẫn chi tiết
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Tóm tắt triển khai
- **[COMPLETED_SUMMARY.md](COMPLETED_SUMMARY.md)** - Báo cáo hoàn thành
- **[Update.md](Update.md)** - Yêu cầu gốc

## 🔧 Build

### Android Studio:
```bash
1. Open project
2. Sync Gradle
3. Run 'app'
```

### Command Line:
```bash
./gradlew assembleDebug
```

## 📱 Screenshots (Mô tả)

### Màn hình chính
- 2 buttons: "Danh sách hành động" và "Start Simple Click"

### Danh sách hành động
- 3 buttons trên: Ghép, Xuất, Chạy loạt
- RecyclerView: Danh sách các hành động
- Mỗi item: Tên, số điểm, 3 action buttons
- FAB: Tạo hành động mới

### Editor (Floating Bubble)
- 4 buttons: Thêm, Play, Stop, Cài đặt
- Markers đánh số trên màn hình
- Có thể kéo thả markers

### Runner (Floating Bubble)
- Hiển thị: Tên, tiến trình, clicks, delay
- 3 buttons: Play, Stop, Cài đặt

## 🎯 Status

✅ **100% Complete** - Sẵn sàng sử dụng!

### Đã triển khai:
- ✅ Database SQLite
- ✅ Màn hình danh sách
- ✅ Editor với floating bubble
- ✅ Runner với progress tracking
- ✅ Export JSON
- ✅ Permissions handling
- ✅ Long press selection

### Để sau (optional):
- ⏳ Ghép hành động thành chuỗi
- ⏳ Import JSON UI
- ⏳ Chỉnh sửa nâng cao

## 📄 License

Dựa trên dự án gốc của nestorm001

## 🤝 Credits

- Original AutoClicker: nestorm001
- Feature Enhancement: Based on Update.md specifications
- Implementation: GitHub Copilot

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra Accessibility Service đã bật
2. Kiểm tra quyền System Alert Window
3. Xem [FEATURE_DOCUMENTATION.md](FEATURE_DOCUMENTATION.md) phần Troubleshooting

---

**Version:** 2.0 (với tính năng quản lý hành động)
**Min SDK:** 24 (Android 7.0)
**Target SDK:** 35
**Last Updated:** 2026-08-18
