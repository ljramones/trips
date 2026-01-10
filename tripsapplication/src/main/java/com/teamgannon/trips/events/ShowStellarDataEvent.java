package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.AstroSearchQuery;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when stellar data needs to be shown/refreshed.
 * This can be triggered by a search query, dataset descriptor, or default parameters.
 */
@Getter
public class ShowStellarDataEvent extends ApplicationEvent {

    private final AstroSearchQuery searchQuery;
    private final DataSetDescriptor dataSetDescriptor;
    private final boolean showPlot;
    private final boolean showTable;

    /**
     * Constructor for showing data based on a search query
     */
    public ShowStellarDataEvent(Object source, AstroSearchQuery searchQuery, boolean showPlot, boolean showTable) {
        super(source);
        this.searchQuery = searchQuery;
        this.dataSetDescriptor = null;
        this.showPlot = showPlot;
        this.showTable = showTable;
    }

    /**
     * Constructor for showing data based on a dataset descriptor
     */
    public ShowStellarDataEvent(Object source, DataSetDescriptor dataSetDescriptor, boolean showPlot, boolean showTable) {
        super(source);
        this.searchQuery = null;
        this.dataSetDescriptor = dataSetDescriptor;
        this.showPlot = showPlot;
        this.showTable = showTable;
    }

    /**
     * Constructor for showing data with default parameters
     */
    public ShowStellarDataEvent(Object source, boolean showPlot, boolean showTable) {
        super(source);
        this.searchQuery = null;
        this.dataSetDescriptor = null;
        this.showPlot = showPlot;
        this.showTable = showTable;
    }

    public boolean hasSearchQuery() {
        return searchQuery != null;
    }

    public boolean hasDataSetDescriptor() {
        return dataSetDescriptor != null;
    }
}
