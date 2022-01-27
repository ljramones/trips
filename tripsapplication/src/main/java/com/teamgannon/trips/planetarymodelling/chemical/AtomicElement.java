package com.teamgannon.trips.planetarymodelling.chemical;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

/**
 * definition for an atomic element
 * <p>
 * e.g. Hydrogen, 1, H, 1.01
 */
@Data
public class AtomicElement {

    /**
     * the element name
     */
    @CsvBindByPosition(position = 0)
    private String name;

    /**
     * the atomic number (number of protons)
     */
    @CsvBindByPosition(position = 1)
    private int atomicNumber;

    /**
     * symbol of the element
     */
    @CsvBindByPosition(position = 2)
    private String symbol;

    /**
     * atomic weight
     */
    @CsvBindByPosition(position = 3)
    private double atomicWeight;

}
