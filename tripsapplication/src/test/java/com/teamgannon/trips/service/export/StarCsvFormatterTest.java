package com.teamgannon.trips.service.export;

import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StarCsvFormatterTest {

    @Test
    void formatUsesImportCompatibleHeaderAndEscapesCommas() {
        StarObject star = new StarObject();
        star.setDataSetName("nearby");
        star.setDisplayName("Alpha, Centauri");
        star.setCommonName("Rigil, Kentaurus");
        star.setSpectralClass("G2V");
        star.setLuminosity("1.52");
        star.setGaiaDR3CatId("gaia-3");
        star.setX(1.0);
        star.setY(2.0);
        star.setZ(3.0);

        String csv = StarCsvFormatter.headers() + StarCsvFormatter.format(star);

        assertThat(csv).startsWith("id,dataSetName,displayName,commonName");
        assertThat(csv).contains("nearby, Alpha~ Centauri, Rigil~ Kentaurus");
        assertThat(csv).contains(", G2V, ");
        assertThat(csv).endsWith("1.0, 2.0, 3.0, 0.0\n");
    }
}
