package com.teamgannon.trips.graphics.operators;

import java.util.Map;

/**
 * Used to select between various graphical panes
 * <p>
 * Created by larrymitchell on 2017-02-05.
 */
public interface ContextSelector {

    /**
     * select a interstellar system space
     *
     * @param objectProperties the properties of the selected object
     */
    void selectInterstellarSpace(Map<String, String> objectProperties);

    /**
     * select a solar system
     *
     * @param objectProperties the properties of the selected object
     */
    void selectSolarSystemSpace(Map<String, String> objectProperties);

}
