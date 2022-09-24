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
 * <p>Binary interpreter for the type "float" of VOTable.</p>
 * <ul>
 * 	<li>Null value = NaN</li>
 * 	<li>Size = 4 bytes</li>
 * 	<li>Java type = Float</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class FloatInterpreter extends BinaryFieldInterpreter<Float> {

    private final IntegerInterpreter intDecoder;

    public FloatInterpreter() throws BinaryInterpreterException {
        this(new int[]{1});
    }

    public FloatInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "float complex", 4);
        intDecoder = new IntegerInterpreter();
    }

    @Override
    public Float decodePrimary(final byte[] bytes, final int offset) throws BinaryInterpreterException {
        return Float.intBitsToFloat(intDecoder.decodePrimary(bytes, offset));
    }

    @Override
    protected Class<Float[]> getArrayClass() {
        return Float[].class;
    }

    @Override
    protected Float convertPrimary(Object value) throws BinaryInterpreterException {
        if (value == null) {
            return Float.NaN;
        }

        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException nfe) {
                throw new BinaryInterpreterException("Impossible to convert \"" + value + "\" into a Float: " + nfe.getMessage() + " !");
            }
        } else {
            throw new BinaryInterpreterException("Impossible to convert a " + value.getClass().getName() + " into a Float !");
        }
    }

    @Override
    public byte[] encodePrimary(Float value) throws BinaryInterpreterException {
        if (value == null) {
            value = Float.NaN;
        }
        return intDecoder.encodePrimary(Float.floatToRawIntBits(value));
    }
}
