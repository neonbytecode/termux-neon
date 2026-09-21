# Security Policy

Termux Neon has a narrow attack surface: it reads and writes files under
`~/.termux/` on the device it's installed on, and requests no `INTERNET`
permission. Most reportable issues will be file-handling edge cases rather
than remote exploits, but please still report privately rather than opening
a public issue.

## Reporting a vulnerability

Use GitHub's private vulnerability reporting for this repository:
[Report a vulnerability](https://github.com/neonbytecode/termux-neon/security/advisories/new)
(repository → Security tab → "Report a vulnerability"). This reaches the
maintainer directly without disclosing details publicly.

This policy covers issues in Termux Neon's own code only. For vulnerabilities
in Termux itself (the terminal app this plugin attaches to), report them via
[Termux's own security policy](https://termux.dev/security) instead.
