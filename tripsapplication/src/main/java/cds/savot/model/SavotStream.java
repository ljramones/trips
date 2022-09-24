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
 * Stream element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotStream extends MarkupComment {

    // content
    private String content = null;
    // type attribute (locator, other)
    private String type = "locator"; // default
    // href attribute
    private String href = null;
    // actuate attribute
    private String actuate = null;
    // width encoding
    private String encoding = null;
    // expires attribute
    private String expires = null;
    // rights attribute
    private String rights = null;

    /**
     * Constructor
     */
    public SavotStream() {
    }

    /**
     * Set type attribute
     * 
     * @param type
     *            (locator, other)
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Get type attribute
     * 
     * @return String
     */
    public String getType() {
        return str(type);
    }

    /**
     * Set href attribute
     * 
     * @param href
     *            (URI)
     */
    public void setHref(final String href) {
        this.href = href;
    }

    /**
     * Get href attribute
     * 
     * @return String
     */
    public String getHref() {
        return str(href);
    }

    /**
     * Set actuate attribute
     * 
     * @param actuate
     *            (onLoad, onRequest, other, none)
     */
    public void setActuate(final String actuate) {
        this.actuate = actuate;
    }

    /**
     * Get actuate attribute
     * 
     * @return String
     */
    public String getActuate() {
        return str(actuate);
    }

    /**
     * Set encoding attribute
     * 
     * @param encoding
     *            (gzip, base64, dynamic, none)
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * Get encoding attribute
     * 
     * @return String
     */
    public String getEncoding() {
        return str(encoding);
    }

    /**
     * Set expires attribute
     * 
     * @param expires
     */
    public void setExpires(final String expires) {
        this.expires = expires;
    }

    /**
     * Get width attribute
     * 
     * @return String
     */
    public String getExpires() {
        return str(expires);
    }

    /**
     * Set rights attribute
     * 
     * @param rights
     */
    public void setRights(final String rights) {
        this.rights = rights;
    }

    /**
     * Get rights attribute
     * 
     * @return String
     */
    public String getRights() {
        return str(rights);
    }

    /**
     * Set content
     * 
     * @param content
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Get content
     * 
     * @return String
     */
    public String getContent() {
        return str(content);
    }
}
