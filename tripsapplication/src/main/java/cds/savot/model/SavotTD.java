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
 * A data (of a row)
 * </p>
 * 
 * @author Andre Schaaff
 * @see SavotSet
 */
public final class SavotTD extends SavotBase {
    // TD content

    private String content = null;

    /**
     * Constructor
     */
    public SavotTD() {
    }

    /**
     * Constructor with content
     * @param content
     */
    public SavotTD(final String content) {
        this.content = content;
    }

    /**
     * Set element content
     * 
     * @param content
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Get element content
     * 
     * @return a String
     */
    public String getContent() {
        return str(content);
    }

    /**
     * Get the raw content value (maybe null)
     *
     * @return a String or null
     */
    public String getRawContent() {
        return content;
    }
}
