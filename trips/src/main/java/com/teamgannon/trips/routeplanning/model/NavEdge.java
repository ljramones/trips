package com.teamgannon.trips.routeplanning.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NavEdge {
    private NavNode first;
    private NavNode second;
    private double distance;
}
