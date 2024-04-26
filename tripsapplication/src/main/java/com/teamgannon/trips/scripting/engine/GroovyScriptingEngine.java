package com.teamgannon.trips.scripting.engine;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Slf4j
@Component("groovyScriptEngine")
public class GroovyScriptingEngine {

    private final ApplicationEventPublisher eventPublisher;

    private final GroovyClassLoader loader;
    private final GroovyShell shell;
    private final GroovyScriptEngine engine;
    private final ScriptEngine engineFromFactory;

    public GroovyScriptingEngine(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        loader = new GroovyClassLoader(this.getClass().getClassLoader());
        shell = new GroovyShell(loader, new Binding());

        URL url = null;
        try {
            url = new File("files/scriptfiles/").toURI().toURL();
        } catch (MalformedURLException e) {
            log.error("Exception while creating url", e);
        }
        engine = new GroovyScriptEngine(new URL[]{url}, this.getClass().getClassLoader());
        engineFromFactory = new GroovyScriptEngineFactory().getScriptEngine();

    }


    public String runAScript(String name, String contents, List<String> parameterList) {
        try {
            Script script = shell.parse(new File("/Users/larrymitchell/tripsnew/trips/files/scriptfiles/", "CalcScript.groovy"));
            log.info("Executing {} + {}", 5, 6);
            Integer result = (Integer) script.invokeMethod("calcSum", new Object[]{5, 6});
            log.info("Result of CalcScript.calcSum() method is {}", result);
            return Integer.toString(result);
        } catch (Exception e) {
            log.error("failed due to: " + e.getMessage());
            return "...";
        }
//
//        String result = (String) Eval.me(contents);
//
////        Script script = shell.parse(contents);
////        String result = (String) script.run();
//        log.info("Result of CalcScript.calcSum() method is {}", result);
//        return result;


    }
}
