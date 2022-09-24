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

import java.util.Objects;

/**
 * <p>
 * Resource element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotResource extends MarkupComment implements IDSupport, NameSupport {

    // name attribute
    private String name = null;

    // id attribute
    private String id = null;

    // type attribute (results, meta)
    private String type = "results"; // default

    // utype attribute
    private String utype = null;

    // DESCRIPTION element
    private String description = null;

    // COOSYS element set - deprecated since 1.2
    private CoosysSet coosys = null;

    // GROUP element set - since VOTable 1.2
    private GroupSet groups = null;

    // PARAM element set
    private ParamSet params = null;

    // INFO element set
    private InfoSet infos = null;

    // LINK element set
    private LinkSet links = null;

    // TABLE element set
    private TableSet tables = null;

    // RESOURCE element set (recursive usage)
    private ResourceSet resources = null;

    // INFO (at End) element set - since VOTable 1.2
    private InfoSet infosAtEnd = null;

    /**
     * Constructor
     */
    public SavotResource() {
    }

    /**
     * init a SavotResource object
     */
    public void init() {
        name = null;
        id = null;
        type = null;
        utype = null;
        description = null;

        coosys = null;
        groups = null;
        params = null;
        infos = null;
        links = null;
        tables = null;
        resources = null;
        infosAtEnd = null;
    }

    /**
     * Set the description
     * 
     * @param description
     *            String
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Get the description
     * 
     * @return a String
     */
    public String getDescription() {
        return str(description);
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
     * Set the Link elements
     * 
     * @param links
     */
    public void setLinks(final LinkSet links) {
        this.links = links;
    }

    /**
     * Get the Link elements
     * 
     * @return a LinkSet object
     */
    public LinkSet getLinks() {
        if (links == null) {
            links = new LinkSet();
        }
        return links;
    }

    /**
     * Set the Table elements
     * 
     * @param tables
     */
    public void setTables(final TableSet tables) {
        this.tables = tables;
    }

    /**
     * Get the Table elements
     * 
     * @return a TableSet object
     */
    public TableSet getTables() {
        if (tables == null) {
            tables = new TableSet();
        }
        return tables;
    }

    /**
     * Set the Resource elements
     * 
     * @param resources
     */
    public void setResources(final ResourceSet resources) {
        this.resources = resources;
    }

    /**
     * Get the Resource elements
     * 
     * @return a ResourceSet object
     */
    public ResourceSet getResources() {
        if (resources == null) {
            resources = new ResourceSet();
        }
        return resources;
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

    /**
     * Set the name attribute
     * 
     * @param name
     *            String
     */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the name attribute
     * 
     * @return a String
     */
    @Override
    public String getName() {
        return str(name);
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
     * Set the type attribute
     * 
     * @param type
     *            String (results, meta)
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
     * Set the utype attribute
     * 
     * @param utype
     *            String
     */
    public void setUtype(final String utype) {
        this.utype = utype;
    }

    /**
     * Get the utype attribute
     * 
     * @return a String
     */
    public String getUtype() {
        return str(utype);
    }

    /**
     * Get the number of TR object for table index tableIndex (shortcut)
     * 
     * @param tableIndex
     * @return int
     */
    public int getTRCount(final int tableIndex) {
        return Objects.requireNonNull(getTables().getItemAt(tableIndex)).getData().getTableData().getTRs().getItemCount();
    }

    /**
     * Get a TRSet object for table index tableIndex (shortcut)
     * 
     * @param tableIndex
     * @return TRSet
     */
    public TRSet getTRSet(final int tableIndex) {
        return getTables().getItemAt(tableIndex).getData().getTableData().getTRs();
    }

    /**
     * Get a TR object for table index tableIndex and the corresponding row
     * index rowIndex of this table (shortcut)
     * 
     * @param tableIndex
     * @param rowIndex
     * @return SavotTR
     */
    public SavotTR getTR(final int tableIndex, final int rowIndex) {
        return getTables().getItemAt(tableIndex).getData().getTableData().getTRs().getItemAt(rowIndex);
    }

    /**
     * Return the number of tables contained in the resource this value doesn't
     * contain the tables of included resources (shortcut)
     * 
     * @return int
     */
    public int getTableCount() {
        return getTables().getItemCount();
    }

    /**
     * Get a FieldSet object for table index tableIndex (shortcut)
     * 
     * @param tableIndex
     * @return FieldSet
     */
    public FieldSet getFieldSet(final int tableIndex) {
        return Objects.requireNonNull(getTables().getItemAt(tableIndex)).getFields();
    }

    /**
     * Get a LinkSet object for table index tableIndex (shortcut)
     * 
     * @param tableIndex
     * @return LinkSet
     */
    public LinkSet getLinkSet(final int tableIndex) {
        return Objects.requireNonNull(getTables().getItemAt(tableIndex)).getLinks();
    }

    /**
     * Get a Description object (String) for table index tableIndex (shortcut)
     * 
     * @param tableIndex
     * @return String
     */
    public String getDescription(final int tableIndex) {
        return Objects.requireNonNull(getTables().getItemAt(tableIndex)).getDescription();
    }

    /**
     * Get a SavotData object for table index tableIndex (shortcut)
     * 
     * @param tableIndex
     * @return SavotData
     */
    public SavotData getData(final int tableIndex) {
        return Objects.requireNonNull(getTables().getItemAt(tableIndex)).getData();
    }
}
