package cds.savot.stax;

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

import cds.savot.common.Markups;
import cds.savot.common.SavotStatistics;
import cds.savot.common.VOTableTag;
import cds.savot.model.*;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 * <p>
 * It has been tested with Java 1.6 Stax implementation
 * but it is possible to use other Stax implementations
 * </p>
 * <p>
 * Designed to use with JSR-173 compliant (Streaming API for XML)
 * </p>
 *
 * <p> remark L. Bourgès : equalsIgnoreCase() vs() equals as XML is case sensitive and VOTable specification says that clearly</p>
 *
 * @author Andre Schaaff
 */
@SuppressWarnings({"deprecation", "UseOfSystemOutOrSystemErr"})
public final class SavotStaxParser implements Markups {

    /**
     * Logger associated to SavotStaxParser classes
     */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SavotStaxParser.class.getName());
    /**
     * flag to enable / disable usage of String.trim() on every value (disabled by default)
     */
    private static final boolean doTrimValues = false;
    /**
     * flag to enable / disable line numbers (TR)
     */
    private static final boolean doLineInfo = false;
    /**
     * flag to enable / disable statistics
     */
    private static final boolean doStats = false;

    /* Parsing modes */
    /**
     * FULL parsing mode: deprecated and replaced by FULLREAD
     */
    public static final int FULL = 0;
    /**
     * FULLREAD parsing mode: all in memory
     */
    public static final int FULLREAD = 0;
    /**
     * SEQUENTIAL parsing mode: deprecated and replaced by RESOURCEREAD
     */
    public static final int SEQUENTIAL = 1;
    /**
     * RESOURCEREAD parsing mode: resource per resource reading
     */
    public static final int RESOURCEREAD = 1;
    /**
     * ROWREAD parsing mode: row per row reading
     */
    public static final int ROWREAD = 2;
    /**
     * default stack capacity = 4 slots
     */
    public final static int DEFAULT_STACK_CAPACITY = 4;
    /**
     * empty TD instance
     */
    private final static SavotTD EMPTY_TD = new SavotTD();
    /**
     * shared XMLInputFactory instance
     */
    private static volatile XMLInputFactory XML_INPUT_FACTORY = null;

    /* members */
    /**
     * statistics dedicated to this parser
     */
    private final SavotStatistics statistics;
    /**
     * xml stream parser needed for sequential parsing
     */
    private XMLStreamReader xmlParser = null;
    /**
     * input stream used to close it anyway
     */
    private InputStream inputStream = null;
    /**
     * reader used to close it anyway
     */
    private Reader reader = null;
    /**
     * debug mode
     */
    private boolean debugMode = false;

    // data model objects
    private SavotVOTable _currentVOTable = new SavotVOTable();
    private SavotResource _currentResource = new SavotResource(); // RESOURCEREAD mode only
    private SavotTR _currentTR = new SavotTR();          // ROWREAD mode only

    // used for statistics
    private int resourceCounter = 0;
    private int tableCounter = 0;
    private int rowCounter = 0;
    private int dataCounter = 0;

    /**
     * used for recursive management
     */
    private final ArrayList<VOTableTag> fatherTags = new ArrayList<VOTableTag>(DEFAULT_STACK_CAPACITY);
    /**
     * used for recursive resources, LIFO mode
     */
    private final ArrayList<SavotResource> resourcestack = new ArrayList<SavotResource>(DEFAULT_STACK_CAPACITY);
    /**
     * used for recursive options, LIFO mode
     */
    private final ArrayList<SavotOption> optionstack = new ArrayList<SavotOption>(DEFAULT_STACK_CAPACITY);
    /**
     * used for recursives groups, LIFO mode
     */
    private final ArrayList<SavotGroup> groupstack = new ArrayList<SavotGroup>(DEFAULT_STACK_CAPACITY);

    // for multi level resource
    private int includedResource = 0;
    // for multi level option
    private int includedOption = 0;
    // for multi level group
    private int includedGroup = 0;

    private SavotTable currentTable = null;
    private SavotField currentField = null;
    private SavotFieldRef currentFieldRef = null;
    private SavotGroup currentGroup = null; // new since VOTable 1.1
    private SavotParam currentParam = null;
    private SavotParamRef currentParamRef = null;
    private SavotData currentData = null;
    private SavotValues currentValues = null;
    private SavotTableData currentTableData = null;
    private String currentDescription = null;
    private SavotLink currentLink = null;
    private SavotInfo currentInfo = null;
    private SavotMin currentMin = null;
    private SavotMax currentMax = null;
    private SavotOption currentOption = null;
    private SavotCoosys currentCoosys = null;
    private SavotDefinitions currentDefinitions = null;
    private SavotBinary currentBinary = null;
    private SavotBinary2 currentBinary2 = null;
    private SavotFits currentFits = null;
    private SavotStream currentStream = null;

    /**
     * Map containing object references which have an ID
     * So it is possible to retrieve such object reference
     * Used to resolve ID ref
     * TODO: move such mapping into SavotVOTable (root element)
     */
    private final Map<String, Object> idRefLinks = new HashMap<String, Object>(256);

    /**
     * @return XMLInputFactory instance
     * @throws FactoryConfigurationError if the factory can not be loaded
     * @throws IllegalArgumentException  if the property "javax.xml.stream.isCoalescing" is not supported
     */
    private static synchronized XMLInputFactory getXMLInputFactory() throws FactoryConfigurationError, IllegalArgumentException {
        if (XML_INPUT_FACTORY == null) {
            final XMLInputFactory xmlif = XMLInputFactory.newInstance();
            xmlif.setProperty("javax.xml.stream.isCoalescing", true);
            XML_INPUT_FACTORY = xmlif;
        }
        return XML_INPUT_FACTORY;
    }

    /**
     * Constructor
     *
     * @param file a file to parse
     * @param mode FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *             ROWREAD (per ROW, for small memory size applications)
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final String file, final int mode) throws IOException, XMLStreamException {
        this(file, mode, false);
    }

    /**
     * Constructor
     *
     * @param file  a file to parse
     * @param mode  FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *              ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final String file, final int mode, final boolean debug) throws IOException, XMLStreamException {
        this(file, mode, debug, new SavotStatistics());
    }

    /**
     * Constructor
     *
     * @param file  a file to parse
     * @param mode  FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *              ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @param stats
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final String file, final int mode, final boolean debug, final SavotStatistics stats) throws IOException, XMLStreamException {

        this.statistics = stats;

        enableDebug(debug);

        final boolean compressed = file.endsWith("gz");

        // set the input of the parser
        this.inputStream = getInputStream(new FileInputStream(file), compressed);
        this.xmlParser = getXMLInputFactory().createXMLStreamReader(this.inputStream, "UTF-8"); /* specify encoding ? */

        // fix parsing mode:
        final int parsingType = parseMode(mode);

        // parse the stream in the given mode
        if (parsingType == SavotStaxParser.FULLREAD) {
            parse(parsingType);
        }
    }

    /**
     * Constructor
     *
     * @param url  url to parse
     * @param mode FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *             ROWREAD (per ROW, for small memory size applications)
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final URL url, final int mode) throws IOException, XMLStreamException {
        this(url, mode, false);
    }

    /**
     * Constructor
     *
     * @param url   url to parse
     * @param mode  FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *              ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final URL url, final int mode, final boolean debug) throws IOException, XMLStreamException {
        this(url, mode, debug, new SavotStatistics());
    }

    /**
     * Constructor
     *
     * @param url   url to parse
     * @param mode  FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *              ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @param stats
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final URL url, final int mode, final boolean debug, final SavotStatistics stats) throws IOException, XMLStreamException {

        this.statistics = stats;

        enableDebug(debug);

        // TODO: detect compression
        final boolean compressed = url.getPath().endsWith("gz");

        // set the input of the parser
        this.inputStream = getInputStream(url.openStream(), compressed);
        this.xmlParser = getXMLInputFactory().createXMLStreamReader(this.inputStream); /* specify encoding ? */

        // fix parsing mode:

        final int parsingType = parseMode(mode);

        // parse the stream in the given mode
        if (parsingType == SavotStaxParser.FULLREAD) {
            parse(parsingType);
        }
    }

    /**
     * Constructor
     *
     * @param instream stream to parse
     * @param mode     FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *                 ROWREAD (per ROW, for small memory size applications)
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final InputStream instream, final int mode) throws IOException, XMLStreamException {
        this(instream, mode, false);
    }

    /**
     * Constructor
     *
     * @param instream stream to parse
     * @param mode     FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *                 ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final InputStream instream, final int mode, final boolean debug) throws IOException, XMLStreamException {

        this(instream, mode, debug, new SavotStatistics());
    }

    /**
     * Constructor
     *
     * @param instream stream to parse
     * @param mode     FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *                 ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @param stats
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final InputStream instream, final int mode, final boolean debug, final SavotStatistics stats) throws IOException, XMLStreamException {

        this.statistics = stats;

        enableDebug(debug);

        // set the input of the parser
        this.inputStream = getInputStream(instream, false);
        this.xmlParser = getXMLInputFactory().createXMLStreamReader(this.inputStream); /* specify encoding ? */

        // fix parsing mode:

        final int parsingType = parseMode(mode);

        // parse the stream in the given mode
        if (parsingType == SavotStaxParser.FULLREAD) {
            parse(parsingType);
        }
    }

    /**
     * Constructor
     *
     * @param reader reader to parse
     * @param mode   FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *               ROWREAD (per ROW, for small memory size applications)
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final Reader reader, final int mode) throws IOException, XMLStreamException {

        this(reader, mode, false);
    }

    /**
     * Constructor
     *
     * @param reader reader to parse
     * @param mode   FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *               ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final Reader reader, final int mode, final boolean debug) throws IOException, XMLStreamException {

        this(reader, mode, debug, new SavotStatistics());
    }

    /**
     * Constructor
     *
     * @param reader reader to parse
     * @param mode   FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *               ROWREAD (per ROW, for small memory size applications)
     * @param debug
     * @param stats
     * @throws IOException
     * @throws XMLStreamException
     */
    public SavotStaxParser(final Reader reader, final int mode,
                           final boolean debug, final SavotStatistics stats) throws IOException, XMLStreamException {

        this.statistics = stats;

        enableDebug(debug);

        // set the input of the parser
        this.reader = reader;
        this.xmlParser = getXMLInputFactory().createXMLStreamReader(this.reader);

        // fix parsing mode:
        final int parsingType = parseMode(mode);

        // parse the stream in the given mode
        if (parsingType == SavotStaxParser.FULLREAD) {
            parse(parsingType);
        }
    }

    /**
     * Return the parsing type (FULLREAD, RESOURCEREAD or ROWREAD)
     *
     * @param mode mode (FULL, SEQUENTIAL, FULLREAD, RESOURCEREAD or ROWREAD)
     * @return parsing type
     */
    private int parseMode(final int mode) {
        switch (mode) {
            case ROWREAD:
                return ROWREAD;
            case SEQUENTIAL:
                return RESOURCEREAD;
            case FULL:
            default:
                return FULLREAD;
        }
    }

    /**
     * Get a buffered input stream or a gzip input stream
     *
     * @param instream   stream to wrap
     * @param compressed true to indicate to use a gzip input stream
     * @return input stream
     * @throws IOException useless
     */
    private InputStream getInputStream(final InputStream instream, final boolean compressed) throws IOException {
        // best buffer size = 8K because kXmlParser uses also a 8K read buffer
        final int bufferSize = 8 * 1024; // 8K read buffer

        final InputStream in;
        if (compressed) {
            in = new GZIPInputStream(instream, bufferSize); // 512 bytes by default
        } else {
            // no buffer as Stax parser has a 8K read buffer too
            in = instream;
        }
        return in;
    }

    /**
     * Close the input stream if still opened and free the internal parser
     */
    public void close() {
        if (this.xmlParser != null) {
            try {
                this.xmlParser.close();
            } catch (XMLStreamException xse) {
                logger.log(Level.INFO, "Exception SavotStaxParser.close: ", xse);
            }
            this.xmlParser = null;
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException ioe) {
                logger.log(Level.INFO, "Exception SavotStaxParser.close: ", ioe);
            }
            this.inputStream = null;
        }
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException ioe) {
                logger.log(Level.INFO, "Exception SavotStaxParser.close: ", ioe);
            }
            this.reader = null;
        }
    }

    /**
     * Reset of the engine before another parsing
     * LBO: useless methods ?
     */
    public void reset() {
        // data model global classes
        _currentVOTable = new SavotVOTable();
        _currentResource = new SavotResource();
        rowCounter = 0;
        resourceCounter = 0;
        tableCounter = 0;
        dataCounter = 0;
        idRefLinks.clear();
        // used for recursive resources, LIFO mode
        resourcestack.clear();
        // used for recursive options, LIFO mode
        optionstack.clear();
        // used for recursive groups, LIFO mode
        groupstack.clear();
    }

    /**
     * Get the last element from the resourcestack
     *
     * @return SavotResource
     */
    private SavotResource getResourceStack() {
        return resourcestack.remove(resourcestack.size() - 1);
    }

    /**
     * Put a resource on the resourcestack
     *
     * @param resource
     */
    private void putResourceStack(final SavotResource resource) {
        resourcestack.add(resource);
    }

    /**
     * Get the last element from the optionstack
     *
     * @return SavotOption
     */
    private SavotOption getOptionStack() {
        return optionstack.remove(optionstack.size() - 1);
    }

    /**
     * Put an option on the optionstack
     *
     * @param option
     */
    private void putOptionStack(final SavotOption option) {
        optionstack.add(option);
    }

    /**
     * Get the last element from the groupstack
     *
     * @return SavotGroup
     */
    private SavotGroup getGroupStack() {
        return groupstack.remove(groupstack.size() - 1);
    }

    /**
     * Put a group on the groupstack
     *
     * @param group
     */
    private void putGroupStack(final SavotGroup group) {
        groupstack.add(group);
    }

    /**
     * Return last father tag
     *
     * @return tag or null
     */
    private VOTableTag lastFather() {
        int size = fatherTags.size();
        if (size == 0) {
            return VOTableTag.UNDEFINED;
        }
        return fatherTags.get(size - 1);
    }

    /**
     * Parsing engine
     *
     * @param parsingType FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *                    ROWREAD (per ROW, for small memory size applications)
     * @throws IOException
     * @throws XMLStreamException
     */
    public void parse(final int parsingType) throws IOException, XMLStreamException {

        final XMLStreamReader parser = this.xmlParser;


        if (parser == null) {
            return;
        }

        if (parsingType != ROWREAD && parsingType != RESOURCEREAD) {
            // for multi level resource
            includedResource = 0;
            // for multi level option
            includedOption = 0;
            // for multi level group
            includedGroup = 0;

            currentTable = new SavotTable();
            currentField = new SavotField();
            currentFieldRef = new SavotFieldRef();
            currentGroup = new SavotGroup(); // new since VOTable 1.1
            currentParam = new SavotParam();
            currentParamRef = new SavotParamRef();
            currentData = new SavotData();
            currentValues = new SavotValues();
            currentTableData = new SavotTableData();
            currentDescription = "";
            currentLink = new SavotLink();
            currentInfo = new SavotInfo();
            currentMin = new SavotMin();
            currentMax = new SavotMax();
            currentOption = new SavotOption();
            currentCoosys = new SavotCoosys();
            currentDefinitions = new SavotDefinitions();
            currentBinary = new SavotBinary();
            currentBinary2 = new SavotBinary2();
            currentFits = new SavotFits();
            currentStream = new SavotStream();
        }

        String operation = "UNDEFINED";
        VOTableTag tag = null;
        int rowCount = rowCounter;
        int dataCount = dataCounter;
        int lineNumber = 0;

        try {
            // local copy for performance:
            final boolean trace = debugMode;
            final SavotStatistics stats = statistics;
            final SavotTD emptyTD = EMPTY_TD;
            final ArrayList<VOTableTag> father = fatherTags;

            // used for RESOURCEREAD parsing
            boolean resourceComplete = false;

            // used for ROWREAD parsing
            boolean TRComplete = false;

            // local variables for performance:
            SavotTR currentTR = _currentTR;
            TDSet tdSet = null;
            SavotTD currentTD = null;

            // name from parser.getName and current markup
            String name, prefix;
            VOTableTag currentMarkup = VOTableTag.UNDEFINED;
            String attrName, attrValue;
            String textValue;
            int i, counter;

            if (currentTR != null) {
                currentTR.clear(); // recycle TR instance (also TDSet)
                tdSet = currentTR.getTDs();
            }

            // event type
            int eventType = parser.getEventType();

            // System.out.println("ENTREE DANS PARSER");
            // while the end of the document is not reach
            while (eventType != XMLEvent.END_DOCUMENT) {
                /* custom line number as not provided by Stax parser */
                lineNumber++;

                // treatment depending on event type
                switch (eventType) {

                    // if a start tag is reach
                    case XMLEvent.START_ELEMENT:
                        operation = "START_ELEMENT";

                        // the name of the current tag
                        name = parser.getLocalName();

                        if (name != null) {
                            if (trace) {
                                System.err.println("Name ---> " + name);
                            }
                            // avoid parsing name twice:
                            if (tag == null) {
                                tag = VOTableTag.parseTag(name);
                            }

                            if (trace) {
                                System.err.println(tag + " begin");
                            }
                            System.err.println("TAG -> " + tag);

                            // use most probable tags FIRST (performance) i.e TD / TR first :
                            switch (tag) {
                                case TD:
                                    // avoid creating new SavotTD if the value is empty !!
                                    currentTD = null;

                                    if (doStats) {
                                        // for statistics only
                                        dataCount++;
                                    }
                                    break;

                                case TR:
                                    stats.iTDLocalReset();

                                    // recycle TR instance:
                                    if (currentTR == null) {
                                        // create a new row
                                        currentTR = new SavotTR();
                                        tdSet = currentTR.getTDs();
                                    }
                                    if (doLineInfo) {
                                        currentTR.setLineInXMLFile(lineNumber);
                                    }
                                    break;

                                case DESCRIPTION:
                                    break;

                                case VOTABLE:
                                    // Get namespaces from Stax:
                                    counter = parser.getNamespaceCount();
                                    if (counter != 0) {
                                        String uri;
                                        for (i = 0; i < parser.getNamespaceCount(); i++) {
                                            prefix = parser.getNamespacePrefix(i);
                                            uri = parser.getNamespaceURI(i);
                                            if (prefix == null) {
                                                // xmlns attribute:
                                                if (trace) {
                                                    System.err.println("xmlns='" + uri + "'");
                                                }
                                                _currentVOTable.setXmlns(uri);
                                            } else {
                                                if (trace) {
                                                    System.err.println("xmlns:" + prefix + "='" + uri + "'");
                                                }
                                                // TODO: handle multiple namespaces:
                                                if (prefix.equalsIgnoreCase(XSI)) {
                                                    _currentVOTable.setXmlnsxsi(uri);
                                                }
                                            }
                                        }
                                    }

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                if (trace) {
                                                    System.err.println("VOTABLE attribute:" + parser.getAttributeName(i) + "='" + attrValue + "'");
                                                }
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(VERSION)) {
                                                    _currentVOTable.setVersion(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    _currentVOTable.setId(attrValue);
                                                    idRefLinks.put(attrValue, _currentVOTable);
                                                } else {
                                                    prefix = parser.getAttributePrefix(i);
                                                    if (trace) {
                                                        System.err.println("VOTABLE attribute:" + prefix + ":" + attrName);
                                                    }
                                                    /* TODO: use uri because prefix can be changed ? */
                                                    if (XSI.equalsIgnoreCase(prefix)) {
                                                        if (attrName.equalsIgnoreCase(XSI_NOSCHEMA)) {
                                                            _currentVOTable.setXsinoschema(attrValue);
                                                        } else if (attrName.equalsIgnoreCase(XSI_SCHEMA)) {
                                                            _currentVOTable.setXsischema(attrValue);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case RESOURCE:
                                    stats.iResourcesInc();

                                    if (includedResource > 0) {
                                        // inner case (multi level resources)
                                        putResourceStack(_currentResource);
                                        if (trace) {
                                            System.err.println("RESOURCE - included");
                                        }
                                    } else if (trace) {
                                        System.err.println("RESOURCE - not included");
                                    }

                                    includedResource++;

                                    // for statistics only
                                    resourceCounter++;

                                    if (parsingType == FULL || _currentResource == null || parsingType == ROWREAD) {
                                        _currentResource = new SavotResource();
                                    } else {
                                        _currentResource.init();
                                    }

                                    _currentResource.setType(""); // correct the "results" default value

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(NAME)) {
                                                    _currentResource.setName(attrValue);
                                                } else if (attrName.equalsIgnoreCase(TYPE)) {
                                                    _currentResource.setType(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    _currentResource.setUtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    _currentResource.setId(attrValue);
                                                    if (parsingType == FULL) {
                                                        idRefLinks.put(attrValue, _currentResource);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case TABLE:
                                    stats.iTablesInc();

                                    currentTable = new SavotTable();

                                    // for statistics only
                                    tableCounter++;

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(NAME)) {
                                                    currentTable.setName(attrValue);
                                                    if (trace) {
                                                        System.err.println("TABLE name " + currentTable.getName());
                                                    }
                                                } else if (attrName.equalsIgnoreCase(UCD)) {
                                                    // new since VOTable 1.1
                                                    currentTable.setUcd(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    // new since VOTable 1.1
                                                    currentTable.setUtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(REF)) {
                                                    currentTable.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(NROWS)) {
                                                    // new since VOTable 1.1
                                                    currentTable.setNrows(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentTable.setId(attrValue);
                                                    if (parsingType == FULL) {
                                                        idRefLinks.put(attrValue, currentTable);
                                                        if (trace) {
                                                            System.err.println(attrValue);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case FIELD:
                                    currentField = new SavotField();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(UNIT)) {
                                                    currentField.setUnit(attrValue);
                                                } else if (attrName.equalsIgnoreCase(DATATYPE)) {
                                                    currentField.setDataType(attrValue);
                                                } else if (attrName.equalsIgnoreCase(PRECISION)) {
                                                    currentField.setPrecision(attrValue);
                                                } else if (attrName.equalsIgnoreCase(WIDTH)) {
                                                    currentField.setWidth(attrValue);
                                                } else if (attrName.equalsIgnoreCase(REF)) {
                                                    currentField.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(NAME)) {
                                                    currentField.setName(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UCD)) {
                                                    currentField.setUcd(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ARRAYSIZE)) {
                                                    currentField.setArraySize(attrValue);
                                                } else if (attrName.equalsIgnoreCase(TYPE)) {
                                                    // deprecated since VOTable 1.1
                                                    currentField.setType(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    currentField.setUtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentField.setId(attrValue);
                                                    idRefLinks.put(attrValue, currentField);
                                                }
                                            }
                                        }
                                    }
                                    if (trace) {
                                        System.err.println("on vient de remplir un FIELD ---> " + currentField.getName());
                                    }
                                    break;

                                case FIELDREF:
                                    currentFieldRef = new SavotFieldRef();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(REF)) {
                                                    currentFieldRef.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UCD)) {
                                                    currentFieldRef.setUcd(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    currentFieldRef.setUtype(attrValue);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case VALUES:
                                    currentValues = new SavotValues();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(TYPE)) {
                                                    currentValues.setType(attrValue);
                                                } else if (attrName.equalsIgnoreCase(NULL)) {
                                                    currentValues.setNull(attrValue);
                                                } else if (attrName.equalsIgnoreCase(INVALID)) {
                                                    currentValues.setInvalid(attrValue);
                                                } else if (attrName.equalsIgnoreCase(REF)) {
                                                    currentValues.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentValues.setId(attrValue);
                                                    idRefLinks.put(attrValue, currentValues);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case STREAM:
                                    currentStream = new SavotStream();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(TYPE)) {
                                                    currentStream.setType(attrValue);
                                                } else if (attrName.equalsIgnoreCase(HREF)) {
                                                    currentStream.setHref(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ACTUATE)) {
                                                    currentStream.setActuate(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ENCODING)) {
                                                    currentStream.setEncoding(attrValue);
                                                } else if (attrName.equalsIgnoreCase(EXPIRES)) {
                                                    currentStream.setExpires(attrValue);
                                                } else if (attrName.equalsIgnoreCase(RIGHTS)) {
                                                    currentStream.setRights(attrValue);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case DATA:
                                    currentData = new SavotData();
                                    break;

                                case BINARY:
                                    currentBinary = new SavotBinary();
                                    break;

                                case BINARY2:
                                    currentBinary2 = new SavotBinary2();
                                    break;

                                case FITS:
                                    currentFits = new SavotFits();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(REF)) {
                                                    currentFits.setExtnum(attrValue);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case TABLEDATA:
                                    currentTableData = new SavotTableData();

                                    if (parsingType == ROWREAD) {
                                        // if row sequential reading then storage of the metadata
                                        if (stats.getITablesLocal() > 1) {
                                            // not the first TABLE of the RESOURCE
                                            _currentVOTable.getResources().removeItemAt(_currentVOTable.getResources().getItemCount() - 1);
                                        }
                                        // TODO: use current table directly with a getter ?
                                        _currentResource.getTables().addItem(currentTable);
                                        _currentVOTable.getResources().addItem(_currentResource);
                                    }
                                    break;

                                case PARAM:
                                    currentParam = new SavotParam();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(UNIT)) {
                                                    currentParam.setUnit(attrValue);
                                                } else if (attrName.equalsIgnoreCase(DATATYPE)) {
                                                    currentParam.setDataType(attrValue);
                                                } else if (attrName.equalsIgnoreCase(PRECISION)) {
                                                    currentParam.setPrecision(attrValue);
                                                } else if (attrName.equalsIgnoreCase(WIDTH)) {
                                                    currentParam.setWidth(attrValue);
                                                } else if (attrName.equalsIgnoreCase(REF)) {
                                                    currentParam.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(NAME)) {
                                                    currentParam.setName(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UCD)) {
                                                    currentParam.setUcd(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    currentParam.setUtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(VALUE)) {
                                                    currentParam.setValue(attrValue);
                                                } else if (attrName.equalsIgnoreCase(XTYPE)) {
                                                    currentParam.setXtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ARRAYSIZE)) {
                                                    currentParam.setArraySize(attrValue);
                                                } else if (attrName.equalsIgnoreCase(XTYPE)) {
                                                    currentParam.setXtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentParam.setId(attrValue);
                                                    idRefLinks.put(attrValue, currentParam);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case PARAMREF:
                                    currentParamRef = new SavotParamRef();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(REF)) {
                                                    currentParamRef.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UCD)) {
                                                    currentParamRef.setUcd(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    currentParamRef.setUtype(attrValue);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case LINK:
                                    currentLink = new SavotLink();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(CONTENTROLE)) {
                                                    currentLink.setContentRole(attrValue);
                                                } else if (attrName.equalsIgnoreCase(CONTENTTYPE)) {
                                                    currentLink.setContentType(attrValue);
                                                } else if (attrName.equalsIgnoreCase(TITLE)) {
                                                    currentLink.setTitle(attrValue);
                                                } else if (attrName.equalsIgnoreCase(VALUE)) {
                                                    currentLink.setValue(attrValue);
                                                } else if (attrName.equalsIgnoreCase(HREF)) {
                                                    currentLink.setHref(attrValue);
                                                } else if (attrName.equalsIgnoreCase(GREF)) {
                                                    // deprecated since VOTable 1.1
                                                    currentLink.setGref(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ACTION)) {
                                                    currentLink.setAction(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentLink.setId(attrValue);
                                                    idRefLinks.put(attrValue, currentLink);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case INFO:
                                    currentInfo = new SavotInfo();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(NAME)) {
                                                    currentInfo.setName(attrValue);
                                                } else if (attrName.equalsIgnoreCase(VALUE)) {
                                                    currentInfo.setValue(attrValue);
                                                } else if (attrName.equalsIgnoreCase(XTYPE)) {
                                                    currentInfo.setXtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UNIT)) {
                                                    currentInfo.setUnit(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UCD)) {
                                                    currentInfo.setUcd(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    currentInfo.setUtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(REF)) {
                                                    currentInfo.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentInfo.setId(attrValue);
                                                    idRefLinks.put(attrValue, currentInfo);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case MIN:
                                    currentMin = new SavotMin();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(VALUE)) {
                                                    currentMin.setValue(attrValue);
                                                } else if (attrName.equalsIgnoreCase(INCLUSIVE)) {
                                                    currentMin.setInclusive(attrValue);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case MAX:
                                    currentMax = new SavotMax();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(VALUE)) {
                                                    currentMax.setValue(attrValue);
                                                } else if (attrName.equalsIgnoreCase(INCLUSIVE)) {
                                                    currentMax.setInclusive(attrValue);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case OPTION:
                                    if (includedOption > 0) {
                                        // inner case (multi level options)
                                        putOptionStack(currentOption);
                                        if (trace) {
                                            System.err.println("OPTION - included");
                                        }
                                    } else if (trace) {
                                        System.err.println("OPTION - not included");
                                    }

                                    includedOption++;

                                    currentOption = new SavotOption();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(NAME)) {
                                                    currentOption.setName(attrValue);
                                                } else if (attrName.equalsIgnoreCase(VALUE)) {
                                                    currentOption.setValue(attrValue);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case GROUP:
                                    // new since VOTable 1.1
                                    stats.iGroupsGlobalInc();

                                    if (includedGroup > 0) {
                                        // inner case (multi level groups)
                                        putGroupStack(currentGroup);
                                        if (trace) {
                                            System.err.println("GROUP - included");
                                        }
                                    } else if (trace) {
                                        System.err.println("GROUP - not included");
                                    }
                                    includedGroup++;

                                    currentGroup = new SavotGroup();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(REF)) {
                                                    currentGroup.setRef(attrValue);
                                                } else if (attrName.equalsIgnoreCase(NAME)) {
                                                    currentGroup.setName(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UCD)) {
                                                    currentGroup.setUcd(attrValue);
                                                } else if (attrName.equalsIgnoreCase(UTYPE)) {
                                                    currentGroup.setUtype(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentGroup.setId(attrValue);
                                                    idRefLinks.put(attrValue, currentGroup);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case COOSYS:
                                    // deprecated since VOTable 1.2
                                    currentCoosys = new SavotCoosys();

                                    counter = parser.getAttributeCount();
                                    if (counter != 0) {
                                        for (i = 0; i < counter; i++) {
                                            attrValue = parser.getAttributeValue(i);
                                            if (attrValue.length() != 0) {
                                                attrName = parser.getAttributeLocalName(i);
                                                if (attrName.equalsIgnoreCase(EQUINOX)) {
                                                    currentCoosys.setEquinox(attrValue);
                                                } else if (attrName.equalsIgnoreCase(EPOCH)) {
                                                    currentCoosys.setEpoch(attrValue);
                                                } else if (attrName.equalsIgnoreCase(SYSTEM)) {
                                                    currentCoosys.setSystem(attrValue);
                                                } else if (attrName.equalsIgnoreCase(ID)) {
                                                    currentCoosys.setId(attrValue);
                                                    idRefLinks.put(attrValue, currentCoosys);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case DEFINITIONS:
                                    currentDefinitions = new SavotDefinitions();
                                    break;

                                default:
                                    System.err.println("VOTable markup error " + operation + " : " + tag + " at line " + lineNumber);
                            }
                            // Anyway define currentMarkup:
                            currentMarkup = tag;
                        }
                        break;

                    // if an end tag is reach
                    case XMLEvent.END_ELEMENT:
                        operation = "END_ELEMENT";

                        // the name of the current tag
                        name = parser.getLocalName();

                        if (name != null) {
                            if (trace) {
                                System.err.println("End ---> " + name);
                            }

                            tag = VOTableTag.parseTag(name);

                            if (trace) {
                                System.err.println(tag + " end");
                            }

                            // use most probable tags FIRST (performance) i.e TD / TR first :
                            switch (tag) {
                                case TD:
                                    stats.iTDInc();

                                    // reduce SavotTD instances:
                                    tdSet.addItem((currentTD == null) ? emptyTD : currentTD);
                                    break;

                                case TR:
                                    stats.iTRInc();
                                    stats.iTDLocalReset();

                                    // trim TDset (reduce memory footprint):
                                    tdSet.trim();

                                    if (parsingType != ROWREAD) {
                                        // add the row to the table
                                        currentTableData.getTRs().addItem(currentTR);
                                        currentTR = null; // used so do not recycle it
                                    } else {
                                        // TR will be used without storage in the model
                                        TRComplete = true;
                                        // update reference:
                                        _currentTR = currentTR;
                                    }

                                    if (doStats) {
                                        // for statistics only
                                        rowCount++;
                                    }

                                    if (trace) {
                                        System.err.println("ADD row");
                                    }
                                    break;

                                case DESCRIPTION:
                                    // DESCRIPTION - several fathers are possible
                                    switch (lastFather()) {
                                        case VOTABLE:
                                            _currentVOTable.setDescription(currentDescription);
                                            break;
                                        case RESOURCE:
                                            _currentResource.setDescription(currentDescription);
                                            break;
                                        case PARAM:
                                            currentParam.setDescription(currentDescription);
                                            break;
                                        case TABLE:
                                            currentTable.setDescription(currentDescription);
                                            break;
                                        case FIELD:
                                            currentField.setDescription(currentDescription);
                                            break;
                                        case GROUP:
                                            currentGroup.setDescription(currentDescription);
                                            break;

                                        default:
                                    }
                                    currentDescription = null;
                                    break;

                                case TABLE:
                                    // avoid duplicates:
                                    if (parsingType != ROWREAD) {
                                        _currentResource.getTables().addItem(currentTable);

                                        if (trace) {
                                            System.err.println("TABLE " + currentTable.getName() + " added");
                                        }
                                    }
                                    break;

                                case FIELD:
                                    if (trace) {
                                        System.err.println("FIELD from father = " + lastFather());
                                    }

                                    if (lastFather() == VOTableTag.TABLE) {
                                        currentTable.getFields().addItem(currentField);
                                        if (trace) {
                                            System.err.println("FIELD from TABLE father = " + father);
                                        }
                                    }
                                    break;

                                case FIELDREF:
                                    if (lastFather() == VOTableTag.GROUP) {
                                        currentGroup.getFieldsRef().addItem(currentFieldRef);
                                        if (trace) {
                                            System.err.println("FIELDRef from GROUP father = " + father);
                                        }
                                    }
                                    break;

                                case DATA:
                                    currentTable.setData(currentData);
                                    break;

                                case RESOURCE:
                                    if (includedResource > 1) {
                                        final SavotResource tmp = _currentResource;
                                        _currentResource = getResourceStack();
                                        _currentResource.getResources().addItem(tmp);
                                    } else {
                                        if (parsingType == FULL) {
                                            _currentVOTable.getResources().addItem(_currentResource);
                                        }
                                        if (trace) {
                                            System.err.println(">>>>>>>> RESOURCE COMPLETED");
                                        }
                                        resourceComplete = true;
                                    }
                                    includedResource--;
                                    break;

                                case OPTION:
                                    if (includedOption > 1) {
                                        final SavotOption tmp = currentOption;
                                        currentOption = getOptionStack();
                                        currentOption.getOptions().addItem(tmp);
                                        includedOption--;
                                    } else {
                                        if (lastFather() == VOTableTag.VALUES) {
                                            currentValues.getOptions().addItem(currentOption);
                                            if (trace) {
                                                System.err.println("OPTION from VALUES father = " + father);
                                            }
                                            includedOption--;
                                        }
                                    }
                                    break;

                                case GROUP:
                                    if (includedGroup > 1) {
                                        final SavotGroup tmp = currentGroup;
                                        currentGroup = getGroupStack();
                                        currentGroup.getGroups().addItem(tmp);
                                        includedGroup--;
                                    } else {
                                        if (lastFather() == VOTableTag.TABLE) {
                                            currentTable.getGroups().addItem(currentGroup);
                                            if (trace) {
                                                System.err.println("GROUP from TABLE father = " + father);
                                            }
                                            includedGroup--;
                                        }
                                    }
                                    break;

                                case TABLEDATA:
                                    stats.iTRLocalReset();

                                    // trim TRset (reduce memory footprint):
                                    currentTableData.getTRs().trim();

                                    currentData.setTableData(currentTableData);
                                    if (trace) {
                                        System.err.println("TABLEDATA " + currentTable.getName() + " added");
                                    }
                                    break;

                                case COOSYS:
                                    // COOSYS - several fathers are possible
                                    switch (lastFather()) {
                                        case DEFINITIONS:
                                            // deprecated since VOTable 1.1
                                            currentDefinitions.getCoosys().addItem(currentCoosys);
                                            if (trace) {
                                                System.err.println("COOSYS from DEFINITIONS father = " + father);
                                            }
                                            break;
                                        case RESOURCE:
                                            _currentResource.getCoosys().addItem(currentCoosys);
                                            if (trace) {
                                                System.err.println("COOSYS from RESOURCE father = " + father);
                                            }
                                            break;
                                        case VOTABLE:
                                            _currentVOTable.getCoosys().addItem(currentCoosys);
                                            if (trace) {
                                                System.err.println("COOSYS from VOTABLE father = " + father);
                                            }
                                            break;
                                        default:
                                    }
                                    break;

                                case PARAM:
                                    // PARAM - several fathers are possible
                                    switch (lastFather()) {
                                        case DEFINITIONS:
                                            // deprecated since VOTable 1.1
                                            currentDefinitions.getParams().addItem(currentParam);
                                            if (trace) {
                                                System.err.println("PARAM from DEFINITIONS father = " + father);
                                            }
                                            break;
                                        case RESOURCE:
                                            _currentResource.getParams().addItem(currentParam);
                                            if (trace) {
                                                System.err.println("PARAM from RESOURCE father = " + father);
                                            }
                                            break;
                                        case TABLE:
                                            currentTable.getParams().addItem(currentParam);
                                            if (trace) {
                                                System.err.println("PARAM from TABLE father = " + father);
                                            }
                                            break;
                                        case GROUP:
                                            currentGroup.getParams().addItem(currentParam);
                                            if (trace) {
                                                System.err.println("PARAM from GROUP father = " + father);
                                            }
                                            break;
                                        case VOTABLE:
                                            _currentVOTable.getParams().addItem(currentParam);
                                            if (trace) {
                                                System.err.println("PARAM from VOTABLE father = " + father);
                                            }
                                            break;
                                        default:
                                    }
                                    break;

                                case PARAMREF:
                                    if (lastFather() == VOTableTag.GROUP) {
                                        currentGroup.getParamsRef().addItem(currentParamRef);
                                        if (trace) {
                                            System.err.println("PARAMRef from GROUP father = " + father);
                                        }
                                    }
                                    break;

                                case LINK:
                                    // LINK - several fathers are possible
                                    switch (lastFather()) {
                                        case RESOURCE:
                                            _currentResource.getLinks().addItem(currentLink);
                                            if (trace) {
                                                System.err.println("LINK from RESOURCE father = " + father);
                                            }
                                            break;
                                        case TABLE:
                                            currentTable.getLinks().addItem(currentLink);
                                            if (trace) {
                                                System.err.println("LINK from TABLE father = " + father);
                                            }
                                            break;
                                        case FIELD:
                                            currentField.getLinks().addItem(currentLink);
                                            if (trace) {
                                                System.err.println("LINK from FIELD father = " + father);
                                            }
                                            break;
                                        case PARAM:
                                            currentParam.getLinks().addItem(currentLink);
                                            if (trace) {
                                                System.err.println("LINK from PARAM father = " + father);
                                            }
                                            break;
                                        default:
                                    }
                                    break;

                                case VALUES:
                                    // VALUES - several fathers are possible
                                    switch (lastFather()) {
                                        case PARAM:
                                            currentParam.setValues(currentValues);
                                            if (trace) {
                                                System.err.println("VALUES from PARAM father = " + father + " ID : " + currentValues.getId());
                                            }
                                            break;
                                        case FIELD:
                                            currentField.setValues(currentValues);
                                            if (trace) {
                                                System.err.println("VALUES from FIELD father = " + father + " ID : " + currentValues.getId());
                                            }
                                            break;
                                        default:
                                    }
                                    break;

                                case MIN:
                                    currentValues.setMin(currentMin);
                                    break;

                                case MAX:
                                    currentValues.setMax(currentMax);
                                    break;

                                case STREAM:
                                    // STREAM - several fathers are possible
                                    switch (lastFather()) {
                                        case BINARY:
                                            currentBinary.setStream(currentStream);
                                            if (trace) {
                                                System.err.println("STREAM from BINARY father = " + father);
                                            }
                                            break;
                                        case BINARY2:
                                            currentBinary2.setStream(currentStream);
                                            if (trace) {
                                                System.err.println("STREAM from BINARY2 father = " + father);
                                            }
                                            break;
                                        case FITS:
                                            currentFits.setStream(currentStream);
                                            if (trace) {
                                                System.err.println("STREAM from FITS father = " + father);
                                            }
                                            break;
                                        default:
                                    }
                                    break;

                                case BINARY:
                                    currentData.setBinary(currentBinary);
                                    break;

                                case BINARY2:
                                    currentData.setBinary2(currentBinary2);
                                    break;

                                case FITS:
                                    currentData.setFits(currentFits);
                                    break;

                                case INFO:
                                    if (trace) {
                                        System.err.println("INFO father = " + father);
                                    }

                                    // INFO - several fathers are possible
                                    switch (lastFather()) {
                                        case VOTABLE:
                                            // since VOTable 1.2 - if RESOURCE then INFO at the end
                                            if (_currentVOTable.getResources() != null && _currentVOTable.getResources().getItemCount() != 0) {
                                                _currentVOTable.getInfosAtEnd().addItem(currentInfo);
                                            } else {
                                                _currentVOTable.getInfos().addItem(currentInfo);
                                            }
                                            if (trace) {
                                                System.err.println("INFO from VOTABLE father = " + father);
                                            }
                                            break;
                                        case RESOURCE:
                                            // since VOTable 1.2 - if RESOURCE or LINK or TABLE then INFO at the end
                                            if ((_currentResource.getResources() != null && _currentResource.getResources().getItemCount() != 0)
                                                    || (_currentResource.getTables() != null && _currentResource.getTables().getItemCount() != 0)
                                                    || (_currentResource.getLinks() != null && _currentResource.getLinks().getItemCount() != 0)) {
                                                _currentResource.getInfosAtEnd().addItem(currentInfo);
                                            } else {
                                                _currentResource.getInfos().addItem(currentInfo);
                                            }
                                            if (trace) {
                                                System.err.println("INFO from RESOURCE father = " + father);
                                            }
                                            break;
                                        case TABLE:
                                            // since VOTable 1.2
                                            currentTable.getInfosAtEnd().addItem(currentInfo);
                                            if (trace) {
                                                System.err.println("INFO from TABLE father = " + father);
                                            }
                                            break;

                                        default:
                                    }
                                    break;

                                case DEFINITIONS:
                                    // deprecated since VOTable 1.1
                                    _currentVOTable.setDefinitions(currentDefinitions);
                                    break;

                                case VOTABLE:
                                    break;

                                default:
                                    System.err.println("VOTable markup error " + operation + " : " + tag + " at line " + lineNumber);
                            }
                            // Anyway reset currentMarkup:
                            currentMarkup = VOTableTag.UNDEFINED;
                        }
                        break;

                    case XMLEvent.CHARACTERS:
                        operation = "CHARACTERS";

                        // avoid parser creating empty String
                        if (!parser.isWhiteSpace()) {
                            // Get new String from parser:
                            textValue = parser.getText();
                            if (doTrimValues) {
                                textValue = textValue.trim();
                            }

                            // only store not empty content:
                            if (textValue.length() != 0) {
                                if (trace) {
                                    System.err.println(currentMarkup + " : " + textValue);
                                }

                                // use most probable tags FIRST (performance) i.e TD / TR first :
                                switch (currentMarkup) {
                                    case TD:
                                        // create a new data
                                        currentTD = new SavotTD(textValue);
                                        break;

                                    case DESCRIPTION:
                                        currentDescription = textValue;
                                        break;

                                    case INFO:
                                        currentInfo.setContent(textValue);
                                        break;

                                    case LINK:
                                        currentLink.setContent(textValue);
                                        break;

                                    case COOSYS:
                                        currentCoosys.setContent(textValue);
                                        break;

                                    case MIN:
                                        currentMin.setContent(textValue);
                                        break;

                                    case MAX:
                                        currentMax.setContent(textValue);
                                        break;

                                    case STREAM:
                                        currentStream.setContent(textValue);
                                        break;

                                    default:
                                }
                            }
                        }
                        break;

                    case XMLEvent.START_DOCUMENT:
                        operation = "START_DOCUMENT";

                        if (trace) {
                            System.err.println("Document start.");
                        }
                        break;

                    default:
                        operation = "UNDEFINED";
                        if (trace) {
                            System.err.println("Ignoring some other (legacy) event at line : " + lineNumber);
                        }
                }

                // reset name and tag:
                tag = null;

                // new values from parser:
                eventType = parser.next();

                // treatment depending on event type
                switch (eventType) {

                    case XMLEvent.START_ELEMENT:
                        operation = "START_ELEMENT";

                        // the name of the current tag
                        name = parser.getLocalName();
                        if (trace) {
                            System.err.println("> FATHER, add : " + name);
                        }

                        tag = VOTableTag.parseTag(name);

                        father.add(tag);
                        break;

                    case XMLEvent.END_ELEMENT:
                        operation = "END_ELEMENT";

                        // the name of the current tag
                        name = parser.getLocalName();

                        if (name != null) {
                            if (trace) {
                                System.err.println("> FATHER, remove : " + name);
                            }

                            father.remove(father.size() - 1);

                        } else if (trace) {
                            // when a lf or cd is reached
                            System.err.println("> FATHER, case null");
                        }
                        break;

                    // if an end document is reached:
                    case XMLEvent.END_DOCUMENT:
                        operation = "END_DOCUMENT";

                        if (trace) {
                            System.err.println("Document end reached!");
                        }

                        // Anyway:
                        if (parsingType == ROWREAD) {
                            // terminate now (return TR null) to indicate that the complete document is done:
                            _currentTR = null;
                        }

                        // Close the stream anyway:
                        close();

                        break;

                    default:
                        // do nothing
                }

                if (TRComplete && (parsingType == ROWREAD)) {
                    eventType = XMLEvent.END_DOCUMENT;
                    if (trace) {
                        System.err.println(">>>>>>>>>>>>>>> ROWREAD case : TR end");
                    }
                } else if (resourceComplete && (parsingType == RESOURCEREAD)) {
                    eventType = XMLEvent.END_DOCUMENT;
                    if (trace) {
                        System.err.println(">>>>>>>>>>>>>>> RESOURCEREAD case : RESOURCE end");
                    }
                }
            }
        } catch (RuntimeException re) {
            logger.log(Level.SEVERE, "Exception " + operation + " - TAG (" + tag + ") : parse : " + re + " at line " + lineNumber, re);
            throw re;
        }

        if (doStats) {
            // update statistics:
            rowCounter = rowCount;
            dataCounter = dataCount;
        }
    }

    /**
     * Get the next Resource (warning : RESOURCEREAD mode only)
     *
     * @return a SavotResource (always NULL if other mode)
     */
    public SavotResource getNextResource() {
        _currentResource = null;
        try {
            parse(RESOURCEREAD);
        } catch (Exception ioe) {
            logger.log(Level.SEVERE, "Exception getNextResource : ", ioe);
            _currentResource = null;
        }
        return _currentResource;
    }

    /**
     * Get the next TR (warning : ROWREAD mode only)
     *
     * @return a SavotTR (always NULL if other mode)
     */
    public SavotTR getNextTR() {
        // note: currentTR is not null as it can be recycled
        try {
            parse(ROWREAD);
        } catch (Exception ioe) {
            logger.log(Level.SEVERE, "Exception getNextTR : ", ioe);
            _currentTR = null;
        }
        return _currentTR;
    }

    /**
     * Get a reference to V0TABLE object
     *
     * @return SavotVOTable
     */
    public SavotVOTable getVOTable() {
        return _currentVOTable;
    }

    /**
     * Get the number of RESOURCE elements in the document (for statistics)
     *
     * @return a long value
     */
    public int getResourceCount() {
        return resourceCounter;
    }

    /**
     * Get the number of TABLE elements in the document (for statistics)
     *
     * @return a long value
     */
    public int getTableCount() {
        return tableCounter;
    }

    /**
     * Get the number of TR elements in the document (for statistics)
     *
     * @return a long value
     */
    public int getTRCount() {
        return rowCounter;
    }

    /**
     * Get the number of DATA elements in the document (for statistics)
     *
     * @return a long value
     */
    public int getDataCount() {
        return dataCounter;
    }

    /**
     * Get a reference on the Hashtable containing the link between ID and ref
     *
     * @return a refernce to the Hashtable
     */
    public Map<String, Object> getIdRefLinks() {
        return idRefLinks;
    }

    /**
     * Search a RESOURCE corresponding to an ID ref
     *
     * @param ref
     * @return a reference to a SavotResource object
     */
    public SavotResource getResourceFromRef(final String ref) {
        return (SavotResource) idRefLinks.get(ref);
    }

    /**
     * Search a FIELD corresponding to an ID ref
     *
     * @param ref
     * @return SavotField
     */
    public SavotField getFieldFromRef(final String ref) {
        return (SavotField) idRefLinks.get(ref);
    }

    /**
     * Search a FIELDref corresponding to an ID ref
     *
     * @param ref
     * @return SavotFieldRef
     */
    public SavotFieldRef getFieldRefFromRef(final String ref) {
        return (SavotFieldRef) idRefLinks.get(ref);
    }

    /**
     * Search a PARAM corresponding to an ID ref
     *
     * @param ref
     * @return SavotParam
     */
    public SavotParam getParamFromRef(final String ref) {
        return (SavotParam) idRefLinks.get(ref);
    }

    /**
     * Search a PARAMref corresponding to an ID ref
     *
     * @param ref
     * @return SavotParamRef
     */
    public SavotParamRef getParamRefFromRef(final String ref) {
        return (SavotParamRef) idRefLinks.get(ref);
    }

    /**
     * Search a TABLE corresponding to an ID ref
     *
     * @param ref
     * @return SavotTable
     */
    public SavotTable getTableFromRef(final String ref) {
        return (SavotTable) idRefLinks.get(ref);
    }

    /**
     * Search a GROUP corresponding to an ID ref
     *
     * @param ref
     * @return SavotGROUP
     */
    public SavotGroup getGroupFromRef(final String ref) {
        return (SavotGroup) idRefLinks.get(ref);
    }

    /**
     * Search a RESOURCE corresponding to an ID ref
     *
     * @param ref
     * @return SavotInfo
     */
    public SavotInfo getInfoFromRef(final String ref) {
        return (SavotInfo) idRefLinks.get(ref);
    }

    /**
     * Search a VALUES corresponding to an ID ref
     *
     * @param ref
     * @return SavotValues
     */
    public SavotValues getValuesFromRef(final String ref) {
        return (SavotValues) idRefLinks.get(ref);
    }

    /**
     * Search a LINK corresponding to an ID ref
     *
     * @param ref
     * @return SavotLink
     */
    public SavotLink getLinkFromRef(final String ref) {
        return (SavotLink) idRefLinks.get(ref);
    }

    /**
     * Search a COOSYS corresponding to an ID ref
     *
     * @param ref
     * @return SavotCoosys
     */
    public SavotCoosys getCoosysFromRef(final String ref) {
        return (SavotCoosys) idRefLinks.get(ref);
    }

    /**
     * Get current VOTable (all resources)
     *
     * @return SavotVOTable
     */
    public SavotVOTable getAllResources() {
        return _currentVOTable;
    }

    /**
     * Enable debug mode
     *
     * @param debug boolean
     */
    public void enableDebug(boolean debug) {
        debugMode = debug;
    }

    /**
     * Returns the statistics
     *
     * @return statistics
     */
    public SavotStatistics getStatistics() {
        return statistics;
    }

    /**
     * Get Parser Version
     *
     * @return String
     */
    public String getVersion() {
        return SavotStaxParser.SAVOTPARSER;
    }

    /**
     * For test only
     */
    public void sequentialTester() {
        SavotResource currentResource;
        do {
            currentResource = getNextResource();
        } while (currentResource != null);
    }

    /**
     * Main
     *
     * @param argv
     * @throws Exception
     */
    public static void main(String[] argv) throws Exception {
        if (argv.length == 0) {
            System.out.println("Usage: java SavotPullParser document");
        } else {
            new SavotStaxParser(argv[0], SavotStaxParser.FULL);
        }
    }
}
