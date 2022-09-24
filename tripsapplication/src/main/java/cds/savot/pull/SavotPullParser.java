package cds.savot.pull;

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

import cds.savot.common.SavotStatistics;
import cds.savot.model.*;
import org.kxml2.io.KXmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

/**
 * <p>
 * It has been tested with kXML Pull parser implementation
 * </p>
 * <p>
 * but it is possible to use other pull parsers
 * </p>
 * <p>
 * Designed to use with Pull parsers complient with Standard Pull Implementation
 * v1
 * </p>
 * 
 * @author Andre Schaaff
 */
@SuppressWarnings({"deprecation", "UseOfSystemOutOrSystemErr"})
public final class SavotPullParser {

    /** the pull parser engine */
    private final SavotPullEngine engine;
    /** statistics dedicated to this parser */
    private final SavotStatistics stats = new SavotStatistics();

    /**
     * Constructor
     * 
     * @param file
     *            a file to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     */
    public SavotPullParser(final String file, final int mode) {
        this(file, mode, false);
    }

    /**
     * Constructor
     * 
     * @param file
     *            a file to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     * @param debug
     */
    public SavotPullParser(final String file, final int mode, final boolean debug) {
        // new parser
        this.engine = new SavotPullEngine(new KXmlParser(), file, mode, debug, stats);
    }

    /**
     * Constructor
     * 
     * @param url
     *            url to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     * @param enc
     *            encoding (example : UTF-8)
     */
    public SavotPullParser(final URL url, final int mode, final String enc) {
        this(url, mode, enc, false);
    }

    /**
     * Constructor
     * 
     * @param url
     *            url to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     * @param enc
     *            encoding (example : UTF-8)
     * @param debug
     */
    public SavotPullParser(final URL url, final int mode, final String enc, final boolean debug) {
        // new parser
        this.engine = new SavotPullEngine(new KXmlParser(), url, mode, enc, debug, stats);
    }

    /**
     * Constructor
     * 
     * @param instream
     *            stream to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     * @param enc
     *            encoding (example : UTF-8)
     */
    public SavotPullParser(final InputStream instream, final int mode, final String enc) {
        this(instream, mode, enc, false);
    }

    /**
     * Constructor
     * 
     * @param instream
     *            stream to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     * @param enc
     *            encoding (example : UTF-8)
     * @param debug           
     */
    public SavotPullParser(final InputStream instream, final int mode, final String enc, final boolean debug) {
        // new parser
        this.engine = new SavotPullEngine(new KXmlParser(), instream, mode, enc, debug, stats);
    }

    /**
     * Constructor
     * 
     * @param reader
     *            reader to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     */
    public SavotPullParser(final Reader reader, final int mode) {
        this(reader, mode, false);
    }

    /**
     * Constructor
     * 
     * @param reader
     *            reader to parse
     * @param mode
     *            FULL or RESOURCEREAD/ROWREAD (for small memory size
     *            applications)
     * @param debug
     */
    public SavotPullParser(final Reader reader, final int mode, final boolean debug) {
        // new parser
        this.engine = new SavotPullEngine(new KXmlParser(), reader, mode, debug, stats);
    }

    /**
     * Close the input stream if still opened
     */
    public void close() {
        engine.close();
    }

    /**
     * Returns the stats
     * 
     * @return statistics
     */
    public SavotStatistics getStatistics() {
        return stats;
    }

    /**
     * Get the next Resource (sequential mode only)
     * 
     * @return a SavotResource
     */
    public SavotResource getNextResource() {
        return engine.getNextResource();
    }

    /**
     * Get the next Resource (sequential mode only)
     * 
     * @return a SavotResource
     */
    public SavotTR getNextTR() {
        return engine.getNextTR();
    }

    /**
     * Get a reference to V0TABLE object
     * 
     * @return SavotVOTable
     */
    public SavotVOTable getVOTable() {
        return engine.getAllResources();
    }

    /**
     * Get the number of RESOURCE elements in the document (for statistics)
     * 
     * @return an int value
     */
    public int getResourceCount() {
        return engine.getResourceCount();
    }

    /**
     * Get the number of TABLE elements in the document (for statistics)
     * 
     * @return an int value
     */
    public int getTableCount() {
        return engine.getTableCount();
    }

    /**
     * Get the number of TR elements in the document (for statistics)
     * 
     * @return an int value
     */
    public int getTRCount() {
        return engine.getTRCount();
    }

    /**
     * Get the number of DATA elements in the document (for statistics)
     * 
     * @return an int value
     */
    public int getDataCount() {
        return engine.getDataCount();
    }

    /**
     * Get a reference on the Hashtable containing the link between ID and ref
     * 
     * @return a refernce to the Hashtable
     */
    public Map<String, Object> getIdRefLinks() {
        return engine.getIdRefLinks();
    }

    /**
     * Search a RESOURCE corresponding to an ID ref
     * 
     * @param ref
     * @return a reference to a SavotResource object
     */
    public SavotResource getResourceFromRef(final String ref) {
        return engine.getResourceFromRef(ref);
    }

    /**
     * Search a FIELD corresponding to an ID ref
     * 
     * @param ref
     * @return SavotField
     */
    public SavotField getFieldFromRef(final String ref) {
        return engine.getFieldFromRef(ref);
    }

    /**
     * Search a PARAM corresponding to an ID ref
     * 
     * @param ref
     * @return SavotParam
     */
    public SavotParam getParamFromRef(final String ref) {
        return engine.getParamFromRef(ref);
    }

    /**
     * Search a TABLE corresponding to an ID ref
     * 
     * @param ref
     * @return SavotTable
     */
    public SavotTable getTableFromRef(final String ref) {
        return engine.getTableFromRef(ref);
    }

    /**
     * Search a RESOURCE corresponding to an ID ref
     * 
     * @param ref
     * @return SavotInfo
     */
    public SavotInfo getInfoFromRef(final String ref) {
        return engine.getInfoFromRef(ref);
    }

    /**
     * Search a VALUES corresponding to an ID ref
     * 
     * @param ref
     * @return SavotValues
     */
    public SavotValues getValuesFromRef(final String ref) {
        return engine.getValuesFromRef(ref);
    }

    /**
     * Search a LINK corresponding to an ID ref
     * 
     * @param ref
     * @return SavotLink
     */
    public SavotLink getLinkFromRef(final String ref) {
        return engine.getLinkFromRef(ref);
    }

    /**
     * Search a COOSYS corresponding to an ID ref
     * 
     * @param ref
     * @return SavotCoosys
     */
    public SavotCoosys getCoosysFromRef(final String ref) {
        return engine.getCoosysFromRef(ref);
    }

    /**
     * Get all resources
     * 
     * @return SavotVOTable
     */
    public SavotVOTable getAllResources() {
        return engine.getAllResources();
    }

    /**
     * Get Parser Version
     * 
     * @return String
     */
    public String getVersion() {
        return SavotPullEngine.SAVOTPARSER;
    }

    /**
     * Enable debug mode
     * 
     * @param debug
     *            boolean
     */
    public void enableDebug(final boolean debug) {
        engine.enableDebug(debug);
    }

    /**
     * For test only
     * 
     */
    public void sequentialTester() {
        SavotResource currentResource = null;
        do {
            currentResource = engine.getNextResource();
        } while (currentResource != null);
    }

    /**
     * Main
     * 
     * @param argv
     * @throws IOException
     */
    public static void main(String[] argv) throws IOException {
        if (argv.length == 0) {
            System.out.println("Usage: java SavotPullParser <xml document>");
        } else {
            new SavotPullParser(argv[0], SavotPullEngine.FULL);
        }
    }
}
