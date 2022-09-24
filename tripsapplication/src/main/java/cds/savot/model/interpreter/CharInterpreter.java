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
 * <p>Binary interpreter for the type "character" of VOTable.</p>
 * <ul>
 * 	<li>Null value = \0</li>
 * 	<li>Size = 1 byte</li>
 * 	<li>Java type = Character</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class CharInterpreter extends BinaryFieldInterpreter<Character> {

    public CharInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        this(arraysizes, "character", 1);
    }

    public CharInterpreter(final int[] arraysizes, final String typeLabel, final int nbBytes) throws BinaryInterpreterException {
        super(arraysizes, "character", nbBytes);
        arraySeparator = "";
    }

    @Override
    public Character decodePrimary(final byte[] b, final int offset) throws BinaryInterpreterException {
        return (char) (b[offset] & 0xff);
    }

    @Override
    protected Class<Character[]> getArrayClass() {
        return Character[].class;
    }

    @Override
    protected Character convertPrimary(Object value) throws BinaryInterpreterException {
        if (value == null) {
            return '\0';
        }

        if (value instanceof Character) {
            return (Character) value;
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.length() > 1) {
                throw new BinaryInterpreterException("Impossible to convert into a single " + TYPE_LABEL + " a String which contains " + str.length() + " characters !");
            } else if (str.isEmpty()) {
                return '\0';
            } else {
                return str.charAt(0);
            }
        } else {
            throw new BinaryInterpreterException("Impossible to convert a " + value.getClass().getName() + " into a " + TYPE_LABEL + " !");
        }
    }

    @Override
    public byte[] encodePrimary(Character value) throws BinaryInterpreterException {
        if (value == null) {
            return getPadding();
        } else {
            return new byte[]{(byte) value.charValue()};
        }
    }
}
