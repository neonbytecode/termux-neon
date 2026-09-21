# Termux Neon

[![Build status](https://github.com/neonbytecode/termux-neon/actions/workflows/github_action_build.yml/badge.svg)](https://github.com/neonbytecode/termux-neon/actions/workflows/github_action_build.yml)

A [Termux](https://termux.org) add-on app to customize the terminal font and
color theme, with a neon-styled Compose UI and a live preview of the current
selection.

This is a rewriting of [termux-styling](https://github.com/termux/termux-styling)
with the same plugin contract: it keeps the `com.termux.styling` package and
shared user ID so it installs and runs exactly like the upstream add-on.

When developing (or packaging), note that this app needs to be signed with the
same key as the main Termux app in order to have the permission to modify the
required font or color files. Debug builds use the untrusted key in
`app/testkey_untrusted.jks` so they install next to a debug Termux build;
release signing must come from CI/secrets.

## Installation

Build the app and install it with `adb install` (or build a release APK and
install it from a file manager):

```sh
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Per-push CI builds are available from the [workflow runs](https://github.com/neonbytecode/termux-neon/actions/workflows/github_action_build.yml).
Signature keys of different build sources differ: before switching source you
may have to uninstall the Termux application and all currently installed
plugins. See <https://github.com/termux/termux-app#Installation> for more info.

## How to use

1. When inside Termux, long press anywhere on the terminal.
2. Select `More...` in the resulting dialog.
3. Select `Style` in the next dialog.
4. In the app, pick a color scheme or font by tapping its card.
   The preview at the top updates live; cards show an `APPLIED` badge for the
   style currently installed in `~/.termux`.
5. Tap `Apply scheme` or `Apply font` to install your selection. The badge
   updates and Termux reloads the style over a package-scoped broadcast.

If the scheme/font is changed outside of the app (e.g. `echo` into
`~/.termux/colors.properties`), the badges re-sync when the app resumes.

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