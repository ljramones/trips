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
 * For min and max
 * </p>
 * 
 * @author Andre Schaaff
 */
public class Boundary extends MarkupComment {

    // value attribute
    private String value = null;
    // inclusive attribute
    private String inclusive = "yes";
    // MIN, MAX, ... element content
    private String content = null;

    /**
     * Constructor
     */
    public Boundary() {
    }

    /**
     * Set value attribute
     * 
     * @param value
     */
    public final void setValue(final String value) {
        this.value = value;
    }

    /**
     * Get value attribute
     * 
     * @return a String
     */
    public final String getValue() {
        return str(value);
    }

    /**
     * Set inclusive attribute
     * 
     * @param inclusive
     *            (yes, no)
     */
    public final void setInclusive(final String inclusive) {
        this.inclusive = inclusive;
    }

    /**
     * Get inclusive attribute
     * 
     * @return a String
     */
    public final String getInclusive() {
        return str(inclusive);
    }

    /**
     * Set element content
     * 
     * @param content
     */
    public final void setContent(final String content) {
        this.content = content;
    }

    /**
     * Get element content
     * 
     * @return a String
     */
    public final String getContent() {
        return str(content);
    }
}
