package com.teamgannon.trips.javafxsupport;

import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.util.function.Predicate;

/**
 * Helpers for inline form-field validation, replacing modal {@code Alert}
 * popups for input-validation nudges (Issue 29 / Phase 7).
 * <p>
 * Pairs a {@link Control} (typically a {@code TextField}) with a small error
 * {@link Label} placed below it in the FXML. On invalid input the control
 * gets the {@code :error} CSS pseudo-class (red border, defined in
 * {@code theme.css}) and the label shows a one-line message. On valid input
 * both are cleared. The whole UI stays responsive — no modal blocking for
 * "please enter a value" nudges.
 *
 * <h2>FXML setup</h2>
 * <pre>{@code
 *   <TextField fx:id="searchField"/>
 *   <Label fx:id="searchFieldError" styleClass="trips-inline-error" visible="false" managed="false"/>
 * }</pre>
 *
 * <h2>Controller usage</h2>
 * <pre>{@code
 *   if (!InlineFieldValidation.validate(
 *           searchField, searchFieldError,
 *           text -> !text.isBlank(),
 *           "Please enter a partial name")) {
 *       return;  // submit blocked
 *   }
 * }</pre>
 *
 * <h2>What stays modal</h2>
 * Genuinely-blocking errors (DB failure, unreadable file, network failure)
 * should still use {@code AlertFactory.showErrorAlert} — those need the
 * user's full attention and demand acknowledgement. Inline validation is for
 * recoverable input nudges only.
 */
public final class InlineFieldValidation {

    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

    /**
     * Severity colour duplicated from theme.css {@code -trips-color-danger} —
     * the helper applies this inline so it works in dialogs that haven't
     * loaded the stylesheet yet. When theme.css is wired into every scene
     * (Phase 7.10 follow-up), this becomes redundant with the CSS rule and
     * can be removed.
     */
    private static final String ERROR_BORDER_STYLE =
            "-fx-border-color: #c0392b; -fx-border-width: 1.5; -fx-border-radius: 3;";

    private static final String ERROR_LABEL_STYLE =
            "-fx-text-fill: #c0392b; -fx-font-size: 10px; -fx-padding: 2 0 0 4;";

    private InlineFieldValidation() {
    }

    /**
     * Mark a field as invalid: add the {@code :error} pseudo-class (CSS
     * paints a red border) and show {@code message} in {@code errorLabel}.
     */
    public static void attachError(Control field, Label errorLabel, String message) {
        if (field != null) {
            field.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, true);
            field.setStyle(ERROR_BORDER_STYLE);
        }
        if (errorLabel != null) {
            errorLabel.setStyle(ERROR_LABEL_STYLE);
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Clear any previously-attached error on a field/label pair. Safe to call
     * unconditionally before re-validating.
     */
    public static void clearError(Control field, Label errorLabel) {
        if (field != null) {
            field.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
            field.setStyle("");
        }
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Run {@code rule} against {@code field}'s text. If it returns {@code true}
     * the field is cleared of errors; otherwise {@code errorMessage} is
     * attached. Returns the rule's outcome so the caller can short-circuit
     * submit logic.
     */
    public static boolean validate(javafx.scene.control.TextField field,
                                   Label errorLabel,
                                   Predicate<String> rule,
                                   String errorMessage) {
        boolean ok = rule.test(field == null ? "" : field.getText());
        if (ok) {
            clearError(field, errorLabel);
        } else {
            attachError(field, errorLabel, errorMessage);
        }
        return ok;
    }
}
