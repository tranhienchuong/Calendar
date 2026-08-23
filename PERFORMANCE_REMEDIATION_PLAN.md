# Kế Hoạch Tối Ưu Hóa Toàn Diện Hiệu Năng & Độ Trễ Ứng Dụng Lịch

Tài liệu này vạch ra lộ trình kỹ thuật chi tiết theo từng **Phase** nhằm giải quyết triệt để độ trễ (latency), hiện tượng khựng khung hình (jank/dropped frames) khi vuốt, chuyển trang, ấn nút và điều hướng trong toàn bộ ứng dụng.

---

## 1. Mục Tiêu & Tiêu Chuẩn Nghiệm Thu (Acceptance Criteria)

- **Phản hồi chạm (Touch Responsiveness):** Phản hồi thị giác bắt đầu trong vòng 1 frame ($\le 16\text{ ms}$) sau khi chạm hoặc vượt ngưỡng touch slop.
- **Tốc độ khung hình (Frame Timing):** 
  - `gfxinfo` $p95 \le 16\text{ ms}$, $p99 \le 32\text{ ms}$ trên màn hình 60Hz/120Hz.
  - Tỷ lệ khung hình giật (Janky frames) $< 3\%$.
  - Không có frame nào $\ge 50\text{ ms}$ trong các phiên đo ổn định.
  - `Slow UI thread` $= 0$ và `High input latency` $\le 2$ trên 10 lần thao tác liên tiếp.
- **Tính toàn vẹn:** Đảm bảo 100% độ chính xác thuật toán Âm lịch, ngày Hoàng Đạo, sự kiện đặc biệt và khả năng hỗ trợ TalkBack.

---

## 2. Chi Tiết Các Phase Thực Hiện

### Phase 1: Tối Ưu Hóa Cây Giao Diện & Cơ Chế Recomposition của Lưới Lịch

- **Trích xuất Theme & Color Palette:**
  - Dời việc gọi `isSystemInDarkTheme()` và `MaterialTheme.colorScheme` ra khỏi `DayCell`.
  - Tạo data class `@Immutable DayCellThemePalette` được tính toán 1 lần tại cấp `CalendarGrid` và truyền xuống.
- **Tối giản hóa cấu trúc Composable của `DayCell`:**
  - Gom các phần tử layout lồng nhau (loại bỏ các `Spacer` thừa, tinh gọn modifier chain).
  - Tận dụng `Modifier.drawBehind` để vẽ nền / viền / chấm sự kiện thay vì lồng thêm `Box` và chuỗi `clip().border().background()`.
- **Ổn định hóa Click Callback (Stable Lambdas):**
  - Thay thế lambda tạo mới `{ onDayClick(day.year, day.month, day.day) }` trong vòng lặp bằng lambda ổn định hoặc callback tham chiếu trực tiếp để kích hoạt cơ chế **Skippable Composable**.
- **Memoize Content Description cho TalkBack:**
  - Đảm bảo `contentDescription` được tạo sẵn trong `CalendarDayUiModel` từ background worker, không phân bổ chuỗi mới trong quá trình render.

*Tập tin tác động:*
- `app/src/main/java/com/example/lichvannien/ui/calendar/CalendarScreen.kt`
- `app/src/main/java/com/example/lichvannien/ui/calendar/CalendarViewModel.kt`

---

### Phase 2: Tối Ưu Hóa Quản Lý State & Cơ Chế Caching

- **Warm-up Cache khởi tạo:**
  - Ngay khi ViewModel khởi tạo, tính toán sẵn cả 3 tháng ($M-1, M, M+1$) trước khi UI Pager render để tránh `getMonthDays` trả về `null`.
- **Gộp State Updates (Batch Preloading Updates):**
  - Khi `preloadAdjacentMonths` tính toán xong tháng trước và tháng sau, gom kết quả và cập nhật `_uiState` một lần duy nhất thay vì phát nhiều state rời rạc.
- **Tách biệt State hiển thị Header khỏi State Lưới ngày:**
  - Sử dụng `derivedStateOf` để lắng nghe trang hiện tại của `PagerState` cho Header (tháng/năm), ngăn việc thay đổi trạng thái Header làm recompose toàn bộ màn hình khi đang drag.
- **Hủy bỏ Job cũ kịp thời khi vuốt nhanh:**
  - Giữ cơ chế `activeLoadJob?.cancel()` khi có yêu cầu chuyển tháng mới, đảm bảo CPU chỉ xử lý tháng đích cuối cùng.

*Tập tin tác động:*
- `app/src/main/java/com/example/lichvannien/ui/calendar/CalendarViewModel.kt`
- `app/src/main/java/com/example/lichvannien/ui/calendar/CalendarScreen.kt`

---

### Phase 3: Tối Ưu Hóa Tốc Độ Phản Hồi Chạm & Điều Hướng

- **Hiển thị tức thì cho `DayDetailScreen` (Instant UI Rendering):**
  - Không để màn hình trắng hiển thị ProgressBar khi ấn vào ngày; hiển thị ngay thông tin ngày Dương, ngày Âm và Trực từ dữ liệu đã có.
  - Tải ngầm thông tin mở rộng (chi tiết sự kiện, giờ hoàng đạo từ database) và cập nhật mượt mà sau đó.
- **Tối giản hóa `DayDetailScreen` Composable:**
  - Chuyển logic tính thứ (`LocalDate.of`) ra ngoài Composable body.
  - Tinh gọn `FlowRow` và các `SuggestionChip` trong `HoursCard`.
- **Tối ưu hóa chuyển Tab (`MainScreen`):**
  - Đơn giản hóa cơ chế `Crossfade` khi chuyển tab Bottom Navigation để tránh render đồng thời 2 cây giao diện nặng trong quá trình chuyển cảnh.

*Tập tin tác động:*
- `app/src/main/java/com/example/lichvannien/ui/detail/DayDetailScreen.kt`
- `app/src/main/java/com/example/lichvannien/ui/detail/DayDetailViewModel.kt`
- `app/src/main/java/com/example/lichvannien/ui/MainScreen.kt`

---

### Phase 4: Tối Ưu Hóa Animation & Loại Bỏ Anti-Patterns

- **Tái cấu trúc Shimmer Effect:**
  - Loại bỏ `Modifier.composed` trong `HoroscopeScreen.kt`.
  - Viết lại `Modifier.shimmerEffect` bằng `Modifier.drawWithContent` kết hợp `Brush.linearGradient` và `graphicsLayer` để không phân bổ lại modifier và tránh recomposition liên tục trên UI thread.
- **Chuẩn hóa Animation Curves & Delays:**
  - Giảm thiểu các `delayMillis` lồng nhau trong `AnimatedVisibility` để giao diện phản hồi nhanh và dứt khoát hơn khi người dùng tương tác.

*Tập tin tác động:*
- `app/src/main/java/com/example/lichvannien/ui/horoscope/HoroscopeScreen.kt`
- `app/src/main/java/com/example/lichvannien/ui/onboarding/OnboardingScreen.kt`

---

### Phase 5: Baseline Profiles, Build Release & Đo Lường Hồi Quy (Verification Gate)

- **Cập nhật Baseline Profile (`baseline-prof.txt`):**
  - Bổ sung quy tắc biên dịch sẵn (AOT) cho toàn bộ các Composable quan trọng (`DayCell`, `CalendarGrid`, `DayDetailScreen`, `HoroscopeScreen`, Navigation routes).
- **Chạy bộ kiểm thử tự động toàn diện:**
  - Chạy Unit test: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task unit-test`
  - Chạy Lint kiểm tra: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task lint`
- **Đo lường hồi quy trên thiết bị thật:**
  - Thực thi script đo đạc thao tác vuốt:
    ```powershell
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\measure-calendar-swipe.ps1 -Serial <serial> -Count 10 -Direction Left
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\measure-calendar-swipe.ps1 -Serial <serial> -Count 10 -Direction Right
    ```
  - Kiểm tra các tiêu chuẩn `PASS` của gate: $p95 \le 16\text{ ms}$, Janky frames $< 3\%$, `Slow UI thread` $= 0$.
