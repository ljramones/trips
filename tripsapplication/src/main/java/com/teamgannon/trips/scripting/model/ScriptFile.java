package com.teamgannon.trips.scripting.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScriptFile {

    /**
     * true means we selected it
     */
    private boolean selected;

    /**
     * the type of scripting engine that this program works in
     */
    private ScriptEngineEnum engineType;

    /**
     * the menuing root for this script to add
     */
    private String menuRoot;

    /**
     * the name of the script file
     */
    private String name;

    /**
     * the absolute path to the file
     */
    private String path;

    /**
     * the contents of the file
     */
    private String contents;

}
