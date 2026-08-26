# Vietnamese Perpetual Calendar & Eastern Feng Shui (Lịch Vạn Niên) 📅✨

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.3.20-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Version](https://img.shields.io/badge/Android-minSdk%2024%20|%20compileSdk%2036-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-orange.svg?logo=jetpackcompose)](https://m3.material.io)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVVM-purple.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Lịch Vạn Niên** (Perpetual Calendar & Eastern Feng Shui) is a modern Android application for Vietnamese Lunar-Solar calendar lookup, Eastern astrological Feng Shui insights, task scheduling with persistent alarm reminders, and an integrated AI Feng Shui Assistant powered by DeepSeek.

Built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, **Room**, **Hilt**, and strict adherence to **Clean Architecture** principles.

---

## 🌟 Key Features

### 1. 📅 Dual Solar-Lunar Calendar (Lịch Âm Dương)
- **High-Precision Astronomy Algorithm**: Exact solar-to-lunar conversions for the GMT+7 timezone based on the renowned astronomical algorithm by Dr. Ho Ngoc Duc.
- **Monthly Overview**: Month grid view featuring lunar day markings, auspicious (Hoàng Đạo) and inauspicious (Hắc Đạo) indicators, major national holidays, and traditional solar terms (Tiết Khí).
- **Interactive Gestures**: Smooth horizontal month swiping with predictive animations.

### 2. 🔮 Eastern Feng Shui & Day Insights (Phong Thủy & Cát Hung)
- **Can Chi Calculation**: Comprehensive computation of Heavenly Stems and Earthly Branches (Can Chi) for year, month, day, and hour.
- **Auspicious Hours (Giờ Hoàng Đạo)**: Identifies favorable time blocks for conducting important tasks (departures, weddings, groundbreakings, etc.).
- **12 Day Officers (Trực) & 28 Mansions (Nhị Thập Bát Tú)**: Deep traditional insights, conflict ages (Tuổi xung khắc), and auspicious departure directions (Hướng xuất hành).

### 3. 📝 Smart Task Management & Persistent Alarm Reminders
- **Rich Task Organizer**: Organize tasks with pastel color palettes, priority levels, categories, and calendar date bindings.
- **Exact Alarms & Notification Reminders**: Uses `AlarmManager` with exact scheduling and foreground services to trigger reliable reminders.
- **Full-Screen Lock Screen Alarm**: Full-screen overlay `TaskAlarmActivity` with vibration and ringtones for high-priority reminders.
- **Device Reboot Resilience**: Automatically reschedules active alarms upon system reboot via `TaskBootReceiver`.

### 4. 🤖 AI Feng Shui & Astrology Assistant (DeepSeek AI)
- **Integrated AI Chat**: Powered by DeepSeek API with streaming responses.
- **Personalized Advice**: Ask questions regarding auspicious dates, Feng Shui consultation, horoscopes, and event planning according to traditional Eastern principles.

### 5. 🔍 Universal Search & Custom Onboarding
- **Search Engine**: Instantly search for dates, traditional holidays, special events, and tasks.
- **Personalized Onboarding**: Setup birth date to personalize Eastern horoscope calculations stored locally with **Jetpack DataStore**.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (2.3.20)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: Clean Architecture (Domain, Data, UI Layers) + MVVM Pattern + Unidirectional Data Flow (UDF)
- **Dependency Injection**: [Dagger Hilt](https://dagger.dev/hilt/) (2.60)
- **Local Database**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP
- **Data Storage**: [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) + [OkHttp 3](https://square.github.io/okhttp/) + [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Asynchronous**: Kotlin Coroutines & StateFlow
- **Performance**: Baseline Profiles (`androidx.profileinstaller`)
- **Testing**: JUnit 4, Google Truth, MockK, Compose UI Testing

---

## 📂 Project Structure

```text
app/src/main/java/com/example/lichvannien/
├── data/
│   ├── local/
│   │   ├── datastore/       # UserPreferences (DataStore implementation)
│   │   ├── db/              # Room AppDatabase & DAOs (TaskDao, SpecialDayDao)
│   │   └── entity/          # Room Entities (TaskEntity, SpecialDayEntity)
│   ├── mapper/              # Entity <-> Domain Model mappers
│   ├── remote/              # Retrofit APIs & DTOs (DeepSeekApi, DeepSeekDto)
│   └── repository/          # Repository Implementations (TaskRepositoryImpl, etc.)
├── domain/
│   ├── model/               # Pure Domain Entities (SolarDate, LunarDate, Task, DayDetail)
│   ├── repository/          # Repository Interfaces
│   ├── usecase/             # Business Use Cases (GetDayDetailUseCase, etc.)
│   └── util/                # Algorithms (LunarConverter, AuspiciousCalculator, EasternFengShuiHelper)
├── di/                      # Dependency Injection Hilt Modules
└── ui/
    ├── ai/                  # AI Chatbot Screen & ViewModel
    ├── calendar/            # Month Calendar Screen & ViewModel
    ├── detail/              # Day Detail / Feng Shui Screen & ViewModel
    ├── onboarding/          # User Onboarding & Birthday Selection
    ├── search/              # Universal Search Dialog & ViewModel
    ├── task/                # Task Management, Alarms, Reminders, Receivers & Services
    ├── today/               # Daily Overview Dashboard Screen & ViewModel
    ├── navigation/          # Navigation Graph & Destinations
    └── theme/               # Material 3 Theme, Typography, and Color Palette
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 17+
- **Android SDK**: Compile SDK `36`, Minimum SDK `24`

### Installation & Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/tranhienchuong/Calendar.git
   cd Calendar
   ```

2. **Configure API Key (Optional for AI Assistant)**:
   Add your DeepSeek API Key in `local.properties` (this file is gitignored):
   ```properties
   DEEPSEEK_API_KEY=your_deepseek_api_key_here
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Run Code Quality & Lint**:
   ```bash
   ./gradlew lintDebug
   ```

5. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The generated APK will be available at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Testing Strategy

- **Unit Tests**: Full coverage for lunar conversion algorithms, auspicious time computations, Feng Shui helpers, use cases, and ViewModels.
- **Instrumented UI Tests**: Compose UI tests covering calendar interactions, onboarding flow, and navigation.

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
