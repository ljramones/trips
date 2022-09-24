package cds.savot.common;

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
 * VOTable markups and attributes
 * </p>
 * 
 * @author Andre Schaaff 
 */
public interface Markups {

    // markups
    final static String SAVOTPARSER = "4.0";
    final static String XML = "XML";
    final static String VOTABLE = "VOTABLE";
    final static String TABLE = "TABLE";
    final static String FIELD = "FIELD";
    final static String FIELDREF = "FIELDref"; /* new since VOTAble 1.1 */

    final static String TABLEDATA = "TABLEDATA";
    final static String DESCRIPTION = "DESCRIPTION";
    final static String DATA = "DATA";
    final static String RESOURCE = "RESOURCE";
    final static String PARAM = "PARAM";
    final static String PARAMREF = "PARAMref"; /* new since VOTAble 1.1 */

    final static String DEFINITIONS = "DEFINITIONS";
    final static String LINK = "LINK";
    final static String GROUP = "GROUP"; /* new since VOTAble 1.1 */

    final static String INFO = "INFO";
    final static String TR = "TR";
    final static String TD = "TD";
    final static String COOSYS = "COOSYS";
    final static String SYSTEM = "SYSTEM";
    final static String OPTION = "OPTION";
    final static String FITS = "FITS";
    final static String STREAM = "STREAM";
    final static String BINARY = "BINARY";
    final static String BINARY2 = "BINARY2";
    final static String VALUES = "VALUES";

    // attributes
    final static String ARRAYSIZE = "arraysize";
    final static String DATATYPE = "datatype";
    final static String EPOCH = "epoch";
    final static String EQUINOX = "equinox";
    final static String INCLUSIVE = "inclusive";
    final static String MAX = "max";
    final static String MIN = "min";
    final static String NAME = "name";
    final static String PRECISION = "precision";
    final static String REF = "ref";
    final static String TYPE = "type";
    final static String XTYPE = "xtype"; // since VOTable 1.2
    final static String UTYPE = "utype"; // since VOTable 1.1
    final static String UCD = "ucd";
    final static String UNIT = "unit";
    final static String VALUE = "value";
    final static String WIDTH = "width";
    final static String ID = "id";
    final static String CONTENTROLE = "content-role";
    final static String CONTENTTYPE = "content-type";
    final static String TITLE = "title";
    final static String HREF = "href";
    final static String GREF = "gref"; // deprecated since VOTable 1.1
    final static String ACTION = "action";
    final static String VERSION = "version";
    final static String ENCODING = "encoding";
    final static String EXTNUM = "extnum";
    final static String NULL = "null";
    final static String INVALID = "invalid";
    final static String ACTUATE = "actuate";
    final static String EXPIRES = "expires";
    final static String RIGHTS = "rights";
    final static String NROWS = "nrows"; // since VOTable 1.1
    final static String XMLNS = "xmlns";
    final static String XSI = "xsi";
    final static String XMLNSXSI = XMLNS + ":" + XSI;
    final static String XSI_NOSCHEMA = "noNamespaceSchemaLocation";
    final static String XSI_SCHEMA = "schemaLocation";
    final static String XSINOSCHEMA = XSI + ":" + XSI_NOSCHEMA;
    final static String XSISCHEMA = XSI + ":" + XSI_SCHEMA;
    final static String SYSTEM_ATTRIBUTE = "system";
}
