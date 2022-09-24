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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * 
 * @author Gregory Mantelet
 *
 */
public class BitInterpreter extends CharInterpreter {

    public BitInterpreter(final int[] arraysizes) throws BinaryInterpreterException {
        super(arraysizes, "bit array", 1);
    }

    @Override
    public Character[] decode(final InputStream input) throws IOException, BinaryInterpreterException {
        int arraysize = getArraySize(input);

        int nbBytes = arraysize / 8;
        if (arraysize % 8 > 0) {
            nbBytes++;
        }

        byte[] bytes = readBytes(input, nbBytes);

        Character[] decoded = createEmptyArray(arraysize);
        int indBit = 0;
        for (int i = 0; i < nbBytes && indBit < arraysize; i++) {
            byte b = bytes[i];
            for (int j = 0; j < 8 && indBit < arraysize; j++, indBit++) {
                char c = (char) (((0x80 & (b << j)) >>> 7) | '0');
                decoded[indBit] = c;
            }
        }

        return decoded;
    }

    @Override
    public Character decodePrimary(final byte[] bytes, final int offset) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Class<Character[]> getArrayClass() {
        return Character[].class;
    }

    @Override
    protected Character convertPrimary(Object value) throws BinaryInterpreterException {
        if (value != null && value instanceof Boolean) {
            return ((Boolean) value) ? '1' : '0';
        } else {
            char c = super.convertPrimary(value);
            if (c != '0' && c != '1') {
                throw new BinaryInterpreterException("Incorrect bit value: \"" + c + "\" !");
            }
            return c;
        }
    }

    @Override
    public void encode(OutputStream output, Object value) throws IOException, BinaryInterpreterException {
        boolean variableArray = (nbItems < 0);
        boolean encodeInArray = (variableArray || (nbItems > 1));

        // get the corresponding array, but of only 1 dimension:
        ArrayList<Character> values = new ArrayList<Character>();
        if (value.getClass().isArray()) {
            if (!encodeInArray) {
                throw new BinaryInterpreterException("Impossible to encode an array into a single " + TYPE_LABEL + " !");
            } else {
                values.addAll(convertIntoArray((Object[]) value));
            }
        } else {
            values.add(convertPrimary(value));
        }

        // write the number of bytes if needed:
        if (variableArray) {
            arraySizeInterpreter.encode(output, values.size());
        } else if (values.size() > nbItems) {
            throw new BinaryInterpreterException("The given array is bigger than the arraysize set by the savot field: " + values.size() + " > " + nbItems + " !");
        }

        // count needed bytes (padding included):
        int nbBytes;
        if (nbItems > 0) {
            nbBytes = (nbItems / 8);
            if (nbItems % 8 > 0) {
                nbBytes++;
            }
        } else {
            nbBytes = (values.size() / 8);
            if (values.size() % 8 > 0) {
                nbBytes++;
            }
        }

        // write all the given items (and the padding bytes):
        byte[] bytes = new byte[nbBytes];
        for (int b = 0; b < nbBytes; b++) {
            bytes[b] = 0;
            for (int i = 0, v = b * 8; i < 8 && v < values.size(); i++, v++) {
                char c = values.get(v);
                switch (c) {
                    case '1':
                        bytes[b] |= (byte) (1 << (7 - i));
                        break;
                    case '0':
                        break;
                    default:
                        throw new BinaryInterpreterException("Incorrect bit value: " + c);
                }
            }
        }
        output.write(bytes);
    }

    @Override
    public byte[] encodePrimary(Character value) throws BinaryInterpreterException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getPadding(final int length) {
        throw new UnsupportedOperationException();
    }
}
