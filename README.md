# Lịch Vạn Niên & Tử Vi Hoàng Đạo 📅✨

[![Kotlin Version](https://img.shields.io/badge/kotlin-2.3.20-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Version](https://img.shields.io/badge/minSdk-24-green.svg?logo=android)](https://developer.android.com)
[![Material Design](https://img.shields.io/badge/Material--3-M3-orange.svg?logo=materialdesign)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Ứng dụng **Lịch Vạn Niên & Tử Vi Hoàng Đạo** là giải pháp tra cứu lịch âm dương truyền thống kết hợp xem tử vi cung hoàng đạo cá nhân hóa. Được xây dựng trên nền tảng Kotlin Android hiện đại, tuân thủ nghiêm ngặt **Clean Architecture** và các nguyên lý của **Material 3 Design**.

---

## 🌟 Tính Năng Chính

1. **Lịch Tháng Tra Cứu Âm Dương**:
   - Chuyển đổi Âm - Dương lịch múi giờ GMT+7 chính xác theo thuật toán thiên văn của nhà khoa học Hồ Ngọc Đức.
   - Hiển thị ngày Hoàng Đạo (cát), Hắc Đạo (hung), Trực của ngày và giờ hoàng đạo tương ứng.
   - Đánh dấu các ngày lễ đặc biệt (âm lịch và dương lịch) bằng chấm tròn cam nổi bật.
2. **Chi Tiết Ngày Cát Hung**:
   - Tra cứu chi tiết Can Chi của ngày/tháng/năm hiện tại.
   - Liệt kê danh sách các giờ hoàng đạo chi tiết trong ngày dưới dạng các chip bo góc.
   - Thống kê các ngày lễ truyền thống hoặc sự kiện lịch sử xảy ra trong ngày.
3. **Onboarding Cá Nhân Hóa & Cung Hoàng Đạo**:
   - Lựa chọn ngày sinh bằng DatePicker trực quan để tự động phân tích và tính toán cung hoàng đạo của riêng bạn.
   - Lưu trữ an toàn ngày sinh nội bộ bất đồng bộ thông qua **Jetpack DataStore**.
4. **Tử Vi Hàng Ngày, Tuần, Tháng**:
   - Nạp thông tin tử vi (Màu sắc, Con số may mắn, Giờ hoàng đạo, Tâm trạng, Cung hợp...) thông qua API `aztro`.
   - Cơ chế **Room Caching 24 giờ** độc lập theo từng tab giúp tăng tốc độ tải và tiết kiệm dữ liệu mạng.
   - Cơ chế tự động fallback thông minh sang dữ liệu offline từ assets JSON khi mất mạng.

---

## 🛠️ Công Nghệ Sử Dụng

- **Ngôn ngữ**: Kotlin (bản 2.3.20)
- **UI Framework**: Jetpack Compose (Material 3) với chuyển động trượt ngang, staggered fade-in so le và crossfade transitions mượt mà.
- **Dependency Injection**: Dagger Hilt (2.60)
- **Cơ sở dữ liệu**: Room Database (2.6.1) quản lý sự kiện và cache tử vi.
- **Lưu trữ nhẹ**: Jetpack DataStore Preferences
- **Mạng**: Retrofit 2 + OkHttp + kotlinx.serialization
- **Testing**: JUnit 4, Google Truth, MockK, Compose UI Test.

---

## 📂 Cấu Trúc Dự Án (Clean Architecture)

```text
app/src/main/java/com/example/lichvannien/
├── data/
│   ├── local/
│   │   ├── db/          # Room DB, DAOs (SpecialDayDao, HoroscopeCacheDao)
│   │   ├── entity/      # Room Entities (SpecialDayEntity, HoroscopeCacheEntity)
│   │   └── datastore/   # UserPreferences lưu ngày sinh
│   ├── remote/
│   │   ├── api/         # AztroApi (Retrofit interface)
│   │   └── dto/         # DTO classes (HoroscopeResponse)
│   └── repository/      # Triển khai các Repository (SpecialDayRepositoryImpl, HoroscopeRepositoryImpl)
├── domain/
│   ├── model/           # Các domain models thuần tuý (SolarDate, LunarDate, Horoscope, DayDetail)
│   ├── repository/      # Các Repository Interface định nghĩa nghiệp vụ
│   ├── usecase/         # Lớp Use Cases (GetDayDetailUseCase)
│   └── util/            # Lớp tiện ích thuật toán (LunarConverter, AuspiciousCalculator, ZodiacHelper)
├── di/                  # Hilt Dependency Injection Modules
└── ui/
    ├── calendar/        # Màn hình Lịch Tháng (CalendarScreen, CalendarViewModel)
    ├── detail/          # Màn hình Chi Tiết Ngày (DayDetailScreen, DayDetailViewModel)
    ├── horoscope/       # Màn hình Tử Vi (HoroscopeScreen, HoroscopeViewModel)
    ├── onboarding/      # Màn hình Chọn ngày sinh (OnboardingScreen, OnboardingViewModel)
    ├── navigation/      # Cấu hình routes và navigation Compose
    └── theme/           # Cấu hình Material 3 Colors, Typo, Theme ấm cúng đỏ/cam
```

---

## 🚀 Hướng Dẫn Xây Dựng & Chạy Ứng Dụng

### Yêu Cầu Hệ Thống
- **Android Studio**: Ladybug (2024.2.1+) hoặc mới hơn.
- **JDK**: Java 17 trở lên.
- **Android SDK**: Compile SDK 36, Min SDK 24.

### Các Bước Thực Hiện

1. **Clone dự án**:
   ```bash
   git clone <repository_url>
   cd Calendar
   ```
2. **Chạy Unit Tests**:
   Kiểm tra tính chính xác của các thuật toán âm dương lịch, tính ngày tốt xấu và caching:
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. **Chạy Phân Tích Chất Lượng Code (Lint)**:
   ```bash
   ./gradlew lintDebug
   ```
4. **Biên dịch và sinh APK Debug**:
   ```bash
   ./gradlew assembleDebug
   ```
   *File APK sau khi compile thành công sẽ nằm ở:* `app/build/outputs/apk/debug/app-debug.apk`

---

## 📜 Giấy Phép (License)

Dự án được phân phối dưới giấy phép Apache License 2.0. Xem chi tiết tại tệp `LICENSE`.
