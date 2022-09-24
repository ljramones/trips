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
 * Link element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotLink extends MarkupComment implements IDSupport {

    // content
    private String content = null;
    // ID attribute
    private String id = null;
    // content-role attribute
    private String contentRole = null;
    // content-type attribute
    private String contentType = null;
    // title attribute
    private String title = null;
    // value attribute
    private String value = null;
    // href attribute
    private String href = null;
    // gref attribute - removed since 1.1
    private String gref = null;
    // action attribute - extension since 1.1
    private String action = null;

    /**
     * Constructor
     */
    public SavotLink() {
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
     * Set contentRole attribute
     * 
     * @param contentRole
     *            (query, hints, doc, location)
     */
    public void setContentRole(final String contentRole) {
        this.contentRole = contentRole;
    }

    /**
     * Get contentRole attribute
     * 
     * @return String
     */
    public String getContentRole() {
        return str(contentRole);
    }

    /**
     * Set contentType attribute
     * 
     * @param contentType
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get contentType attribute
     * 
     * @return String
     */
    public String getContentType() {
        return str(contentType);
    }

    /**
     * Set title attribute
     * 
     * @param title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Get title attribute
     * 
     * @return String
     */
    public String getTitle() {
        return str(title);
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
     * Set href attribute
     * 
     * @param href
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
     * Set gref attribute removed in VOTable 1.1
     * 
     * @param gref
     */
    public void setGref(final String gref) {
        this.gref = gref;
    }

    /**
     * Get gref attribute removed in VOTable 1.1
     * 
     * @return String
     */
    public String getGref() {
        return str(gref);
    }

    /**
     * Set action attribute
     * 
     * @param action
     */
    public void setAction(final String action) {
        this.action = action;
    }

    /**
     * Get action attribute
     * 
     * @return String
     */
    public String getAction() {
        return str(action);
    }
}
