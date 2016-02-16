# GateKeeper SDK

## Releases

To release a build, perform the following steps:

### Document Changes

1. Satisfy any pull requests.
2. Switch to the "staging" branch.
3. Ensure that the branch is up-to-date with the "master" branch.
4. Update `build.gradle` file with the next appropriate Version and Build
   Numbers.
5. Update the Changelog (`CHANGELOG.md`) with relevant notes (as needed).
6. Commit Changelog changes with `Release Version <x.x.x>` as the commit
   message.
7. Merge this branch with master.

### Build the SDK

1. Run `bin/build.sh` from the project root path.
2. Locate the `gatekeepersdk-<x.x.x>.aar` file in the `builds` path.
3. Archive the `.aar` file with a `.zip` file of the same name (e.g.
   `gatekeepersdk-1.12.9.aar.zip`).
4. Locate the `gatekeepersdk-<x.x.x>` folder in the `builds` path.
5. Archive this folder with a `.zip` file of the same name, plus `.docs` before
   the extension(e.g. `gatekeepersdk-1.12.9.docs.zip`).

### Creating a Release on Github

1. Draft a new release with `v<x.x.x>` (e.g. `v1.12.9`) as the "Tag Version" and
   "Release Title."
2. Set the "Target" to the merge commit for this release on master.
3. Add any Changelog notes for this release to the Release Description.
4. Attach the `.aar` and `.docs` archives as binaries.
5. Publish.

### Versioning

This SDK uses [Semantic Versioning](http://semver.org/). A version number for
this project is modeled as `MAJOR.MINOR.PATCH`. When committing code to this
project, observe the following guidelines to properly update the SDK version.

#### Patch Versions

When fixing bugs, optimizing performance, or adjusting the SDK in a way which
does not add or change behavior, this can be considered a patch. Increment the
patch version number by one (e.g. `1.12.9` becomes `1.12.10`). Patches must
preserve backwards compatibility.

#### Minor Versions

When behavior in the SDK is changed in a way that supports previous behavior and
does not force consumers of the SDK to change, this can be considered a minor
change. Increment the minor version number by one and reset the patch version
number to zero (e.g. `1.12.9` becomes `1.13.0`). Minor updates must preserve
backwards compatibility.

#### Major Versions

When behavior in the SDK is changed in a way which forces consumers of the SDK
to change, this breaks backwards compatibility. This constitutes a major change
to the SDK. Increment the major version number by one, and reset both the minor
and patch version numbers to zero (e.g. `1.12.9` becomes `2.0.0`).

Whenever possible, major releases should be reserved for substantial or complete
revisions to the SDK and underlying GateKeeper Card API.

SDK Versions should be maintained in relation to the GateKeeper Card Firmware
Version, which indicates which actions can be performed with the Card and how
the related requests must be formed.

### First Public Release

The primary compatibility goal of the first public release should be to ensure
that all 1.x.x versions of the SDK support all actions of the GateKeeper Card
API between its first public release and onward.

It is expected that multiple major revisions will necessitate deprecation and
deliberate incompatibilities between the SDK and the API. Any strategy employed
to address the functional breaks in behavior should do so gracefully. All method
calls within the SDK should succeed or fail without surprising the developer
employing it.
