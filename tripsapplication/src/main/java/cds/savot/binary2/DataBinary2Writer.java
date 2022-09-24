package cds.savot.binary2;

import cds.savot.binary.Base64OutputStream;
import cds.savot.binary.SavotDataWriter;
import cds.savot.model.*;
import cds.savot.model.interpreter.BinaryFieldInterpreter;
import cds.savot.model.interpreter.BinaryInterpreterException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * <p>Lets write binary data (that is to say: a votable.resource.table.data.binary node).</p>
 * 
 * <p>NOTE:
 * 	Accepted encoding algorithms are: <code>base64</code>, <code>gzip</code> or <code>dynamic</code>.
 * 	"dynamic" encoding is accepted but no particular encoding will be applied while writing data. They will be written just in binary.
 * </p>
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public final class DataBinary2Writer implements SavotDataWriter {

    /** Stream in which binary data will be written encoded. */
    private OutputStream output;
    /** List of encoders: one per available cell. */
    private BinaryFieldInterpreter<?>[] encoders;

    /* CONSTRUCTORS */
    /**
     * Builds a DataBinaryWriter with no encoding.
     * 
     * @param rawStream		Simple output stream.
     * @param fields		List of fields metadata (one per cell).
     * @throws cds.savot.cds.savot.model.interpreter.BinaryInterpreterException
     * @throws IOException	If an error occurs while building the output stream.
     * 
     * @see #DataBinaryWriter(OutputStream, FieldSet, String)
     */
    public DataBinary2Writer(final OutputStream rawStream, final FieldSet fields) throws BinaryInterpreterException, IOException {
        this(rawStream, fields, null);
    }

    /**
     * Builds a DataBinaryWriter with a particular encoding (base64 or gzip).
     * 
     * @param rawStream		Simple output stream.
     * @param fields		List of fields metadata (one per cell).
     * @param encoding		Name of an encoding algorithm (base64, gzip or <code>null</code>).
     * 
     * @throws IOException	If an error occurs while building the output stream.
     * 
     * @see #getEncodedStream(OutputStream, String)
     * @see BinaryFieldInterpreter#createInterpreter(SavotField)
     */
    public DataBinary2Writer(final OutputStream rawStream, final FieldSet fields, final String encoding) throws IOException {
        if (rawStream == null) {
            throw new NullPointerException("The given output stream is NULL !");
        }

        this.output = getEncodedStream(rawStream, encoding);

        final int nFields = fields.getItemCount();
        encoders = new BinaryFieldInterpreter<?>[nFields];

        for (int i = 0; i < nFields; i++) {
            encoders[i] = BinaryFieldInterpreter.createInterpreter(Objects.requireNonNull(fields.getItemAt(i)));
        }
    }

    /* STREAM BUILDING METHOD */
    /**
     * <p>Gets a stream which encodes data into the given output stream.</p>
     * 
     * <p>NOTE: Accepted encoding algorithms are: <code>base64</code> or <code>gzip</code>.</p>
     * 
     * @param rawStream			Simple output stream.
     * @param encoding			Name of the encoding algorithm (<code>base64</code> or <code>gzip</code>).
     * 
     * @return					An output stream which encodes data read into the given output stream.
     * 
     * @throws IOException		If there is an error while building the output stream.
     * @throws cds.savot.cds.savot.model.interpreter.BinaryInterpreterException
     * @see Base64OutputStream2
     * @see GZIPOutputStream
     */
    OutputStream getEncodedStream(final OutputStream rawStream, final String encoding) throws IOException, BinaryInterpreterException {
        if (encoding == null || encoding.isEmpty()) {
            return rawStream;
        } else if (encoding.equalsIgnoreCase("base64")) {
            return new Base64OutputStream(rawStream);
        } else if (encoding.equalsIgnoreCase("gzip")) {
            return new GZIPOutputStream(rawStream);
        } else if (encoding.equalsIgnoreCase("dynamic")) {
            return rawStream;
        } else {
            throw new BinaryInterpreterException("Unknown encoding \"" + encoding + "\" ! It must be either \"base64\" or \"gzip\" !");
        }
    }

    /**
     * @param row SavotTR
     * @throws IOException
     * @throws cds.savot.cds.savot.model.interpreter.BinaryInterpreterException
     */
    @Override
    public void writeTR(final SavotTR row) throws IOException, BinaryInterpreterException {
        if (output == null) {
            throw new IOException("Writer closed !");
        }

        final TDSet tds = row.getTDSet();

        for (int i = 0, len = tds.getItemCount(); i < len; i++) {
            final SavotTD td = tds.getItemAt(i);
            encoders[i].encode(output, Objects.requireNonNull(td).getContent());
        }
    }

    /**
     * @param rows TRSet
     * @throws IOException
     * @throws cds.savot.cds.savot.model.interpreter.BinaryInterpreterException
     */
    @Override
    public void writeTRSet(final TRSet rows) throws IOException, BinaryInterpreterException {
        if (output == null) {
            throw new IOException("Writer closed !");
        }

        for (int i = 0, len = rows.getItemCount(); i < len; i++) {
            writeTR(rows.getItemAt(i));
        }
    }

    /**
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        output.flush();
    }

    /**
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        output.close();
        output = null;
        encoders = null;
    }
}
