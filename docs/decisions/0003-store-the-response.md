# 3. A forecast is stored exactly as it arrived

## Context

Forecasts have to survive on the device so the application opens with weather on
screen and keeps working without a network.

The usual approach is to normalise: a table for the current conditions, one for
the hours, one for the days.

## Decision

The response is stored verbatim, as text, in one row keyed by city, alongside
the moment it arrived. Reading it back goes through exactly the same parser that
reads a fresh response.

## Consequences

A forecast is read as a whole and replaced as a whole. It is never queried by
hour and never joined against anything, so three tables would buy nothing.

More importantly, normalising would create a second path from the wire format to
the domain: one for the response and one for the rows. Two paths can disagree,
and the disagreement would only appear for cached data, which is the hardest
case to reproduce. There is one path, so a document read back after a week
cannot be interpreted differently from one that arrived a second ago.

The cost is that a stored forecast cannot be queried by SQL. Nothing in this
application wants to.

Deciding which cities are due for a refresh does not read the documents: a
separate query returns the identifiers and arrival times only, because reading
twenty full responses to compare timestamps would be several hundred kilobytes
for two columns.
