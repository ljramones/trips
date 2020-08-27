package com.teamgannon.trips.support;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class AlertFactory {

    /**
     * show an information message
     *
     * @param title   the title
     * @param message the information message
     */
    public static void showInfoMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
        log.info(message);
    }

    /**
     * show a warning message
     *
     * @param title   the title
     * @param message the warning message
     */
    public static void showWarningMessage(String title,
                                          String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
        log.warn(message);
    }

    /**
     * show a confirmation alert
     *
     * @param title      the title
     * @param headerTxt  the header text
     * @param contentTxt the content text
     * @return the button that was selected
     */
    public static Optional<ButtonType> showConfirmationAlert(String title,
                                                             String headerTxt,
                                                             String contentTxt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerTxt);
        alert.setContentText(contentTxt);
        return alert.showAndWait();
    }

    /**
     * show an error alert
     *
     * @param title the title
     * @param error the error
     */
    public static void showErrorAlert(String title, String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(error);
        alert.showAndWait();
        log.error(error);
    }

}
