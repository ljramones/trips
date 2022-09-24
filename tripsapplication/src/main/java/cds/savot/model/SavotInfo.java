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
 * Info element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotInfo extends MarkupComment implements IDSupport, NameSupport, RefSupport {

    // id attribute
    private String id = null;

    // name attribute
    private String name = null;

    // value attribute
    private String value = null;

    // INFO element content
    private String content = null;

    // xtype attribute @since 1.2
    private String xtype = null;

    // ref attribute @since 1.2
    private String ref = null;

    // unit attribute @since 1.2
    private String unit = null;

    // ucd attribute @since 1.2
    private String ucd = null;

    // utype attribute @since 1.2
    private String utype = null;

    // DESCRIPTION element - since VOTable 1.2 (not in the standard)
    private String description = null;

    // VALUES element - since VOTable 1.2 (not in the standard)
    private SavotValues values = null;

    // LINK elements - since VOTable 1.2 (not in the standard)
    private LinkSet links = null;

    /**
     * Constructor
     */
    public SavotInfo() {
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
     * Set value attribute
     * 
     * @param value
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Get value attribute
     * 
     * @return String
     */
    public String getValue() {
        return str(value);
    }

    /**
     * Set xtype attribute
     * 
     * @since VOTable 1.2
     * @param xtype
     */
    public void setXtype(final String xtype) {
        this.xtype = xtype;
    }

    /**
     * Get xtype attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    public String getXtype() {
        return str(xtype);
    }

    /**
     * Set ref attribute
     * 
     * @since VOTable 1.2
     * @param ref
     */
    @Override
    public void setRef(final String ref) {
        this.ref = ref;
    }

    /**
     * Get ref attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    @Override
    public String getRef() {
        return str(ref);
    }

    /**
     * Set unit attribute
     * 
     * @since VOTable 1.2
     * @param unit
     */
    public void setUnit(final String unit) {
        this.unit = unit;
    }

    /**
     * Get unit attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    public String getUnit() {
        return str(unit);
    }

    /**
     * Set ucd attribute
     * 
     * @since VOTable 1.2
     * @param ucd
     */
    public void setUcd(final String ucd) {
        this.ucd = ucd;
    }

    /**
     * Get ucd attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    public String getUcd() {
        return str(ucd);
    }

    /**
     * Set utype attribute
     * 
     * @since VOTable 1.2
     * @param utype
     */
    public void setUtype(final String utype) {
        this.utype = utype;
    }

    /**
     * Get utype attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    public String getUtype() {
        return str(utype);
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

    /**
     * Set DESCRIPTION content
     * 
     * @since VOTable 1.2 (not in the standard)
     * @param description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Get DESCRIPTION content
     * 
     * @since VOTable 1.2 (not in the standard)
     * @return String
     */
    public String getDescription() {
        return str(description);
    }

    /**
     * Set the VALUES element
     * 
     * @since VOTable 1.2 (not in the standard)
     * @param values
     */
    public void setValues(final SavotValues values) {
        this.values = values;
    }

    /**
     * Get the VALUES element
     * 
     * @since VOTable 1.2 (not in the standard)
     * @return SavotValues
     */
    public SavotValues getValues() {
        return values;
    }

    /**
     * Get LINK elements set reference
     * 
     * @since VOTable 1.2 (not in the standard)
     * @return LinkSet
     */
    public LinkSet getLinks() {
        if (links == null) {
            links = new LinkSet();
        }
        return links;
    }

    /**
     * Set LINK elements set reference
     * 
     * @since VOTable 1.2 (not in the standard)
     * @param links
     */
    public void setLinks(final LinkSet links) {
        this.links = links;
    }
}
