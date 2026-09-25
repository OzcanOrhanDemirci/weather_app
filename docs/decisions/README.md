# Decisions

Architecture decision records. One file per decision, numbered in the order it
was taken, never rewritten once merged.

A record exists for a decision that was not obvious — where a cheaper or more
common option was available and was rejected. The valuable half of each file is
therefore the alternative, because the diff already shows what was chosen and
nothing shows what was not.

The form is the same throughout: the context, the decision, the alternatives
that were rejected and why, and the consequences that were accepted along with
it.

| | Decision | In short |
| --- | --- | --- |
| 1 | [Contrast is solved, not chosen](0001-contrast-is-computed.md) | The palette is generated, so its legibility cannot be approved once by eye. The pane that carries content is darkened by the least opacity that still reaches the guideline ratio over that particular sky, and a test walks 10,752 skies looking for one that fails. |
| 2 | [The sun is computed, not faked](0002-real-solar-position.md) | Sliding a disc between a reported sunrise and sunset is far less work and is wrong in every way a viewer can see. |
| 3 | [A forecast is stored as it arrived](0003-store-the-response.md) | The response is read as a whole and replaced as a whole, so normalising it into tables would add a second way to turn the wire format into the domain. |
| 4 | [One sky, above navigation](0004-one-ambient-sky.md) | Moving between screens should not restart the weather. The backdrop belongs to the window, not to the page. |
| 5 | [Compiled against a released SDK](0005-released-sdk-only.md) | The newest AndroidX requires an SDK that has not shipped. A repository that only builds on one machine is not a repository. |
| 6 | [The sky takes its time](0006-the-sky-takes-its-time.md) | A change of weather is not a control answering a tap. Arriving somewhere new takes nearly two seconds; following a finger along the hourly curve takes a third of one. |

## Adding one

Write a record when a choice will look wrong to someone who arrives later
without the context — and that includes yourself in a month. A decision that
anyone would make the same way does not need a file.

Copy the shape of an existing record, take the next number, and add a row above.
Once a record is merged it is not edited: a decision that has been superseded
gets a new record saying so, because the point of the file is what was known at
the time.
