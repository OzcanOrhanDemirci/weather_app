# 4. One sky, held above navigation

## Context

At first every screen built its own backdrop. It worked for the list and for the
detail screen, and it produced two problems.

A screen that did not build one had none: the search field opened onto a flat
grey rectangle, because `HavaTheme` fell back to its placeholder palette with
nothing drawn behind it.

And moving between screens restarted the weather. Two skies crossfaded past each
other, which is not what the interface is trying to say.

## Decision

There is one `AmbientSkyState`, held above the navigation graph. It is drawn
once, and every screen is placed on it.

Screens state which weather they want to be seen under, through `SkyOf`. A
screen with no opinion keeps whatever the reader arrived with.

## Consequences

Moving from the list to a city, or from a city to the search field, no longer
restarts anything. The sky is the same sky and the reader moved inside it, which
is why the transition between screens is a scale rather than a slide: a
horizontal slide would move a backdrop that has not changed.

Every surface is lit by the same conditions. A card, a chart and the navigation
bar cannot end up under different weather.

One canvas animates instead of four.

It also settled a rule that had been implicit. A sky standing behind content is
doing a different job from one a reader is looking at directly: it sets the
colour of everything on top of it. Anything sharp in it shows through the gap
between two cards, or through a chart, as a bright fragment that reads as a
defect rather than as the sun. The `Backdrop` level of detail therefore carries
light and not objects: the gradient, the stars, the cloud and the rain, with the
glow of the sun standing in for its disc.
