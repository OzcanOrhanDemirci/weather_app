# Architecture

## Shape

Fifteen modules in three tiers. Dependencies point downwards only; nothing in a
`core` module knows that a `feature` module exists.

```
                       app
                        │
        ┌───────────────┼───────────────┬───────────────┐
   feature:cities  feature:detail  feature:favorites  feature:search
        └───────────────┴───────┬───────┴───────────────┘
                                │
                            core:ui ────────── core:sky
                                │                  │
                            core:data          core:designsystem
                       ┌────────┼────────┐         │
                 core:network core:database core:common
                       └────────┴─────────┴─────────┘
                                │
                            core:model
```

`core:model` is a plain Kotlin module with no Android types at all. Keeping the
domain free of the platform is what lets the rules that carry the most meaning,
such as the mapping of World Meteorological Organization codes, be tested in
milliseconds without a device.

## Where a forecast comes from

```
Open-Meteo
   │  one HTTP request, verbatim
   ▼
ForecastDocument ──────────► ForecastEntity          (stored as it arrived)
   │                              │
   │        ForecastReader        │  the only place the wire format is read
   ▼                              ▼
              WeatherSnapshot  (the domain)
                     │
                     ▼
              CachedForecast   (+ when it arrived)
                     │
                     ▼
                  ViewModel ──► UiState ──► Screen
```

The document is kept exactly as the service sent it, so a forecast read back
after a week goes through the same code as one that arrived a second ago. There
is one path from the wire format to the domain, which means the two can never
drift apart. The reasoning is in
[decision 3](decisions/0003-store-the-response.md).

## Reading and refreshing are separate

`WeatherRepository` exposes what the device holds as a `Flow` and refreshing as
a suspending call that returns an `Outcome`.

That separation is the whole of the offline story. A screen observes storage, so
it has something to show the instant it opens and keeps showing it when the
network is gone. Refreshing is an action with a result, so a failure can be
reported *next to* content rather than instead of it.

It is why the list can say "No connection. Showing what was last stored on this
device." above twenty working cities, which is the behaviour most weather
applications get wrong.

## State on a screen

Each screen has one sealed state type with a case per thing it can be showing.
They are separate types rather than one object with flags, so a screen cannot
render a combination that was never meant to exist.

```kotlin
sealed interface CitiesUiState {
    data object Loading
    data object Empty
    data class Failed(reason)
    data class Content(cities, isRefreshing, problem)
}
```

The one combination that does exist is `Content` with a `problem`. That is not
an error state: the weather on screen is still true, only older than intended,
and replacing it with an apology would remove the one thing the reader came for.

## The sky

`core:sky` is the largest single idea in the project and has its own shape.

| Piece | Responsibility |
| --- | --- |
| `SolarGeometry` | Where the sun is, from the NOAA algorithm |
| `LunarGeometry` | Where the moon is and how much of it is lit |
| `SkyPaletteFactory` | Conditions to colour roles, with contrast solved |
| `layer/*` | Drawing: gradient, stars, luminary, cloud, precipitation, fog, lightning |
| `Sky` | The composable that orders those layers |
| `AmbientSkyState` | The one sky the whole application stands under |

Everything except the drawing is pure arithmetic with no Android types, which is
why a visual feature has 12 unit tests.

Three levels of detail exist, and the difference between them is not only cost:

- **Full** for a sky nothing is placed over.
- **Miniature** for a card, drawn many times at once.
- **Backdrop** for a sky with content on top of it. It carries light, not
  objects: stars read as texture through a translucent pane and rain is in
  motion, but the disc of the sun keeps its edge and appears through a chart as
  something that looks like a defect.

## Drawing within a frame

Two rules keep the backdrop cheap enough to animate under everything else.

**Particles are grouped and drawn in one call per group.** Stars are banded by
brightness, precipitation by depth. A full night sky costs eight draw calls and
a heavy downpour costs three, rather than several hundred.

**Position is a function of elapsed time, never of the previous frame.** A
dropped frame skips ahead instead of falling behind, and the same field can be
drawn from any moment. That second property is what makes the hourly curve
draggable: the rain at three in the morning is not something the animation has
to travel to.

## Build

Convention plugins in an included `build-logic` build own everything modules
share: SDK levels, the Java level, the JVM toolchain, Compose, Hilt. A module
build file states what the module is and what it depends on, and nothing else.

The JVM toolchain is pinned rather than inherited from whichever JDK runs
Gradle, so a workstation, the build server and a second machine produce the same
output.

## Injection

Hilt, with one component. Dispatchers and the clock are injected rather than
read from statics, because everything here is a function of the time: which
hours the forecast shows, whether the cache is stale, where the sun is. A test
that cannot choose the moment can only assert that something happened, not that
the right thing happened at the right time.
