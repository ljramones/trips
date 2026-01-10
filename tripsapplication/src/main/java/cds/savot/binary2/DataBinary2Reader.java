package cds.savot.binary2;

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

import cds.savot.binary.Base64InputStream;
import cds.savot.binary.SavotDataReader;
import cds.savot.model.*;
import cds.savot.model.interpreter.BinaryFieldInterpreter;
import cds.savot.model.interpreter.BinaryInterpreterException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * <p>Lets read binary data (that is to say: a votable.resource.table.data.binary node).</p>
 * 
 * <p>
 * 	A DataBinaryReader must be initialized with a {@link SavotStream} object which contains all information to access and read the data,
 * 	and with a {@link FieldSet} which lists all fields (or cells) that are expected in the data.
 * 	A {@link SavotStream} has several attributes that are more or less managed by a DataBinaryReader:
 * </p>
 * <ul>
 * 	<li><b>href</b>: path to the resource which contains the binary data. This attribute is <u>fully MANAGED</u> !</li>
 * 	<li><b>encoding</b>: it specifies how binary data have been encoded. This attribute is <u>MANAGED WITH the value "base64", "gzip" and "dynamic"</u> !</li>
 * 	<li><b>expires</b>: data expiration date. After this date, data are supposed to be not valid any more. This attribute is <u>fully MANAGED</u> and can be ignored on demand !</li>
 * 	<li><b>rights</b>: it expresses authentication information (i.e. password). This attribute is <u>NOT MANAGED</u> !</li>
 * 	<li><b>actuate</b>: it indicates when data have to be fetched (onRequest (by default) or onLoad). This attribute is <u>NOT MANAGED</u> !</li>
 * </ul>
 * 
 * <h3>HREF attribute</h3>
 * <p>
 * 	The following protocols are accepted in the "href" attribute: http, https, httpg (not tested at all), ftp and file.
 * 	If the "href" attribute contains a relative path to a local file, the parent directory must be specified to the reader.
 * </p>
 * 
 * <h3>Encoding attribute</h3>
 * <p>
 * 	The following STREAM encoding are managed: base64, gzip, dynamic.
 * 	If no encoding is specified, the data will be merely considered as just binary data.
 * </p>
 * <p>
 * 	The "dynamic" encoding implies that the data is in a remote resource (specified by the "href" attribute),
 * 	and the encoding will be delivered with the header of the data. This occurs with the http protocol,
 * 	where the MIME header (http header field "Content-Encoding") indicates the type of encoding that has been used.
 * 	In this case only base64 and gzip are accepted.
 * </p>
 * 
 * <h3>Expires attribute</h3>
 * <p>
 * 	If the date given by this attribute has been reached, no data will be read and an {@link IOException} will be thrown.
 * 	However, this attribute can be ignored at the creation of a DataBinaryReader if needed.
 * </p>
 * 
 * @author Gregory Mantelet (CDS), Andre Schaaff (CDS) for Binary2 add 
 * @since 09/2011
 */
public final class DataBinary2Reader implements SavotDataReader {

    /**  Regular expression of a valid URL (http, https, httpg, ftp, file) for the "href" attribute. */
    private static final String URL_REGEXP = "^(http([sg])?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    /** Valid date format (Universal Time ; ISO-8601) for the "expires" attribute. */
    private static final DateFormat EXPIRES_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    /** Stream toward the (decoded) binary data. */
    private InputStream data;
    /** List of decoders: one per available cell. */
    private BinaryFieldInterpreter<?>[] decoders;
    /** The last read row (the length of the array is equal to the number of decoders). */
    private Object[] row;
    /** Indicates whether the next() function has yet been called. */
    private boolean nextCalled = false;
    /** Indicates whether the end of the file has been reached. */
    private boolean eof = false;
    /** Binary2 (VOTABLE 1.3) null management through a number of Bytes = (nb columns + 7) / 8 for each line in the stream */
    private int nullBytes = 0;

    /* CONSTRUCTORS */
    /**
     * <p>Builds a DataBinaryReader with a {@link SavotStream}.</p>
     * 
     * <p>NOTE: The expiration date is NOT ignored and there is no parent directory.</p>
     * 
     * @param stream	The {@link SavotStream} which contains data to read.
     * @param fields	List of fields metadata (one per cell).
     * 
     * @throws IOException    If an error occurs while building the input stream.
     *
     */
    public DataBinary2Reader(final SavotStream stream,  final FieldSet fields) throws IOException {
        this(stream, fields, false, null);
    }

    /**
     * <p>Builds a DataBinaryReader with a {@link SavotStream}.</p>
     * 
     * <p>NOTE: There is no parent directory.</p>
     * 
     * @param stream			The {@link SavotStream} which contains data to read.
     * @param fields			List of fields metadata (one per cell).
     * @param ignoreExpiryDate	<i>true</i> to ignore the "expires" attribute, <i>false</i> otherwise.
     * 
     * @throws IOException		If an error occurs while building the input stream.
     *
     */
    public DataBinary2Reader(final SavotStream stream,  final FieldSet fields, final boolean ignoreExpiryDate) throws IOException {
        this(stream, fields, ignoreExpiryDate, null);
    }

    /**
     * <p>Builds a DataBinaryReader with a {@link SavotStream}.</p>
     * 
     * @param stream			The {@link SavotStream} which contains data to read.
     * @param fields			List of fields metadata (one per cell).
     * @param ignoreExpiryDate	<i>true</i> to ignore the "expires" attribute, <i>false</i> otherwise.
     * @param parentDirectory	Directory which contains the data file if the path given in the "href" attribute is relative.
     * 
     * @throws IOException		If an error occurs while building the input stream.
     * 
     * @see #getData(SavotStream, boolean, String)
     */
    public DataBinary2Reader(final SavotStream stream,  final FieldSet fields, final boolean ignoreExpiryDate, final String parentDirectory) throws IOException {
        this(fields);
        data = getData(stream, ignoreExpiryDate, parentDirectory);
    }

    /**
     * <p>Builds a DataBinaryReader with an {@link InputStream} on the ENCODED data.</p>
     * 
     * @param encodedData	Encoded data.
     * @param encoding		Encoding of the data (base64, gzip or <code>null</code>).
     * @param fields		List of fields metadata (one per cell).
     * 
     * @throws IOException	If an error occurs while building the input stream.
     * 
     * @see #getDecodedStream(InputStream, String)
     */
    public DataBinary2Reader(final InputStream encodedData, final String encoding,  final FieldSet fields) throws IOException {
        this(fields);
        data = getDecodedStream(encodedData, encoding);
    }

    /**
     * <p>Builds a DataBinaryReader with an {@link InputStream} on the DECODED data.</p>
     * 
     * @param decodedData	Decoded data.
     * @param fields		List of fields metadata (one per cell).
     * 
     * @throws BinaryInterpreterException	If it is impossible to build a field decoder.
     *
     */
    public DataBinary2Reader(final InputStream decodedData,  final FieldSet fields) throws BinaryInterpreterException {
        this(fields);
        data = decodedData;
    }

    /**
     * <p>
     * 	Builds one binary decoder per field and initializes the variable
     * 	which aims to contains the read row after a {@link #next()} call.
     * </p>
     * 
     * <p>
     * 	WARNING: The {@link #data} attribute is not initialized !
     * 	This constructor is just designed to initialize the decoders list.
     * 	<u>Thus it must be called by another constructor which will be able
     * 	to initialize the data input stream.</u>
     * </p>
     * 
     * @param	fields				List of fields metadata (one per cell).
     * 
     * @throws	BinaryInterpreterException	It it is impossible to build a field decoder.
     * 
     * @see 	BinaryFieldInterpreter#createInterpreter(SavotField)
     */
    protected DataBinary2Reader(final FieldSet fields) throws BinaryInterpreterException {
    	
        int nFields = fields.getItemCount();

        // Binary2 management by adding nbNullBytes Virtual fields, nbNullBytes = (initial field count + 7 ) / 8
		nullBytes = (fields.getItemCount() + 7) / 8;
		for (int identNull=0; identNull < nullBytes; identNull++) {
			SavotField thenull = new SavotField();
			thenull.setDataType("unsignedByte");
			thenull.setName("NullByte" + identNull);
			fields.set.add(identNull,thenull);
		} 
		// new number of fields
		nFields+=nullBytes;
        decoders = new BinaryFieldInterpreter<?>[nFields];
        for (int i = 0; i < nFields; i++) {
            decoders[i] = BinaryFieldInterpreter.createInterpreter(Objects.requireNonNull(fields.getItemAt(i)));
        }
        // removing of the Virtual fields to avoid problems with the field set 
        // we know now the number of null Bytes
        for (int i = 0; i < nullBytes; i++) {
        		fields.removeItemAt(0);
        }
        row = new Object[nFields];
    }

    /* STREAM BUILDING METHODS */
    /**
     * Builds an input stream on decoded binary data with the given {@link SavotStream}.
     * 
     * @param stream			The {@link SavotStream} which contains all information to get the (encoded or not) binary data.
     * @param ignoreExpiryDate	<i>true</i> to ignore the expiration date, <i>false</i> otherwise.
     * @param parentDirectory	Directory which contains the data file if the path given in the "href" attribute is relative.
     * 
     * @return		An input stream which reads the encoded binary data and returns the corresponding decoded binary data.
     * 
     * @throws IOException	If the expiration date has been reached, if the given encoding is unknown
     * 						or if there is an error while building the input stream.
     * 
     * @see #getInputFromURL(URL, String)
     * @see #getDynamicEncoding(URL, String)
     * @see #getDecodedStream(InputStream, String)
     */
    private InputStream getData(final SavotStream stream, final boolean ignoreExpiryDate, final String parentDirectory) throws IOException {
        // 1. CHECK THE DATA VALIDITY THANKS TO THE EXPIRES ATTRIBUTE:
        if (!ignoreExpiryDate && stream.getExpires() != null) {
            String expires = stream.getExpires().trim();
            if (!expires.isEmpty()) {
                try {
                    Date expiryDate = EXPIRES_DATE_FORMAT.parse(expires);
                    if (expiryDate.before(new Date())) {
                        throw new IOException("Data are not valid any more (expiry date: \"" + expires + "\") !");
                    }
                } catch (ParseException pe) {
                    System.out.println("Warning: unknown date format (" + expires + "): the stream expiry date will be ignored !");
                }
            }
        }

        InputStream data = null;
        String encoding = (stream.getEncoding() == null) ? null : stream.getEncoding().trim();

        // 2. GET THE ENCODED DATA:
        String href = stream.getHref();
        // href data:
        if (href != null && !href.trim().isEmpty()) {
            href = href.trim();
            // URL:
            if (href.matches(URL_REGEXP)) {
                URL url = new URL(href);
                data = getInputFromURL(url, stream.getRights());
                // extract the data encoding, if needed:
                if (encoding != null && encoding.equalsIgnoreCase("dynamic")) {
                    try {
                        encoding = getDynamicEncoding(url, stream.getRights());
                    } catch (IOException ioe) {
                        throw new IOException("Can't fetch the data encoding from the URL \"" + href + "\", because: " + ioe.getMessage(), ioe);
                    }
                }
            } // FILE:
            else {
                File dataFile;
                if (parentDirectory == null) {
                    dataFile = new File(href);
                } else {
                    dataFile = new File(parentDirectory, href);
                }
                data = new FileInputStream(dataFile);
            }
        } // Inline data:
        else {
            data = new ByteArrayInputStream(stream.getContent().getBytes());
        }

        // 3. GET THE DECODED DATA:
        data = getDecodedStream(data, encoding);

        return data;
    }

    /**
     * <p>Gets an input stream on the given URL considering the given rights.</p>
     * 
     * <p>
     * 	WARNING: By default, <code>rights</code> is not considered.
     * 	That is to say, by default what is returned is: <code>dataUrl.openStream()</code>.
     * </p>
     * 
     * @param dataUrl		The URL toward the data.
     * @param rights		Authentication information (i.e. password) needed to open a stream on the given URL.
     * 
     * @return			An input stream on the given URL.
     * 
     * @throws IOException	If there is an error while creating the input stream.
     */
    private InputStream getInputFromURL(final URL dataUrl, final String rights) throws IOException {
        return dataUrl.openStream();
    }

    /**
     * <p>Extracts the encoding of the data from the protocol header.</p>
     * 
     * <p>For instance: if the protocol is HTTP, the header <code>Content-Encoding</code> gives the data encoding.</p>
     * 
     * <p>WARNING: By default, <code>rights</code> is not considered.</p>
     * 
     * @param dataUrl		The URL toward the data.
     * @param rights		Authentication information (i.e. password) needed to open a stream on the given URL.
     * 
     * @return				The name of the data encoding. (may be <code>null</code>).
     * 
     * @throws IOException	If there is an error while opening a connection on the given URL.
     */
    private String getDynamicEncoding(final URL dataUrl, final String rights) throws IOException {
        URLConnection connection = dataUrl.openConnection();
        connection.connect();
        return connection.getContentEncoding();
    }

    /**
     * <p>Gets a stream which decodes data coming from the given input stream.</p>
     * 
     * <p>NOTE: Accepted encoding algorithms are: <code>base64</code> or <code>gzip</code>.</p>
     * 
     * @param encodedStream		Input stream on encoded data.
     * @param encoding			Name of the encoding algorithm (<code>base64</code> or <code>gzip</code>).
     * 
     * @return				An input stream which decodes the encoded data read from the given input stream.
     * 
     * @throws IOException		If there is an error while building the input stream.
     * 
     * @see GZIPInputStream
     */
    private InputStream getDecodedStream(final InputStream encodedStream, final String encoding) throws IOException {
        if (encoding == null || encoding.isEmpty()) {
            return encodedStream;
        } else if (encoding.equalsIgnoreCase("base64")) {
            return new Base64InputStream(encodedStream);
        } else if (encoding.equalsIgnoreCase("gzip")) {
            return new GZIPInputStream(encodedStream);
        } else {
            throw new BinaryInterpreterException("Unknown encoding \"" + encoding + "\" ! It must be either \"base64\" or \"gzip\" !");
        }
    }

    /* INHERITED METHODS */
    /**
     * @throws IOException
     */
    @Override
    public boolean next() throws IOException {
        if (row == null) {
            throw new IOException("Reader closed !");
        } else if (eof) {
            return false;
        }

        for (int i = 0; i < row.length; i++) {
            // Reads the value of the i-th cell:
            row[i] = decoders[i].decode(data);

            // EOF detection:
            if (row[i] == null) {	// decode(...) must return an array. If null is returned => EOF !
                // EOF accepted ONLY while getting the first cell of a row:
                if (i == 0) {
                    eof = true;
                    nextCalled = true;
                    return false;
                } // otherwise an exception is thrown !
                else {
                    throw new IOException("Unexpected EOF: the row has not been read completely ; only " + i + " columns on " + row.length + " has been successfully read !");
                }
            }
        }
        nextCalled = true;
        return true;
    }

    /**
     * <p>Ensures that:</p>
     * <ul>
     * 	<li>the input stream is open</li>
     * 	<li>the end of file has not yet been reached</li>
     * 	<li><code>next()</code> has been called.</li>
     * </ul>
     * 
     * @throws IllegalStateException	If at least one check fails.
     */
    private void ensureRowAvailable() throws IllegalStateException {
        if (row == null) {
            throw new IllegalStateException("No more row available: the reader is closed !");
        } else if (eof) {
            throw new IllegalStateException("No more row available: the end of file has been reached !");
        } else if (!nextCalled) {
            throw new IllegalStateException("No row available: next() has not yet been called !");
        }
    }

    /**
     * @return an Object
     * @throws IllegalStateException
     */
    @Override
    public Object[] getRow() throws IllegalStateException {
        ensureRowAvailable();
        return row;
    }

    /**
     * @return a SAVOT TR internal model object
     * @throws IllegalStateException
     */
    @Override
    public SavotTR getTR() throws IllegalStateException {
        ensureRowAvailable();

        SavotTR tr = new SavotTR();
        TDSet tds = new TDSet();
        for (int i = 0; i < row.length; i++) {
            tds.addItem(getTD(i));
        }

        tr.setTDs(tds);
        return tr;
    }

    /**
     * @param indColumn
     * @return Object
     * @throws IndexOutOfBoundsException
     * @throws IllegalStateException
     */
    @Override
    public Object getCell(final int indColumn) throws IndexOutOfBoundsException, IllegalStateException {
        ensureRowAvailable();
        return (row == null) ? null : row[indColumn + nullBytes];
    }

    /**
     * @param indColumn
     * @return String
     * @throws IndexOutOfBoundsException
     * @throws IllegalStateException
     */
    @Override
    public String getCellAsString(final int indColumn) throws IndexOutOfBoundsException, IllegalStateException {
        ensureRowAvailable();
        return (row == null) ? null : decoders[indColumn + nullBytes].convertToString(row[indColumn + nullBytes]);
    }

    /**
     * Test if field bit is set to 1 which means null
     * @param indColumn
     * @return
     * @throws IndexOutOfBoundsException
     * @throws IllegalStateException
     */
    public boolean isCellNull(final int indColumn) throws IndexOutOfBoundsException, IllegalStateException {
        ensureRowAvailable();

        // which Byte, it depends on the column
        int concernedByte = indColumn / 8;
        
        // bit position in the Byte, takes into account the most significant bit 
        int positionInByte = 7 - (indColumn % 8); 
        
        int intForPosition = ((int)Math.pow(2, positionInByte));
		
        int result =  Integer.parseInt(Objects.requireNonNull(getCellAsString(concernedByte - nullBytes))) & intForPosition;
		
        if (result == intForPosition)
        		return true;
        else 
        		return (false);
    }
    
    /**
     * @param indColumn
     * @return SavotTD SAVOT TD internal model object
     * @throws IndexOutOfBoundsException
     * @throws IllegalStateException
     */
    @Override
    public SavotTD getTD(final int indColumn) throws IndexOutOfBoundsException, IllegalStateException {
        ensureRowAvailable();
        SavotTD td = new SavotTD();
        td.setContent(getCellAsString(indColumn));
        return td;
    }

    /**
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        data.close();
        data = null;
        row = null;
        decoders = null;
    }
}
