package com.teamgannon.trips.screenobjects;

import javafx.scene.control.TextField;

import java.util.function.Consumer;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Utility class for validating and parsing star field values from text fields.
 * Provides reusable validation methods that show error alerts on invalid input.
 */
public class StarFieldValidator {

    private static final String DIALOG_TITLE = "Edit Star Record";

    private StarFieldValidator() {
        // Utility class
    }

    /**
     * Parse a double value from a text field and apply it via the setter.
     * Shows an error alert if the value is invalid.
     *
     * @param textField the text field to read from
     * @param setter    the setter to apply the parsed value
     * @return true if parsing succeeded, false otherwise
     */
    public static boolean parseDouble(TextField textField, Consumer<Double> setter) {
        try {
            double value = Double.parseDouble(textField.getText());
            setter.accept(value);
            return true;
        } catch (NumberFormatException nfe) {
            showErrorAlert(DIALOG_TITLE, textField.getText() + " is an invalid floating point number");
            return false;
        }
    }

    /**
     * Parse a double value from a text field without showing an error.
     * Used when collecting all data at once and errors are handled elsewhere.
     *
     * @param textField the text field to read from
     * @return the parsed double value
     * @throws NumberFormatException if the value cannot be parsed
     */
    public static double parseDoubleOrThrow(TextField textField) {
        return Double.parseDouble(textField.getText());
    }

    /**
     * Parse a double value from a text field, returning a default if invalid.
     *
     * @param textField    the text field to read from
     * @param defaultValue the default value if parsing fails
     * @return the parsed value or the default
     */
    public static double parseDoubleOrDefault(TextField textField, double defaultValue) {
        try {
            return Double.parseDouble(textField.getText());
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Get string value from text field, returning null if empty or blank.
     *
     * @param textField the text field to read from
     * @return the trimmed text or null if empty
     */
    public static String getStringOrNull(TextField textField) {
        String text = textField.getText();
        return (text == null || text.isBlank()) ? null : text.trim();
    }

    /**
     * Get string value from text field, returning empty string if null.
     *
     * @param textField the text field to read from
     * @return the text or empty string
     */
    public static String getStringOrEmpty(TextField textField) {
        String text = textField.getText();
        return text != null ? text : "";
    }

    /**
     * Set up a text field with a prompt and on-enter validation handler.
     *
     * @param textField   the text field to configure
     * @param initialValue the initial value to display
     * @param promptText  the prompt text hint
     * @param onEnter     the action to run when Enter is pressed
     */
    public static void setupDoubleField(TextField textField, double initialValue,
                                        String promptText, Runnable onEnter) {
        textField.setText(Double.toString(initialValue));
        textField.setPromptText(promptText);
        textField.setOnKeyPressed(ke -> {
            if (ke.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onEnter.run();
            }
        });
    }

    /**
     * Set up a text field with a prompt and on-enter validation handler.
     *
     * @param textField   the text field to configure
     * @param initialValue the initial value to display (may be null)
     * @param promptText  the prompt text hint
     * @param onEnter     the action to run when Enter is pressed
     */
    public static void setupStringField(TextField textField, String initialValue,
                                        String promptText, Runnable onEnter) {
        textField.setText(initialValue != null ? initialValue : "");
        textField.setPromptText(promptText);
        if (onEnter != null) {
            textField.setOnKeyPressed(ke -> {
                if (ke.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    onEnter.run();
                }
            });
        }
    }

    /**
     * Set up a read-only display field (no prompt, no Enter handler).
     *
     * @param textField   the text field to configure
     * @param initialValue the initial value to display
     */
    public static void setupReadOnlyField(TextField textField, double initialValue) {
        textField.setText(Double.toString(initialValue));
    }

    /**
     * Set up a text field with just initial value and prompt text.
     *
     * @param textField   the text field to configure
     * @param initialValue the initial value
     * @param promptText  the prompt text
     */
    public static void setupTextField(TextField textField, String initialValue, String promptText) {
        textField.setText(initialValue != null ? initialValue : "");
        if (promptText != null) {
            textField.setPromptText(promptText);
        }
    }
}
