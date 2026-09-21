# I tracked down why my Termux styling plugin could write the files but not hot-reload the terminal

I’m building [Termux Neon](https://github.com/neonbytecode/termux-neon), a
from-scratch Jetpack Compose rewrite of the Termux:Styling add-on. The core
feature is applying a color scheme and font to a running Termux session
without restarting it.

The first version wrote the expected files correctly:

- `~/.termux/colors.properties`
- `~/.termux/font.ttf`

But the terminal did not repaint until Termux was restarted.

The issue turned out to be two separate contract mismatches. First, the
reload broadcast used the wrong extra. Termux expects the boolean
`com.termux.app.TermuxActivity.EXTRA_RECREATE_ACTIVITY`; passing `false`
requests a live style reload without recreating the activity.

Second, Termux registers that receiver only while `TermuxActivity` is
started. When the styling app sends the broadcast, Termux is in the
background, so there may be no receiver listening. The fix is to bring the
Termux task to the foreground and send the broadcast again after a short
delay.

The broader lesson was that an Android integration contract includes
receiver lifecycle, not just an action string and an extra name. Reading the
receiving app’s source was what exposed the actual behavior.

The full devlog, including the relevant Kotlin snippets, is here:

https://neonbytecode.github.io/termux-neon/blog/shared-uid-hot-reload

I’d especially appreciate compatibility feedback from people using Termux
plugins or testing this with a debug-signed Termux build.
