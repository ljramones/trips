package com.teamgannon.trips.planetary.modelling.chemical;

import com.opencsv.bean.CsvToBeanBuilder;
import com.teamgannon.trips.config.application.Localization;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/******************************************************************************
 *  Compilation:  javac MolecularWeight.java
 *  Execution:    java MolecularWeight
 *  Dependencies: ST.java StdIn.java elements.txt
 *
 *  Reads in a list of elements and their molecular weights.
 *  Prompts the user for the molecular description of a chemical
 *  formula and prints out its molecular weight.
 *
 *  % java MolecularWeight
 *  H2.O
 *  Molecular weight of H2.O = 18.020000
 *  N.H4.N.O3
 *  Molecular weight of N.H4.N.O3 = 80.060000
 *
 ******************************************************************************/

@Slf4j
@Component
public class MolecularWeightCalculator {

    private final Localization localization;

    private String elementCount = "^[A-Z]?[a-z]*[0-9]*";

    private String element = "^[A-Z]?[a-z]*";

    private Pattern elementCountPattern;
    private Pattern elementPattern;

    Map<String, AtomicElement> elementMap = new HashMap<>();

    public MolecularWeightCalculator(Localization localization) {
        this.localization = localization;
    }

    /**
     * load the atomic elements for look up
     */
    @PostConstruct
    public void loadElements() {
        elementCountPattern = Pattern.compile(elementCount);
        elementPattern = Pattern.compile(element);

        // Path.of(...).resolve(...) inserts a separator even when programdata
        // has no trailing slash.
        File file = Path.of(localization.getProgramdata()).resolve("molecularWeight.csv").toFile();
        if (!file.exists()) {
            // Optional data file — molecular weight lookups just won't work.
            // Don't log a stack trace for the first-run case.
            log.warn("Molecular weight data file not found at {} — atmospheric chemistry calculations will be unavailable. "
                    + "Install the optional data bundle to enable this feature.", file);
            return;
        }
        try {
            List<AtomicElement> beans = new CsvToBeanBuilder(new FileReader(file))
                    .withType(AtomicElement.class)
                    .build()
                    .parse();
            for (AtomicElement element : beans) {
                elementMap.put(element.getSymbol(), element);
            }
            MolecularSpecies molecularSpecies = getMolecularWeight("C12H26");
            log.info("Atomic-element table loaded; C12H26 = {}", molecularSpecies);
        } catch (FileNotFoundException e) {
            // File existed when we checked but disappeared between exists() and
            // open — that's a real I/O race worth a full stack trace.
            log.error("Failed to read molecular weight data from {}", file, e);
        }
    }

    /**
     * breakdow the signature and do an assay
     *
     * @param molecularFormula the molecular formula
     * @return the breakdown of the molecular species
     */
    public MolecularSpecies getMolecularWeight(String molecularFormula) {
        MolecularSpecies species = new MolecularSpecies();
        species.setSignature(molecularFormula);

        while (molecularFormula.length() > 0) {
            // look for an element and count
            Matcher elementCountMatcher = elementCountPattern.matcher(molecularFormula);
            if (elementCountMatcher.find()) {
                String mol = elementCountMatcher.group();
                // match just the element to do a lookup
                Matcher elementMatcher = elementPattern.matcher(molecularFormula);
                if (elementMatcher.find()) {
                    String element = elementMatcher.group();
                    // get the element
                    AtomicElement atomicElement = elementMap.get(element);
                    double weight = atomicElement.getAtomicWeight();
                    // find out how much of it there is
                    int molCount = Integer.parseInt(mol.substring(element.length()));
                    // add the contribution of this element and its count
                    species.getMoleculeDescriptorList().add(MoleculeDescriptor.builder().element(atomicElement).count(molCount).build());
                }
                molecularFormula = molecularFormula.substring(mol.length());
            }
        }
        return species;
    }

}
