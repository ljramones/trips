package com.teamgannon.trips.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

/**
 * Used to initialize the application startup
 * There will be a splash screen that displays which this is in progress
 * <p>
 * Created by larrymitchell on 2017-01-20.
 */
@Slf4j
@Service
public class LifecycleService {


    public void initialize() {
        log.info("initializing the Terran Republic Viewer Application");
    }


    /**
     * this ensures that the shutdown will be called prior to exit
     * place any required shut down code here
     */
    @PreDestroy
    public void shutdown() {
        log.info("shutting down the Terran Republic Viewer Application");
    }

}
