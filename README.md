# IMCI Decision Support

An offline Android app that guides community health workers (CHWs) through the WHO
Integrated Management of Childhood Illness (IMCI) assessment. It covers children from
2 months to 5 years, gives colour-coded classifications and treatment recommendations,
and stores assessments on the device.

Final Year Project, Department of Computing and Informatics, University of Zambia (2026).
Student: Robert N Chishangila. Supervisor: Mr. Mofya Phiri.

> This app supports, but does not replace, clinical judgment. CHWs should always follow
> their IMCI training and supervision.

## Features

- Step-by-step assessment in IMCI order: danger signs, cough or difficult breathing,
  diarrhoea, fever (with measles), ear problem, malnutrition and anaemia
- Rule-based decision engine with one classifier per illness category
- Integrated result: every classification that applies, most urgent first, coloured like the
  WHO Chart Booklet (pink = urgent referral, yellow = treat, green = home care)
- Offline storage in a local SQLite (Room) database, with history and detail views
- App-level PIN lock; patient data is excluded from Android cloud backup and device transfer
- In-app user guide

## Build and run

Requirements: Android Studio, JDK 11+, Android SDK 36. Minimum device: Android 7.0 (API 24).

```bash
./gradlew assembleDebug          # build the APK
./gradlew testDebugUnitTest      # decision-engine unit tests (JVM, no emulator)
./gradlew connectedAndroidTest   # database tests (needs an emulator or device)
```

## Documentation

- [Technical documentation](docs/TECHNICAL.md): architecture, database schema, decision
  rules, testing and development log
- [User guide](docs/USER_GUIDE.md): printable guide for CHWs
