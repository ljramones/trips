package com.teamgannon.trips.scripting.engine;

import com.teamgannon.trips.listener.StatusUpdaterListener;
import lombok.extern.slf4j.Slf4j;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Slf4j
@Component
public class PythonScriptEngine {

    private StatusUpdaterListener statusUpdaterListener;

    public String runAScript(String scriptName,
                              String theScript) {

        log.info("run this script named {}::{}", scriptName, theScript);
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            StringWriter output = new StringWriter();
            pyInterp.setOut(output);
            if (!theScript.isEmpty()) {
                try {
                    statusUpdaterListener.updateStatus("Running script: " + scriptName);
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

    public void setUpdateListener(StatusUpdaterListener listener) {
        this.statusUpdaterListener = listener;
    }

}
