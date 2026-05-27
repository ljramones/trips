package com.teamgannon.trips.javafxsupport;

import javafx.application.Platform;

public final class FxThread {

    private FxThread() {
    }

    public static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Throws {@link IllegalStateException} if called off the JavaFX Application
     * Thread. Use to document and enforce thread invariants on methods that
     * mutate scene-graph or FX-confined state without synchronisation.
     */
    public static void assertFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Expected to be on JavaFX Application Thread, but was on: "
                            + Thread.currentThread().getName());
        }
    }
}
