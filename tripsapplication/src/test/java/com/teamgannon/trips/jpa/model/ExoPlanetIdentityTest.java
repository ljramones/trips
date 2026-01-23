package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExoPlanet identity methods (equals, hashCode) and Serializable implementation.
 */
@DisplayName("ExoPlanet Identity and Serialization")
class ExoPlanetIdentityTest {

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            ExoPlanet planet1 = new ExoPlanet();
            planet1.setId("planet-123");
            planet1.setName("Kepler-442b");

            ExoPlanet planet2 = new ExoPlanet();
            planet2.setId("planet-123");
            planet2.setName("Different Name");

            assertEquals(planet1, planet2);
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            ExoPlanet planet1 = new ExoPlanet();
            planet1.setId("planet-123");
            planet1.setName("Kepler-442b");

            ExoPlanet planet2 = new ExoPlanet();
            planet2.setId("planet-456");
            planet2.setName("Kepler-442b");

            assertNotEquals(planet1, planet2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            ExoPlanet planet = new ExoPlanet();
            planet.setId("planet-123");

            assertNotEquals(null, planet);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            ExoPlanet planet = new ExoPlanet();
            planet.setId("planet-123");

            assertNotEquals("planet-123", planet);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            ExoPlanet planet = new ExoPlanet();
            planet.setId("planet-123");

            assertEquals(planet, planet);
        }

        @Test
        @DisplayName("should handle null ID correctly")
        void shouldHandleNullId() {
            ExoPlanet planet1 = new ExoPlanet();
            planet1.setId(null);

            ExoPlanet planet2 = new ExoPlanet();
            planet2.setId(null);

            // Two entities with null IDs should not be equal (JPA convention)
            assertNotEquals(planet1, planet2);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            ExoPlanet planet = new ExoPlanet();
            planet.setId("planet-123");

            int hash1 = planet.hashCode();
            int hash2 = planet.hashCode();

            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("equal objects should have same hashCode")
        void equalObjectsShouldHaveSameHashCode() {
            ExoPlanet planet1 = new ExoPlanet();
            planet1.setId("planet-123");

            ExoPlanet planet2 = new ExoPlanet();
            planet2.setId("planet-123");

            assertEquals(planet1, planet2);
            assertEquals(planet1.hashCode(), planet2.hashCode());
        }

        @Test
        @DisplayName("should work correctly in HashSet")
        void shouldWorkInHashSet() {
            ExoPlanet planet1 = new ExoPlanet();
            planet1.setId("planet-123");

            ExoPlanet planet2 = new ExoPlanet();
            planet2.setId("planet-123");

            ExoPlanet planet3 = new ExoPlanet();
            planet3.setId("planet-456");

            Set<ExoPlanet> set = new HashSet<>();
            set.add(planet1);
            set.add(planet2); // Same ID, should not be added
            set.add(planet3);

            assertEquals(2, set.size());
            assertTrue(set.contains(planet1));
            assertTrue(set.contains(planet3));
        }
    }

    @Nested
    @DisplayName("Serializable Implementation")
    class SerializableTests {

        @Test
        @DisplayName("should be serializable")
        void shouldBeSerializable() {
            ExoPlanet planet = new ExoPlanet();
            planet.setId(UUID.randomUUID().toString());
            planet.setName("Test Planet");
            planet.setSemiMajorAxis(1.5);
            planet.setMass(2.0);

            assertTrue(planet instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            ExoPlanet original = new ExoPlanet();
            original.setId(UUID.randomUUID().toString());
            original.setName("Serialization Test Planet");
            original.setSemiMajorAxis(1.234);
            original.setMass(5.678);
            original.setEccentricity(0.05);
            original.setPlanetStatus("Confirmed");

            // Serialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            // Deserialize
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            ExoPlanet deserialized = (ExoPlanet) ois.readObject();
            ois.close();

            // Verify
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getName(), deserialized.getName());
            assertEquals(original.getSemiMajorAxis(), deserialized.getSemiMajorAxis());
            assertEquals(original.getMass(), deserialized.getMass());
            assertEquals(original.getEccentricity(), deserialized.getEccentricity());
            assertEquals(original.getPlanetStatus(), deserialized.getPlanetStatus());
        }

        @Test
        @DisplayName("serialized object should maintain equality")
        void serializedObjectShouldMaintainEquality() throws IOException, ClassNotFoundException {
            ExoPlanet original = new ExoPlanet();
            original.setId("equality-test-id");
            original.setName("Equality Test");

            // Serialize and deserialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            ExoPlanet deserialized = (ExoPlanet) ois.readObject();
            ois.close();

            assertEquals(original, deserialized);
        }
    }
}
