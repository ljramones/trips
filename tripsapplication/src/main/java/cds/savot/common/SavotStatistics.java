package cds.savot.common;

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
 * Statistics
 * </p>
 * @author Andre Schaaff 
 * 
 */
public final class SavotStatistics {

    private int iTablesGlobal = 0; // index of TABLE element
    private int iTablesLocal = 0; // index of TABLE element in current RESOURCE
    private int iTRGlobal = 0; // index of TR element
    private int iTRLocal = 0; // index of TR element in current TABLE
    private int iTDGlobal = 0; // index of TD element
    private int iTDLocal = 0; // index of TD element in current TABLE
    private int iResources = 0; // index of RESOURCES element
    private int iGroupsGlobal = 0; // index of GROUP element

    public void iTablesInc() {
        iTablesGlobal++;
        iTablesLocal++;
    }

    public void iTablesGlobalReset() {
        iTablesGlobal = 0;
    }

    public int getITablesGlobal() {
        return iTablesGlobal;
    }

    public void iTablesLocalReset() {
        iTablesLocal = 0;
    }

    public int getITablesLocal() {
        return iTablesLocal;
    }

    public void iTRInc() {
        iTRGlobal++;
        iTRLocal++;
    }

    public void iTRGlobalReset() {
        iTRGlobal = 0;
    }

    public int getITRGlobal() {
        return iTRGlobal;
    }

    public void iTRLocalReset() {
        iTRLocal = 0;
    }

    public int getITRLocal() {
        return iTRLocal;
    }

    public void iTDInc() {
        iTDGlobal++;
        iTDLocal++;
    }

    public void iTDGlobalReset() {
        iTDGlobal = 0;
    }

    public int getITDGlobal() {
        return iTDGlobal;
    }

    public void iTDLocalReset() {
        iTDLocal = 0;
    }

    public int getITDLocal() {
        return iTDLocal;
    }

    public void iResourcesInc() {
        iResources++;
    }

    public void iResourcesReset() {
        iResources = 0;
    }

    public void iResources(final int value) {
        iResources = value;
    }

    public int getIResources() {
        return iResources;
    }

    public void iGroupsGlobalInc() {
        iGroupsGlobal++;
    }

    public void iGroupsGlobalReset() {
        iGroupsGlobal = 0;
    }

    public void iGroupsGlobal(final int value) {
        iGroupsGlobal = value;
    }

    public int getIGroupsGlobal() {
        return iGroupsGlobal;
    }
}
