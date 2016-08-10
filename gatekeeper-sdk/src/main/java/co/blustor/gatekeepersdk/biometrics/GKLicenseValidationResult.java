package co.blustor.gatekeepersdk.biometrics;

/**
 * Indicates the status of validating licenses
 *
 * @since 0.11.0
 */
public enum GKLicenseValidationResult {
    /**
     * There are no unused licenses left on the card
     */
    NO_LICENSES_AVAILABLE,
    /**
     * The generated license is no longer valid
     */
    VALIDATION_FAILURE,
    /**
     * A license was successfully validated
     */
    SUCCESS,
    /**
     * An error occurred in the process of activating or validating a license
     */
    ERROR,
    /**
     * An IO error occurred communicating with the card
     */
    COMMUNICATION_ERROR

}
