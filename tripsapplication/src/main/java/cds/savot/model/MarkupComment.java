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
 * Comments for XML output
 * </p>
 * 
 * @author Andre Schaaff
 */
public class MarkupComment extends SavotBase {

    // comments used for VOTable XML document creation
    private String aboveComment = null;
    private String belowComment = null;

    /**
     * Constructor
     */
    public MarkupComment() {
    }

    /**
     * Set below comment
     * 
     * @param belowComment
     */
    public final void setBelow(final String belowComment) {
        this.belowComment = belowComment;
    }

    /**
     * Get below comment
     * 
     * @return a String
     */
    public final String getBelow() {
        return str(belowComment);
    }

    /**
     * Set above comment
     * 
     * @param aboveComment
     */
    public final void setAbove(final String aboveComment) {
        this.aboveComment = aboveComment;
    }

    /**
     * Get above comment
     * 
     * @return a String
     */
    public final String getAbove() {
        return str(aboveComment);
    }
}
