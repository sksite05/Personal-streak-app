# StreakSip

A personal habit-tracking Android app built with Kotlin and Jetpack Compose. StreakSip helps you build momentum with daily reading, hydration, and goal-based streaks — all in one simple dashboard.

<p align="center">
  <img src="assets/streaksip-screenshot.svg" alt="StreakSip app dashboard" width="420" />
</p>

## Why this app?

This project was created as a lightweight personal productivity tool to track the habits that matter most:

- Daily reading and focus streaks
- Water intake reminders
- XP progression and level tracking
- Habit momentum and consistency
- Clean mobile-first UI designed for daily check-ins

## Features

- Habit dashboard inspired by streak-based productivity apps
- Reading focus tracker with daily completion state
- Hydration tracking with progress toward a target
- XP and beginner-to-next-level progression
- Minimal, modern UI using Compose and Material 3
- Easy to extend with more habits over time

## Tech stack

- Kotlin
- Android Jetpack Compose
- Material 3
- Room (for persistence)
- Retrofit + Moshi (for API integration if needed)
- Firebase integration support in project config

## Project structure

```text
Personal-streak-app/
├── app/
│   ├── src/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .env.example
├── metadata.json
├── gradlew
├── gradlew.bat
└── README.md
```

## Getting started

### Prerequisites

- Android Studio
- JDK 17+
- Android SDK configured for the project

### Run the app

1. Clone the repository
2. Open the project in Android Studio
3. Let Gradle sync
4. Copy `.env.example` to `.env` if you want to use the secrets setup expected by the app
5. Run the `app` configuration on an emulator or physical device

```bash
git clone https://github.com/sksite05/Personal-streak-app.git
cd Personal-streak-app
# Open in Android Studio and run the app
```

## Notes

This app is intentionally personal and lightweight, designed for daily habit management rather than broad feature complexity. It is a good starting point for adding more streak categories, analytics, reminders, or cloud sync in the future.

## License

This project does not currently declare a license. If you plan to share or distribute it publicly, consider adding an appropriate open-source license.
