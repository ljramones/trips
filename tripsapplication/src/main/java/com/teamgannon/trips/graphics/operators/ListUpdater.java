package com.teamgannon.trips.graphics.operators;

import java.util.Map;

/**
 * Used to provide a feedback
 * <p>
 * Created by larrymitchell on 2017-02-01.
 */
public interface ListUpdater {

    /**
     * update the list
     *
     * @param listItem the list item
     */
    void updateList(Map<String, String> listItem);

    /**
     * clear the entire list
     */
    void clearList();

}
