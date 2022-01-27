package com.teamgannon.trips.graphics.entities;

public enum RouteVisibility {

    FULL("Fully"),
    PARTIAL("Partially"),
    OFFSCREEN("Not visible on this plot");

    private final String visibility;

    RouteVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getVisibility() {
        return visibility;
    }

}
