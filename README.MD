# compose-multiplatform-template

### Android
To run the application on android device/emulator:
- open project in Android Studio and run imported android run configuration

To build the application bundle:
- run `./gradlew app:androidApp:assembleDebug app:androidApp:assembleRelease`
- find `.apk` file in `app/androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- Run android UI tests on the virtual device: `./gradlew managedVirtualDeviceDebugAndroidTest managedVirtualDeviceAndroidDeviceTest -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect`

### Desktop
Run the desktop application: `./gradlew :app:desktopApp:run`  
Run the desktop **hot reload** application: `./gradlew :app:desktopApp:hotRun --auto`
Run desktop UI tests: `./gradlew jvmTest`

### iOS
To run the application on iPhone device/simulator:
- Open `app/iosApp/iosApp.xcodeproj` in Xcode and run standard configuration
- Or use [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) for Android Studio
- Build iOS: `./gradlew ciIos`
- Run iOS simulator UI tests: `./gradlew iosSimulatorArm64Test`

### JS Browser
Run the browser application: `./gradlew :app:webApp:jsBrowserDevelopmentRun`
Run browser UI tests: `./gradlew jsBrowserTest`
Build web distribution: `./gradlew :app:webApp:jsBrowserDistribution`  
Deploy a dir `app/webApp/build/dist/js/productionExecutable` to a web server

### Wasm Browser
Run the browser application: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`  
Run browser UI tests: `./gradlew wasmJsBrowserTest`
Build web distribution: `./gradlew :app:webApp:wasmJsBrowserDistribution`  
Deploy a dir `app/webApp/build/dist/wasmJs/productionExecutable` to a web server

### Code style and linting
- launch ktlintFormat: `./gradlew ktlintFormat`
- launch ktlint, detekt and lint checks: `./gradlew ktlintCheck detekt lint checkModuleBoundaries`

### Screenshot Testing
Record a screenshot: `./gradlew recordRoborazzi`
Verify a screenshot: `./gradlew verifyRoborazzi`

### Code Coverage
Code coverage is measured with [Kotlinx Kover](https://github.com/Kotlin/kotlinx-kover). Each library module merges
its `jvm` and `android` test coverage (the AGP KMP library plugin exposes an `android` variant, not `debug`/`release`)
into a custom `coverage` report variant, which is then aggregated into a single report at the root project.

Generate the aggregated reports (run tests first, or let the report tasks trigger them):
- HTML report: `./gradlew koverHtmlReportCoverage` — open `build/reports/kover/htmlCoverage/index.html`
- XML report: `./gradlew koverXmlReportCoverage` — output at `build/reports/kover/reportCoverage.xml`
- Verify coverage against the rule: `./gradlew koverVerifyCoverage` — fails the build if coverage drops below the threshold
