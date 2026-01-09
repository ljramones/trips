package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;


public class DataSetDescriptorCellFactory implements Callback<ListView<DataSetDescriptor>, ListCell<DataSetDescriptor>> {

    private final ApplicationEventPublisher eventPublisher;

    public DataSetDescriptorCellFactory(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public @NotNull ListCell<DataSetDescriptor> call(ListView<DataSetDescriptor> dataSetDescriptorListView) {
        return new DataSetDescriptorCell(eventPublisher);
    }

}
