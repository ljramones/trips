package cds.savot.binary;

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
//Author, Co-Author:  Andre Schaaff (CDS), Laurent Bourges (LAOG)
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * <p>Lets encoding and decoding String with the Base64.</p>
 * 
 * <p><u>Note:</u>
 * 	To encode/decode stream of data, you can also use Base64InputStream and Base64OutputStream.
 * </p>
 * 
 * <p>Examples:</p>
 * <pre>
 * public final static void main(final String[] args) throws Exception {
 * 	String message = "Hi ! If you can read this, the Base64 encoding/decoding has completely worked ! Well done ;-) !";
 * 
 * 	System.out.println("ORIGINAL MESSAGE:\n\""+message+"\"");
 * 
 * 	String encoded, decoded;
 * 
 * 	System.out.println("\nEncoding....");
 * 	encoded = Base64.encodeStr(message);
 * 	System.out.println("ENCODED MESSAGE:\n\""+encoded+"\"");
 * 
 * 	System.out.println("\nDecoding....");
 * 	decoded = Base64.decodeStr(encoded);
 * 	System.out.println("DECODED MESSAGE:\n\""+decoded+"\"");
 * }
 * </pre>
 * 
 * @author Gregory Mantelet (CDS)
 * @since 09/2011
 */
public final class Base64 {

    /* package protected fields */
    static final int LINE_LENGTH = 76;
    static final String NEW_LINE = System.getProperty("line.separator");
    static final char[] base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    static final char PADDING_CHAR = '=';
    static final int[] base64decode = new int[256];

    static {
        Arrays.fill(base64decode, -1);
        for (int i = 0, iS = base64code.length; i < iS; i++) {
            base64decode[base64code[i]] = i;
        }
        base64decode[PADDING_CHAR] = 0;
    }

    private Base64() {
    }

    /* ************ BASE 64 ENCODING ************ */
    /**
     * Encodes the given string in Base64.
     * 
     * @param string	The string to encode.
     * 
     * @return		Its Base64 encoding.
     * 
     * @see #encodeStr(String, String)
     */
    public static String encodeStr(final String string) {
        return encodeStr(string, null);
    }

    /**
     * Encodes the given string in Base64.
     * 
     * @param string		The string to encode <i>(string supposed to be encoded with the given {@link Charset charset})</i>.
     * @param charset		The name of a supported {@link Charset charset} (i.e. 'UTF-8').
     * 
     * @return			Its Base64 encoding.
     * 
     * @see #encode(byte[])
     */
    public static String encodeStr(final String string, final String charset) {
        byte[] byteArray;
        try {
            byteArray = (charset == null) ? string.getBytes() : string.getBytes(charset);  // use appropriate encoding string!
        } catch (UnsupportedEncodingException ignored) {
            byteArray = string.getBytes();  // use local default rather than croak
        }
        return encode(byteArray);
    }

    /**
     * Encodes the given bytes array in Base64 characters.
     * 
     * @param byteArray	Data to encode.
     * 
     * @return	The encoded data.
     */
    public static String encode(final byte[] byteArray) {
        /* TODO: define intial capacity or recycle StringBuilder instances */
        final StringBuilder encoded = new StringBuilder();

        // 1st STEP: Pad the given bytes array with 0 so that ALL bytes can be read in groups of 3 bytes:
        final int nbRealBytes = byteArray.length;
        int paddingCount = (3 - (nbRealBytes % 3)) % 3;
        byte[] paddedByteArray = zeroPad(nbRealBytes + paddingCount, byteArray);

        for (int i = 0; i < paddedByteArray.length; i += 3) {
            // 2nd STEP: group 3 bytes (=> 3*8 = 24 bits):
            int group = ((paddedByteArray[i] & 0xff) << 16);
            group |= ((paddedByteArray[i + 1] & 0xff) << 8);
            group |= (paddedByteArray[i + 2] & 0xff);

            // 3rd STEP: split this group in 4 sub-groups (of 6 bits) which correspond each to one character in the Base64 alphabet:
            encoded.append(base64code[(group >> 18) & 0x3f]);
            encoded.append(base64code[(group >> 12) & 0x3f]);
            encoded.append((i + 1 >= nbRealBytes) ? Base64.PADDING_CHAR : Base64.base64code[(group >> 6) & 0x3f]);
            encoded.append((i + 2 >= nbRealBytes) ? Base64.PADDING_CHAR : Base64.base64code[group & 0x3f]);
        }

        // 4th step: split the encoded data in lines of LINE_LENGTH (76) characters:
        return splitLines(encoded).toString();
    }

    /**
     * <p>Pads the given bytes array with <code>length</code>-<code>bytes.length</code> 0.</p>
     * 
     * <p><u>In other words:</u>
     * Creates a bytes array of the given length and whose the first items are those given in parameter.
     * All other items are initialized to 0x00.</p>
     * 
     * @param length	Length of the resulting bytes array.
     * @param bytes	First bytes of the resulting bytes array.
     * 
     * @return		The 0-padded array.
     */
    static byte[] zeroPad(final int length, final byte[] bytes) {
        byte[] padded = new byte[length]; // initialized to zero by JVM
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    /**
     * Splits the given encoded data in lines of {@link #LINE_LENGTH} characters.
     * 
     * @param encoded	Data to split in lines.
     * 
     * @return		The line-split data.
     */
    static StringBuilder splitLines(final StringBuilder encoded) {
        for (int i = 0; i < encoded.length(); i += LINE_LENGTH + NEW_LINE.length()) {
            if ((i + LINE_LENGTH) <= encoded.length()) {
                encoded.insert(i + LINE_LENGTH, NEW_LINE);
            }
        }
        return encoded;
    }

    /* ************ BASE 64 DECODING ************ */
    /**
     * Decodes the given string which is supposed to be encoded in Base64.
     * 
     * @param encoded	Message to decode.
     * 
     * @return		The decoded message.
     * 
     * @throws Base64Exception	If the encoded message is corrupted (that's to say: not conform to the Base64 encoding).
     * 
     * @see #decodeStr(String, String)
     */
    public static String decodeStr(final String encoded) throws Base64Exception {
        return decodeStr(encoded, null);
    }

    /**
     * Decodes the given string which is supposed to be encoded in Base64.
     * 
     * @param encoded	Message to decode.
     * @param charset	The name of a supported {@link Charset charset}.
     * 
     * @return			The decoded message <i>(string encoded using the given {@link Charset charset})</i>.
     * 
     * @throws Base64Exception	If the encoded message is corrupted (that's to say: not conform to the Base64 encoding).
     * 
     * @see #decode(String)
     */
    public static String decodeStr(final String encoded, final String charset) throws Base64Exception {
        byte[] bytes = decode(encoded);
        if (bytes == null) {
            return null;
        }

        if (charset != null) {
            try {
                return new String(bytes, charset);
            } catch (UnsupportedEncodingException e) {
                /* ignored */
            }
        }
        return new String(bytes);
    }

    /**
     * Decodes the given string which is supposed to be encoded in Base64.
     * 
     * @param encoded	Data to decode.
     * 
     * @return		The decoded data.
     * 
     * @throws Base64Exception	If the encoded data are corrupted (that's to say: not conform to the Base64 encoding).
     * 
     * @see #decode(char[])
     */
    public static byte[] decode(final String encoded) throws Base64Exception {
        return decode(encoded.toCharArray());
    }

    /**
     * Decodes the given string which is supposed to be encoded in Base64.
     * 
     * @param encoded	Data to decode.
     * 
     * @return		The decoded data.
     * 
     * @throws Base64Exception	If the encoded data are corrupted (that's to say: not conform to the Base64 encoding).
     */
    public static byte[] decode(final char[] encoded) throws Base64Exception {
        int cLength = 0;		// number of valid bytes
        int pad = 0;			// number of padding bytes
        boolean eom = false;	// End Of Message: true if padding character(s) has(ve) been encountered

        // 1st STEP: Count the number of valid characters and of padding characters:
        for (int i = 0; !eom && i < encoded.length; i++) {
            if (base64decode[encoded[i]] > -1) {
                if (pad > 0 && encoded[i] != PADDING_CHAR) {
                    throw new Base64Exception("Encoded message corrupted: \"" + encoded[i] + "\" has been encountered after a padding character (\"" + PADDING_CHAR + "\") !");
                } else {
                    cLength++;
                    if (encoded[i] == PADDING_CHAR) {
                        pad++;
                    }
                }
            } else {
                if (!Character.isWhitespace(encoded[i])) // only white-spaces are ignored
                {
                    System.err.println("Warning: encoded message may be corrupted: unknown base64 character: \"" + encoded[i] + "\" !");
                }
            }

            // EOM if a group of 4 characters ends with padding characters:
            if (pad > 0 && cLength % 4 == 0) {
                eom = true;
            }

            // Check the number padding characters:
            if (pad > 2) {
                throw new Base64Exception("Encoded message corrupted: a message encoding in base64 can end with at most 2 padding characters (\"" + PADDING_CHAR + "\") !");
            }
        }

        // Throw an error if it's not possible to read ALL characters by group of 4:
        if (cLength % 4 != 0) {
            throw new Base64Exception("Encoded message corrupted (only " + cLength + " valid characters) !");
        }

        // 2nd STEP: Build the bytes array which will be filled with decoded data:
        int bLength = ((cLength / 4) * 3) - pad;		// N group of 4 characters => N*3 bytes - P (where P is the number of padding bytes)
        byte[] decoded = new byte[bLength];

        // Decode all valid characters in the built bytes array:
        int nbChar = 0, nbBytes = 0;
        int nbGroupedChars = 0, group = 0;
        for (int i = 0; i < encoded.length && nbChar < cLength; i++) {
            if (base64decode[encoded[i]] > -1) {
                // 3rd STEP: group the 6 last bits of 4 characters:
                group |= ((base64decode[encoded[i]] & 0x3f) << (6 * (3 - nbGroupedChars)));
                nbGroupedChars++;

                // 4th STEP: when 4 characters have been grouped, extract the 3 resulting bytes (=> 4*6 bits = 24 bits = 3*8 bits):
                if (nbGroupedChars == 4) {
                    decoded[nbBytes++] = (byte) ((group >> 16) & 0xff);
                    if (nbBytes < bLength) {		// extract the 2nd byte only if it's not a padding:
                        decoded[nbBytes++] = (byte) ((group >> 8) & 0xff);
                        if (nbBytes < bLength) // extract the 3rd byte only if it's not a padding:
                        {
                            decoded[nbBytes++] = (byte) (group & 0xff);
                        }
                    }
                    group = 0;
                    nbGroupedChars = 0;
                }
                nbChar++;
            }
        }
        return decoded;
    }
}
