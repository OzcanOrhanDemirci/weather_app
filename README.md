<div align="center">

# Hava

**A weather application for Android, where the backdrop is the data.**

[![Build and verify](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/ci.yml/badge.svg)](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/ci.yml)
[![Release](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/release.yml/badge.svg)](https://github.com/OzcanOrhanDemirci/weather_app/actions/workflows/release.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Min SDK](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Latest release](https://img.shields.io/github/v/release/OzcanOrhanDemirci/weather_app?label=release&color=success)](https://github.com/OzcanOrhanDemirci/weather_app/releases/latest)
[![Licence](https://img.shields.io/badge/licence-MIT-blue)](LICENSE)

**Built at a Youth Camp, in the [Türkcell Kamp+](#where-this-was-built) information technology camp.**

[![Türkcell Kamp+](https://img.shields.io/badge/T%C3%BCrkcell%20Kamp%2B-Information%20Technology%20Camp-FFC72C?labelColor=1a1a1a)](#where-this-was-built)
[![Programme](https://img.shields.io/badge/Programme-Mobile%20Development-FFC72C?labelColor=1a1a1a)](#where-this-was-built)
[![Turkcell Akademi](https://img.shields.io/badge/Turkcell-Akademi-FFC72C?labelColor=1a1a1a)](#where-this-was-built)

**[Download the latest package](https://github.com/OzcanOrhanDemirci/weather_app/releases/latest)** · Android 8.0 and above · no account, no key, nothing collected

[The idea](#the-idea) · [Decisions](#decisions-worth-reading) · [Architecture](#architecture) · [What is verified](#what-is-verified) · [Building](#building) · [Accessibility](#accessibility)

*[Türkçe](README.tr.md)*

</div>

---

The forecast comes from [Open-Meteo](https://open-meteo.com), which needs no API
key and no account. There is no backend of its own: everything the application
knows lives on the device, so it opens with weather on screen before the network
answers and keeps working when the network is gone.

| The list | A city | Its night | With no connection |
| --- | --- | --- | --- |
| ![Twenty cities](docs/images/cities.png) | ![A city](docs/images/detail-day.png) | ![The same city at three in the morning](docs/images/detail-night.png) | ![Offline](docs/images/offline.png) |

Turned on its side it is laid out again rather than stretched: the list becomes
two columns, and a city becomes two panes with the reading held in place while
the forecast scrolls beside it.

| Two columns | Two panes |
| --- | --- |
| ![The list on a turned phone, in two columns](docs/images/wide-cities.png) | ![One city on a turned phone: the reading on the left, the forecast scrolling on the right](docs/images/wide-detail.png) |

## Where this was built

> **This application was written at a Youth Camp, during the Türkcell Kamp+
> information technology camp, in the mobile application development programme
> run with Turkcell Akademi.** September 2026.

The camp sets the same exercise for everyone: build **Hava**, a weather
application, in **Kotlin** and **Jetpack Compose**, against the free
**Open-Meteo** service. Three days, five sessions, four checkpoints, one working
application. By the last day each participant should have twenty cities, a
detail screen with hourly and daily forecasts, favourites shown consistently
across screens, the four asynchronous states — loading, content, empty, error —
and a signed release package installed on their own phone.

**This repository is one participant's answer, and it goes past the brief on
purpose.**

The purpose is not to tick the checkpoints. It is to answer a harder question
that the exercise raises but does not ask: *what separates an application that
satisfies a requirement from one someone would choose to keep?* A weather
application is a good place to ask it, because the requirement is small enough
to finish and the craft has no ceiling. So the exercise's list of cities became
twenty live skies computed from astronomy; its state machine became an
offline-first data layer; and its "run it on your phone" became a signed release
pipeline that refuses to publish a package it cannot verify.

Everything the brief asked for is here, and every checkpoint criterion was met
and checked on a running device rather than in the source. What was added on top
is recorded in [docs/decisions](docs/decisions), each entry with the cheaper
option that was rejected and the reason — because a decision without its
alternative is just a preference.

The camp also treats **Git as part of the work**, not as a place to put the work
afterwards. That is why this repository has a linear history of single-purpose
commits, a protected `main`, a verification pipeline on every pull request, and
a release that can only be cut from a tag that agrees with the source. How all
of that is run is in [CONTRIBUTING.md](CONTRIBUTING.md).

## The idea

**The backdrop is the data.**

The sky behind every screen is computed from the weather at that place and the
real position of the sun, using the algorithm published by the NOAA Global
Monitoring Laboratory. The moon is placed by its phase and drawn with the shape
it actually has tonight. Nothing is a stock image and nothing is a preset: there
is no moment at which the application switches from one theme to another,
because there is no theme to switch to.

Two things follow from that, and they are the reason the rest of the code looks
the way it does.

**There are no weather icons. Anywhere.** A card in the list is already the sky
of that place, drawn from the same reading that produced the number beside it. A
small picture of a cloud on top of an actual cloud says nothing the card has not
said better. What is left for words is the part a picture is bad at, which is
how much of it there is, so the text says *heavy rain* rather than *rain*.

**The hourly curve can be dragged, and the world follows.** Moving along it
moves the hour the screen is seen under: the light, the height of the sun, the
stars, the rain. The forecast stops being a table and becomes a day that can be
walked through.

![Six skies](docs/images/sky-engine.png)

## Decisions worth reading

Each of these is recorded in full under [docs/decisions](docs/decisions).

| | |
| --- | --- |
| [Contrast is solved, not chosen](docs/decisions/0001-contrast-is-computed.md) | The palette is generated, so it cannot be approved once by eye. The pane that carries content is darkened by the least opacity that still reaches the guideline ratio over that particular sky. A test walks 10,752 generated palettes and fails if any loses legibility. |
| [The sun is computed, not faked](docs/decisions/0002-real-solar-position.md) | Sliding a disc between a reported sunrise and sunset is much less work and is wrong in every way a viewer can see. |
| [A forecast is stored as it arrived](docs/decisions/0003-store-the-response.md) | It is read as a whole and replaced as a whole, so normalising it would add a second way to turn the wire format into the domain. |
| [One sky, above navigation](docs/decisions/0004-one-ambient-sky.md) | Moving between screens should not restart the weather. |
| [Compiled against a released SDK](docs/decisions/0005-released-sdk-only.md) | The newest AndroidX requires an SDK that has not shipped. A repository that only builds on one machine is not a repository. |
| [The sky takes its time](docs/decisions/0006-the-sky-takes-its-time.md) | A change of weather is not a control answering a tap. Arriving somewhere new takes nearly two seconds; following a finger along the hourly curve takes a third of one. |

## Architecture

Thirteen modules, wired by convention plugins so that adding one does not mean
copying twenty lines of configuration that then drift apart.

```
app  ──────────────  the single Activity, navigation, the ambient sky
│
├── feature:cities      twenty places, each under its own sky
├── feature:detail      one place, hour by hour
├── feature:favorites   the places that were kept
├── feature:search      a place found by name
│
├── core:ui             the shared card, the words for a condition
├── core:sky            the backdrop: astronomy, palette, layers
├── core:designsystem   colour roles, type, motion, glass
├── core:data           repositories, offline first
├── core:network        Open-Meteo, and the only place the wire format is read
├── core:database       Room: places and the last forecast for each
├── core:common         dispatchers and the clock, both injected
└── core:model          the domain, in plain Kotlin with no Android types
```

Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how the layers talk to
each other and why the boundaries are where they are.

## What is verified

```bash
./gradlew test    # unit tests
./gradlew lint    # Android Lint, which fails the build on an error
```

Both run on every push and every pull request. Neither may fail for a change to
reach `main`.

The tests that carry the most weight are the ones that check arithmetic against
something other than a previous run.

- **The sun is checked against astronomy.** At its highest it reaches 90 degrees
  minus the difference between the latitude and the declination of the day, and
  it stands on the meridian. The arctic midsummer sun never sets, the southern
  midday sun stands to the north, and day and night are equal at the equator at
  the equinox.
- **The palette is checked for legibility** across four cities, four dates,
  every hour and every published weather code. That is 10,752 skies, and none of
  them may put content below the contrast threshold or need a pane opaque enough
  to hide the weather.
- **The forecast parser runs against a response captured from the live
  service**, not one written by hand, because a handwritten fixture only tests
  that the parser agrees with whoever wrote it.

Beyond the pipeline there is one rule this project holds above the rest: **run
it on the screen before saying it is done.** Layout work is looked at on a
turned window and at a font scale of 1.8, because those are where layouts break.

## Building

```bash
git clone https://github.com/OzcanOrhanDemirci/weather_app.git
cd weather_app
./gradlew :app:assembleDebug
```

Requires JDK 17 or newer and Android SDK Platform 36. The Gradle toolchain pins
the compiler to JDK 21, so the output is the same on a workstation, on the build
server and on a second machine. There is nothing to configure: no key, no
account, no `local.properties` to fill in by hand.

A release build is signed with a key that is never committed and is read either
from an ignored `keystore.properties` or from the environment. When neither is
present the release is signed with the debug key, so this repository can be
cloned and built into a working package by anyone. What they cannot produce is a
package that updates an installation of the real one.

## Releasing

Pushing a tag builds, tests, signs and publishes:

```bash
git tag v1.0.0 && git push --tags
```

The pipeline refuses to publish a tag that disagrees with the version declared
in `gradle.properties`. A release where the name people quote and the number a
device compares against have drifted apart is worse than no release. It also
reads the certificate out of the finished package and stops if it is not the
release key.

Every version is listed in [CHANGELOG.md](CHANGELOG.md); the full procedure,
including what happens to the signing key, is in
[docs/RELEASE.md](docs/RELEASE.md).

## Accessibility

- Every card is one thing to a screen reader. Read out piece by piece it would
  be a name, then a number, then a word, with nothing to say they belong
  together.
- The setting that removes animation is read once in the theme and exposed to
  every component, so none of them can forget it. Because each layer of the sky
  is a function of one clock, stopping that clock leaves a correct still image
  rather than an empty one.
- Contrast is computed rather than assumed, and the computation is a test.
- The interface is laid out for the window it is in rather than stretched to
  fit: a turned phone shows two columns of cities and a city as two panes, and
  labels that would collide at a large font scale are spaced by measurement.
- Both languages are complete. A string that genuinely cannot be translated is
  marked as such, with a comment saying why.

## Technology

| Concern | Choice |
| --- | --- |
| Language | Kotlin 2.4.20 |
| Interface | Jetpack Compose, Material 3 |
| Injection | Hilt |
| Network | Ktor with the platform engine, kotlinx.serialization |
| Storage | Room |
| Build | Gradle 9.8.0, Android Gradle Plugin 9.4.1, convention plugins |
| Minimum SDK | 26 (Android 8.0) |
| Compile SDK | 36 |

## Working in this repository

| | |
| --- | --- |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Commit style, branch naming, what a pull request has to say, and the rule about running it before calling it done. |
| [CHANGELOG.md](CHANGELOG.md) | Every released version and what changed in it. |
| [SECURITY.md](SECURITY.md) | What this application can reach, and how to report a problem privately. |
| [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) | The Contributor Covenant. |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | The module graph and where the boundaries are. |
| [docs/RELEASE.md](docs/RELEASE.md) | How a release is cut, and what happens to the key. |
| [docs/decisions](docs/decisions) | Why the unusual parts are the way they are. |

`main` takes no direct pushes. Every change arrives through a pull request that
the verification pipeline has passed, and history is kept linear.

## Licence

MIT. See [LICENSE](LICENSE). The bundled Inter typeface is used under the SIL
Open Font License; its terms are in `core/designsystem/licenses`.

Weather data by [Open-Meteo](https://open-meteo.com), used under CC BY 4.0.

## Author

**Özcan Orhan Demirci** · Flutter and Android developer, İzmir ·
[github.com/OzcanOrhanDemirci](https://github.com/OzcanOrhanDemirci)
