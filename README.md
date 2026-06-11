# Barbell Tracker

**Repository:** https://github.com/chrisOTM/barbell-tracker-android
**Autor:** ChrisOTM ([@chrisOTM](https://github.com/chrisOTM))

Native Android app for structured barbell training: plans, guided workout execution with
rest timer, set logging, weight progression, and a training diary. Fully offline — no
login, cloud, ads, or tracking.

Built from `spec/Barbell-Tracker-requirements.md`.

## Stack
Kotlin · Jetpack Compose (Material 3) · Navigation-Compose · Room (KSP) · Hilt · MVVM + Repository.
`minSdk 26`, `compileSdk 36`, `targetSdk 36`.

## Architecture
```
ui (Compose screens + ViewModels)
  → data/repo (interfaces + impls)   ← swappable for a future remote source
    → data/dao (Room DAOs)
      → data/db (AppDatabase, seeder)
domain  (pure logic: RestDefaults, ProgressionCalculator, WorkoutEngine — unit-tested)
```
Layers wired with Hilt. No business logic in composables. Library + plan templates seeded on
first launch.

## Build & run
The Gradle daemon must run on **JDK 17–21** (newer JDKs are not supported by AGP 8.7.3).
Point Gradle at a suitable JDK in one of these ways — none is committed:

```bash
export JAVA_HOME=/path/to/jdk-21   # e.g. the Android Studio JBR
# or, machine-local and never committed:
echo "org.gradle.java.home=/path/to/jdk-21" >> ~/.gradle/gradle.properties
```

Then:

```bash
./gradlew :app:assembleDebug      # build APK (offline-capable once deps are cached)
./gradlew test                    # domain unit tests
./gradlew :app:installDebug       # install on a running emulator/device
```

The Android SDK location is read from `local.properties` (`sdk.dir=…`), which is gitignored —
create it locally or let Android Studio generate it.

### SDK note (Android 16 / API 36)
AGP 8.7.3 resolves `compileSdk = 36` to an SDK platform whose hash is `android-36`. If your
SDK only ships the minor-versioned `android-36.1` platform, create a non-destructive alias
directory `platforms/android-36` (symlinks to `android-36.1` plus a `package.xml`/`source.properties`
declaring `api-level 36`), or bump AGP to ≥ 8.9 which understands minor SDK versions.

## Feature → requirement map
See `spec/Barbell-Tracker-requirements.md`. All MUST and SHOULD user stories are implemented;
the foreground-service rest timer (optional in the spec) is deferred — the timer runs in-app
with the screen kept on.

## Autor
**ChrisOTM** — [github.com/chrisOTM](https://github.com/chrisOTM)

Repository: https://github.com/chrisOTM/barbell-tracker-android
