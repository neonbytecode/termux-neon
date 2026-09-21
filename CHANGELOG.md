# Changelog

All notable changes to this project are documented here.

This project follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Improved project documentation and contributor guidance.
- Clearer module-level architecture notes and onboarding context for future contributors.
- Smart Pick recommendations based on local time of day.
- Versioned JSON preset export/import.
- Live palette readability feedback based on foreground/background contrast.
- Quick Settings tile and launcher shortcut entry points.

### Changed

- Improved production diagnostics, imported-asset validation, documentation
  navigation, accessibility guidance, and contributor templates.

- Refined the README to better explain the project purpose, compatibility constraints, architecture, and usage flow for both beginner and advanced readers.

## [1.0.0] - 2026-09-21

This release marks the from-scratch rewrite of the app: a Jetpack Compose UI built on a modular architecture, replacing the previous View-based `termux-styling` implementation while preserving the exact plugin contract expected by Termux (`com.termux.styling` package, shared `sharedUserId`, and `~/.termux` file paths).

### Added

- Compose UI with a neon-styled theme, scanline/grid backdrop, and live terminal preview for scheme and font browsing.
- 15 curated color schemes, including Catppuccin, Cyberpunk, Gotham, Solarized, Dracula, Nord, Gruvbox, Tokyo Night, Rosé Pine, Monokai, One Dark, Ayu Dark, and Kanagawa.
- Search and filtering across color schemes and fonts.
- Favorites with persistence and favorites-first ordering.
- Randomized shuffle mode for previewing scheme/font combinations.
- Per-card live font preview.
- A custom foreground text color override using curated swatches, applied independently of the selected scheme.
- A single unified **Apply All** action that applies the chosen scheme, font, and foreground color together.
- Asset integrity tests covering shipped schemes and fonts.

### Changed

- Hardware/back navigation now returns to Termux more reliably using the proper activity reorder behavior.
- Hot-reload behavior was tightened so the app re-sends the reload broadcast when Termux is back in focus, accounting for the receiver lifecycle in Termux itself.
- The visual design is now consistently dark-mode-only, matching the CRT/neon aesthetic of the app.
- The floating apply controls were simplified to feel more native and unobtrusive while keeping the call to action prominent.

### Removed

- Older Base16-heavy scheme variants in favor of the curated 15-scheme collection.
- The Monofur font after license review found it was not distributed under a clear FOSS-compatible license.

## Earlier releases (pre-rewrite)

Version history before the Compose rewrite tracked the earlier `termux-styling` app more closely. See the git history for older tagged releases such as `v0.31`, `v0.32.0`, and `v0.32.1`.

[Unreleased]: https://github.com/neonbytecode/termux-neon/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/neonbytecode/termux-neon/compare/v0.32.1...v1.0.0
