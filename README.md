<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&height=180&color=0:0a0e18,100:161b29&text=Termux%20Neon&fontColor=4de8f0&fontSize=46&animation=fadeIn&fontAlignY=40&desc=Terminal%20theming%2C%20live%20preview%2C%20one%20tap%20apply&descAlignY=62&descColor=8b93a8&descSize=15" width="100%"/>

<img src="https://readme-typing-svg.demolab.com/?font=Fira+Code&size=16&pause=1400&color=4DE8F0&center=true&vCenter=true&width=560&lines=15+curated+color+schemes;Live+preview+%2B+hot-reload;Apply+All+%E2%80%94+one+tap%2C+done." alt="Typing SVG" />

[![Build status](https://img.shields.io/github/actions/workflow/status/neonbytecode/termux-neon/github_action_build.yml?style=for-the-badge&logo=githubactions&logoColor=black&label=build&color=4de8f0)](https://github.com/neonbytecode/termux-neon/actions/workflows/github_action_build.yml)
[![License: GPLv3](https://img.shields.io/badge/license-GPLv3-ff4de0?style=for-the-badge)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/neonbytecode/termux-neon?style=for-the-badge&color=4dffa0&label=release)](https://github.com/neonbytecode/termux-neon/releases)

</div>

Termux Neon is a modern, neon-styled Android add-on for [Termux](https://termux.org) that lets you preview, choose, and apply a custom terminal palette and font without restarting your session.

It is a from-scratch Compose rewrite of Termux's official [termux-styling](https://github.com/termux/termux-styling) add-on, while preserving the same compatibility contract: the `com.termux.styling` package name, shared `com.termux` user ID, and the same `~/.termux` file paths expected by Termux itself.

<div align="center">
<img src="docs/assets/1_preview.png" width="30%"/>
<img src="docs/assets/2_applied.png" width="30%"/>
<img src="docs/assets/3_textcolor.png" width="30%"/>
</div>

## Why this project exists

Termux already supports theme customization, but the original add-on flow is limited to a more basic experience. Termux Neon focuses on:

- a cleaner visual interface
- live preview before applying
- fast swapping of schemes and fonts
- one-tap apply + hot reload
- a modular architecture that is easier to extend and maintain

This makes it easier for both casual users and advanced terminal enthusiasts to tune their environment without editing raw config files by hand.

## Highlights

- 15 curated color schemes, including Catppuccin, Dracula, Nord, Solarized, Tokyo Night, Rosé Pine, Gruvbox, Monokai, One Dark, Kanagawa, and more.
- A large bundled font collection with popular monospace and Nerd Font variants.
- Live preview before any write happens.
- Search, sorting, favorites, and random shuffle helpers.
- Per-session custom text color override beyond the selected scheme.
- Smart Pick recommendations that adapt to the current time of day.
- Preset backup and restore through portable versioned JSON files.
- Readability feedback with live foreground/background contrast checks.
- One-tap apply of scheme + font + foreground text color together.
- Hot reload support for running Termux sessions with no restart required.
- Quick Settings tile and launcher shortcut for fast access.

## Quick start

### Install

> [!IMPORTANT]
> This app must be signed with the same key as your Termux install. The plugin uses the shared `sharedUserId="com.termux"` contract and writes to `~/.termux/colors.properties` and `~/.termux/font.ttf`.
>
> If the signatures do not match, Android will refuse to install the app or block access to Termux's private files. Install Termux Neon from the same source as your Termux install.

Recommended installation paths:

- F-Droid: best option when available; it matches the official Termux distribution model.
- GitHub Releases: for advanced users testing against a debug-signed Termux build.
- Local build: for development and testing only.

#### Build locally

```sh
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

The debug APK is intended for development and testing. It will not work beside a normal packaged Termux install unless both are signed by the same key.

### Use it

1. Open Termux.
2. Long-press anywhere in the terminal.
3. Choose `More...` and then `Style`.
4. Select a color scheme, font, and optional text color override.
5. Tap `Apply All`.

The app writes the selected style to Termux's config files and triggers a package-scoped reload broadcast. The running session updates live without restarting Termux.

## Installation compatibility matrix

| Termux install source | Compatible Termux Neon source |
|---|---|
| F-Droid | F-Droid |
| Self-built debug Termux | Locally built debug APK |
| Self-built release Termux | Locally signed release APK with matching key |

If you mix sources, Android will reject the install or block the file access required for styling.

## Why the shared UID matters

This project deliberately keeps the legacy Termux add-on contract:

- package name: `com.termux.styling`
- shared user ID: `com.termux`
- same files under `~/.termux`

This is the mechanism that allows the add-on to read and write Termux's private styling data. It also explains why signature matching across both APKs is mandatory.

## Project structure

```text
.
├── app/                          # Android app + Compose UI + runtime state
│   ├── src/main/java/com/termux/styling/
│   ├── src/main/assets/colors/   # bundled scheme files
│   └── src/main/assets/fonts/    # bundled font files
├── core/
│   ├── termux/                   # Termux environment integration
│   ├── theme-engine/             # ANSI palette model + colors.properties parsing
│   └── designsystem/             # reusable neon UI building blocks
├── docs/                         # project documentation and notes
├── gradle/                       # Gradle wrapper and config
├── app/build.gradle.kts          # Android app module config
├── settings.gradle.kts           # module graph
├── THIRD_PARTY_LICENSES.md        # bundled asset licensing and source metadata
├── README.md                     # project overview and usage guide
├── CONTRIBUTING.md                # contribution guidelines
├── LICENSE                       # project license
├── setup-fonts.sh                # regenerate bundled fonts
├── setup-nerd-fonts.sh          # regenerate Nerd Font assets
└── fontpatcher-py3.patch        # compatibility patch for font generation
```

## Documentation

- [`docs/architecture.md`](docs/architecture.md) — module boundaries and runtime flow
- [`CHANGELOG.md`](CHANGELOG.md) — version history and notable changes
- [`THIRD_PARTY_LICENSES.md`](THIRD_PARTY_LICENSES.md) — bundled asset provenance and licensing
- [`SECURITY.md`](SECURITY.md) — security reporting and compatibility-sensitive areas
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — development and contribution workflow

## Architecture overview

The project is intentionally split into modules so each layer has a clear responsibility:

- `:app` — Compose UI, state, and the plugin activity entry point.
- `:core:termux` — interaction with Termux's live environment, file access, and reload broadcast.
- `:core:theme-engine` — ANSI palette model and parsing logic for `colors.properties`.
- `:core:designsystem` — shared neon theme and reusable UI components.

This separation helps keep styling logic, Android/plugin integration, and presentation concerns isolated.

### Beginner view

The app is a selector and preview screen. The user chooses a scheme and font,
then `MainViewModel` coordinates the apply operation.

### Contributor view

The app module owns UI state and Android entry points. Termux file access and
reload behavior belong in `core:termux`; palette parsing belongs in
`core:theme-engine`; reusable Compose visuals belong in
`core:designsystem`.

### Expert view

The shared-UID plugin contract is the compatibility boundary. The writer
performs atomic, idempotent updates to Termux's private files, then uses a
package-scoped broadcast whose receiver is lifecycle-bound in Termux. Signing,
file format, receiver timing, and rollback semantics are all part of the
runtime contract.

## Development

### Requirements

- JDK 17+
- Android SDK configured for the project
- Gradle wrapper included in the repo

### Run tests

```sh
./gradlew testDebugUnitTest
```

### Lint and build

```sh
./gradlew lintDebug :app:assembleDebug :app:assembleRelease
```

### Compose instrumentation tests

```sh
./gradlew :app:connectedDebugAndroidTest
```

### Useful focused tests

```sh
./gradlew :app:testDebugUnitTest --tests com.termux.styling.AppAssetsIntegrityTest
./gradlew :core:termux:testDebugUnitTest --tests dev.neonbytecode.neon.termux.TermuxStyleWriterTest
./gradlew :core:theme-engine:testDebugUnitTest --tests dev.neonbytecode.neon.themeengine.TermuxColorsParserTest
```

## Troubleshooting

### “Termux not found”

Install Termux before launching the add-on, then reopen Termux Neon.

### “Signed by a different source”

Uninstall both apps and reinstall them from the same distribution source. The shared UID contract requires matching signatures.

### Styles apply but the terminal does not update

Return to Termux after applying. The add-on writes the files first and sends a reload broadcast when the app is foregrounded. If needed, verify that the files exist under:

- `~/.termux/colors.properties`
- `~/.termux/font.ttf`

### App reports inaccessible Termux environment

This usually indicates:

- Termux is not installed
- the apps are signed by different keys
- private storage access is blocked or incomplete

Reinstall both apps from the same source before reporting a bug.

## Relationship to `termux-styling`

Termux Neon is not a fork of the upstream project source; it is a ground-up Compose rewrite built to preserve the same runtime behavior, compatibility contract, and file format contract that Termux expects. It is designed as a drop-in replacement from Termux's point of view while improving the user experience and maintainability of the codebase.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md). This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md).

## License

This project is GPLv3-only. See [`LICENSE`](LICENSE).

Third-party bundled fonts and color schemes keep their own individual licenses and sources; see [`THIRD_PARTY_LICENSES.md`](THIRD_PARTY_LICENSES.md).

## Release process

1. Update version metadata and `CHANGELOG.md`.
2. Run the project verification suite.
3. Ensure the release tag and F-Droid metadata are aligned.
4. Create a version tag and publish the GitHub release.
5. Submit or update the F-Droid metadata separately; F-Droid signs its own builds.
