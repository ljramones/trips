package com.teamgannon.trips.workbench;

import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.workbench.service.WorkbenchCsvService;
import com.teamgannon.trips.workbench.service.WorkbenchEnrichmentService;
import com.teamgannon.trips.workbench.service.WorkbenchTapService;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WorkbenchEnrichmentTabTest {

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit already started by another test.
        }
    }

    @Test
    void bindInitializesTapDefaultsAndHidesProgress() throws Exception {
        runOnFxAndWait(() -> {
            TextField batchField = new TextField();
            TextField backoffField = new TextField();
            ProgressBar progressBar = new ProgressBar();
            progressBar.setVisible(true);

            WorkbenchEnrichmentTab tab = newTab(mock(WorkbenchTapService.class));
            tab.bind(new WorkbenchEnrichmentTab.Bindings(batchField, backoffField, progressBar));

            assertEquals("50", batchField.getText());
            assertEquals("1000", backoffField.getText());
            assertFalse(progressBar.isVisible());
        });
    }

    @Test
    void cancelTapDelegatesToTapService() {
        WorkbenchTapService tapService = mock(WorkbenchTapService.class);
        WorkbenchEnrichmentTab tab = newTab(tapService);

        tab.onCancelTap();

        verify(tapService).cancelCurrentJob(any(Consumer.class), any(Consumer.class));
    }

    private WorkbenchEnrichmentTab newTab(WorkbenchTapService tapService) {
        return new WorkbenchEnrichmentTab(
                mock(DatasetService.class),
                mock(WorkbenchEnrichmentService.class),
                mock(WorkbenchCsvService.class),
                tapService,
                mock(WorkbenchSourceActions.class),
                message -> {
                },
                (title, message) -> {
                },
                () -> null);
    }

    private static void runOnFxAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final RuntimeException[] failure = new RuntimeException[1];
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (RuntimeException e) {
                failure[0] = e;
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for JavaFX action");
        }
        if (failure[0] != null) {
            throw failure[0];
        }
    }
}
