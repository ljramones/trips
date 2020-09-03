package com.teamgannon.trips.controller.support;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StellarDataUpdaterListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;


public class DataSetDescriptorCellFactory implements Callback<ListView<DataSetDescriptor>, ListCell<DataSetDescriptor>> {

    private StellarDataUpdaterListener updater;

    public DataSetDescriptorCellFactory(StellarDataUpdaterListener updater) {
        this.updater = updater;
    }

    @Override
    public ListCell<DataSetDescriptor> call(ListView<DataSetDescriptor> dataSetDescriptorListView) {
        return new DataSetDescriptorCell(updater);
    }

}
