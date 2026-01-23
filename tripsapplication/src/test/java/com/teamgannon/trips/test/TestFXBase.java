package com.teamgannon.trips.test;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;

/**
 * Base class for TestFX integration tests.
 * Provides common setup and teardown for JavaFX testing in headless mode.
 *
 * <p>Usage:</p>
 * <pre>
 * class MyDialogTest extends TestFXBase {
 *
 *     {@literal @}Override
 *     public void start(Stage stage) {
 *         // Set up your JavaFX scene
 *         MyDialog dialog = new MyDialog();
 *         stage.setScene(new Scene(dialog, 400, 300));
 *         stage.show();
 *     }
 *
 *     {@literal @}Test
 *     void testButtonClick() {
 *         clickOn("#myButton");
 *         verifyThat("#resultLabel", hasText("Clicked!"));
 *     }
 * }
 * </pre>
 */
public abstract class TestFXBase extends ApplicationTest {

    /**
     * Set up headless mode properties before any tests run.
     * This is called once per test class.
     */
    @BeforeAll
    public static void setupHeadlessMode() {
        // These properties enable headless testing with Monocle
        if (Boolean.getBoolean("testfx.headless")) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
    }

    /**
     * Clean up after each test to prevent state leakage.
     */
    @AfterEach
    public void afterEachTest() throws TimeoutException {
        // Release all keys and buttons
        release(new javafx.scene.input.KeyCode[]{});
        release(new javafx.scene.input.MouseButton[]{});

        // Clean up stages
        FxToolkit.hideStage();
        WaitForAsyncUtils.waitForFxEvents(20);
    }

    /**
     * Helper method to find a node by CSS selector with retry.
     * Useful for elements that may appear after a delay.
     *
     * @param query CSS selector query
     * @param <T> Node type
     * @return The found node
     */
    protected <T extends Node> T find(String query) {
        return lookup(query).query();
    }

    /**
     * Helper method to wait for a condition with timeout.
     *
     * @param condition The condition to wait for
     * @param timeoutMs Timeout in milliseconds
     */
    protected void waitFor(Runnable condition, long timeoutMs) {
        try {
            WaitForAsyncUtils.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS,
                () -> {
                    try {
                        condition.run();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
        } catch (java.util.concurrent.TimeoutException e) {
            throw new AssertionError("Timeout waiting for condition after " + timeoutMs + "ms", e);
        }
    }
}
