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
 * Group element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotGroup extends MarkupComment implements IDSupport, NameSupport, RefSupport {

    // ID attribute
    private String id = null;
    // name attribute
    private String name = null;
    // ref attribute
    private String ref = null;
    // ucd attribute
    private String ucd = null;
    // utype attribute
    private String utype = null;
    // description element
    private String description = null;
    // FIELDRef elements
    private FieldRefSet fieldsref = null;
    // PARAM elements
    private ParamSet params = null;
    // PARAMRef elements
    private ParamRefSet paramsref = null;
    // GROUP elements
    private GroupSet groups = null;

    /**
     * Constructor
     */
    public SavotGroup() {
    }

    /**
     * Set ID attribute
     * 
     * @param id
     */
    @Override
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get id attribute
     * 
     * @return String
     */
    @Override
    public String getId() {
        return str(id);
    }

    /**
     * Set name attribute
     * 
     * @param name
     */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get name attribute
     * 
     * @return String
     */
    @Override
    public String getName() {
        return str(name);
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
     *            ([A-Za-z0-9_.,-]*)
     */
    public void setUcd(final String ucd) {
        this.ucd = ucd;
    }

    /**
     * Get ucd attribute
     * 
     * @return String
     */
    public String getUcd() {
        return str(ucd);
    }

    /**
     * Set utype attribute
     * 
     * @param utype
     */
    public void setUtype(final String utype) {
        this.utype = utype;
    }

    /**
     * Get utype attribute
     * 
     * @return String
     */
    public String getUtype() {
        return str(utype);
    }

    /**
     * Set DESCRIPTION element content
     * 
     * @param description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Get DESCRIPTION element content
     * 
     * @return String
     */
    public String getDescription() {
        return str(description);
    }

    /**
     * Get PARAM elements set reference
     * 
     * @return ParamSet
     */
    public ParamSet getParams() {
        if (params == null) {
            params = new ParamSet();
        }
        return params;
    }

    /**
     * Set PARAM elements set reference
     * 
     * @param params
     */
    public void setParams(final ParamSet params) {
        this.params = params;
    }

    /**
     * Set PARAMref elements set reference
     * 
     * @param paramsref
     */
    public void setParamsRef(final ParamRefSet paramsref) {
        this.paramsref = paramsref;
    }

    /**
     * Get PARAMref elements set reference
     * 
     * @return ParamRefSet
     */
    public ParamRefSet getParamsRef() {
        if (paramsref == null) {
            paramsref = new ParamRefSet();
        }
        return paramsref;
    }

    /**
     * Get FIELDref elements set reference
     * 
     * @return FieldRefSet
     */
    public FieldRefSet getFieldsRef() {
        if (fieldsref == null) {
            fieldsref = new FieldRefSet();
        }
        return fieldsref;
    }

    /**
     * Set FIELDref elements set reference
     * 
     * @param fieldsref
     */
    public void setFieldsRef(final FieldRefSet fieldsref) {
        this.fieldsref = fieldsref;
    }

    /**
     * Get GROUP elements set reference
     * 
     * @return GroupSet
     */
    public GroupSet getGroups() {
        if (groups == null) {
            groups = new GroupSet();
        }
        return groups;
    }

    /**
     * Set GROUP elements set reference
     * 
     * @param groups
     */
    public void setGroups(final GroupSet groups) {
        this.groups = groups;
    }
}
