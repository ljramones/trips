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
}
