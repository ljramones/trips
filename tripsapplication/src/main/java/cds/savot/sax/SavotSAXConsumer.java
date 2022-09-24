package cds.savot.sax;

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

import java.util.ArrayList;

/**
 * <p>
 * This interface must be implemented to use the Savot SAX parser
 * </p>
 * 
 * @author Andre Schaaff
 */
@SuppressWarnings("deprecation")
public interface SavotSAXConsumer {

    // start elements
    public abstract void startVotable(ArrayList<SavotVOTable> attributes);

    public abstract void startDescription();

    public abstract void startResource(ArrayList<SavotResource> attributes);

    public abstract void startTable(ArrayList<SavotTable> attributes);

    public abstract void startField(ArrayList<SavotField> attributes);

    public abstract void startFieldref(ArrayList<SavotFieldRef> attributes);

    public abstract void startValues(ArrayList<SavotValues> attributes);

    public abstract void startStream(ArrayList<SavotStream> attributes);

    public abstract void startTR();

    public abstract void startTD(ArrayList<SavotTD> attributes);

    public abstract void startData();

    public abstract void startBinary();

    public abstract void startFits(ArrayList<SavotFits> attributes);

    public abstract void startTableData();

    public abstract void startParam(ArrayList<SavotParam> attributes);

    public abstract void startParamRef(ArrayList<SavotParamRef> attributes);

    public abstract void startLink(ArrayList<SavotLink> attributes);

    public abstract void startInfo(ArrayList<SavotInfo> attributes);

    public abstract void startMin(ArrayList<SavotMin> attributes);

    public abstract void startMax(ArrayList<SavotMax> attributes);

    public abstract void startOption(ArrayList<SavotOption> attributes);

    public abstract void startGroup(ArrayList<SavotGroup> attributes);

    /**
     * @deprecated since VOTable 1.2
     * @param attributes
     */
    public abstract void startCoosys(ArrayList<SavotCoosys> attributes);

    /**
     * @deprecated since VOTable 1.1
     */
    public abstract void startDefinitions();

    // end elements
    public abstract void endVotable();

    public abstract void endDescription();

    public abstract void endResource();

    public abstract void endTable();

    public abstract void endField();

    public abstract void endFieldref();

    public abstract void endValues();

    public abstract void endStream();

    public abstract void endTR();

    public abstract void endTD();

    public abstract void endData();

    public abstract void endBinary();

    public abstract void endFits();

    public abstract void endTableData();

    public abstract void endParam();

    public abstract void endParamRef();

    public abstract void endLink();

    public abstract void endInfo();

    public abstract void endMin();

    public abstract void endMax();

    public abstract void endOption();

    public abstract void endGroup();

    /**
     * @deprecated since VOTable 1.2
     */
    public abstract void endCoosys();

    /**
     * @deprecated since VOTable 1.1
     */
    public abstract void endDefinitions();

    // TEXT
    public abstract void textTD(String text);

    public abstract void textMin(String text);

    public abstract void textMax(String text);

    /**
     * @deprecated since VOTable 1.2
     * @param text
     */
    public abstract void textCoosys(String text);

    public abstract void textLink(String text);

    public abstract void textOption(String text);

    public abstract void textGroup(String text);

    public abstract void textInfo(String text);

    public abstract void textDescription(String text);

    public abstract void textStream(String text);

    // document
    public abstract void startDocument();

    public abstract void endDocument();
}
