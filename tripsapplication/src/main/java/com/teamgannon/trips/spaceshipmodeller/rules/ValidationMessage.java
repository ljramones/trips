package com.teamgannon.trips.spaceshipmodeller.rules;

/**
 * A single finding produced by the {@link ValidationEngine}.
 *
 * @param severity how serious the finding is
 * @param code     stable machine-readable rule identifier (e.g. {@code "MASS_DRY_TOO_LOW"})
 * @param message  human-readable explanation
 * @author TRIPS Spaceship Modeller
 */
public record ValidationMessage(Severity severity, String code, String message) {

    /**
     * Compact constructor enforcing presence of all fields.
     */
    public ValidationMessage {
        if (severity == null) {
            throw new IllegalArgumentException("severity must be provided");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must be provided");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must be provided");
        }
    }

    /** @return an {@link Severity#ERROR} message */
    public static ValidationMessage error(String code, String message) {
        return new ValidationMessage(Severity.ERROR, code, message);
    }

    /** @return a {@link Severity#WARNING} message */
    public static ValidationMessage warning(String code, String message) {
        return new ValidationMessage(Severity.WARNING, code, message);
    }

    /** @return an {@link Severity#INFO} message */
    public static ValidationMessage info(String code, String message) {
        return new ValidationMessage(Severity.INFO, code, message);
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + code + ": " + message;
    }
}
