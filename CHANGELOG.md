# Changelog

Every released version, what changed in it and why.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and
the numbering follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Each version corresponds to a `v`-prefixed tag, and each tag produced a signed
package through the release pipeline described in
[docs/RELEASE.md](docs/RELEASE.md).

Every version below was released on the same day. The application was written
during the Türkcell Kamp+ information technology camp, and the dates are what
they are.

## [Unreleased]

Nothing yet.

## [1.2.1] — 2026-09-25

### Fixed

- The humidity reading was the only value on the detail screen assembled in
  Kotlin rather than read from a string resource, so it printed `40%` in every
  language. Turkish writes the sign before the number, and now so does the
  application.

### Removed

- Two strings nothing read: a tagline replaced by the subtitle on the list, and
  a description for the sky. The sky is decorative and is correctly silent to a
  screen reader, because the weather it draws is written out in words beside it.

## [1.2.0] — 2026-09-25

### Added

- A launcher icon. There was none, so every device fell back to the platform
  default and the application could not have been submitted to a store. It is an
  adaptive icon drawn as vectors with a themed monochrome layer, and its colours
  are read from the daylight table the sky engine already uses rather than
  matched by eye.
- A kept city is now marked in the list as well as on its own screen, and the
  screen reader description says so.
- Eleven previews covering the four list states, a failed refresh over stored
  weather, a long place name, a narrow screen and a font scale of 1.8.

### Changed

- Wide windows are laid out rather than stretched. Cities and favourites share
  one grid whose column count follows from the narrowest width a card still
  reads at, so an upright phone is unchanged and a turned one shows two columns.
  A window wider than 720dp shows a city as two panes, with the reading held in
  place while the forecast scrolls beside it.
- Search stops widening at a readable column, and the navigation bar stops
  growing before the window does.
- Screens are held clear of a display cutout sideways, while the sky still runs
  underneath it.

### Fixed

- Hour labels on the temperature curve are spaced by measurement rather than by
  a fixed step of three hours. At a font scale of 1.8 the first two labels used
  to close up and read as a single four digit number.

## [1.1.0] — 2026-09-25

### Changed

- The sky now travels between states instead of switching between them.
  Scrolling the list quickly used to restart the transition every few frames, so
  the weather never resolved and the screen flickered through a dozen half
  finished skies. Arriving somewhere new now takes nearly two seconds, while
  following a finger along the hourly curve takes a third of one, because those
  are two different things and only one of them is an answer to a gesture.

## [1.0.0] — 2026-09-25

The first complete application: twenty cities, a detail screen, favourites,
search, and a release pipeline that signs and publishes what it built.

### Added

- **Twenty cities**, each drawn under its own sky, with the backdrop following
  whichever card the reader has settled on.
- **A city in full**: a draggable hourly curve, the sun's arc for the day, the
  week ahead and the current readings.
- **Favourites** and **search**, the latter debounced so that a word is one
  request rather than six.
- **An offline-first data layer.** A forecast is stored exactly as the service
  sent it, so the application opens with weather on screen before the network
  answers and keeps working when the network is gone.
- **The sky engine**: solar position from the algorithm published by the NOAA
  Global Monitoring Laboratory, lunar phase drawn with the shape the moon
  actually has, and a palette generated from the two rather than chosen from a
  set of themes.
- **A generated palette whose contrast is solved rather than approved.** A test
  walks 10,752 skies and fails if any of them puts content below the legibility
  threshold.
- **Thirteen modules** wired by convention plugins, so adding one does not mean
  copying configuration that then drifts apart.
- **A verification pipeline** on every push and pull request, and a release
  pipeline that refuses to publish a tag disagreeing with the declared version,
  or a package signed with the wrong key.

[Unreleased]: https://github.com/OzcanOrhanDemirci/weather_app/compare/v1.2.1...HEAD
[1.2.1]: https://github.com/OzcanOrhanDemirci/weather_app/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/OzcanOrhanDemirci/weather_app/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/OzcanOrhanDemirci/weather_app/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/OzcanOrhanDemirci/weather_app/releases/tag/v1.0.0
