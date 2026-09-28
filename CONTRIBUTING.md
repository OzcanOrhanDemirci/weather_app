# Contributing

Thank you for looking. This document says how the repository is worked in, so
that a change written by someone else arrives in the same shape as the ones
already here.

## Where this project came from

Hava was written during the **Türkcell Kamp+ information technology camp**, in
the mobile development programme run at a Youth Camp with Turkcell Akademi. The
reference exercise was a weather application in Kotlin and Jetpack Compose
against the Open-Meteo service. This repository is one participant's answer to
it, taken further than the exercise asked. [README.md](README.md) explains what
that means and why.

That history is worth knowing before changing anything, because several
decisions here exist to answer a teaching point rather than a product
requirement, and they are recorded as such in
[docs/decisions](docs/decisions).

## Before you start

```bash
git clone https://github.com/OzcanOrhanDemirci/weather_app.git
cd weather_app
./gradlew :app:assembleDebug
```

You need a JDK of 17 or newer and Android SDK Platform 36. Gradle provisions the
JDK 21 toolchain itself, so the compiler is the same on every machine. No API
key, no account and no server: Open-Meteo needs none of them.

## The shape of a change

**One commit, one change.** A commit that fixes a bug and renames a variable is
two commits. `git log --oneline` should read like a list of decisions.

**Subjects follow [Conventional Commits](https://www.conventionalcommits.org/):**

```
type(scope): summary in the imperative, lowercase, no full stop
```

`type` is one of `feat`, `fix`, `docs`, `refactor`, `perf`, `test`, `build`,
`ci`, `chore`, `style` or `revert`. `scope` is the module or feature the change
lives in, such as `detail`, `sky` or `app`, and may be omitted when a change is
genuinely project-wide. The whole subject stays within 80 characters, because a
log is read at a glance.

This is checked rather than asked for. The pipeline runs it on every pull
request, and you can run it yourself before pushing:

```bash
.github/scripts/check-commit-subjects.sh
```

A convention that nothing verifies is a suggestion, and suggestions drift.

**The body says why.** A reviewer can read the diff. What they cannot read is
the option you rejected, and that is the part worth writing down:

```
fix(detail): space hour labels by measurement rather than a fixed step

Labelling every third hour held only at the default font scale. Text grows
with the reader's setting while the chart does not, so at 1.8 the first two
labels closed up and read as one four digit number.

The step is now derived from the width of a laid out label against the width
of one hour, and a label that would still meet the one before it is dropped.
A missing label reads as spacing; an overlapping one reads as a defect.
```

**Branches are named after the change**, using the same type as its commits:
`fix/hour-label-spacing`, `feat/wide-window-layout`, `chore/repository-standards`.

## Pull requests

`main` is protected: it takes no direct pushes, requires the verification
pipeline to pass, and keeps a linear history. Everything lands through a pull
request.

The [pull request template](.github/pull_request_template.md) asks four
questions, and the fourth is the one that matters most:

- **What** changed.
- **Why**, including the alternative you did not take.
- **Verification**: what you ran, and what you *looked at*. Not "it works".
- **Not verified**: what this change could break that nothing here checks.

A pull request that says what it did not prove is worth more than one that
implies it proved everything.

## Code

The conventions are not written down twice. Read the file you are changing and
match it; if that is not enough, [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
explains where the boundaries are and why. A few rules that are easy to miss:

- **Comments say why, never what.** The code already says what it does. A
  comment that restates it will be wrong after the next edit and nobody will
  notice.
- **No placeholders, no notes to self, no dead code.** If something is
  unfinished it does not belong in a commit.
- **Every user-visible string is a resource**, in both `values` and `values-tr`,
  unless it genuinely cannot be translated — and then it is marked
  `translatable="false"` with a comment saying why.
- **Numbers are named.** A literal in a layout or an animation is a named
  constant with a comment explaining the value, not a number in the middle of a
  function.
- **The domain module has no Android types.** `core:model` is plain Kotlin and
  stays that way.
- **A new module gets a convention plugin**, not a copy of another module's
  build file.

## Verification

```bash
./gradlew test                              # unit tests
./gradlew lint                              # Android Lint; an error fails the build
.github/scripts/check-commit-subjects.sh    # commit subjects
```

All three run on every pull request and all three must pass before a change can
reach `main`.

The last of the three can also run while a message is being written rather than
after it has been pushed:

```bash
git config core.hooksPath .githooks
```

That points the checkout at `.githooks/commit-msg`, which applies the rule out
of the same file the pipeline reads. A subject caught there costs a retyped
line. The same subject caught after a push costs a rewritten history.

Beyond that, this project holds one rule above the rest:

> **Run it on the screen before saying it is done.**

A change that compiles, passes its tests and has never been looked at is not
finished. Install it, open the screen it touches, and say in the pull request
what you saw. Where a change is about layout, look at it at a font scale of 1.8
and on a window turned on its side, because those are where layouts break.

Tests are most valuable when they check a result against something other than a
previous run. The solar geometry is checked against astronomy, the palette
against a contrast threshold, and the parser against a response captured from
the live service — not against fixtures written by whoever wrote the parser.

## Releasing

Releases are cut from `main` by pushing a tag. The version lives in
`gradle.properties` and nowhere else, and the pipeline refuses to publish a tag
that disagrees with it. [docs/RELEASE.md](docs/RELEASE.md) has the full
procedure, including what happens to the signing key.

## Reporting things

- **A defect or an idea:** open an [issue](https://github.com/OzcanOrhanDemirci/weather_app/issues).
- **A security problem:** do not open an issue. [SECURITY.md](SECURITY.md) says
  what to do instead.
- **Behaviour:** everyone taking part is held to the
  [Code of Conduct](CODE_OF_CONDUCT.md).
