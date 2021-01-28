package com.teamgannon.trips.solarsysmodelling.accrete;

import lombok.Data;

@Data
public class DustRecord {
    DustRecord next = null;
    double innerEdge = 0.0, outerEdge = 0.0;
    boolean dustPresent = false, gasPresent = false;
}