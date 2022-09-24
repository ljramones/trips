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
 * <p>Binary interpreter for the type "logical" of VOTable.</p>
 * <ul>
 * 	<li>Null value = 0x00</li>
 * 	<li>Size = 1 byte</li>
 * 	<li>Java type = Boolean</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class BooleanInterpreter extends BinaryFieldInterpreter<Boolean> {

    public BooleanInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "logical value", 1);
    }

    @Override
    public Boolean decodePrimary(final byte[] b, final int offset) throws BinaryInterpreterException {
        char temp = (char) (b[offset] & 0xff);
        switch (temp) {
            case '1':
            case 'T':
            case 't':
                return Boolean.TRUE;
            case '0':
            case 'F':
            case 'f':
                return Boolean.FALSE;
            case '\0':
            case ' ':
            case '?':
                return null;
            default:
                throw new BinaryInterpreterException("Unknown logical representation: \"" + temp + "\" !");
        }
    }

    @Override
    protected Class<Boolean[]> getArrayClass() {
        return Boolean[].class;
    }

    @Override
    protected Boolean convertPrimary(Object value) throws BinaryInterpreterException {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("t") || str.equalsIgnoreCase("1")) {
                return Boolean.TRUE;
            } else if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("f") || str.equalsIgnoreCase("0")) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        } else {
            throw new BinaryInterpreterException("Impossible to convert a " + value.getClass().getName() + " into a Boolean !");
        }
    }

    @Override
    public byte[] encodePrimary(Boolean value) throws BinaryInterpreterException {
        if (value == null) {
            return getPadding();
        } else {
            return new byte[]{(value ? (byte) '1' : (byte) '0')};
        }
    }
}
