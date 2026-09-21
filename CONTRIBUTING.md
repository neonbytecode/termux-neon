# Contributing

Thanks for considering a contribution to Termux Neon. This project is intentionally small, modular, and focused — the goal is to keep the Termux plugin contract stable while improving the user experience and maintainability of the app.

Contributions are welcome, but we do expect changes to stay narrow and aligned with the project’s architecture and distribution model.

## Local development setup

Requirements:

- JDK 17+
- Android SDK configured for the repo
- a working local Android debug keystore for shared-UID compatibility testing

## Build and test

```sh
./gradlew testDebugUnitTest lintDebug :app:assembleDebug :app:assembleRelease
```

For Compose UI instrumentation tests on a device or emulator:

```sh
./gradlew :app:connectedDebugAndroidTest
```

The core validation flow is the same in CI and locally. If you change a bundled asset, parser behavior, or Termux write path, run the relevant tests before submitting a PR.

## Pull request conventions

- Open PRs against `master`.
- Keep each PR focused on one change or one concern.
- Prefer small, reviewable diffs.
- Do not include unrelated cleanup in the same patch.
- Ensure the project still builds and tests successfully before requesting review.

## Asset contribution checklist

This project has a strict policy for bundled assets because it ships via F-Droid and must stay legally clean.

When adding a font or color scheme:

1. Place the asset in the correct directory:
   - fonts: `app/src/main/assets/fonts/`
   - schemes: `app/src/main/assets/colors/`
2. Ensure the scheme matches the expected 16-color ANSI `colors.properties` format.
3. Add a source/license entry to `THIRD_PARTY_LICENSES.md`.
4. Validate that the asset parses cleanly and passes the integrity tests.

Important: fonts and schemes without a confirmed FOSS-compatible license are not accepted.

## Shared-UID and signing notes

This project is not a standalone Android app in the usual sense — it is a Termux plugin that depends on the legacy shared-UID contract. That means:

- the package name must remain `com.termux.styling`
- the app must be signed with the same key as the installed Termux build
- the project must keep writing to the same files under `~/.termux`

Do not commit signing keys, keystore files, or credentials. Use local testing credentials only.

## Feature placement guide

- UI interactions and screen state belong in `app`.
- Termux package access, file writes, and broadcasts belong in
  `core/termux`.
- Palette parsing and color semantics belong in `core/theme-engine`.
- Reusable Compose visuals belong in `core/designsystem`.

New smart features should remain offline-first and deterministic. Imported
fonts, schemes, and preset bundles must be validated before they are stored,
and user-visible failures should explain what action can fix the problem.

## Code of conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md).
