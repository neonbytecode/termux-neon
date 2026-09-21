---
title: "Debugging a shared-UID Android plugin: why my Termux theme wasn't hot-reloading"
published: false
tags: android, kotlin, opensource, debugging
canonical_url: https://neonbytecode.github.io/termux-neon/blog/shared-uid-hot-reload
---

*This is a devlog from building [Termux Neon](https://github.com/neonbytecode/termux-neon), a Compose UI rewrite of Termux's official theming add-on.*

## The symptom

Termux Neon lets you pick a terminal color scheme and font, then apply them
to a running [Termux](https://termux.org) session. The apply step itself
worked fine — I could see the new `colors.properties` and `font.ttf` land
correctly in Termux's data directory. But the actual terminal on screen
wouldn't update. You had to fully restart Termux to see your new theme.
For an app whose entire pitch is "live preview, one tap, done," that's not
a minor bug — it's the core feature not working.

## Why this app can touch Termux's files at all

Termux Neon isn't a normal, sandboxed Android app. It's a *plugin* in the
specific, old-school Android sense: it declares
`android:sharedUserId="com.termux"` in its manifest, which — provided both
APKs are signed with the same key — lets it run with the same UID as
Termux itself. That's what makes
`context.createPackageContext("com.termux", CONTEXT_IGNORE_SECURITY)`
succeed and hand back Termux's own `filesDir`, so the plugin can write
directly into `~/.termux/colors.properties` and `~/.termux/font.ttf` as if
it *were* Termux.

Writing the files was never the problem. The problem was telling the
*already-running* Termux process to notice.

## The reload contract

Termux exposes a broadcast for exactly this: send an intent with action
`com.termux.app.reload_style`, targeted at the `com.termux` package, and its
`TermuxActivity` reloads colors and font from disk. My first implementation
sent that broadcast right after writing the files and called it done. It
didn't work, and there were two separate bugs stacked on top of each other.

**Bug 1 — wrong extra.** I was passing a custom string extra to control
whether Termux should recreate its activity. Termux doesn't read that key at
all. Reading Termux's actual source turned up the real contract: a
*boolean* extra called `com.termux.app.TermuxActivity.EXTRA_RECREATE_ACTIVITY`,
read as `intent.getBooleanExtra(EXTRA_RECREATE_ACTIVITY, true)`. Since the
receiver reloads colors/font live regardless, and only *optionally* also
recreates the activity, the fix was to pass `false` explicitly — otherwise
you get a jarring full activity restart on every apply, not just a color
refresh:

```kotlin
fun requestStyleReload(context: Context) {
    val intent = Intent(RELOAD_STYLE_ACTION)
        .setPackage(TERMUX_PACKAGE)
        .putExtra(RELOAD_STYLE_EXTRA_RECREATE_ACTIVITY, false)
    context.sendBroadcast(intent)
}
```

Fixing the extra got the broadcast *understood*. It still didn't work
reliably, because of a second, more interesting bug.

**Bug 2 — nobody's listening yet.** Termux only registers its
`reload_style` receiver while `TermuxActivity` is started — inside its
`onStart()`/`onStop()` lifecycle callbacks, not globally in the manifest.
That makes sense defensively (don't listen for broadcasts you can't act on
correctly), but it means: when you tap "Apply" *inside the theming app*,
Termux's activity is backgrounded — fully covered by the theming app's own
UI — so its receiver isn't registered at that exact moment. The broadcast
gets sent into the void.

The fix isn't to abandon the live-reload broadcast — it's to guarantee the
broadcast gets sent again once Termux is actually the foreground activity.
When the user backs out to Termux, I bring its task forward explicitly
(more on why "explicitly" matters below), then re-send the reload broadcast
on a short delay once its `onStart()` has almost certainly fired:

```kotlin
private fun returnToTermux() {
    val launchTermux = packageManager.getLaunchIntentForPackage(TermuxEnvironment.TERMUX_PACKAGE)
    if (launchTermux != null) {
        launchTermux.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(launchTermux)
        val appContext = applicationContext
        Handler(Looper.getMainLooper()).postDelayed(
            { TermuxEnvironment.requestStyleReload(appContext) },
            350L,
        )
    }
    finish()
}
```

350ms is not a principled number — it's "comfortably longer than an
activity transition takes on real hardware," found by testing on-device
rather than derived. It's a small, honest hack: there's no callback for
"the other app's receiver just registered," so a delay is the pragmatic
tool available given the plugin's broadcast-based contract.

## A related bug in the same code path

While fixing this I found a second, related issue: back navigation (gesture
or button) out of the theming app didn't reliably return to Termux. A plain
`finish()` relies on the default back-stack pop, which lands wherever the
activity happened to be launched from — sometimes Termux, sometimes the
home screen, depending on the exact launch path (Termux's own long-press
menu vs. the launcher vs. `adb`). Explicitly building a launch `Intent` with
`FLAG_ACTIVITY_REORDER_TO_FRONT` (see `returnToTermux()` above) removes the
ambiguity: it always brings Termux's task to front regardless of how this
activity was entered. This turned out to matter for the reload fix too —
"bring Termux to front" is precisely the moment the delayed re-send needs
to target.

## Verifying it actually works

Screenshots don't prove a broadcast landed. What I actually checked,
end-to-end, on a real device:

```sh
adb shell run-as com.termux cat files/home/.termux/colors.properties
```

confirming the applied scheme's exact values were written, then watching
the live terminal repaint without a restart after backing out of the
theming app — the thing that wasn't happening before either fix.

## The takeaway

Cross-process communication on Android that isn't a formal `ContentProvider`
or bound service — a plain broadcast between two independently-signed-but-
shared-UID apps — has no compiler or IDE checking the contract for you. The
receiver's *registration lifecycle* is just as much a part of the contract
as the intent's action string and extras, and it's the part that's easy to
miss because it only shows up as an intermittent-feeling bug ("it works
sometimes") rather than a clean failure. Reading the actual receiving app's
source, not just its public docs, was what surfaced both bugs — the wrong
extra key would never have shown up from the sending side alone.

---

*Termux Neon is open source (GPLv3) — [source on GitHub](https://github.com/neonbytecode/termux-neon).*
