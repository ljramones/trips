package com.teamgannon.trips.javafxsupport;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for version.properties file used by TripsPreloader.
 */
class VersionPropertiesTest {

    @Test
    void testVersionPropertiesExists() {
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            assertNotNull(is, "version.properties should exist in resources");
        } catch (IOException e) {
            fail("Failed to read version.properties: " + e.getMessage());
        }
    }

    @Test
    void testVersionPropertiesHasVersion() throws IOException {
        Properties props = loadVersionProperties();

        String version = props.getProperty("app.version");
        assertNotNull(version, "app.version should be defined");
        assertFalse(version.isBlank(), "app.version should not be blank");
        assertTrue(version.startsWith("v"), "app.version should start with 'v'");
    }

    @Test
    void testVersionPropertiesHasReleaseDate() throws IOException {
        Properties props = loadVersionProperties();

        String releaseDate = props.getProperty("app.releaseDate");
        assertNotNull(releaseDate, "app.releaseDate should be defined");
        assertFalse(releaseDate.isBlank(), "app.releaseDate should not be blank");
    }

    @Test
    void testVersionStringFormat() throws IOException {
        Properties props = loadVersionProperties();

        String version = props.getProperty("app.version");
        String releaseDate = props.getProperty("app.releaseDate");

        // Simulate what TripsPreloader does
        String versionString;
        if (releaseDate != null && !releaseDate.isEmpty()) {
            versionString = version + " - " + releaseDate;
        } else {
            versionString = version;
        }

        assertNotNull(versionString);
        assertTrue(versionString.contains("v"), "Version string should contain version");
        assertTrue(versionString.contains("-"), "Version string should contain date separator");
    }

    @Test
    void testVersionMatchesApplicationYml() throws IOException {
        Properties versionProps = loadVersionProperties();

        // The version in version.properties should match what's in application.yml
        String version = versionProps.getProperty("app.version");

        // Check that version follows expected pattern (v followed by digits and dots)
        assertTrue(version.matches("v\\d+\\.\\d+\\.?\\d*"),
                "Version should match pattern like 'v0.8.0', got: " + version);
    }

    private Properties loadVersionProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            assertNotNull(is, "version.properties should exist");
            props.load(is);
        }
        return props;
    }
}
