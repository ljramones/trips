package com.teamgannon.trips.screenobjects;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarFieldValidator.
 */
class StarFieldValidatorTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            // Already initialized
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available, some tests will be skipped: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @Nested
    @DisplayName("parseDouble tests")
    class ParseDoubleTests {

        @Test
        @DisplayName("should parse valid double and call setter")
        void shouldParseValidDoubleAndCallSetter() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> capturedValue = new AtomicReference<>();
            AtomicBoolean result = new AtomicBoolean(false);

            runOnFxThread(() -> {
                TextField field = new TextField("123.456");
                result.set(StarFieldValidator.parseDouble(field, capturedValue::set));
            });

            assertTrue(result.get());
            assertEquals(123.456, capturedValue.get(), 0.0001);
        }

        // Note: Cannot test invalid input path for parseDouble because it triggers
        // a blocking alert dialog. Use parseDoubleOrDefault for graceful error handling
        // or parseDoubleOrThrow to catch exceptions.

        @Test
        @DisplayName("should parse negative double")
        void shouldParseNegativeDouble() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> capturedValue = new AtomicReference<>();
            AtomicBoolean result = new AtomicBoolean(false);

            runOnFxThread(() -> {
                TextField field = new TextField("-42.5");
                result.set(StarFieldValidator.parseDouble(field, capturedValue::set));
            });

            assertTrue(result.get());
            assertEquals(-42.5, capturedValue.get(), 0.0001);
        }

        @Test
        @DisplayName("should parse zero")
        void shouldParseZero() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> capturedValue = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("0");
                StarFieldValidator.parseDouble(field, capturedValue::set);
            });

            assertEquals(0.0, capturedValue.get(), 0.0001);
        }

        @Test
        @DisplayName("should parse scientific notation")
        void shouldParseScientificNotation() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> capturedValue = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("1.5E10");
                StarFieldValidator.parseDouble(field, capturedValue::set);
            });

            assertEquals(1.5E10, capturedValue.get(), 1E5);
        }
    }

    @Nested
    @DisplayName("parseDoubleOrThrow tests")
    class ParseDoubleOrThrowTests {

        @Test
        @DisplayName("should parse valid double")
        void shouldParseValidDouble() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> result = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("99.99");
                result.set(StarFieldValidator.parseDoubleOrThrow(field));
            });

            assertEquals(99.99, result.get(), 0.0001);
        }

        @Test
        @DisplayName("should throw NumberFormatException for invalid input")
        void shouldThrowNumberFormatExceptionForInvalidInput() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean threw = new AtomicBoolean(false);

            runOnFxThread(() -> {
                TextField field = new TextField("invalid");
                try {
                    StarFieldValidator.parseDoubleOrThrow(field);
                } catch (NumberFormatException e) {
                    threw.set(true);
                }
            });

            assertTrue(threw.get());
        }

        @Test
        @DisplayName("should throw for empty string")
        void shouldThrowForEmptyString() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean threw = new AtomicBoolean(false);

            runOnFxThread(() -> {
                TextField field = new TextField("");
                try {
                    StarFieldValidator.parseDoubleOrThrow(field);
                } catch (NumberFormatException e) {
                    threw.set(true);
                }
            });

            assertTrue(threw.get());
        }
    }

    @Nested
    @DisplayName("parseDoubleOrDefault tests")
    class ParseDoubleOrDefaultTests {

        @Test
        @DisplayName("should parse valid double")
        void shouldParseValidDouble() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> result = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("42.0");
                result.set(StarFieldValidator.parseDoubleOrDefault(field, 0.0));
            });

            assertEquals(42.0, result.get(), 0.0001);
        }

        @Test
        @DisplayName("should return default for invalid input")
        void shouldReturnDefaultForInvalidInput() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> result = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("not valid");
                result.set(StarFieldValidator.parseDoubleOrDefault(field, 99.0));
            });

            assertEquals(99.0, result.get(), 0.0001);
        }

        @Test
        @DisplayName("should return default for empty string")
        void shouldReturnDefaultForEmptyString() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Double> result = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("");
                result.set(StarFieldValidator.parseDoubleOrDefault(field, -1.0));
            });

            assertEquals(-1.0, result.get(), 0.0001);
        }
    }

    @Nested
    @DisplayName("getStringOrNull tests")
    class GetStringOrNullTests {

        @Test
        @DisplayName("should return trimmed string for valid input")
        void shouldReturnTrimmedStringForValidInput() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> result = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("  hello world  ");
                result.set(StarFieldValidator.getStringOrNull(field));
            });

            assertEquals("hello world", result.get());
        }

        @Test
        @DisplayName("should return null for empty string")
        void shouldReturnNullForEmptyString() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> result = new AtomicReference<>("not null");

            runOnFxThread(() -> {
                TextField field = new TextField("");
                result.set(StarFieldValidator.getStringOrNull(field));
            });

            assertNull(result.get());
        }

        @Test
        @DisplayName("should return null for blank string")
        void shouldReturnNullForBlankString() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> result = new AtomicReference<>("not null");

            runOnFxThread(() -> {
                TextField field = new TextField("   ");
                result.set(StarFieldValidator.getStringOrNull(field));
            });

            assertNull(result.get());
        }
    }

    @Nested
    @DisplayName("getStringOrEmpty tests")
    class GetStringOrEmptyTests {

        @Test
        @DisplayName("should return string for valid input")
        void shouldReturnStringForValidInput() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> result = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField("test value");
                result.set(StarFieldValidator.getStringOrEmpty(field));
            });

            assertEquals("test value", result.get());
        }

        @Test
        @DisplayName("should return empty string for null text")
        void shouldReturnEmptyStringForNullText() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> result = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField();
                field.setText(null);
                result.set(StarFieldValidator.getStringOrEmpty(field));
            });

            assertEquals("", result.get());
        }
    }

    @Nested
    @DisplayName("setupDoubleField tests")
    class SetupDoubleFieldTests {

        @Test
        @DisplayName("should set initial value")
        void shouldSetInitialValue() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> text = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField();
                StarFieldValidator.setupDoubleField(field, 123.456, "hint", () -> {});
                text.set(field.getText());
            });

            assertEquals("123.456", text.get());
        }

        @Test
        @DisplayName("should set prompt text")
        void shouldSetPromptText() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> prompt = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField();
                StarFieldValidator.setupDoubleField(field, 0.0, "Enter value", () -> {});
                prompt.set(field.getPromptText());
            });

            assertEquals("Enter value", prompt.get());
        }
    }

    @Nested
    @DisplayName("setupTextField tests")
    class SetupTextFieldTests {

        @Test
        @DisplayName("should set initial value")
        void shouldSetInitialValue() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> text = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField();
                StarFieldValidator.setupTextField(field, "initial", "hint");
                text.set(field.getText());
            });

            assertEquals("initial", text.get());
        }

        @Test
        @DisplayName("should set empty string for null value")
        void shouldSetEmptyStringForNullValue() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> text = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField();
                StarFieldValidator.setupTextField(field, null, "hint");
                text.set(field.getText());
            });

            assertEquals("", text.get());
        }

        @Test
        @DisplayName("should set prompt text")
        void shouldSetPromptText() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> prompt = new AtomicReference<>();

            runOnFxThread(() -> {
                TextField field = new TextField();
                StarFieldValidator.setupTextField(field, "value", "The prompt");
                prompt.set(field.getPromptText());
            });

            assertEquals("The prompt", prompt.get());
        }
    }

    private void runOnFxThread(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }
    }
}
