<p align="center">
  <img src="docs/banner.png" alt="NotifVault – messages never die" width="100%">
</p>

# NotifVault

<!-- Project status -->
<p align="center">
  <a href="https://github.com/pepperonas/notif-vault/releases/latest"><img alt="Release" src="https://img.shields.io/github/v/release/pepperonas/notif-vault?logo=github&color=14B8A6"></a>
  <a href="https://github.com/pepperonas/notif-vault/releases/latest"><img alt="Release date" src="https://img.shields.io/github/release-date/pepperonas/notif-vault?color=14B8A6"></a>
  <a href="https://github.com/pepperonas/notif-vault/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/pepperonas/notif-vault/total?logo=github&label=downloads&color=0D9488"></a>
  <a href="https://github.com/pepperonas/notif-vault/releases/latest"><img alt="Latest downloads" src="https://img.shields.io/github/downloads/pepperonas/notif-vault/latest/total?label=latest%20downloads&color=0D9488"></a>
  <a href="https://github.com/pepperonas/notif-vault/actions/workflows/release.yml"><img alt="Build" src="https://img.shields.io/github/actions/workflow/status/pepperonas/notif-vault/release.yml?logo=githubactions&logoColor=white&label=release%20build"></a>
</p>
<p align="center">
  <a href="https://github.com/pepperonas/notif-vault/commits/main"><img alt="Last commit" src="https://img.shields.io/github/last-commit/pepperonas/notif-vault?logo=git&logoColor=white&color=F59E0B"></a>
  <a href="https://github.com/pepperonas/notif-vault/commits/main"><img alt="Commit activity" src="https://img.shields.io/github/commit-activity/m/pepperonas/notif-vault?color=F59E0B"></a>
  <a href="https://github.com/pepperonas/notif-vault/issues"><img alt="Issues" src="https://img.shields.io/github/issues/pepperonas/notif-vault?logo=github"></a>
  <img alt="Code size" src="https://img.shields.io/github/languages/code-size/pepperonas/notif-vault?color=0F172A">
  <img alt="Repo size" src="https://img.shields.io/github/repo-size/pepperonas/notif-vault?color=0F172A">
  <img alt="Top language" src="https://img.shields.io/github/languages/top/pepperonas/notif-vault?logo=kotlin&logoColor=white&color=7F52FF">
</p>

<!-- Tech stack -->
<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="minSdk" src="https://img.shields.io/badge/minSdk-26%20(Android%208.0)-3DDC84?logo=android&logoColor=white">
  <img alt="targetSdk" src="https://img.shields.io/badge/targetSdk-35-3DDC84?logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10-4285F4?logo=jetpackcompose&logoColor=white">
  <img alt="Material 3" src="https://img.shields.io/badge/Material%203-Expressive-757575?logo=materialdesign&logoColor=white">
</p>
<p align="center">
  <img alt="Gradle" src="https://img.shields.io/badge/Gradle-8.11.1-02303A?logo=gradle&logoColor=white">
  <img alt="AGP" src="https://img.shields.io/badge/AGP-8.7.2-3DDC84?logo=android&logoColor=white">
  <img alt="KSP" src="https://img.shields.io/badge/KSP-2.0.21--1.0.28-7F52FF?logo=kotlin&logoColor=white">
  <img alt="JDK" src="https://img.shields.io/badge/JDK-17-007396?logo=openjdk&logoColor=white">
  <img alt="Coroutines" src="https://img.shields.io/badge/Coroutines-1.9.0-7F52FF?logo=kotlin&logoColor=white">
</p>

<!-- Data & privacy -->
<p align="center">
  <img alt="Room" src="https://img.shields.io/badge/Room-2.6.1-FF6F00?logo=sqlite&logoColor=white">
  <img alt="SQLCipher" src="https://img.shields.io/badge/SQLCipher-4.6.1-1BA1E2">
  <img alt="Encryption" src="https://img.shields.io/badge/encryption-AES--256-success?logo=letsencrypt&logoColor=white">
  <img alt="Biometric lock" src="https://img.shields.io/badge/lock-Biometric-blueviolet">
  <img alt="On-device" src="https://img.shields.io/badge/data-100%25%20on--device-14B8A6">
  <img alt="No network" src="https://img.shields.io/badge/network-none-critical">
  <img alt="License" src="https://img.shields.io/badge/license-Proprietary-lightgrey">
  <img alt="Made by celox.io" src="https://img.shields.io/badge/made%20by-celox.io-0D9488">
</p>

Speichert eingehende Nachrichten-Benachrichtigungen **dauerhaft und verschlüsselt** – wie
der Samsung-Benachrichtigungsverlauf, aber ohne 24-Stunden-Verfall. Gelöschte WhatsApp-
Nachrichten bleiben so lesbar, weil die ursprüngliche Benachrichtigung in eine lokale,
verschlüsselte Datenbank geschrieben wird, sobald sie ankommt.

## Download

Die fertige, signierte APK gibt es unter **[Releases](https://github.com/pepperonas/notif-vault/releases/latest)**.
APK herunterladen → auf dem Gerät öffnen → Installation aus unbekannter Quelle erlauben.

## Wie es funktioniert

Android liefert jede Benachrichtigung an einen `NotificationListenerService`
(`service/NotificationCaptureService.kt`). WhatsApp sendet beim Löschen einer Nachricht
**keine** zweite Benachrichtigung – die Originalnachricht ist also längst angekommen.
NotifVault parst sie sofort (bevorzugt über `MessagingStyle`, das Absender + echten
Zeitstempel jeder Einzelnachricht enthält) und speichert sie ab. Mehrfach gelieferte
Nachrichten werden über einen Inhalts-Hash dedupliziert.

## Was geht – und was nicht

| Funktion | Status |
|---|---|
| Text-Nachrichten (1:1 & Gruppen) | ✅ zuverlässig |
| Absender + echter Zeitstempel | ✅ via MessagingStyle |
| Volltextsuche, Export (CSV/JSON) | ✅ |
| Verschlüsselung (SQLCipher/AES-256), Biometrie-Sperre | ✅ |
| **Medien** (Fotos, Sprach-/Videonachrichten) | ❌ technisch nicht möglich – stecken nicht in der Notification, Scoped Storage sperrt WhatsApps Medienordner |
| Stummgeschaltete Chats | ❌ erzeugen oft keine Benachrichtigung |
| Nachrichten empfangen, während der Chat offen ist | ❌ keine Benachrichtigung |

## Bauen

1. **Android Studio** (Ladybug/2024.2+) → *Open* → diesen Ordner wählen. Gradle-Sync
   lädt alle Abhängigkeiten (AGP 8.7, Kotlin 2.0, Compose, Room, SQLCipher).
2. Gerät/Emulator anschließen → *Run ▶*.

Oder per Terminal: `./gradlew assembleDebug` → APK unter `app/build/outputs/apk/debug/`.
(Eine `local.properties` mit `sdk.dir=...` wird von Android Studio automatisch angelegt.)

## Einrichtung auf dem Samsung S24 Ultra (wichtig)

One UI killt Hintergrunddienste sehr aggressiv. Damit kein Mitschnitt verloren geht:

1. **Benachrichtigungszugriff erteilen** – beim ersten Start, oder
   *Einstellungen → Apps → Spezieller Zugriff → Benachrichtigungszugriff → NotifVault*.
2. **Akku-Optimierung ausnehmen** – im Onboarding-Schritt, oder
   *Einstellungen → Akku → Hintergrundnutzungslimits → „Nie in den Standby" → NotifVault hinzufügen*.
3. Optional: in *Einstellungen → Apps → NotifVault* die Option „Im Hintergrund aktiv lassen".

## Datenschutz / DSGVO

- Keine Netzwerkberechtigung, keine Cloud, kein Tracking. Alles bleibt auf dem Gerät.
- Datenbank verschlüsselt (SQLCipher, 256-bit). Schlüssel in `EncryptedSharedPreferences`
  (AES-256-GCM, Android Keystore). Backups (Cloud/Geräte­transfer) sind deaktiviert.
- Erfasst werden nur Benachrichtigungen, die **auf diesem Gerät** eingehen – also
  Nachrichten *an dich*. Beachte: gespeicherte Inhalte stammen von Dritten; deren
  Weiterverarbeitung/Weitergabe liegt in deiner Verantwortung als Betreiber.

## Projektstruktur

```
data/    CapturedMessage (Entity), MessageDao, AppDatabase,
         DatabaseProvider (SQLCipher), SettingsStore (DataStore)
notif/   MessageExtractor – Notification → CapturedMessage(s)
service/ NotificationCaptureService – der Listener
ui/      Compose-Screens (Onboarding, Home, Conversation, Settings) + ViewModel
util/    PermissionUtils, ExportUtils
```

## Release erstellen (Maintainer)

Releases werden signiert und automatisch von GitHub Actions gebaut
(`.github/workflows/release.yml`). Der Signing-Keystore liegt **nur** im privaten Repo
`pepperonas/keystore` und in den Repo-Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`) – nie in diesem Repo.

```bash
# versionCode (+1) und versionName in app/build.gradle.kts erhöhen, dann:
git tag v1.2.3 && git push origin v1.2.3
```

Der Workflow baut die signierte APK und hängt sie an einen neuen GitHub Release. Alle
Releases sind mit demselben Keystore signiert und damit als Update übereinander
installierbar.

---
© 2026 Martin Pfeffer | celox.io
