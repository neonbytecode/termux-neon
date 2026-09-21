# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Termux Neon is a from-scratch Jetpack Compose rewrite of Termux's official
`termux-styling` add-on. It is **not a standalone app** — it's an Android
plugin that shares `sharedUserId="com.termux"` with Termux itself (same
`com.termux.styling` package name, kept intentionally, since Termux launches
this exact component by class name from its long-press context menu). This
shared UID is what lets the app read/write Termux's private
`~/.termux/colors.properties` and `~/.termux/font.ttf` — but only when both
APKs are signed with the same key. This constraint shapes almost everything
non-obvious in the codebase: why there's no custom release keystore in the
repo, why distribution is F-Droid/IzzyOnDroid rather than the Play Store, and
why `TermuxEnvironment.AccessProblem` distinguishes "not installed" from
"wrong signature" from other failures.

## Commands

Requires JDK 17+. AGP 9.x ships Kotlin built in; the Compose compiler
version is pinned in `gradle/libs.versions.toml`.

```sh
# Full local verification (what CI and CONTRIBUTING.md both use)
./gradlew testDebugUnitTest lintDebug :app:assembleDebug :app:assembleRelease

# Compose instrumentation tests (need an attached emulator/device)
./gradlew :app:connectedDebugAndroidTest

# Single unit test
./gradlew :app:testDebugUnitTest --tests com.termux.styling.AppAssetsIntegrityTest
./gradlew :core:termux:testDebugUnitTest --tests dev.neonbytecode.neon.termux.StyleCatalogTest
./gradlew :core:theme-engine:testDebugUnitTest --tests dev.neonbytecode.neon.themeengine.TermuxColorsParserTest

# Install a debug build (must run alongside a matching debug-signed Termux — see README's Installation section)
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

`:app:assembleRelease` produces an **unsigned** release APK — this repo
deliberately contains no release signing key. Production signing belongs to
F-Droid's own reproducible build (see `distribution/fdroid/`), not this repo
or its CI.

`setup-fonts.sh` / `setup-nerd-fonts.sh` regenerate the bundled font assets
in `app/src/main/assets/fonts/`; `fontpatcher-py3.patch` patches the
Nerd Fonts powerline patcher for Python 3 and is applied by `setup-fonts.sh`.

## Architecture

Four Gradle modules, deliberately layered so Termux-specific and
Android-plugin-contract logic stays out of the UI and theme-parsing code:

- **`:app`** (`com.termux.styling`) — `TermuxStyleActivity` (the plugin's
  entry point, launched by Termux's context menu), the Compose UI
  (`NeonApp.kt`), and `MainViewModel` (owns all UI state: selection,
  favorites, preview, busy/message state, apply operations). Business logic
  belongs in the ViewModel, not the composables.
- **`:core:termux`** (`dev.neonbytecode.neon.termux`) — the boundary with
  the installed Termux environment. `TermuxEnvironment` probes
  accessibility and holds the plugin-contract constants (package name,
  reload broadcast action/extra). `StyleCatalog` enumerates and caches
  bundled scheme/font asset bytes. `AppliedStyleReader` reads what's
  currently installed. `TermuxStyleWriter` writes scheme/font/foreground
  changes atomically (via `AtomicFile`) and sends the reload broadcast.
- **`:core:theme-engine`** (`dev.neonbytecode.neon.themeengine`) —
  Termux-agnostic: the `AnsiPalette` model and `TermuxColorsParser` for
  `colors.properties` (expects `color0`..`color15` plus `foreground`/
  `background`/`cursor`). Most color-parsing logic should live here, not
  in `:core:termux`.
- **`:core:designsystem`** (`dev.neonbytecode.neon.designsystem`) — the
  neon visual theme and reusable Compose building blocks
  (`NeonTheme`, `NeonCard`, `NeonButton`, `TerminalPreview`, etc.). Keep
  app-specific behavior out of this module.

**Apply flow**: user picks a scheme/font/text-color in the Compose UI →
`MainViewModel.applyAll()` → `TermuxStyleWriter.applyAll()` writes both
`colors.properties` and `font.ttf` as one transaction (snapshots both files
first; if either write fails, both are rolled back byte-for-byte) → sends
the `com.termux.app.reload_style` broadcast.

**Why hot-reload needs a delayed re-send**: Termux only registers its
reload-broadcast receiver while its own activity is started
(`onStart`/`onStop`), so a broadcast sent while this plugin's UI fully
covers Termux lands on a receiver that isn't registered yet. See
`TermuxStyleActivity.returnToTermux()` — it explicitly brings Termux's task
to front (`FLAG_ACTIVITY_REORDER_TO_FRONT`, not just `finish()`, since a
plain back-stack pop doesn't reliably land on Termux depending on how the
activity was launched) and re-sends the reload broadcast on a short delay
after. The reasoning is written up in detail in
`distribution/blog/shared-uid-hot-reload.md`.

## Repository-specific conventions

- Keep the plugin contract exact: package name, shared user ID, and the
  `~/.termux` file names/formats Termux itself expects. This is not
  refactorable without breaking installs for every existing user.
- New bundled fonts (`app/src/main/assets/fonts/`) or color schemes
  (`app/src/main/assets/colors/`, 16-color ANSI `colors.properties` format)
  **must** get a license/source row in `THIRD_PARTY_LICENSES.md` in the same
  PR. Assets without a confirmed FOSS-compatible license are rejected —
  this project ships via F-Droid, which requires every bundled asset to be
  verifiably free. `AppAssetsIntegrityTest` enforces that every shipped
  scheme/font parses cleanly.
- `versionName` in `app/build.gradle.kts` must stay valid semver
  (`major.minor.patch(-pre)(+build)`); CI injects it via the
  `TERMUX_STYLING_APP_BUILD__APP_VERSION_NAME` env var and validates it
  against the same regex used in the release workflow.
