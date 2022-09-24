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
 * Row element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotTR extends SavotBase {

    // TR element set
    private TDSet TDs = null;
    private int lineInXMLFile = 0;

    /**
     * Constructor
     */
    public SavotTR() {
    }

    /**
     * Create a TR element from a Separated Value String
     * 
     * @param svline
     *            String, line with separated values
     * @param sv
     *            char, separated value
     */
    public void SVtoTR(final String svline, final char sv) {
        String line = svline;

        int index;
        String token;
        TDs = new TDSet();
        // cut sv following the separator

        // tabulation
        do {
            if ((index = line.indexOf(sv)) >= 0) {
                token = line.substring(0, index);
                line = line.substring(index + 1);
            } else {
                // last element
                token = line;
            }
            SavotTD td = new SavotTD();
            td.setContent(token);
            TDs.addItem(td);
        } while (index >= 0);
    }

    /**
     * Get the TD set (same as getTDSet) TDSet
     *
     * @return TDSet
     */
    public TDSet getTDs() {
        if (TDs == null) {
            TDs = new TDSet();
        }
        return TDs;
    }

    /**
     * Get the TD set (same as getTDs) TDSet
     * @param capacity minimal capacity to provide 
     * @return TDSet
     */
    public TDSet getTDSet(final int capacity) {
        if (TDs == null) {
            TDs = new TDSet();
            TDs.ensureCapacity(capacity);
        }
        return TDs;
    }

    /**
     * Get the TD set (same as getTDs) TDSet
     * @see #getTDSet(int)
     * 
     * @return TDSet
     */
    public TDSet getTDSet() {
        if (TDs == null) {
            TDs = new TDSet();
        }
        return TDs;
    }

    /**
     * Set the TD set (same as setTDSet) TDSet
     * 
     * @param TDs
     */
    public void setTDs(final TDSet TDs) {
        this.TDs = TDs;
    }

    /**
     * Set the TD set (same as setTDs) TDSet
     * 
     * @param TDs
     */
    public void setTDSet(final TDSet TDs) {
        this.TDs = TDs;
    }

    /**
     * Get the corresponding line in the XML file or flow
     * 
     * @return lineInXMLFile
     */
    public int getLineInXMLFile() {
        return lineInXMLFile;
    }

    /**
     * Set the corresponding line in the XML file or flow during the parsing
     * 
     * @param lineInXMLFile
     */
    public void setLineInXMLFile(final int lineInXMLFile) {
        this.lineInXMLFile = lineInXMLFile;
    }

    /**
     * Clear this TR instance to recycle it
     */
    public void clear() {
        if (TDs != null) {
            TDs.removeAllItems(); // recycle TDSet (same capacity)
        }
        lineInXMLFile = 0;
    }
}
