package com.teamgannon.trips.listener;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;

import java.util.UUID;

/**
 * event listener for triggering a redraw of the main screen
 */
public interface RedrawListener {

    void recenter(StarDisplayRecord starId);

    void highlightStar(UUID starId);
}
