# Firebase setup

Koog Chat's auth and sync are entirely optional — skip this doc by setting
`IS_FAKE_DATA_ENABLED=true` (in `local.properties`, or `-DIS_FAKE_DATA_ENABLED=true` on
the command line) to build the Fake variant, which runs fully local (see
`docs/architecture.md`'s `*Fake` modules). This doc **is** `docs/TASKS.md` row 5 — do it
before rows 6 (`coreAuthFirebase`) and 7 (`coreSyncFirebase`), since both rows' manual
verification needs a real project to run against, not after.

## 1. Create the Firebase project

1. In the [Firebase console](https://console.firebase.google.com/), create a project.
2. Enable **Firestore** (production mode; lock it down with the rule below before any
   real data goes in it).
3. Enable **Authentication → Sign-in method → Google**.

## 2. Android

1. Add an Android app in the Firebase console with package name `koog.chat.app` and the
   debug/release SHA-1 fingerprint(s).
2. Download `google-services.json` into `app/androidApp/`.
3. The `com.google.gms.google-services` Gradle plugin should still be applied
   conditionally — either the same `isFakeDataEnabled()` check `diApp` uses, or the
   presence of that file — so a clone without a Firebase project configured still builds.
   Note this no longer decides which auth/sync modules `diApp` binds — that's the
   `IS_FAKE_DATA_ENABLED` flag alone (see `docs/architecture.md`); this plugin apply just
   avoids failing the Android build when there's no `google-services.json` to read. This
   conditional-apply logic is this row's own implementation work.

## 3. iOS

1. Add an iOS app in the Firebase console with bundle ID `koog.chat.app`.
2. Download `GoogleService-Info.plist` into `app/iosApp/iosApp/`.
3. Add the **reversed client ID** (from that plist) as a URL scheme under iosApp's
   Info.plist / URL Types, required for the Google sign-in redirect to complete. Per
   KMPAuth's Google guide, also add `GIDClientID` (iOS client ID) and `GIDServerClientID`
   (the Web client ID, same as `GOOGLE_WEB_CLIENT_ID` below) to Info.plist. The
   `onOpenURL` → `GIDSignIn.sharedInstance.handle(url)` forwarding in `iosApp.swift` is
   already done. Without these Info.plist keys, tapping Google sign-in on iOS fails at runtime.

The native SDK side is already in the Xcode project (no CocoaPods — KMPAuth 3.x dropped
it): Kotlin's generated `app/iosApp/KotlinMultiplatformLinkedPackage` local package, which
pulls in `GoogleSignIn-iOS` transitively. Don't add `GoogleSignIn` to the app target
directly: Xcode then splits it into its own framework, and launch crashes with
`dyld: Symbol not found: _OBJC_CLASS_$_GIDSignIn` because `ComposeApp` expects it inside
`KotlinMultiplatformLinkedPackageDylib`. `import GoogleSignIn` in Swift still resolves
through the linkage package.
`kmpauth-google` declares GoogleSignIn through Kotlin's `swiftPMDependencies {}`, and
`embedAndSignAppleFrameworkForXcode` fails with "SwiftPM linkage package not integrated
into Xcode project" until that linkage package is wired in. Whenever a SwiftPM-backed
dependency is added or bumped (e.g. a KMPAuth version change, or GitLive's Firebase
modules in rows 6/7), regenerate it with the Firebase variant active and commit the result:

```
XCODEPROJ_PATH="$PWD/app/iosApp/iosApp.xcodeproj" ./gradlew -DIS_FAKE_DATA_ENABLED=false :diApp:integrateLinkagePackage
```

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
IS_FAKE_DATA_ENABLED=false
```

`IS_FAKE_DATA_ENABLED=false` (or the key omitted entirely) selects the Firebase variant
described by the rest of this doc; set it to `true` — or pass
`-DIS_FAKE_DATA_ENABLED=true` on the command line, which always wins over the
`local.properties` value — to build the Fake variant instead and skip all of the above.

`GOOGLE_WEB_CLIENT_ID` is the **Web** OAuth client ID from Firebase console → Project
Settings → General → Your apps (or Google Cloud Console → Credentials) — this is also
the `serverId` KMPAuth's `GoogleAuthCredentials` needs on every platform, including
Android/iOS, not just desktop/web. `core/coreAuthFirebase`'s `generateGoogleAuthConfig` task
already reads it into a generated `GoogleAuthConfig.WEB_CLIENT_ID` (rebuild after changing it).
If it's missing, the app still runs; only the Google sign-in launcher fails. On the same Web
OAuth client, also:

- add `http://localhost:8080/callback` to **Authorized redirect URIs** (desktop: KMPAuth opens
  the browser and catches the redirect on that local port, so it must be free), and
- add each web origin you serve the app from (e.g. the `jsBrowserDevelopmentRun` dev server
  URL) to **Authorized JavaScript origins** (JS/WasmJs use Google One Tap/FedCM).

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
