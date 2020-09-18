package com.teamgannon.trips.listener;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;

import java.util.Map;

/**
 * Used to provide a feedback
 * <p>
 * Created by larrymitchell on 2017-02-01.
 */
public interface ListUpdaterListener {

    /**
     * update the list
     *
     * @param starDisplayRecord the list item
     */
    void updateList(StarDisplayRecord starDisplayRecord);

    /**
     * clear the entire list
     */
    void clearList();

}
