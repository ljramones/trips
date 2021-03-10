package com.teamgannon.trips.config.application;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ConstellationSet {

    /**
     * the list of constellations
     */
    private List<Constellation> constellationList = new ArrayList<>();

    /**
     * the hashset to lookup
     */
    private Set<String> lookup = new HashSet<>();

    public  void setup() {
        for (Constellation constellation: constellationList) {
            lookup.add(constellation.getIauAbbr());
        }
    }

}
