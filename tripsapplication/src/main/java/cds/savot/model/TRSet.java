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

import java.util.Objects;

/**
 * <p>
 * Set of TR elements
 * </p>
 * 
 * @author Andre Schaaff
 * @see SavotSet
 */
public final class TRSet extends SavotSet<SavotTR> {

    /**
     * Constructor
     */
    public TRSet() {
    }

    /**
     * Get a TDSet object at the TRIndex position of the TRSet
     * 
     * @param TRIndex
     * @return TDSet
     */
    public TDSet getTDSet(final int TRIndex) {
        if (this.getItemCount() != 0) {
            return Objects.requireNonNull(getItemAt(TRIndex)).getTDSet();
        }
        return new TDSet();
    }
}
