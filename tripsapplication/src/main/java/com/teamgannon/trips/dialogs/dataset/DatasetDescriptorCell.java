package com.teamgannon.trips.dialogs.dataset;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;

public class DatasetDescriptorCell extends ListCell<DataSetDescriptor> {

    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    final Tooltip tooltip = new Tooltip();

   public DatasetDescriptorCell() {
       super.setPrefWidth(100);
    }

    @Override
    public void updateItem(DataSetDescriptor descriptor, boolean empty) {
        super.updateItem(descriptor, empty);

        super.updateItem(descriptor, empty);
        if (descriptor != null) {
            setText(descriptor.getDataSetName());
        }
        else {
            setText(null);
        }
    }

}
