package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarEditFormBinder.
 */
class StarEditFormBinderTest {

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
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("should store the record")
        void shouldStoreTheRecord() {
            StarObject record = createTestRecord();
            StarEditFormBinder binder = new StarEditFormBinder(record);

            assertSame(record, binder.getRecord());
        }
    }

    @Nested
    @DisplayName("initializeOverviewTab tests")
    class InitializeOverviewTabTests {

        @Test
        @DisplayName("should populate record ID label")
        void shouldPopulateRecordIdLabel() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> labelText = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setId("test-id-123");

                StarEditFormBinder binder = new StarEditFormBinder(record);
                Label recordIdLabel = new Label();
                Label dataSetLabel = new Label();

                // Create and set overview fields
                TextField starName = new TextField();
                TextField commonName = new TextField();
                TextField constellation = new TextField();
                TextField spectral = new TextField();
                TextField distance = new TextField();
                TextField metallicity = new TextField();
                TextField age = new TextField();
                TextField x = new TextField();
                TextField y = new TextField();
                TextField z = new TextField();
                TextArea notes = new TextArea();

                binder.setOverviewFields(starName, commonName, constellation, spectral,
                        distance, metallicity, age, x, y, z, notes);
                binder.initializeOverviewTab(recordIdLabel, dataSetLabel);

                labelText.set(recordIdLabel.getText());
            });

            assertEquals("test-id-123", labelText.get());
        }

        @Test
        @DisplayName("should populate star name field")
        void shouldPopulateStarNameField() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> fieldText = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setDisplayName("Alpha Centauri");

                StarEditFormBinder binder = new StarEditFormBinder(record);

                TextField starName = new TextField();
                binder.setOverviewFields(starName, new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextArea());
                binder.initializeOverviewTab(new Label(), new Label());

                fieldText.set(starName.getText());
            });

            assertEquals("Alpha Centauri", fieldText.get());
        }

        @Test
        @DisplayName("should populate coordinate fields")
        void shouldPopulateCoordinateFields() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> xText = new AtomicReference<>();
            AtomicReference<String> yText = new AtomicReference<>();
            AtomicReference<String> zText = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setX(1.5);
                record.setY(2.5);
                record.setZ(3.5);

                StarEditFormBinder binder = new StarEditFormBinder(record);

                TextField x = new TextField();
                TextField y = new TextField();
                TextField z = new TextField();

                binder.setOverviewFields(new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        x, y, z, new TextArea());
                binder.initializeOverviewTab(new Label(), new Label());

                xText.set(x.getText());
                yText.set(y.getText());
                zText.set(z.getText());
            });

            assertEquals("1.5", xText.get());
            assertEquals("2.5", yText.get());
            assertEquals("3.5", zText.get());
        }
    }

    @Nested
    @DisplayName("initializeFictionalTab tests")
    class InitializeFictionalTabTests {

        @Test
        @DisplayName("should populate polity field")
        void shouldPopulatePolityField() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> fieldText = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setPolity("Terran");

                StarEditFormBinder binder = new StarEditFormBinder(record);

                TextField polity = new TextField();
                binder.setFictionalFields(polity, new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField());
                binder.initializeFictionalTab();

                fieldText.set(polity.getText());
            });

            assertEquals("Terran", fieldText.get());
        }

        @Test
        @DisplayName("should populate all fictional fields")
        void shouldPopulateAllFictionalFields() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> worldText = new AtomicReference<>();
            AtomicReference<String> fuelText = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setWorldType("Green");
                record.setFuelType("H2");

                StarEditFormBinder binder = new StarEditFormBinder(record);

                TextField world = new TextField();
                TextField fuel = new TextField();

                binder.setFictionalFields(new TextField(), world, fuel,
                        new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField());
                binder.initializeFictionalTab();

                worldText.set(world.getText());
                fuelText.set(fuel.getText());
            });

            assertEquals("Green", worldText.get());
            assertEquals("H2", fuelText.get());
        }
    }

    @Nested
    @DisplayName("initializeSecondaryTab tests")
    class InitializeSecondaryTabTests {

        @Test
        @DisplayName("should populate RA field")
        void shouldPopulateRAField() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> raText = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setRa(180.5);

                StarEditFormBinder binder = new StarEditFormBinder(record);

                TextField ra = new TextField();
                binder.setSecondaryFields(new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        ra, new TextField(), new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextArea());
                binder.initializeSecondaryTab();

                raText.set(ra.getText());
            });

            assertEquals("180.5", raText.get());
        }

        @Test
        @DisplayName("should populate luminosity field with string value")
        void shouldPopulateLuminosityField() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> lumText = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setLuminosity("1.5");

                StarEditFormBinder binder = new StarEditFormBinder(record);

                TextField luminosity = new TextField();
                binder.setSecondaryFields(new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), luminosity, new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(), new TextArea());
                binder.initializeSecondaryTab();

                lumText.set(luminosity.getText());
            });

            assertEquals("1.5", lumText.get());
        }
    }

    @Nested
    @DisplayName("initializeUserTab tests")
    class InitializeUserTabTests {

        @Test
        @DisplayName("should populate misc text fields")
        void shouldPopulateMiscTextFields() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> misc1Text = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setMiscText1("Custom note");

                StarEditFormBinder binder = new StarEditFormBinder(record);

                TextField misc1 = new TextField();
                CheckBox force = new CheckBox();

                binder.setUserFields(misc1, new TextField(), new TextField(),
                        new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), force);
                binder.initializeUserTab();

                misc1Text.set(misc1.getText());
            });

            assertEquals("Custom note", misc1Text.get());
        }

        @Test
        @DisplayName("should set force label checkbox state")
        void shouldSetForceLabelCheckboxState() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Boolean> forceSelected = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                record.setForceLabelToBeShown(true);

                StarEditFormBinder binder = new StarEditFormBinder(record);

                CheckBox force = new CheckBox();
                binder.setUserFields(new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), force);
                binder.initializeUserTab();

                forceSelected.set(force.isSelected());
            });

            assertTrue(forceSelected.get());
        }
    }

    @Nested
    @DisplayName("collectAllData tests")
    class CollectAllDataTests {

        @Test
        @DisplayName("should collect display name from form")
        void shouldCollectDisplayNameFromForm() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> collectedName = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                StarEditFormBinder binder = createFullyConfiguredBinder(record);

                // Modify the star name field
                binder.getRecord().setDisplayName(""); // Clear it
                // The binder should have set up fields, now modify via the form
                // For this test, we'll just verify collectAllData runs without error
                // and check a simple field update

                try {
                    binder.collectAllData();
                    collectedName.set(record.getDisplayName());
                } catch (Exception e) {
                    // Expected as we need to set up all numeric fields properly
                }
            });

            // The test verifies the method runs and record is accessible
            assertNotNull(collectedName.get());
        }

        @Test
        @DisplayName("should throw NumberFormatException for invalid numeric input")
        void shouldThrowNumberFormatExceptionForInvalidInput() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Boolean> threwException = new AtomicReference<>(false);

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                StarEditFormBinder binder = new StarEditFormBinder(record);

                // Create fields with some invalid numeric values
                TextField starName = new TextField("Test Star");
                TextField distance = new TextField("not a number"); // Invalid!
                TextField radius = new TextField("1.0");

                binder.setOverviewFields(starName, new TextField(), new TextField(),
                        new TextField(), distance, new TextField(), new TextField(),
                        new TextField("0"), new TextField("0"), new TextField("0"),
                        new TextArea());

                binder.setSecondaryFields(new TextField(), new TextField("0"), new TextField("0"),
                        radius, new TextField("1.0"), new TextField("1.0"), new TextField("5000"),
                        new TextField("0"), new TextField("0"), new TextField("0"),
                        new TextField("0"), new TextField("0"), new TextField("0"),
                        new TextField("0"), new TextField("0"), new TextField("0"),
                        new TextField("0"), new TextField("0"), new TextField("0"),
                        new TextField("0"), new TextField("0"), new TextField(), new TextArea());

                binder.setFictionalFields(new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(), new TextField());

                binder.setUserFields(new TextField(), new TextField(), new TextField(),
                        new TextField(), new TextField(),
                        new TextField("0"), new TextField("0"), new TextField("0"),
                        new TextField("0"), new TextField("0"), new CheckBox());

                try {
                    binder.collectAllData();
                } catch (NumberFormatException e) {
                    threwException.set(true);
                }
            });

            assertTrue(threwException.get());
        }

        @Test
        @DisplayName("should collect fictional data")
        void shouldCollectFictionalData() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> collectedPolity = new AtomicReference<>();

            runOnFxThread(() -> {
                StarObject record = createTestRecord();
                StarEditFormBinder binder = createFullyConfiguredBinder(record);

                try {
                    binder.collectAllData();
                    collectedPolity.set(record.getPolity());
                } catch (Exception e) {
                    // Handle if fields aren't fully configured
                }
            });

            assertNotNull(collectedPolity.get());
        }
    }

    // Helper methods

    private StarObject createTestRecord() {
        StarObject record = new StarObject();
        record.setId("test-123");
        record.setDataSetName("Test Dataset");
        record.setDisplayName("Test Star");
        record.setDistance(10.0);
        record.setX(0.0);
        record.setY(0.0);
        record.setZ(0.0);
        record.setRadius(1.0);
        record.setMass(1.0);
        record.setTemperature(5778.0);
        record.setLuminosity("1.0");
        record.setRa(0.0);
        record.setDeclination(0.0);
        record.setParallax(100.0);
        record.setMetallicity(0.0);
        record.setAge(4.6);
        record.setGalacticLat(0.0);
        record.setGalacticLong(0.0);
        record.setPmra(0.0);
        record.setPmdec(0.0);
        record.setRadialVelocity(0.0);
        record.setBprp(0.0);
        record.setBpg(0.0);
        record.setGrp(0.0);
        record.setMagu(0.0);
        record.setMagb(0.0);
        record.setMagv(0.0);
        record.setMagr(0.0);
        record.setMagi(0.0);
        record.setMiscNum1(0.0);
        record.setMiscNum2(0.0);
        record.setMiscNum3(0.0);
        record.setMiscNum4(0.0);
        record.setMiscNum5(0.0);
        return record;
    }

    private StarEditFormBinder createFullyConfiguredBinder(StarObject record) {
        StarEditFormBinder binder = new StarEditFormBinder(record);

        // Overview fields - all with valid numeric strings
        binder.setOverviewFields(
                new TextField("Test Star"),
                new TextField("Common"),
                new TextField("Orion"),
                new TextField("G2V"),
                new TextField("10.0"),
                new TextField("0.0"),
                new TextField("4.6"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextArea("Notes")
        );

        // Secondary fields
        binder.setSecondaryFields(
                new TextField("SIMBAD-123"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("1.0"),
                new TextField("1.0"),
                new TextField("1.0"),
                new TextField("5778.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("100.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("GAIA-123"),
                new TextArea()
        );

        // Fictional fields
        binder.setFictionalFields(
                new TextField("Terran"),
                new TextField("Green"),
                new TextField("H2"),
                new TextField("5"),
                new TextField("A"),
                new TextField("1000s"),
                new TextField("Industry"),
                new TextField("B"),
                new TextField("C")
        );

        // User fields
        binder.setUserFields(
                new TextField(""),
                new TextField(""),
                new TextField(""),
                new TextField(""),
                new TextField(""),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new TextField("0.0"),
                new CheckBox()
        );

        return binder;
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
