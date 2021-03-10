package com.teamgannon.trips.config.application;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Constellation {

    /**
     * constellation name
     */
    private String name;

    /**
     * the IAU abbreviation
     */
    private String iauAbbr;

    /**
     * the NASA abbreviation
     */
    private String nasaAbbr;

    /**
     * the brighest star
     */
    private String brightestStar;

}
