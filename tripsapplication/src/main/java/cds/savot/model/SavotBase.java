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
 * Base class of the Savot model
 * </p>
 * 
 * @author Andre Schaaff
 */
public class SavotBase implements SimpleTypes {

    /**
     * Protected constructor
     */
    protected SavotBase() {
    }

    // TODO: type conversion
    /**
     * Helper function to return String value or "" if the given string is null
     * @param str any String value
     * @return String value or "" if the given string is null
     */
    final String str(final String str) {
        return (str != null) ? str : "";
    }

    /**
     * Helper function to return int value or 0 if the given string is null
     * @param str any String value
     * @return int value or 0 if the given string is null
     */
    final int integer(final String str) {
        return (str != null) ? Integer.parseInt(str) : 0;
    }

    /**
     * Helper function to convert the integer value to String or null if the given integer is negative
     * @param val an integer value
     * @return String value or null if the given integer is negative
     */
    final String toStr(final int val) {
        return (val >= 0) ? Integer.toString(val) : null;
    }
}
