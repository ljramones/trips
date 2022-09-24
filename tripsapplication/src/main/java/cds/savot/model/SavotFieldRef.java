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
 * Reference to Field element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotFieldRef extends MarkupComment implements RefSupport {

    // ref attribute
    private String ref = null;
    // ucd attribute - since VOTable 1.2
    private String ucd = null;
    // utype attribute - since VOTable 1.2
    private String utype = null;

    /**
     * Constructor
     */
    public SavotFieldRef() {
    }

    /**
     * Set ref attribute
     * 
     * @param ref
     */
    @Override
    public void setRef(final String ref) {
        this.ref = ref;
    }

    /**
     * Get ref attribute
     * 
     * @return String
     */
    @Override
    public String getRef() {
        return str(ref);
    }

    /**
     * Set ucd attribute
     * 
     * @param ucd
     * @since VOTable 1.2
     */
    public void setUcd(final String ucd) {
        this.ucd = ucd;
    }

    /**
     * Get ucd attribute
     * 
     * @return String
     * @since VOTable 1.2
     */
    public String getUcd() {
        return str(ucd);
    }

    /**
     * Set utype attribute
     * 
     * @param utype
     * @since VOTable 1.2
     */
    public void setUtype(final String utype) {
        this.utype = utype;
    }

    /**
     * Get utype attribute
     * 
     * @return String
     * @since VOTable 1.2
     */
    public String getUtype() {
        return str(utype);
    }
}
