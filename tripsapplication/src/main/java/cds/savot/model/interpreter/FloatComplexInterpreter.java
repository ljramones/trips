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
import java.util.ArrayList;

/**
 * <p>Binary interpreter for the type "floatComplex" of VOTable.</p>
 * <ul>
 * 	<li>Null value = NaN NaN</li>
 * 	<li>Size = 2*4 bytes</li>
 * 	<li>Java type = Float[2]</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class FloatComplexInterpreter extends BinaryFieldInterpreter<Float[]> {

    private final FloatInterpreter floatDecoder;

    public FloatComplexInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "float complex", 8);
        floatDecoder = new FloatInterpreter();
    }

    @Override
    public Float[] decodePrimary(final byte[] bytes, final int offset) throws BinaryInterpreterException {
        Float[] complex = new Float[2];
        complex[0] = floatDecoder.decodePrimary(bytes, offset);
        complex[1] = floatDecoder.decodePrimary(bytes, offset + (NB_BYTES / 2));

        if (complex[0] == null || Float.isNaN(complex[0])
                || complex[1] == null || Float.isNaN(complex[1])) {
            complex = new Float[]{Float.NaN, Float.NaN};
        }

        return complex;
    }

    @Override
    protected Class<Float[][]> getArrayClass() {
        return Float[][].class;
    }

    @Override
    protected ArrayList<Float[]> convertIntoArray(Object value) throws BinaryInterpreterException {
        ArrayList<Float> floats = floatDecoder.convertIntoArray(value);

        if (floats.size() % 2 > 0) {
            throw new BinaryInterpreterException("Bad number of float values (" + floats.size() + "): not a factor of 2 ! Note: a Float Complex is composed of 2 float values.");
        }

        ArrayList<Float[]> list = new ArrayList<Float[]>();
        for (int i = 0; i + 1 < floats.size(); i += 2) {
            list.add(new Float[]{floats.get(i), floats.get(i + 1)});
        }
        return list;
    }

    @Override
    protected Float[] convertPrimary(Object value) throws BinaryInterpreterException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] encodePrimary(Float[] value) throws BinaryInterpreterException {
        if (value.length % 2 > 0) {
            throw new BinaryInterpreterException("Bad number of float values: a Float Complex is composed of 2 float values !");
        }

        byte[] encoded = new byte[2 * floatDecoder.NB_BYTES];
        for (int i = 0; i < 2; i++) {
            byte[] bytes = floatDecoder.encodePrimary(value[i]);
            if (floatDecoder.NB_BYTES >= 0) System.arraycopy(bytes, 0, encoded, i * 8, floatDecoder.NB_BYTES);
        }
        return encoded;
    }
}
