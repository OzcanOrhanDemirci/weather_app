# Hava

A weather application for Android, built with Kotlin and Jetpack Compose.

Weather data comes from [Open-Meteo](https://open-meteo.com), which requires no
API key. The application has no backend of its own: every piece of state lives
on the device.

## Requirements

- JDK 17 or newer
- Android SDK Platform 36
- An Android 8.0 (API 26) device or emulator

## Build

```bash
./gradlew :app:assembleDebug
```

## Technology

| Concern | Choice |
| --- | --- |
| Language | Kotlin 2.4.20 |
| UI | Jetpack Compose, Material 3 |
| Build | Gradle 9.8.0, Android Gradle Plugin 9.4.1 |
| Minimum SDK | 26 |
| Compile SDK | 36 |

## License

MIT. See [LICENSE](LICENSE).
