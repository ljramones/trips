package cds.savot.model.interpreter;

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
import java.io.IOException;

/**
 * Exception sent while encoding/decoding binary data.
 * 
 * @author Gregory Mantelet
 * @since 09/2011
 * 
 * @see BinaryFieldInterpreter
 */
public class BinaryInterpreterException extends IOException {

    private static final long serialVersionUID = 1L;

    public BinaryInterpreterException() {
        super();
    }

    public BinaryInterpreterException(String msg) {
        super(msg);
    }

    public BinaryInterpreterException(Throwable cause) {
        super(cause);
    }

    public BinaryInterpreterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
