package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the StarCatalogIds @Embeddable class.
 */
@DisplayName("StarCatalogIds Tests")
class StarCatalogIdsTest {

    @Nested
    @DisplayName("Serializable")
    class SerializableTests {

        @Test
        @DisplayName("should implement Serializable")
        void shouldImplementSerializable() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            assertTrue(catalogIds instanceof Serializable);
        }

        @Test
        @DisplayName("should serialize and deserialize correctly")
        void shouldSerializeAndDeserialize() throws IOException, ClassNotFoundException {
            StarCatalogIds original = new StarCatalogIds();
            original.setSimbadId("SIMBAD-123");
            original.setHipCatId("HIP 12345");
            original.setHdCatId("HD 98765");
            original.setGlieseCatId("GJ 551");
            original.setGaiaDR3CatId("Gaia DR3 12345");
            original.setCatalogIdList("HIP 12345,HD 98765,GJ 551");

            StarCatalogIds deserialized = serializeAndDeserialize(original);

            assertEquals(original.getSimbadId(), deserialized.getSimbadId());
            assertEquals(original.getHipCatId(), deserialized.getHipCatId());
            assertEquals(original.getHdCatId(), deserialized.getHdCatId());
            assertEquals(original.getGlieseCatId(), deserialized.getGlieseCatId());
            assertEquals(original.getGaiaDR3CatId(), deserialized.getGaiaDR3CatId());
            assertEquals(original.getCatalogIdList(), deserialized.getCatalogIdList());
        }
    }

    @Nested
    @DisplayName("initDefaults()")
    class InitDefaultsTests {

        @Test
        @DisplayName("should initialize all fields to empty strings")
        void shouldInitializeAllFieldsToEmpty() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setSimbadId("test");
            catalogIds.setHipCatId("test");

            catalogIds.initDefaults();

            assertEquals("", catalogIds.getSimbadId());
            assertEquals("", catalogIds.getBayerCatId());
            assertEquals("", catalogIds.getGlieseCatId());
            assertEquals("", catalogIds.getHipCatId());
            assertEquals("", catalogIds.getHdCatId());
            assertEquals("", catalogIds.getFlamsteedCatId());
            assertEquals("", catalogIds.getTycho2CatId());
            assertEquals("", catalogIds.getGaiaDR2CatId());
            assertEquals("", catalogIds.getGaiaDR3CatId());
            assertEquals("", catalogIds.getGaiaEDR3CatId());
            assertEquals("", catalogIds.getTwoMassCatId());
            assertEquals("", catalogIds.getCsiCatId());
            assertEquals("NA", catalogIds.getCatalogIdList());
        }
    }

    @Nested
    @DisplayName("getCatalogIdListParsed()")
    class CatalogIdListParsedTests {

        @Test
        @DisplayName("should return empty list for null")
        void shouldReturnEmptyForNull() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList(null);

            List<String> result = catalogIds.getCatalogIdListParsed();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for empty string")
        void shouldReturnEmptyForEmptyString() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList("");

            List<String> result = catalogIds.getCatalogIdListParsed();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for NA")
        void shouldReturnEmptyForNA() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList("NA");

            List<String> result = catalogIds.getCatalogIdListParsed();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should parse comma-separated list")
        void shouldParseCommaSeparatedList() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList("HIP 12345, HD 98765, GJ 551");

            List<String> result = catalogIds.getCatalogIdListParsed();

            assertEquals(3, result.size());
            assertEquals("HIP 12345", result.get(0));
            assertEquals("HD 98765", result.get(1));
            assertEquals("GJ 551", result.get(2));
        }

        @Test
        @DisplayName("should handle single entry")
        void shouldHandleSingleEntry() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList("HIP 12345");

            List<String> result = catalogIds.getCatalogIdListParsed();

            assertEquals(1, result.size());
            assertEquals("HIP 12345", result.get(0));
        }
    }

    @Nested
    @DisplayName("hasCatalogEntry()")
    class HasCatalogEntryTests {

        @Test
        @DisplayName("should return false for null catalog list")
        void shouldReturnFalseForNull() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList(null);

            assertFalse(catalogIds.hasCatalogEntry("HIP"));
        }

        @Test
        @DisplayName("should return true when prefix exists")
        void shouldReturnTrueWhenPrefixExists() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList("HIP 12345, HD 98765");

            assertTrue(catalogIds.hasCatalogEntry("HIP"));
            assertTrue(catalogIds.hasCatalogEntry("HD"));
        }

        @Test
        @DisplayName("should return false when prefix does not exist")
        void shouldReturnFalseWhenPrefixNotExists() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            catalogIds.setCatalogIdList("HIP 12345, HD 98765");

            assertFalse(catalogIds.hasCatalogEntry("GJ"));
            assertFalse(catalogIds.hasCatalogEntry("TYC"));
        }
    }

    @Nested
    @DisplayName("getRawCatalogIdList()")
    class GetRawCatalogIdListTests {

        @Test
        @DisplayName("should return raw string")
        void shouldReturnRawString() {
            StarCatalogIds catalogIds = new StarCatalogIds();
            String raw = "HIP 12345, HD 98765, GJ 551";
            catalogIds.setCatalogIdList(raw);

            assertEquals(raw, catalogIds.getRawCatalogIdList());
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
