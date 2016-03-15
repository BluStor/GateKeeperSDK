# Changelog

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
