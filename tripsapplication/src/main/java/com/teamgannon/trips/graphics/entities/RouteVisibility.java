package com.teamgannon.trips.graphics.entities;

public enum RouteVisibility {

    FULL("Full"),
    PARTIAL("Partial"),
    INVISIBLE("Invisible");

    private String visibility;

    RouteVisibility(String visiblity) {
        this.visibility = visiblity;
    }

    public String getVisibility() {
        return visibility;
    }

}
