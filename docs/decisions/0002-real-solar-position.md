# 2. The sun is computed, not faked

## Context

The backdrop needs to know where the sun is, in order to place it, to decide how
much light there is, and to colour the sky accordingly.

Open-Meteo already returns a sunrise and a sunset for each day. Interpolating a
disc between them along a fixed arc would have been a few lines.

## Decision

The position of the sun is computed from the algorithm published by the NOAA
Global Monitoring Laboratory, using latitude, longitude and the moment. The moon
is placed from its phase relative to the sun, treating its orbit as lying in the
plane of the ecliptic.

## Consequences

The interpolated arc would have been wrong in ways a viewer can see rather than
ways only an astronomer can. It is too high in winter and too low in summer,
because its height does not depend on the declination of the day. It is
symmetric about noon, which the real one is not. And it is the same shape
everywhere, when the whole character of a sky at 69 degrees north is that the
shape is different.

The cost is a few dozen trigonometric calls per frame, which is nothing.

It is also what makes the hourly curve work. Dragging to three in the morning
has to put the sun somewhere specific and below the horizon, and a disc
interpolated between today's sunrise and sunset has nothing to say about that
moment.

The tests hold the implementation to astronomy rather than to a previous run:
the maximum height of the sun, the meridian it stands on, an arctic midsummer
that never sets, a southern midday that stands to the north, and an equinox at
the equator that splits the day in half.

The moon is accurate to a few degrees rather than a few minutes of arc, which is
invisible on a disc a few millimetres across. What is visible, and correct, is
that a crescent hangs near the horizon after sunset while a full moon climbs
across the whole night.
