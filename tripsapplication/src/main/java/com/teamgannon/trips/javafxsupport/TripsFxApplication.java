package com.teamgannon.trips.javafxsupport;

import com.teamgannon.trips.TripsSpringBootApplication;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


public class TripsFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        ApplicationContextInitializer<GenericApplicationContext> initializer = this::initialize;
        this.context = new SpringApplicationBuilder()
                .sources(TripsSpringBootApplication.class)
                .initializers(initializer)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        context.publishEvent(new StageReadyEvent(primaryStage));
        primaryStage.setOnCloseRequest(TripsFxApplication::exitApplication);
    }

    @Override
    public void stop() throws Exception {
        context.close();
        Platform.exit();
    }

    private void initialize(@NotNull GenericApplicationContext context) {
        context.registerBean(Application.class, () -> TripsFxApplication.this);
        context.registerBean(Parameters.class, this::getParameters);
        context.registerBean(HostServices.class, this::getHostServices);
    }

    private static void exitApplication(WindowEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
