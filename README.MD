# Koog Chat

### Android
To run the application on android device/emulator:
- open project in Android Studio and run imported android run configuration

To build the application bundle:
- run `./gradlew app:androidApp:assembleDebug app:androidApp:assembleRelease`
- find `.apk` file in `app/androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- Run android UI tests on the virtual device: `./gradlew managedVirtualDeviceDebugAndroidTest managedVirtualDeviceAndroidDeviceTest -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect -DIS_DATA_STUB_ENABLED=true`

### Desktop
Run the desktop application: `./gradlew :app:desktopApp:run`  
Run the desktop **hot reload** application: `./gradlew :app:desktopApp:hotRun --auto`
Run desktop UI tests: `./gradlew jvmTest -DIS_DATA_STUB_ENABLED=true`

### iOS
To run the application on iPhone device/simulator:
- Open `iosApp/iosApp.xcproject` in Xcode and run standard configuration
- Or use [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) for Android Studio
- Build iOS: `./gradlew ciIos`
- Run iOS simulator UI tests: `./gradlew iosSimulatorArm64Test -DIS_DATA_STUB_ENABLED=true`

### JS Browser
Run the browser application: `./gradlew :app:webApp:jsBrowserDevelopmentRun`
Run browser UI tests: `./gradlew jsBrowserTest -DIS_DATA_STUB_ENABLED=true`
Build web distribution: `./gradlew :app:webApp:jsBrowserDistribution`  
Deploy a dir `app/webApp/build/dist/js/productionExecutable` to a web server

### Wasm Browser
Run the browser application: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`  
Run browser UI tests: `./gradlew wasmJsBrowserTest -DIS_DATA_STUB_ENABLED=true`
Build web distribution: `./gradlew :app:webApp:wasmJsBrowserDistribution`  
Deploy a dir `app/webApp/build/dist/wasmJs/productionExecutable` to a web server

### Code style and linting
- launch ktlintFormat: `./gradlew ktlintFormat`
- launch ktlint, detekt and lint checks: `./gradlew ktlintCheck detekt lint`

### Screenshot Testing
Record a screenshot: `./gradlew recordRoborazzi -DIS_DATA_STUB_ENABLED=true`
