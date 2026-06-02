package com.teamgannon.trips.service.export;

import com.teamgannon.trips.jpa.model.StarObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared CSV serialization for star exports.
 * <p>
 * Column order must match {@code RegularStarCatalogCsvReader.parseAstroCSVStar()}.
 */
public final class StarCsvFormatter {

    private StarCsvFormatter() {
    }

    public static @NotNull String headers() {
        return "id," +                    // 0
                "dataSetName," +          // 1
                "displayName," +          // 2
                "commonName," +           // 3
                "systemName," +           // 4
                "epoch," +                // 5
                "constellationName," +    // 6
                "mass," +                 // 7
                "notes," +                // 8
                "source," +               // 9
                "catalogIdList," +        // 10
                "simbadId," +             // 11
                "gaiaDR2," +              // 12
                "radius," +               // 13
                "ra," +                   // 14
                "declination," +          // 15
                "pmra," +                 // 16
                "pmdec," +                // 17
                "distance," +             // 18
                "radialVelocity," +       // 19
                "spectralClass," +        // 20
                "temperature," +          // 21
                "realStar," +             // 22
                "bprp," +                 // 23
                "bpg," +                  // 24
                "grp," +                  // 25
                "luminosity," +           // 26
                "magu," +                 // 27
                "magb," +                 // 28
                "magv," +                 // 29
                "magr," +                 // 30
                "magi," +                 // 31
                // 32-42: worldbuilding columns (other, anomaly, polity, worldType,
                // fuelType, portType, populationType, techType, productType,
                // milSpaceType, milPlanType) removed by normalization task.
                "age," +                  // 43
                "metallicity," +          // 44
                "miscText1," +            // 45
                "miscText2," +            // 46
                "miscText3," +            // 47
                "miscText4," +            // 48
                "miscText5," +            // 49
                "miscNum1," +             // 50
                "miscNum2," +             // 51
                "miscNum3," +             // 52
                "miscNum4," +             // 53
                "miscNum5," +             // 54
                "numExoplanets," +        // 55
                "absoluteMagnitude," +    // 56
                "gaiaDR3," +              // 57
                "x," +                    // 58
                "y," +                    // 59
                "z," +                    // 60
                "parallax" +              // 61
                "\n";
    }

    public static @NotNull String format(@NotNull StarObject starObject) {
        StringBuilder csvBuilder = new StringBuilder(1024);

        appendField(csvBuilder, starObject.getId() != null ? starObject.getId().toString() : null);
        appendField(csvBuilder, starObject.getDataSetName());
        appendField(csvBuilder, starObject.getDisplayName());
        appendField(csvBuilder, starObject.getCommonName());
        appendField(csvBuilder, starObject.getSystemName());
        appendField(csvBuilder, starObject.getEpoch());
        appendField(csvBuilder, starObject.getConstellationName());
        csvBuilder.append(starObject.getMass()).append(", ");
        appendField(csvBuilder, starObject.getNotes());
        appendField(csvBuilder, starObject.getSource());
        var catalogIds = starObject.getCatalogIdList();
        csvBuilder.append(catalogIds != null ? String.join("~", catalogIds) : "").append(", ");
        appendField(csvBuilder, starObject.getSimbadId());
        appendField(csvBuilder, starObject.getGaiaDR2CatId());
        csvBuilder.append(starObject.getRadius()).append(", ");
        csvBuilder.append(starObject.getRa()).append(", ");
        csvBuilder.append(starObject.getDeclination()).append(", ");
        csvBuilder.append(starObject.getPmra()).append(", ");
        csvBuilder.append(starObject.getPmdec()).append(", ");
        csvBuilder.append(starObject.getDistance()).append(", ");
        csvBuilder.append(starObject.getRadialVelocity()).append(", ");
        appendField(csvBuilder, starObject.getSpectralClass());
        csvBuilder.append(starObject.getTemperature()).append(", ");
        csvBuilder.append(starObject.isRealStar()).append(", ");
        csvBuilder.append(starObject.getBprp()).append(", ");
        csvBuilder.append(starObject.getBpg()).append(", ");
        csvBuilder.append(starObject.getGrp()).append(", ");
        appendField(csvBuilder, starObject.getLuminosity());
        csvBuilder.append(starObject.getMagu()).append(", ");
        csvBuilder.append(starObject.getMagb()).append(", ");
        csvBuilder.append(starObject.getMagv()).append(", ");
        csvBuilder.append(starObject.getMagr()).append(", ");
        csvBuilder.append(starObject.getMagi()).append(", ");
        // 32-42: worldbuilding fields removed by normalization task.
        csvBuilder.append(starObject.getAge()).append(", ");
        csvBuilder.append(starObject.getMetallicity()).append(", ");
        // 45-49: miscText1..5 dropped in V5 (Issue 31/54) — empty placeholders
        csvBuilder.append(", , , , , ");
        // 50-54: miscNum1..5 dropped in V5 (Issue 31/54) — zero placeholders
        csvBuilder.append("0, 0, 0, 0, 0, ");
        csvBuilder.append(starObject.getNumExoplanets()).append(", ");
        appendField(csvBuilder, starObject.getAbsoluteMagnitude());
        appendField(csvBuilder, starObject.getGaiaDR3CatId());
        csvBuilder.append(starObject.getX()).append(", ");
        csvBuilder.append(starObject.getY()).append(", ");
        csvBuilder.append(starObject.getZ()).append(", ");
        csvBuilder.append(starObject.getParallax());
        csvBuilder.append('\n');

        return csvBuilder.toString();
    }

    private static void appendField(@NotNull StringBuilder sb, @Nullable String value) {
        if (value != null) {
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                sb.append(c == ',' ? '~' : c);
            }
        }
        sb.append(", ");
    }
}
