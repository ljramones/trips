package com.teamgannon.trips.screenobjects;

/**
 * Save-time validation error for a single star-edit field.
 */
public class StarFieldValidationException extends RuntimeException {

    private final String fieldLabel;
    private final String fieldValue;

    public StarFieldValidationException(String fieldLabel, String fieldValue, Throwable cause) {
        super("%s must be a valid floating point number. Current value: '%s'"
                .formatted(fieldLabel, fieldValue), cause);
        this.fieldLabel = fieldLabel;
        this.fieldValue = fieldValue;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public String getFieldValue() {
        return fieldValue;
    }
}
