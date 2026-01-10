package com.teamgannon.trips.javafxsupport;

import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BackgroundTaskRunner {

    private BackgroundTaskRunner() {
    }

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
            if (onFailure != null) {
                onFailure.accept(task.getException());
            }
            if (onFinally != null) {
                onFinally.run();
            }
        });

        task.setOnCancelled(event -> {
            if (onFinally != null) {
                onFinally.run();
            }
        });

        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
        return new TaskHandle(task, thread);
    }

    public static final class TaskHandle {
        private final Task<?> task;
        private final Thread thread;

        private TaskHandle(Task<?> task, Thread thread) {
            this.task = task;
            this.thread = thread;
        }

        public void cancel() {
            task.cancel();
            thread.interrupt();
        }
    }
}
