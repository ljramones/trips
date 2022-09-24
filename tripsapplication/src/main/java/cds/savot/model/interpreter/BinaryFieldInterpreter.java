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

import cds.savot.model.SavotField;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A BinaryFieldInterpreter is able to encode and to decode a given type of data in binary.
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 *
 * @param <T>	Type this interpreter can encode into and decode from binary data.
 */
public abstract class BinaryFieldInterpreter<T> {

    /** Name/Label of the type managed by this BinaryFieldInterpreter. */
    protected final String TYPE_LABEL;
    /** Number of bytes occupied by the type managed in this BinaryFieldInterpreter. */
    protected final int NB_BYTES;
    /** Interpreter used only to encode or to decode the array size. */
    protected final IntegerInterpreter arraySizeInterpreter;
    /** Indicates the dimension(s) of the data to encode/decode. */
    protected final int[] fixArraySizes;
    /** Total number of items to encode/decode, all dimensions confounded. */
    protected final int nbItems;
    /** String representation of a <code>null</code> value. */
    protected String strNullValue = "";
    /** Separator of items in the string representation of an array. */
    protected String arraySeparator = " ";

    /**
     * Builds a BinaryFieldInterpreter.
     * 
     * @param arraysizes	Dimensions of the primary data type to encode/decode.
     * @param typeLabel		Name/Label of the type to manage.
     * @param nbBytes		Number of bytes of a primary data type.
     * 
     * @throws BinaryInterpreterException	If the dimensions are not valid or if there is a problem while creating the array size interpreter.
     */
    public BinaryFieldInterpreter(final int[] arraysizes, final String typeLabel, final int nbBytes) throws BinaryInterpreterException {
        TYPE_LABEL = (typeLabel == null || typeLabel.trim().isEmpty()) ? "byte" : typeLabel.trim();
        NB_BYTES = (nbBytes <= 0) ? 1 : nbBytes;

        // Count the total number of items and check the given array-sizes:
        int nbItems = 0;
        fixArraySizes = new int[arraysizes.length];
        for (int i = 0; i < arraysizes.length; i++) {
            if (arraysizes[i] <= 0) {
                if (i == arraysizes.length - 1) {
                    fixArraySizes[i] = -1;
                    nbItems = -1;
                } else {
                    throw new BinaryInterpreterException("Incorrect arraysize: only the last dimension of an array can be variable in length !");
                }
            } else {
                fixArraySizes[i] = arraysizes[i];
                nbItems += arraysizes[i];
            }
        }
        this.nbItems = (nbItems <= 0) ? -1 : nbItems;

        // An array-size interpreter is needed only if the array-size is variable:
        if (this.nbItems != -1) {
            arraySizeInterpreter = null;
        } else {
            arraySizeInterpreter = new IntegerInterpreter();
        }
    }

    /**
     * <p>Decodes the binary data coming from the given input stream.</p>
     * 
     * <p>
     * 	Basically, this method gets the array-size (particularly if variable),
     * 	creates an empty array of the good dimension(s) and fills it by decoding
     * 	one by one data of type T.
     * </p>
     * 
     * @param input	Data to decode.
     * 
     * @return		<code>null</code> if EOF, else the decoded data of type T.
     * 
     * @throws IOException	If the EOF has been reached in an unexpected manner
     * 						or if an error occurs while reading bytes from the given input stream.
     * 
     * @see #getArraySize(InputStream)
     * @see #readBytes(InputStream, int)
     * @see #createEmptyArray(int)
     * @see #decodePrimary(byte[],int)
     */
    public T[] decode(final InputStream input) throws IOException {
        int arraysize = getArraySize(input);
        if (arraysize == -1) {
            return null;
        }
        byte[] bytes = readBytes(input, arraysize);
        if (bytes == null) {
            if (arraySizeInterpreter == null) {
                return null;
            } else {
                throw new BinaryInterpreterException("Unexpected EOF: " + arraysize + " items of type " + TYPE_LABEL + " should be read !");
            }
        }

        T[] decoded = createEmptyArray(arraysize);
        for (int i = 0; i < arraysize; i++) {
            decoded[i] = decodePrimary(bytes, i * NB_BYTES);
        }

        return decoded;
    }

    /**
     * <p>Gets the number of data of type T to get currently.</p>
     * 
     * <p>
     * 	Either the array-size is defined at the creation, or it is variable. In one hand the given array-size is just returned.
     * 	In another hand the array-size is an integer value which prefixes the data to read.
     * 	In that case, we must read one integer from the given input stream.
     * 	This integer corresponds to the length of the data of type T to get.
     * </p>
     * 
     * @param input		The data to decode.
     * @return			<code>-1</code> if EOF, else the length of data of type T to return.
     * 
     * @throws IOException	If an error occurs while decoding the array-size from the given input stream.
     */
    protected int getArraySize(final InputStream input) throws IOException {
        if (arraySizeInterpreter != null) {
            Integer[] ints = arraySizeInterpreter.decode(input);
            if (ints == null) {
                return -1;
            } else {
                return ints[0];
            }
        } else {
            return nbItems;
        }
    }

    /**
     * <p>
     * 	Reads <code>length</code> data of type T from the given input stream considering
     * 	the number of bytes one data of this type is supposed to occupied (see {@link #NB_BYTES}).
     * </p>
     * 
     * @param input	Input stream from which bytes must be read.
     * @param length
     * @return		<code>null</code> if EOF, else the read bytes (the length of this array is a multiple of {@link #NB_BYTES}).
     * 
     * @throws IOException	If the end of file has been reached before getting <code>length</code> full items of type T
     * 						or if there is an error while reading bytes from the given input stream.
     */
    protected byte[] readBytes(final InputStream input, final int length) throws IOException {
        if (length < 0) {
            throw new NegativeArraySizeException("Impossible to get negative number of " + TYPE_LABEL + " !");
        } else if (length == 0) {
            return new byte[0];
        }

        // Read "length" items that is to say: (length * NB_BYTES) bytes:
        byte[] bytes = new byte[NB_BYTES * length];
        int nbRead = input.read(bytes);

        // If all bytes have been read:
        if (nbRead == bytes.length) {
            return bytes;
        } // If EOF has been reached before getting one byte:
        else if (nbRead == -1) {
            return null;
        } // If less than "length" full items have been read:
        else if (nbRead % NB_BYTES == 0) {
            throw new BinaryInterpreterException("Unexpected EOF: can not get " + length + " value of type " + TYPE_LABEL + " ! (" + (nbRead / NB_BYTES) + " items successfully read)");
        } // If one item has not been read completely:
        else {
            throw new BinaryInterpreterException("Unexpected EOF: can not get a full " + TYPE_LABEL + " (= " + NB_BYTES + " bytes) ! (" + (nbRead / NB_BYTES) + " items successfully read)");
        }
    }

    /**
     * <p>Serializes the given cell value.</p>
     * 
     * <p>NOTE:
     * 	The given value can be a single value or a multidimensional array.
     * 	In this last case all items (whatever is their dimension)
     * 	are written in a String separated by {@link #arraySeparator}
     * 	(which depends of the type managed by the BinaryFieldInterpreter ; by default ' ').
     * </p>
     * 
     * @param cellValue		Value (single value or array) of a column/cell/field. (may be <code>null</code>)
     * 
     * @return				The String serialization of the given value.
     */
    public String convertToString(Object cellValue) {
        if (cellValue == null) {
            return strNullValue;
        } else if (cellValue.getClass().isArray()) {
            Object[] array = (Object[]) cellValue;
            /* TODO : define initial capacity or recycle StringBuilder instances */
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    str.append(arraySeparator);
                }
                str.append(convertToString(array[i]));
            }
            return str.toString();
        } else {
            return cellValue.toString();
        }
    }

    /**
     * Creates an array (1 dimension) of type T with the given length.
     * 
     * @param arraysize	Length of the array to create. (MUST BE >= 0)
     * 
     * @return			An empty array of type T and of length <code>arraysize</code>.
     * 
     * @throws ClassCastException			If it is impossible to create an array of T.
     * @throws NegativeArraySizeException	If the given array size is negative.
     * 
     * @see #getArrayClass()
     */
    protected T[] createEmptyArray(final int arraysize) throws ClassCastException, NegativeArraySizeException {
        return getArrayClass().cast(Array.newInstance(getArrayClass().getComponentType(), arraysize));
    }

    /**
     * <p>Gets the precise array type.</p>
     * 
     * <p>Generally: <code>T[].class</code>, where T must be a concrete class.</p>
     * 
     * @return	The class of an array of type T.
     */
    protected abstract Class<T[]> getArrayClass();

    /**
     * <p>Decodes only one data of type T from the given bytes array.</p>
     * 
     * <p>WARNING: <code>bytes</code> is supposed to contain enough bytes (>= {@link #NB_BYTES}) from the given offset.</p>
     * 
     * @param bytes		Array to use to extract enough bytes so that decoding one data of type T.
     * @param offset	Position from which bytes must be read.
     * 
     * @return		The decoded value.
     * 
     * @throws BinaryInterpreterException	If an error occurs while decoding bytes.
     */
    public abstract T decodePrimary(final byte[] bytes, final int offset) throws BinaryInterpreterException;


    /* *************** */
    /* BINARY ENCODING */
    /* *************** */
    /**
     * Encodes the given value in binary and writes it in the given output stream.
     * 
     * @param output	Stream in which the encoded value must be written.
     * @param value		The value to write once encoded in binary.
     * 
     * @throws IOException					If there is an error while writing in the given stream.
     * @throws BinaryInterpreterException	If there is an error while encoding the given value.
     * 
     * @see #convertIntoArray(Object)
     * @see #encodePrimary(Object)
     * @see #getPadding()
     */
    public void encode(final OutputStream output, final Object value) throws IOException, BinaryInterpreterException {
        boolean variableArray = (nbItems < 0);
        boolean encodeInArray = (variableArray || (nbItems > 1));

        // Get the corresponding array, but of only 1 dimension:
        if (value != null && value.getClass().isArray()) {
            if (!encodeInArray) {
                throw new BinaryInterpreterException("Impossible to encode an array into a single " + TYPE_LABEL + " !");
            }
        }
        ArrayList<T> values = new ArrayList<T>(convertIntoArray(value));

        // Write the number of items if needed:
        if (variableArray) {
            arraySizeInterpreter.encode(output, values.size());
        } else if (values.size() > nbItems) {
            throw new BinaryInterpreterException("The given array is bigger than the arraysize set by the savot field: " + values.size() + " > " + nbItems + " !");
        }

        // Write all given items:
        for (T t : values) {
            output.write(encodePrimary(t));
        }

        // Include padding items if needed:
        if (values.size() < nbItems) {
            output.write(getPadding(nbItems - values.size()));
        }
    }

    /**
     * Creates an array of the given length with padding values (0x00).
     * 
     * param length
     * @return	Array of the given length of padding values.
     */
    public byte[] getPadding(final int length) {
        return new byte[length];
    }

    /**
     * Creates an array of the length of the type T ({@link #NB_BYTES}) with padding values (0x00).
     * 
     * @return	Bytes of one padding value of type T.
     * 
     * @see #getPadding(int)
     */
    public final byte[] getPadding() {
        return getPadding(NB_BYTES);
    }

    /**
     * <p>Converts the given value (single value or multidimensional array) into one array of one dimension.</p>
     * 
     * <p>NOTE:
     * 	A String value will be considered as an array whose items are separated by {@link #arraySeparator}.
     * 	Another type (except an array) must be understandable by {@link #convertPrimary(Object)}.
     * </p>
     * 
     * @param value	Value to convert (single value or multidimensional array).
     * 
     * @return		A list of all values included into the given object.
     * 
     * @throws BinaryInterpreterException	If {@link #convertPrimary(Object)} fails.
     * 
     * @see #convertPrimary(Object)
     */
    protected ArrayList<T> convertIntoArray(final Object value) throws BinaryInterpreterException {
        ArrayList<T> list = new ArrayList<T>();

        // NULL values must be understandable by convertPrimary(...):
        if (value == null) {
            list.add(convertPrimary(value));
            return list;
        }

        // If the value is a string, it could be an array in which items are separated by the given array separator (by default a whitespace):
        if (value instanceof String) {
            String str = (String) value;
            // Split the string in function of the given array separator:
            /* TODO: define initial capacity or recycle StringBuilder instances */
            String[] values;
            if (arraySeparator == null || arraySeparator.isEmpty()) {
                values = new String[str.length()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = str.charAt(i) + "";
                }
            } else {
                values = str.split(arraySeparator);
            }

            // Convert all items:
            for (String v : values) {
                if (v == null || v.equalsIgnoreCase(strNullValue)) {
                    list.add(null);
                } else {
                    list.add(convertPrimary(v));
                }
            }
        } // If the given object is actually an array, all items are converted and added in the list:
        else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            for (Object o : array) {
                list.addAll(convertIntoArray(o));
            }
        } // Otherwise the given object is supposed to be a single value:
        else {
            list.add(convertPrimary(value));
        }

        return list;
    }

    /**
     * Converts/Casts the given object into an object of type T.
     * 
     * @param value	The value to cast. (MAY BE NULL)
     * 
     * @return		The casted value.
     * 
     * @throws BinaryInterpreterException	If there is an error while converting the given value.
     */
    protected abstract T convertPrimary(final Object value) throws BinaryInterpreterException;

    /**
     * <p>Encodes a single value of type T in binary.</p>
     * 
     * <p>NOTE: If the given value is null, {@link #getPadding()} will be returned.</p>
     * 
     * @param value	The value to encode. (MAY BE NULL).
     * 
     * @return		The value encoded in binary.
     * 
     * @throws BinaryInterpreterException	If there is an error while encoding the given value.
     */
    public abstract byte[] encodePrimary(final T value) throws BinaryInterpreterException;


    /* **************************************** */
    /* SAVOT_FIELD --> BINARY_FIELD_INTERPRETER */
    /* **************************************** */
    /**
     * <p>Creates the BinaryFieldInterpreter corresponding to the given {@link SavotField}.</p>
     * 
     * <h3>Data type</h3>
     * <ul>
     * 	<li>Accepted data-types are: boolean, bit, unsignedByte, char, unicode char, short, int,
     * 								 long, float, double, floatComplex, doubleComplex.</li>
     * 	<li>Another data-type will be considered as: char[*].</li>
     * </ul>
     * 
     * <h3>Array size</h3>
     * <ul>
     * 	<li>The array-size can be multi-dimensional (each dimension separated by a 'x' character)
     * 		and variable (specified by a '*' character) on the last dimension.</li>
     * 	<li>Delimited dimension (i.e. 12*) will be considered as variable dimension (so 12* => *).</li>
     * 	<li>A negative, null or non-numeric value will be considered as variable (*).</li>
     * </ul>
     * 
     * <h3>Values</h3>
     * <p>
     * 	Only the <code>null</code> attribute is managed. It indicates the string representation of a <code>null</code> value.
     * 	It is used to identify a <code>null</code>s and to write them while converting a cell value in string (see {@link #convertToString(Object)}).
     * </p>
     * 
     * @param field		A {@link SavotField}.
     * 
     * @return			Its corresponding BinaryFieldInterpreter.
     * 
     * @throws BinaryInterpreterException	If there is an error while creating the interpreter.
     */
    public static BinaryFieldInterpreter<?> createInterpreter(SavotField field) throws BinaryInterpreterException {
        // 1. EXTRACTS THE DATA TYPE FROM THIS FIELD:
        String type = field.getDataType();
        if (type == null) {
            type = "";
        }

        // 2. EXTRACTS THE ARRAY SIZE IF ANY:
        int[] arraysizes;
        // no array size => 1 item of T
        if (field.getArraySize().trim().isEmpty()) {
            arraysizes = new int[]{1};
        } // otherwise, interpret it completely:
        else {
            // If there are several dimensions, they are separated by a 'x' character.
            String[] sizesStr = field.getArraySize().trim().split("x");
            arraysizes = new int[sizesStr.length];
            for (int i = 0; i < sizesStr.length; i++) {
                // CASE 1: variable dimension. It is specified by a '*' character.
                if (sizesStr[i].endsWith("*")) {
                    arraysizes[i] = -1;
                } // CASE 2: defined dimension. Must be a positive numeric value.
                else {
                    try {
                        arraysizes[i] = Integer.parseInt(sizesStr[i]);
                        if (arraysizes[i] <= 0) {
                            System.err.println("Warning: an array-size must be positive and non-null => \"" + arraysizes[i] + "\" in the array-size \"" + field.getArraySize() + "\" of \"" + field.getName() + "\" will be considered as variable !");
                            arraysizes[i] = -1;
                        }
                    } catch (NumberFormatException nfe) {
                        /* 2 cases:
                         * - the array-size is not numeric => syntax error.
                         * - the dimension is delimited. For instance: "12*", which means "at least 12 items and maybe more".
                         * In both cases, the array-size will be considered as variable (the same as '*').
                         */
                        arraysizes[i] = -1;
                        System.err.println("Warning: undefined array-size \"" + sizesStr[i] + "\" (in \"" + field.getArraySize() + "\") for \"" + field.getName() + "\". Supposed to be \"*\".");
                    }
                }
                // ONLY THE LAST DIMENSION CAN BE VARIABLE !
                if (arraysizes[i] == -1 && i != sizesStr.length - 1) {
                    arraysizes = new int[]{-1};
                    System.err.println("Warning: incorrect arraysize syntax \"" + field.getArraySize() + "\" for \"" + field.getName() + "\". Only the last dimension can be variable in length ! This field will be considered as an array of \"" + field.getDataType() + "\" with only one dimension variable in length.");
                    break;
                }
            }
        }

        // 3. BUILD THE INTERPRETER CORRESPONDING TO THE EXTRACTED DATA-TYPE AND CONSIDERING THE INTERPRETED ARRAY-SIZE:
        BinaryFieldInterpreter<?> interpreter;
        if (type.equalsIgnoreCase("boolean")) {
            interpreter = new BooleanInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("bit")) {
            interpreter = new BitInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("unsignedByte")) {
            interpreter = new UnsignedByteInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("char")) {
            interpreter = new CharInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("unicodeChar")) {
            interpreter = new UnicodeCharInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("short")) {
            interpreter = new ShortInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("int")) {
            interpreter = new IntegerInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("long")) {
            interpreter = new LongInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("float")) {
            interpreter = new FloatInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("double")) {
            interpreter = new DoubleInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("floatComplex")) {
            interpreter = new FloatComplexInterpreter(arraysizes);
        } else if (type.equalsIgnoreCase("doubleComplex")) {
            interpreter = new DoubleComplexInterpreter(arraysizes);
        } else {
            System.err.println("Warning: missing type attribute for \"" + field.getName() + "\". Supposed to be char(" + arraySizeToString(arraysizes) + ") !");
            interpreter = new CharInterpreter(arraysizes);
        }

        // 4. SET THE STRING REPRESENTATION OF A NULL VALUE IF NEEDED:
        if (field.getValues() != null && field.getValues().getNull() != null && !field.getValues().getNull().equalsIgnoreCase(interpreter.arraySeparator)) {
            interpreter.strNullValue = field.getValues().getNull();
        }

        return interpreter;
    }

    /* ******************************** */
    /* ARRAY SIZE INTERPRETATION METHOD */
    /* ******************************** */
    /**
     * Lets serialize the given array size in String.
     * 
     * @param arraysize	Array-size to serialize.
     * 
     * @return			Its serialization.
     */
    public static String arraySizeToString(final int[] arraysize) {
        /* TODO: define initial capacity or recycle StringBuilder instances */
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < arraysize.length; i++) {
            if (i > 0) {
                str.append('x');
            }
            if (arraysize[i] <= 0) {
                str.append('*');
            } else {
                str.append(arraysize[i]);
            }
        }
        return str.toString();
    }
}
