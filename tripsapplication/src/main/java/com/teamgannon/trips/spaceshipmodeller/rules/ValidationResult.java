package com.teamgannon.trips.spaceshipmodeller.rules;

import java.util.List;

/**
 * The outcome of validating a design: an immutable, ordered list of {@link ValidationMessage}s with
 * convenient severity-filtered views.
 *
 * @param messages all findings, in the order they were produced
 * @author TRIPS Spaceship Modeller
 */
public record ValidationResult(List<ValidationMessage> messages) {

    /**
     * Compact constructor making the message list immutable.
     */
    public ValidationResult {
        messages = messages == null ? List.of() : List.copyOf(messages);
    }

    /** @return an empty result (no findings) */
    public static ValidationResult empty() {
        return new ValidationResult(List.of());
    }

    /** @return only the {@link Severity#ERROR} findings */
    public List<ValidationMessage> errors() {
        return filter(Severity.ERROR);
    }

    /** @return only the {@link Severity#WARNING} findings */
    public List<ValidationMessage> warnings() {
        return filter(Severity.WARNING);
    }

    /** @return only the {@link Severity#INFO} findings */
    public List<ValidationMessage> infos() {
        return filter(Severity.INFO);
    }

    /** @return {@code true} if there are no errors (warnings are tolerated) */
    public boolean isValid() {
        return errors().isEmpty();
    }

    /** @return {@code true} if there is at least one warning */
    public boolean hasWarnings() {
        return !warnings().isEmpty();
    }

    /** @return {@code true} if there are no findings of any severity */
    public boolean isClean() {
        return messages.isEmpty();
    }

    private List<ValidationMessage> filter(Severity severity) {
        return messages.stream().filter(m -> m.severity() == severity).toList();
    }
}
