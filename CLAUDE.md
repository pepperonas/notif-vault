# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

NotifVault is an Android app (`io.celox.notifvault`) that permanently and encryptedly archives incoming
messaging notifications — like Samsung's notification history but without the 24h expiry. Its core trick:
WhatsApp sends **no** notification when a message is deleted, so the original notification (already captured
on arrival) survives deletion. Everything is on-device; the app has **no `INTERNET` permission**, no cloud,
no tracking, and backups are disabled (`allowBackup="false"`).

Single Gradle module (`:app`), Kotlin + Jetpack Compose, minSdk 26 / target+compile 35, JDK 17.

## Build & run

```bash
./gradlew assembleDebug        # APK → app/build/outputs/apk/debug/
./gradlew installDebug         # build + install to connected device/emulator
./gradlew lint                 # Android lint
./gradlew test                 # JVM unit tests (none exist yet)
```

There is **no `local.properties`** committed — Android Studio creates it, or copy `local.properties.example`
and set `sdk.dir`. There is **no git repo** here yet.

## Architecture (data flow)

The whole app is one pipeline: a system notification → a stored, encrypted row → Compose UI reads it back.

1. **`service/NotificationCaptureService`** — a `NotificationListenerService` that receives *every* posted
   notification. Filters by `SettingsStore` (capture-all toggle, else monitored package allowlist), hands
   the `StatusBarNotification` to `MessageExtractor`, and inserts the results. Runs work on a private IO
   `CoroutineScope`. On `onListenerConnected` it snapshots the current shade; on disconnect it calls
   `requestRebind` because Samsung One UI aggressively kills listeners. Deletion is **never** acted on —
   the original is already saved.

2. **`notif/MessageExtractor`** — converts one notification into 0..N `CapturedMessage`s. Skips
   `FLAG_GROUP_SUMMARY`. **Prefers `NotificationCompat.MessagingStyle`** (gives per-message sender + real
   timestamp + bundled back-history); falls back to title/`EXTRA_TEXT_LINES`/`EXTRA_TEXT`. Flags
   suspected-deletion text via `deletionMarkers`.

3. **De-duplication is the key invariant.** `CapturedMessage.id` is a SHA-256 of
   `"$pkg|$conversation|$sender|$text|$time"`, and inserts use `OnConflictStrategy.IGNORE`. The same message
   re-delivered inside many successive notifications therefore collapses to exactly one row. Preserve this —
   changing the hash inputs or conflict strategy breaks dedup.

4. **`data/`** — Room (`AppDatabase` v1, single `messages` table) over **SQLCipher**. `DatabaseProvider` is a
   singleton that loads the native `sqlcipher` lib, builds the DB with a 32-byte random passphrase stored in
   `EncryptedSharedPreferences` (AES-256-GCM, Android Keystore). Uses `fallbackToDestructiveMigration()` —
   if you bump the schema version without a migration, **all archived messages are wiped**; add a real
   migration instead. `MessageDao` exposes Flows for conversation summaries, per-chat messages, search, and
   count. `SettingsStore` (DataStore Preferences) holds the monitored-package set, capture-all flag, and
   biometric-lock flag; `KNOWN_MESSENGERS` is the toggle list shown in Settings, `DEFAULT_PACKAGES` is the
   WhatsApp default.

5. **`ui/`** — Compose. `MainActivity` is a **`FragmentActivity`** (required by `BiometricPrompt`); it gates
   the app behind biometric/device-credential unlock when enabled, then a `NavHost` routes
   onboarding → home → chat → settings. Nav args (conversation title, package) are `URLEncoder`-escaped.
   `VaultViewModel` (`AndroidViewModel`) owns the DAO Flows as `StateFlow`s and the search query. Theme in
   `ui/theme/`.

6. **`util/`** — `PermissionUtils` (notification-access check via `enabled_notification_listeners`, battery-
   optimization exemption) and `ExportUtils` (hand-rolled CSV/JSON serialization).

`NotifVaultApp` (Application) warms up `DatabaseProvider` at startup so the listener can write immediately.

## Things to keep in mind

- **Media can't be captured** — photos/voice/video aren't in the notification payload and Scoped Storage
  blocks WhatsApp's media folder. Don't attempt to add media capture; it's a documented hard limitation.
- **Don't add network permissions or dependencies.** The privacy guarantee (offline-only) is a feature.
- UI strings and user-facing text are **German** (`res/values/strings.xml`); match that.
- Release builds currently have `isMinifyEnabled = false`; if enabling R8, SQLCipher/Room may need
  `proguard-rules.pro` keep rules.
