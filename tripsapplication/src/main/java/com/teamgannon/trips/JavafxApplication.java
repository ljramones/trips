package com.teamgannon.trips;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.event.StageReadyEvent;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.excel.ExcelReader;
import com.teamgannon.trips.graphics.AstrographicPlotter;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.starmodel.StarBase;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class JavafxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        ApplicationContextInitializer<GenericApplicationContext> initializer = genericApplicationContext -> {
            genericApplicationContext.registerBean(Application.class, () -> JavafxApplication.this);
            genericApplicationContext.registerBean(Parameters.class, this::getParameters);
            genericApplicationContext.registerBean(HostServices.class, this::getHostServices);

            genericApplicationContext.registerBean(DatabaseManagementService.class);
            genericApplicationContext.registerBean(ChviewReader.class);
            genericApplicationContext.registerBean(ExcelReader.class);
            genericApplicationContext.registerBean(AstrographicPlotter.class);
            genericApplicationContext.registerBean(StarBase.class);
            genericApplicationContext.registerBean(TripsContext.class);
//            genericApplicationContext.registerBean(AstrographicObjectRepository.class);
        };

        this.context = new SpringApplicationBuilder()
                .sources(TerranRepublicInterstellarPlotterApplication.class)
                .initializers(initializer)
                .build().run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.context.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() throws Exception {
        this.context.close();
        Platform.exit();
    }

}
