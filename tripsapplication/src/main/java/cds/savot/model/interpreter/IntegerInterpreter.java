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
 * <p>Binary interpreter for the type "int" of VOTable.</p>
 * <ul>
 * 	<li>Null value = 0xffffffff</li>
 * 	<li>Size = 4 bytes</li>
 * 	<li>Java type = Integer</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class IntegerInterpreter extends BinaryFieldInterpreter<Integer> {

    public IntegerInterpreter() throws BinaryInterpreterException {
        this(new int[]{1});
    }

    public IntegerInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "integer value", 4);
    }

    @Override
    public Integer decodePrimary(final byte[] b, final int offset) throws BinaryInterpreterException {
        return (int) ((((b[offset] & 0xff) << 24)
                | ((b[offset + 1] & 0xff) << 16)
                | ((b[offset + 2] & 0xff) << 8)
                | ((b[offset + 3] & 0xff))));
    }

    @Override
    protected Class<Integer[]> getArrayClass() {
        return Integer[].class;
    }

    @Override
    protected Integer convertPrimary(Object value) throws BinaryInterpreterException {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException nfe) {
                throw new BinaryInterpreterException("Impossible to convert \"" + value + "\" into an Integer: " + nfe.getMessage() + " !");
            }
        } else {
            throw new BinaryInterpreterException("Impossible to convert a " + value.getClass().getName() + " into an Integer !");
        }
    }

    @Override
    public byte[] encodePrimary(Integer value) throws BinaryInterpreterException {
        if (value == null) {
            return new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        }

        byte[] encoded = new byte[4];
        encoded[0] = (byte) (value >>> 24);
        encoded[1] = (byte) (value >>> 16);
        encoded[2] = (byte) (value >>> 8);
        encoded[3] = (byte) (value & 0xff);
        return encoded;
    }
}
