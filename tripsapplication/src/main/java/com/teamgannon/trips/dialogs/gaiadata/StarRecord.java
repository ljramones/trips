package com.teamgannon.trips.dialogs.gaiadata;


import lombok.Data;

/**
 * 1-  4  I4    ---      Seq       Running number for object
 * 6-  9  I4    ---      Sys       Running number for system
 * 11- 39  A29   ---      Name      Name of the system
 * 41- 46  A6    ---      ObjType   Type of the object (1)
 * 48- 76  A29   ---      ObjName   Name of the object
 * 79- 91  F13.9 deg      RAdeg     Right ascension in ICRS at Epoch
 * 94-106  F13.9 deg      DEdeg     Declination in ICRS at Epoch
 * 108-113  F6.1  yr       Epoch     Epoch for position
 * 115-122  F8.4  mas      plx       Parallax
 * 124-131  F8.4  mas    e_plx       ?=- Parallax uncertainty
 * 133-163  A31   ---    r_plx       Reference for the parallax
 * 165-180  F16.9 mas/yr   pmRA      ?=- Proper motion in right ascension,
 * pmRA*cosDE
 * 182-197  F16.9 mas/yr e_pmRA      ?=- Proper motion uncertainty in
 * right ascension
 * 199-214  F16.9 mas/yr   pmDE      ?=- Proper motion in declination
 * 216-231  F16.9 mas/yr e_pmDE      ?=- Proper motion uncertainty in declination
 * 233-262  A30   ---    r_pmDE      Reference for proper motion
 * 264-271  F8.3  km/s     RV        ?=- Line-of-sight velocity
 * 273-280  F8.4  km/s   e_RV        ?=- Line-of-sight velocity uncertainty
 * 282-300  A19   ---    r_RV        Reference for line-of-sight velocity
 * 302-309  A8    ---      SpType    Spectral type
 * 311-335  A25   ---    r_SpType    Reference for spectral type
 * 337-348  A12   ---      SpMethod  Method used to derive the spectral type
 * 350-351  I2    ---      GCode     [2/20]? Code for Gmag origin (2)
 * 353-361  F9.6  mag      Gmag      ?=- Gaia G band magnitude measured, given
 * only if GCode is 2 or 3
 * 363-368  F6.2  mag      Gest      ?=- Gaia G band magnitude estimated, given
 * only if GCode is 10 or 20
 * 370-378  F9.6  mag      GBPmag    ?=- Gaia BP band magnitude measured, given
 * only if GCode is 2 or 3
 * 380-388  F9.6  mag      GRPmag    ?=- Gaia RP band magnitude measured, given
 * only if GCode is 2 or 3
 * 390-396  F7.3  mag      Umag      ?=- U band magnitude
 * 398-404  F7.3  mag      Bmag      ?=- B band magnitude
 * 406-412  F7.3  mag      Vmag      ?=- V band magnitude
 * 414-420  F7.3  mag      Rmag      ?=- R band magnitude
 * 422-428  F7.3  mag      Imag      ?=- I band magnitude
 * 430-436  F7.3  mag      Jmag      ?=- J band magnitude
 * 438-444  F7.3  mag      Hmag      ?=- H band magnitude
 * 446-452  F7.3  mag      Ksmag     ?=- Ks band magnitude
 * 454-474  A21   ---    r_Sys       Reference for multiplicity or exoplanets
 * 476  I1    ---      Nexopl    ? Number of confirmed exoplanets
 * 478-496  I19   ---      GaiaDR2   ?=- Gaia DR2 identifier
 * 498-516  I19   ---      GaiaEDR3  ?=- Gaia EDR3 identifier
 * 518-543  A26   ---      SIMBAD    Name resolved by SIMBAD
 * 545-561  A17   ---      Common    Common name
 * 563-572  A10   ---      GJ        Gliese & Jahreiss catalogue identifier,
 * 574-585  A12   ---      HD        Henry Draper catalogue identifier
 * 587-596  A10   ---      HIP       Hipparcos catalogue identifier
 * 598-945  A348  ---      Com       Additional comments on exoplanets,
 * multiplicity, etc
 */
@Data
public class StarRecord {

    /**
     * 1-  4  I4    ---      Seq       Running number for object
     * not needed for DB
     */
    private int Seq;

    /**
     * 6-  9  I4    ---      Sys       Running number for system
     * not needed for DB
     */
    private int Sys;

    /**
     * 11- 39  A29   ---      Name     name of the system
     * not in DB, should we have it?
     */
    private String systemName;

    /**
     * 41- 46  A6    ---      ObjType   Type of the object (1)
     * not in DB
     */
    private String ObjType;

    /**
     * 48- 76  A29   ---      ObjName   Name of the object
     * displayName in DB
     */
    private String ObjName;

    /**
     * 79- 91  F13.9 deg      RAdeg     Right ascension in ICRS at Epoch
     * ra in DB
     */
    private double RAdeg;

    /*
     * 94-106  F13.9 deg      DEdeg     Declination in ICRS at Epoch
     * declination in DB
     */
    private double DEdeg;

    /**
     * 108-113  F6.1  yr       Epoch     Epoch for position
     * not in DB
     */
    private double Epoch;

    /**
     * 115-122  F8.4  mas      plx       Parallax
     * not in DB, we calculate it, see distance below
     */
    private double plx;

    /**
     * the distance in light years
     * distance in DB
     */
    private double distance;

    /**
     * the equatorial coordinates in light years
     * seperated out as x, y, z
     */
    private double[] coordinates;

    /**
     * 124-131  F8.4  mas    e_plx       ?=- Parallax uncertainty
     * not needed
     */
    private double e_plx;

    /**
     * 133-163  A31   ---    r_plx       Reference for the parallax
     * not needed
     */
    private String r_plx;

    /**
     * 165-180  F16.9 mas/yr   pmRA      ?=- Proper motion in right ascension, pmRA*cosDE
     * pmra in DB
     */
    private double pmRA;

    /**
     * 182-197  F16.9 mas/yr e_pmRA      ?=- Proper motion uncertainty in right ascension
     * not needed
     */
    private double e_pmRA;

    /**
     * 199-214  F16.9 mas/yr   pmDE      ?=- Proper motion in declination
     * pmdec in DB
     */
    private double pmDE;

    /**
     * 216-231  F16.9 mas/yr e_pmDE      ?=- Proper motion uncertainty in declination
     * not needed
     */
    private double e_pmDE;

    /**
     * 233-262  A30   ---    r_pmDE      Reference for proper motion
     * not needed
     */
    private String r_pmDE;

    /**
     * 264-271  F8.3  km/s     RV        ?=- Line-of-sight velocity
     * radialVelocity in DB
     */
    private double RV;

    /**
     * 273-280  F8.4  km/s   e_RV        ?=- Line-of-sight velocity uncertainty
     * not needed
     */
    private double e_RV;

    /**
     * 282-300  A19   ---    r_RV        Reference for line-of-sight velocity
     * not needed
     */
    private String r_RV;

    /**
     * 302-309  A8    ---      SpType    Spectral type
     * spectralClass in DB
     */
    private String SpType;

    /**
     * 311-335  A25   ---    r_SpType    Reference for spectral type
     * not needed
     */
    private String r_SpType;

    /**
     * 337-348  A12   ---      SpMethod  Method used to derive the spectral type
     * not needed
     */
    private String SpMethod;

    /**
     * 350-351  I2    ---      GCode     [2/20]? Code for Gmag origin (2)
     * not needed
     */
    private int GCode;

    /**
     * 353-361  F9.6  mag      Gmag      ?=- Gaia G band magnitude measured, given only if GCode is 2 or 3
     * not needed
     */
    private double Gmag;

    /**
     * 363-368  F6.2  mag      Gest      ?=- Gaia G band magnitude estimated, given only if GCode is 10 or 20
     * not needed
     */
    private double Gest;

    /**
     * 370-378  F9.6  mag      GBPmag    ?=- Gaia BP band magnitude measured, given only if GCode is 2 or 3
     *
     */
    private double GBPmag;

    /**
     * 380-388  F9.6  mag      GRPmag    ?=- Gaia RP band magnitude measured, given only if GCode is 2 or 3
     * not needed
     */
    private double GRPmag;

    /**
     * 390-396  F7.3  mag      Umag      ?=- U band magnitude
     *  not needed
     */
    private double Umag;

    /**
     * 398-404  F7.3  mag      Bmag      ?=- B band magnitude
     * not needed
     */
    private double Bmag;

    /**
     * 406-412  F7.3  mag      Vmag      ?=- V band magnitude
     * not needed
     */
    private double Vmag;

    /**
     * 414-420  F7.3  mag      Rmag      ?=- R band magnitude
     * not needed
     */
    private double Rmag;

    /**
     * 422-428  F7.3  mag      Imag      ?=- I band magnitude
     * not needed
     */
    private double Imag;

    /**
     * 430-436  F7.3  mag      Jmag      ?=- J band magnitude
     * not needed
     */
    private double Jmag;

    /**
     * 438-444  F7.3  mag      Hmag      ?=- H band magnitude
     * not needed
     */
    private double Hmag;

    /**
     * 446-452  F7.3  mag      Ksmag     ?=- Ks band magnitude
     * not needed
     */
    private double Ksmag;

    /**
     * 454-474  A21   ---    r_Sys       Reference for multiplicity or exoplanets
     * not needed
     */
    private String r_Sys;

    /**
     * 476  I1    ---      Nexopl    ? Number of confirmed exoplanets
     * number of exoplanets in DB
     */
    private int Nexopl;

    /**
     * 478-496  I19   ---      GaiaDR2   ?=- Gaia DR2 identifier
     * gaiaDR2 in DB
     */
    private long GaiaDR2;

    /**
     * 498-516  I19   ---      GaiaEDR3  ?=- Gaia EDR3 identifier
     * gaiaEDR3 in DB
     */
    private long GaiaEDR3;

    /**
     * 518-543  A26   ---      SIMBAD    Name resolved by SIMBAD
     * simbadId in DB
     */
    private String SIMBAD;

    /**
     * 545-561  A17   ---      Common    Common name
     * commonName in DB
     */
    private String Common;

    /**
     * 563-572  A10   ---      GJ        Gliese & Jahreiss catalogue identifier,
     * add to catalogIdList in DB
     */
    private String GJ;

    /**
     * 574-585  A12   ---      HD        Henry Draper catalogue identifier
     * add to catalogIdList in DB
     */
    private String HD;

    /**
     * 587-596  A10   ---      HIP       Hipparcos catalogue identifier
     * add to catalogIdList in DB
     */
    private String HIP;

    /**
     * 598-945  A348  ---      Com       Additional comments on exoplanets,
     * multiplicity, etc
     * add to notes in DB
     */
    private String Com;

}
