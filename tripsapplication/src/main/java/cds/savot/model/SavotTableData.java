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
 * TableData element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotTableData extends MarkupComment {

    private TRSet TRs = null;

    /**
     * Constructor
     */
    public SavotTableData() {
    }

    /**
     * Get the TR set reference
     * 
     * @return TRs
     */
    public TRSet getTRs() {
        if (TRs == null) {
            TRs = new TRSet();
        }
        return TRs;
    }

    /**
     * Set the TR set reference
     * 
     * @param TRs
     */
    public void setTRs(final TRSet TRs) {
        this.TRs = TRs;
    }
}
