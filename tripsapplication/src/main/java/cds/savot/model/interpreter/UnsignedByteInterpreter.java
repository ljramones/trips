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
 * <p>Binary interpreter for the type "unsignedByte" of VOTable.</p>
 * <ul>
 * 	<li>Null value = 0xff</li>
 * 	<li>Size = 1 bytes</li>
 * 	<li>Java type = Short</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class UnsignedByteInterpreter extends BinaryFieldInterpreter<Short> {

    public UnsignedByteInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "unsigned byte", 1);
    }

    @Override
    public Short decodePrimary(final byte[] b, final int offset) throws BinaryInterpreterException {
        return (short) (b[offset] & 0x00ff);
    }

    @Override
    protected Class<Short[]> getArrayClass() {
        return Short[].class;
    }

    @Override
    protected Short convertPrimary(Object value) throws BinaryInterpreterException {
        if (value == null) {
            return null;
        }

        if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof Byte) {
            return (short) (Byte) value;
        } else if (value instanceof String) {
            try {
                return Short.parseShort((String) value);
            } catch (NumberFormatException nfe) {
                throw new BinaryInterpreterException("Impossible to convert \"" + value + "\" into an Unsigned Byte: " + nfe.getMessage() + " !");
            }
        } else {
            throw new BinaryInterpreterException("Impossible to convert a " + value.getClass().getName() + " into an Unsigned Byte !");
        }
    }

    @Override
    public byte[] encodePrimary(Short value) throws BinaryInterpreterException {
        if (value == null) {
            return new byte[]{(byte) 0xff};
        } else {
            return new byte[]{(byte) (value & 0xff)};
        }
    }
}
