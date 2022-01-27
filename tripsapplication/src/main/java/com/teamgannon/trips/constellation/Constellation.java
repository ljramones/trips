package com.teamgannon.trips.constellation;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Builder;
import lombok.Data;

@Data
public class Constellation {

    /**
     * constellation name
     */
    @CsvBindByPosition(position = 0)
    private String name;

    /**
     * the IAU abbreviation
     */
    @CsvBindByPosition(position = 1)
    private String iauAbbr;

    /**
     * the NASA abbreviation
     */
    @CsvBindByPosition(position = 2)
    private String nasaAbbr;

    /**
     * the plural name
     */
    @CsvBindByPosition(position = 3)
    private String pluralName;

    /**
     * the origin
     */
    @CsvBindByPosition(position = 4)
    private String origin;

    /**
     * the meaning
     */
    @CsvBindByPosition(position = 5)
    private String meaning;

    /**
     * the brightest star
     */
    @CsvBindByPosition(position = 6)
    private String brightestStar;


}
