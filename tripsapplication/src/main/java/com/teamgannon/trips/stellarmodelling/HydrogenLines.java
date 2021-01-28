package com.teamgannon.trips.stellarmodelling;

/**
 * The type of hydrogen lines
 * <p>
 * Created by larrymitchell on 2017-02-18.
 */
public enum HydrogenLines {

    WEAK("weak"),
    MEDIUM("medium"),
    STRONG("string"),
    VERY_WEAK("very weak");

    private final String lines;

    HydrogenLines(String lines) {
        this.lines = lines;
    }

    public String lines() {
        return lines;
    }
}
