package com.teamgannon.trips.javafxspringbootsupport;


import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.elasticsearch.repository.AstrographicObjectRepository;
import com.teamgannon.trips.controllers.MainPane;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.excel.ExcelReader;
import com.teamgannon.trips.graphics.AstrographicPlotter;
import com.teamgannon.trips.model.StarBase;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Felix Roske
 */
@Slf4j
public abstract class AbstractJavaFxApplicationSupport extends Application {

    protected static String[] savedArgs;

    protected static Class<? extends AbstractFxmlView> savedInitialView;

    protected ConfigurableApplicationContext applicationContext;

    protected Stage primaryStage;
    protected Scene scene;

    @Override
    public void init() throws Exception {
        applicationContext = SpringApplication.run(getClass(), savedArgs);
        log.debug("initializing");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showView(savedInitialView);
    }

    public void showView(Class<? extends AbstractFxmlView> newView) {

        AbstractFxmlView view = applicationContext.getBean(newView);

        DatabaseManagementService databaseManagementService = applicationContext.getBean(DatabaseManagementService.class);
        ChviewReader chviewReader = applicationContext.getBean(ChviewReader.class);
        ExcelReader excelReader = applicationContext.getBean(ExcelReader.class);
        AstrographicPlotter astrographicPlotter = applicationContext.getBean(AstrographicPlotter.class);
        StarBase starBase = applicationContext.getBean(StarBase.class);
        TripsContext tripsContext = applicationContext.getBean(TripsContext.class);
        AstrographicObjectRepository astrographicObjectRepository = applicationContext.getBean(AstrographicObjectRepository.class);

        primaryStage.titleProperty().bind(view.titleProperty());

        int width = 1100;
        int height = 700;
        int depth = 700;
        int spacing = 20;

//        MainPane pane = new MainPane(
//                width, height, depth, spacing,
//                databaseManagementService,
//                applicationContext,
//                chviewReader,
//                excelReader,
//                astrographicPlotter,
//                starBase,
//                tripsContext,
//                astrographicObjectRepository
//        );
//        pane.initialize();


        MainPane pane = new MainPane(
                width, height, depth, spacing,
                databaseManagementService,
                applicationContext,
                chviewReader,
                excelReader,
                astrographicPlotter,
                starBase,
                tripsContext,
                astrographicObjectRepository
        );

        scene = new Scene(pane, width, height);
        log.info("Size of Scene is x({}), y({})", scene.getWidth(), scene.getHeight());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {

        super.stop();
        applicationContext.close();
    }

    protected static void launchApp(Class<? extends AbstractJavaFxApplicationSupport> appClass,
                                    Class<? extends AbstractFxmlView> view, String[] args) {
        savedInitialView = view;
        savedArgs = args;
        Application.launch(appClass, args);
    }

}
