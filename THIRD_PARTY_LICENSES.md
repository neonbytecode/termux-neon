# Third-Party Licenses

Termux Neon bundles curated color schemes and monospace fonts as assets. This
file catalogues the license of each bundled item for compliance purposes —
this matters both for general GPLv3 project hygiene and for F-Droid
submission, since F-Droid requires every bundled asset to carry a clear,
verifiable FOSS license.

Termux Neon itself is licensed under GPLv3-only (see `LICENSE`). It is a
Compose UI rewrite of [`termux/termux-styling`](https://github.com/termux/termux-styling),
keeping the same plugin contract established by the Termux project — thanks
to the Termux maintainers and community for that groundwork.

## Fonts

All font files live in `app/src/main/assets/fonts/`.

| Font | Source | License | Notes |
|---|---|---|---|
| Anonymous Pro | [marksimonson.com/fonts/view/anonymous](https://www.marksimonson.com/fonts/view/anonymous) | OFL-1.1 | Mark Simonson, 2009. |
| Bedstead Condensed | [bjh21.me.uk/bedstead](http://bjh21.me.uk/bedstead/) | CC0 | Public domain dedication, confirmed in the bundled `Bedstead-Condensed.txt`. |
| Cascadia Code | [github.com/microsoft/cascadia-code](https://github.com/microsoft/cascadia-code) | OFL-1.1 | Microsoft Corporation. |
| Courier Prime | [quoteunquoteapps.com/courierprime](https://quoteunquoteapps.com/courierprime/) | OFL-1.1 | Alan Dague-Greene / Quote-Unquote Apps. |
| D2 Coding | [github.com/naver/d2-coding-font](https://github.com/naver/d2-coding-font) | OFL-1.1 | NAVER Corporation, Reserved Font Name "D2Coding". |
| DejaVu Sans Mono | [dejavu-fonts.github.io](https://dejavu-fonts.github.io/) | Bitstream Vera License (permissive; DejaVu's own modifications are public domain) | Derived from Bitstream Vera; cannot be sold by itself, must be renamed if modified — already satisfied since we redistribute unmodified. |
| Fantasque Sans Mono | [github.com/belluzj/fantasque-sans](https://github.com/belluzj/fantasque-sans) | OFL-1.1 | Jany Belluz. |
| Fira Code | [github.com/tonsky/FiraCode](https://github.com/tonsky/FiraCode) | OFL-1.1 | Nikita Prokopov, building on Mozilla's Fira family. |
| Fira Mono | [github.com/mozilla/Fira](https://github.com/mozilla/Fira) | OFL-1.1 | Mozilla Foundation, Reserved Font Name "Fira Mono". |
| GNU FreeFont | [gnu.org/software/freefont](https://www.gnu.org/software/freefont/) | GPL-3.0-or-later WITH Font-exception-2.0 | GNU Project. The font-embedding exception means documents/apps bundling it unmodified aren't themselves pulled under GPL. |
| Go Mono | [go.dev/blog/go-fonts](https://go.dev/blog/go-fonts) | BSD-3-Clause | Bigelow & Holmes, for the Go project. |
| Hack | [github.com/source-foundry/Hack](https://github.com/source-foundry/Hack) | Hack Open Font License v2.0 + Bitstream Vera License | Christopher Simpkins; permissive, OFL-equivalent terms. |
| Hermit | [github.com/pcaro90/hermit](https://github.com/pcaro90/hermit) | OFL-1.1 | Pablo Caro. |
| Inconsolata | [levien.com/type/myfonts/inconsolata.html](https://levien.com/type/myfonts/inconsolata.html) | OFL-1.1 | Raph Levien. |
| Iosevka | [github.com/be5invis/Iosevka](https://github.com/be5invis/Iosevka) | OFL-1.1 (support code: BSD) | Belleve Invis. |
| JetBrains Mono | [github.com/JetBrains/JetBrainsMono](https://github.com/JetBrains/JetBrainsMono) | OFL-1.1 | JetBrains s.r.o. |
| Liberation Mono | [github.com/liberationfonts/liberation-fonts](https://github.com/liberationfonts/liberation-fonts) | OFL-1.1 | Red Hat, Inc. |
| Meslo | [github.com/andreberg/Meslo-Font](https://github.com/andreberg/Meslo-Font) | Apache-2.0 | André Berg, customized Menlo derivative. |
| Monoid | [github.com/larsenwork/monoid](https://github.com/larsenwork/monoid) | MIT OR OFL-1.1 (dual-licensed) | Andreas Larsen and contributors. |
| OpenDyslexic | [github.com/antijingoist/opendyslexic](https://github.com/antijingoist/opendyslexic) | OFL-1.1 | Abbie Gonzalez / antijingoist. Earlier releases used a bespoke license; current releases are OFL. |
| Roboto Mono | [github.com/googlefonts/roboto-2](https://github.com/googlefonts/roboto-2) | Apache-2.0 | Google (Christian Robertson). |
| Source Code Pro | [github.com/adobe-fonts/source-code-pro](https://github.com/adobe-fonts/source-code-pro) | OFL-1.1 | Adobe (Paul D. Hunt). |
| Terminus | [terminus-font.sourceforge.net](https://terminus-font.sourceforge.net/) | OFL-1.1 | Dimitar Toshkov Zhekov; TTF conversion via [files.ax86.net/terminus-ttf](https://files.ax86.net/terminus-ttf/). |
| Ubuntu Mono | [font.ubuntu.com](https://font.ubuntu.com/) | Ubuntu Font Licence 1.0 (`Ubuntu-font-1.0`) | Canonical/Dalton Maag. OFL-inspired, not identical to OFL — recognized permissive font license, no issue for F-Droid. |
| Victor Mono | [github.com/rubjo/victor-mono](https://github.com/rubjo/victor-mono) | OFL-1.1 | Rune Bjørnerås. |

## Color Schemes

All scheme files live in `app/src/main/assets/colors/` (each also carries a
source-URL comment at the top of its `.properties` file).

| Scheme | Source | License | Notes |
|---|---|---|---|
| Ayu Dark | [github.com/ayu-theme/ayu-colors](https://github.com/ayu-theme/ayu-colors) | MIT | |
| Catppuccin Latte / Mocha | [github.com/catppuccin/catppuccin](https://github.com/catppuccin/catppuccin) | MIT | |
| Cyberpunk | [github.com/mbadolato/iTerm2-Color-Schemes](https://github.com/mbadolato/iTerm2-Color-Schemes) (xrdb/Cyberpunk.xrdb) | MIT | Collection-wide MIT license, copyright Mark Badolato, 2011–present. Not an original creation of this project. |
| Dracula | [draculatheme.com](https://draculatheme.com/) | MIT | |
| Gotham | [github.com/whatyouhide/gotham-contrib](https://github.com/whatyouhide/gotham-contrib/blob/master/xresources/gotham) | MIT | |
| Gruvbox Dark | [github.com/morhetz/gruvbox](https://github.com/morhetz/gruvbox) | MIT | |
| Kanagawa | [github.com/rebelot/kanagawa.nvim](https://github.com/rebelot/kanagawa.nvim) | Apache-2.0 | |
| Monokai | [monokai.pro](https://monokai.pro/) (classic xterm mapping) | No formal license from original author (Wimer Hazenberg) | The specific numeric color values used here are a common community xterm/ANSI mapping (comparable ports, e.g. [tomasr/molokai](https://github.com/tomasr/molokai), are MIT-licensed). Raw color values are generally not independently copyrightable; low practical risk, but note the absence of a formal grant explicitly rather than assuming MIT. |
| Nord | [nordtheme.com](https://www.nordtheme.com/) | MIT | |
| One Dark | [github.com/atom/one-dark-ui](https://github.com/atom/one-dark-ui) | MIT | |
| Rosé Pine | [rosepinetheme.com](https://rosepinetheme.com/) | MIT | |
| Solarized Dark / Light | [github.com/altercation/solarized](https://github.com/altercation/solarized/blob/master/xresources/solarized) | MIT | |
| Tokyo Night | [github.com/enkia/tokyo-night-vscode-theme](https://github.com/enkia/tokyo-night-vscode-theme) | MIT | |

## Summary for F-Droid submission

All 25 bundled fonts and 14 of 15 color schemes carry a clear, verifiable
FOSS license. One item is worth noting but isn't a hard blocker:

- **Monokai** (color scheme) — the underlying numeric values have no formal
  license grant from the original author, though this is low practical risk
  since color values generally aren't independently copyrightable and
  comparable community ports are MIT. Worth a note in the fdroiddata MR
  description rather than a blocker, but flagged here for visibility.

**Monofur** was originally bundled as a font but has been removed
(2025) after this audit found it was freeware with no recognized FOSS
license grant — not a fit for F-Droid distribution.

Everything else in both tables checked out clean against each item's actual
upstream source during this audit (not assumed from memory).
