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
 * <p>Binary interpreter for the type "double" of VOTable.</p>
 * <ul>
 * 	<li>Null value = NaN</li>
 * 	<li>Size = 8 bytes</li>
 * 	<li>Java type = Double</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class DoubleInterpreter extends BinaryFieldInterpreter<Double> {

    private final LongInterpreter longDecoder = new LongInterpreter();

    public DoubleInterpreter() throws BinaryInterpreterException {
        this(new int[]{1});
    }

    public DoubleInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "double value", 8);
    }

    @Override
    public Double decodePrimary(final byte[] bytes, final int offset) throws BinaryInterpreterException {
        return Double.longBitsToDouble(longDecoder.decodePrimary(bytes, offset));
    }

    @Override
    protected Class<Double[]> getArrayClass() {
        return Double[].class;
    }

    @Override
    protected Double convertPrimary(Object value) throws BinaryInterpreterException {
        if (value == null) {
            return Double.NaN;
        }

        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException nfe) {
                throw new BinaryInterpreterException("Impossible to convert \"" + value + "\" into a Double: " + nfe.getMessage() + " !");
            }
        } else {
            throw new BinaryInterpreterException("Impossible to convert a " + value.getClass().getName() + " into a Double !");
        }
    }

    @Override
    public byte[] encodePrimary(Double value) throws BinaryInterpreterException {
        if (value == null) {
            value = Double.NaN;
        }
        return longDecoder.encodePrimary(Double.doubleToRawLongBits(value));
    }
}
