package com.teamgannon.trips.workbench.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;

/**
 * Display model for showing exoplanet-to-star match results in table.
 */
@Data
public class ExoplanetMatchRow {

    private String exoplanetName;
    private String csvStarName;
    private String matchedStarName;
    private String matchedStarId;
    private String matchType;
    private String confidence;

    /**
     * Selection state for import (bound to checkbox in table).
     */
    private final BooleanProperty selected = new SimpleBooleanProperty(true);

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    /**
     * Check if this row has a valid match that can be imported.
     */
    public boolean hasMatch() {
        return matchedStarId != null && !matchedStarId.isBlank();
    }
}
