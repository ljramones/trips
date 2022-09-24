package cds.savot.sax;

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

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * <p>
 * It has been tested with kXML Pull parser implementation
 * </p>
 * 
 * @author Andre Schaaff
 */
@SuppressWarnings({"deprecation", "UseOfSystemOutOrSystemErr"})
public final class SavotSAXEngine implements cds.savot.common.Markups {

    // use for debug
    private boolean trace = false;

    // needed for sequential parsing
    private XmlPullParser parser = null;

    // SAVOT SAX consumer
    SavotSAXConsumer consumer;

    // father of a markup
    ArrayList<String> father = new ArrayList<String>();

    /**
     * 
     */
    public SavotSAXEngine() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            SavotSAXConsumer
     * @param parser
     *            XmlPullParser
     * @param file
     *            a file to parse
     * @param debug
     *            boolean
     */
    public SavotSAXEngine(SavotSAXConsumer consumer, XmlPullParser parser,
                          String file, boolean debug) {

        try {
            this.setParser(parser);
            this.consumer = consumer;
            enableDebug(debug);

            // set the input of the parser
            FileInputStream inStream = new FileInputStream(new File(file));
            BufferedInputStream dataBuffInStream = new BufferedInputStream(inStream);

            parser.setInput(dataBuffInStream, "UTF-8");

            // parse the stream
            parse(parser);

        } catch (Exception e) {
            System.err.println("Exception SavotSAXEngine : " + e);
        }
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            SavotSAXConsumer
     * @param parser
     *            XmlPullParser
     * @param url
     *            url to parse
     * @param enc
     *            encoding (example : UTF-8)
     * @param debug
     *            boolean
     */
    public SavotSAXEngine(SavotSAXConsumer consumer, XmlPullParser parser,
                          URL url, String enc, boolean debug) {

        try {
            this.setParser(parser);
            this.consumer = consumer;
            enableDebug(debug);
            // set the input of the parser (with the given encoding)
            parser.setInput(new DataInputStream(url.openStream()), enc);

            // parse the stream
            parse(parser);

        } catch (Exception e) {
            System.err.println("Exception SavotSAXEngine : " + e);
        }
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            SavotSAXConsumer
     * @param parser
     *            XmlPullParser
     * @param instream
     *            stream to parse
     * @param enc
     *            encoding (example : UTF-8)
     * @param debug
     *            boolean
     */
    public SavotSAXEngine(SavotSAXConsumer consumer, XmlPullParser parser,
                          InputStream instream, String enc, boolean debug) {
        // public SavotSAXEngine(XmlPullParser parser, InputStream instream, int
        // mode, String enc) {
        try {
            this.setParser(parser);
            this.consumer = consumer;
            enableDebug(debug);

            // DataInputStream dataInStream = new DataInputStream(instream);
            BufferedInputStream dataBuffInStream = new BufferedInputStream(instream);

            // set the input of the parser (with the given encoding)
            // parser.setInput(new DataInputStream(instream), enc);
            parser.setInput(dataBuffInStream, enc);

            // parser the stream
            parse(parser);

        } catch (Exception e) {
            System.err.println("Exception SavotSAXEngine : " + e);
        }
    }

    /**
     * Parsing engine
     * 
     * @param parser
     *            an XML pull parser (example : kXML)
     * 
     * @throws IOException
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    public void parse(XmlPullParser parser) throws IOException {

        String currentMarkup = "XML";

        try {

            // envent type
            int eventType = parser.getEventType();
            father.add((parser.getName()));

            // while the end of the document is not reach
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // treatment depending on event type
                switch (eventType) {
                    // if a start tag is reach
                    case KXmlParser.START_TAG:
                        try {
                            // the name of the current tag
                            currentMarkup = parser.getName();
                            father.add((parser.getName()));

                            // trace mode
                            if (trace) {
                                System.err.println("Name ---> " + parser.getName());
                            }
                            if (currentMarkup != null) {

                                // VOTABLE
                                if (currentMarkup.equalsIgnoreCase(VOTABLE)) {

                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, VERSION) != null) {
                                            attributes.add(VERSION);
                                            attributes.add(parser.getAttributeValue(null, VERSION));
                                        }
                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                        consumer.startVotable(attributes);
                                    }

                                    // trace mode
                                    if (trace) {
                                        System.err.println("VOTABLE begin");
                                    }
                                } else // DESCRIPTION
                                if (currentMarkup.equalsIgnoreCase(DESCRIPTION)) {
                                    consumer.startDescription();

                                    // trace mode
                                    if (trace) {
                                        System.err.println("DESCRIPTION begin");
                                    }
                                } // RESOURCE
                                else if (currentMarkup.equalsIgnoreCase(RESOURCE)) {
                                    ArrayList attributes = new ArrayList();

                                    if (parser.getAttributeCount() != 0) {

                                        if (parser.getAttributeValue(null, NAME) != null) {
                                            attributes.add(NAME);
                                            attributes.add(parser.getAttributeValue(null, NAME));
                                        }

                                        if (parser.getAttributeValue(null, TYPE) != null) {
                                            attributes.add(TYPE);
                                            attributes.add(parser.getAttributeValue(null, TYPE));
                                        }

                                        // new since VOTable 1.1
                                        if (parser.getAttributeValue(null, UTYPE) != null) {
                                            attributes.add(UTYPE);
                                            attributes.add(parser.getAttributeValue(null, UTYPE));
                                        }

                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                    }
                                    consumer.startResource(attributes);
                                    // trace mode
                                    if (trace) {
                                        System.err.println("RESOURCE");
                                    }
                                } // TABLE
                                else if (currentMarkup.equalsIgnoreCase(TABLE)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {

                                        if (parser.getAttributeValue(null, NAME) != null) {
                                            attributes.add(NAME);
                                            attributes.add(parser.getAttributeValue(null, NAME));
                                        }

                                        // new since VOTable 1.1
                                        if (parser.getAttributeValue(null, UCD) != null) {
                                            attributes.add(UCD);
                                            attributes.add(parser.getAttributeValue(null, UCD));
                                        }

                                        // new since VOTable 1.1
                                        if (parser.getAttributeValue(null, UTYPE) != null) {
                                            attributes.add(UTYPE);
                                            attributes.add(parser.getAttributeValue(null, UTYPE));
                                        }

                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                            attributes.add(parser.getAttributeValue(null, REF));
                                        }

                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }

                                        // new since VOTable 1.1
                                        if (parser.getAttributeValue(null, NROWS) != null) {
                                            attributes.add(NROWS);
                                            attributes.add(parser.getAttributeValue(null, NROWS));
                                        }
                                    }
                                    consumer.startTable(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("TABLE begin");
                                    }
                                } // FIELD
                                else if (currentMarkup.equalsIgnoreCase(FIELD)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {

                                        if (parser.getAttributeValue(null, UNIT) != null) {
                                            attributes.add(UNIT);
                                            attributes.add(parser.getAttributeValue(null, UNIT));
                                        }

                                        if (parser.getAttributeValue(null, DATATYPE) != null) {
                                            attributes.add(DATATYPE);
                                            attributes.add(parser.getAttributeValue(null, DATATYPE));
                                        }

                                        if (parser.getAttributeValue(null,
                                                PRECISION) != null) {
                                            attributes.add(PRECISION);
                                            attributes.add(parser.getAttributeValue(null, PRECISION));
                                        }

                                        if (parser.getAttributeValue(null, WIDTH) != null) {
                                            attributes.add(WIDTH);
                                            attributes.add(parser.getAttributeValue(null, WIDTH));
                                        }

                                        // since VOTable 1.2
                                        if (parser.getAttributeValue(null, XTYPE) != null) {
                                            attributes.add(XTYPE);
                                            attributes.add(parser.getAttributeValue(null, XTYPE));
                                        }

                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                            attributes.add(parser.getAttributeValue(null, REF));
                                        }

                                        if (parser.getAttributeValue(null, NAME) != null) {
                                            attributes.add(NAME);
                                            attributes.add(parser.getAttributeValue(null, NAME));
                                        }

                                        if (parser.getAttributeValue(null, UCD) != null) {
                                            attributes.add(UCD);
                                            attributes.add(parser.getAttributeValue(null, UCD));
                                        }

                                        if (parser.getAttributeValue(null,
                                                ARRAYSIZE) != null) {
                                            attributes.add(ARRAYSIZE);
                                            attributes.add(parser.getAttributeValue(null, ARRAYSIZE));
                                        }

                                        if (parser.getAttributeValue(null, TYPE) != null) { // deprecated
                                            // since
                                            // VOTable
                                            // 1.1
                                            attributes.add(TYPE);
                                            attributes.add(parser.getAttributeValue(null, TYPE));
                                        }

                                        if (parser.getAttributeValue(null, UTYPE) != null) {
                                            attributes.add(UTYPE);
                                            attributes.add(parser.getAttributeValue(null, UTYPE));
                                        }

                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                    }
                                    consumer.startField(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("FIELD begin");
                                    }
                                } // FIELDREF
                                else if (currentMarkup.equalsIgnoreCase(FIELDREF)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                        }
                                        attributes.add(parser.getAttributeValue(null, REF));
                                    }
                                    consumer.startFieldref(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("FIELDREF begin");
                                    }
                                } // VALUES
                                else if (currentMarkup.equalsIgnoreCase(VALUES)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, TYPE) != null) {
                                            attributes.add(TYPE);
                                            attributes.add(parser.getAttributeValue(null, TYPE));
                                        }

                                        if (parser.getAttributeValue(null, NULL) != null) {
                                            attributes.add(NULL);
                                            attributes.add(parser.getAttributeValue(null, NULL));
                                        }

                                        if (parser.getAttributeValue(null, INVALID) != null) { // deprecated
                                            // since
                                            // VOTable
                                            // 1.1
                                            attributes.add(INVALID);
                                            attributes.add(parser.getAttributeValue(null, INVALID));
                                        }

                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                            attributes.add(parser.getAttributeValue(null, REF));
                                        }

                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                    }
                                    consumer.startValues(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("VALUES begin");
                                    }
                                } // STREAM
                                else if (currentMarkup.equalsIgnoreCase(STREAM)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {

                                        if (parser.getAttributeValue(null, TYPE) != null) {
                                            attributes.add(TYPE);
                                            attributes.add(parser.getAttributeValue(null, TYPE));
                                        }
                                        if (parser.getAttributeValue(null, HREF) != null) {
                                            attributes.add(HREF);
                                            attributes.add(parser.getAttributeValue(null, HREF));
                                        }
                                        if (parser.getAttributeValue(null, ACTUATE) != null) {
                                            attributes.add(ACTUATE);
                                            attributes.add(parser.getAttributeValue(null, ACTUATE));
                                        }
                                        if (parser
                                                .getAttributeValue(null, ENCODING) != null) {
                                            attributes.add(ENCODING);
                                            attributes.add(parser.getAttributeValue(null, ENCODING));
                                        }
                                        if (parser.getAttributeValue(null, EXPIRES) != null) {
                                            attributes.add(EXPIRES);
                                            attributes.add(parser.getAttributeValue(null, EXPIRES));
                                        }
                                        if (parser.getAttributeValue(null, RIGHTS) != null) {
                                            attributes.add(RIGHTS);
                                            attributes.add(parser.getAttributeValue(null, RIGHTS));
                                        }
                                    }
                                    consumer.startStream(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("STREAM begin");
                                    }
                                } // TR
                                else if (currentMarkup.equalsIgnoreCase(TR)) {
                                    consumer.startTR();

                                    // trace mode
                                    if (trace) {
                                        System.err.println("TR begin");
                                    }
                                } // TD
                                else if (currentMarkup.equalsIgnoreCase(TD)) {
                                    ArrayList attributes = new ArrayList();

                                    if (parser.getAttributeCount() != 0) {
                                        if (parser
                                                .getAttributeValue(null, ENCODING) != null) {
                                            attributes.add(ENCODING);
                                            attributes.add(parser
                                                    .getAttributeValue(null,
                                                            ENCODING));
                                        }
                                    }
                                    consumer.startTD(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("TD begin");
                                    }
                                } // DATA
                                else if (currentMarkup.equalsIgnoreCase(DATA)) {
                                    consumer.startData();

                                    // trace mode
                                    if (trace) {
                                        System.err.println("DATA begin");
                                    }
                                } // BINARY
                                else if (currentMarkup.equalsIgnoreCase(BINARY)) {
                                    consumer.startBinary();

                                    // trace mode
                                    if (trace) {
                                        System.err.println("BINARY begin");
                                    }
                                } // FITS
                                else if (currentMarkup.equalsIgnoreCase(FITS)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, EXTNUM) != null) {
                                            attributes.add(EXTNUM);
                                            attributes.add(parser.getAttributeValue(null, EXTNUM));
                                        }
                                    }
                                    consumer.startFits(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("FITS begin");
                                    }
                                } // TABLEDATA
                                else if (currentMarkup.equalsIgnoreCase(TABLEDATA)) {
                                    consumer.startTableData();

                                    // trace mode
                                    if (trace) {
                                        System.err.println("TABLEDATA begin");
                                    }
                                } // PARAM
                                else if (currentMarkup.equalsIgnoreCase(PARAM)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, UNIT) != null) {
                                            attributes.add(UNIT);
                                            attributes.add(parser.getAttributeValue(null, UNIT));
                                        }
                                        if (parser
                                                .getAttributeValue(null, DATATYPE) != null) {
                                            attributes.add(DATATYPE);
                                            attributes.add(parser.getAttributeValue(null, DATATYPE));
                                        }
                                        if (parser.getAttributeValue(null,
                                                PRECISION) != null) {
                                            attributes.add(PRECISION);
                                            attributes.add(parser.getAttributeValue(null, PRECISION));
                                        }
                                        if (parser.getAttributeValue(null, WIDTH) != null) {
                                            attributes.add(WIDTH);
                                            attributes.add(parser.getAttributeValue(null, WIDTH));
                                        }
                                        // since VOTable 1.2
                                        if (parser.getAttributeValue(null, XTYPE) != null) {
                                            attributes.add(XTYPE);
                                            attributes.add(parser.getAttributeValue(null, XTYPE));
                                        }
                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                            attributes.add(parser.getAttributeValue(null, REF));
                                        }
                                        if (parser.getAttributeValue(null, NAME) != null) {
                                            attributes.add(NAME);
                                            attributes.add(parser.getAttributeValue(null, NAME));
                                        }
                                        if (parser.getAttributeValue(null, UCD) != null) {
                                            attributes.add(UCD);
                                            attributes.add(parser.getAttributeValue(null, UCD));
                                        }
                                        if (parser.getAttributeValue(null, UTYPE) != null) { // new
                                            // since
                                            // VOTable
                                            // 1.1
                                            attributes.add(UTYPE);
                                            attributes.add(parser.getAttributeValue(null, UTYPE));
                                        }
                                        if (parser.getAttributeValue(null, VALUE) != null) {
                                            attributes.add(VALUE);
                                            attributes.add(parser.getAttributeValue(null, VALUE));
                                        }
                                        if (parser.getAttributeValue(null,
                                                ARRAYSIZE) != null) {
                                            attributes.add(ARRAYSIZE);
                                            attributes.add(parser.getAttributeValue(null, ARRAYSIZE));
                                        }
                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                    }
                                    consumer.startParam(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("PARAM begin");
                                    }
                                } // PARAMREF
                                else if (currentMarkup.equalsIgnoreCase(PARAMREF)) { // new
                                    // since
                                    // VOTable
                                    // 1.1
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                            attributes.add(parser.getAttributeValue(null, REF));
                                        }
                                    }
                                    consumer.startParamRef(attributes);

                                    // trace mode
                                    if (trace) {
                                        System.err.println("PARAMref begin");
                                    }
                                } // LINK
                                else if (currentMarkup.equalsIgnoreCase(LINK)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null,
                                                CONTENTROLE) != null) {
                                            attributes.add(CONTENTROLE);
                                            attributes.add(parser.getAttributeValue(null, CONTENTROLE));
                                        }
                                        if (parser.getAttributeValue(null,
                                                CONTENTTYPE) != null) {
                                            attributes.add(CONTENTTYPE);
                                            attributes.add(parser.getAttributeValue(null, CONTENTTYPE));
                                        }
                                        if (parser.getAttributeValue(null, TITLE) != null) {
                                            attributes.add(TITLE);
                                            attributes.add(parser.getAttributeValue(null, TITLE));
                                        }
                                        if (parser.getAttributeValue(null, VALUE) != null) {
                                            attributes.add(VALUE);
                                            attributes.add(parser.getAttributeValue(null, VALUE));
                                        }
                                        if (parser.getAttributeValue(null, HREF) != null) {
                                            attributes.add(HREF);
                                            attributes.add(parser.getAttributeValue(null, HREF));
                                        }
                                        if (parser.getAttributeValue(null, GREF) != null) { // deprecated
                                            // since
                                            // VOTable
                                            // 1.1
                                            attributes.add(GREF);
                                            attributes.add(parser.getAttributeValue(null, GREF));
                                        }
                                        if (parser.getAttributeValue(null, ACTION) != null) {
                                            attributes.add(ACTION);
                                            attributes.add(parser.getAttributeValue(null, ACTION));
                                        }
                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                    }
                                    consumer.startLink(attributes);

                                    if (trace) {
                                        System.err.println("LINK begin");
                                    }
                                } // INFO
                                else if (currentMarkup.equalsIgnoreCase(INFO)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, NAME) != null) {
                                            attributes.add(NAME);
                                            attributes.add(parser.getAttributeValue(null, NAME));
                                        }
                                        if (parser.getAttributeValue(null, VALUE) != null) {
                                            attributes.add(VALUE);
                                            attributes.add(parser.getAttributeValue(null, VALUE));
                                        }
                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                        // since VOTable 1.2
                                        if (parser.getAttributeValue(null, XTYPE) != null) {
                                            attributes.add(XTYPE);
                                            attributes.add(parser.getAttributeValue(null, XTYPE));
                                        }
                                        // since VOTable 1.2
                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                            attributes.add(parser.getAttributeValue(null, REF));
                                        }
                                        // since VOTable 1.2
                                        if (parser.getAttributeValue(null, UNIT) != null) {
                                            attributes.add(UNIT);
                                            attributes.add(parser.getAttributeValue(null, UNIT));
                                        }
                                        // since VOTable 1.2
                                        if (parser.getAttributeValue(null, UCD) != null) {
                                            attributes.add(UCD);
                                            attributes.add(parser.getAttributeValue(null, UCD));
                                        }
                                        // since VOTable 1.2
                                        if (parser.getAttributeValue(null, UTYPE) != null) {
                                            attributes.add(UTYPE);
                                            attributes.add(parser.getAttributeValue(null, UTYPE));
                                        }
                                    }
                                    consumer.startInfo(attributes);

                                    if (trace) {
                                        System.err.println("INFO begin");
                                    }
                                } // MIN
                                else if (currentMarkup.equalsIgnoreCase(MIN)) {
                                    ArrayList attributes = new ArrayList();
                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, VALUE) != null) {
                                            attributes.add(VALUE);
                                            attributes.add(parser.getAttributeValue(null, VALUE));
                                        }
                                        if (parser.getAttributeValue(null, INCLUSIVE) != null) {
                                            attributes.add(INCLUSIVE);
                                            attributes.add(parser.getAttributeValue(null, INCLUSIVE));
                                        }
                                    }
                                    consumer.startMin(attributes);

                                    // mode trace
                                    if (trace) {
                                        System.err.println("MIN begin");
                                    }
                                } // MAX
                                else if (currentMarkup.equalsIgnoreCase(MAX)) {
                                    ArrayList attributes = new ArrayList();

                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, VALUE) != null) {
                                            attributes.add(VALUE);
                                            attributes.add(parser.getAttributeValue(null, VALUE));
                                        }
                                        if (parser.getAttributeValue(null,
                                                INCLUSIVE) != null) {
                                            attributes.add(INCLUSIVE);
                                            attributes.add(parser.getAttributeValue(null, INCLUSIVE));
                                        }
                                    }
                                    consumer.startMax(attributes);

                                    // mode trace
                                    if (trace) {
                                        System.err.println("MAX begin ");
                                    }
                                } // OPTION
                                else if (currentMarkup.equalsIgnoreCase(OPTION)) {
                                    ArrayList attributes = new ArrayList();

                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, NAME) != null) {
                                            attributes.add(NAME);
                                            attributes.add(parser.getAttributeValue(null, NAME));
                                        }
                                        if (parser.getAttributeValue(null, VALUE) != null) {
                                            attributes.add(VALUE);
                                            attributes.add(parser.getAttributeValue(null, VALUE));
                                        }
                                    }
                                    consumer.startOption(attributes);

                                    // mode trace
                                    if (trace) {
                                        System.err.println("OPTION begin - not included");
                                    }
                                } // GROUP new 1.1
                                else if (currentMarkup.equalsIgnoreCase(GROUP)) {
                                    ArrayList attributes = new ArrayList();

                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, REF) != null) {
                                            attributes.add(REF);
                                            attributes.add(parser.getAttributeValue(null, REF));
                                        }
                                        if (parser.getAttributeValue(null, NAME) != null) {
                                            attributes.add(NAME);
                                            attributes.add(parser.getAttributeValue(null, NAME));
                                        }
                                        if (parser.getAttributeValue(null, UCD) != null) {
                                            attributes.add(UCD);
                                            attributes.add(parser.getAttributeValue(null, UCD));
                                        }
                                        if (parser.getAttributeValue(null, UTYPE) != null) {
                                            attributes.add(UTYPE);
                                            attributes.add(parser.getAttributeValue(null, UTYPE));
                                        }
                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                    }
                                    consumer.startGroup(attributes);

                                    // mode trace
                                    if (trace) {
                                        System.err.println("GROUP begin - not included");
                                    }
                                } // COOSYS - deprecated since VOTable 1.2
                                else if (currentMarkup.equalsIgnoreCase(COOSYS)) {
                                    ArrayList attributes = new ArrayList();

                                    if (parser.getAttributeCount() != 0) {
                                        if (parser.getAttributeValue(null, EQUINOX) != null) {
                                            attributes.add(EQUINOX);
                                            attributes.add(parser.getAttributeValue(null, EQUINOX));
                                        }
                                        if (parser.getAttributeValue(null, EPOCH) != null) {
                                            attributes.add(EPOCH);
                                            attributes.add(parser.getAttributeValue(null, EPOCH));
                                        }
                                        if (parser.getAttributeValue(null, SYSTEM) != null) {
                                            attributes.add(SYSTEM);
                                            attributes.add(parser.getAttributeValue(null, SYSTEM));
                                        }
                                        if (parser.getAttributeValue(null, ID) != null) {
                                            attributes.add(ID);
                                            attributes.add(parser.getAttributeValue(null, ID));
                                        }
                                    }
                                    consumer.startCoosys(attributes);

                                    // mode trace
                                    if (trace) {
                                        System.err.println("COOSYS begin");
                                    }
                                } // DEFINITIONS - deprecated since VOTable 1.1
                                else if (currentMarkup.equalsIgnoreCase(DEFINITIONS)) {
                                    consumer.startDefinitions();

                                    // mode trace
                                    if (trace) {
                                        System.err.println("DEFINITIONS begin");
                                    }
                                } else {
                                    System.err.println("VOTable markup error : "
                                            + currentMarkup + " at line "
                                            + parser.getLineNumber());
                                }
                            } // currentMarkup = name;
                        } catch (Exception e) {
                            System.err.println("Exception START_TAG : " + e
                                    + " at line " + parser.getLineNumber());
                        }
                        break;

                    // if an end tag is reach
                    case KXmlParser.END_TAG:
                        try {
                            if (trace) {
                                System.err.println("End ---> " + currentMarkup);
                            }
                            // DESCRIPTION
                            if (currentMarkup.equalsIgnoreCase(DESCRIPTION)) {
                                consumer.endDescription();

                                // trace mode
                                if (trace) {
                                    System.err.println("DESCRIPTION");
                                }
                            } // TABLE
                            else if (currentMarkup.equalsIgnoreCase(TABLE)) {
                                consumer.endTable();

                                // trace mode
                                if (trace) {
                                    System.err.println("TABLE");
                                }
                            } // FIELD
                            else if (currentMarkup.equalsIgnoreCase(FIELD)) {
                                consumer.endField();

                                // trace mode
                                if (trace) {
                                    System.err.println("FIELD");
                                }
                            } // FIELDref
                            else if (currentMarkup.equalsIgnoreCase(FIELDREF)) {
                                consumer.endFieldref();

                                // trace mode
                                if (trace) {
                                    System.err.println("FIELDRef");
                                }
                            } // TR
                            else if (currentMarkup.equalsIgnoreCase(TR)) {
                                consumer.endTR();

                                // trace mode
                                if (trace) {
                                    System.err.println("TR");
                                }
                            } // DATA
                            else if (currentMarkup.equalsIgnoreCase(DATA)) {
                                consumer.endData();

                                // trace mode
                                if (trace) {
                                    System.err.println("DATA");
                                }
                            } // TD
                            else if (currentMarkup.equalsIgnoreCase(TD)) {
                                consumer.endTD();

                                // trace mode
                                if (trace) {
                                    System.err.println("TD");
                                }
                            } // RESOURCE
                            else if (currentMarkup.equalsIgnoreCase(RESOURCE)) {
                                consumer.endResource();

                                // trace mode
                                if (trace) {
                                    System.err.println("RESOURCE");
                                }
                            } // OPTION
                            else if (currentMarkup.equalsIgnoreCase(OPTION)) {
                                consumer.endOption();

                                // trace mode
                                if (trace) {
                                    System.err.println("OPTION");
                                }
                            } // GROUP
                            else if (currentMarkup.equalsIgnoreCase(GROUP)) {
                                consumer.endGroup();

                                // trace mode
                                if (trace) {
                                    System.err.println("GROUP");
                                }
                            } // TABLEDATA
                            else if (currentMarkup.equalsIgnoreCase(TABLEDATA)) {
                                consumer.endTableData();

                                // trace mode
                                if (trace) {
                                    System.err.println("TABLEDATA");
                                }
                            } // COOSYS - deprecated since VOTable 1.2
                            else if (currentMarkup.equalsIgnoreCase(COOSYS)) {
                                consumer.endCoosys();

                                // trace mode
                                if (trace) {
                                    System.err.println("COOSYS");
                                }
                            } // PARAM
                            else if (currentMarkup.equalsIgnoreCase(PARAM)) {
                                consumer.endParam();

                                // trace mode
                                if (trace) {
                                    System.err.println("PARAM");
                                }
                            } // PARAMREF
                            else if (currentMarkup.equalsIgnoreCase(PARAMREF)) {
                                consumer.endParamRef();

                                // trace mode
                                if (trace) {
                                    System.err.println("PARAMRef");
                                }
                            } // LINK
                            else if (currentMarkup.equalsIgnoreCase(LINK)) {
                                consumer.endLink();

                                // trace mode
                                if (trace) {
                                    System.err.println("LINK");
                                }
                            } // VALUES
                            else if (currentMarkup.equalsIgnoreCase(VALUES)) {
                                consumer.endValues();

                                // trace mode
                                if (trace) {
                                    System.err.println("VALUES");
                                }
                            } // MIN
                            else if (currentMarkup.equalsIgnoreCase(MIN)) {
                                consumer.endMin();

                                // trace mode
                                if (trace) {
                                    System.err.println("MIN");
                                }
                            } // MAX
                            else if (currentMarkup.equalsIgnoreCase(MAX)) {
                                consumer.endMax();

                                // trace mode
                                if (trace) {
                                    System.err.println("MAX");
                                }
                            } // STREAM
                            else if (currentMarkup.equalsIgnoreCase(STREAM)) {
                                consumer.endStream();

                                // trace mode
                                if (trace) {
                                    System.err.println("STREAM");
                                }
                            } // BINARY
                            else if (currentMarkup.equalsIgnoreCase(BINARY)) {
                                consumer.endBinary();

                                // trace mode
                                if (trace) {
                                    System.err.println("BINARY");
                                }
                            } // FITS
                            else if (currentMarkup.equalsIgnoreCase(FITS)) {
                                consumer.endFits();

                                // trace mode
                                if (trace) {
                                    System.err.println("FITS");
                                }
                            } // INFO
                            else if (currentMarkup.equalsIgnoreCase(INFO)) {
                                consumer.endInfo();

                                // trace mode
                                if (trace) {
                                    System.err.println("INFO");
                                }
                            } // DEFINITIONS - deprecated since VOTable 1.1
                            else if (currentMarkup.equalsIgnoreCase(DEFINITIONS)) {
                                consumer.endDefinitions();

                                // trace mode
                                if (trace) {
                                    System.err.println("DEFINITIONS");
                                }
                            } // VOTABLE
                            else if (currentMarkup.equalsIgnoreCase(VOTABLE)) {
                                consumer.endVotable();

                                // trace mode
                                if (trace) {
                                    System.err.println("VOTABLE");
                                }
                            } else {
                                System.err.println("VOTable markup error : "
                                        + currentMarkup + " at line "
                                        + parser.getLineNumber());
                            }

                            father.remove(father.size() - 1);
                            currentMarkup = (String) father.get(father.size() - 1);

                        } catch (Exception e) {
                            System.err.println("Exception END_TAG : " + e + " at line " + parser.getLineNumber());
                        }
                        break;

                    case KXmlParser.END_DOCUMENT:
                        try {
                            consumer.endDocument();

                            // trace mode
                            if (trace) {
                                System.err.println("Document end reached!");
                            }
                        } catch (Exception e) {
                            System.err.println("Exception END_DOCUMENT : " + e + " at line " + parser.getLineNumber());
                        }
                        break;

                    case KXmlParser.TEXT:
                        try {
                            // TD
                            if (currentMarkup.equalsIgnoreCase(TD)) {
                                consumer.textTD((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("TD : "
                                            + (parser.getText()).trim());
                                }
                            } // STREAM 
                            else if (currentMarkup.equalsIgnoreCase(STREAM)) {
                                consumer.textStream((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("STREAM : " + (parser.getText()).trim());
                                }
                            } // DESCRIPTION 
                            else if (currentMarkup.equalsIgnoreCase(DESCRIPTION)) {
                                consumer.textDescription((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("DESCRIPTION : " + (parser.getText()).trim());
                                }
                            } // MIN
                            else if (currentMarkup.equalsIgnoreCase(MIN)) {
                                consumer.textMin((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("MIN : " + (parser.getText()).trim());
                                }
                            } // MAX
                            else if (currentMarkup.equalsIgnoreCase(MAX)) {
                                consumer.textMax((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("MAX : " + (parser.getText()).trim());
                                }
                            } // COOSYS - deprecated since VOTable 1.2
                            else if (currentMarkup.equalsIgnoreCase(COOSYS)) {
                                consumer.textCoosys((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("COOSYS : " + (parser.getText()).trim());
                                }
                            } // LINK
                            else if (currentMarkup.equalsIgnoreCase(LINK)) {
                                consumer.textLink((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("LINK : " + (parser.getText()).trim());
                                }
                            } // OPTION
                            else if (currentMarkup.equalsIgnoreCase(OPTION)) {
                                consumer.textOption((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("OPTION : " + (parser.getText()).trim());
                                }
                            } // GROUP
                            else if (currentMarkup.equalsIgnoreCase(GROUP)) {
                                consumer.textGroup((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("GROUP : " + (parser.getText()).trim());
                                }
                            } // INFO
                            else if (currentMarkup.equalsIgnoreCase(INFO)) {
                                consumer.textInfo((parser.getText()).trim());

                                // trace mode
                                if (trace) {
                                    System.err.println("INFO : " + (parser.getText()).trim());
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Exception TEXT : " + e + " at line " + parser.getLineNumber());
                        }
                        break;

                    case KXmlParser.START_DOCUMENT:
                        try {
                            consumer.startDocument();

                            // trace mode
                            if (trace) {
                                System.err.println("Document start reached!");
                            }
                        } catch (Exception e) {
                            System.err.println("Exception START_DOCUMENT : " + e + " at line " + parser.getLineNumber());
                        }
                        break;

                    default:
                        if (trace) {
                            System.err.println(" ignoring some other (legacy) event at line : " + parser.getLineNumber());
                        }
                }

                // new values
                eventType = parser.next();
            }
        } catch (Exception f) {
            System.err.println("Exception parse : " + f + " at line " + parser.getLineNumber());
        }
        try {
            consumer.endDocument();

            // trace mode
            if (trace) {
                System.err.println("Document end reached!");
            }
        } catch (Exception e) {
            System.err.println("Exception END_DOCUMENT : " + e + " at line " + parser.getLineNumber());
        }
    }

    /**
     * Enable debug mode
     * 
     * @param debug
     *            boolean
     */
    public void enableDebug(boolean debug) {
        trace = debug;
    }

    private void jbInit() throws Exception {
    }

    public XmlPullParser getParser() {
        return parser;
    }

    public void setParser(XmlPullParser parser) {
        this.parser = parser;
    }
}
