package com.teamgannon.trips.dialogs.problemreport;

import com.teamgannon.trips.jpa.model.AppRegistration;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;

import java.util.Optional;

/**
 * Dialog for first-time user registration for problem reports.
 * Collects email, display name, and default preferences.
 */
public class AppRegistrationDialog extends Dialog<AppRegistration> {

    private final TextField emailField;
    private final TextField displayNameField;
    private final CheckBox includeSystemInfoCheckbox;
    private final CheckBox includeLogsCheckbox;
    private final CheckBox includeScreenshotCheckbox;

    public AppRegistrationDialog() {
        setTitle("Register for Problem Reporting");
        setHeaderText("Enter your information to submit problem reports.\n" +
                "This helps us follow up on issues you report.");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Info section
        Label infoLabel = new Label(
                "Your information is only used to respond to your reports.\n" +
                        "It is stored locally and sent only with your reports."
        );
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-text-fill: #666;");
        content.getChildren().add(infoLabel);

        // Form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        emailField = new TextField();
        emailField.setPromptText("your.email@example.com");
        emailField.setPrefWidth(300);
        grid.add(emailLabel, 0, 0);
        grid.add(emailField, 1, 0);

        Label nameLabel = new Label("Name:");
        nameLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        displayNameField = new TextField();
        displayNameField.setPromptText("Your name (optional)");
        displayNameField.setPrefWidth(300);
        grid.add(nameLabel, 0, 1);
        grid.add(displayNameField, 1, 1);

        content.getChildren().add(grid);

        // Default preferences section
        Label prefsLabel = new Label("Default Attachments:");
        prefsLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        content.getChildren().add(prefsLabel);

        VBox prefsBox = new VBox(5);
        prefsBox.setPadding(new Insets(0, 0, 0, 20));

        includeSystemInfoCheckbox = new CheckBox("Include system information");
        includeSystemInfoCheckbox.setSelected(true);
        includeSystemInfoCheckbox.setTooltip(new Tooltip(
                "OS, Java version, memory, CPU info"
        ));

        includeLogsCheckbox = new CheckBox("Include application logs");
        includeLogsCheckbox.setSelected(true);
        includeLogsCheckbox.setTooltip(new Tooltip(
                "Last 500 lines from the log file"
        ));

        includeScreenshotCheckbox = new CheckBox("Include screenshot");
        includeScreenshotCheckbox.setSelected(false);
        includeScreenshotCheckbox.setTooltip(new Tooltip(
                "Capture the current window when reporting"
        ));

        prefsBox.getChildren().addAll(
                includeSystemInfoCheckbox,
                includeLogsCheckbox,
                includeScreenshotCheckbox
        );
        content.getChildren().add(prefsBox);

        Label prefsNote = new Label(
                "(You can change these for each report)"
        );
        prefsNote.setStyle("-fx-text-fill: #888; -fx-font-size: 10;");
        content.getChildren().add(prefsNote);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            setResult(null);
            close();
        });

        Button registerButton = new Button("Register");
        registerButton.setDefaultButton(true);
        registerButton.setOnAction(e -> handleRegister());

        buttonBox.getChildren().addAll(cancelButton, registerButton);
        content.getChildren().add(buttonBox);

        getDialogPane().setContent(content);
        DialogUtils.bindCloseHandler(this, this::handleClose);
    }

    private void handleRegister() {
        String email = emailField.getText().trim();

        // Validate email
        if (email.isEmpty()) {
            showError("Email is required", "Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            showError("Invalid email", "Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        // Create registration
        AppRegistration registration = AppRegistration.createNew(
                email,
                displayNameField.getText().trim()
        );
        registration.setDefaultIncludeSystemInfo(includeSystemInfoCheckbox.isSelected());
        registration.setDefaultIncludeLogs(includeLogsCheckbox.isSelected());
        registration.setDefaultIncludeScreenshot(includeScreenshotCheckbox.isSelected());

        setResult(registration);
        close();
    }

    private void handleClose(WindowEvent event) {
        setResult(null);
        close();
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows the dialog and waits for user input.
     *
     * @return Optional containing the registration if completed, empty if cancelled
     */
    public Optional<AppRegistration> showAndGetResult() {
        showAndWait();
        return Optional.ofNullable(getResult());
    }
}
