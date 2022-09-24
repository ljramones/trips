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
 * Fits element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotFits extends MarkupComment {

    // extnum attribute
    private String extnum = null;
    // STREAM element
    private SavotStream stream = null;

    /**
     * Constructor
     */
    public SavotFits() {
    }

    /**
     * Set the extnum
     * 
     * @param extnum
     *            String
     */
    public void setExtnum(final String extnum) {
        this.extnum = extnum;
    }

    /**
     * Get the extnum
     * 
     * @return a String
     */
    public String getExtnum() {
        return str(extnum);
    }

    /**
     * Set the STREAM element
     * 
     * @param stream
     */
    public void setStream(final SavotStream stream) {
        this.stream = stream;
    }

    /**
     * Get the STREAM element
     * 
     * @return SavotStream
     */
    public SavotStream getStream() {
        return stream;
    }
}
