# Contributing

Thanks for considering contributing to Termux Neon. This is a small,
mostly solo-maintained project — response times to issues and PRs may take a
few days, and that's normal, not a sign your contribution isn't wanted.

## Building and testing

```sh
./gradlew testDebugUnitTest lintDebug :app:assembleDebug :app:assembleRelease
```

Compose instrumentation tests require an attached emulator or device:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Requires JDK 17+. See `README.md` for the full module layout and how to
install a built debug APK next to a debug Termux build.

Debug signing uses Gradle's standard local debug keystore. Do not commit
keystores or passwords; use a local Android debug keystore or a separately
managed signing setup when testing shared-UID compatibility.

## Branch/PR conventions

- Open a PR against `master`.
- Keep PRs focused on one change; unrelated cleanup can be a separate PR.
- CI (`github_action_build.yml`) must pass before merge.
- Fill out the PR template checklist.

Release signing secrets are CI-only — contributors never need (and should
never be asked for) a copy of the release signing key.

## Adding a font or color scheme

This is the most common kind of external contribution, so here's the
checklist specifically for it:

1. Add the font (`.ttf`) to `app/src/main/assets/fonts/`, or the color
   scheme (`.properties`, matching the existing 16-color ANSI format used by
   the other files in `app/src/main/assets/colors/`) to
   `app/src/main/assets/colors/`.
2. Add a row for it to `THIRD_PARTY_LICENSES.md` with its upstream source and
   license. **Fonts or schemes without a confirmed FOSS-compatible license
   cannot be merged** — this project bundles these assets and ships them via
   F-Droid, which requires every bundled asset to be verifiably free.
3. Run the asset integrity test (`./gradlew testDebugUnitTest`) — it will
   fail if the new asset doesn't parse cleanly or is missing required fields.

## Code of Conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md).
