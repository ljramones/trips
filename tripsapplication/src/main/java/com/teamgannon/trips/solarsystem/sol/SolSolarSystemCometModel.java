package com.teamgannon.trips.solarsystem.sol;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;


@Data
public class SolSolarSystemCometModel {

    @CsvBindByPosition(position = 0)
    private String number;

    @CsvBindByPosition(position = 1)
    private String name;

    @CsvBindByPosition(position = 2)
    private double orbitalPeriod;

    @CsvBindByPosition(position = 3)
    private String perihelionDate;

    @CsvBindByPosition(position = 4)
    private double perihelionDistance;

    @CsvBindByPosition(position = 5)
    private double semiMajorAxis;

    @CsvBindByPosition(position = 6)
    private double orbitEccentricity;

    @CsvBindByPosition(position = 7)
    private double orbitalInclination;

    @CsvBindByPosition(position = 8)
    private double absoluteMagnitude;

}
