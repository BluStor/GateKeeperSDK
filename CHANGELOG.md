# Changelog

## [v0.18.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.18.1)
* Include `proguard-rules.pro.example` and SDK install instructions

## [v0.18.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.18.0)
* Add `GKCardSettings.getCardSettings` and `GKCardSettings.updateCardSettings` which act on `GKCardConfiguration` objects
* Add `GKCard.Response.readDataFile` to read small data files to strings

## [v0.17.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.17.0)
* Renames main library package from `app` to `gatekeeper-sdk` to be consistent with naming practices
* Add `./bin/installLocal.sh` and `./bin/publishRelease.sh` to handle publishing package to local and remote maven repositories
* Update release and development instructions in the README

## [v0.16.3](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.16.3)
* Write downloaded files to temp files, then copy to the final location
    * This prevents partially downloaded files from appearing in the device Downloads directory

## [v0.16.2](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.16.2)
* Fully disconnect on IOExceptions and verify connection before all card calls

## [v0.16.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.16.1)
* Put packet size back to 512B to speed up uploads

## [v0.16.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.16.0)
* write all data from data channel directly to file, do not store in buffer
    * This caused an issue where large enough files would blow up the heap, which is limited on most
     android devices. This replaces all of the threaded packet buffering with simply writing
     commands and reading responses in a synchronous manner.
* `GKBluetoothCard` requires a dataCacheDir to store downloaded data in, which should normally be `context.getExternalCacheDir`

## [v0.15.2](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.15.2)
* Return `SUCCESS` status for 226 code in `GKCardSettings`

## [v0.15.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.15.1)
* Fix issue where `GKCardSettings.getFirmwareInformation` would throw exception if response was not success

## [v0.15.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.15.0)
* Add the file size to `GKFile` and populate it on `GKAuthentication.listFiles` commands

## [v0.14.2](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.14.2)
* Lower packet size from 512 bytes to 256 bytes

## [v0.14.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.14.1)
* Fix GKFileActions.listFiles to return an empty list if the response data is null

## [v0.14.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.14.0)
* Prepare SDK for production releases
    * bump android support-annotations version
    * bump gradle version to 2.10.0 (android gradle version 2.0)
    * add library manifests to excluded files in packagingOptions, so consumers of this SDK no longer need to put the following in their build.gradle
        * ```
            packagingOptions {
                exclude 'META-INF/LE-832C0.RSA'
                exclude 'META-INF/LE-832C0.SF'
            }
        ```

## [v0.13.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.13.0)
* Updates all relevant Neurotech libraries to version 6.0 revision 146188 (March 18, 2016)
* Adds the following functions in GKFaces to extract templates using the camera
    * `setFaceCaptureView`
    * `startCapturing`
    * `captureImage`
    * `finishCameraCapture`

## [v0.12.2](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.12.2)
* Remove unnecessary steps in license validation for enterprise license

## [v0.12.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.12.1)
* Create new bluetooth socket connection if the current socket is no longer connected

## [v0.12.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.12.0)
* Change `GKAuthentication` enroll/revoke face/recoveryCode id from int to String

## [v0.11.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.11.0)
* Change `GKEnvironment` and `GKLicensing` to handle license files being stored on the card
* `GKEnvironment.InitializationListener` now has 3 new callbacks
    * `void onNoLicensesAvailable();` is called when all of the license files are used
    * `void onLicenseValidationFailure();` is called when the license validation fails, based on return values from biometric libraries
    * `void onLicenseValidationError();` is called when there is an exception thrown anywhere along the way, or if the calls to the card return non-SUCCESS statuses

## [v0.10.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.10.1)
* Fix data upload
    * Previously, when calling GKBluetoothCard.put data would be sent in chunks of 512 bytes. The method
    used to buffer this data did not reset the buffer or send the correct size of the data, causing
    any data that was longer than 512 bytes (and not a multiple of 512) to get garbage data in the last
    `N - (N % 512)` bytes.
* Rename `GKBluetoothMultiplexer` to `GKMultiplexer` and make it generic enough to handle IO streams only
* Add `GKMultiplexer.writeToDataChannel(InputStream inputStream)` to handle chunked data transfer

## [v0.10.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.10.0)

* Rename `GKAuthentication.enrollWithPin` to `GKAuthentication.enrollWithRecoveryCode`
* Rename `GKAuthentication.signInWithPin` to `GKAuthentication.signInWithRecoveryCode`
* Rename `GKAuthentication.revokePin` to `GKAuthentication.revokeRecoveryCode`
* Rename `GKAuthentication.listPinTemplates` to `GKAuthentication.listRecoveryCodeTemplates`
* Add `GKCardSettings.getFirmwareInformation`
* Update recovery code request paths to match firmware version 5.0

## [v0.9.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.9.0)

* Rename `GKAuthentication.listTemplates` to `GKAuthentication.listFaceTemplates`
* Add `GKAuthentication.listPinTemplates`
* Make `GKAuthentication.AuthResult` static
* Call `GKCard.connect` before every service call

## [v0.8.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.8.0)

* Rename packages to `co.blustor.gatekeepersdk`

## [v0.7.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.7.0)

* Add SDK methods for enrolling, signing in, and revoking with PIN templates
* Update routes for firmware versions 3.0/4.0 (no functional change between 3.0 and 4.0)

## [v0.6.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.6.1)

* `GKStringUtils.join()` now takes an array of String objects.
* Javadocs have been created.
* Build instructions have been updated.

## [v0.6.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.6.0)

* Templates can be created from image File objects.

## [v0.5.1](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.5.1)

* A Changelog and README have been created.

## [v0.5.0](https://github.com/BluStor/GateKeeperSDK/releases/tag/v0.5.0)

* The GateKeeper SDK is now versioned.

# SDK/Firmware Compatibilities

| SDK Version | Firmware Version |
| :---------: | :--------------: |
| 0.10.0 | 5.0 |
| 0.9.0 | 4.0 |
| 0.8.0 | 4.0 |
| 0.7.0 | 4.0 |
| 0.6.1 | 2.0 |
| 0.6.0 | 2.0 |
| 0.5.1 | 2.0 |
| 0.5.0 | 2.0 |
