package com.teamgannon.trips.listener;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;

import java.util.Map;

/**
 * Used to select between various graphical panes
 * <p>
 * Created by larrymitchell on 2017-02-05.
 */
public interface ContextSelectorListener {

    /**
     * select a interstellar system space
     *
     * @param objectProperties the properties of the selected object
     */
    void selectInterstellarSpace(Map<String, String> objectProperties);

    /**
     * select a solar system
     *
     * @param starDisplayRecord the properties of the selected object
     */
    void selectSolarSystemSpace(StarDisplayRecord starDisplayRecord);

}
