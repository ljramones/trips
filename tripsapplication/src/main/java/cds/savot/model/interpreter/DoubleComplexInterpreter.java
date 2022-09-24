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
 * <p>Binary interpreter for the type "doubleComplex" of VOTable.</p>
 * <ul>
 * 	<li>Null value = NaN NaN</li>
 * 	<li>Size = 2*8 bytes</li>
 * 	<li>Java type = Double[2]</li>
 * </ul>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public class DoubleComplexInterpreter extends BinaryFieldInterpreter<Double[]> {

    private final DoubleInterpreter doubleDecoder;

    public DoubleComplexInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "double complex", 16);
        doubleDecoder = new DoubleInterpreter();
    }

    @Override
    public Double[] decodePrimary(final byte[] bytes, final int offset) throws BinaryInterpreterException {
        Double[] complex = new Double[2];
        complex[0] = doubleDecoder.decodePrimary(bytes, offset);
        complex[1] = doubleDecoder.decodePrimary(bytes, offset + (NB_BYTES / 2));

        if (complex[0] == null || Double.isNaN(complex[0])
                || complex[1] == null || Double.isNaN(complex[1])) {
            complex = new Double[]{null, null};
        }

        return complex;
    }

    @Override
    protected Class<Double[][]> getArrayClass() {
        return Double[][].class;
    }

    @Override
    protected ArrayList<Double[]> convertIntoArray(Object value) throws BinaryInterpreterException {
        ArrayList<Double> doubles = doubleDecoder.convertIntoArray(value);

        if (doubles.size() % 2 > 0) {
            throw new BinaryInterpreterException("Bad number of double values (" + doubles.size() + "): not a factor of 2 ! Note: a Double Complex is composed of 2 double values.");
        }

        ArrayList<Double[]> list = new ArrayList<Double[]>();
        for (int i = 0; i + 1 < doubles.size(); i += 2) {
            list.add(new Double[]{doubles.get(i), doubles.get(i + 1)});
        }
        return list;
    }

    @Override
    protected Double[] convertPrimary(Object value) throws BinaryInterpreterException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] encodePrimary(Double[] value) throws BinaryInterpreterException {
        if (value.length % 2 > 0) {
            throw new BinaryInterpreterException("Bad number of double values: a Double Complex is composed of 2 double values !");
        }

        byte[] encoded = new byte[2 * doubleDecoder.NB_BYTES];
        for (int i = 0; i < 2; i++) {
            byte[] bytes = doubleDecoder.encodePrimary(value[i]);
            if (doubleDecoder.NB_BYTES >= 0) System.arraycopy(bytes, 0, encoded, i * 8, doubleDecoder.NB_BYTES);
        }
        return encoded;
    }
}
