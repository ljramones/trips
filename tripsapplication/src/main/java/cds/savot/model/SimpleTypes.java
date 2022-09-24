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
 * This interface will be used to reduce the memory usage
 * </p>
 * 
 * @author Andre Schaaff
 */
public interface SimpleTypes {

    static final String[] DATATYPE = {"boolean", "bit", "unsignedByte",
                                      "short", "int", "long", "char", "unicodeChar", "float", "double",
                                      "floatComplex", "doubleComplex"};
    static final String version = "1.1";
    static final byte BOOLEAN = 0;
    static final byte BIT = 1;
    static final byte UNSIGNEDBYTE = 2;
    static final byte SHORT = 3;
    static final byte INT = 4;
    static final byte LONG = 5;
    static final byte CHAR = 6;
    static final byte UNICODECHAR = 7;
    static final byte FLOAT = 8;
    static final byte DOUBLE = 8;
    static final byte FLOATCOMPLEX = 8;
    static final byte DOUBLECOMPLEX = 8;
    static final String[] YESNO = {"yes", "no"};
    static final byte YES = 0;
    static final byte NO = 1;
    static final String[] RESOURCETYPE = {"results", "meta"};
    static final byte RESULTS = 0;
    static final byte META = 1;
    static final String[] FIELDTYPE = {"hidden", "no_query", "trigger",
                                       "location"};
    static final byte HIDDEN = 0;
    static final byte NO_QUERY = 1;
    static final byte TRIGGER = 2;
    static final String[] VALUESTYPE = {"legal", "actual"};
    static final byte LEGAL = 0;
    static final byte ACTUAL = 1;
    static final String[] LINKCONTENT_ROLE = {"query", "hints", "doc",
                                              "location"};
    static final byte QUERY = 0;
    static final byte HINTS = 1;
    static final byte DOC = 2;
    static final byte LOCATION = 3;
    static final String[] STREAMACTUATE = {"onLoad", "onRequest", "other",
                                           "none"};
    static final byte ONLOAD = 0;
    static final byte ONREQUEST = 1;
    static final byte OTHER = 2;
    static final byte NONE = 3;
    static final String[] ENCODINGTYPE = {"gzip", "base64", "dynamic", "none"};
    static final byte GZIP = 0;
    static final byte BASE64 = 1;
    static final byte DYNAMIC = 2;
    /* defined in STREAMACTUATE final static byte NONE = 3; */
    static final String[] COOSYSSYSTEM = {"eq_FK4", "eq_FK5", "ICRS",
                                          "ecl_FK4", "ecl_FK5", "galactic", "supergalactic", "xy",
                                          "barycentric", "geo_app"};
    static final byte EQ_FK4 = 0;
    static final byte EQ_FK5 = 1;
    static final byte ICRS = 2;
    static final byte ECL_FK4 = 2;
    static final byte ECL_FK5 = 2;
    static final byte GALACTIC = 2;
    static final byte SUPERGALACTIC = 2;
    static final byte XY = 2;
    static final byte BARYCENTRIC = 2;
    static final byte GEO_APP = 2;
}
