# GateKeeper SDK

## Using GateKeeperSDK

* Add the following dependency to your `build.gradle`:

```
compile 'co.blustor:gatekeeper-sdk:0.18.1'
```

* Copy the contents of `gatekeeper-sdk/proguard-rules.pro.example` to your `proguard-rules.pro`
* Android Support Repository must be installed to use the maven features. Importing the project into Android Studio, the build should fail, and give you the option to install "Android Support Repository". For more information, refer to [the Android docs](https://developer.android.com/topic/libraries/support-library/setup.html)
* If developing an application targeting Android Marshmallow (v6.0 / API level 23) or later, you will need to [request some permissions at runtime](https://developer.android.com/training/permissions/requesting.html):
    * `android.permission.CAMERA`
    * `android.permission.READ_PHONE_STATE`
    * `android.permission.RECORD_AUDIO`
    * `android.permission.WRITE_EXTERNAL_STORAGE`
    * `android.permission.WRITE_SETTINGS`

## Development

The GateKeeperSDK is distributed via [maven repositories](https://maven.apache.org/guides/introduction/introduction-to-repositories.html).
Releases are published to [Bintray](https://bintray.com/) and distributed through the [jcenter](https://bintray.com/bintray/jcenter)
repo for wide use. To develop locally, the GateKeeperSDK should be installed to your local maven repository.
To do this:

* Change the `artifact_version_name` in `gradle.properties` to not conflict with a published SDK version, ex:

```
artifact_version_name=my-test-version
```

* Run `./bin/installLocal.sh` from the project root
```
# You must be in the GateKeeperSDK directory or you may not find `gradlew`
cd GateKeeperSDK
./bin/installLocal.sh
```
* In the project which depends on GateKeeperSDK, add `mavenLocal()` to the `repositories` section in `build.gradle`, ex:

```
allprojects {
    repositories {
        jcenter()
        mavenLocal()
    }
}
```

* Update your project's `build.gradle` to point to the locally installed version of the SDK, ex:

```
dependencies {
    compile 'co.blustor:gatekeeper-sdk:my-test-version'
}
```

## Releases

To release a build, perform the following steps:

### Document Changes

* Satisfy any pull requests.
* Switch to the "staging" branch.
* Ensure that the branch is up-to-date with the "master" branch.
* Update `gradle.properties` file with the next appropriate `artifact_version_name` and `artifact_version_code`numbers.
```
cd /<your path>/GateKeeperSDK
vi gradle.properties
  
  # Example values:
  artifact_version_name=0.20.0
  artifact_version_code=34
```
* Update the [usage instructions above](#using-gatekeepersdk) with the latest version
* Update the Changelog (`CHANGELOG.md`) with relevant notes (as needed).
* Commit Changelog changes with `Release Version <x.x.x>` as the commit
   message.
* Merge this branch with master.

### Build the SDK

* Make sure you are on the "master" branch and you have all the changes you wish to release
* Set the `bintray_user` and `bintray_api_key` to the correct [Bintray](https://bintray.com/) user name and API key in `local.properties`
```
// To get your API Key, open the website and login: https://bintray.com/blufrog.
// Click "Edit" profile.  Left side navigate to "API Key".
// Enter your bintray password
// Copy the key to your clipboard and then add to local.properties.
// See next step for location of property file

// Note: This file is managed by Android Studio and all changes will be lost
// You have to set these properties each time you build
  
  cd /<your path>/GateKeeperSDK
  vi local.properties

   bintray_user=blufrog
   bintray_api_key=<key not shown>
```
* Run `bin/publishRelease.sh` from the project root path.
* Done. The version specified in `gradle.properties` will now be available to install from the jcenter repository.

### Creating a Release on Github

* Draft a new release with `v<x.x.x>` (e.g. `v1.12.9`) as the "Tag Version" and
   "Release Title."
* Set the "Target" to the merge commit for this release on master.
* Add any Changelog notes for this release to the Release Description.
* Publish.

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
