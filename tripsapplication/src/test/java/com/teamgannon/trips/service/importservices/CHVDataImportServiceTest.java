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
 * Integration test for {@link CHVDataImportService}: same Service-lifecycle
 * shape as {@link CSVDataImportServiceTest}, just for the CHView importer.
 * Issue 47 / Phase 7.9 closeout.
 */
class CHVDataImportServiceTest {

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
                CHVDataImportService svc = newService(taskReturning(null));
                assertEquals("CHV importer service", svc.whoAmI());
            });
        }

        @Test
        @DisplayName("getCurrentDataSet starts null and reflects processDataSet input")
        void getCurrentDataSetReflectsInput() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
            runOnFx(() -> {
                CHVDataImportService svc = newService(taskReturning(null));
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
                CHVDataImportService svc = newService(taskReturning(null));
                Label label = new Label();
                ProgressBar bar = new ProgressBar();
                Button cancel = new Button();
                Dataset ds = new Dataset();
                ds.setName("Foo");
                boolean ok = svc.processDataSet(ds, null, label, bar, cancel);
                assertTrue(ok);
                assertTrue(label.textProperty().isBound());
                assertTrue(bar.progressProperty().isBound());
                assertTrue(cancel.disableProperty().isBound());
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

            CSVDataImportServiceTest.RecordingEventPublisher events = new CSVDataImportServiceTest.RecordingEventPublisher();
            CSVDataImportServiceTest.RecordingComplete onComplete = new CSVDataImportServiceTest.RecordingComplete();
            CountDownLatch done = new CountDownLatch(1);

            runOnFx(() -> {
                CHVDataImportService svc = new CHVDataImportService(events, mock(BulkLoadService.class)) {
                    @Override
                    protected Task<FileProcessResult> createTask() {
                        return taskReturning(result);
                    }
                };
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                CSVDataImportServiceTest.awaitTerminalState(svc, done);
                svc.start();
            });

            assertTrue(done.await(30, TimeUnit.SECONDS));
            fxFence();

            assertEquals(3, events.events.size(),
                    "expected StatusUpdate + AddDataSet + SetContextDataSet, got: " + events.events);
            assertTrue(events.events.get(0) instanceof StatusUpdateEvent);
            assertTrue(events.events.get(1) instanceof AddDataSetEvent);
            assertTrue(events.events.get(2) instanceof SetContextDataSetEvent);
            assertTrue(onComplete.invoked.get());
            assertTrue(onComplete.lastStatus.get());
            assertSame(result, onComplete.lastResult.get());
        }

        @Test
        @DisplayName("succeeded with null FileProcessResult invokes onComplete(false)")
        void succeededWithNullResultFailsCleanly() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            CSVDataImportServiceTest.RecordingEventPublisher events = new CSVDataImportServiceTest.RecordingEventPublisher();
            CSVDataImportServiceTest.RecordingComplete onComplete = new CSVDataImportServiceTest.RecordingComplete();
            CountDownLatch done = new CountDownLatch(1);

            runOnFx(() -> {
                CHVDataImportService svc = new CHVDataImportService(events, mock(BulkLoadService.class)) {
                    @Override
                    protected Task<FileProcessResult> createTask() {
                        return taskReturning(null);
                    }
                };
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                CSVDataImportServiceTest.awaitTerminalState(svc, done);
                svc.start();
            });

            assertTrue(done.await(30, TimeUnit.SECONDS));
            fxFence();

            assertEquals(1, events.events.size(),
                    "expected only StatusUpdate, got: " + events.events);
            assertTrue(events.events.get(0) instanceof StatusUpdateEvent);
            assertTrue(onComplete.invoked.get());
            assertFalse(onComplete.lastStatus.get());
        }

        @Test
        @DisplayName("failed: fires error StatusUpdate, invokes onComplete(false)")
        void failedFiresStatusUpdate() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            CSVDataImportServiceTest.RecordingEventPublisher events = new CSVDataImportServiceTest.RecordingEventPublisher();
            CSVDataImportServiceTest.RecordingComplete onComplete = new CSVDataImportServiceTest.RecordingComplete();
            CountDownLatch done = new CountDownLatch(1);

            runOnFx(() -> {
                CHVDataImportService svc = new CHVDataImportService(events, mock(BulkLoadService.class)) {
                    @Override
                    protected Task<FileProcessResult> createTask() {
                        return taskThrowing(new RuntimeException("kaboom"));
                    }
                };
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                CSVDataImportServiceTest.awaitTerminalState(svc, done);
                svc.start();
            });

            assertTrue(done.await(30, TimeUnit.SECONDS),
                    "CHV import service did not reach FAILED state within the timeout");
            fxFence();

            assertEquals(1, events.events.size(),
                    "expected only failure StatusUpdate, got: " + events.events);
            StatusUpdateEvent statusEvent = (StatusUpdateEvent) events.events.get(0);
            assertTrue(statusEvent.getStatus().contains("dataset load failed"),
                    "status should mention dataset failure, got: " + statusEvent.getStatus());
            assertTrue(statusEvent.getStatus().contains("kaboom"),
                    "status should include the task failure message, got: " + statusEvent.getStatus());
            assertTrue(onComplete.invoked.get());
            assertFalse(onComplete.lastStatus.get());
        }

        @Test
        @DisplayName("cancelled: fires StatusUpdate + onComplete(false)")
        void cancelledFiresStatusUpdate() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            CSVDataImportServiceTest.RecordingEventPublisher events = new CSVDataImportServiceTest.RecordingEventPublisher();
            CSVDataImportServiceTest.RecordingComplete onComplete = new CSVDataImportServiceTest.RecordingComplete();
            CountDownLatch cancelDone = new CountDownLatch(1);
            CountDownLatch runningLatch = new CountDownLatch(1);

            runOnFx(() -> {
                CHVDataImportService svc = new CHVDataImportService(events, mock(BulkLoadService.class)) {
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
                Dataset ds = new Dataset();
                ds.setName("Foo");
                svc.processDataSet(ds, onComplete, new Label(), new ProgressBar(), new Button());
                CSVDataImportServiceTest.awaitTerminalState(svc, cancelDone);
                svc.start();

                new Thread(() -> {
                    try {
                        runningLatch.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(svc::cancel);
                }).start();
            });

            assertTrue(cancelDone.await(30, TimeUnit.SECONDS));
            fxFence();

            assertFalse(events.events.isEmpty());
            assertTrue(events.events.get(0) instanceof StatusUpdateEvent);
            String msg = ((StatusUpdateEvent) events.events.get(0)).getStatus();
            assertTrue(msg.contains("cancelled"));
            assertTrue(onComplete.invoked.get());
            assertFalse(onComplete.lastStatus.get());
        }
    }

    // ==================== helpers ====================

    private static CHVDataImportService newService(Task<FileProcessResult> task) {
        return new CHVDataImportService(event -> { }, mock(BulkLoadService.class)) {
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

    private static void runOnFx(Runnable action) throws Exception {
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

    private static void fxFence() throws Exception {
        CountDownLatch fence = new CountDownLatch(1);
        Platform.runLater(fence::countDown);
        assertTrue(fence.await(5, TimeUnit.SECONDS));
    }
}
