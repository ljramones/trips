package com.teamgannon.trips.javafxsupport;

import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;


public class StageReadyEvent extends ApplicationEvent {

    public final @NotNull Stage stage;

    public StageReadyEvent(@NotNull Stage stage) {
        super(stage);
        this.stage = stage;
    }

    public @NotNull Stage getStage() {
        return stage;
    }
}
