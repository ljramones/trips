package com.teamgannon.trips.listener;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;

public interface RoutingPanelListener {

    /**
     * update the routing panel
     */
    void updateRoutingPanel(DataSetDescriptor dataSetDescriptor);

}
