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

import cds.savot.model.SavotTR;
import cds.savot.model.TRSet;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Common interface of a writer of the data of a VOTable DATA node
 * (whatever is its child node: FITS, BINARY or TABLEDATA).
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 */
public interface SavotDataWriter extends Closeable, Flushable {

    /**
     * Writes the given row.
     * 
     * @param row		Row to write.
     * 
     * @throws IOException	If there is an error while writing the given row.
     */
    public void writeTR(SavotTR row) throws IOException;

    /**
     * Writes the given rows.
     * 
     * @param rows		Rows to write.
     * 
     * @throws IOException	If there is an error while writing the given rows.
     */
    public void writeTRSet(TRSet rows) throws IOException;
}
