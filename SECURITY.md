# Security

## What this application can reach

Knowing the shape of the application is most of the answer to what can go wrong
with it, so it is worth stating plainly.

- **There is no backend.** Nothing in this repository runs on a server. The
  application talks to [Open-Meteo](https://open-meteo.com) and to nothing else.
- **There is no account, no login and no API key.** Open-Meteo needs none, so
  there is no credential in the application to steal and none in the repository
  to leak.
- **No personal data is collected, stored or transmitted.** Location is never
  requested: a city is chosen from a list or searched for by name, never
  detected. The manifest asks for two permissions, `INTERNET` and
  `ACCESS_NETWORK_STATE`, and nothing else.
- **Everything the application knows is on the device**, in a Room database
  holding the chosen cities and the last forecast for each. It goes away with
  the application.
- **There is no analytics, no crash reporting and no advertising.** Nothing is
  sent anywhere.

The one secret this project has is the release signing key. It has never been in
this repository and never will be. It is read from an ignored
`keystore.properties` or from environment variables, and on the build server it
comes from a repository secret, is written to the runner's temporary directory
for the length of the job and is deleted afterwards.
[docs/RELEASE.md](docs/RELEASE.md) describes this in full.

## Supported versions

The most recent release is the supported one. Older versions receive nothing.

| Version | Supported |
| --- | --- |
| 1.2.x | Yes |
| < 1.2 | No |

Releases are listed on the
[releases page](https://github.com/OzcanOrhanDemirci/weather_app/releases). Each
package is signed with the release key, and the pipeline verifies the
certificate before publishing; a package that was not signed with it is not
published.

## Reporting a vulnerability

**Please do not open a public issue for a security problem.**

Use GitHub's
[private vulnerability reporting](https://github.com/OzcanOrhanDemirci/weather_app/security/advisories/new),
or write to **ozcanorhandem@gmail.com** with `weather_app security` in the
subject.

Please include what you found, the version or commit you found it in, and the
steps to reach it. A report that can be reproduced is worth far more than one
that has to be guessed at.

You can expect an acknowledgement within **three days** and an assessment within
**seven**. If the report is valid you will be told what the fix is and when it
ships, and you will be credited in the
[changelog](CHANGELOG.md) unless you would rather not be.

## Verifying a package

Every release is signed with the same key. Before installing a package that
claims to be this application, check what signed it:

```bash
apksigner verify --print-certs hava-1.2.1.apk
```

The certificate should read:

```
CN=Ozcan Orhan Demirci, OU=Hava, O=Ozcan Orhan Demirci, L=Izmir, ST=Izmir, C=TR
```

A package signed with anything else did not come from here. A debug-signed
package will install and run and be quietly the wrong package: it cannot update
an installation of the real one, and the mistake only becomes visible to whoever
already has it.
