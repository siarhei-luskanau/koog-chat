## What / why

<!-- One or two sentences. Link docs/TASKS.md or docs/DECISIONS.md entries if relevant. -->

## Verification

Check off the layers this change actually needed (see `docs/quality-gates.md`); leave
the rest unchecked with a one-line reason (e.g. "docs-only change").

- [ ] **Layer 1 — static analysis**: `./gradlew ktlintFormat ktlintCheck detekt lint checkModuleBoundaries`
- [ ] **Layer 2 — tests**: touched targets' test tasks pass (`jvmTest`, `testAndroidHostTest`,
      `jsBrowserTest`, `wasmJsBrowserTest`, `iosSimulatorArm64Test`, managed-device tests —
      only the ones relevant to the change)
- [ ] **Layer 2b — coverage**: `./gradlew koverVerifyCoverage` (only if new/changed
      production code)
- [ ] **Layer 3 — app actually runs**: launched on at least one affected target
      (`./gradlew :app:desktopApp:run` is the cheapest check) and/or
      `./gradlew verifyRoborazzi` if a `@Preview` composable changed

## CI evidence

<!-- Link the CI run, and any failed-gate artifact if a layer above needed local
     re-verification: reports-<task>-<os>, coverage-report, roborazzi-diff-<module>-<os>.
     See "Where to look when a gate fails" in docs/quality-gates.md. -->
