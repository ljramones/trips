package com.teamgannon.trips.planetarymodelling.chemical;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * describes a molecule
 */
@Data
public class MolecularSpecies {

    /**
     * moleculor definition
     */
    private String signature;

    /**
     * the piecewise breakdown of the atoms in this
     */
    private List<MoleculeDescriptor> moleculeDescriptorList = new ArrayList<>();

    /**
     * get the weight of the species
     *
     * @return the total weight
     */
    public double getWeight() {
        return moleculeDescriptorList.stream().mapToDouble(moleculeDescriptor -> moleculeDescriptor.getElement().getAtomicWeight() * moleculeDescriptor.getCount()).sum();
    }

}
