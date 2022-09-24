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
 * Definitions element - removed in VOTable 1.1
 * </p>
 * 
 * @author Andre Schaaff
 * @deprecated since VOTable 1.1
 */
public final class SavotDefinitions extends MarkupComment {

    // COOSYS elements
    private CoosysSet coosys = null;
    // PARAM elements
    private ParamSet params = null;

    /**
     * Constructor
     */
    public SavotDefinitions() {
    }

    /**
     * Get COOSYS set reference removed in VOTable 1.1
     * 
     * @return a CoosysSet reference
     */
    public CoosysSet getCoosys() {
        if (coosys == null) {
            coosys = new CoosysSet();
        }
        return coosys;
    }

    /**
     * Set COOSYS set reference removed in VOTable 1.1
     * 
     * @param coosys
     */
    public void setCoosys(final CoosysSet coosys) {
        this.coosys = coosys;
    }

    /**
     * Get PARAM set reference removed in VOTable 1.1
     * 
     * @return a ParamSet reference
     */
    public ParamSet getParams() {
        if (params == null) {
            params = new ParamSet();
        }
        return params;
    }

    /**
     * Set PARAM set reference removed in VOTable 1.1
     * 
     * @param params
     */
    public void setParams(final ParamSet params) {
        this.params = params;
    }
}
