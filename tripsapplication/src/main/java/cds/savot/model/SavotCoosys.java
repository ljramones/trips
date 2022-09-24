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
 * Coosys element
 * </p>
 * 
 * @author Andre Schaaff
 * @deprecated since 1.2
 */
public final class SavotCoosys extends MarkupComment implements IDSupport {

    // ID attribute
    private String id = null;
    // equinox attribute
    private String equinox = null;
    // epoch attribute
    private String epoch = null;
    // system attribute eq_FK4, eq_FK5, ICRS, ecl_FK4, ecl_FK5, galactic,
    // supergalactic, xy, barycentric, geo_app
    private String system = "eq_FK5"; // default
    // element content
    private String content = null;

    /**
     * Constructor
     */
    public SavotCoosys() {
    }

    /**
     * Set the id attribute
     * 
     * @param id
     */
    @Override
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the id attribute value
     * 
     * @return String
     */
    @Override
    public String getId() {
        return str(id);
    }

    /**
     * Set the equinox attribute
     * 
     * @param equinox
     *            ([JB]?[0-9]+([.][0-9]*)?)
     */
    public void setEquinox(final String equinox) {
        this.equinox = equinox;
    }

    /**
     * Get the equinox attribute value
     * 
     * @return String
     */
    public String getEquinox() {
        return str(equinox);
    }

    /**
     * Set the epoch attribute
     * 
     * @param epoch
     *            ([JB]?[0-9]+([.][0-9]*)?)
     */
    public void setEpoch(final String epoch) {
        this.epoch = epoch;
    }

    /**
     * Get the epoch attribute value
     * 
     * @return String
     */
    public String getEpoch() {
        return str(epoch);
    }

    /**
     * Set the system attribute
     * 
     * @param system
     *            (eq_FK4, eq_FK5, ICRS, ecl_FK4, ecl_FK5, galactic,
     *            supergalactic, xy, barycentric, geo_app)
     */
    public void setSystem(final String system) {
        this.system = system;
    }

    /**
     * Get the system attribute value
     * 
     * @return String
     */
    public String getSystem() {
        return str(system);
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
}
