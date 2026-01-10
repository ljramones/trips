package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolarSystemGenOptions {

    private StarObject starObject;

    private boolean createMoons;

    private boolean verbose;

    private boolean extraVerbose;

    private boolean doGeneration;

}
