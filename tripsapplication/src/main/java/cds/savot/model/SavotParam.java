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
 * Param element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotParam extends MarkupComment implements IDSupport, NameSupport, RefSupport {

    // ID attribute
    private String id = null;
    // unit attribute
    private String unit = null;
    // datatype attribute
    private String datatype = null;
    // precision attribute
    private String precision = null;
    // width attribute
    private String width = null;
    // xtype attribute @since 1.2
    private String xtype = null;
    // ref attribute
    private String ref = null;
    // name attribute
    private String name = null;
    // ucd attribute
    private String ucd = null;
    // utype attribute
    private String utype = null;
    // arraysize attribute
    private String arraysize = null;
    // value attribute
    private String value = null;
    // DESCRIPTION element
    private String description = null;
    // VALUES element
    private SavotValues values = null;
    // LINK element
    private LinkSet links = null;

    /**
     * Constructor
     */
    public SavotParam() {
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
     * Set unit attribute
     * 
     * @param unit
     */
    public void setUnit(final String unit) {
        this.unit = unit;
    }

    /**
     * Get unit attribute
     * 
     * @return String
     */
    public String getUnit() {
        return str(unit);
    }

    /**
     * Set datatype attribute
     * 
     * @param datatype
     *            (boolean, bit, unsignedByte, short, int, long, char,
     *            unicodeChar, float, double, floatComplex, doubleComplex)
     */
    public void setDataType(final String datatype) {
        this.datatype = datatype;
    }

    /**
     * Get datatype attribute
     * 
     * @return String
     */
    public String getDataType() {
        return str(datatype);
    }

    /**
     * Set precision attribute
     * 
     * @param precision
     *            ([EF]?[1-0][0-9]*)
     */
    public void setPrecision(final String precision) {
        this.precision = precision;
    }

    /**
     * Get precision attribute
     * 
     * @return String
     */
    public String getPrecision() {
        return str(precision);
    }

    /**
     * Set width attribute
     * 
     * @param width
     */
    public void setWidth(final String width) {
        this.width = width;
    }

    /**
     * Set width attribute
     * 
     * @param width
     */
    public void setWidthValue(final int width) {
        this.width = toStr(width);
    }

    /**
     * Get width attribute
     * 
     * @return String
     */
    public String getWidth() {
        return str(width);
    }

    /**
     * Get width attribute
     * 
     * @return String
     */
    public int getWidthValue() {
        return integer(width);
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
     * Set arraysize attribute
     * 
     * @param arraysize
     *            (([0-9]+x)*[0-9]*[*]?(s\W)?)
     */
    public void setArraySize(final String arraysize) {
        this.arraysize = arraysize;
    }

    /**
     * Get arraysize attribute
     * 
     * @return String
     */
    public String getArraySize() {
        return str(arraysize);
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
     * @return String
     */
    public String getDescription() {
        return str(description);
    }

    /**
     * Set VALUES element
     * 
     * @param values
     */
    public void setValues(final SavotValues values) {
        this.values = values;
    }

    /**
     * Get VALUES element
     * 
     * @return a SavotValues object
     */
    public SavotValues getValues() {
        return values;
    }

    /**
     * Get Link set reference
     * 
     * @return a set of LINK elements
     */
    public LinkSet getLinks() {
        if (links == null) {
            links = new LinkSet();
        }
        return links;
    }

    /**
     * Set Link set reference
     * 
     * @param links
     */
    public void setLinks(LinkSet links) {
        this.links = links;
    }
}
