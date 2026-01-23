package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Serializable implementation across all JPA entities.
 */
@DisplayName("JPA Entity Serializable Tests")
class JpaEntitySerializableTest {

    @Nested
    @DisplayName("TripsPrefs Serializable")
    class TripsPrefsTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            TripsPrefs prefs = new TripsPrefs();
            assertTrue(prefs instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            TripsPrefs original = new TripsPrefs();
            original.setId("test-prefs-id");
            original.setSkipStartupDialog(true);
            original.setDatasetName("TestDataset");

            TripsPrefs deserialized = serializeAndDeserialize(original);

            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.isSkipStartupDialog(), deserialized.isSkipStartupDialog());
            assertEquals(original.getDatasetName(), deserialized.getDatasetName());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            TripsPrefs original = new TripsPrefs();
            original.setId("equality-test-id");

            TripsPrefs deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("TransitSettings Serializable")
    class TransitSettingsTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            TransitSettings settings = new TransitSettings();
            assertTrue(settings instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            TransitSettings original = new TransitSettings();
            original.setId("transit-id");
            original.setUpperDistance(15.0);
            original.setLowerDistance(5.0);
            original.setLineWidth(2.5);
            original.setLineColor("0x00ff00ff");

            TransitSettings deserialized = serializeAndDeserialize(original);

            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getUpperDistance(), deserialized.getUpperDistance());
            assertEquals(original.getLowerDistance(), deserialized.getLowerDistance());
            assertEquals(original.getLineWidth(), deserialized.getLineWidth());
            assertEquals(original.getLineColor(), deserialized.getLineColor());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            TransitSettings original = new TransitSettings();
            original.setId("equality-transit-id");

            TransitSettings deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("AsteroidBelt Serializable")
    class AsteroidBeltTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            AsteroidBelt belt = new AsteroidBelt();
            assertTrue(belt instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            AsteroidBelt original = new AsteroidBelt();
            original.setId(UUID.randomUUID());
            original.setDataSetName("TestDataset");
            original.setInnerRadius(2.0);
            original.setOuterRadius(3.5);
            original.setDensity(0.5);
            original.setDiameter(100.0);

            AsteroidBelt deserialized = serializeAndDeserialize(original);

            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getDataSetName(), deserialized.getDataSetName());
            assertEquals(original.getInnerRadius(), deserialized.getInnerRadius());
            assertEquals(original.getOuterRadius(), deserialized.getOuterRadius());
            assertEquals(original.getDensity(), deserialized.getDensity());
            assertEquals(original.getDiameter(), deserialized.getDiameter());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            AsteroidBelt original = new AsteroidBelt();
            original.setId(UUID.randomUUID());

            AsteroidBelt deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("GraphColorsPersist Serializable")
    class GraphColorsPersistTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            GraphColorsPersist colors = new GraphColorsPersist();
            assertTrue(colors instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            GraphColorsPersist original = new GraphColorsPersist();
            original.setId("colors-id");
            original.setLabelColor("0xff0000ff");
            original.setGridColor("0x00ff00ff");
            original.setExtensionColor("0x0000ffff");
            original.setLegendColor("0xffffffff");
            original.setStemLineWidth(1.5);
            original.setGridLineWidth(0.8);
            original.setLabelFont("Courier:12");

            GraphColorsPersist deserialized = serializeAndDeserialize(original);

            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getLabelColor(), deserialized.getLabelColor());
            assertEquals(original.getGridColor(), deserialized.getGridColor());
            assertEquals(original.getExtensionColor(), deserialized.getExtensionColor());
            assertEquals(original.getLegendColor(), deserialized.getLegendColor());
            assertEquals(original.getStemLineWidth(), deserialized.getStemLineWidth());
            assertEquals(original.getGridLineWidth(), deserialized.getGridLineWidth());
            assertEquals(original.getLabelFont(), deserialized.getLabelFont());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            GraphColorsPersist original = new GraphColorsPersist();
            original.setId("equality-colors-id");

            GraphColorsPersist deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("GraphEnablesPersist Serializable")
    class GraphEnablesPersistTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            GraphEnablesPersist enables = new GraphEnablesPersist();
            assertTrue(enables instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            GraphEnablesPersist original = new GraphEnablesPersist();
            original.setId("enables-id");
            original.setDisplayPolities(false);
            original.setDisplayGrid(true);
            original.setDisplayStems(false);
            original.setDisplayLabels(true);
            original.setDisplayLegend(false);
            original.setDisplayRoutes(true);

            GraphEnablesPersist deserialized = serializeAndDeserialize(original);

            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.isDisplayPolities(), deserialized.isDisplayPolities());
            assertEquals(original.isDisplayGrid(), deserialized.isDisplayGrid());
            assertEquals(original.isDisplayStems(), deserialized.isDisplayStems());
            assertEquals(original.isDisplayLabels(), deserialized.isDisplayLabels());
            assertEquals(original.isDisplayLegend(), deserialized.isDisplayLegend());
            assertEquals(original.isDisplayRoutes(), deserialized.isDisplayRoutes());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            GraphEnablesPersist original = new GraphEnablesPersist();
            original.setId("equality-enables-id");

            GraphEnablesPersist deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("StarDetailsPersist Serializable")
    class StarDetailsPersistTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            StarDetailsPersist details = new StarDetailsPersist();
            assertTrue(details instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            StarDetailsPersist original = new StarDetailsPersist();
            original.setId("details-id");
            original.setStellarClass("G2V");
            original.setStarColor("0xffff00ff");
            original.setRadius(1.0f);

            StarDetailsPersist deserialized = serializeAndDeserialize(original);

            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getStellarClass(), deserialized.getStellarClass());
            assertEquals(original.getStarColor(), deserialized.getStarColor());
            assertEquals(original.getRadius(), deserialized.getRadius());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            StarDetailsPersist original = new StarDetailsPersist();
            original.setId("equality-details-id");

            StarDetailsPersist deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("CivilizationDisplayPreferences Serializable")
    class CivilizationDisplayPreferencesTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
            assertTrue(prefs instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            CivilizationDisplayPreferences original = new CivilizationDisplayPreferences();
            original.setStorageTag("TestTag");
            original.setHumanPolityColor("0xff0000ff");
            original.setDornaniPolityColor("0x00ff00ff");

            CivilizationDisplayPreferences deserialized = serializeAndDeserialize(original);

            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getStorageTag(), deserialized.getStorageTag());
            assertEquals(original.getHumanPolityColor(), deserialized.getHumanPolityColor());
            assertEquals(original.getDornaniPolityColor(), deserialized.getDornaniPolityColor());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            CivilizationDisplayPreferences original = new CivilizationDisplayPreferences();
            // ID is initialized in constructor

            CivilizationDisplayPreferences deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("DataSetDescriptor Serializable")
    class DataSetDescriptorTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            assertTrue(descriptor instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            DataSetDescriptor original = new DataSetDescriptor();
            original.setDataSetName("TestDataset");
            original.setFilePath("/path/to/file");
            original.setFileCreator("TestCreator");
            original.setNumberStars(1000L);
            original.setDistanceRange(50.0);

            DataSetDescriptor deserialized = serializeAndDeserialize(original);

            assertEquals(original.getDataSetName(), deserialized.getDataSetName());
            assertEquals(original.getFilePath(), deserialized.getFilePath());
            assertEquals(original.getFileCreator(), deserialized.getFileCreator());
            assertEquals(original.getNumberStars(), deserialized.getNumberStars());
            assertEquals(original.getDistanceRange(), deserialized.getDistanceRange());
        }

        @Test
        @DisplayName("should maintain equality after serialization")
        void shouldMaintainEquality() throws IOException, ClassNotFoundException {
            DataSetDescriptor original = new DataSetDescriptor();
            original.setDataSetName("equality-test-dataset");

            DataSetDescriptor deserialized = serializeAndDeserialize(original);

            assertEquals(original, deserialized);
        }
    }

    // ==================== Helper Methods ====================

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T serializeAndDeserialize(T original) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        T deserialized = (T) ois.readObject();
        ois.close();

        return deserialized;
    }
}
