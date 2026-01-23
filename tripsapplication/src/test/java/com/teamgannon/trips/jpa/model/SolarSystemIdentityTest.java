package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SolarSystem identity methods (equals, hashCode) and Serializable implementation.
 */
@DisplayName("SolarSystem Identity and Serialization")
class SolarSystemIdentityTest {

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            SolarSystem system1 = new SolarSystem("Alpha Centauri");
            // Both constructors generate UUIDs, so we need to set the same ID
            String sharedId = system1.getId();

            SolarSystem system2 = new SolarSystem("Different Name");
            system2.setId(sharedId);

            assertEquals(system1, system2);
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            SolarSystem system1 = new SolarSystem("Alpha Centauri");
            SolarSystem system2 = new SolarSystem("Alpha Centauri");
            // Each has a unique UUID from constructor

            assertNotEquals(system1, system2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            SolarSystem system = new SolarSystem("Test System");

            assertNotEquals(null, system);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            SolarSystem system = new SolarSystem("Test System");

            assertNotEquals("Test System", system);
            assertNotEquals(new Object(), system);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            SolarSystem system = new SolarSystem("Test System");

            assertEquals(system, system);
        }

        @Test
        @DisplayName("should handle null ID correctly")
        void shouldHandleNullId() {
            SolarSystem system1 = new SolarSystem();
            system1.setId(null);

            SolarSystem system2 = new SolarSystem();
            system2.setId(null);

            // Two entities with null IDs should not be equal (JPA convention)
            assertNotEquals(system1, system2);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            SolarSystem system = new SolarSystem("Test System");

            int hash1 = system.hashCode();
            int hash2 = system.hashCode();

            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("equal objects should have same hashCode")
        void equalObjectsShouldHaveSameHashCode() {
            SolarSystem system1 = new SolarSystem("System A");
            String sharedId = system1.getId();

            SolarSystem system2 = new SolarSystem("System B");
            system2.setId(sharedId);

            assertEquals(system1, system2);
            assertEquals(system1.hashCode(), system2.hashCode());
        }

        @Test
        @DisplayName("should work correctly in HashSet")
        void shouldWorkInHashSet() {
            SolarSystem system1 = new SolarSystem("System 1");
            String id1 = system1.getId();

            SolarSystem system2 = new SolarSystem("System 2");
            system2.setId(id1); // Same ID as system1

            SolarSystem system3 = new SolarSystem("System 3");

            Set<SolarSystem> set = new HashSet<>();
            set.add(system1);
            set.add(system2); // Same ID, should not be added
            set.add(system3);

            assertEquals(2, set.size());
            assertTrue(set.contains(system1));
            assertTrue(set.contains(system3));
        }
    }

    @Nested
    @DisplayName("Serializable Implementation")
    class SerializableTests {

        @Test
        @DisplayName("should be serializable")
        void shouldBeSerializable() {
            SolarSystem system = new SolarSystem("Test System");

            assertTrue(system instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            SolarSystem original = new SolarSystem("Serialization Test System", "star-123");
            original.setDataSetName("TestDataset");
            original.setStarCount(2);
            original.setPlanetCount(5);
            original.setHasAsteroidBelt(true);
            original.setHasHabitableZonePlanets(true);
            original.setHabitableZoneInnerAU(0.8);
            original.setHabitableZoneOuterAU(1.5);
            original.setDistanceFromSol(4.37);
            original.setPolity("Federation");
            original.setColonized(true);

            // Serialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            // Deserialize
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            SolarSystem deserialized = (SolarSystem) ois.readObject();
            ois.close();

            // Verify all fields
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getSystemName(), deserialized.getSystemName());
            assertEquals(original.getPrimaryStarId(), deserialized.getPrimaryStarId());
            assertEquals(original.getDataSetName(), deserialized.getDataSetName());
            assertEquals(original.getStarCount(), deserialized.getStarCount());
            assertEquals(original.getPlanetCount(), deserialized.getPlanetCount());
            assertEquals(original.isHasAsteroidBelt(), deserialized.isHasAsteroidBelt());
            assertEquals(original.isHasHabitableZonePlanets(), deserialized.isHasHabitableZonePlanets());
            assertEquals(original.getHabitableZoneInnerAU(), deserialized.getHabitableZoneInnerAU());
            assertEquals(original.getHabitableZoneOuterAU(), deserialized.getHabitableZoneOuterAU());
            assertEquals(original.getDistanceFromSol(), deserialized.getDistanceFromSol());
            assertEquals(original.getPolity(), deserialized.getPolity());
            assertEquals(original.isColonized(), deserialized.isColonized());
        }

        @Test
        @DisplayName("serialized object should maintain equality")
        void serializedObjectShouldMaintainEquality() throws IOException, ClassNotFoundException {
            SolarSystem original = new SolarSystem("Equality Test System");

            // Serialize and deserialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            SolarSystem deserialized = (SolarSystem) ois.readObject();
            ois.close();

            assertEquals(original, deserialized);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("fromStar should create system with correct properties")
        void fromStarShouldCreateSystemCorrectly() {
            StarObject star = new StarObject();
            star.setId("star-456");
            star.setDisplayName("Proxima Centauri");
            star.setDataSetName("TestDataset");
            star.setDistance(4.24);
            star.setPolity("Earth Alliance");

            SolarSystem system = SolarSystem.fromStar(star);

            assertNotNull(system.getId());
            assertEquals("Proxima Centauri system", system.getSystemName());
            assertEquals("star-456", system.getPrimaryStarId());
            assertEquals("TestDataset", system.getDataSetName());
            assertEquals(4.24, system.getDistanceFromSol());
            assertEquals("Earth Alliance", system.getPolity());
            assertEquals(1, system.getStarCount());
        }
    }

    @Nested
    @DisplayName("Helper Methods")
    class HelperMethodTests {

        @Test
        @DisplayName("isMultiStarSystem should return correct value")
        void isMultiStarSystemShouldWork() {
            SolarSystem single = new SolarSystem("Single Star");
            single.setStarCount(1);
            assertFalse(single.isMultiStarSystem());

            SolarSystem binary = new SolarSystem("Binary Star");
            binary.setStarCount(2);
            assertTrue(binary.isMultiStarSystem());

            SolarSystem trinary = new SolarSystem("Trinary Star");
            trinary.setStarCount(3);
            assertTrue(trinary.isMultiStarSystem());
        }

        @Test
        @DisplayName("hasPlanets should return correct value")
        void hasPlanetsShouldWork() {
            SolarSystem noPlanets = new SolarSystem("No Planets");
            noPlanets.setPlanetCount(0);
            assertFalse(noPlanets.hasPlanets());

            SolarSystem withPlanets = new SolarSystem("With Planets");
            withPlanets.setPlanetCount(3);
            assertTrue(withPlanets.hasPlanets());
        }
    }
}
