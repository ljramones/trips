package com.teamgannon.trips.constellation;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConstellationLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void initializeLoadsConstellationsFromProgramData() throws Exception {
        Path csv = tempDir.resolve("constellation.csv");
        Files.writeString(csv,
                """
                Orion,Ori,ORI,Orions,Greek,Hunter,Rigel
                Lyra,Lyr,LYR,Lyras,Greek,Lyre,Vega
                """,
                StandardCharsets.UTF_8);

        Localization localization = new Localization();
        localization.setProgramdata(tempDir.toString());

        TripsContext tripsContext = mock(TripsContext.class);
        Map<String, Constellation> constellationMap = new HashMap<>();
        when(tripsContext.getConstellationMap()).thenReturn(constellationMap);

        ConstellationLoader loader = new ConstellationLoader(tripsContext, localization);
        loader.initialize();

        assertEquals(2, constellationMap.size());
        assertNotNull(constellationMap.get("Orion"));
        assertEquals("Ori", constellationMap.get("Orion").getIauAbbr());
    }
}
