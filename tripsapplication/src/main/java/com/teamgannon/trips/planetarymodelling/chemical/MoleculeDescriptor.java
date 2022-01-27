package com.teamgannon.trips.planetarymodelling.chemical;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoleculeDescriptor {

    /**
     * the atomic element
     */
    private AtomicElement element;

    /**
     * the count of this element
     */
    private int count;

}
