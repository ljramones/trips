package com.teamgannon.trips.dialogs.problemreport;

import com.teamgannon.trips.jpa.model.AppRegistration;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;

import java.util.Optional;

/**
 * Dialog for submitting a problem report.
 * Collects problem description and attachment preferences.
 */
public class ReportProblemDialog extends Dialog<ReportProblemResult> {

    private final TextArea descriptionArea;
    private final CheckBox includeScreenshotCheckbox;
    private final CheckBox includeLogsCheckbox;
    private final CheckBox includeSystemInfoCheckbox;
    private final CheckBox includeCrashFilesCheckbox;

    public ReportProblemDialog(AppRegistration registration, boolean hasCrashFiles) {
        setTitle("Report a Problem");
        setHeaderText("Describe the problem you're experiencing.");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        // User info display
        Label userLabel = new Label(String.format(
                "Reporting as: %s (%s)",
                registration.getDisplayName() != null && !registration.getDisplayName().isEmpty()
                        ? registration.getDisplayName()
                        : "User",
                registration.getEmail()
        ));
        userLabel.setStyle("-fx-text-fill: #666;");
        content.getChildren().add(userLabel);

        // Description section
        Label descLabel = new Label("Problem Description:");
        descLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        content.getChildren().add(descLabel);

        descriptionArea = new TextArea();
        descriptionArea.setPromptText(
                "Please describe what happened, what you expected, and any steps to reproduce the issue...");
        descriptionArea.setPrefRowCount(8);
        descriptionArea.setWrapText(true);
        VBox.setVgrow(descriptionArea, Priority.ALWAYS);
        content.getChildren().add(descriptionArea);

        // Attachments section
        Label attachLabel = new Label("Include with Report:");
        attachLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        content.getChildren().add(attachLabel);

        VBox attachBox = new VBox(5);
        attachBox.setPadding(new Insets(0, 0, 0, 20));

        includeSystemInfoCheckbox = new CheckBox("System information");
        includeSystemInfoCheckbox.setSelected(registration.isDefaultIncludeSystemInfo());
        includeSystemInfoCheckbox.setTooltip(new Tooltip(
                "OS version, Java version, memory, CPU info"
        ));

        includeLogsCheckbox = new CheckBox("Application logs (last 500 lines)");
        includeLogsCheckbox.setSelected(registration.isDefaultIncludeLogs());
        includeLogsCheckbox.setTooltip(new Tooltip(
                "Recent log entries that may help diagnose the problem"
        ));

        includeScreenshotCheckbox = new CheckBox("Screenshot of current window");
        includeScreenshotCheckbox.setSelected(registration.isDefaultIncludeScreenshot());
        includeScreenshotCheckbox.setTooltip(new Tooltip(
                "Captures the application window as it appears now"
        ));

        includeCrashFilesCheckbox = new CheckBox("JVM crash files (hs_err_pid*.log)");
        includeCrashFilesCheckbox.setSelected(hasCrashFiles);
        includeCrashFilesCheckbox.setDisable(!hasCrashFiles);
        if (!hasCrashFiles) {
            includeCrashFilesCheckbox.setTooltip(new Tooltip("No crash files detected"));
        } else {
            includeCrashFilesCheckbox.setTooltip(new Tooltip(
                    "Include JVM crash logs from recent crashes"
            ));
        }

        attachBox.getChildren().addAll(
                includeSystemInfoCheckbox,
                includeLogsCheckbox,
                includeScreenshotCheckbox,
                includeCrashFilesCheckbox
        );
        content.getChildren().add(attachBox);

        // Privacy note
        Label privacyNote = new Label(
                "Your report will be uploaded securely and used only for troubleshooting."
        );
        privacyNote.setStyle("-fx-text-fill: #888; -fx-font-size: 10;");
        privacyNote.setWrapText(true);
        content.getChildren().add(privacyNote);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            setResult(ReportProblemResult.cancelled());
            close();
        });

        Button submitButton = new Button("Submit Report");
        submitButton.setDefaultButton(true);
        submitButton.setOnAction(e -> handleSubmit());

        buttonBox.getChildren().addAll(cancelButton, submitButton);
        content.getChildren().add(buttonBox);

        getDialogPane().setContent(content);
        DialogUtils.bindCloseHandler(this, this::handleClose);

        // Focus on description
        descriptionArea.requestFocus();
    }

    private void handleSubmit() {
        String description = descriptionArea.getText().trim();

        // Validate description
        if (description.isEmpty()) {
            showError("Description required",
                    "Please describe the problem you're experiencing.");
            descriptionArea.requestFocus();
            return;
        }

        if (description.length() < 10) {
            showError("Description too short",
                    "Please provide more detail about the problem.");
            descriptionArea.requestFocus();
            return;
        }

        ReportProblemResult result = ReportProblemResult.builder()
                .submitted(true)
                .description(description)
                .includeScreenshot(includeScreenshotCheckbox.isSelected())
                .includeLogs(includeLogsCheckbox.isSelected())
                .includeSystemInfo(includeSystemInfoCheckbox.isSelected())
                .includeCrashFiles(includeCrashFilesCheckbox.isSelected() &&
                        !includeCrashFilesCheckbox.isDisabled())
                .build();

        setResult(result);
        close();
    }

    private void handleClose(WindowEvent event) {
        setResult(ReportProblemResult.cancelled());
        close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows the dialog and waits for user input.
     *
     * @return Optional containing the result if submitted, empty if cancelled
     */
    public Optional<ReportProblemResult> showAndGetResult() {
        showAndWait();
        ReportProblemResult result = getResult();
        if (result != null && result.isSubmitted()) {
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
