package com.teamgannon.trips.routing.model;

import lombok.Data;

import java.util.UUID;

@Data
public class SparseStarRecord {

    private String recordId;

    private String starName;

    private double[] actualCoordinates = new double[3];

}
