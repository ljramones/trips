package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StellarDataUpdaterListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;


public class DataSetDescriptorCellFactory implements Callback<ListView<DataSetDescriptor>, ListCell<DataSetDescriptor>> {

    private final DataSetChangeListener dataSetChangeListener;
    private final StellarDataUpdaterListener updater;

    public DataSetDescriptorCellFactory(DataSetChangeListener dataSetChangeListener,
                                        StellarDataUpdaterListener stellarDataUpdaterListener) {
        this.dataSetChangeListener = dataSetChangeListener;
        this.updater = stellarDataUpdaterListener;
    }

    @Override
    public @NotNull ListCell<DataSetDescriptor> call(ListView<DataSetDescriptor> dataSetDescriptorListView) {
        return new DataSetDescriptorCell(dataSetChangeListener, updater);
    }

}
