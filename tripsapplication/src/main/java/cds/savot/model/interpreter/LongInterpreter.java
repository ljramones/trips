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
 * <p>Binary interpreter for the type "long" of VOTable.</p>
 * <ul>
 * 	<li>Null value = 0xffffffffffffffff</li>
 * 	<li>Size = 8 bytes</li>
 * 	<li>Java type = Long</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class LongInterpreter extends BinaryFieldInterpreter<Long> {

    public LongInterpreter() throws BinaryInterpreterException {
        this(new int[]{1});
    }

    public LongInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "long value", 8);
    }

    @Override
    public Long decodePrimary(final byte[] b, final int offset) throws BinaryInterpreterException {
        return ((((long) b[offset] & 0xff) << 56)
                | (((long) b[offset + 1] & 0xff) << 48)
                | (((long) b[offset + 2] & 0xff) << 40)
                | (((long) b[offset + 3] & 0xff) << 32)
                | (((long) b[offset + 4] & 0xff) << 24)
                | (((long) b[offset + 5] & 0xff) << 16)
                | (((long) b[offset + 6] & 0xff) << 8)
                | (((long) b[offset + 7] & 0xff)));
    }

    @Override
    protected Class<Long[]> getArrayClass() {
        return Long[].class;
    }

    @Override
    protected Long convertPrimary(Object value) throws BinaryInterpreterException {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException nfe) {
                throw new BinaryInterpreterException("Impossible to convert \"" + value + "\" into a Long: " + nfe.getMessage() + " !");
            }
        } else {
            throw new BinaryInterpreterException("Impossible to convert a " + value.getClass().getName() + " into a Long !");
        }
    }

    @Override
    public byte[] encodePrimary(Long value) throws BinaryInterpreterException {
        if (value == null) {
            return new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        }

        byte[] encoded = new byte[8];
        for (int i = 0; i < encoded.length; i++) {
            encoded[i] = (byte) (value >>> 8 * (encoded.length - 1 - i));
        }
        return encoded;
    }
}
