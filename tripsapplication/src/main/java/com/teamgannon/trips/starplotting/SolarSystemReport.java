package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.solarsysmodelling.accrete.StarSystem;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class SolarSystemReport {

    /**
     * whether we measure or not
     */
    private boolean saveSelected;

    /**
     * out source star to preform measurements
     */
    private StarObject sourceStar;

    /**
     * generation options
     */
    private SolarSystemGenOptions options;

    /**
     * the report
     */
    private String generatedReport;

    public SolarSystemReport(StarObject starDisplayRecord, SolarSystemGenOptions options) {
        this.sourceStar = starDisplayRecord;
        this.options = options;
    }

    public void generateReport() {
        StarSystem starSystem = new StarSystem(sourceStar,
                options.isCreateMoons(),
                options.isVerbose(),
                options.isExtraVerbose());
        this.generatedReport = starSystem.toString();
    }

    /**
     * tell the recipient whether we save thi report or not
     *
     * @param save true is save
     */
    public void setSave(boolean save) {
        this.saveSelected = save;
    }

}
