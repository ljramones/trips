package com.teamgannon.trips.javafxsupport;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundTaskRunnerTest {

    @BeforeAll
    static void initToolkit() {
        // Ensure JavaFX toolkit is initialized for Task callbacks
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    @Timeout(5)
    void testSuccessfulTask() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicBoolean finallyCalled = new AtomicBoolean(false);

        BackgroundTaskRunner.runCancelable(
                "test-success",
                () -> "hello",
                value -> {
                    result.set(value);
                    latch.countDown();
                },
                error -> fail("Should not fail"),
                () -> finallyCalled.set(true)
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Task should complete");
        assertEquals("hello", result.get());
        // Give a moment for finally to run
        Thread.sleep(100);
        assertTrue(finallyCalled.get(), "Finally should be called");
    }

    @Test
    @Timeout(5)
    void testFailedTask() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> caughtError = new AtomicReference<>();
        AtomicBoolean finallyCalled = new AtomicBoolean(false);

        BackgroundTaskRunner.runCancelable(
                "test-failure",
                () -> {
                    throw new RuntimeException("Test error");
                },
                value -> fail("Should not succeed"),
                error -> {
                    caughtError.set(error);
                    latch.countDown();
                },
                () -> finallyCalled.set(true)
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Task should complete");
        assertNotNull(caughtError.get());
        assertEquals("Test error", caughtError.get().getMessage());
        // Give a moment for finally to run
        Thread.sleep(100);
        assertTrue(finallyCalled.get(), "Finally should be called on failure");
    }

    @Test
    @Timeout(5)
    void testFailedTaskWithNullHandler() throws Exception {
        // When onFailure is null, the error should be logged but not thrown
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean finallyCalled = new AtomicBoolean(false);

        BackgroundTaskRunner.runCancelable(
                "test-failure-null-handler",
                () -> {
                    throw new RuntimeException("Test error - should be logged");
                },
                value -> fail("Should not succeed"),
                null, // No failure handler - should log instead
                () -> {
                    finallyCalled.set(true);
                    latch.countDown();
                }
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Task should complete");
        assertTrue(finallyCalled.get(), "Finally should be called even with null error handler");
    }

    @Test
    @Timeout(5)
    void testCancelledTask() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicBoolean finallyCalled = new AtomicBoolean(false);
        AtomicBoolean successCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);

        BackgroundTaskRunner.TaskHandle handle = BackgroundTaskRunner.runCancelable(
                "test-cancel",
                () -> {
                    startLatch.countDown();
                    try {
                        Thread.sleep(10000); // Long sleep
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "should not return";
                },
                value -> successCalled.set(true),
                error -> failureCalled.set(true),
                () -> {
                    finallyCalled.set(true);
                    finishLatch.countDown();
                }
        );

        // Wait for task to start
        assertTrue(startLatch.await(2, TimeUnit.SECONDS), "Task should start");

        // Cancel the task
        handle.cancel();

        // Wait for finally to be called
        assertTrue(finishLatch.await(2, TimeUnit.SECONDS), "Finally should be called after cancel");
        assertTrue(finallyCalled.get(), "Finally should be called on cancel");
        assertTrue(handle.isCancelled(), "Task should be cancelled");
    }

    @Test
    @Timeout(5)
    void testTaskHandleStatusAfterCompletion() throws Exception {
        CountDownLatch completeLatch = new CountDownLatch(1);
        AtomicBoolean taskCompleted = new AtomicBoolean(false);

        BackgroundTaskRunner.TaskHandle handle = BackgroundTaskRunner.runCancelable(
                "test-status",
                () -> "done",
                value -> {
                    taskCompleted.set(true);
                    completeLatch.countDown();
                },
                null,
                null
        );

        // Wait for task to complete
        assertTrue(completeLatch.await(3, TimeUnit.SECONDS), "Task should complete");
        assertTrue(taskCompleted.get(), "Success callback should have been called");

        // TaskHandle exists and cancel doesn't throw
        assertNotNull(handle, "Handle should not be null");
        // Calling cancel on completed task should be safe (no-op)
        handle.cancel();
    }

    @Test
    void testNullCallbacks() throws Exception {
        // All callbacks can be null - task should still complete
        CountDownLatch latch = new CountDownLatch(1);

        BackgroundTaskRunner.runCancelable(
                "test-null-callbacks",
                () -> {
                    latch.countDown();
                    return "done";
                },
                null,
                null,
                null
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Task should complete even with null callbacks");
    }
}
