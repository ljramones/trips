package com.teamgannon.trips.scripting;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.scripting.engine.GroovyScriptingEngine;
import com.teamgannon.trips.scripting.engine.PythonScriptEngine;
import com.teamgannon.trips.service.DatabaseManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScriptManager {

    private final MainPane mainPane;
    private final DatabaseManagementService databaseManagementService;
    private final GroovyScriptingEngine groovyScriptEngine;
    private final PythonScriptEngine pythonScriptEngine;

    public ScriptManager(MainPane mainPane,
                         DatabaseManagementService databaseManagementService,
                         GroovyScriptingEngine groovyScriptEngine,
                         PythonScriptEngine pythonScriptEngine) {

        this.mainPane = mainPane;
        this.databaseManagementService = databaseManagementService;

        this.groovyScriptEngine = groovyScriptEngine;
        this.pythonScriptEngine = pythonScriptEngine;

        groovyScriptEngine.setUpdateListener(mainPane);
        pythonScriptEngine.setUpdateListener(mainPane);
    }

    public void loadScriptsFromDB() {

    }


}
