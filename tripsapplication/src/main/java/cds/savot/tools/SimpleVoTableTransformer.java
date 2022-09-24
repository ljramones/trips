package cds.savot.tools;

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

import cds.savot.model.*;
import cds.savot.stax.SavotStaxParser;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * Very simple VOTable converter to CSV (comma) or TSV (tab)
 * but very efficient for large tables (ROWREAD parsing mode: row per row processing)
 * <p>
 * Designed to use with JSR-173 compliant (Streaming Api for XML)
 * </p>
 *
 * @author bourgesl
 */
public final class SimpleVoTableTransformer {

    /**
     * Logger associated to SavotStaxParser classes
     */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SimpleVoTableTransformer.class.getName());
    /**
     * table size threshold to show progress information
     */
    private final static int LARGE_TABLE_THRESHOLD = 5000;

    /**
     * @return new VOTable converter to CSV format
     */
    public static SimpleVoTableTransformer newCSVTransformer() {
        return new SimpleVoTableTransformer(',');
    }

    /**
     * @return new VOTable converter to TSV format
     */
    public static SimpleVoTableTransformer newTSVTransformer() {
        return new SimpleVoTableTransformer('\t');
    }

    /**
     * Basic TSV transformer
     *
     * @param argv file paths: [inputFile] [outputFile]
     */
    public static void main(String[] argv) {
        if (argv.length < 2) {
            logger.log(Level.SEVERE, "Missing args: {0} <inputFile> <outputFile> !", SimpleVoTableTransformer.class.getName());
            return;
        }
        final String inputFile = argv[0];
        final String outputFile = argv[1];
        logger.info(String.format("Transforming '%s' to '%s' ...", inputFile, outputFile));
        try {
            newTSVTransformer().transform(inputFile, outputFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "fatal error", e);
        }
    }


    /* members */
    private final char separator;

    /**
     * Public constructor
     *
     * @param separator any separator character between values
     */
    public SimpleVoTableTransformer(final char separator) {
        this.separator = separator;
    }

    private void transform(final String inFile, final String outputFile) throws IOException, XMLStreamException {

        final long start = System.nanoTime();

        final SavotStaxParser parser = new SavotStaxParser(inFile, SavotStaxParser.ROWREAD);

        // start parsing document and get first TR if data are present:
        SavotTR tr = parser.getNextTR();

        // Get the VOTable
        final SavotVOTable savotVoTable = parser.getVOTable();
        final ResourceSet resources = savotVoTable.getResources();

        SavotResource resource = null;
        SavotTable table = null;

        if (resources.getItemCount() == 1) {
            // VOTable must have 1 resource:
            resource = resources.getItemAt(0);

            if (Objects.requireNonNull(resource).getTables().getItemCount() == 1) {
                // resource must have ony 1 table:
                table = resource.getTables().getItemAt(0);
            }
        }

        // check that the votable has one resource / table containing groups and fields:
        if (resource == null || table == null) {
            throw new IllegalArgumentException("Incorrect VOTable format (1 mandatory table) !");
        }

        final char sep = separator;

        final FileOutputStream outputStream = new FileOutputStream(new File(outputFile));

        // 64K text buffer
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream), 64 * 1024)) {
            bw.append("#TABLE: ").append(inFile).write("\n");

            writeDescription(bw, table.getDescription());

            writeParamSet(bw, table.getParams());

            final FieldSet fields = table.getFields();
            writeFieldSet(bw, fields);

            final int tableRows = table.getNrowsValue(); // optional
            final int rowLen = fields.getItemCount();

            int nRow = 0, i, tdLen;
            String value;
            TDSet row;

            // step for progress info for very large tables:
            final int step = (tableRows > LARGE_TABLE_THRESHOLD) ? tableRows / 20 : Integer.MAX_VALUE;

            // Iterate on rows:
            do {
                if ((nRow % step == 0) && (nRow != 0)) {
                    // progress bar:
                    logger.info(String.format("processing row: %d / %d ...", nRow, tableRows));
                }

                // Get the data corresponding to the current row
                // note: use TDSet that ensures range checks
                row = tr.getTDSet();

                tdLen = row.getItemCount();

                for (i = 0; i < rowLen; i++) {
                    // Add separator
                    if (i != 0) {
                        bw.write(sep);
                    }
                    if (i < tdLen) {
                        // write the value directly:
                        value = row.getRawContent(i);
                        if (value != null) {
                            bw.write(value);
                        }
                    }
                }

                bw.write('\n');

                nRow++;

            } while ((tr = parser.getNextTR()) != null);

            logger.info(String.format("transform: %d rows processed in %.3f ms.", nRow, 1e-6d * (System.nanoTime() - start)));
        } finally {
            parser.close();
        }
    }

    private void writeDescription(final BufferedWriter bw, final String description) throws IOException {
        if (description.length() != 0) {
            final StringTokenizer t = new StringTokenizer(description, "\n");
            while (t.hasMoreTokens()) {
                bw.append("#  ").append(t.nextToken()).write('\n');
            }
        }
    }

    private void writeParamSet(final BufferedWriter bw, final ParamSet paramSet) throws IOException {
        bw.write("#\n#PARAMS:\n");

        final int len = paramSet.getItemCount();
        if (len == 0) {
            return;
        }

        SavotParam param;

        // 1. write param IDs:
        for (int i = 0; i < len; i++) {
            param = paramSet.getItemAt(i);

            bw.append("# ").append(param.getName()).append(" = ").append(param.getValue()).write('\n');

            writeDescription(bw, param.getDescription());
        }
    }

    private void writeFieldSet(final BufferedWriter bw, final FieldSet fieldSet) throws IOException {
        bw.write("#\n#FIELDS:\n");

        final int len = fieldSet.getItemCount();
        if (len == 0) {
            return;
        }
        final char sep = separator;
        SavotField field;

        // 1. write field IDs:
        bw.write("# ");
        for (int i = 0; i < len; i++) {
            field = fieldSet.getItemAt(i);

            bw.append(field.getId()).write(sep);
        }
        bw.write('\n');

        // 2. write field names:
        for (int i = 0; i < len; i++) {
            field = fieldSet.getItemAt(i);

            bw.append(field.getName()).write(sep);
        }
        bw.write('\n');

        // 3. write field UCD:
        bw.write("# ");
        for (int i = 0; i < len; i++) {
            field = fieldSet.getItemAt(i);

            bw.append(field.getUcd()).write(sep);
        }
        bw.write('\n');

        // 4. write field unit:
        bw.write("# ");
        for (int i = 0; i < len; i++) {
            field = fieldSet.getItemAt(i);

            bw.append(field.getUnit()).write(sep);
        }
        bw.write('\n');
    }
}
