# 1. Contrast is computed, not chosen

## Context

The palette of this application is generated. Every colour comes from the height
of the sun and the weather at one place, interpolated over a continuum, so there
is no fixed set of values that a designer could check once and sign off.

Content still has to be legible on all of it, from a white overcast noon to a
black clear midnight.

## Decision

Content sits on a translucent pane, never directly on the sky, and the opacity
of that pane is calculated rather than chosen. `SkyPaletteFactory` asks for the
lowest opacity that still reaches the Web Content Accessibility Guidelines
minimum for body text over that particular sky, and uses it.

The pane is always dark and never light, and it keeps the hue of the sky it
covers rather than being neutral.

A test walks four cities, four dates, every hour of the day and every weather
code Open-Meteo publishes. That is 10,752 generated palettes. It fails if any of
them puts content below the threshold, and separately if any of them needs a
pane opaque enough to hide the weather behind it.

## Consequences

The pane is as thin as it can be, so the weather stays visible through it, and
it thickens automatically when the sky brightens.

The test found a real defect before it could be seen anywhere. The first version
used a light pane at night. Content is near white, so raising the opacity of a
light pane moves it *towards* the text rather than away from it: the search for
a sufficient opacity was walking in the wrong direction and terminating at its
own upper bound. It surfaced at a snowy dawn in Izmir, which is not a
combination anybody would have thought to look at.

A second requirement was added later that the contrast calculation knows nothing
about. A pane also has to scatter what is behind it, and at the minimum opacity
a bright disc keeps its edge. That is handled by a fixed addition on top of the
computed value, and by the rule in
[decision 4](0004-one-ambient-sky.md) that a sky behind content carries light
rather than objects.
