# Draft: outreach to Termux maintainers

Not sent yet — for you to review, edit into your own voice, and send from
your own accounts. Two channel options below; pick one (GitHub issue is
more likely to get a tracked response; Matrix is faster/more informal).

## Where to send it

- **GitHub**: open a "Discussion" (not an Issue — this isn't a bug) on
  `termux/termux-styling` if Discussions are enabled there, otherwise on
  `termux/termux-app`. https://github.com/termux
- **Matrix**: Termux's community chat is linked from https://termux.dev —
  look for their Matrix room and ask there for a maintainer, referencing
  the GitHub post so there's a durable record.

Send the GitHub post first; mention it in Matrix as a follow-up rather than
duplicating the full text in both places.

## Draft message

> **Subject: Compose rewrite of termux-styling — question about the
> com.termux.styling package on F-Droid**
>
> Hi — I'm the author of [Termux Neon](https://github.com/neonbytecode/termux-neon),
> a from-scratch Jetpack Compose rewrite of termux-styling. It keeps the
> exact same plugin contract (package `com.termux.styling`, shared
> `sharedUserId="com.termux"`), so it installs and behaves as a drop-in
> replacement from Termux's side — same install flow, same
> `~/.termux/colors.properties` / `font.ttf` contract, same reload
> broadcast.
>
> What's different: a Compose UI with live preview, 15 curated color
> schemes, a larger bundled font set, search/favorites/shuffle, a custom
> text-color override, and a single "Apply All" action. Full credit to
> termux-styling's original authors — this project exists because that
> contract was already there to build on, and the repo's commit history is
> a direct continuation of termux-styling's, not a from-scratch copy.
>
> I'd like to get this on F-Droid, which is where it's most useful to
> people (matching signature with an F-Droid-installed Termux). Since it
> keeps the `com.termux.styling` package ID, and that's already the ID
> your app uses there, I wanted to ask directly rather than just filing an
> fdroiddata merge request and surprising anyone:
>
> - Would you be open to this replacing/supplementing the current
>   `com.termux.styling` F-Droid listing, given it's contract-compatible?
> - Or is there a different way you'd rather this be handled — e.g. a
>   distinct package ID (I understand this would break the plugin
>   contract, so I'd want to understand the tradeoff you'd prefer), or
>   something else entirely?
>
> Happy to answer anything about the rewrite, and no rush — just didn't
> want to go straight to fdroiddata without checking in first.
>
> — neonbytecode

## Notes for you before sending

- This is deliberately low-key and technical, not a launch announcement —
  it's the private/early step from the plan, separate from posting to
  r/termux or elsewhere later.
- If they respond positively, that response is worth screenshotting/saving
  — it'll matter for the fdroiddata MR description.
- If there's no response after ~1-2 weeks, it's reasonable to proceed with
  the fdroiddata MR anyway and note the outreach attempt in the MR
  description — F-Droid reviewers may loop in upstream themselves.
