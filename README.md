# Chowdhury Mills (Android)

Android client for the [Chowdhury Mills](https://github.com/ovi-salman/chowdhury-mills) mustard-oil ledger.

The phone talks to the same Next.js API the web app uses (`/api/login`, `/api/me`, posting, directory, ledger, balances, and the rest). Configure the server base URL on the sign-in screen (**Server address**). Emulator default: `http://10.0.2.2:3210`.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- OkHttp + kotlinx.serialization
- DataStore for base URL and `mill_session` cookie

## Build

```bash
./gradlew :app:assembleDebug
```

Install the debug APK from `app/build/outputs/apk/debug/`.

Open the web ledger first (see the web repo README), then point this app at that host.
