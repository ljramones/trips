package com.teamgannon.trips.solarsysmodelling.accrete;

import lombok.Data;

@Data
public class WDouble {
    double value = 0.0;
    WDouble(double d) {
        value = d;
    }
}