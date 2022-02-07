package com.teamgannon.trips.config.application;

import com.teamgannon.trips.scripting.model.ScriptFile;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScriptContext {

    /**
     * the active list of scripts
     */
    private List<ScriptFile> scriptContextList = new ArrayList<>();

}
