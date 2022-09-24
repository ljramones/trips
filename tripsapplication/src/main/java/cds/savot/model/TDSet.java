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
 * Set of TD elements
 * </p>
 * 
 * @author Andre Schaaff
 * @see SavotSet
 */
public final class TDSet extends SavotSet<SavotTD> {

    /**
     * Constructor
     */
    public TDSet() {
    }

    /**
     * Get the content at the TDIndex position of the TDSet
     * 
     * @param TDIndex
     * @return String
     */
    public String getContent(final int TDIndex) {
        final SavotTD td = getItemAt(TDIndex);
        if (td == null) {
            return "";
        }
        return td.getContent();
    }

    /**
     * Get the raw content at the TDIndex position of the TDSet (maybe null)
     * 
     * @param TDIndex
     * @return a String or null
     */
    public String getRawContent(final int TDIndex) {
        final SavotTD td = getItemAt(TDIndex);
        if (td == null) {
            return null;
        }
        return td.getRawContent();
    }
}
