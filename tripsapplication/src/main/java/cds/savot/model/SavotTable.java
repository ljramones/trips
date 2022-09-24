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
 * Table element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotTable extends MarkupComment implements IDSupport, NameSupport, RefSupport {

    // id attribute
    private String id = null;
    // name attribute
    private String name = null;
    // ucd attribute
    private String ucd = null;
    // utype attribute
    private String utype = null;
    // ref attribute
    private String ref = null;
    // nrows attribute
    private String nrows = null;
    // DESCRIPTION element
    private String description = null;
    // FIELD element
    private FieldSet fields = null;
    // PARAM element
    private ParamSet params = null;
    // GROUP element - since VOTable 1.1
    private GroupSet groups = null;
    // LINK element
    private LinkSet links = null;
    // DATA element
    private SavotData data = null;
    // INFO element at the end - since VOTable 1.2
    private InfoSet infosAtEnd = null;

    /**
     * Constructor
     */
    public SavotTable() {
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
     * Get ID attribute
     * 
     * @return a String
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
     * @return a String
     */
    @Override
    public String getName() {
        return str(name);
    }

    /**
     * Set ucd attribute
     * 
     * @param ucd
     */
    public void setUcd(final String ucd) {
        this.ucd = ucd;
    }

    /**
     * Get ucd attribute
     * 
     * @return a String
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
     * @return a String
     */
    public String getUtype() {
        return str(utype);
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
     * @return a String
     */
    @Override
    public String getRef() {
        return str(ref);
    }

    /**
     * Set nrows attribute
     * 
     * @param nrows
     */
    public void setNrows(final String nrows) {
        this.nrows = nrows;
    }

    /**
     * Set nrows attribute
     * 
     * @param nrows
     */
    public void setNrowsValue(final int nrows) {
        this.nrows = toStr(nrows);
    }

    /**
     * Get nrows attribute
     * 
     * @return a String
     */
    public String getNrows() {
        return str(nrows);
    }

    /**
     * Get nrows attribute
     * 
     * @return an int
     */
    public int getNrowsValue() {
        return integer(nrows);
    }

    /**
     * Set DESCRIPTION content
     * 
     * @param description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Get DESCRIPTION content
     * 
     * @return a String
     */
    public String getDescription() {
        return str(description);
    }

    /**
     * Get FIELD element set reference
     * 
     * @return FieldSet
     */
    public FieldSet getFields() {
        if (fields == null) {
            fields = new FieldSet();
        }
        return fields;
    }

    /**
     * Set FIELD element set reference
     * 
     * @param fields
     */
    public void setFields(final FieldSet fields) {
        this.fields = fields;
    }

    /**
     * Get PARAM element set reference
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
     * Set PARAM element set reference
     * 
     * @param params
     */
    public void setParams(final ParamSet params) {
        this.params = params;
    }

    /**
     * Get GROUP element set reference
     * 
     * @since VOTable 1.1
     * @return GroupSet
     */
    public GroupSet getGroups() {
        if (groups == null) {
            groups = new GroupSet();
        }
        return groups;
    }

    /**
     * Set GROUP element set reference
     * 
     * @since VOTable 1.1
     * @param groups
     */
    public void setGroups(final GroupSet groups) {
        this.groups = groups;
    }

    /**
     * Get LINK element set reference
     * 
     * @return LinkSet
     */
    public LinkSet getLinks() {
        if (links == null) {
            links = new LinkSet();
        }
        return links;
    }

    /**
     * Set LINK element set reference
     * 
     * @param links
     */
    public void setLinks(final LinkSet links) {
        this.links = links;
    }

    /**
     * Set DATA element
     * 
     * @param data
     */
    public void setData(final SavotData data) {
        this.data = data;
    }

    /**
     * Get DATA element
     * 
     * @return SavotData
     */
    public SavotData getData() {
        return data;
    }

    /**
     * Set the InfosAtEnd elements
     * 
     * @param infosAtEnd
     * @since VOTable 1.2
     */
    public void setInfosAtEnd(final InfoSet infosAtEnd) {
        this.infosAtEnd = infosAtEnd;
    }

    /**
     * Get the InfosAtEnd elements
     * 
     * @return an InfoSet object
     * @since VOTable 1.2
     */
    public InfoSet getInfosAtEnd() {
        if (infosAtEnd == null) {
            infosAtEnd = new InfoSet();
        }
        return infosAtEnd;
    }
}
