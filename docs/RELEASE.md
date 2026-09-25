# Releasing

## What the pipeline does

Pushing a tag that starts with `v` runs `.github/workflows/release.yml`, which:

1. Checks that the tag matches `hava.versionName` in `gradle.properties`, and
   stops if it does not.
2. Runs the unit tests.
3. Restores the signing key from a repository secret into the runner's
   temporary directory.
4. Assembles the release build: minified, resources shrunk, signed.
5. Confirms which key was used by reading the certificate out of the finished
   package, and stops if it is not the release one.
6. Publishes a pre-release with the package attached.
7. Keeps the mapping file as a build artefact for ninety days.
8. Deletes the key.

```bash
git tag v1.0.0
git push --tags
```

## Why the tag is checked against the source

The tag is what people quote. The version inside the package is what a device
compares against when deciding whether an update is newer. A release where those
two disagree is worse than no release, because it is wrong in a way nobody
notices until an update silently refuses to install.

They come from one place, `gradle.properties`, and the pipeline refuses to
publish a tag that disagrees with it.

## Why the certificate is checked

A package signed with the debug key installs and runs, and is quietly the wrong
package: it cannot update an installation of the real one, and the mistake only
becomes visible to whoever already has it installed. The pipeline reads the
certificate out of the finished package and stops if it is not the release key.

## The key

The signing key is not in this repository and never has been. It is read from
`keystore.properties`, which is ignored, or from four environment variables:

| Variable | Meaning |
| --- | --- |
| `HAVA_KEYSTORE_FILE` | Path to the key store |
| `HAVA_KEYSTORE_PASSWORD` | Its password |
| `HAVA_KEY_ALIAS` | The alias inside it |
| `HAVA_KEY_PASSWORD` | The password of that alias |

On the build server the store itself comes from a repository secret holding it
base64 encoded, and is written to the runner's temporary directory for the
length of the job.

When none of this is present, the release build is signed with the debug key.
That is deliberate: anyone can clone this repository and produce a working
package. What they cannot produce is one that updates an installation of the
real one.

**If the key is lost, this application can never be updated again.** Not by
anyone, including its author. It is worth more than the source, because the
source can be rewritten.

## Building a release by hand

```bash
./gradlew :app:assembleRelease
```

The result is at `app/build/outputs/apk/release/app-release.apk`. Confirm what
signed it before giving it to anyone:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## Never send a debug build to be judged

A debug build carries extra checking, has no minification, and animates worse
than the real thing. Asking someone whether the motion is smooth, and handing
them a debug package to answer with, produces a report about a package nobody
will ever install.
