# Firebase setup

Koog Chat's auth and sync are entirely optional — skip this doc and the app runs fully
local (see `docs/architecture.md`'s `*Fake` modules). This doc **is** `docs/TASKS.md` row
5 — do it before rows 6 (`coreAuthFirebase`) and 7 (`coreSyncFirestore`), since both rows'
manual verification needs a real project to run against, not after.

## 1. Create the Firebase project

1. In the [Firebase console](https://console.firebase.google.com/), create a project.
2. Enable **Firestore** (production mode; lock it down with the rule below before any
   real data goes in it).
3. Enable **Authentication → Sign-in method → Google**.

## 2. Android

1. Add an Android app in the Firebase console with package name `koog.chat.app` and the
   debug/release SHA-1 fingerprint(s).
2. Download `google-services.json` into `app/androidApp/`.
3. The `com.google.gms.google-services` Gradle plugin should be applied conditionally —
   only if that file exists — so a clone without it still builds and runs `coreAuthFake`/
   `coreSyncFake` ("app works offline when Firebase unconfigured", `AGENTS.md` constraint
   #11). This conditional-apply logic is this row's own implementation work.

## 3. iOS

1. Add an iOS app in the Firebase console with bundle ID `koog.chat.app`.
2. Download `GoogleService-Info.plist` into `app/iosApp/iosApp/`.
3. Add the **reversed client ID** (from that plist) as a URL scheme under iosApp's
   Info.plist / URL Types, required for the Google sign-in redirect to complete.

## 4. Desktop (JVM) and Web (JS/WasmJs)

Neither platform auto-reads a config file the way Android/iOS do. Both KMPAuth and
GitLive's SDK need to be initialized manually with the same project's values, read from
`local.properties` (already `.gitignore`d in this repo) and surfaced to the app via a
generated `BuildConfig`-style object. Building that reader is part of this row
(`docs/TASKS.md` row 5) — it has no other owner:

```properties
# local.properties — not committed
FIREBASE_API_KEY=...
FIREBASE_PROJECT_ID=...
FIREBASE_APPLICATION_ID=...
FIREBASE_STORAGE_BUCKET=...
GOOGLE_WEB_CLIENT_ID=...
```

`GOOGLE_WEB_CLIENT_ID` is the **Web** OAuth client ID from Firebase console → Project
Settings → General → Your apps (or Google Cloud Console → Credentials) — this is also
the `serverId` KMPAuth's `GoogleAuthCredentials` needs on every platform, including
Android/iOS, not just desktop/web.

## 5. Firestore security rules

Every document lives under `users/{uid}/...` (see `docs/architecture.md`) — restrict
read/write to the owning user:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

## 6. Known risks to verify, not assume

- **WasmJs is very new** in `firebase-kotlin-sdk 3.0.0-alpha02` (added in that exact
  release) — test sign-in and Firestore sync on this target early, don't leave it for
  last.
- **JVM desktop session persistence** for `Firebase.auth.signInWithCredential` is
  unverified against this repo's setup — see the risk noted in `docs/DECISIONS.md` and
  `docs/TASKS.md` row 6.
- The `kotlinx-coroutines` version pin conflict (`docs/DECISIONS.md`) surfaces here too:
  if the `wasmJs` build fails to resolve once these dependencies are added, this is the
  first place to look.
