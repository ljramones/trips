package cds.savot.model;

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
/**
 * <p>
 * Data element
 * </p>
 * 
 * @author Andre Schaaff
 */
public final class SavotData extends MarkupComment {

    // TABLEDATA element
    private SavotTableData tableData = null;
    // BINARY element
    private SavotBinary binary = null;
    // BINARY2 element
    private SavotBinary2 binary2 = null;
    // FITS element
    private SavotFits fits = null;

    /**
     * Constructor
     */
    public SavotData() {
    }

    /**
     * Set the TABLEDATA element
     * 
     * @param tableData
     */
    public void setTableData(final SavotTableData tableData) {
        this.tableData = tableData;
    }

    /**
     * Get the TABLEDATA element
     * 
     * @return SavotTableData
     */
    public SavotTableData getTableData() {
        return tableData;
    }

    /**
     * Set the BINARY element
     * 
     * @param binary
     */
    public void setBinary(final SavotBinary binary) {
        this.binary = binary;
    }

    /**
     * Get the BINARY element
     * 
     * @return SavotBinary
     */
    public SavotBinary getBinary() {
        return binary;
    }

    /**
     * Set the BINARY2 element
     * 
     * @param binary
     */
    public void setBinary2(final SavotBinary2 binary2) {
        this.binary2 = binary2;
    }

    /**
     * Get the BINARY2 element
     * 
     * @return SavotBinary
     */
    public SavotBinary2 getBinary2() {
        return binary2;
    }
    
    /**
     * Set the FITS element
     * 
     * @param fits
     */
    public void setFits(final SavotFits fits) {
        this.fits = fits;
    }

    /**
     * Get the FITS element
     * 
     * @return SavotFits
     */
    public SavotFits getFits() {
        return fits;
    }
}
