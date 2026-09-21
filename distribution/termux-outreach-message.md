Subject: Termux Neon: compatibility review for a Compose rewrite of Termux:Styling

Hi — I'm the author of Termux Neon:

https://github.com/neonbytecode/termux-neon

It is a from-scratch Jetpack Compose rewrite of the Termux:Styling add-on.
It preserves the existing plugin contract:

- application ID: `com.termux.styling`
- shared user ID: `com.termux`
- `~/.termux/colors.properties`
- `~/.termux/font.ttf`
- the live style reload broadcast

The goal is a maintainable alternative implementation, not a change to
Termux itself. It includes live preview, curated schemes and fonts,
favorites, search, shuffle, and one-tap Apply All.

I have published the v1.0.0 release and am preparing an F-Droid submission:

https://github.com/neonbytecode/termux-neon/releases/tag/v1.0.0

Could you review whether the compatibility assumptions above are correct and
whether there are any concerns with using the existing package namespace and
the Termux name in the project description? In particular, would you prefer
this to supplement the existing Termux:Styling listing, replace it, or be
handled another way?

For reference, in case it's useful to look at directly rather than take my
word for:

- What changed and why: https://github.com/neonbytecode/termux-neon/blob/master/CHANGELOG.md
- Bundled asset licensing (every font/scheme individually checked, one
  dropped for not being FOSS-licensed):
  https://github.com/neonbytecode/termux-neon/blob/master/THIRD_PARTY_LICENSES.md
- How the reload-broadcast timing works, since it depends on the exact
  receiver-registration lifecycle in termux-app:
  https://neonbytecode.github.io/termux-neon/blog/shared-uid-hot-reload.html

Thanks.
