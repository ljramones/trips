package cds.savot.model.interpreter;

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
 * <p>Binary interpreter for the type "unicodeChar" of VOTable.</p>
 * <ul>
 * 	<li>Null value = \0</li>
 * 	<li>Size = 2 bytes</li>
 * 	<li>Java type = Character</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class UnicodeCharInterpreter extends CharInterpreter {

    public UnicodeCharInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "unicode character", 2);
    }

    @Override
    public Character decodePrimary(final byte[] b, final int offset) throws BinaryInterpreterException {
        return (char) ((b[offset] << 8) | (b[offset + 1] & 0xff));
    }

    @Override
    public byte[] encodePrimary(Character value) throws BinaryInterpreterException {
        if (value == null) {
            return getPadding();
        } else {
            return new byte[]{(byte) (value >> 8), (byte) (value & 0xff)};
        }
    }
}
