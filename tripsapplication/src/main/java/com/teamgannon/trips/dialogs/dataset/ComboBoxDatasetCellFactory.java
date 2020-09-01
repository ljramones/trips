package com.teamgannon.trips.dialogs.dataset;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ComboBoxDatasetCellFactory implements Callback<ListView<DataSetDescriptor>, ListCell<DataSetDescriptor>> {

    @Override
    public ListCell<DataSetDescriptor> call(ListView<DataSetDescriptor> listview) {
        return new DatasetDescriptorCell();
    }

}