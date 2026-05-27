package com.teamgannon.trips.solarsystem.modelling.accrete;

import lombok.Data;

@Data
public class WDouble {
    double value = 0.0;

    WDouble(double d) {
        value = d;
    }
}