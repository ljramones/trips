package com.teamgannon.trips.api;

import com.teamgannon.trips.service.DatabaseManagementService;
import org.springframework.context.ApplicationContext;

public class DBApiServer {

    private final ApplicationContext appContext;

    private final DatabaseManagementService dbService;

    public DBApiServer(ApplicationContext appContext) {
        this.appContext = appContext;
        dbService = (DatabaseManagementService) appContext.getBean("dbservice");
    }



}
