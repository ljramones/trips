package com.teamgannon.trips.dialogs.tycho2hip;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BayerChecker {

    private static final Set<String> GREEK_LETTERS;
    private static final Set<String> GENITIVE_CONSTELLATIONS;

    static {
        GREEK_LETTERS = new HashSet<>();
        // Add Greek letters and their English transliterations
        String[][] letters = {
                {"α", "Alpha", "alpha"},
                {"β", "Beta", "beta"},
                {"γ", "Gamma", "gamma"},
                {"δ", "Delta", "delta"},
                {"ε", "Epsilon", "epsilon"},
                {"ζ", "Zeta", "zeta"},
                {"η", "Eta", "eta"},
                {"θ", "Theta", "theta"},
                {"ι", "Iota", "iota"},
                {"κ", "Kappa", "kappa"},
                {"λ", "Lambda", "lambda"},
                {"μ", "Mu", "mu"},
                {"ν", "Nu", "nu"},
                {"ξ", "Xi", "xi"},
                {"ο", "Omicron", "omicron"},
                {"π", "Pi", "pi"},
                {"ρ", "Rho", "rho"},
                {"σ", "Sigma", "sigma"},
                {"τ", "Tau", "tau"},
                {"υ", "Upsilon", "upsilon"},
                {"φ", "Phi", "phi"},
                {"χ", "Chi", "chi"},
                {"ψ", "Psi", "psi"},
                {"ω", "Omega", "omega"}
        };

        for (String[] letter : letters) {
            GREEK_LETTERS.addAll(Arrays.asList(letter));
        }

        GENITIVE_CONSTELLATIONS = new HashSet<>();

        // Add genitive forms of constellations
        String[][] constellations = {
                {"Andromeda", "Andromedae"},
                {"Antlia", "Antliae"},
                {"Apus", "Apodis"},
                {"Aquarius", "Aquarii"},
                {"Aquila", "Aquilae"},
                {"Ara", "Arae"},
                {"Aries", "Arietis"},
                {"Auriga", "Aurigae"},
                {"Boötes", "Boötis"},
                {"Caelum", "Caeli"},
                {"Camelopardalis", "Camelopardalis"}, // The genitive form is the same as the nominative
                {"Cancer", "Cancri"},
                {"Canes Venatici", "Canum Venaticorum"},
                {"Canis Major", "Canis Majoris"},
                {"Canis Minor", "Canis Minoris"},
                {"Capricornus", "Capricorni"},
                {"Carina", "Carinae"},
                {"Cassiopeia", "Cassiopeiae"},
                {"Centaurus", "Centauri"},
                {"Cepheus", "Cephei"},
                {"Cetus", "Ceti"},
                {"Chamaeleon", "Chamaeleontis"},
                {"Circinus", "Circini"},
                {"Columba", "Columbae"},
                {"Coma Berenices", "Comae Berenices"},
                {"Corona Australis", "Coronae Australis"},
                {"Corona Borealis", "Coronae Borealis"},
                {"Corvus", "Corvi"},
                {"Crater", "Crateris"},
                {"Crux", "Crucis"},
                {"Cygnus", "Cygni"},
                {"Delphinus", "Delphini"},
                {"Dorado", "Doradus"},
                {"Draco", "Draconis"},
                {"Equuleus", "Equulei"},
                {"Eridanus", "Eridani"},
                {"Fornax", "Fornacis"},
                {"Gemini", "Geminorum"},
                {"Grus", "Gruis"},
                {"Hercules", "Herculis"},
                {"Horologium", "Horologii"},
                {"Hydra", "Hydrae"},
                {"Hydrus", "Hydri"},
                {"Indus", "Indi"},
                {"Lacerta", "Lacertae"},
                {"Leo", "Leonis"},
                {"Leo Minor", "Leonis Minoris"},
                {"Lepus", "Leporis"},
                {"Libra", "Librae"},
                {"Lupus", "Lupi"},
                {"Lynx", "Lyncis"},
                {"Lyra", "Lyrae"},
                {"Mensa", "Mensae"},
                {"Microscopium", "Microscopii"},
                {"Monoceros", "Monocerotis"},
                {"Musca", "Muscae"},
                {"Norma", "Normae"},
                {"Octans", "Octantis"},
                {"Ophiuchus", "Ophiuchi"},
                {"Orion", "Orionis"},
                {"Pavo", "Pavonis"},
                {"Pegasus", "Pegasi"},
                {"Perseus", "Persei"},
                {"Phoenix", "Phoenicis"},
                {"Pictor", "Pictoris"},
                {"Pisces", "Piscium"},
                {"Piscis Austrinus", "Piscis Austrini"},
                {"Puppis", "Puppis"},
                {"Pyxis", "Pyxidis"},
                {"Reticulum", "Reticuli"},
                {"Sagitta", "Sagittae"},
                {"Sagittarius", "Sagittarii"},
                {"Scorpius", "Scorpii"},
                {"Sculptor", "Sculptoris"},
                {"Scutum", "Scuti"},
                {"Serpens", "Serpentis"},
                {"Sextans", "Sextantis"},
                {"Taurus", "Tauri"},
                {"Telescopium", "Telescopii"},
                {"Triangulum", "Trianguli"},
                {"Triangulum Australe", "Trianguli Australis"},
                {"Tucana", "Tucanae"},
                {"Ursa Major", "Ursae Majoris"},
                {"Ursa Minor", "Ursae Minoris"},
                {"Vela", "Velorum"},
                {"Virgo", "Virginis"},
                {"Volans", "Volantis"},
                {"Vulpecula", "Vulpeculae"}
        };

        for (String[] constellation : constellations) {
            // The first entry is the nominative form, the second is the genitive form
            GENITIVE_CONSTELLATIONS.add(constellation[1]);
        }
    }

    public static boolean isBayerFormat(String starName) {
        if (starName == null || starName.isEmpty()) {
            return false;
        }

        String[] parts = starName.split(" ");
        if (parts.length != 2) {
            return false;
        }

        return GREEK_LETTERS.contains(parts[0]) && GENITIVE_CONSTELLATIONS.contains(parts[1]);
    }

    public static boolean isFlamsteedFormat(String starName) {
        if (starName == null || starName.isEmpty()) {
            return false;
        }

        String[] parts = starName.split(" ");
        if (parts.length != 2) {
            return false;
        }

        try {
            Integer.parseInt(parts[0]); // Check if the first part is a number
        } catch (NumberFormatException e) {
            return false; // First part isn't a number
        }

        return GENITIVE_CONSTELLATIONS.contains(parts[1]);
    }

    public static void main(String[] args) {
        System.out.println(isBayerFormat("α Centauri")); // true
        System.out.println(isBayerFormat("alpha Centauri")); // true
        System.out.println(isBayerFormat("alpha Centauri")); // true
        System.out.println(isBayerFormat("β Orionis"));      // Depends on if "β" and "Orionis" are added to the sets
    }
}
