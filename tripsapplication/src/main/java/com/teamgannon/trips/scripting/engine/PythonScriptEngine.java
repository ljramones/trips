package com.teamgannon.trips.scripting.engine;

import com.teamgannon.trips.events.StatusUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.python.util.PythonInterpreter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Slf4j
@Component
public class PythonScriptEngine {

    private final ApplicationEventPublisher eventPublisher;

    public PythonScriptEngine(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public String runAScript(String scriptName,
                             String theScript) {

        log.info("run this script named {}::{}", scriptName, theScript);
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            StringWriter output = new StringWriter();
            pyInterp.setOut(output);
            if (!theScript.isEmpty()) {
                try {
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Running script: " + scriptName));
                    pyInterp.exec(theScript);
                    return output.toString();
                } catch (Exception e) {
                    log.error("Filed to execute python because:" + e.getMessage());
                    return "Filed to execute python because:" + e.getMessage();
                }
            }
        }
        return "nothing to run";
    }


}
