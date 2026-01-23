package com.teamgannon.trips.test;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * Sample TestFX test to verify headless testing setup works.
 * This test creates a simple JavaFX UI and verifies interactions.
 */
class SampleTestFXTest extends TestFXBase {

    private Label resultLabel;
    private int clickCount = 0;

    @Override
    public void start(Stage stage) {
        // Create a simple test UI
        resultLabel = new Label("Not clicked");
        resultLabel.setId("resultLabel");

        Button testButton = new Button("Click Me");
        testButton.setId("testButton");
        testButton.setOnAction(e -> {
            clickCount++;
            resultLabel.setText("Clicked " + clickCount + " time(s)");
        });

        Button resetButton = new Button("Reset");
        resetButton.setId("resetButton");
        resetButton.setOnAction(e -> {
            clickCount = 0;
            resultLabel.setText("Not clicked");
        });

        VBox root = new VBox(10, resultLabel, testButton, resetButton);
        root.setStyle("-fx-padding: 20;");

        stage.setScene(new Scene(root, 300, 200));
        stage.setTitle("TestFX Sample");
        stage.show();
    }

    @Test
    @DisplayName("TestFX headless mode should be working")
    void testFXHeadlessModeShouldWork() {
        // Verify initial state
        verifyThat("#resultLabel", hasText("Not clicked"));
    }

    @Test
    @DisplayName("Button click should update label")
    void buttonClickShouldUpdateLabel() {
        // Click the button
        clickOn("#testButton");

        // Verify the label was updated
        verifyThat("#resultLabel", hasText("Clicked 1 time(s)"));
    }

    @Test
    @DisplayName("Multiple clicks should increment counter")
    void multipleClicksShouldIncrementCounter() {
        // Click multiple times
        clickOn("#testButton");
        clickOn("#testButton");
        clickOn("#testButton");

        // Verify the label shows correct count
        verifyThat("#resultLabel", hasText("Clicked 3 time(s)"));
    }

    @Test
    @DisplayName("Reset button should clear counter")
    void resetButtonShouldClearCounter() {
        // Click the test button
        clickOn("#testButton");
        verifyThat("#resultLabel", hasText("Clicked 1 time(s)"));

        // Click reset
        clickOn("#resetButton");

        // Verify reset
        verifyThat("#resultLabel", hasText("Not clicked"));
    }

    @Test
    @DisplayName("Label should be found by ID")
    void labelShouldBeFoundById() {
        Label label = find("#resultLabel");
        assertNotNull(label);
        assertEquals("Not clicked", label.getText());
    }
}
