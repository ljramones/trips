package com.teamgannon.trips.solarsysmodelling.accrete;

import lombok.Data;

@Data
public class Chemical {
    private int atomicNumber;
    private String name, symbol;
    private double weight, meltingPoint, boilingPoint, density, abundanceE, abundanceS, reactivity, maxInspiredPartialPressure;

    Chemical(int an, String sym, String name, double weight, double melt, double boil, double dense, double abundE, double abundS, double react, double ipp) {
        this.atomicNumber = an;
        this.symbol = sym;
        this.name = name;
        this.weight = weight;
        this.meltingPoint = melt;
        this.boilingPoint = boil;
        this.density = dense;
        this.abundanceE = abundE; // abundance on Earth?
        this.abundanceS = abundS; // abundance in the Sun?
        this.reactivity = react;
        this.maxInspiredPartialPressure = ipp;
    }
}
