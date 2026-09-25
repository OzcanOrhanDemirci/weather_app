# 5. Compiled against an SDK that has actually shipped

## Context

The newest AndroidX releases at the time of writing, including Compose 1.12 and
Lifecycle 2.11, declare that they require `compileSdk 37`.

Android SDK Platform 37 has not been published. It is not in the stable channel,
the beta channel or the canary channel.

## Decision

`compileSdk` stays at 36, and every dependency is pinned to the newest version
that compiles against it: Compose through the 2026.06.01 bill of materials,
Lifecycle 2.10.0, Navigation 2.9.8, Hilt Navigation Compose 1.3.0.

The network engine is the platform one rather than OkHttp for the same reason:
the current OkHttp release also requires SDK 37, and this application makes two
kinds of plain read, so none of what OkHttp adds is needed here.

## Consequences

A repository that only builds on the machine it was written on is not a
repository. Depending on an unreleased SDK would mean nobody could clone this
and build it, and the continuous integration job could not run at all.

The cost is a few months of AndroidX. Nothing in this application uses an API
that arrived in those months.

The pinning is not silent. Every version is in one catalog, the reason is here,
and raising them is a single edit once Platform 37 ships.
