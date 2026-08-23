# Kế hoạch khắc phục độ trễ thao tác lịch

## Mục tiêu và phạm vi

Phạm vi là thao tác đổi tháng trên `CalendarScreen`, nơi đã tái hiện lỗi trên thiết bị thật V2352A (Android 16). Mục tiêu là phản hồi trực tiếp khi vuốt, không bỏ/lặp thao tác khi vuốt liên tiếp, và giữ khung hình ổn định trên màn hình 120 Hz.

Điều kiện nghiệm thu cho 10 lần vuốt trái/phải liên tiếp trên thiết bị thật:

- Tháng đổi đúng 10 lần; không bị bỏ thao tác hoặc tải lặp cùng một tháng.
- Phản hồi thị giác bắt đầu trong tối đa 1 frame sau khi vượt touch slop.
- `gfxinfo`: p95 <= 16 ms, p99 <= 32 ms, janky frames < 3%, và không có frame nào >= 50 ms trong lượt đo ổn định.
- Không có frame `Slow UI thread` trong lượt đo ổn định. Nếu hệ điều hành báo `High input latency`, tổng số phải <= 2 trên 10 lần vuốt.

Các ngưỡng được kiểm tra lại trên một thiết bị Android thật chậm hơn trước khi chốt. Không đánh đổi khả năng truy cập, độ chính xác âm lịch, hoặc chức năng chọn ngày để lấy hiệu năng.

## Cơ sở hiện tại

Mẫu tối thiểu là một lần đổi tháng. Trên thiết bị thật, p95 là 350–400 ms; năm lần vuốt cho p95 450 ms, p99 650 ms, và GPU p95 chỉ 13 ms. Tắt animation hệ thống vẫn để lại các frame 300–350 ms. Vì vậy, nghẽn là dựng/layout lại UI trên CPU, không phải GPU hay mạng.

Hai nguồn chính cần xử lý là:

1. `AnimatedContent` đang animate cả danh sách tháng, nên hai `LazyVerticalGrid` với các `DayCell` được composition/layout cùng lúc.
2. Kết quả tháng chỉ được publish sau khi toàn bộ truy vấn sự kiện và tính 42 ngày hoàn tất; các lần vuốt nhanh còn có thể dựa trên `_currentMonthYear` cũ.

## Pha 1 — Biến phép đo thành regression gate

1. Tạo `scripts/measure-calendar-swipe.ps1` nhận `-Serial`, số lượt vuốt, chiều vuốt và thời lượng gesture. Script chỉ chạy trên thiết bị thật, reset `gfxinfo`, thực hiện lượt vuốt xác định, lấy header tháng cuối và in các chỉ số frame quan trọng.
2. Thêm kiểm tra pass/fail theo các ngưỡng ở trên. Lưu kết quả đo dạng text/JSON ngoài source tree.
3. Khi cần xác nhận phản hồi thị giác, thu trace Perfetto trên thiết bị thật sau khi có phê duyệt; không thêm module benchmark vào app.

Hoàn thành khi một lệnh có thể tái hiện lỗi hiện tại và chuyển xanh sau khi sửa:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\measure-calendar-swipe.ps1 -Serial <serial> -Count 10
```

## Pha 2 — Tách điều hướng tháng khỏi việc nạp dữ liệu

1. Thay hai `StateFlow` rời rạc bằng một `CalendarUiState` bất biến: tháng hiển thị, tháng đích, dữ liệu grid đã sẵn sàng, và trạng thái tải.
2. Cập nhật tháng đích đồng bộ khi nhận gesture. Dùng `flatMapLatest`/job có thể hủy để chỉ giữ yêu cầu tháng mới nhất; loại bỏ việc mỗi lần vuốt nhanh đều tính lại từ `_currentMonthYear` cũ.
3. Cache lưới của tháng hiện tại và hai tháng kề bằng cache giới hạn kích thước. Khi mở tháng M, nạp trước M-1 và M+1 trên `Dispatchers.Default`/`IO` mà không chặn UI.
4. Gộp dữ liệu sự kiện tháng thành map/set trước khi tạo 42 ô, thay vì duyệt danh sách sự kiện với `any` cho từng ngày.

Kiểm chứng bằng unit test: 10 yêu cầu `next/previous` nhanh phải có tháng đích chính xác, yêu cầu cũ bị hủy/deduplicate, và cache trả lưới tháng kề mà không gọi lại repository.

## Pha 3 — Thay cấu trúc render tháng

1. Loại bỏ `AnimatedContent(targetState = daysList)` bao quanh toàn bộ grid. Không animate đồng thời lưới cũ và mới.
2. Dùng `HorizontalPager` của Compose Foundation với chỉ trang hiện tại và trang kề được giữ sẵn; để pager xử lý drag, velocity và phản hồi theo ngón tay. Không còn `pointerInput`/ngưỡng 100 px tự quản lý.
3. Tách `CalendarGrid` thành composable nhận render model ổn định. So sánh `LazyVerticalGrid` với grid cố định 6 hàng x 7 cột; chọn biến thể đạt frame budget tốt hơn trên thiết bị thật.
4. Thay tháng chỉ thay dữ liệu của trang, không dựng lại toàn bộ scaffold/header/bottom navigation.

Kiểm chứng bằng UI test: vuốt trái/phải đổi đúng một tháng, vuốt ngược trả về tháng cũ, click ngày vẫn điều hướng đúng, và 10 gesture nhanh không mất thứ tự.

## Pha 4 — Giảm công việc trong `DayCell`

1. Tạo `CalendarDayUiModel` bất biến trong ViewModel/background work: thứ trong tuần, màu/trạng thái, chuỗi ngày âm hiển thị, cờ sự kiện, và các giá trị cần vẽ.
2. Giữ semantics cho TalkBack, nhưng memoize `contentDescription` theo ngày và locale; không dựng lại `LocalDate` và chuỗi dài ở mọi recomposition.
3. Ổn định key, lambda và modifier của ô ngày; chỉ recomposition ô có dữ liệu thay đổi. Giữ màu/chip/viền hiện có trừ khi phép đo chứng minh chúng vượt frame budget.

Kiểm chứng bằng Layout Inspector/Compose tracing: khi đổi tháng chỉ grid/trang liên quan recomposition; một thay đổi trạng thái ngoài lịch không dựng lại 42 ô.

## Pha 5 — Hoàn thiện và chốt hiệu năng

1. Tạo Baseline Profile cho luồng mở app → lịch → đổi tháng để giảm JIT ở lần tương tác đầu.
2. Đo cold start, lần đổi tháng đầu và chuỗi 10 lần đổi tháng trên thiết bị thật; so sánh với baseline đã ghi ở Pha 1.
3. Chạy unit test, lint, UI/instrumented test, và regression gate vuốt tháng. Kiểm tra thủ công TalkBack, dark mode, ngày lễ và tháng nhuận.
4. Xóa toàn bộ probe/timing tạm thời. Cập nhật tài liệu đo hiệu năng và ghi rõ ngưỡng đạt được.

## Thứ tự triển khai và điểm dừng

Triển khai Pha 1 trước, sau đó lần lượt Pha 2, Pha 3, Pha 4. Sau mỗi pha phải chạy lại phép đo; nếu các ngưỡng đã đạt sớm thì không làm tối ưu tiếp làm phức tạp kiến trúc. Pha 5 chỉ bắt đầu khi tương tác đổi tháng đã xanh trên thiết bị thật.
