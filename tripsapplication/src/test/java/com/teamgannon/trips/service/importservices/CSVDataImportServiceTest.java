package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.model.ImportTaskComplete;
import com.teamgannon.trips.events.AddDataSetEvent;
import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.BulkLoadService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Integration test for {@link CSVDataImportService}: drives the Service
 * through real {@link Task} lifecycle (succeed / fail / cancel) and pins
 * the event fan-out + {@link ImportTaskComplete} callback + property-
 * binding contracts. Issue 47 / Phase 7.9 closeout.
 *
 * <h2>What this does not cover</h2>
 * The actual CSV → DB pipeline lives in {@code BulkLoadService.loadCsvDataset}
 * and is pinned by {@code FlywayBaselineSmokeTest} + {@code BulkLoadServiceTest}.
 * This test focuses on the Service-layer behaviour the import dialog
 * depends on.
 */
class CSVDataImportServiceTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { });
            javaFxInitialized = true;
        } catch (IllegalStateException already) {
            javaFxInitialized = true;
        } catch (Exception e) {
            javaFxInitialized = false;
        }
    }

    @Nested
    @DisplayName("identity accessors")
    class IdentityAccessors {

        @Test
        @DisplayName("whoAmI returns the human-friendly importer name")
        void whoAmIIsStable() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
            runOnFx(() -> {
                CSVDataImportService svc = newService(taskReturning(null));
                assertEquals("CSV importer", svc.whoAmI());
            });
        }

        @Test
        @DisplayName("getCurrentDataSet starts null and reflects processDataSet input")
        void getCurrentDataSetReflectsInput() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
            runOnFx(() -> {
                CSVDataImportService svc = newService(taskReturning(null));
                assertNull(svc.getCurrentDataSet());
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, null, new Label(), new ProgressBar(), new Button());
                assertSame(ds, svc.getCurrentDataSet());
            });
        }
    }

    @Nested
    @DisplayName("processDataSet binding")
    class ProcessDataSetBinding {

        @Test
        @DisplayName("binds progress text / progress bar / cancel-disable to the Service properties")
        void bindsAllControls() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
            runOnFx(() -> {
                CSVDataImportService svc = newService(taskReturning(null));
                Label label = new Label();
                ProgressBar bar = new ProgressBar();
                Button cancel = new Button();
                Dataset ds = new Dataset();
                ds.setName("Foo");
                boolean ok = svc.processDataSet(ds, null, label, bar, cancel);
                assertTrue(ok, "processDataSet should return true on success");
                assertTrue(label.textProperty().isBound(), "label text bound to service.messageProperty");
                assertTrue(bar.progressProperty().isBound(), "bar progress bound to service.progressProperty");
                assertTrue(cancel.disableProperty().isBound(), "cancel disable bound to service.stateProperty");
            });
        }
    }

    @Nested
    @DisplayName("Service lifecycle event fan-out")
    class LifecycleEvents {

        @Test
        @DisplayName("succeeded: fires StatusUpdate + AddDataSet + SetContextDataSet, invokes onComplete(true)")
        void succeedFiresFullFanOut() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            FileProcessResult result = new FileProcessResult();
            result.setSuccess(true);
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("Loaded");
            result.setDataSetDescriptor(descriptor);

            RecordingEventPublisher events = new RecordingEventPublisher();
            RecordingComplete onComplete = new RecordingComplete();
            AtomicReference<CSVDataImportService> svcRef = new AtomicReference<>();

            runOnFx(() -> {
                CSVDataImportService svc = new CSVDataImportService(mock(BulkLoadService.class), events) {
                    @Override
                    protected Task<FileProcessResult> createTask() {
                        return taskReturning(result);
                    }
                };
                svcRef.set(svc);
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                svc.start();
            });

            awaitTerminalState(svcRef.get(), 30_000);

            assertEquals(3, events.events.size(),
                    "expected StatusUpdate + AddDataSet + SetContextDataSet, got: " + events.events);
            assertTrue(events.events.get(0) instanceof StatusUpdateEvent);
            assertTrue(events.events.get(1) instanceof AddDataSetEvent);
            assertTrue(events.events.get(2) instanceof SetContextDataSetEvent);
            assertTrue(onComplete.invoked.get(), "ImportTaskComplete should fire");
            assertTrue(onComplete.lastStatus.get(), "ImportTaskComplete should fire with status=true");
            assertSame(result, onComplete.lastResult.get(), "ImportTaskComplete should carry the FileProcessResult");
        }

        @Test
        @DisplayName("succeeded with null FileProcessResult invokes onComplete(false) and skips the dataset events")
        void succeededWithNullResultFailsCleanly() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            RecordingEventPublisher events = new RecordingEventPublisher();
            RecordingComplete onComplete = new RecordingComplete();
            AtomicReference<CSVDataImportService> svcRef = new AtomicReference<>();

            runOnFx(() -> {
                CSVDataImportService svc = new CSVDataImportService(mock(BulkLoadService.class), events) {
                    @Override
                    protected Task<FileProcessResult> createTask() {
                        return taskReturning(null);
                    }
                };
                svcRef.set(svc);
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                svc.start();
            });

            awaitTerminalState(svcRef.get(), 30_000);

            assertEquals(1, events.events.size(),
                    "expected only StatusUpdate, got: " + events.events);
            assertTrue(events.events.get(0) instanceof StatusUpdateEvent);
            assertTrue(onComplete.invoked.get(), "onComplete should still fire");
            assertFalse(onComplete.lastStatus.get(), "onComplete should fire with status=false");
        }

        @Test
        @DisplayName("failed: fires error StatusUpdate, invokes onComplete(false)")
        void failedFiresStatusUpdate() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            RecordingEventPublisher events = new RecordingEventPublisher();
            RecordingComplete onComplete = new RecordingComplete();
            AtomicReference<CSVDataImportService> svcRef = new AtomicReference<>();

            runOnFx(() -> {
                CSVDataImportService svc = new CSVDataImportService(mock(BulkLoadService.class), events) {
                    @Override
                    protected Task<FileProcessResult> createTask() {
                        return taskThrowing(new RuntimeException("boom"));
                    }
                };
                svcRef.set(svc);
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                svc.start();
            });

            awaitTerminalState(svcRef.get(), 30_000);

            assertEquals(1, events.events.size());
            StatusUpdateEvent statusEvent = (StatusUpdateEvent) events.events.get(0);
            assertTrue(statusEvent.getStatus().contains("dataset load failed"),
                    "status event should mention failure, got: " + statusEvent.getStatus());
            assertTrue(statusEvent.getStatus().contains("boom"));
            assertTrue(onComplete.invoked.get());
            assertFalse(onComplete.lastStatus.get());
        }

        @Test
        @DisplayName("cancelled: fires StatusUpdate + onComplete(false)")
        void cancelledFiresStatusUpdate() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            RecordingEventPublisher events = new RecordingEventPublisher();
            RecordingComplete onComplete = new RecordingComplete();
            CountDownLatch runningLatch = new CountDownLatch(1);
            AtomicReference<CSVDataImportService> svcRef = new AtomicReference<>();

            runOnFx(() -> {
                CSVDataImportService svc = new CSVDataImportService(mock(BulkLoadService.class), events) {
                    @Override
                    protected Task<FileProcessResult> createTask() {
                        return new Task<>() {
                            @Override
                            protected FileProcessResult call() throws InterruptedException {
                                runningLatch.countDown();
                                Thread.sleep(10_000);
                                return null;
                            }
                        };
                    }
                };
                svcRef.set(svc);
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                svc.start();

                // Cancel as soon as the task signals it's running, on the FX thread.
                new Thread(() -> {
                    try {
                        runningLatch.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(svc::cancel);
                }).start();
            });

            awaitTerminalState(svcRef.get(), 30_000);

            assertFalse(events.events.isEmpty(), "expected at least one event");
            assertTrue(events.events.get(0) instanceof StatusUpdateEvent);
            String msg = ((StatusUpdateEvent) events.events.get(0)).getStatus();
            assertTrue(msg.contains("cancelled"), "status event should mention cancellation, got: " + msg);
            assertTrue(onComplete.invoked.get());
            assertFalse(onComplete.lastStatus.get());
        }
    }

    // ==================== helpers ====================

    private static CSVDataImportService newService(Task<FileProcessResult> task) {
        return new CSVDataImportService(mock(BulkLoadService.class), event -> { }) {
            @Override
            protected Task<FileProcessResult> createTask() {
                return task;
            }
        };
    }

    private static Task<FileProcessResult> taskReturning(FileProcessResult value) {
        return new Task<>() {
            @Override
            protected FileProcessResult call() {
                return value;
            }
        };
    }

    private static Task<FileProcessResult> taskThrowing(Throwable t) {
        return new Task<>() {
            @Override
            protected FileProcessResult call() throws Exception {
                if (t instanceof Exception e) throw e;
                throw new RuntimeException(t);
            }
        };
    }

    static void runOnFx(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                err.set(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX action timed out");
        if (err.get() != null) throw err.get();
    }

    /**
     * Register a state-property listener that counts the latch down as soon
     * as the service reaches any terminal state. Kept for callers who still
     * use the latch idiom, but prefer {@link #awaitTerminalState(javafx.concurrent.Service, long)}
     * for new tests — polling is immune to listener-attach races.
     */
    static void awaitTerminalState(javafx.concurrent.Service<?> svc, CountDownLatch latch) {
        svc.stateProperty().addListener((obs, oldState, newState) -> {
            if (isTerminal(newState)) {
                latch.countDown();
            }
        });
        if (isTerminal(svc.getState())) {
            latch.countDown();
        }
    }

    /**
     * Poll {@code svc.getState()} (on the FX thread; JavaFX rejects access from
     * any other thread) until terminal (or timeout) and then drain the FX
     * thread once more so the Service's protected {@code succeeded()/failed()
     * /cancelled()} callbacks have completed. Strictly more reliable than the
     * listener-based latch pattern — each FX-thread fence reads the current
     * state and can't miss a transition that happened before a listener was
     * attached.
     * <p>
     * Used by the lifecycle-event tests below to avoid a race that surfaces
     * under heavy CI load.
     */
    static void awaitTerminalState(javafx.concurrent.Service<?> svc, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            AtomicReference<Worker.State> stateRef = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                stateRef.set(svc.getState());
                latch.countDown();
            });
            if (!latch.await(2, TimeUnit.SECONDS)) {
                continue; // FX queue is backed up — try again
            }
            if (isTerminal(stateRef.get())) {
                fxFence(); // make sure succeeded()/failed()/cancelled() handlers drained
                return;
            }
            Thread.sleep(25);
        }
        fail("service did not reach terminal state within " + timeoutMs + "ms");
    }

    private static boolean isTerminal(Worker.State state) {
        return state == Worker.State.SUCCEEDED
                || state == Worker.State.FAILED
                || state == Worker.State.CANCELLED;
    }

    /** Run a no-op on the FX thread and wait for it — drains queued runLater callbacks ahead of it. */
    static void fxFence() throws Exception {
        CountDownLatch fence = new CountDownLatch(1);
        Platform.runLater(fence::countDown);
        assertTrue(fence.await(5, TimeUnit.SECONDS), "FX fence timed out");
    }

    /** Recording publisher for ApplicationEvents — simple in-memory list. */
    static final class RecordingEventPublisher implements ApplicationEventPublisher {
        final List<Object> events = new ArrayList<>();

        @Override
        public synchronized void publishEvent(Object event) {
            events.add(event);
        }
    }

    static final class RecordingComplete implements ImportTaskComplete {
        final AtomicBoolean invoked = new AtomicBoolean(false);
        final AtomicBoolean lastStatus = new AtomicBoolean(false);
        final AtomicReference<FileProcessResult> lastResult = new AtomicReference<>();

        @Override
        public void complete(boolean status, Dataset dataset, FileProcessResult fileProcessResult, String errorMessage) {
            invoked.set(true);
            lastStatus.set(status);
            lastResult.set(fileProcessResult);
        }
    }
}
