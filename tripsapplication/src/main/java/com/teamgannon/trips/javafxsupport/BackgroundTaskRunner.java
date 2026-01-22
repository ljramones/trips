package com.teamgannon.trips.javafxsupport;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility for running cancellable background tasks with proper error handling.
 * Tasks run on daemon threads so they won't prevent application shutdown.
 */
@Slf4j
public final class BackgroundTaskRunner {

    private BackgroundTaskRunner() {
    }

    /**
     * Run a cancellable background task.
     *
     * @param threadName the name for the background thread (for debugging)
     * @param work       the work to perform in the background
     * @param onSuccess  callback when work completes successfully (nullable)
     * @param onFailure  callback when work fails with an exception (nullable - will log if null)
     * @param onFinally  callback that runs regardless of success/failure (nullable)
     * @param <T>        the result type of the work
     * @return a TaskHandle that can be used to cancel the task
     */
    public static <T> TaskHandle runCancelable(String threadName,
                                               Supplier<T> work,
                                               Consumer<T> onSuccess,
                                               Consumer<Throwable> onFailure,
                                               Runnable onFinally) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };

        task.setOnSucceeded(event -> {
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
            if (onFinally != null) {
                onFinally.run();
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            if (onFailure != null) {
                onFailure.accept(exception);
            } else {
                // Default: log the error if no handler provided
                log.error("Background task '{}' failed with unhandled exception", threadName, exception);
            }
            if (onFinally != null) {
                onFinally.run();
            }
        });

        task.setOnCancelled(event -> {
            log.debug("Background task '{}' was cancelled", threadName);
            if (onFinally != null) {
                onFinally.run();
            }
        });

        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
        log.debug("Started background task '{}' on daemon thread", threadName);
        return new TaskHandle(task, thread);
    }

    /**
     * Handle for controlling a background task.
     */
    public static final class TaskHandle {
        private final Task<?> task;
        private final Thread thread;

        private TaskHandle(Task<?> task, Thread thread) {
            this.task = task;
            this.thread = thread;
        }

        /**
         * Cancel the task and interrupt the thread.
         */
        public void cancel() {
            task.cancel();
            thread.interrupt();
            log.debug("Cancelled background task on thread '{}'", thread.getName());
        }

        /**
         * Check if the task is still running.
         */
        public boolean isRunning() {
            return task.isRunning();
        }

        /**
         * Check if the task was cancelled.
         */
        public boolean isCancelled() {
            return task.isCancelled();
        }

        /**
         * Check if the task completed (successfully or with failure).
         */
        public boolean isDone() {
            return task.isDone();
        }
    }
}
