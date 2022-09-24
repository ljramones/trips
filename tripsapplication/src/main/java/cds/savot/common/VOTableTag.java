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
 * This enum contains all tags present in VOTable specification
 *
 * @author Laurent Bourges
 */
public enum VOTableTag {

    UNDEFINED,
    BINARY,
    BINARY2,
    COOSYS,
    DATA,
    DEFINITIONS,
    DESCRIPTION,
    FIELD,
    FIELDREF,
    FITS,
    GROUP,
    INFO,
    LINK,
    MIN,
    MAX,
    OPTION,
    PARAM,
    PARAMREF,
    RESOURCE,
    STREAM,
    SYSTEM,
    TABLE,
    TABLEDATA,
    TD,
    TR,
    VALUES,
    VOTABLE;

    /**
     * Return the VoTableTag corresponding to the given string (ignore case)
     * <p>
     * LBO: Note: equalsIgnoreCase() vs() equals as XML is case sensitive and VOTable specification says that clearly
     *
     * @param name tag name to look up
     * @return VoTableTag or VoTableTag.UNDEFINED if no match
     */
    public static VOTableTag parseTag(final String name) {
        switch (name.charAt(0)) {
            case 'T':
            case 't':
                // note: String comparison using == because our kXml parser or Java Stax Parser provides interned strings using SymbolTable:
                if (Markups.TD.equals(name)) {
                    return TD;
                }
                if (Markups.TR.equals(name)) {
                    return TR;
                }
                // standard comparison fallback:
                if (Markups.TD.equalsIgnoreCase(name)) {
                    return TD;
                }
                if (Markups.TR.equalsIgnoreCase(name)) {
                    return TR;
                }
                if (Markups.TABLE.equalsIgnoreCase(name)) {
                    return TABLE;
                }
                if (Markups.TABLEDATA.equalsIgnoreCase(name)) {
                    return TABLEDATA;
                }
                break;

            case 'F':
            case 'f':
                if (Markups.FIELD.equalsIgnoreCase(name)) {
                    return FIELD;
                }
                if (Markups.FIELDREF.equalsIgnoreCase(name)) {
                    return FIELDREF;
                }
                if (Markups.FITS.equalsIgnoreCase(name)) {
                    return FITS;
                }
                break;

            case 'G':
            case 'g':
                if (Markups.GROUP.equalsIgnoreCase(name)) {
                    return GROUP;
                }
                break;

            case 'P':
            case 'p':
                if (Markups.PARAM.equalsIgnoreCase(name)) {
                    return PARAM;
                }
                if (Markups.PARAMREF.equalsIgnoreCase(name)) {
                    return PARAMREF;
                }
                break;

            case 'I':
            case 'i':
                if (Markups.INFO.equalsIgnoreCase(name)) {
                    return INFO;
                }
                break;

            case 'D':
            case 'd':
                if (Markups.DESCRIPTION.equalsIgnoreCase(name)) {
                    return DESCRIPTION;
                }
                if (Markups.DATA.equalsIgnoreCase(name)) {
                    return DATA;
                }
                if (Markups.DEFINITIONS.equalsIgnoreCase(name)) {
                    return DEFINITIONS;
                }
                break;

            case 'B':
            case 'b':
                if (Markups.BINARY.equalsIgnoreCase(name)) {
                    return BINARY;
                }
                if (Markups.BINARY2.equalsIgnoreCase(name)) {
                    return BINARY2;
                }
                break;

            case 'C':
            case 'c':
                if (Markups.COOSYS.equalsIgnoreCase(name)) {
                    return COOSYS;
                }
                break;

            case 'L':
            case 'l':
                if (Markups.LINK.equalsIgnoreCase(name)) {
                    return LINK;
                }
                break;

            case 'M':
            case 'm':
                if (Markups.MIN.equalsIgnoreCase(name)) {
                    return MIN;
                }
                if (Markups.MAX.equalsIgnoreCase(name)) {
                    return MAX;
                }
                break;

            case 'O':
            case 'o':
                if (Markups.OPTION.equalsIgnoreCase(name)) {
                    return OPTION;
                }
                break;

            case 'S':
            case 's':
                if (Markups.STREAM.equalsIgnoreCase(name)) {
                    return STREAM;
                }
                if (Markups.SYSTEM.equalsIgnoreCase(name)) {
                    return SYSTEM;
                }
                break;

            case 'R':
            case 'r':
                if (Markups.RESOURCE.equalsIgnoreCase(name)) {
                    return RESOURCE;
                }
                break;

            case 'V':
            case 'v':
                if (Markups.VALUES.equalsIgnoreCase(name)) {
                    return VALUES;
                }
                if (Markups.VOTABLE.equalsIgnoreCase(name)) {
                    return VOTABLE;
                }
                break;

            default:
        }
        return UNDEFINED;
    }
}
