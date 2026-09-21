# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

This release marks the from-scratch rewrite of the app: a Jetpack Compose UI
on top of a modular architecture, replacing the original View-based
`termux-styling` app while keeping the exact same plugin contract
(`com.termux.styling` package, shared `sharedUserId` with Termux).

### Added

- Compose UI with a neon-styled theme, live scanline/grid backdrop, and a
  live terminal preview that updates as you browse schemes and fonts.
- 15 curated, vivid color schemes (Catppuccin Mocha/Latte, Cyberpunk, Gotham,
  Solarized Dark/Light, Dracula, Nord, Gruvbox Dark, Tokyo Night, Rosé Pine,
  Monokai, One Dark, Ayu Dark, Kanagawa), replacing the previous full Base16
  scheme set with a smaller, hand-picked collection.
- Search/filter across both schemes and fonts.
- Favorites (star) with persistence and favorites-first sorting.
- Random shuffle button for browsing scheme/font combinations.
- Per-chip live font preview.
- Custom text (foreground) color override via 10 curated swatches, applied
  independently of the selected color scheme; tapping a selected swatch
  again clears the override.
- A single unified **Apply All** action that applies the selected scheme,
  font, and text color override together in one tap, replacing the previous
  separate "Apply scheme" / "Apply font" buttons.
- Asset integrity test guarding every shipped color scheme and font file.

### Changed

- Gesture/hardware back now reliably returns to Termux's own task
  (`FLAG_ACTIVITY_REORDER_TO_FRONT`) instead of occasionally landing on the
  home screen, regardless of how the activity was launched.
- Style hot-reload into the live Termux session now works reliably: the
  reload broadcast's extra key/value now matches Termux's actual contract,
  and is re-sent shortly after Termux regains focus to account for
  Termux only registering its receiver during `onStart`/`onStop`.
- The app now always renders in dark mode — its CRT/neon visual identity has
  no coherent light-mode equivalent.
- Floating apply controls: replaced a bordered, solid-background action bar
  with a borderless vertical scrim so the primary action floats over content
  without a hard panel edge.

### Removed

- All Base16 scheme variants, in favor of the curated 15-scheme set above.
- The Monofur font, after a license audit found it distributed under
  freeware terms with no recognized FOSS license grant (see
  `THIRD_PARTY_LICENSES.md`).

## Earlier releases (pre-rewrite)

Versions prior to the Compose rewrite tracked the original `termux-styling`
app closely. See the git history for details; notable tagged releases
included `v0.31`, `v0.32.0`, and `v0.32.1`.

[Unreleased]: https://github.com/neonbytecode/termux-neon/compare/v0.32.1...HEAD
