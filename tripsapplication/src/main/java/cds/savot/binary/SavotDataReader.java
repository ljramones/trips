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

import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;

import java.io.Closeable;
import java.io.IOException;

/**
 * Common interface of a reader of a VOTable DATA node
 * (whatever is its child node: FITS, BINARY or TABLEDATA).
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public interface SavotDataReader extends Closeable {

    /**
     * <p>Reads to the next row.</p>
     * 
     * <p>
     * 	Once this function called, you can get the full row with {@link #getRow()} or {@link #getTR()},
     * 	or get specific cells with {@link #getCell(int)}, {@link #getCellAsString(int)}
     * 	or {@link #getTD(int)}.
     * </p>
     * 
     * @return	<i>true</i> if the next row has been successfully fetched, <i>false</i> otherwise.
     * 
     * @throws IOException		If an error occurs while reading the next row.
     */
    public boolean next() throws IOException;

    /**
     * Gets the last read row.
     * 
     * @return	The last read row.
     * @throws IllegalStateException	If <code>next</code> has not yet been called, if the EOF has been reached, or if the reader is closed.
     */
    public Object[] getRow() throws IllegalStateException;

    /**
     * Gets the last read row as a {@link SavotTR} object.
     * 
     * @return	The last read row.
     * @throws IllegalStateException	If <code>next</code> has not yet been called, if the EOF has been reached, or if the reader is closed.
     */
    public SavotTR getTR() throws IllegalStateException;

    /**
     * Gets the specified cell of the last read row.
     * 
     * @param indColumn		Index of the cell to get.
     * 
     * @return			The specified cell of the last read row.
     * 
     * @throws ArrayIndexOutOfBoundsException	If the given index is less than 0 or is greater than the number of available cell.
     * @throws IllegalStateException		If <code>next</code> has not yet been called, if the EOF has been reached, or if the reader is closed.
     */
    public Object getCell(final int indColumn) throws ArrayIndexOutOfBoundsException, IllegalStateException;

    /**
     * Gets the specified cell of the last read row as a String.
     * 
     * @param indColumn		Index of the cell to get.
     * 
     * @return			The string representation of specified cell.
     * 
     * @throws ArrayIndexOutOfBoundsException	If the given index is less than 0 or is greater than the number of available cell.
     * @throws IllegalStateException			If <code>next</code> has not yet been called, if the EOF has been reached, or if the reader is closed.
     */
    public String getCellAsString(final int indColumn) throws ArrayIndexOutOfBoundsException, IllegalStateException;

    /**
     * Gets the specified cell of the last read row as {@link SavotTD}.
     * 
     * @param indColumn		Index of the cell to get.
     * 
     * @return			The specified cell of the last read row.
     * 
     * @throws ArrayIndexOutOfBoundsException	If the given index is less than 0 or is greater than the number of available cell.
     * @throws IllegalStateException			If <code>next</code> has not yet been called, if the EOF has been reached, or if the reader is closed.
     */
    public SavotTD getTD(final int indColumn) throws ArrayIndexOutOfBoundsException, IllegalStateException;
}
