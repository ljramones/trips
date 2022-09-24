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
 * VOTable element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotVOTable extends MarkupComment implements IDSupport {

    // xmlns attribute
    private String xmlns = null;
    // xmlns:xsi attribute
    private String xmlnsxsi = null;
    // xsi:NoNamespaceSchemaLocation attribute
    private String xsinoschema = null;
    // xsi:schemaLocation attribute
    private String xsischema = null;
    // id attribute
    private String id = null;
    // version attribute (default : 1.2)
    private String version = "1.2";
    // DESCRIPTION element
    private String description = null;
    // COOSYS element set - deprecated since 1.2
    private CoosysSet coosys = null;
    // PARAM element set
    private ParamSet params = null;
    // GROUP element set
    private GroupSet groups = null;
    // INFO element set
    private InfoSet infos = null;
    // DEFINITIONS element
    @SuppressWarnings("deprecation")
    private SavotDefinitions definitions = null;
    // RESOURCE element
    private ResourceSet resources = null;
    // INFO element set at the end - since VOTable1.2
    private InfoSet infosAtEnd = null;

    /**
     * Constructor
     */
    public SavotVOTable() {
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xmlns
     *            String
     */
    public void setXmlns(final String xmlns) {
        this.xmlns = xmlns;
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXmlns() {
        return str(xmlns);
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xmlnsxsi
     *            String
     */
    public void setXmlnsxsi(final String xmlnsxsi) {
        this.xmlnsxsi = xmlnsxsi;
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXmlnsxsi() {
        return str(xmlnsxsi);
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xsinoschema
     *            String
     */
    public void setXsinoschema(final String xsinoschema) {
        this.xsinoschema = xsinoschema;
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXsinoschema() {
        return str(xsinoschema);
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xsischema
     *            String
     */
    public void setXsischema(final String xsischema) {
        this.xsischema = xsischema;
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXsischema() {
        return str(xsischema);
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
     * @return String
     */
    @Override
    public String getId() {
        return str(id);
    }

    /**
     * Set the version attribute
     * 
     * @param version
     *            String
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Get the version attribute
     * 
     * @return String
     */
    public String getVersion() {
        return str(version);
    }

    /**
     * Set DESCRIPTION element
     * 
     * @param description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Get DESCRIPTION element
     * 
     * @return a String
     */
    public String getDescription() {
        return str(description);
    }

    /**
     * Set DEFINITIONS element
     * 
     * @deprecated since VOTable 1.1
     * @param definitions
     */
    public void setDefinitions(final SavotDefinitions definitions) {
        this.definitions = definitions;
    }

    /**
     * Get DEFINITIONS element
     * 
     * @deprecated since VOTable 1.1
     * @return SavotDefinitions
     */
    public SavotDefinitions getDefinitions() {
        return definitions;
    }

    /**
     * Set the Coosys elements
     * 
     * @deprecated since VOTable 1.2
     * @param coosys
     */
    public void setCoosys(final CoosysSet coosys) {
        this.coosys = coosys;
    }

    /**
     * Get the Coosys elements
     * 
     * @deprecated since VOTable 1.2
     * @return a CoosysSet object
     */
    public CoosysSet getCoosys() {
        if (coosys == null) {
            coosys = new CoosysSet();
        }
        return coosys;
    }

    /**
     * Set the Infos elements
     * 
     * @param infos
     */
    public void setInfos(final InfoSet infos) {
        this.infos = infos;
    }

    /**
     * Get the Infos elements
     * 
     * @return a InfoSet object
     */
    public InfoSet getInfos() {
        if (infos == null) {
            infos = new InfoSet();
        }
        return infos;
    }

    /**
     * Set the Param elements
     * 
     * @param params
     */
    public void setParams(final ParamSet params) {
        this.params = params;
    }

    /**
     * Get the Param elements
     * 
     * @return a ParamSet object
     */
    public ParamSet getParams() {
        if (params == null) {
            params = new ParamSet();
        }
        return params;
    }

    /**
     * Set GROUP element set reference
     * 
     * @since VOTable 1.2
     * @param groups
     */
    public void setGroups(final GroupSet groups) {
        this.groups = groups;
    }

    /**
     * Get GROUP element set reference
     * 
     * @since VOTable 1.2
     * @return GroupSet
     */
    public GroupSet getGroups() {
        if (groups == null) {
            groups = new GroupSet();
        }
        return groups;
    }

    /**
     * Get RESOURCE set reference (FULL mode only)
     * 
     * @return ResourceSet (always NULL in SEQUENTIAL mode)
     */
    public ResourceSet getResources() {
        if (resources == null) {
            resources = new ResourceSet();
        }
        return resources;
    }

    /**
     * Set RESOURCE set reference
     * 
     * @param resources
     */
    public void setResources(final ResourceSet resources) {
        this.resources = resources;
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
     * @return a InfoSet object
     * @since VOTable 1.2
     */
    public InfoSet getInfosAtEnd() {
        if (infosAtEnd == null) {
            infosAtEnd = new InfoSet();
        }
        return infosAtEnd;
    }
}
