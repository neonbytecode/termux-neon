# Architecture overview

This project is intentionally organized into a few well-defined modules so the app stays compatible with Termux’s styling contract while remaining maintainable and easy to extend.

## Project layout

```text
app/                     # UI and app entry point
core/termux/            # Termux integration and filesystem boundary
core/theme-engine/      # ANSI palette and colors.properties parsing
core/designsystem/      # shared neon UI components and preview surfaces
```

## :app

The `:app` module is the Android application layer and the highest-level user-facing surface. It owns:

- `TermuxStyleActivity`: the entry point Termux launches from its styling menu
- `NeonApp`: the Compose UI shell and screen composition
- `MainViewModel`: preview state, favorites, selection state, apply logic, and user flows
- local storage wrappers such as favorites, presets, and custom font imports

This layer is intentionally responsible for UI and orchestration, not low-level file or parsing logic.

## :core:termux

The `:core:termux` module is the compatibility boundary with the installed Termux environment. It owns:

- `TermuxEnvironment`: package detection, signature/access checks, and reload broadcast constants
- `StyleCatalog`: bundled asset discovery and byte caching
- `AppliedStyleReader`: reads the currently installed scheme/font from Termux's private files
- `TermuxStyleWriter`: writes `colors.properties` and `font.ttf` atomically and triggers reloads

This is the layer that preserves the plugin contract required by Termux itself.

## :core:theme-engine

The `:core:theme-engine` module is intentionally platform-agnostic and reusable. It includes:

- `AnsiPalette`: the model representing the color palette
- `TermuxColorsParser`: parsing logic for `colors.properties` files
- preview-related parsing helpers used by the app layer

Color parsing and palette semantics belong here rather than in the UI layer.

## :core:designsystem

The `:core:designsystem` module contains reusable UI primitives and visual components, such as:

- `NeonTheme`
- `NeonButton`, `NeonCard`, and other shared composables
- `TerminalPreview` and other preview-related UI primitives

This keeps app-specific behavior out of the design layer and makes the visual system easier to reuse.

## Typical runtime flow

The normal flow is:

1. The user selects a scheme, font, and optional text color in the Compose UI.
2. `MainViewModel` updates preview state without writing anything yet.
3. `TermuxStyleWriter.applyAll()` writes the selected data to Termux’s `~/.termux` files atomically.
4. The app sends the `com.termux.app.reload_style` reload broadcast.
5. Termux re-reads the updated files and refreshes live without requiring a terminal restart.

## Premium experience components

- `ThemeRecommendationEngine` provides a deterministic Smart Pick based on
  the local time of day, keeping recommendations useful without collecting
  telemetry.
- `PresetBundle` and `PresetsStore` provide versioned JSON backup and restore
  for saved style combinations.
- `ThemeHealthChecker` calculates foreground/background contrast and warns
  about palettes that may be difficult to read.
- `TermuxStyleTileService` and the launcher shortcut expose fast Android-native
  entry points without changing the Termux file contract.

## Design principles

- Keep Termux-specific compatibility logic in `:core:termux`.
- Keep parsing logic in `:core:theme-engine`.
- Keep reusable visuals in `:core:designsystem`.
- Keep UI state and behavior in `:app` rather than leaking app logic into composables.
- Preserve the plugin contract exactly; it is a compatibility requirement, not a refactor target.

## Why this matters

The project is intentionally designed around compatibility with Termux’s existing contract, not around abstract Android patterns alone. That is why the architecture is split in this way: the app can remain modern and maintainable without risking the plugin compatibility Termux depends on.

## Failure and recovery model

- Environment access is evaluated before writes. The app distinguishes a
  missing Termux package, incompatible signing, and other access failures.
- Style writes use an idempotent fast path and atomic files.
- `applyAll()` snapshots both target files and restores both snapshots if a
  replacement fails.
- Imported assets remain app-private until the user explicitly applies them.
- Preset bundles are versioned and validated before they are merged into local
  storage.

## Extension guide

When adding a feature, place it at the narrowest responsible layer:

| Concern | Preferred location |
| --- | --- |
| User interaction and screen state | `app` / `MainViewModel` |
| Termux package, files, or broadcasts | `core/termux` |
| Palette semantics or properties parsing | `core/theme-engine` |
| Reusable Compose visuals | `core/designsystem` |
| Bundled asset provenance | `THIRD_PARTY_LICENSES.md` |

Avoid placing Termux filesystem logic in composables or adding app-specific
behavior to the design system.
