# 6. The sky takes its time, except when a finger is on it

## Context

The first version animated only the palette, at the same speed everything else
in the application uses, and left every other quantity to change on the frame
the conditions did.

Two things were wrong with that, and both were reported from a real device
rather than found here.

Colours arrived in under half a second, which is the right speed for a control
answering a tap and the wrong speed for daylight becoming overcast. The change
read as a cut.

And it was only the colours. Cloud appeared, rain started, stars arrived and the
arrangement of both was replaced outright, all on one frame, while the colours
were still travelling. Half a transition looks worse than none.

Scrolling the list quickly made it plain. The backdrop follows the card nearest
the middle of the screen, so a flick past six cities handed it six new places in
under a second. Each one restarted a half second animation that never finished,
and the screen stepped through a series of unfinished skies.

## Decision

Four changes, and one thing deliberately left fast.

**Every quantity that separates one sky from the next is animated**, not only
the colours: how much cloud there is, how hard it is raining or snowing, how far
night has fallen, where the sun sits, how much of it is hidden. Rain and snow
are carried as two separate amounts rather than as a type and a strength,
because a type cannot be interpolated; as amounts, one thins out while the other
thickens and for a moment both are falling, which is what happens outside.

**The backdrop keeps one arrangement of stars and cloud.** It used to be seeded
from the coordinates, so scrolling past a different city rearranged the sky.
Cards still get their own, because twenty cards should be twenty skies, but the
backdrop is a single sky the reader is standing under and its stars should not
move when they look somewhere else.

**The journey takes nearly two seconds.** A critically damped spring at a
stiffness of 14 settles in about 1.8 seconds, which is long enough to read as
movement and short enough that a deliberate scroll is not left waiting.

**The sky moves only once the list has stopped moving past it.** The focused
card has to hold the middle of the screen for four hundred milliseconds before
the backdrop is given it. A slow scroll clears that easily and is followed; a
flick never does, and becomes one clean change when it lands.

**A scrub stays fast.** Dragging the hourly curve is direct manipulation: the
reader is holding the hour, and a backdrop arriving two seconds later has
stopped answering them. That path settles in a third of a second.

## Consequences

Telling the two apart was the interesting part. The first attempt started a
timer whenever the place changed and treated everything for the next few seconds
as part of the journey. It was wrong in a way that only shows up in use: opening
a city and immediately dragging the curve meant the drag inherited the slow
speed and lagged a second behind the finger.

What is remembered instead is the hour the place was arrived at. While the
current hour still matches it, nothing has been scrubbed and the sky is
travelling. The moment it differs, the reader is moving time and the sky follows
them directly. No timer, and no state that can be left set.
