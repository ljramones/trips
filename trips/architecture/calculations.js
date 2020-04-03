Memory = "0";      // initialise memory variable
Current = "0";      //   and value of Display ("current" value)
Operation = 0;      // Records code for eg * / etc.
MAXLENGTH = 30;     // maximum number of digits before decimal!

globalJRA = 0;
globalJDec = 0;
globalUserYearRA = 0;
globalUserYearDec = 0;
globalUserYear = 0;
globalCurrentYearRA = 0;
globalCurrentYearDec = 0;
globalCurrentYear = 0;
globalBRA = 0;
globalBDec = 0;
globalOldL = 0;
globalOldB = 0;
globalNewL = 0;
globalNewB = 0;

pi = 3.1415926536;
toDegrees = 180.0 / pi;

biggest_current_year = 2020.0;  // Allow a bit of future so don't have to update every year.

var jdate = new Date();
var nowYear = jdate.getFullYear() + ( 30.0 * jdate.getMonth() + jdate.getDay() ) / 365.25;


// B1950 to J2000   From Explanatory Supplement
var BtoJ = new Array(
    0.9999256782, -0.0111820611, -0.0048579477,
    0.0111820610, 0.9999374784, -0.0000271765,
    0.0048579479, -0.0000271474, 0.9999881997);
// No matrices in Ecmascript, this is a 3x3 stored in reading order.
// Various definitions of this flattenedMatrix differ in the sixth decimal,
// E.g. the ApJ article gives the first row as
// 0.9999257079523629, -0.0111789381377700, -0.0048590038153592

// J2000 to B1950   From Explanatory Supplement
var JtoB = new Array(
    0.9999256795, 0.0111814828, 0.0048590039,
    -0.0111814828, 0.9999374849, -0.0000271771,
    -0.0048590040, -0.0000271557, 0.9999881946);

// From J2000 to "galactic coordinates"
// Spherical Astronomy by Green, equation 14.55, page 355
var JtoG = new Array(
    -0.054876, -0.873437, -0.483835,
    0.494109, -0.444830, 0.746982,
    -0.867666, -0.198076, 0.455984);

// Aha, printed good article from ApJ at
// http://adsabs.harvard.edu/full/1989A&A...218..325M
// The ApJ flattenedMatrix for J2000 to Galactic agrees with Smart to six digits.

// From ApJ article, notice this is about the transform of Green
var JtoGapj = new Array(
    -0.054875529, 0.494109454, -0.867666136,
    -0.873437105, -0.444829594, -0.198076390,
    -0.483834992, 0.746982249, 0.455983795);

// 17h 45m -29 degrees is about the location of the Galactic Center, new coords.
// And the Green values give the right answer J => Galactic

// From http://idlastro.gsfc.nasa.gov/ftp/pro/astro/gal_uvw.pro
// A_G = [ [ 0.0548755604, +0.4941094279, -0.8676661490], $
//         [ 0.8734370902, -0.4448296300, -0.1980763734], $
//         [ 0.4838350155,  0.7469822445, +0.4559837762] ]
// This agrees with the ApJ article except for signs of the first column!

var GtoJ = new Array(
    -0.0548755604, 0.4941094279, -0.8676661490,
    -0.8734370902, -0.4448296300, -0.1980763734,
    -0.4838350155, 0.7469822445, 0.4559837762);
// Yes, the signs in the ApJ article are the good ones!
// BUT ApJ swears that these are the coords for a J => G transform!
// Maybe my flattenedMatrix multiply is wrong?  No, I get the right answer for B <--> J
// Aha!  a) the above is the ApJ J->G and b) they wrote their flattenedMatrix
// as the transpose because the used row/column vectors or whatever.
// So the GtoJ above is really the inverse-transform of the JtoG ...
// Can I find a real GtoJ direction-cosine transform?

// http://njoubert.com/assets/astronomy/lab4.pdf
// offers this flattenedMatrix:
//   -0.054876  -0.873437  -0.483835
//    0.494109  -0.444830   0.746982
//   -0.867666  -0.198076   0.455984
// and states "convert Galactic Coordinates (l,b) to Equatorial Cordinates (alpha,delta)"
// Notice that the values are Green's values.


// jhu.edu precession routine says that:
//  0,0 in B1950 is 0h 2m 33.77s +0 16 42.1 in J2000
//  0,0 in J2000 is 23h 57m 26.33s  -0 16 42.3
// ... and I get those correct.


// From Fundamental Astronomy by Karttunen et al:
// The galactic coordinates can be obtained from the equatorial
// ones with the transformation equations
//  sin (Ln - L) cos (B) = cos (delta) sin (alpha - AP)
//  cos (Ln - L) cos (B) = - cos(delta) sin(deltaP) cos(alpha-AP)
//   + sin(delta) cos (deltaP)
//  sin (B) = cos (delta) cos (deltaP) cos (alpha-AP)
//   + sin (delta) sin (deltaP)
// where the direction of the Galactic north pole is
// alphaP = 12h 51.4m, deltaP = 27d 08', and the
// galactic longitude of the celestial pole Ln = 123.0 degrees.

// Note from wikipedia: In order to be a rotation flattenedMatrix, it needs
// to have det == 1 and have its transpose equal its inverse.

// Formulation of a flattenedMatrix from the three rotations is described at
// http://chsfpc5.chem.ncsu.edu/~franzen/CH795Z/math/lab_frame/lab_frame.html
// ...
// Let the three angles be A B C.  And let c and s represent cos and sin.
// The flattenedMatrix is [but beware my typos]
//     cCcBcA-sCsA  sCcBcA+cCsA   -sBcA
//     -cCcBsA-sCcA  -sCcBsA+cCcA   sBsA
//     cCsB         sCsB            cB
// (And one may need the transpose, depending on pre-multiply vs post-multiply.)
//
// (This same flattenedMatrix (I hope its the same) apears on page 103 of the Supplement.)
//
// Think of the above as being the product of three rotations.
// Start with yourself (say in an airplane), oriented pointing to the old
// north pole (NP) and aligned with the 0,0 location.
// 1. Do a "roll" so that your vertical is aligned with the oldNP--newNP line.
// 2. Do a pitch to point yourself to the new NP.
// 3. To a roll to align yourself with the new 0,0 location.

// If I look at the above and try to find "cB" in the Green table, I see
// that flattenedMatrix[8] = 0.45598 and arccos() of that is 62.872 degrees.
// That agrees excellently with the deltaP = 27.133 -- 90-N because the
// value is "decDec of pole" and decDec is 90 - pole-angle.
// ... but it is not clear how the "three rotation angles" relate to the
// three numbers for pole-position and 'longitude of celestial pole'
// ... reverse engineering from the Green values:
// I know that cB = 0.455984  That makes B = 1.097 radians (62.87 deg)
// so sB = 0.8900. [or -0.89]
// Green m[7] sCsB is -0.198076, so sC = -0.2226, C = -0.2244
// or -12.859 degrees.
// Which corresponds to the 12h 51.4m value: 12.859 deg is 51.4m !
// and the sign is correct ...
// Green m[5] is 0.746982 so sA = 0.8393 and A = 57.067 degrees and
// that matches the Ln of 123.0 !
// Feedng those three angles in to Test gives me correct values for
// m[5] m[6] m[7] and m[8] only ... so I probably have an ambiguity,
// where I, e.g. picked 80 not 100 for arc-sine although the cos is different.
// SO ... trying with B = -62.87 degrees.
// B = -62.87 and C = 11h09m give a good Green m[678]
// Adding an A of 123 degrees gives the wrong sign on m[5]; get correct
// [m5] with A = +237 degrees. This makes m[2] correct also.
// But the other four: m[0134] are not-Green.
// Can I fix this by choosing the
// opposite sign for sB and fixing with A and C?  That makes
// B = +62.87 and C = 12 51m give a good m[678], add A = +57 and get
// good m[25] too.
// (So two solutions for good m[25678]: fair, considering rotation theory.)
// AHA A = +57, B = +62.87 C = 12h51m and my "Test" AGREES with all Green!


// AJ vol 64 p195  "Definition of the New I.A.U. System of Calactic Coordinates"
//    The new north galactic pole is at (B1950)  12h49m  +27.4
//    The new zero of longitude is the great semicircle originating at the
//    new north pole at the position angle 123 degrees with respect to the
//    equatorial pole for 1950.0
//    The old galactic coordinates of the new pole
//    L-old = 347.7  B-old = +88.51   347.7 deg = 23h 10m 48s
//    0,0 of new galactic coords = 17h 42.4m  -28d 55' (1950.0)
//    0,0 of new galactic in terms of old galactic:  L-old 327.69  B-old -1.40

// ( Modern g-long of 33 is about the old long of 0 )

// The website tool at www.astro.utu.fi/EGal/CooC/CooC7.html
// gives these conversions, which look wrong
// Old Galactic 0,0 => New Galactic 327.692  -1.257
// Old Galactic 0,90 => New Galactic 0.0  88.513   (sic l==0)
// Old Galactic 347.7,88.51 => New Galactic 337.667  87.246  (not the new pole, see above)

// Old Calactic 45,45 => New Galactic 12.386  43.549
// Old Galactic 135,45 => New Galactic 101.238  45.308
// Old Galactic 0,0 => Equatorial (2000) 15h 54m 21.387s  -54d 44m 23.562s
// Old Galactic 0,90 => Equatorial (2000) 12h 54m 34.597s   26d 35m 0.342s

// A possible flattenedMatrix for the above is the transpose of
// "Test" with   A = 32.31  B = 1.49  C = 23h 10m 48s ..
//    <much elided>
// Transpose of Test() of  +340  1.49  23 10 48 calculates new 0,0 => old 327.69 -1.40; check.
// SO SO  The above looks good but what IS the A==340 ??
// It is (per my roll-pitch explanation), the angle between the meridian of the old NP
// and the 0,0 meridian.
// Since the change in north pole positionis small, that is very close to the
// sum of the change 12.3 deg (the 23 10 48) and the -33 deg of galac-long change.

var NewToOld = new Array(0.844951, 0.534239, 0.025405, -0.534284, 0.845286, -0.005539, -0.024434, -0.008893, 0.999661);

// The transpose of the above:
var OldToNew = new Array(0.844951, -0.534284, -0.024434, 0.534239, 0.845286, -0.008893, 0.025405, -0.005539, 0.999661);

function MakeMatrix(a, b, c) // For creating direction-cosine flattenedMatrix from angles
{
    var m = new Array(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    var cA = Math.cos(a);
    var sA = Math.sin(a);
    var cB = Math.cos(b);
    var sB = Math.sin(b);
    var cC = Math.cos(c);
    var sC = Math.sin(c);
    m[0] = cC * cB * cA - sC * sA;
    m[1] = sC * cB * cA + cC * sA;
    m[2] = -sB * cA;
    m[3] = -cC * cB * sA - sC * cA;
    m[4] = -sC * cB * sA + cC * cA;
    m[5] = sB * sA;
    m[6] = cC * sB;
    m[7] = sC * sB;
    m[8] = cB;
    return m;
}

// The trig-value flattenedMatrix on page 103 of the Explanatory Supplement is NOT THE SAME as
// the one at http://chsfpc5.chem.ncsu.edu/~franzen/CH795Z/math/lab_frame/lab_frame.html
// Further, I had to use the transpose to get the right answer (matching other 1950->2000
// results), presumably because the Supplement does a post- not a pre- multiply or
// the other way around.
function MakeMatrixSupplement(a, b, c) { // zeta(supp) is A, theta(supp) is B; and the third greek letter is C
    var m = new Array(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    var cA = Math.cos(a);
    var sA = Math.sin(a);
    var cB = Math.cos(b);
    var sB = Math.sin(b);
    var cC = Math.cos(c);
    var sC = Math.sin(c);
    m[0] = cA * cB * cC - sA * sC; // same
    m[3] = -cA * cB * sC - sA * cC;
    m[6] = -cA * sB;
    m[1] = sA * cB * cC + cA * sC;
    m[4] = -sA * cB * sC + cA * cC; // same
    m[7] = -sA * sB;
    m[2] = sB * cC;
    m[5] = -sB * sC;
    m[8] = cB; // same
    return m;
}

// Calculate the three key angles (A B and C in the above)
// Supplement page 104
// To match the above, theta(supp) is B; zeta(supp) is A, and the third greek letter is C.
function Supplement(fixed, date) // Here years, maybe should be JDays
{
    var T = (fixed - 2000.0) / 100.0;
    var t = (date - fixed) / 100.0; // should be Julian days / 36535
    var asec = (2306.218 + 1.397 * T) * t + 1.095 * t * t;
    var bsec = (2004.311 - 0.853 * T) * t - 0.427 * t * t;
    var csec = (2306.218 + 1.397 * T) * t + 0.302 * t * t;
    var SecondsPerRadian = (180.0 / pi) * 3600.0
    m = MakeMatrixSupplement(asec / SecondsPerRadian, bsec / SecondsPerRadian, csec / SecondsPerRadian);
    return m;
}

// Goal is to allow the user maximum flexibility in input syntax,
// because he may be cut-n-pasting from somewhere.
// In particular: Allow text like "h" and "m".  Allow 12h 13.5m and similar.

function Numberish(char3) {
    if (char3 == "0") return 1;
    if (char3 == "1") return 1;
    if (char3 == "2") return 1;
    if (char3 == "3") return 1;
    if (char3 == "4") return 1;
    if (char3 == "5") return 1;
    if (char3 == "6") return 1;
    if (char3 == "7") return 1;
    if (char3 == "8") return 1;
    if (char3 == "9") return 1;
    if (char3 == ".") return 1;
    return 0;
}

function ParseRA(val) {
    var answer = 0;
    var val2 = 0;
    var times = 15.0 * 3600.0;

    // Skip initial blanks
    while ((val.length > 0) && (val.indexOf(" ") == 0)) {
        val = val.substring(1);
    }

    // Special form: initial "+" => degrees, not hours
    if ((val.length > 0) && (val.indexOf("+") == 0)) {
        times = 3600.0;
        val = val.substring(1);
    }

    // Special form: degree sign anywhere => degrees not hours
    if (val.indexOf("?") != -1) {
        times = 3600.0;
    }

// Don't blame me for the ECMAScript String class ...

    while (val.length > 0) // Big loop pulling numbers
    {
        if (!Numberish(val.charAt(0))) {
            val = val.substring(1);
            continue;
        }
        // val[0] is numberish
        var coun = 0;
        while ((coun < val.length) && Numberish(val.charAt(coun))) {
            coun = coun + 1;
        }
        // Have a number in [0..coun)

        val2 = val.substring(0, coun);
        val = val.substring(coun);
        // Have the number in val2 and the rest of the string in val.

        answer = parseFloat(answer) + parseFloat(val2) * times;
        times = times / 60;
    } // big loop pulling numbers

    return answer;
}

// parseFloat and eval seem to work.  ToNumber fails.

function ParseDec(val) {
    var negative = 0;
    var answer = 0;
    var val2 = 0;
    var times = 3600.0;

    // Skip initial blanks
    while ((val.length > 0) && (val.indexOf(" ") == 0)) {
        val = val.substring(1);
    }

    if ((val.length > 0) && (val.indexOf("-") == 0)) {
        negative = 1;
        val = val.substring(1);
    }


// Don't blame me for the ECMAScript String class ...

    while (val.length > 0) // Big loop pulling numbers
    {
        if (!Numberish(val.charAt(0))) {
            val = val.substring(1);
            continue;
        }
        // val[0] is numberish
        var coun = 0;
        while ((coun < val.length) && Numberish(val.charAt(coun))) {
            coun = coun + 1;
        }
        // Have a number in [0..coun)

        val2 = val.substring(0, coun);
        val = val.substring(coun);
        // Have the number in val2 and the rest of the string in val.

        answer = parseFloat(answer) + parseFloat(val2) * times;
        times = times / 60;
    } // big loop pulling numbers

    if (negative) answer = 0.0 - answer;
    return answer;
}


function AllClear()             //Clear ALL entries!
{
    document.Calculator.BRA.value = "";
    document.Calculator.BDec.value = "";

    document.Calculator.JRA.value = "";
    document.Calculator.JDec.value = "";

    document.Calculator.UserYear.value = "";
    document.Calculator.UserYearRA.value = "";
    document.Calculator.UserYearDec.value = "";

    document.Calculator.CurrentYear.value = Trim2(nowYear, 2);
    document.Calculator.CurrentYearRA.value = "";
    document.Calculator.CurrentYearDec.value = "";

    document.Calculator.GalacticL.value = "";
    document.Calculator.GalacticB.value = "";

    document.Calculator.OldGalacticL.value = "";
    document.Calculator.OldGalacticB.value = "";

    document.Calculator.CalcJ2000.value = "";
    document.Calculator.CalcB1950.value = "";
    document.Calculator.OutUserYear.value = "";
    document.Calculator.CalcUserYear.value = "";
    document.Calculator.OutCurrentYear.value = "";
    document.Calculator.CalcCurrentYear.value = "";

    document.Calculator.CalcGalacticNew.value = "";
    document.Calculator.CalcGalacticOld.value = "";


}

function Nonblank(trial) {
    var index = trial.length - 1;
    while (index >= 0) {
        if (trial.charAt(index) != " ") {
            return 1;
        }
        index = index - 1;
    }
    return 0;
}

function RadiansPrintHMS(rad) {
    var ret = new String("");
    var hh = (rad * toDegrees / 15.0) + (0.5 / 36000.0); // Rounding tenths of seconds

    var h = Math.floor(hh);
    hh = hh - h;
    hh = hh * 60;
    var m = Math.floor(hh);
    hh = hh - m;
    hh = hh * 60;
    var s = Math.floor(hh);
    hh = hh - s;
    hh = hh * 10;
    var s10 = Math.floor(hh)
    ret = h + "h " + m + "m " + s + "." + s10 + "s ";
    return ret;
}

function RadiansPrintDMS(rad) {
    var sign2 = "";
    if (rad < 0.0) {
        sign2 = "-";
        rad = 0.0 - rad;
    }
    var hh = rad * toDegrees + (0.5 / 3600.0);
    var h = Math.floor(hh);
    hh = hh - h;
    hh = hh * 60;
    var m = Math.floor(hh);
    hh = hh - m;
    hh = hh * 60;
    var s = Math.floor(hh);
    ret = sign2 + h + "° " + m + "' " + s + "\"";
    return ret;
}


function RadiansPrintDM(rad) {
    var sign2 = "";
    if (rad < 0.0) {
        sign2 = "-";
        rad = 0.0 - rad;
    }

    var hh = rad * toDegrees;
    hh = hh + (0.005 / 60.0); // rounding
    var h = Math.floor(hh);
    hh = hh - h;
    hh = hh * 60;
    var m = Math.floor(hh);
    hh = hh - m; // fraction
    hh = hh * 10;
    var f1 = Math.floor(hh); // Crude but easy way to get leading zeroes in fraction
    hh = hh - f1;
    hh = hh * 10;
    var f2 = Math.floor(hh);
    ret = sign2 + h + "° " + m + "." + f1 + f2 + "'";
    return ret;
}

function RadiansPrintD(rad) {
    var sign2 = "";
    if (rad < 0.0) {
        sign2 = "-";
        rad = 0.0 - rad;
    }

    var hh = rad * toDegrees;
    hh = hh + 0.00005; // rounding
    var h = Math.floor(hh);
    hh = hh - h; // fraction
    hh = hh * 10;
    var f1 = Math.floor(hh); // Crude but easy way to get leading zeroes in fraction
    hh = hh - f1;
    hh = hh * 10;
    var f2 = Math.floor(hh);
    hh = hh - f2;
    hh = hh * 10;
    var f3 = Math.floor(hh);
    hh = hh - f3;
    hh = hh * 10;
    var f4 = Math.floor(hh);
    ret = sign2 + h + "." + f1 + f2 + f3 + f4 + "°";
    return ret;
}

function Transform(radec, matrix) // returns a radec array of two elements
{
    var r0 = new Array(
        Math.cos(radec[0]) * Math.cos(radec[1]),
        Math.sin(radec[0]) * Math.cos(radec[1]),
        Math.sin(radec[1]));

    var s0 = new Array(
        r0[0] * matrix[0] + r0[1] * matrix[1] + r0[2] * matrix[2],
        r0[0] * matrix[3] + r0[1] * matrix[4] + r0[2] * matrix[5],
        r0[0] * matrix[6] + r0[1] * matrix[7] + r0[2] * matrix[8]);

    var r = Math.sqrt(s0[0] * s0[0] + s0[1] * s0[1] + s0[2] * s0[2]);

    var result = new Array(0.0, 0.0);
    result[1] = Math.asin(s0[2] / r); // New decDec in range -90.0 -- +90.0
    // or use sin^2 + cos^2 = 1.0
    var cosaa = ( (s0[0] / r) / Math.cos(result[1]) );
    var sinaa = ( (s0[1] / r) / Math.cos(result[1]) );
    result[0] = Math.atan2(sinaa, cosaa);
    if (result[0] < 0.0) result[0] = result[0] + pi + pi;
    return result;
}

function CalcX(argument)    // "CalculateX" as a name fails.  Why?
{
    var coun = 0;
    var fieldx;
    var which = 0;

    document.Calculator.Display.value = "";


    // Which coordinates did the user supply?
    if (Nonblank(document.Calculator.JRA.value)) {
        fieldx = document.Calculator.JRA;
        coun = coun + 1;
        which = 1;
    }
    if (Nonblank(document.Calculator.BRA.value)) {
        fieldx = document.Calculator.BRA;
        coun = coun + 1;
        which = 2;
    }
    if (Nonblank(document.Calculator.GalacticL.value)) {
        fieldx = document.Calculator.GalacticL;
        coun = coun + 1;
        which = 3;
    }
    if (Nonblank(document.Calculator.OldGalacticL.value)) {
        fieldx = document.Calculator.OldGalacticL;
        coun = coun + 1;
        which = 4;
    }
    if (Nonblank(document.Calculator.UserYearRA.value)) {
        fieldx = document.Calculator.UserYearRA;
        coun = coun + 1;
        which = 5;
    }
    if (Nonblank(document.Calculator.CurrentYearRA.value)) {
        fieldx = document.Calculator.CurrentYearRA;
        coun = coun + 1;
        which = 6;
    }

    if (coun != 1) {
        document.Calculator.Display.value = "Need exactly one input row";
        return 0; // Acting like exit
    }

    var useryear = 0.0;
    var userSuppliedYear = Nonblank(document.Calculator.UserYear.value);
    // The user can supply a year without using the year's RA and Dec -- if he wants output
    if (userSuppliedYear) {
        useryear = parseFloat(document.Calculator.UserYear.value);
        if ((useryear < 1.0) || (useryear > biggest_current_year)) {
            document.Calculator.Display.value = "User-supplied year out of range";
            return 0; // Acting like exit
        }
    }
    else // no user year
    {
        if (which == 5) {
            document.Calculator.Display.value = "For user-supplied equinox, must supply the year";
            return 0; // Acting like exit
        }
    }

    var currentyear = 0.0;
    var currentSuppliedYear = Nonblank(document.Calculator.CurrentYear.value);
    if (currentSuppliedYear) {
        currentyear = parseFloat(document.Calculator.CurrentYear.value);
        if ((currentyear < 1.0) || (currentyear > biggest_current_year)) {
            document.Calculator.Display.value = "'Current' year out of range";
            return 0; // Acting like exit
        }
    }
    else // no current year
    {
        if (which == 6) {
            document.Calculator.Display.value = "Need 'current year' value";
            return 0; // Acting like exit
        }
    }

// First, calculate J2000 coords (easy if supplied) and store.
// Then calculate other coords from J2000 -- even if supplied.
// Then fill the calculated values into the output fields.

    var xradec = new Array(99.0, 99.0);

    if (which == 1) {
        globalJRA = ParseRA(document.Calculator.JRA.value);
        globalJDec = ParseDec(document.Calculator.JDec.value);
        // the above "globals" are in arcseconds.

        if ((globalJRA >= 1296000) || (globalJRA < 0)) {
            document.Calculator.Display.value = "J2000 RA out of range";
            return 0; // Acting like exit
        }
        if ((globalJDec > 324000) || (globalJDec < -324000)) {
            document.Calculator.Display.value = "J2000 Dec out of range";
            return 0; // Acting like exit
        }

        var radec1 = new Array((globalJRA / 3600.0) / toDegrees,
            (globalJDec / 3600.0) / toDegrees);

        xradec = radec1;
    }

    else if (which == 2) {
        globalBRA = ParseRA(document.Calculator.BRA.value);
        globalBDec = ParseDec(document.Calculator.BDec.value);
        // the above "globals" are in arcseconds.

        if ((globalBRA >= 1296000) || (globalBRA < 0)) {
            document.Calculator.Display.value = "B1950 RA out of range";
            return 0; // Acting like exit
        }
        if ((globalBDec > 324000) || (globalBDec < -324000)) {
            document.Calculator.Display.value = "B1950 Dec out of range";
            return 0; // Acting like exit
        }

        var radec2 = new Array((globalBRA / 3600.0) / toDegrees,
            (globalBDec / 3600.0) / toDegrees);

        xradec = Transform(radec2, BtoJ);
    }

    else if (which == 3) {
        globalNewL = ParseDec(document.Calculator.GalacticL.value);
        globalNewB = ParseDec(document.Calculator.GalacticB.value);

        if ((globalNewL >= 1296000) || (globalNewL < 0)) {
            document.Calculator.Display.value = "New Galactic Longitude out of range";
            return 0; // Acting like exit
        }
        if ((globalNewB > 324000) || (globalNewB < -324000)) {
            document.Calculator.Display.value = "New Galactic Latitude out of range";
            return 0; // Acting like exit
        }

        var radec3 = new Array((globalNewL / 3600.0) / toDegrees,
            (globalNewB / 3600.0) / toDegrees);

        xradec = Transform(radec3, GtoJ);
    }

    else if (which == 4) {
        globalOldL = ParseDec(document.Calculator.OldGalacticL.value);
        globalOldB = ParseDec(document.Calculator.OldGalacticB.value);

        if ((globalOldL >= 1296000) || (globalOldL < 0)) {
            document.Calculator.Display.value = "Old Galactic Longitude out of range";
            return 0; // Acting like exit
        }
        if ((globalOldB > 324000) || (globalOldB < -324000)) {
            document.Calculator.Display.value = "Old Galactic Latitude out of range";
            return 0; // Acting like exit
        }

        var radec4 = new Array((globalOldL / 3600.0) / toDegrees,
            (globalOldB / 3600.0) / toDegrees);
        var radec41 = Transform(radec4, OldToNew);
        xradec = Transform(radec41, GtoJ);
    }
    else if (which == 5) {
        if ((useryear < 1.0) || (useryear > biggest_current_year)) {
            document.Calculator.Display.value = "User-supplied year out of range";
            return 0; // Acting like exit
        }
        globalUserYearRA = ParseRA(document.Calculator.UserYearRA.value);
        globalUserYearDec = ParseDec(document.Calculator.UserYearDec.value);
        // the above "globals" are in arcseconds.

        if ((globalUserYearRA >= 1296000) || (globalUserYearRA < 0)) {
            document.Calculator.Display.value = "User Year RA out of range";
            return 0; // Acting like exit
        }
        if ((globalUserYearDec > 324000) || (globalUserYearDec < -324000)) {
            document.Calculator.Display.value = "User Year Dec out of range";
            return 0; // Acting like exit
        }

        var radec5 = new Array((globalUserYearRA / 3600.0) / toDegrees,
            (globalUserYearDec / 3600.0) / toDegrees);

        var UserToJ = Supplement(2000.0, useryear);
        xradec = Transform(radec5, UserToJ);
    } //else which = 5

    else // which == 6   Just like "user year" but with "current year" values
    {
        if ((currentyear < 1.0) || (currentyear > biggest_current_year)) {
            document.Calculator.Display.value = "'Current' year out of range";
            return 0; // Acting like exit
        }
        globalCurrentYearRA = ParseRA(document.Calculator.CurrentYearRA.value);
        globalCurrentYearDec = ParseDec(document.Calculator.CurrentYearDec.value);
        // the above "globals" are in arcseconds.

        if ((globalCurrentYearRA >= 1296000) || (globalCurrentYearRA < 0)) {
            document.Calculator.Display.value = "Current Year RA out of range";
            return 0; // Acting like exit
        }
        if ((globalCurrentYearDec > 324000) || (globalCurrentYearDec < -324000)) {
            document.Calculator.Display.value = "Current Year Dec out of range";
            return 0; // Acting like exit
        }

        var radec6 = new Array((globalCurrentYearRA / 3600.0) / toDegrees,
            (globalCurrentYearDec / 3600.0) / toDegrees);

        var CurrentToJ = Supplement(2000.0, currentyear);
        xradec = Transform(radec6, CurrentToJ);

    } //else which == 65

// OK, have a J2000 value set in xradec.
// Convert to the other forms for display.
// We may convert to J2000 and then back; that is a feature. It lets the
// user see our precision or lack thereof.

    document.Calculator.CalcJ2000.value =
        RadiansPrintHMS(xradec[0]) + "  " + RadiansPrintDMS(xradec[1]) + "   //   +"
        + RadiansPrintD(xradec[0]) + "  /  " + RadiansPrintDM(xradec[1]) + "  "
        + RadiansPrintD(xradec[1]);

    var bradec = Transform(xradec, JtoB);
    document.Calculator.CalcB1950.value =
        RadiansPrintHMS(bradec[0]) + "  " + RadiansPrintDMS(bradec[1]) + "    //    +"
        + RadiansPrintD(bradec[0]) + "  /  " + RadiansPrintDM(bradec[1]) + "  "
        + RadiansPrintD(bradec[1]);

    if (userSuppliedYear) {
        var JtoUser = Supplement(useryear, 2000.0);
        var uradec = Transform(xradec, JtoUser);
        document.Calculator.OutUserYear.value = useryear;
        document.Calculator.CalcUserYear.value =
            RadiansPrintHMS(uradec[0]) + "  " + RadiansPrintDMS(uradec[1]) + "    //    +"
            + RadiansPrintD(uradec[0]) + "  /  " + RadiansPrintDM(uradec[1]) + "  "
            + RadiansPrintD(uradec[1]);
    }
    else {
        document.Calculator.OutUserYear.value = "";
        document.Calculator.CalcUserYear.value = "";
    }

    if (currentSuppliedYear) {
        var JtoCurrent = Supplement(currentyear, 2000.0);
        var cradec = Transform(xradec, JtoCurrent);
        document.Calculator.OutCurrentYear.value = currentyear;
        document.Calculator.CalcCurrentYear.value =
            RadiansPrintHMS(cradec[0]) + "  " + RadiansPrintDMS(cradec[1]) + "    //    +"
            + RadiansPrintD(cradec[0]) + "  /  " + RadiansPrintDM(cradec[1]) + "  "
            + RadiansPrintD(cradec[1]);
    }
    else {
        document.Calculator.OutCurrentYear.value = "";
        document.Calculator.CalcCurrentYear.value = "";
    }


    var galac = Transform(xradec, JtoG);
    document.Calculator.CalcGalacticNew.value = RadiansPrintD(galac[0])
        + "  " + RadiansPrintD(galac[1]);

// Use Green approximation (page 47) to convert Galactic to OldCalactic.
// Note that it is poor near the galactic poles.
    var oldgalacB = 0.0;
    var oldgalacL = 0.0;
    oldgalacB = galac[1] - (1.5 / toDegrees) * Math.cos(galac[0] - 20.0 / toDegrees);
    if (oldgalacB > 90.0 / toDegrees) oldgalacB = 90.0 / toDegrees;
    if (oldgalacB < -90.0 / toDegrees) oldgalacB = -90.0 / toDegrees;

    var nearpole = (galac[1] > 89.0 / toDegrees) || (galac[1] < -89.0 / toDegrees);
    if (nearpole) {
        oldgalacL = 0.0
    }
    else {
        oldgalacL = galac[0] - (32.3 / toDegrees)
            - (1.5 / toDegrees) * Math.tan(galac[1]) * Math.sin(galac[0] - 20.0 / toDegrees);
        if (oldgalacL < 0.0) {
            oldgalacL = oldgalacL + 360.0 / toDegrees;
        }
        if (oldgalacL >= 360.0 / toDegrees) {
            oldgalacL = oldgalacL - 360.0 / toDegrees;
        }
    }

    var oldgalac = Transform(galac, NewToOld);

    document.Calculator.CalcGalacticOld.value = RadiansPrintD(oldgalacL)
        + "  " + RadiansPrintD(oldgalacB) + "    //    " + RadiansPrintD(oldgalac[0])
        + "  " + RadiansPrintD(oldgalac[1]);

}

function Trim(m) {
    var s = new String(m);
    var dot = s.indexOf(".");
    if (dot == -1) return s;
    s = s.substring(0, dot + 7);
    return s;
}
function Trim2(m, dd) {
    var s = new String(m);
    var dot = s.indexOf(".");
    if (dot == -1) return s;
    s = s.substring(0, dot + dd + 1);
    return s;
}

function TestX(argument) {
    globalJRA = ParseRA(document.Calculator.JRA.value);
    globalJDec = ParseDec(document.Calculator.JDec.value);
    globalBRA = ParseRA(document.Calculator.BRA.value);

    m = MakeMatrix(
        (globalJRA / 3600.0) / toDegrees,
        (globalJDec / 3600.0) / toDegrees,
        (globalBRA / 3600.0) / toDegrees);
    document.Calculator.Display.value =
        Trim(m[0]) + ", " + Trim(m[1]) + ", " + Trim(m[2]) + ",   " +
        Trim(m[3]) + ", " + Trim(m[4]) + ", " + Trim(m[5]) + ",   " +
        Trim(m[6]) + ", " + Trim(m[7]) + ", " + Trim(m[8]) + " // " +
        Trim(m[0]) + ", " + Trim(m[3]) + ", " + Trim(m[6]) + ",   " +
        Trim(m[1]) + ", " + Trim(m[4]) + ", " + Trim(m[7]) + ",   " +
        Trim(m[2]) + ", " + Trim(m[5]) + ", " + Trim(m[8])
    return;
}