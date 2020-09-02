The HYG Database
published by David on Sat, 2006-03-18 04:26
The current version of the database is now hosted at Github as well as here.

The HYG database (v3.0) is a compilation of interesting (to me, anyway) stellar data from a variety of catalogs. It is useful for background information on all sorts of data: star names, positions, brightnesses, distances, and spectrum information. It also powers the charts elsewhere on this site.

Downloads
Choose the version of the database that best serves your needs:

HYG 3.0: Database containing all stars in Hipparcos, Yale Bright Star, and Gliese catalogs (almost 120,000 stars, 14 MB)
HYG 2.0: Older version of the full Hipparcos/Yale/Gliese database (almost 120,000 stars, 9 MB)
HYG v 1.1 database: Database containing all stars brighter than magnitude +9.0, or closer than 50 parsecs.(87476 stars)
About the HYG Database
The database is a subset of the data in three major catalogs: the Hipparcos Catalog,the Yale Bright Star Catalog (5th Edition), and the Gliese Catalog of Nearby Stars (3rd Edition). Each of these catalogs contains information useful to amateur astronomers:

The Hipparcos catalog is the largest collection of high-accuracy stellar positional data, particularly parallaxes, which makes it useful as a starting point for stellar distance data.
The Yale Bright Star Catalog contains basic data on essentially all naked-eye stars, including much information (such as the traditional Bayer Greek letters and Flamsteed numbers) missing from many other catalogs
The Gliese catalog is the most comprehensive catalog of nearby stars (those within 75 light years of the Sun). It contains many fainter stars not found in Hipparcos.
The name of the database comes from the three catalogs comprising its data: Hipparcos, Yale, and Gliese.

All told, this database contains ALL stars that are either brighter than a certain magnitude cutoff (magnitude +7.5 to +9.0) or within 50 parsecs (about 160 light years) from the Sun. The current version, v. 3.0, has no magnitude cutoff: any star in Hipparcos, Yale, or Gliese is represented.

The database is a comma separated values (CSV) file that can be imported into most database and spreadsheet programs. On this web site it is stored as a Zip file or a GZ file, which most popular unzippers can open.

Fields in the Database
Version 3: The field content is very nearly the same as in Version 2, but the column headers are somewhat different, and a few extra fields (for variable star range and multiple star info) have been added to the end of each record. For a full list of the updated column names, see the official database documentation on Github.

Fields labeled with "*" exist only in version 2.0 or higher. Also, since I used a larger set of data for this version, the StarID differs from versions 1.*

StarID: The database primary key from a larger "master database" of stars.
HD: The star's ID in the Henry Draper catalog, if known.
HR: The star's ID in the Harvard Revised catalog, which is the same as its number in the Yale Bright Star Catalog.
Gliese: The star's ID in the third edition of the Gliese Catalog of Nearby Stars.
BayerFlamsteed: The Bayer / Flamsteed
designation, from the Fifth Edition of the Yale Bright Star Catalog. This is a combination of the two designations. The Flamsteed number, if present, is given first; then a three-letter abbreviation for the Bayer Greek letter; the Bayer superscript number, if present; and finally, the three-letter constellation abbreviation. Thus Alpha Andromedae has the field value "21Alp And", and Kappa1 Sculptoris (no Flamsteed number) has "Kap1Scl".
RA, Dec: The star's right ascension and declination, for epoch 2000.0. Stars present only in the Gliese Catalog, which uses 1950.0 coordinates, have had these coordinates precessed to 2000.
ProperName: A common name for the star, such as "Barnard's Star" or "Sirius". I have taken these names primarily from the Hipparcos project's web site, which lists representative names for the 150 brightest stars and many of the 150 closest stars. I have added a few names to this list. Most of the additions are designations from catalogs mostly now forgotten (e.g., Lalande, Groombridge, and Gould ["G."]) except for certain nearby stars which are still best known by these designations.
Distance: The star's distance in parsecs, the most common unit in astrometry. To convert parsecs to light years, multiply by 3.262. A value of 10000000 indicates missing or dubious (e.g., negative) parallax data in Hipparcos.
Mag: The star's apparent visual magnitude.
AbsMag: The star's absolute visual magnitude (its apparent magnitude from a distance of 10 parsecs).
Spectrum: The star's spectral type, if known.
ColorIndex: The star's color index (blue magnitude - visual magnitude), where known.
* X,Y,Z: The Cartesian coordinates of the star, in a system based on the equatorial coordinates as seen from Earth. +X is in the direction of the vernal equinox (at epoch 2000), +Z towards the north celestial pole, and +Y in the direction of R.A. 6 hours, declination 0 degrees.
* VX,VY,VZ: The Cartesian velocity components of the star, in the same coordinate system described immediately above. They are determined from the proper motion and the radial velocity (when known). The velocity unit is parsecs per year; these are small values (around 10-5 to 10-6), but they enormously simplify calculations using parsecs as base units for celestial mapping.
Database Construction
I came up with this database while creating the 3D Universe web site. I needed a reference that would let me search for groups of stars by magnitude or distance, while giving me more information than was contained in any one catalog.

I started with the Hipparcos data. The Hipparcos data set represents by far the most comprehensive collection of stellar distance and brightness data in existence, except for very low-luminosity stars. Essentially all naked-eye stars (in fact, most stars down to about apparent magnitude +9 and many others down to about +11) are represented in the Hipparcos catalog.

In older versions of the dataset, I first prepared a subset of the Hipparcos data. I did this for boring technical reasons that no longer apply, so version 2.0 uses the entire Hipparcos catalog.

I next consulted the Gliese catalog to fill in gaps in the Hipparcos catalog, and to add various Gliese data to the catalog. In particular, the Gliese catalog ID is a common reference for nearby stars, and the Gliese catalog contains radial velocity data, which Hipparcos lacks. Additionally, though Hipparcos distances are generally superior to Gliese data, the Hipparcos catalog missed many nearby stars that were below its magnitude cutoff.

To cross-reference stars, I used the Henry Draper catalog number, whenever present, to add Gliese data to the Hipparcos catalog. Many of the faintest stars lacked this catalog number, so I compared the positions and brightnesses of Gliese stars to those in Hipparcos, and if they matched to within a certain tolerance, I assigned the appropriate Gliese data to the Hipparcos star. Stars that failed both references were then added to the end of the Hipparcos list.

I also converted Hipparcos apparent magnitudes to Gliese values for all components of known multiple stars in the latter catalog. Again, the Hipparcos magnitude measurements are often superior, but the Hipparcos catalog treats multiple stars inconsistently. In particular, it breaks some out as separate stars (e.g., Alpha Centauri) but merges others (such as Capella, 70 Ophiuchi, and many others). By contrast the Gliese catalog breaks all known multiple stars, excluding those too close to be separated optically, into their components, and gives each one a magnitude.

I then calculated absolute magnitudes for all stars, added those to the database, and added about 250 proper names. Then, again using Henry Draper as a cross reference, I added data from the Yale Bright Star Catalog: HR numbers, radial velocities (if not already added from Gliese), and the Bayer and Flamsteed designations. Finally, I added a number of radial velocities from the Wilson Evans Batten catalogue to stars that didn't already have that information.

These steps resulted in the full database. To make the various subsets, I took the resulting database and extracted subsets of the data.

Database Quality Issues
With over 100,000 stars to worry about, I generally couldn't go in and edit suspect records by hand. Consequently, there are some issues that serious users may want to be aware of:

The spectral types, in general, come from the Hipparcos catalog. A few stars -- those found only in Gliese, have a spectral type from that catalog. The spectral types from Hipparcos have not been closely vetted and I have already found some probable errors. For example, the spectral type of 36 Ophiuchi B (a double star that was merged in Hipparcos) is given as K2 III (giant), when its luminosity clearly indicates K2 V (main sequence). Also, the star HIP 84720 (Gliese 666 A) is listed as M0 V, whereas its luminosity and color index are more consistent with a late G-type star (about G8 V). M0 V appears to be the spectral type of Gliese 666 B, a companion to this star. Use the spectral types with caution.
There may be errors in the Henry Draper numbers in one or more catalogs, leading to false cross-references.
There may be errors in the matching of Gliese stars to Hipparcos stars by position and magnitude. In general, this is likely to be an issue only for multiple stars with highly uncertain magnitudes in both catalogs, as the position constraints were fairly severe (stars had to have positions matching to +/- 0.15 degrees, less than the radius of the full Moon). I have not seen any apparent errors on scanning the database thus far, but this is one area that could be a problem.
Radial velocity information can be quite uncertain. Uncertainties of a few km/second are not unusual. There are 3 primary sources: the values in the Gliese catalog, the values in the Yale catalog, and the Wilson Evans Batten catalog mentioned earlier, in that order. I do not yet have a detailed breakdown of the uncertainties in these sources.
In short, though I have done what I can, I can't warrant the database to be error-proof. If you need to launch probes to all the stars in the database, you might want to give it a more thorough going-over before doing so :-)

Other Issues
The data in this database are subject to change. I may add or delete stuff as I feel like. If there are any really large changes, I will post copies to this site.