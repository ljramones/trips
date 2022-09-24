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
 * Values element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotValues extends MarkupComment implements IDSupport, RefSupport {

    // ID attribute
    private String id = null;
    // type attribute
    private String type = "legal";
    // null content
    private String nul = null;
    // ref content
    private String ref = null;
    // invalid content - deprecated since VOTable 1.1
    private String invalid = null;
    // MIN element
    private SavotMin min = null;
    // MAX element
    private SavotMax max = null;
    // OPTION element
    private OptionSet options = null;

    /**
     * Constructor
     */
    public SavotValues() {
    }

    /**
     * Set the id attribute
     * 
     * @param id
     *            String
     */
    @Override
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the id attribute
     * 
     * @return a String
     */
    @Override
    public String getId() {
        return str(id);
    }

    /**
     * Set the type attribute
     * 
     * @param type
     *            String (legal, actual)
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Get the type attribute
     * 
     * @return a String
     */
    public String getType() {
        return str(type);
    }

    /**
     * Set the null attribute
     * 
     * @param nul
     *            String
     */
    public void setNull(final String nul) {
        this.nul = nul;
    }

    /**
     * Get the null attribute
     * 
     * @return a String
     */
    public String getNull() {
        return str(nul);
    }

    /**
     * Set the ref attribute
     * 
     * @param ref
     *            ref
     */
    @Override
    public void setRef(final String ref) {
        this.ref = ref;
    }

    /**
     * Get the ref attribute
     * 
     * @return a String
     */
    @Override
    public String getRef() {
        return str(ref);
    }

    /**
     * Set the invalid attribute deprecated since VOTable 1.1
     * 
     * @param invalid
     *            String
     */
    public void setInvalid(final String invalid) {
        this.invalid = invalid;
    }

    /**
     * Get the invalid attribute deprecated since VOTable 1.1
     * 
     * @return a String
     */
    public String getInvalid() {
        return str(invalid);
    }

    /**
     * Set MIN element
     * 
     * @param min
     */
    public void setMin(final SavotMin min) {
        this.min = min;
    }

    /**
     * Get MIN element
     * 
     * @return a SavotMin object
     */
    public SavotMin getMin() {
        return min;
    }

    /**
     * Set MAX element
     * 
     * @param max
     */
    public void setMax(final SavotMax max) {
        this.max = max;
    }

    /**
     * Get MAX element
     * 
     * @return a SavotMax object
     */
    public SavotMax getMax() {
        return max;
    }

    /**
     * Get OPTION element set reference
     * 
     * @return OptionSet object
     */
    public OptionSet getOptions() {
        if (options == null) {
            options = new OptionSet();
        }
        return options;
    }

    /**
     * Set OPTION element set reference
     * 
     * @param options
     */
    public void setOptions(final OptionSet options) {
        this.options = options;
    }
}
