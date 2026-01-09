package com.teamgannon.trips;

import com.teamgannon.trips.javafxsupport.TripsFxApplication;
import com.teamgannon.trips.javafxsupport.TripsPreloader;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.search.AstroSearchQuery;
import javafx.application.Application;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.InjectionPointLazyFxControllerAndViewResolver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
public class TripsSpringBootApplication implements CommandLineRunner {

    // Manual log field (Lombok @Slf4j should generate this but adding explicitly)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TripsSpringBootApplication.class);

    /**
     * storage of astrographic objects in DB
     */
    @Autowired
    private StarObjectRepository starObjectRepository;

    public static void main(String[] args) {

        System.setProperty("javafx.preloader", TripsPreloader.class.getName());
        Application.launch(TripsFxApplication.class, args);
    }

    @Bean
    public @NotNull FxWeaver fxWeaver(@NotNull ConfigurableApplicationContext applicationContext) {
        // Would also work with javafx-weaver-core only:
        // return new FxWeaver(applicationContext::getBean, applicationContext::close);
        return new SpringFxWeaver(applicationContext);
    }

    /**
     * for example, usage.
     * <p/>
     * <strong>MUST be in scope prototype!</strong>
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <C, V extends Node> FxControllerAndView<C, V> controllerAndView(FxWeaver fxWeaver,
                                                                           InjectionPoint injectionPoint) {
        return new InjectionPointLazyFxControllerAndViewResolver(fxWeaver)
                .resolve(injectionPoint);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("TRIPS UP and Running!!");
    }

}
