package com.teamgannon.trips.listener;

import com.teamgannon.trips.jpa.model.StarObject;

/**
 * Used to display a property set for a stellar object
 * <p>
 * Created by larrymitchell on 2017-02-02.
 */
public interface StellarPropertiesDisplayerListener {

    void displayStellarProperties(StarObject starObject);

    void clearData();

}
