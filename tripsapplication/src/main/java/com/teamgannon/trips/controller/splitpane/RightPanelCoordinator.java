package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatasetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RightPanelCoordinator {

    private final RightPanelController rightPanelController;
    private final DataSetPanelCoordinator dataSetPanelCoordinator;
    private final DatasetService datasetService;
    private final ApplicationEventPublisher eventPublisher;
    private final SearchContextCoordinator searchContextCoordinator;

    public RightPanelCoordinator(RightPanelController rightPanelController,
                                 DataSetPanelCoordinator dataSetPanelCoordinator,
                                 DatasetService datasetService,
                                 ApplicationEventPublisher eventPublisher,
                                 SearchContextCoordinator searchContextCoordinator) {
        this.rightPanelController = rightPanelController;
        this.dataSetPanelCoordinator = dataSetPanelCoordinator;
        this.datasetService = datasetService;
        this.eventPublisher = eventPublisher;
        this.searchContextCoordinator = searchContextCoordinator;
    }

    public void initialize() {
        dataSetPanelCoordinator.loadDataSets();
        rightPanelController.setupDataSetView(datasetService, eventPublisher, searchContextCoordinator);
        rightPanelController.setupObjectViewPane();
        log.info("Right panel initialized");
    }

    public void refreshDataSets() {
        dataSetPanelCoordinator.refreshDataSets();
        rightPanelController.refreshDataSets(datasetService);
    }

    public void addDataSetToContext(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.addDataSet(descriptor);
    }

    public void removeDataSetFromContext(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.removeDataSet(descriptor);
    }

    public void handleDataSetAdded(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.addDataSet(descriptor);
        refreshDataSets();
    }

    public void handleDataSetRemoved(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.removeDataSet(descriptor);
        refreshDataSets();
    }
}
