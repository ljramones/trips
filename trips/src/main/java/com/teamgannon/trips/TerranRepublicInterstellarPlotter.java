package com.teamgannon.trips;

import com.teamgannon.trips.controllers.MainViewer;
import com.teamgannon.trips.javafxspringbootsupport.AbstractJavaFxApplicationSupport;
import com.teamgannon.trips.service.LifecycleService;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * the main entry pont for this spring boot application
 */
@Slf4j
@SpringBootApplication
public class TerranRepublicInterstellarPlotter extends AbstractJavaFxApplicationSupport implements CommandLineRunner {

    @Autowired
    private LifecycleService startupService;

    /**
     * main start for application
     *
     * @param args the incoming args
     */
    public static void main(String[] args) {
        launchApp(TerranRepublicInterstellarPlotter.class, MainViewer.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showView(savedInitialView);
    }


    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        startupService.initialize();
    }

    public Stage getStage() {
        return primaryStage;
    }

}
