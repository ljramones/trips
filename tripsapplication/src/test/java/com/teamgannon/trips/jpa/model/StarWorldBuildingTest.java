package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the StarWorldBuilding @Embeddable class.
 */
@DisplayName("StarWorldBuilding Tests")
class StarWorldBuildingTest {

    @Nested
    @DisplayName("Serializable")
    class SerializableTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            assertTrue(worldBuilding instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            StarWorldBuilding original = new StarWorldBuilding();
            original.setPolity("Terran");
            original.setWorldType("Habitable");
            original.setFuelType("Refined");
            original.setPortType("Class A");
            original.setPopulationType("Billions");
            original.setTechType("High");
            original.setProductType("Industrial");
            original.setMilSpaceType("Naval Base");
            original.setMilPlanType("Army Base");
            original.setOther(true);
            original.setAnomaly(true);

            StarWorldBuilding deserialized = serializeAndDeserialize(original);

            assertEquals(original.getPolity(), deserialized.getPolity());
            assertEquals(original.getWorldType(), deserialized.getWorldType());
            assertEquals(original.getFuelType(), deserialized.getFuelType());
            assertEquals(original.getPortType(), deserialized.getPortType());
            assertEquals(original.getPopulationType(), deserialized.getPopulationType());
            assertEquals(original.getTechType(), deserialized.getTechType());
            assertEquals(original.getProductType(), deserialized.getProductType());
            assertEquals(original.getMilSpaceType(), deserialized.getMilSpaceType());
            assertEquals(original.getMilPlanType(), deserialized.getMilPlanType());
            assertEquals(original.isOther(), deserialized.isOther());
            assertEquals(original.isAnomaly(), deserialized.isAnomaly());
        }
    }

    @Nested
    @DisplayName("initDefaults()")
    class InitDefaultsTests {

        @Test
        @DisplayName("should initialize all fields to default values")
        void shouldInitializeAllFieldsToDefaults() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.setPolity("Terran");
            worldBuilding.setOther(true);
            worldBuilding.setAnomaly(true);

            worldBuilding.initDefaults();

            assertEquals("NA", worldBuilding.getPolity());
            assertEquals("NA", worldBuilding.getWorldType());
            assertEquals("NA", worldBuilding.getFuelType());
            assertEquals("NA", worldBuilding.getPortType());
            assertEquals("NA", worldBuilding.getPopulationType());
            assertEquals("NA", worldBuilding.getTechType());
            assertEquals("NA", worldBuilding.getProductType());
            assertEquals("NA", worldBuilding.getMilSpaceType());
            assertEquals("NA", worldBuilding.getMilPlanType());
            assertFalse(worldBuilding.isOther());
            assertFalse(worldBuilding.isAnomaly());
        }
    }

    @Nested
    @DisplayName("hasAnyFieldsSet()")
    class HasAnyFieldsSetTests {

        @Test
        @DisplayName("should return false when all fields are default")
        void shouldReturnFalseWhenAllDefault() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();

            assertFalse(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when polity is set")
        void shouldReturnTrueWhenPolitySet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setPolity("Terran");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when worldType is set")
        void shouldReturnTrueWhenWorldTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setWorldType("Habitable");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when fuelType is set")
        void shouldReturnTrueWhenFuelTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setFuelType("Refined");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when portType is set")
        void shouldReturnTrueWhenPortTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setPortType("Class A");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when populationType is set")
        void shouldReturnTrueWhenPopulationTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setPopulationType("Billions");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when techType is set")
        void shouldReturnTrueWhenTechTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setTechType("High");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when productType is set")
        void shouldReturnTrueWhenProductTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setProductType("Industrial");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when milSpaceType is set")
        void shouldReturnTrueWhenMilSpaceTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setMilSpaceType("Naval Base");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when milPlanType is set")
        void shouldReturnTrueWhenMilPlanTypeSet() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setMilPlanType("Army Base");

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when other is true")
        void shouldReturnTrueWhenOtherTrue() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setOther(true);

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return true when anomaly is true")
        void shouldReturnTrueWhenAnomalyTrue() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.initDefaults();
            worldBuilding.setAnomaly(true);

            assertTrue(worldBuilding.hasAnyFieldsSet());
        }

        @Test
        @DisplayName("should return false for empty string values")
        void shouldReturnFalseForEmptyStrings() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();
            worldBuilding.setPolity("");
            worldBuilding.setWorldType("   ");
            worldBuilding.setFuelType(null);

            assertFalse(worldBuilding.hasAnyFieldsSet());
        }
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have NA as default for string fields")
        void shouldHaveNAAsDefault() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();

            assertEquals("NA", worldBuilding.getPolity());
            assertEquals("NA", worldBuilding.getWorldType());
            assertEquals("NA", worldBuilding.getFuelType());
            assertEquals("NA", worldBuilding.getPortType());
            assertEquals("NA", worldBuilding.getPopulationType());
            assertEquals("NA", worldBuilding.getTechType());
            assertEquals("NA", worldBuilding.getProductType());
            assertEquals("NA", worldBuilding.getMilSpaceType());
            assertEquals("NA", worldBuilding.getMilPlanType());
        }

        @Test
        @DisplayName("should have false as default for boolean fields")
        void shouldHaveFalseAsDefault() {
            StarWorldBuilding worldBuilding = new StarWorldBuilding();

            assertFalse(worldBuilding.isOther());
            assertFalse(worldBuilding.isAnomaly());
        }
    }

    // Helper method
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
