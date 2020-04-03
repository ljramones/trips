package com.teamgannon.trips.routeplanning.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NavNode {

    private int index;
    private UUID nodeId;
    private String nodeName;
    double[] coordinates = new double[3];

}
