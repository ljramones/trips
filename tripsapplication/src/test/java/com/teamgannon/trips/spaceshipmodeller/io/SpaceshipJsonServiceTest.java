package com.teamgannon.trips.spaceshipmodeller.io;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link SpaceshipJsonService} round-tripping and file IO. */
class SpaceshipJsonServiceTest {

    private final SpaceshipJsonService json = new SpaceshipJsonService();

    private SpaceshipDesign sample() {
        return SpaceshipBuilder.create("Donnager").designation("MCRN-1")
                .shipClass(ShipClass.MOTHERSHIP).driveType(DriveType.FUSION_TORCH)
                .structureTons(1000).engineTons(500).propellantTons(2000)
                .payloadTons(800).crewTons(200).radiatorTons(400)
                .crew(80).lengthMeters(500).icon("ship.png").description("flagship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("MCRN").era("~2350")
                .carry("Viper", ShipClass.FIGHTER, 12, 8, "escort").build();
    }

    @Test
    @DisplayName("single design round-trips through JSON unchanged")
    void singleRoundTrip() {
        SpaceshipDesign d = sample();
        SpaceshipDesign back = json.parseDesign(json.toJson(d));
        assertEquals(d, back);
        assertEquals(SourceType.SCIENCE_FICTION, back.sourceType());
        assertEquals("The Expanse", back.sourceUniverse());
        assertEquals("MCRN", back.faction());
        assertEquals("~2350", back.era());
    }

    @Test
    @DisplayName("a list of designs round-trips through JSON")
    void listRoundTrip() {
        SpaceshipDesign a = sample();
        SpaceshipDesign b = SpaceshipBuilder.create("Roci")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.FUSION_TORCH)
                .structureTons(200).engineTons(150).propellantTons(450).payloadTons(50).build();
        List<SpaceshipDesign> back = json.parseDesigns(json.toJson(List.of(a, b)));
        assertEquals(2, back.size());
        assertEquals(a, back.get(0));
        assertEquals(b, back.get(1));
    }

    @Test
    @DisplayName("parseDesigns also accepts a single JSON object")
    void parseDesignsAcceptsSingleObject() {
        assertEquals(1, json.parseDesigns(json.toJson(sample())).size());
    }

    @Test
    @DisplayName("export to and import from a file round-trips")
    void fileRoundTrip(@TempDir Path dir) {
        SpaceshipDesign d = sample();
        File file = dir.resolve("ship.json").toFile();
        json.exportToFile(d, file);
        assertTrue(file.exists());
        List<SpaceshipDesign> back = json.importFromFile(file);
        assertEquals(1, back.size());
        assertEquals(d, back.get(0));
    }

    @Test
    @DisplayName("invalid JSON throws a SpaceshipJsonException")
    void invalidJsonThrows() {
        assertThrows(SpaceshipJsonException.class, () -> json.parseDesign("{not valid json"));
    }

    @Test
    @DisplayName("exported JSON contains data fields but no derived accessors")
    void jsonContainsOnlyDataFields() {
        String s = json.toJson(sample());
        assertTrue(s.contains("\"driveType\""));
        assertTrue(s.contains("\"massBudget\""));
        assertFalse(s.contains("mothership"), "derived isMothership() must not be serialised");
        assertFalse(s.contains("suitableForLanding"), "derived isSuitableForLanding() must not be serialised");
    }
}
