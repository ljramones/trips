package cds.savot.binary;

//Copyright 2002-2014 - UDS/CNRS
//The SAVOT library is distributed under the terms
//of the GNU General Public License version 3.
//
//This file is part of SAVOT.
//
// SAVOT is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// SAVOT is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// The GNU General Public License is available in COPYING file
// along with SAVOT.
//
//SAVOT - Simple Access to VOTable - Parser
//
//Author, Co-Author:  Andre Schaaff (CDS), Laurent Bourges (LAOG)
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>A Base64OutputStream encodes given bytes in Base64 characters and writes them in the given OutputStream.</p>
 * 
 * <h3>The Base 64 encoding</h3>
 * <ul>
 * 	<li>According to the RFC-2045, valid Base64 characters are:
 * 		A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9,+,/.</li>
 * 	<li>Encoded data ARE splitted in lines of 76 characters, using the local line separator (System.getProperty("line.separator")).</li>
 * </ul>
 * 
 * <h3>Buffer</h3>
 * <p>
 * 	This stream is buffered. That means that encoded bytes sent to the inner output stream will be written when a given number of bytes are collected.
 * 	By default the buffer size is: 8192.
 * </p>
 * <p>
 * 	<u>Warning !</u>
 * 	Actually the buffer is not managed in Base64OutputStream but in its inner output stream. At the initialization the given output stream is used
 * 	to create a {@link BufferedOutputStream} with (N/3)*4 as buffer size (where N is the given buffer size).
 * 	Indeed, with the Base64 encoding, the number of encoded bytes is always greater than the number of the corresponding decoded bytes:
 * 	3 bytes will be encoded by 4 characters encoded on 6 bits (which allows an alphabet of 2^6=64 characters, hence base64).
 * 	Consequently: a buffer of N bytes in a BufferedOutputStream corresponds to a buffer of (N/3)*4 bytes in its inner output stream.
 * 	<u>So, when you set a buffer size of 8192 bytes at the creation of a Base64OutputStream, it is really implemented by a buffer of 10924 bytes.</u>
 * </p>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 * 
 * @see Base64
 */
public final class Base64OutputStream extends FilterOutputStream {

    /** Default buffer size. */
    private static final int defaultBufferSize = 8192;
    /** Group of 3 bytes. */
    private int group = 0;
    /** Number of grouped bytes (0&le;bytecounter&le;3) */
    private int bytecounter = 0;
    /** Number of bytes on the current line. After {@link Base64#LINE_LENGTH} bytes a {@link Base64#NEW_LINE} is added. */
    private int linecounter = 0;

    /**
     * 
     * @param stream
     */
    public Base64OutputStream(final OutputStream stream) {
        this(stream, defaultBufferSize);
    }

    /**
     * 
     * @param stream
     * @param bufferSize
     */
    public Base64OutputStream(final OutputStream stream, final int bufferSize) {
        super(new BufferedOutputStream(stream, encodedDataSize(bufferSize)));
    }

    /**
     * Computes the size of the encoded data (3 decoded bytes => 4 encoded characters/bytes).
     * 
     * @param nbDataToEncode	Size of the data to encode.
     * 
     * @return					Size of the encoded data.
     */
    static int encodedDataSize(final int nbDataToEncode) {
        if (nbDataToEncode <= 0) {
            return 0;
        }

        int encodedSize = nbDataToEncode / 3;
        if (nbDataToEncode % 3 != 0) {
            encodedSize++;
        }
        encodedSize *= 4;

        return encodedSize;
    }

    @Override
    public void write(int b) throws IOException {
        group |= ((b & 0xFF) << (16 - (bytecounter * 8)));
        bytecounter++;
        if (bytecounter == 3) {
            commit();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (b == null) {
            throw new NullPointerException("The given byte buffer is NULL !");
        }

        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("The given byte buffer is NULL !");
        }

        if (off < 0) {
            throw new ArrayIndexOutOfBoundsException("The offset parameter is negative (" + off + ") !");
        } else if (off >= b.length) {
            throw new ArrayIndexOutOfBoundsException("The offset parameter (" + off + ") is greater than the buffer size (" + b.length + ") !");
        }

        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("The length parameter is negative (" + len + ") !");
        } else if ((off + len) > b.length) {
            throw new ArrayIndexOutOfBoundsException("Impossible to read " + len + " bytes from a byte array whose the size is " + b.length + " from the " + off + "-th item !");
        }

        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }

    /**
     * It commits 4 bytes to the underlying stream.
     * 
     * @throws IOException If an error occurs while writing bytes thanks to the inner output stream.
     */
    private void commit() throws IOException {
        if (bytecounter > 0) {
            if (Base64.LINE_LENGTH > 0 && linecounter == Base64.LINE_LENGTH) {
                out.write(Base64.NEW_LINE.getBytes());
                linecounter = 0;
            }
            linecounter += 4;

            out.write(Base64.base64code[(group >> 18) & 0x3f]);
            out.write(Base64.base64code[(group >> 12) & 0x3f]);
            out.write((bytecounter < 2) ? Base64.PADDING_CHAR : Base64.base64code[(group >> 6) & 0x3f]);
            out.write((bytecounter < 3) ? Base64.PADDING_CHAR : Base64.base64code[group & 0x3f]);

            bytecounter = 0;
            group = 0;
        }
    }

    /**
     * ONLY FLUSH THE INNER OUTPUT STREAM !
     * 
     * @throws IOException
     * @see FilterOutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Decodes and writes last given bytes
     * and finally flushes and closes the inner output stream.
     * 
     * @throws IOException
     * @see FilterOutputStream#close()
     */
    @Override
    public void close() throws IOException {
        commit();
        out.flush();
        out.close();
    }
}
