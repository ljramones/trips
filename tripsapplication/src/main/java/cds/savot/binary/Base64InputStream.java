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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>A Base64InputStream decodes Base64-encoded bytes read from the given InputStream.</p>
 * 
 * <h3>The Base 64 decoding</h3>
 * <ul>
 * 	<li>The length of Base64-encoded data MUST be a multiple of 4 and MAY end by 1 or 2 padding characters (=).</li>
 * 	<li>Encoded data MAY be splitted in lines of 76 characters.</li>
 * 	<li>Valid Base64 characters are: A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9,+,/.</li>
 * 	<li>Invalid Base64 characters are ignored but a warning message is displayed in the standard error output.</li>
 * </ul>
 * 
 * <h3>Buffer</h3>
 * <p>
 * 	This stream is buffered. That means that several bytes have already been read and decoded from the inner input stream.
 * 	By default the buffer size is: 8192.
 * </p>
 * <p>
 * 	<u>Warning !</u>
 * 	To fill the buffer of a Base64InputStream with N bytes, (N/3)*4 bytes must be fetched from the inner input stream.
 * 	Indeed, with the Base64 encoding, the number of encoded bytes is always greater than the number of the corresponding decoded bytes:
 * 	3 bytes will be encoded by 4 characters encoded on 6 bits (which allows an alphabet of 2^6=64 characters, hence base64).
 * 	<u>Consequently: for a buffer of 8192 bytes, 10924 bytes are needed.</u>
 * </p>
 * <p>
 * 	However the number of bytes stored in the buffer may be less than the size given at the initialization.
 * 	That is to say: the buffer can contain AT MOST 8192 decoded bytes.
 * 	Indeed some bytes coming from the inner input stream may correspond to invalid Base64 characters.
 * 	In this case, they are ignored and so they are not stored in the buffer.
 * </p>
 * 
 * <h3>Warning &amp; Errors</h3>
 * <p>A Base64InputStream writes a warning in the standard error output when:</p>
 * <ul>
 * 	<li>invalid Base64 characters are encountered before the end of the encoded data</li>
 * </ul>
 * 
 * <p>A Base64InputStream throws an exception when:</p>
 * <ul>
 * 	<li>the inner stream ends whereas a group of 4 valid characters/bytes is not complete</li>
 * 	<li>valid Base64 characters are encountered after padding characters</li>
 * 	<li>more than 2 padding characters are encountered</li>
 * </ul>
 * 
 * <h3>Mark &amp; Reset</h3>
 * <p>The mark(int) and reset() methods are not supported in a Base64InputStream.</p>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 * 
 * @see Base64
 */
public final class Base64InputStream extends FilterInputStream {

    /** Default buffer size. */
    private static final int defaultBufferSize = 8192;
    /** Buffer of decoded data. */
    private byte[] buffer;
    /** The size of the block of encoded data to fetch so that filling the buffer. */
    private int fetchSize;
    /** The position (in the buffer) of the next byte to read. */
    private int pos;
    /** The number of buffered decoded bytes. */
    private int count;
    /** Indicates whether the end of the message has been reached. */
    private boolean eom = false;
    private int pad = 0, group = 0, nbGroupedChars = 0;

    public Base64InputStream(final InputStream encodedStream) {
        this(encodedStream, defaultBufferSize);
    }

    /**
     * 
     * @param encodedStream
     * @param bufferSize
     */
    public Base64InputStream(final InputStream encodedStream, final int bufferSize) {
        super(encodedStream);

        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize <= 0");
        }

        buffer = new byte[bufferSize];
        count = 0;
        pos = 0;

        // compute the size of the buffer of encoded data to fetch: (3 decoded bytes => 4 encoded characters/bytes)
        fetchSize = bufferSize / 3;
        if (bufferSize % 3 != 0) {
            fetchSize++;
        }
        fetchSize *= 4;
    }

    /**
     * Check to make sure that this stream has not been closed.
     * 
     * @throws IOException	If the stream is closed.
     */
    private void ensureOpen() throws IOException {
        if (in == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * <p>Fill as completely as possible the buffer.</p>
     * 
     * <p>
     * 	Concretely <code>fetchSize</code> bytes are fetched from the input stream.
     * 	Then bytes are grouped by 4 so that to be decoded in 3 bytes which are finally written in the buffer.
     * </p>
     * 
     * <p><i><u>Note:</u>
     * 	An encoding message is supposed to end with one full group of 4 bytes.
     * 	The 2 last bytes of this group may be a padding character (see {@link Base64#PADDING_CHAR}).
     * 	If these conditions are not respected, an {@link IOException} is thrown.
     * </i></p>
     * 
     * @throws IOException	If the stream is closed or if the encoded message is corrupted.
     */
    private void fill() throws IOException {
        count = pos = 0;

        // Get a block of encoded data:
        byte[] data = new byte[fetchSize];
        int n1 = in.read(data);

        // Return immediately if EOF:
        if (n1 == -1) {
            if (nbGroupedChars > 0) {
                throw new IOException("Encoded message corrupted: unexpected EOF !");
            } else {
                return;
            }
        }

        // Decode all the fetched data:
        for (int i = 0; !eom && i < n1; i++) {
            // If the character is in the Base64 alphabet, add it to the group, else go to the next character:
            if (Base64.base64decode[data[i]] > -1) {
                if (pad > 0 && data[i] != Base64.PADDING_CHAR) {
                    throw new IOException("Encoded message corrupted: \"" + ((char) data[i]) + "\" has been encountered after a padding character (\"" + Base64.PADDING_CHAR + "\") !");
                } else {
                    group |= ((Base64.base64decode[data[i]] & 0x3f) << (6 * (3 - nbGroupedChars)));
                    nbGroupedChars++;
                    if (data[i] == Base64.PADDING_CHAR) {
                        pad++;
                    }
                }
            } else {
                if (!Character.isWhitespace(data[i])) // only white-spaces are ignored !
                {
                    System.err.println("Warning: encoded message may be corrupted: unknown base64 character encountered: \"" + ((char) data[i]) + "\" !");
                }
            }

            // If 4 characters have been grouped, extract the 3 encoded bytes:
            if (nbGroupedChars == 4) {
                if (pad > 0) {
                    eom = true;	// padding characters => End Of Message !
                }
                if (pad > 2) {
                    throw new IOException("Encoded message corrupted: a message encoding in base64 can end with at most 2 padding characters (\"" + Base64.PADDING_CHAR + "\") !");
                }

                buffer[count++] = (byte) (group >> 16);
                if (pad <= 1) {
                    buffer[count++] = (byte) (group >> 8);
                    if (pad <= 0) {
                        buffer[count++] = (byte) group;
                    }
                }
                group = 0;
                nbGroupedChars = 0;
            }
        }
    }

    @Override
    public int available() throws IOException {
        ensureOpen();
        return (count - pos) + ((in.available() * 6) / 8); // just an estimation (white spaces, wrong characters and padding are not considered)
    }

    @Override
    public int read() throws IOException {
        if (pos >= count) {
            ensureOpen();
            fill();
            if (pos >= count) {
                return -1;
            }
        }
        return buffer[pos++] & 0xff;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();

        if (b == null) {
            throw new NullPointerException("The given byte buffer is NULL !");
        }

        if (off < 0) {
            throw new ArrayIndexOutOfBoundsException("The offset parameter is negative (" + off + ") !");
        } else if (off >= b.length) {
            throw new ArrayIndexOutOfBoundsException("The offset parameter (" + off + ") is greater than the buffer size (" + b.length + ") !");
        }

        if (len == 0) {
            return 0;
        } else if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("The length parameter is negative (" + len + ") !");
        } else if ((off + len) > b.length) {
            throw new ArrayIndexOutOfBoundsException("Impossible to store " + len + " bytes in a byte array whose the size is " + b.length + " from the " + off + "-th item !");
        }

        int nbReadBytes = 0;
        int avail, remLen, cnt;
        while (nbReadBytes < len) {
            if (pos >= count) {
                fill();
            }

            avail = (count - pos);
            if (avail <= 0) {
                return (nbReadBytes == 0) ? -1 : nbReadBytes;
            }
            remLen = (len - nbReadBytes);
            cnt = Math.min(remLen, avail);

            System.arraycopy(buffer, pos, b, (off + nbReadBytes), cnt);

            nbReadBytes += cnt;
            pos += cnt;
        }

        return nbReadBytes;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("Number of bytes to skip < 0");
        }

        if (n == 0) {
            return 0;
        }

        long nbSkipped = 0;

        long nbIteration = n / buffer.length;
        byte[] skipped = new byte[buffer.length];
        int nbRead;
        for (long i = 0; i < nbIteration; i++) {
            nbRead = read(skipped);
            if (nbRead == -1) {
                return nbSkipped;
            }
            nbSkipped += nbRead;
        }

        skipped = new byte[(int) (n % buffer.length)];
        nbRead = read(skipped);
        if (nbRead > 0) {
            nbSkipped += nbRead;
        }

        return nbSkipped;
    }

    @Override
    public void close() throws IOException {
        if (in == null) {
            return;
        }
        in.close();
        in = null;
        buffer = null;
        pos = count = 0;
    }

    /* MARK & RESET NOT SUPPORTED */
    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("Mark not supported in a Base64InputStream !");
    }
}
