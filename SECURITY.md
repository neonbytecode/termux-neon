# Security Policy

Termux Neon has a narrow attack surface: it reads and writes files under
`~/.termux/` on the device it's installed on, and requests no `INTERNET`
permission. It also imports user-selected fonts, schemes, and preset bundles
through Android's document provider APIs. Most reportable issues will
therefore be file-validation, unsafe-path, shared-UID, or data-integrity
issues rather than remote exploits.

The project does not intentionally collect telemetry or send user styles,
presets, or font data over the network.

## Reporting a vulnerability

Use GitHub's private vulnerability reporting for this repository:
[Report a vulnerability](https://github.com/neonbytecode/termux-neon/security/advisories/new)
(repository → Security tab → "Report a vulnerability"). This reaches the
maintainer directly without disclosing details publicly.

This policy covers issues in Termux Neon's own code only. For vulnerabilities
in Termux itself (the terminal app this plugin attaches to), report them via
[Termux's own security policy](https://termux.dev/security) instead.

## Compatibility-sensitive areas

Changes involving any of the following require extra review:

- `android:sharedUserId="com.termux"`
- the `com.termux.styling` package or `TermuxStyleActivity`
- writes to `~/.termux/colors.properties` or `~/.termux/font.ttf`
- the `com.termux.app.reload_style` broadcast
- imported file names, path sanitization, and atomic rollback behavior

Do not include private keys, signing material, device backups, or private
Termux data in an issue, pull request, log, or test fixture.
