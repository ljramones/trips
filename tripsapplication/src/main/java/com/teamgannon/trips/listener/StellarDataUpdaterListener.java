package com.teamgannon.trips.listener;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.AstroSearchQuery;

import java.util.List;

public interface StellarDataUpdaterListener {

    /**
     * do a plot update based on new search query
     *
     * @param searchQuery the search query
     * @param showPlot    show the grphical plot
     * @param showTable   show the table
     */
    void showNewStellarData(AstroSearchQuery searchQuery, boolean showPlot, boolean showTable);

    /**
     * do a plot update based on default query
     *
     * @param showPlot  show the grphical plot
     * @param showTable show the table
     */
    void showNewStellarData(boolean showPlot, boolean showTable);




}
