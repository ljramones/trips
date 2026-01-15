package com.teamgannon.trips.solarsystem.nightsky;

import java.util.List;

public final class PlanetarySkyModel {

    private final List<VisibleStarResult> visibleStars;
    private final List<VisibleStarResult> topBrightest;
    private final int visibleCount;
    private final double hostStarAltitudeDeg;
    private final double effectiveMagnitudeLimit;

    public PlanetarySkyModel(List<VisibleStarResult> visibleStars,
                             List<VisibleStarResult> topBrightest,
                             int visibleCount,
                             double hostStarAltitudeDeg,
                             double effectiveMagnitudeLimit) {
        this.visibleStars = visibleStars;
        this.topBrightest = topBrightest;
        this.visibleCount = visibleCount;
        this.hostStarAltitudeDeg = hostStarAltitudeDeg;
        this.effectiveMagnitudeLimit = effectiveMagnitudeLimit;
    }

    public List<VisibleStarResult> getVisibleStars() {
        return visibleStars;
    }

    public List<VisibleStarResult> getTopBrightest() {
        return topBrightest;
    }

    public int getVisibleCount() {
        return visibleCount;
    }

    public double getHostStarAltitudeDeg() {
        return hostStarAltitudeDeg;
    }

    public double getEffectiveMagnitudeLimit() {
        return effectiveMagnitudeLimit;
    }
}
