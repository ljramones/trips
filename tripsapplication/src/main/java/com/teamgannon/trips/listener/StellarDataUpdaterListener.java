package com.teamgannon.trips.listener;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.AstroSearchQuery;

public interface StellarDataUpdaterListener {

    /**
     * do a plot update based on a new search query
     *
     * @param searchQuery the search query
     * @param showPlot    show the graphical plot
     * @param showTable   show the table
     */
    void showNewStellarData(AstroSearchQuery searchQuery, boolean showPlot, boolean showTable);

    /**
     * do a plot update based on new search query
     *
     * @param dataSetDescriptor the dataset descriptor
     * @param showPlot          show the graphical plot
     * @param showTable         show the table
     */
    void showNewStellarData(DataSetDescriptor dataSetDescriptor, boolean showPlot, boolean showTable);

    /**
     * do a plot update based on default query
     *
     * @param showPlot  show the graphical plot
     * @param showTable show the table
     */
    void showNewStellarData(boolean showPlot, boolean showTable);

    /**
     * create an export file for the selected dataset
     *
     * @param newQuery the query to base our dataset on
     */
    void doExport(AstroSearchQuery newQuery);

}
