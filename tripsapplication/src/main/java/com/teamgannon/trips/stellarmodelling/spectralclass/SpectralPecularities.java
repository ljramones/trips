package com.teamgannon.trips.stellarmodelling.spectralclass;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpectralPecularities {

    /**
     * the list of stellar pecularities
     */
    public static Map<String, String> pecularities = Stream.of(new String[][]{
            {":", "uncertain spectral value"},
            {"...", "Undescribed spectral peculiarities exist"},
            {"!", "Special peculiarity"},
            {"comp", "Composite spectrum"},
            {"e", "Emission lines present"},
            {"[e]", "\"Forbidden\" emission lines present"},
            {"er", "\"Reversed\" center of emission lines weaker than edges"},
            {"eq", "Emission lines with P Cygni profile"},
            {"f", "N III and He II emission"},
            {"f*", "N IV λ4058Å is stronger than the N III λ4634Å, λ4640Å, & λ4642Å lines"},
            {"f+", "Si IV λ4089Å & λ4116Å are emitted, in addition to the N III line"},
            {"(f)", "N III emission, absence or weak absorption of He II"},
            {"(f+)", "https://iopscience.iop.org/article/10.1088/0004-6256/138/2/510"},
            {"((f)", "Displays strong He II absorption accompanied by weak N III emissions"},
            {"((f*))", "https://iopscience.iop.org/article/10.1088/0004-6256/138/2/510"},
            {"h", "WR stars with hydrogen emission lines"},
            {"ha", "WR stars with hydrogen seen in both absorption and emission."},
            {"He wk", "Weak Helium lines"},
            {"k", "Spectra with interstellar absorption feat"},
            {"m", "Enhanced metal features"},
            {"n", "Broad nebulous absorption due to spinning"},
            {"nn", "Very broad absorption features"},
            {"neb", "A nebula's spectrum mixed in"},
            {"p", "Unspecified peculiarity, peculiar star"},
            {"pq", "peculiar  spectrum,  similar to the spectra of novae"},
            {"q", "P Cygni profiles"},
            {"s", "narrow/sharp absorption lines"},
            {"ss", "very narrow lines"},
            {"sh", "Shell star features"},
            {"var", "Variable spectral feature"},
            {"v", "Variable spectral feature"},
            {"wl", "Weak lines"},
            {"wk", "Weak lines"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    /**
     * get the associated pecularity for this star
     *
     * @param pecularity the short code we have
     * @return the pecularity
     */
    public String getPecularity(String pecularity) {
        return pecularities.get(pecularity);
    }

}
