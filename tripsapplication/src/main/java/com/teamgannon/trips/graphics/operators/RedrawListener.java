package com.teamgannon.trips.graphics.operators;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;

/**
 * event listener for triggering a redraw of the main screen
 */
public interface RedrawListener {

    void recenter(StarDisplayRecord starId);
}
