package cds.savot.writer;

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

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * <p>
 * VOTable document generation from memory
 * </p>
 * 
 * 
 * @author Andre Schaaff
 * 
 *          6 June 2005 : the user can now write a VOTable document flow step by
 *          step, the previous method is available too (writing of a whole
 *          document) (kickoff 31 May 02)
 */
@SuppressWarnings({"deprecation", "UseOfSystemOutOrSystemErr"})
public final class SavotWriter {

    private static final String tdempty = "<TD/>";
    private static final String tdbegin = "<TD>";
    private static final String tdend = "</TD>";
    private static final String trbegin = "<TR>";
    private static final String trend = "</TR>\n";
    private static final String tabledatabegin = "\n<TABLEDATA>\n";
    private static final String tabledataend = "</TABLEDATA>";
    private static final String databegin = "\n<DATA>";
    private static final String dataend = "\n</DATA>";
    private static final String tableend = "\n</TABLE>";
    private static final String resourceend = "\n</RESOURCE>";
    private static final String descriptionbegin = "\n<DESCRIPTION>";
    private static final String descriptionend = "</DESCRIPTION>";
    private static final String groupend = "\n</GROUP>";
    private static final String definitionsbegin = "\n<DEFINITIONS>";
    private static final String definitionsend = "\n</DEFINITIONS>";
    private static final String paramend = "\n</PARAM>";
    private static final String fieldend = "\n</FIELD>";
    private static final String linkend = "</LINK>";
    private static final String valuesend = "\n</VALUES>";
    private static final String fitsend = "\n</FITS>";
    private static final String binarybegin = "\n<BINARY>";
    private static final String binaryend = "\n</BINARY>";
    private static final String coosysend = "</COOSYS>";
    private static final String streamend = "\n</STREAM>";
    private static final String minend = "</MIN>";
    private static final String maxend = "</MAX>";
    private static final String optionend = "\n</OPTION>";

    /* members */
    // default xml top
    private String top1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private String styleSheet = "";
    private boolean attributeEntities = true;
    private boolean elementEntities = true;
    /** buffered writer */
    private Writer bw = null;
    /* Regexp matchers */
    private final Pattern patternAMP = Pattern.compile("&");
    private final Pattern patternQUOT = Pattern.compile("\"");
    private final Pattern patternLT = Pattern.compile("<");
    private final Pattern patternGT = Pattern.compile(">");

    /**
     * Public constructor
     */
    public SavotWriter() {
        // nop
    }

    /**
     * Change the default XML document head Default value <?xml
     * version="1.0 encoding="UTF-8"?>
     * 
     * @param top1
     * @since VOTable 1.2
     * 
     */
    public void setTop1(final String top1) {
        this.top1 = top1;
    }

    /**
     * Set a stylesheet Also possible with setTop1
     * 
     * @param href
     * @since VOTable 1.2
     * 
     */
    public void setStyleSheet(final String href) {
        this.styleSheet = href;
    }

    /**
     * Enable or disable Attribute entities mapping
     * 
     * @param entities
     *            true if Attribute entities are taken into account
     */
    public void enableAttributeEntities(final boolean entities) {
        this.attributeEntities = entities;
    }

    /**
     * Enable or disable Element entities mapping
     * 
     * @param entities
     *            true if Element entities are taken into account
     */
    public void enableElementEntities(final boolean entities) {
        this.elementEntities = entities;
    }

    /**
     * Enable or disable Attribute and Element entities mapping
     * 
     * @param entities
     *            true if all entities are taken into account
     */
    public void enableEntities(final boolean entities) {
        this.attributeEntities = entities;
        this.elementEntities = entities;
    }

    /**
     * Encode special characters to entities
     * @param src
     * @return src
     */
    public String encodeAttribute(final String src) {
        if (attributeEntities) {
            String out = patternAMP.matcher(src).replaceAll("&amp;"); // Character [&] (xml restriction)
            out = patternLT.matcher(out).replaceAll("&lt;"); // Character [<] (xml restriction)
            out = patternGT.matcher(out).replaceAll("&gt;"); // Character [>] (xml restriction)
            out = patternQUOT.matcher(out).replaceAll("&quot;"); // Character ["] (attribute delimiter)
            return out;
        }
        return src;
    }

    /**
     * Encode special characters to entities
     * @param src
     * @return src
     */
    public String encodeElement(final String src) {
        if (elementEntities) {
            String out = patternAMP.matcher(src).replaceAll("&amp;"); // Character [&] (xml restriction)
            out = patternLT.matcher(out).replaceAll("&lt;"); // Character [<] (xml restriction)
            out = patternGT.matcher(out).replaceAll("&gt;"); // Character [>] (xml restriction)
            return out;
        }
        return src;
    }

    /**
     * Generates a VOTable XML document corresponding to the internal model The
     * result is sent to the standard output
     * 
     * @param votable
     *            object corresponding to the savot internal model
     * @throws IOException
     */
    public void generateDocument(final SavotVOTable votable) throws IOException {
        generateDocument(votable, null, null);
    }

    /**
     * Generates a VOTable XML document corresponding to the internal model
     * 
     * @param votable
     *            object corresponding to the savot internal model
     * @param stream
     *            the result is sent to this stream
     * @throws IOException
     */
    public void generateDocument(final SavotVOTable votable, final OutputStream stream)
            throws IOException {
        generateDocument(votable, null, stream);
    }

    /**
     * Generates a VOTable XML document corresponding to the internal model
     * 
     * @param votable
     *            object corresponding to the savot internal model
     * @param file
     *            is sent to this file
     * @throws IOException
     */
    public void generateDocument(final SavotVOTable votable, final String file)
            throws IOException {
        generateDocument(votable, file, null);
    }

    /**
     * Generates a VOTable XML document corresponding to the internal model
     * 
     * @param votable
     *            SavotVOTable
     * @param file
     *            String
     * @param stream
     *            OutputStream
     * @throws IOException  
     */
    public void generateDocument(final SavotVOTable votable, final String file,
                                 final OutputStream stream) throws IOException {

        if (file != null) {
            initStream(file);
        } else if (stream != null) {
            initStream(stream);
        } else {
            initStream(new PrintWriter(System.out));
        }

        writeDocumentHead(votable);

        // write COOSYS elements - deprecated since VOTable 1.2
        writeCoosys(votable.getCoosys());

        // write INFO elements
        writeInfo(votable.getInfos());

        // write PARAM elements
        writeParam(votable.getParams());

        // write GROUP elements - since VOTable 1.2
        writeGroup(votable.getGroups());

        // RESOURCE
        writeResource(votable.getResources());

        // write INFO elements - since VOTable 1.2
        writeInfo(votable.getInfosAtEnd());

        writeDocumentEnd();
    }

    /**
     * Init the Stream for the output
     * 
     * @param file
     *            String
     * @throws IOException  
     */
    public void initStream(final String file) throws IOException {
        final boolean compressed = file.endsWith("gz");

        final OutputStream outStream = getOutputStream(new FileOutputStream(file), compressed);
        initStream(new OutputStreamWriter(outStream));
    }

    /**
     * Init the Stream for the output
     * 
     * @param stream
     *            OutputStream
     * @throws IOException  
     */
    public void initStream(final OutputStream stream) throws IOException {
        final OutputStream outStream = getOutputStream(stream, false);
        initStream(new OutputStreamWriter(outStream));
    }

    /**
     * Init the Stream for the output using the given Writer
     * 
     * @param writer writer implementation to write into
     */
    public void initStream(final Writer writer) {
        bw = new BufferedWriter(writer, 64 * 1024); // 64K text buffer
    }

    /**
     * Get a buffered output stream or a gzip output stream
     * @param outstream stream to wrap
     * @param compressed true to indicate to use a gzip input stream
     * @return input stream
     * @throws IOException useless 
     */
    private OutputStream getOutputStream(final OutputStream outstream, final boolean compressed) throws IOException {
        final int bufferSize = 64 * 1024; // 64K write buffer

        final OutputStream out;
        if (compressed) {
            out = new GZIPOutputStream(outstream, bufferSize); // 512 by default
        } else {
            out = new BufferedOutputStream(outstream, bufferSize);
        }

        return out;
    }

    /**
     * Write a comment
     * 
     * @param comment
     *            String
     * @throws IOException  
     */
    public void writeComment(final String comment) throws IOException {
        if (comment.length() != 0) {
            bw.append("\n<!-- ").append(comment).append(" -->\n");
        }
    }

    /**
     * Write a description
     * 
     * @param description
     * @throws IOException  
     * 
     */
    public void writeDescription(final String description) throws IOException {
        if (description.length() != 0) {
            // DESCRIPTION
            bw.append(descriptionbegin).append(encodeElement(description)).append(descriptionend);
        }
    }

    /**
     * Write a VOTable XML head
     * 
     * @param votable
     * @throws IOException  
     */
    public void writeDocumentHead(final SavotVOTable votable) throws IOException {
        final Writer w = bw; // local copy

        w.write(top1);

        writeComment(votable.getAbove());

        // XSL stylesheet
        if (styleSheet.length() != 0) {
            w.append("\n<?xml-stylesheet type=\"text/xsl\" href=\"").append(styleSheet).append("\" ?>");
        }

        w.write("\n<VOTABLE");
        if (votable.getXmlns().length() != 0 || votable.getXmlnsxsi().length() != 0 || votable.getXsischema().length() != 0 || votable.getXsinoschema().length() != 0) {
            if (votable.getXmlns().length() != 0) {
                w.append(" xmlns=\"").append(votable.getXmlns()).append('"');
            }

            if (votable.getXmlnsxsi().length() != 0) {
                w.append(" xmlns:xsi=\"").append(votable.getXmlnsxsi()).append('"');
            }

            if (votable.getXsischema().length() != 0) {
                w.append(" xsi:schemaLocation=\"").append(votable.getXsischema()).append('"');
            }

            if (votable.getXsinoschema().length() != 0) {
                w.append(" xsi:noNamespaceSchemaLocation=\"").append(votable.getXsinoschema()).append('"');
            }

        }
        w.write(" version=");

        if (votable.getVersion().length() != 0) {
            if (votable.getId().length() != 0) {
                w.append('"').append(votable.getVersion()).append('"').append(" ID=\"").append(votable.getId()).append('"').append('>');
            } else {
                w.append('"').append(votable.getVersion()).append('"').append('>');
            }
        } else {
            if (votable.getId().length() != 0) {
                w.append('"').append("1.2").append('"').append(" ID=\"").append(votable.getId()).append('"').append('>');
            } else {
                w.append('"').append("1.2").append('"').append('>');
            }
        }

        writeComment(votable.getBelow());

        // write DESCRIPTION element
        writeDescription(votable.getDescription());

        // deprecated since VOTable 1.1
        if (votable.getDefinitions() != null) {
            // DEFINITIONS begin
            w.write(definitionsbegin);

            // write COOSYS elements
            writeCoosys(votable.getDefinitions().getCoosys());

            // write PARAM elements
            writeParam(votable.getDefinitions().getParams());

            // DEFINITIONS end
            w.write(definitionsend);
        }
        w.flush();
    }

    /**
     * Write a VOTable XML end
     * 
     * @throws IOException 
     */
    public void writeDocumentEnd() throws IOException {
        bw.write("\n</VOTABLE>\n");
        bw.flush();
        bw.close();
        bw = null;
    }

    /**
     * Write a COOSYS set
     * 
     * @param coosysSet
     * @throws IOException  
     */
    public void writeCoosys(final CoosysSet coosysSet) throws IOException {
        final Writer w = bw; // local copy

        SavotCoosys coosys;

        for (int i = 0, len = coosysSet.getItemCount(); i < len; i++) {
            coosys = coosysSet.getItemAt(i);

            writeComment(coosys.getAbove());

            w.write("\n<COOSYS");

            if (coosys.getId().length() != 0) {
                w.append(" ID=\"").append(coosys.getId()).append('"');
            }

            if (coosys.getEquinox().length() != 0) {
                w.append(" equinox=\"").append(coosys.getEquinox()).append('"');
            }

            if (coosys.getEpoch().length() != 0) {
                w.append(" epoch=\"").append(coosys.getEpoch()).append('"');
            }

            if (coosys.getSystem().length() != 0) {
                w.append(" system=\"").append(coosys.getSystem()).append('"');
            }

            if (coosys.getContent().length() != 0) {
                w.write(">");

                writeComment(coosys.getBelow());

                w.write(coosys.getContent());

                w.write(coosysend);
            } else {
                /* no content */
                w.write("/>");

                writeComment(coosys.getBelow());
            }
        }
    }

    /**
     * Write a PARAM set
     * 
     * @param params
     * @throws IOException  
     */
    public void writeParam(final ParamSet params) throws IOException {
        if (params != null) {
            final Writer w = bw; // local copy

            SavotParam param;

            for (int i = 0, len = params.getItemCount(); i < len; i++) {
                param = params.getItemAt(i);

                writeComment(param.getAbove());

                w.write("\n<PARAM");

                if (param.getName().length() != 0) {
                    w.append(" name=\"").append(encodeAttribute(param.getName())).append('"');
                }

                if (param.getId().length() != 0) {
                    w.append(" ID=\"").append(encodeAttribute(param.getId())).append('"');
                }

                if (param.getDataType().length() != 0) {
                    w.append(" datatype=\"").append(encodeAttribute(param.getDataType())).append('"');
                }

                if (param.getArraySize().length() != 0) {
                    w.append(" arraysize=\"").append(encodeAttribute(param.getArraySize())).append('"');
                }

                if (param.getPrecision().length() != 0) {
                    w.append(" precision=\"").append(encodeAttribute(param.getPrecision())).append('"');
                }

                if (param.getWidth().length() != 0) {
                    w.append(" width=\"").append(encodeAttribute(param.getWidth())).append('"');
                }

                // since VOTable version 1.2
                if (param.getXtype().length() != 0) {
                    w.append(" xtype=\"").append(encodeAttribute(param.getXtype())).append('"');
                }

                if (param.getRef().length() != 0) {
                    w.append(" ref=\"").append(encodeAttribute(param.getRef())).append('"');
                }

                if (param.getUcd().length() != 0) {
                    w.append(" ucd=\"").append(encodeAttribute(param.getUcd())).append('"');
                }

                if (param.getUtype().length() != 0) {
                    w.append(" utype=\"").append(encodeAttribute(param.getUtype())).append('"');
                }

                // Mandatory value:
                w.append(" value=\"").append(encodeAttribute(param.getValue())).append('"');

                if (param.getUnit().length() != 0) {
                    w.append(" unit=\"").append(encodeAttribute(param.getUnit())).append('"');
                }

                if (param.getValues() != null || param.getLinks().getItemCount() != 0 || param.getDescription().length() != 0) {
                    w.write(">");

                    writeComment(param.getBelow());

                    // write DESCRIPTION element
                    writeDescription(param.getDescription());

                    // write VALUES element
                    writeValues(param.getValues());

                    // write LINK elements
                    writeLink(param.getLinks());

                    // write PARAM end
                    w.write(paramend);
                } else {
                    w.write("/>");

                    writeComment(param.getBelow());
                }
            }
        }
    }

    /**
     * Write a PARAMref set
     * 
     * @param refparams
     * @throws IOException  
     */
    public void writeParamRef(final ParamRefSet refparams) throws IOException {
        if (refparams != null) {
            final Writer w = bw; // local copy

            SavotParamRef paramref;

            for (int i = 0, len = refparams.getItemCount(); i < len; i++) {
                paramref = refparams.getItemAt(i);

                writeComment(paramref.getAbove());

                w.write("\n<PARAMref");

                if (paramref.getRef().length() != 0) {
                    w.append(" ref=\"").append(encodeAttribute(paramref.getRef())).append('"');
                }

                if (paramref.getUcd().length() != 0) {
                    w.append(" ucd=\"").append(encodeAttribute(paramref.getUcd())).append('"');
                }

                if (paramref.getUtype().length() != 0) {
                    w.append(" utype=\"").append(encodeAttribute(paramref.getUtype())).append('"');
                }

                w.write("/>");

                writeComment(paramref.getBelow());
            }
        }
    }

    /**
     * Write a LINK set
     * 
     * @param linkSet
     * @throws IOException  
     */
    public void writeLink(final LinkSet linkSet) throws IOException {
        final Writer w = bw; // local copy

        SavotLink link;

        for (int i = 0, len = linkSet.getItemCount(); i < len; i++) {
            link = linkSet.getItemAt(i);

            writeComment(Objects.requireNonNull(link).getAbove());

            w.write("\n<LINK");

            if (link.getId().length() != 0) {
                w.append(" ID=\"").append(encodeAttribute(link.getId())).append('"');
            }

            if (link.getContentRole().length() != 0) {
                w.append(" content-role=\"").append(encodeAttribute(link.getContentRole())).append('"');
            }

            if (link.getContentType().length() != 0) {
                w.append(" content-type=\"").append(encodeAttribute(link.getContentType())).append('"');
            }

            if (link.getTitle().length() != 0) {
                w.append(" title=\"").append(encodeAttribute(link.getTitle())).append('"');
            }

            if (link.getValue().length() != 0) {
                w.append(" value=\"").append(encodeAttribute(link.getValue())).append('"');
            }

            if (link.getHref().length() != 0) {
                w.append(" href=\"").append(encodeAttribute(link.getHref()) + '"');
            }

            if (link.getGref().length() != 0) {
                w.append(" gref=\"").append(encodeAttribute(link.getGref())).append(String.valueOf('"'));
            }

            if (link.getAction().length() != 0) {
                w.append(" action=\"").append(encodeAttribute(link.getAction())).append(String.valueOf('"'));
            }

            if (link.getContent().length() != 0) {
                w.write(">");

                writeComment(link.getBelow());

                w.write(link.getContent());

                w.write(linkend);
            } else {
                /* no content */
                w.write("/>");

                writeComment(link.getBelow());
            }
        }
    }

    /**
     * Write an INFO set
     * 
     * @param infoSet
     * @throws IOException  
     */
    public void writeInfo(final InfoSet infoSet) throws IOException {
        if (infoSet != null) {
            final Writer w = bw; // local copy

            SavotInfo info;

            for (int i = 0, len = infoSet.getItemCount(); i < len; i++) {
                info = infoSet.getItemAt(i);

                // INFO
                writeComment(info.getAbove());

                w.write("\n<INFO");

                if (info.getId().length() != 0) {
                    w.append(" ID=\"").append(encodeAttribute(info.getId())).append('"');
                }

                if (info.getName().length() != 0) {
                    w.append(" name=\"").append(encodeAttribute(info.getName())).append('"');
                }

                if (info.getValue().length() != 0) {
                    w.append(" value=\"").append(encodeAttribute(info.getValue())).append('"');
                }

                // since VOTable version 1.2
                if (info.getXtype().length() != 0) {
                    w.append(" xtype=\"").append(encodeAttribute(info.getXtype())).append('"');
                }

                // since VOTable version 1.2
                if (info.getRef().length() != 0) {
                    w.append(" ref=\"").append(encodeAttribute(info.getRef())).append('"');
                }

                // since VOTable version 1.2
                if (info.getUnit().length() != 0) {
                    w.append(" unit=\"").append(encodeAttribute(info.getUnit())).append('"');
                }

                // since VOTable version 1.2
                if (info.getUcd().length() != 0) {
                    w.append(" ucd=\"").append(encodeAttribute(info.getUcd())).append('"');
                }

                // since VOTable version 1.2
                if (info.getUtype().length() != 0) {
                    w.append(" utype=\"").append(encodeAttribute(info.getUtype())).append('"');
                }

                // only for VOTable version before 1.2
                if (info.getContent().length() != 0) {
                    w.write(">");

                    writeComment(info.getBelow());

                    w.append(encodeElement(info.getContent())).append("</INFO>");

                } else {
                    // from VOTable 1.2
                    if (info.getDescription().length() != 0 || info.getValues() != null || info.getLinks().getItemCount() != 0) {
                        w.write(">");

                        writeComment(info.getBelow());

                        // write DESCRIPTION element
                        writeDescription(info.getDescription());

                        // write VALUES element
                        writeValues(info.getValues());

                        // write LINK elements
                        writeLink(info.getLinks());

                        w.write("</INFO>");
                    } else {
                        w.write("/>");
                    }
                }
            }
        }
    }

    /**
     * Write a FIELD set
     * 
     * @param fieldSet
     * @throws IOException  
     */
    public void writeField(final FieldSet fieldSet) throws IOException {
        final Writer w = bw; // local copy

        SavotField field;

        for (int i = 0, len = fieldSet.getItemCount(); i < len; i++) {
            field = fieldSet.getItemAt(i);

            writeComment(field.getAbove());

            w.write("\n<FIELD");

            if (field.getName().length() != 0) {
                w.append(" name=\"").append(encodeAttribute(field.getName())).append('"');
            }

            if (field.getId().length() != 0) {
                w.append(" ID=\"").append(encodeAttribute(field.getId())).append('"');
            }

            if (field.getDataType().length() != 0) {
                w.append(" datatype=\"").append(encodeAttribute(field.getDataType())).append('"');
            }

            if (field.getArraySize().length() != 0) {
                w.append(" arraysize=\"").append(encodeAttribute(field.getArraySize())).append('"');
            }

            if (field.getPrecision().length() != 0) {
                w.append(" precision=\"").append(encodeAttribute(field.getPrecision())).append('"');
            }

            if (field.getWidth().length() != 0) {
                w.append(" width=\"").append(encodeAttribute(field.getWidth())).append('"');
            }

            // since VOTable version 1.2
            if (field.getXtype().length() != 0) {
                w.append(" xtype=\"").append(encodeAttribute(field.getXtype())).append('"');
            }

            if (field.getRef().length() != 0) {
                w.append(" ref=\"").append(encodeAttribute(field.getRef())).append('"');
            }

            if (field.getUcd().length() != 0) {
                w.append(" ucd=\"").append(encodeAttribute(field.getUcd())).append('"');
            }

            if (field.getUtype().length() != 0) {
                w.append(" utype=\"").append(encodeAttribute(field.getUtype())).append('"');
            }

            if (field.getType().length() != 0) {
                w.append(" type=\"").append(encodeAttribute(field.getType())).append('"');
            }

            if (field.getUnit().length() != 0) {
                w.append(" unit=\"").append(encodeAttribute(field.getUnit())).append('"');
            }

            if (field.getDescription().length() != 0 || field.getValues() != null || field.getLinks().getItemCount() != 0) {
                w.write(">");

                writeComment(field.getBelow());

                // write DESCRIPTION element
                writeDescription(field.getDescription());

                // write VALUES element
                writeValues(field.getValues());

                // write LINK elements
                writeLink(field.getLinks());

                w.write(fieldend);
            } else {
                w.write("/>");
            }
        }
    }

    /**
     * Write a FIELD set
     * 
     * @param fieldRefSet
     * @throws IOException  
     */
    public void writeFieldRef(final FieldRefSet fieldRefSet) throws IOException {
        final Writer w = bw; // local copy

        SavotFieldRef fieldref;

        for (int i = 0, len = fieldRefSet.getItemCount(); i < len; i++) {
            fieldref = fieldRefSet.getItemAt(i);

            writeComment(fieldref.getAbove());

            w.write("\n<FIELDref");

            if (fieldref.getRef().length() != 0) {
                w.append(" ref=\"").append(encodeAttribute(fieldref.getRef())).append('"');
            }

            if (fieldref.getUcd().length() != 0) {
                w.append(" ucd=\"").append(encodeAttribute(fieldref.getUcd())).append('"');
            }

            if (fieldref.getUtype().length() != 0) {
                w.append(" utype=\"").append(encodeAttribute(fieldref.getUtype())).append('"');
            }

            w.write("/>");
        }
    }

    /**
     * Write a STREAM element
     * 
     * @param stream
     * @throws IOException  
     */
    public void writeStream(final SavotStream stream) throws IOException {
        final Writer w = bw; // local copy

        writeComment(stream.getAbove());

        w.write("\n<STREAM");

        if (stream.getType().length() != 0) {
            w.append(" type=\"").append(encodeAttribute(stream.getType())).append('"');
        }

        if (stream.getHref().length() != 0) {
            w.append(" href=\"").append(encodeAttribute(stream.getHref())).append('"');
        }

        if (stream.getActuate().length() != 0) {
            w.append(" actuate=\"").append(encodeAttribute(stream.getActuate())).append('"');
        }

        if (stream.getEncoding().length() != 0) {
            w.append(" encoding=\"").append(encodeAttribute(stream.getEncoding())).append('"');
        }

        if (stream.getExpires().length() != 0) {
            w.append(" expires=\"").append(encodeAttribute(stream.getExpires())).append('"');
        }

        if (stream.getRights().length() != 0) {
            w.append(" rights=\"").append(encodeAttribute(stream.getRights())).append('"');
        }

        w.write(">");

        writeComment(stream.getBelow());

        if (stream.getContent().length() != 0) {
            w.write(stream.getContent());
        }

        w.write(streamend);
    }

    /**
     * Write a BINARY element
     * 
     * @param binary
     * @throws IOException  
     */
    public void writeBinary(final SavotBinary binary) throws IOException {
        if (binary.getStream() != null) {
            final Writer w = bw; // local copy

            writeComment(binary.getAbove());

            w.write(binarybegin);

            writeComment(binary.getBelow());

            writeStream(binary.getStream());

            w.write(binaryend);
        }
    }

    /**
     * Write a VALUES element
     * 
     * @param values
     * @throws IOException  
     */
    public void writeValues(final SavotValues values) throws IOException {
        if (values != null) {
            final Writer w = bw; // local copy

            writeComment(values.getAbove());

            w.write("\n<VALUES");

            if (values.getId().length() != 0) {
                w.append(" ID=\"").append(encodeAttribute(values.getId())).append('"');
            }

            if (values.getType().length() != 0) {
                w.append(" type=\"").append(encodeAttribute(values.getType())).append('"');
            }

            if (values.getNull().length() != 0) {
                w.append(" null=\"").append(encodeAttribute(values.getNull())).append('"');
            }

            if (values.getRef().length() != 0) {
                w.append(" ref=\"").append(encodeAttribute(values.getRef())).append('"');
            }

            if (values.getInvalid().length() != 0) {
                w.append(" invalid=\"").append(encodeAttribute(values.getInvalid())).append('"');
            }

            w.write(">");

            writeComment(values.getBelow());

            // MIN element
            if (values.getMin() != null) {
                SavotMin min = values.getMin();
                writeMin(min);
            }

            // MAX element
            if (values.getMax() != null) {
                SavotMax max = values.getMax();
                writeMax(max);
            }

            // write OPTION elements
            writeOption(values.getOptions());

            w.write(valuesend);
        }
    }

    /**
     * Write a FITS element
     * 
     * @param fits
     * @throws IOException  
     */
    public void writeFits(final SavotFits fits) throws IOException {
        final Writer w = bw; // local copy

        writeComment(fits.getAbove());

        w.write("\n<FITS");

        if (fits.getExtnum().length() != 0) {
            w.append(" extnum=\"").append(encodeAttribute(fits.getExtnum())).append('"');
        }

        w.write(">");

        writeComment(fits.getBelow());

        // STREAM element
        if (fits.getStream() != null) {
            // write STREAM element
            writeStream(fits.getStream());
        }

        w.write(fitsend);
    }

    /**
     * Write a MIN element
     * 
     * @param min
     * @throws IOException  
     */
    public void writeMin(final SavotMin min) throws IOException {
        final Writer w = bw; // local copy

        writeComment(min.getAbove());

        w.write("\n<MIN");

        if (min.getValue().length() != 0) {
            w.append(" value=\"").append(encodeAttribute(min.getValue())).append('"');
        }

        if (min.getInclusive().length() != 0) {
            w.append(" inclusive=\"").append(encodeAttribute(min.getInclusive())).append('"');
        }

        if (min.getContent().length() != 0) {
            w.write(">");

            writeComment(min.getBelow());

            w.write(min.getContent());

            w.write(minend);
        } else {
            /* no content */
            w.write("/>");

            writeComment(min.getBelow());
        }
    }

    /**
     * Write a MAX element
     * 
     * @param max
     * @throws IOException  
     */
    public void writeMax(final SavotMax max) throws IOException {
        final Writer w = bw; // local copy

        writeComment(max.getAbove());

        w.write("\n<MAX");

        if (max.getValue().length() != 0) {
            w.append(" value=\"").append(encodeAttribute(max.getValue())).append('"');
        }

        if (max.getInclusive().length() != 0) {
            w.append(" inclusive=\"").append(encodeAttribute(max.getInclusive())).append('"');
        }

        if (max.getContent().length() != 0) {
            w.write(">");

            writeComment(max.getBelow());

            w.write(max.getContent());

            w.write(maxend);
        } else {
            /* no content */
            w.write("/>");

            writeComment(max.getBelow());
        }
    }

    /**
     * Write an OPTION set
     * 
     * @param optionSet
     * @throws IOException  
     */
    public void writeOption(final OptionSet optionSet) throws IOException {
        final Writer w = bw; // local copy

        SavotOption option;

        for (int i = 0, len = optionSet.getItemCount(); i < len; i++) {
            option = optionSet.getItemAt(i);

            writeComment(option.getAbove());

            w.write("\n<OPTION");

            if (option.getName().length() != 0) {
                w.append(" name=\"").append(encodeAttribute(option.getName())).append('"');
            }

            if (option.getValue().length() != 0) {
                w.append(" value=\"").append(encodeAttribute(option.getValue())).append('"');
            }

            // write recursive options
            if (option.getOptions().getItemCount() != 0) {
                w.write(">");

                writeComment(option.getBelow());

                writeOption(option.getOptions());

                w.write(optionend);
            } else {
                w.write("/>");

                writeComment(option.getBelow());
            }
        }
    }

    /**
     * Write a GROUP set
     * 
     * @param groupSet
     * @throws IOException  
     */
    public void writeGroup(final GroupSet groupSet) throws IOException {
        final Writer w = bw; // local copy

        SavotGroup group;

        for (int i = 0, len = groupSet.getItemCount(); i < len; i++) {
            group = groupSet.getItemAt(i);

            writeComment(group.getAbove());

            w.write("\n<GROUP");

            if (group.getId().length() != 0) {
                w.append(" ID=\"").append(encodeAttribute(group.getId())).append('"');
            }

            if (group.getName().length() != 0) {
                w.append(" name=\"").append(encodeAttribute(group.getName())).append('"');
            }

            if (group.getRef().length() != 0) {
                w.append(" ref=\"").append(encodeAttribute(group.getRef())).append('"');
            }

            if (group.getUcd().length() != 0) {
                w.append(" ucd=\"").append(encodeAttribute(group.getUcd())).append('"');
            }

            if (group.getUtype().length() != 0) {
                w.append(" utype=\"").append(encodeAttribute(group.getUtype())).append('"');
            }

            w.write(">");

            writeComment(group.getBelow());

            // write DESCRIPTION element
            writeDescription(group.getDescription());

            // write FIELDref elements
            writeFieldRef(group.getFieldsRef());

            // write PARAMref elements
            writeParamRef(group.getParamsRef());

            // write PARAM elements
            writeParam(group.getParams());

            // write recursive groups
            writeGroup(group.getGroups());

            w.write(groupend);
        }
    }

    /**
     * Write a TABLE begin
     * 
     * @param table
     *            SavotTable
     * @throws IOException  
     */
    public void writeTableBegin(final SavotTable table) throws IOException {
        final Writer w = bw; // local copy

        writeComment(table.getAbove());

        // TABLE
        w.write("\n<TABLE");

        if (table.getId().length() != 0) {
            w.append(" ID=\"").append(encodeAttribute(table.getId())).append('"');
        }

        if (table.getName().length() != 0) {
            w.append(" name=\"").append(encodeAttribute(table.getName())).append('"');
        }

        if (table.getRef().length() != 0) {
            w.append(" ref=\"").append(encodeAttribute(table.getRef())).append('"');
        }

        if (table.getUcd().length() != 0) {
            w.append(" ucd=\"").append(encodeAttribute(table.getUcd())).append('"');
        }

        if (table.getUtype().length() != 0) {
            w.append(" utype=\"").append(encodeAttribute(table.getUtype())).append('"');
        }

        if (table.getNrows().length() != 0) {
            w.append(" nrows=\"").append(encodeAttribute(table.getNrows())).append('"');
        }

        w.write(">");

        writeComment(table.getBelow());

        // write DESCRIPTION element
        writeDescription(table.getDescription());
    }

    /**
     * Write a TABLE end
     * @throws IOException 
     */
    public void writeTableEnd() throws IOException {
        // </TABLE>
        bw.write(tableend);
    }

    /**
     * Write a RESOURCE begin
     * 
     * @param resource
     *            SavotResource
     * @throws IOException  
     */
    public void writeResourceBegin(final SavotResource resource) throws IOException {
        final Writer w = bw; // local copy

        // RESOURCE
        writeComment(resource.getAbove());

        w.write("\n<RESOURCE");

        if (resource.getName().length() != 0) {
            w.append(" name=\"").append(encodeAttribute(resource.getName())).append('"');
        }

        if (resource.getId().length() != 0) {
            w.append(" ID=\"").append(encodeAttribute(resource.getId())).append('"');
        }

        if (resource.getUtype().length() != 0) {
            w.append(" utype=\"").append(encodeAttribute(resource.getUtype())).append('"');
        }

        if (resource.getType().length() != 0) {
            w.append(" type=\"").append(encodeAttribute(resource.getType())).append('"');
        }

        w.write(">");

        writeComment(resource.getBelow());

        // write DESCRIPTION element
        writeDescription(resource.getDescription());
    }

    /**
     * Write a RESOURCE end
     * @throws IOException 
     */
    public void writeResourceEnd() throws IOException {
        // </RESOURCE>
        bw.write(resourceend);
    }

    /**
     * Write a TABLEDATA begin
     * @throws IOException 
     */
    public void writeTableDataBegin() throws IOException {
        // <TABLEDATA>
        bw.write(tabledatabegin);
    }

    /**
     * Write a TABLEDATA end
     * @throws IOException 
     */
    public void writeTableDataEnd() throws IOException {
        // </TABLEDATA>
        bw.write(tabledataend);
    }

    /**
     * Write a DATA begin
     * @throws IOException 
     */
    public void writeDataBegin() throws IOException {
        // <DATA>
        bw.write(databegin);
    }

    /**
     * Write a DATA end
     * @throws IOException 
     */
    public void writeDataEnd() throws IOException {
        // </DATA>
        bw.write(dataend);
    }

    /**
     * Write a TR
     * @param tr
     * @throws IOException  
     */
    public void writeTR(final SavotTR tr) throws IOException {
        final Writer w = bw; // local copy
        final boolean doEncode = elementEntities;
        String v;

        // <TR>
        w.write(trbegin);

        final List<SavotTD> tds = tr.getTDSet().getItems();
        SavotTD td;

        for (SavotTD savotTD : tds) {
            td = savotTD;

            // use raw values to test null values efficiently:
            v = td.getRawContent();

            if (v != null) {
                // <TD>
                w.write(tdbegin);

                w.write((doEncode) ? encodeElement(v) : v);

                // </TD>
                w.write(tdend);

            } else {
                w.write(tdempty);
            }
        }
        // </TR>
        w.write(trend);
    }

    /**
     * Write a RESOURCE set
     * 
     * @param resourceset
     *            ResourceSet
     * @throws IOException  
     */
    public void writeResource(final ResourceSet resourceset) throws IOException {
        if (resourceset != null) {
            SavotResource resource;
            TableSet tableSet;
            SavotTable table;
            SavotData data;
            SavotTableData tableData;
            TRSet trs;

            for (int i = 0, len = resourceset.getItemCount(); i < len; i++) {
                resource = resourceset.getItemAt(i);

                // <RESOURCE>
                writeResourceBegin(Objects.requireNonNull(resource));

                // write INFO elements
                writeInfo(resource.getInfos());

                // write COOSYS elements
                writeCoosys(resource.getCoosys());

                // write GROUP elements - since VOTable 1.2
                writeGroup(resource.getGroups());

                // write PARAM elements
                writeParam(resource.getParams());

                // write LINK elements
                writeLink(resource.getLinks());

                // TABLE elements
                tableSet = resource.getTables();

                for (int j = 0, tableLen = tableSet.getItemCount(); j < tableLen; j++) {
                    table = tableSet.getItemAt(j);

                    // <TABLE>
                    writeTableBegin(Objects.requireNonNull(table));

                    // write FIELD elements
                    writeField(table.getFields());

                    // write PARAM elements
                    writeParam(table.getParams());

                    // write GROUP elements
                    writeGroup(table.getGroups());

                    // write LINK elements
                    writeLink(table.getLinks());

                    data = table.getData();

                    if (data != null) {
                        // <DATA>
                        writeDataBegin();

                        tableData = data.getTableData();

                        if (tableData != null) {
                            // <TABLEDATA>
                            writeTableDataBegin();

                            trs = tableData.getTRs();

                            for (int r = 0, trLen = trs.getItemCount(); r < trLen; r++) {
                                // <TR>
                                writeTR(Objects.requireNonNull(trs.getItemAt(r)));
                            }

                            // </TABLE>
                            writeTableDataEnd();

                        } // TABLE / DATA / TABLEDATA

                        // write BINARY element
                        if (data.getBinary() != null) {
                            writeBinary(data.getBinary());
                        }

                        // write FITS element
                        if (data.getFits() != null) {
                            writeFits(data.getFits());
                        }

                        // </DATA>
                        writeDataEnd();

                    } // TABLE / DATA

                    // write INFO (at End) elements - since VOTable 1.2
                    writeInfo(table.getInfosAtEnd());

                    // </TABLE>
                    writeTableEnd();

                } // TABLE

                writeResource(resource.getResources());

                // write INFO (at End) elements - since VOTable 1.2
                writeInfo(resource.getInfosAtEnd());

                // </RESOURCE>
                writeResourceEnd();
            }
        }
    }
}
