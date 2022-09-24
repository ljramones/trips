package cds.savot.model;

//Copyright 2002-2014 - UDS/CNRS
//The SAVOT library is distributed under the terms
//of the GNU General Public License version 3.
//
//This file is part of SAVOT.
//
//SAVOT is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, version 3 of the License.
//
//SAVOT is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//The GNU General Public License is available in COPYING file
//along with SAVOT.
//
//SAVOT - Simple Access to VOTable - Parser
//
//Author, Co-Author:  Andre Schaaff (CDS), Laurent Bourges (JMMC)
/**
 * <p>
 * Option element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotOption extends MarkupComment implements NameSupport {

    // name attribute
    private String name = null;
    // value attribute
    private String value = null;
    // OPTION elements
    private OptionSet options = null;

    /**
     * Constructor
     */
    public SavotOption() {
    }

    /**
     * Set name attribute
     * 
     * @param name
     */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get name attribute
     * 
     * @return String
     */
    @Override
    public String getName() {
        return str(name);
    }

    /**
     * Set value attribute
     * 
     * @param value
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Get value attribute
     * 
     * @return String
     */
    public String getValue() {
        return str(value);
    }

    /**
     * Get OPTION elements reference
     * 
     * @return OptionSet
     */
    public OptionSet getOptions() {
        if (options == null) {
            options = new OptionSet();
        }
        return options;
    }

    /**
     * Set OPTION elements reference
     * 
     * @param options
     */
    public void setOptions(final OptionSet options) {
        this.options = options;
    }
}
