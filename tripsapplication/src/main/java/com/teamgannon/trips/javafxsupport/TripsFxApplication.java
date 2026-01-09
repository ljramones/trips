package com.teamgannon.trips.javafxsupport;

import com.teamgannon.trips.TripsSpringBootApplication;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


@Slf4j
public class TripsFxApplication extends Application {

    private ConfigurableApplicationContext context;

    private void exitApplication(WindowEvent event) {
        event.consume();
        int exitCode = SpringApplication.exit(context);
        Platform.exit();
        System.exit(exitCode);
    }

    @Override
    public void init() throws Exception {
        ApplicationContextInitializer<GenericApplicationContext> initializer = this::initialize;
        this.context = new SpringApplicationBuilder()
                .sources(TripsSpringBootApplication.class)
                .initializers(initializer)
                .headless(false)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception:");
            throwable.printStackTrace();
        });


        context.publishEvent(new StageReadyEvent(primaryStage));
        primaryStage.setOnCloseRequest(this::exitApplication);
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
}
