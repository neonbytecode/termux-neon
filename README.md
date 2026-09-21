# Termux Neon

[![Build status](https://github.com/neonbytecode/termux-neon/actions/workflows/github_action_build.yml/badge.svg)](https://github.com/neonbytecode/termux-neon/actions/workflows/github_action_build.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

A [Termux](https://termux.org) add-on to customize the terminal's font and
color theme, with a neon-styled Compose UI, live preview, and one-tap apply
that hot-reloads the running Termux session — no restart needed.

This is a from-scratch Compose rewrite of Termux's official
[termux-styling](https://github.com/termux/termux-styling) add-on. It keeps
the exact same plugin contract — the `com.termux.styling` package and shared
user ID — so it installs and runs exactly where the upstream add-on did.

## Features

- **15 curated color schemes** — Catppuccin Mocha/Latte, Cyberpunk, Gotham,
  Solarized Dark/Light, Dracula, Nord, Gruvbox Dark, Tokyo Night, Rosé Pine,
  Monokai, One Dark, Ayu Dark, and Kanagawa.
- **A large bundled font collection**, including popular
  [Nerd Fonts](https://www.nerdfonts.com/)-patched monospace fonts and
  D2 Coding — see `app/src/main/assets/fonts/` for the current set, and
  `THIRD_PARTY_LICENSES.md` for each font's license and source.
- **Live preview** of the selected scheme/font before applying anything.
- **Favorites** — star schemes and fonts, which then sort to the top.
- **Search/filter** across both schemes and fonts.
- **Shuffle** — randomly preview a scheme/font combination.
- **Custom text color** — an independent foreground-color override, picked
  from 10 curated swatches, applied on top of whatever scheme is selected.
- **Apply All** — one action applies the selected scheme, font, and text
  color together, then hot-reloads the live Termux session.

## Installation

> [!IMPORTANT]
> This app must be signed with the same key as your Termux install (shared
> `sharedUserId="com.termux"`) or Android will refuse to install it, or it
> won't have permission to write `~/.termux/colors.properties` /
> `~/.termux/font.ttf`. **Install Termux Neon from the same source you
> installed Termux from** — mixing sources (e.g. an F-Droid Termux with a
> GitHub-built Termux Neon) will fail.

- **F-Droid** — the primary, recommended channel once the listing is live
  (matches how the original `termux-styling` is distributed; F-Droid builds
  and signs the whole Termux app family itself, so the signature always
  matches an F-Droid-installed Termux).
- **GitHub Releases** — direct APK download from the
  [Releases page](https://github.com/neonbytecode/termux-neon/releases).
  **This build is signed with an untrusted debug key** and will only work
  alongside a self-built, debug-signed Termux — it is an advanced/testing
  channel, not for most users running official Termux.
- **Build it yourself**:

  ```sh
  ./gradlew :app:assembleDebug
  adb install app/build/outputs/apk/debug/app-debug.apk
  ```

  Per-push CI builds are also available from the
  [workflow runs](https://github.com/neonbytecode/termux-neon/actions/workflows/github_action_build.yml).
  See <https://github.com/termux/termux-app#Installation> for background on
  why signature sources must match across Termux and its plugins.

## How to use

1. When inside Termux, long press anywhere on the terminal.
2. Select `More...` in the resulting dialog.
3. Select `Style` in the next dialog.
4. In the app, pick a color scheme, a font, and/or a text color by tapping
   its card or swatch. The preview at the top updates live; cards show a
   `PREVIEW` or `APPLIED` badge depending on state.
5. Tap **Apply All** to install your selection. Termux reloads its style
   live over a package-scoped broadcast — no restart needed.

If the scheme/font is changed outside of the app (e.g. `echo` into
`~/.termux/colors.properties`), the badges re-sync when the app resumes.

## Relationship to termux-styling

Termux Neon is not a patch on top of `termux/termux-styling` — it's a
ground-up rewrite of the UI in Jetpack Compose, with the app split into
small, testable modules (see below). The plugin contract Termux relies on
(package name, shared user ID, the files it reads/writes under
`~/.termux/`) is kept identical, so it's a drop-in replacement from Termux's
point of view. Full credit to the original `termux-styling` authors and the
wider Termux project — this project wouldn't exist without their work
establishing that contract. See `THIRD_PARTY_LICENSES.md` for licensing of
every bundled font and color scheme.

## Development

The project is split into small modules:

- `:app` — Compose UI, state, and the `TermuxStyleActivity` the plugin jumps to.
- `:core:theme-engine` — ANSI palette model and `colors.properties` parser.
- `:core:termux` — catalog of bundled schemes/fonts, applied-style reader, writer.
- `:core:designsystem` — Neon theme and reusable building blocks.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Requires JDK 17+; AGP 9 builds Kotlin with its built-in compiler (version via
`gradle/libs.versions.toml`).

The `setup-fonts.sh` / `setup-nerd-fonts.sh` scripts regenerate the bundled
font assets. `fontpatcher-py3.patch` patches the powerline font patcher for
Python 3 and is applied by `setup-fonts.sh`.

See `CONTRIBUTING.md` for how to propose changes, including adding new fonts
or color schemes.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md). This project follows the
[Contributor Covenant](CODE_OF_CONDUCT.md).

## License

GPLv3-only — see [`LICENSE`](LICENSE). Third-party bundled fonts and color
schemes retain their own licenses; see
[`THIRD_PARTY_LICENSES.md`](THIRD_PARTY_LICENSES.md).
