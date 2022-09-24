package cds.savot.binary;

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
//Author, Co-Author:  Andre Schaaff (CDS), Laurent Bourges (LAOG)
/**
 * Exception that can occur during the encoding/decoding of data in Base64.
 * If such an error occurs during the decoding, it would mean that the encoded message/data have been corrupted.
 *
 * @author Gregory Mantelet
 * @since 09/2011
 */
public final class Base64Exception extends Exception {

    private static final long serialVersionUID = 1L;

    public Base64Exception() {
        super();
    }

    public Base64Exception(String msg, Throwable cause) {
        super(msg, cause);
    }

    public Base64Exception(String msg) {
        super(msg);
    }

    public Base64Exception(Throwable cause) {
        super(cause);
    }

}
