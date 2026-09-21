# Copilot instructions for Termux Neon

## Project overview

This repository is an Android app that customizes Termux styling via a Compose UI. The plugin contract stays compatible with Termux's `com.termux.styling` add-on and the shared `com.termux` user ID, so the app must behave like a drop-in replacement for the upstream styling addon.

The repo is split into modules:

- `app/` — Compose UI, activity entry point, state management, and the user-facing preview/apply flow.
- `core/termux/` — bundled scheme/font catalog, Termux environment detection, and the logic that reads/writes the active style under `~/.termux`.
- `core/theme-engine/` — ANSI palette model and `colors.properties` parsing.
- `core/designsystem/` — Neon theme and shared UI building blocks.

The big-picture flow is: select a scheme/font in the Compose UI -> preview in `MainViewModel` -> write the style to Termux's config files via `TermuxStyleWriter` -> reload live over a package-scoped broadcast.

## Build, test, and lint

Use JDK 17+.

Common commands:

```sh
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Single-test commands:

```sh
./gradlew :app:testDebugUnitTest --tests com.termux.styling.AppAssetsIntegrityTest
./gradlew :core:termux:testDebugUnitTest --tests dev.neonbytecode.neon.termux.StyleCatalogTest
./gradlew :core:theme-engine:testDebugUnitTest --tests dev.neonbytecode.neon.themeengine.TermuxColorsParserTest
```

The project uses Android Gradle Plugin 9.x and Kotlin 2.2.x, with Compose compiler support managed via `gradle/libs.versions.toml`.

## Repository-specific conventions

- Keep compatibility with Termux's styling contract: same package name, same shared user ID, and the same files under `~/.termux`.
- When adding or changing bundled assets, update the asset under the correct directory:
  - `app/src/main/assets/colors/` for scheme files
  - `app/src/main/assets/fonts/` for font files
- Preserve the existing 16-color ANSI schema and normalize scheme naming; `TermuxColorsParser` expects `color0`..`color15` and standard `colors.properties` keys.
- New bundled fonts or schemes must be accompanied by a source/license entry in `THIRD_PARTY_LICENSES.md`. The project intentionally rejects assets without a confirmed FOSS-compatible license.
- App-level integrity checks are enforced by tests such as `AppAssetsIntegrityTest`; if you add a new scheme or font, ensure it parses cleanly and has the expected metadata.
- Release/versioning is intentionally structured in `app/build.gradle.kts`; keep semver-style `versionName` formatting intact if you change the build version logic.
- PRs are expected to be focused, and the repo targets `master` as the merge branch.

## Key architecture details

- `StyleCatalog` is the asset abstraction. It enumerates fonts and scheme files and caches bytes so the UI can preview assets without re-reading the APK every time.
- `TermuxColorsParser` is the canonical parser for scheme files. Most color-related logic should stay in or near the theme-engine module.
- `MainViewModel` owns the UI state, favorites, preview state, and apply operations; avoid pushing business logic into the composable layer.
- Smart recommendations, preset import/export, theme health checks, and
  Android quick actions should remain deterministic and offline-first.
- Imported fonts and schemes are app-private until explicitly applied. Keep
  filename sanitization, validation, and user-visible errors intact.
- `AppliedStyleReader` and `TermuxStyleWriter` are the boundary with the installed Termux environment; changes to file format or parsing should be reflected in both.
- `core/designsystem` is meant for reusable visual primitives and the terminal preview; keep app-specific behavior in `app/` instead of mixing it into the design system.

## Asset and release notes

- `setup-fonts.sh` and `setup-nerd-fonts.sh` regenerate the bundled font assets.
- `fontpatcher-py3.patch` is a repository-specific patch applied by the font setup scripts.
- The README's installation note is important: debug APKs must be installed next to a matching debug-signed Termux build, or Android will reject the install or block writes under `~/.termux`.

## When making changes

- Prefer minimal, module-aware edits; the codebase is intentionally organized around the `app` + `core/*` split.
- Keep style-specific logic close to the relevant module instead of scattering it across the app layer.
- Verify asset and parsing behavior with the project’s unit tests before shipping a change that affects fonts or color schemes.
- When changing behavior, update the README, architecture notes, changelog,
  and relevant issue/PR guidance so beginner and advanced contributors see
  the same contract.
