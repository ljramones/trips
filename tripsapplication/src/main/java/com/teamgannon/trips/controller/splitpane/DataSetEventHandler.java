package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.javafxsupport.BackgroundTaskRunner;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CancellationException;

import javafx.scene.control.ButtonType;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Handles dataset-related events (add, remove, set context).
 * <p>
 * This class manages dataset lifecycle events, coordinating between
 * the right panel coordinator, trips context, and query dialog.
 */
@Slf4j
@Component
public class DataSetEventHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final TripsContext tripsContext;
    private final RightPanelCoordinator rightPanelCoordinator;
    private final InterstellarSpacePane interstellarSpacePane;

    private RightPanelController rightPanelController;

    /**
     * Reference to the query dialog (optional, set after initialization).
     */
    @Setter
    private QueryDialog queryDialog;

    public DataSetEventHandler(ApplicationEventPublisher eventPublisher,
                               TripsContext tripsContext,
                               RightPanelCoordinator rightPanelCoordinator,
                               InterstellarSpacePane interstellarSpacePane) {
        this.eventPublisher = eventPublisher;
        this.tripsContext = tripsContext;
        this.rightPanelCoordinator = rightPanelCoordinator;
        this.interstellarSpacePane = interstellarSpacePane;
    }

    /**
     * Initialize with the right panel controller (called after construction).
     *
     * @param rightPanelController the right panel controller
     */
    public void initialize(RightPanelController rightPanelController) {
        this.rightPanelController = rightPanelController;
    }

    @EventListener
    public void onAddDataSetEvent(AddDataSetEvent event) {
        DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();

        FxThread.runOnFxThread(() -> {
            try {
                // add to side-panel
                rightPanelCoordinator.handleDataSetAdded(dataSetDescriptor);

                // update the query dialog
                if (queryDialog != null) {
                    queryDialog.updateDataContext(dataSetDescriptor);
                }
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset: " + dataSetDescriptor.getDataSetName() + " loaded"));
            } catch (Exception e) {
                log.error("Error handling add dataset event for {}", dataSetDescriptor.getDataSetName(), e);
                showErrorAlert("Add Dataset Error", "Failed to add dataset: " + e.getMessage());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to add dataset"));
            }
        });
    }

    @EventListener
    public void onRemoveDataSetEvent(RemoveDataSetEvent event) {
        DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();
        FxThread.runOnFxThread(() -> {
            Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                    "Remove",
                    "Are you sure you want to remove: " + dataSetDescriptor.getDataSetName());

            if ((buttonType.isPresent()) && (buttonType.get() == ButtonType.OK)) {
                eventPublisher.publishEvent(new StatusUpdateEvent(this,
                        "Removing dataset " + dataSetDescriptor.getDataSetName() + "..."));
                String taskId = createTaskId("remove-dataset");
                BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                        "trips-remove-dataset",
                        () -> {
                            rightPanelCoordinator.removeDataSetFromContext(dataSetDescriptor);
                            return null;
                        },
                        result -> FxThread.runOnFxThread(() -> {
                            // update the query dialog
                            if (queryDialog != null) {
                                queryDialog.removeDataset(dataSetDescriptor);
                            }

                            // redisplay the datasets
                            rightPanelCoordinator.refreshDataSets();
                            eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset: " + dataSetDescriptor.getDataSetName() + " removed"));
                        }),
                        exception -> FxThread.runOnFxThread(() -> {
                            if (isCancellation(exception)) {
                                eventPublisher.publishEvent(new StatusUpdateEvent(this,
                                        "Dataset removal cancelled for " + dataSetDescriptor.getDataSetName()));
                                return;
                            }
                            String message = exception == null ? "Failed to remove dataset." : exception.getMessage();
                            showErrorAlert("Remove Dataset Error", message);
                            eventPublisher.publishEvent(new StatusUpdateEvent(this,
                                    "Failed to remove dataset " + dataSetDescriptor.getDataSetName()));
                        }),
                        () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
                eventPublisher.publishEvent(new BusyStateEvent(this,
                        taskId,
                        true,
                        "Removing dataset " + dataSetDescriptor.getDataSetName() + "...",
                        taskHandle::cancel));
            }
        });
    }

    @EventListener
    public void onSetContextDataSetEvent(SetContextDataSetEvent event) {
        DataSetDescriptor descriptor = event.getDescriptor();
        if (descriptor == null) {
            log.warn("SetContextDataSetEvent received with null descriptor.");
            return;
        }

        FxThread.runOnFxThread(() -> {
            try {
                // clear all the current data
                clearAll();

                // update the trips context and write through to the database
                tripsContext.setDataSetContext(new DataSetContext(descriptor));

                // update the side panel
                if (rightPanelController != null) {
                    rightPanelController.selectDataSet(descriptor);
                }

                if (queryDialog != null) {
                    queryDialog.setDataSetContext(descriptor);
                }

                eventPublisher.publishEvent(new StatusUpdateEvent(this, ("You are looking at the stars in " + descriptor.getDataSetName() + " dataset.  ")));
            } catch (Exception e) {
                log.error("Error handling set context dataset event for {}", descriptor.getDataSetName(), e);
                showErrorAlert("Set Context Error", "Failed to set dataset context: " + e.getMessage());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to set dataset context"));
            }
        });
    }

    /**
     * Clears all data from the display.
     */
    private void clearAll() {
        eventPublisher.publishEvent(new ClearDataEvent(this));
        eventPublisher.publishEvent(new ClearListEvent(this));
        interstellarSpacePane.clearAll();
    }

    private String createTaskId(String base) {
        return base + "-" + System.nanoTime();
    }

    private boolean isCancellation(Throwable exception) {
        return exception instanceof CancellationException;
    }
}
