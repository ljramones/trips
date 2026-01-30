package com.teamgannon.trips.dialogs.nebula.catalog;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.model.NebulaType;
import com.teamgannon.trips.service.NebulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.teamgannon.trips.dialogs.nebula.catalog.NebulaCatalogEntry.*;

/**
 * Service providing access to built-in nebula catalogs.
 * <p>
 * Contains data for well-known Messier and NGC nebulae that can be
 * imported into datasets for visualization.
 */
@Slf4j
@Service
public class NebulaCatalogService {

    private final NebulaService nebulaService;
    private final List<NebulaCatalogEntry> catalogEntries;

    public NebulaCatalogService(NebulaService nebulaService) {
        this.nebulaService = nebulaService;
        this.catalogEntries = initializeCatalog();
        log.info("Initialized nebula catalog with {} entries", catalogEntries.size());
    }

    /**
     * Get all catalog entries.
     */
    public List<NebulaCatalogEntry> getAllEntries() {
        return Collections.unmodifiableList(catalogEntries);
    }

    /**
     * Get catalog entries filtered by type.
     */
    public List<NebulaCatalogEntry> getEntriesByType(NebulaType type) {
        return catalogEntries.stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Get catalog entries filtered by source catalog.
     */
    public List<NebulaCatalogEntry> getEntriesByCatalog(String sourceCatalog) {
        return catalogEntries.stream()
                .filter(e -> sourceCatalog.equalsIgnoreCase(e.getSourceCatalog()))
                .collect(Collectors.toList());
    }

    /**
     * Search catalog entries by name (catalog ID or common name).
     */
    public List<NebulaCatalogEntry> searchByName(String query) {
        String lowerQuery = query.toLowerCase();
        return catalogEntries.stream()
                .filter(e -> e.getCatalogId().toLowerCase().contains(lowerQuery) ||
                        (e.getCommonName() != null && e.getCommonName().toLowerCase().contains(lowerQuery)) ||
                        (e.getAlternateCatalogId() != null && e.getAlternateCatalogId().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    /**
     * Get a catalog entry by its catalog ID.
     */
    public Optional<NebulaCatalogEntry> getEntryById(String catalogId) {
        return catalogEntries.stream()
                .filter(e -> e.getCatalogId().equalsIgnoreCase(catalogId) ||
                        (e.getAlternateCatalogId() != null && e.getAlternateCatalogId().equalsIgnoreCase(catalogId)))
                .findFirst();
    }

    /**
     * Get all unique source catalogs.
     */
    public List<String> getSourceCatalogs() {
        return catalogEntries.stream()
                .map(NebulaCatalogEntry::getSourceCatalog)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Import selected catalog entries into a dataset.
     *
     * @param entries     the catalog entries to import
     * @param datasetName the target dataset
     * @return number of nebulae imported
     */
    public int importToDataset(List<NebulaCatalogEntry> entries, String datasetName) {
        int imported = 0;
        for (NebulaCatalogEntry entry : entries) {
            // Check if already exists
            if (nebulaService.existsByName(datasetName, entry.getCommonName() != null ?
                    entry.getCommonName() : entry.getCatalogId())) {
                log.debug("Skipping {} - already exists in dataset", entry.getCatalogId());
                continue;
            }

            Nebula nebula = entry.toNebula(datasetName);
            nebulaService.save(nebula);
            imported++;
            log.debug("Imported {} to dataset {}", entry.getCatalogId(), datasetName);
        }
        log.info("Imported {} nebulae to dataset '{}'", imported, datasetName);
        return imported;
    }

    /**
     * Initialize the built-in catalog with well-known nebulae.
     */
    private List<NebulaCatalogEntry> initializeCatalog() {
        List<NebulaCatalogEntry> entries = new ArrayList<>();

        // ==================== MESSIER CATALOG ====================

        // M1 - Crab Nebula (Supernova Remnant)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M1")
                .commonName("Crab Nebula")
                .alternateCatalogId("NGC 1952")
                .raDegrees(raToDegreesFromHMS(5, 34, 31.94))
                .decDegrees(decToDegreesFromDMS(22, 0, 52.2))
                .distanceLy(6500)
                .angularSizeArcmin(7)
                .type(NebulaType.SUPERNOVA_REMNANT)
                .sourceCatalog("Messier")
                .constellation("Taurus")
                .description("Remnant of supernova observed in 1054 AD. Contains a pulsar.")
                .build());

        // M8 - Lagoon Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M8")
                .commonName("Lagoon Nebula")
                .alternateCatalogId("NGC 6523")
                .raDegrees(raToDegreesFromHMS(18, 3, 37))
                .decDegrees(decToDegreesFromDMS(-24, 23, 12))
                .distanceLy(5200)
                .angularSizeArcmin(90)
                .type(NebulaType.EMISSION)
                .sourceCatalog("Messier")
                .constellation("Sagittarius")
                .description("Large emission nebula with active star formation. Contains the Hourglass Nebula.")
                .build());

        // M16 - Eagle Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M16")
                .commonName("Eagle Nebula")
                .alternateCatalogId("NGC 6611")
                .raDegrees(raToDegreesFromHMS(18, 18, 48))
                .decDegrees(decToDegreesFromDMS(-13, 47, 0))
                .distanceLy(7000)
                .angularSizeArcmin(35)
                .type(NebulaType.EMISSION)
                .sourceCatalog("Messier")
                .constellation("Serpens")
                .description("Famous for the 'Pillars of Creation' imaged by Hubble.")
                .build());

        // M17 - Omega/Swan Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M17")
                .commonName("Omega Nebula")
                .alternateCatalogId("NGC 6618")
                .raDegrees(raToDegreesFromHMS(18, 20, 26))
                .decDegrees(decToDegreesFromDMS(-16, 10, 36))
                .distanceLy(5000)
                .angularSizeArcmin(46)
                .type(NebulaType.EMISSION)
                .sourceCatalog("Messier")
                .constellation("Sagittarius")
                .description("Also called the Swan, Checkmark, or Horseshoe Nebula.")
                .build());

        // M20 - Trifid Nebula (Emission/Reflection)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M20")
                .commonName("Trifid Nebula")
                .alternateCatalogId("NGC 6514")
                .raDegrees(raToDegreesFromHMS(18, 2, 23))
                .decDegrees(decToDegreesFromDMS(-23, 1, 48))
                .distanceLy(5200)
                .angularSizeArcmin(28)
                .type(NebulaType.EMISSION)
                .sourceCatalog("Messier")
                .constellation("Sagittarius")
                .description("Combines emission, reflection, and dark nebula regions.")
                .build());

        // M27 - Dumbbell Nebula (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M27")
                .commonName("Dumbbell Nebula")
                .alternateCatalogId("NGC 6853")
                .raDegrees(raToDegreesFromHMS(19, 59, 36.34))
                .decDegrees(decToDegreesFromDMS(22, 43, 16.1))
                .distanceLy(1360)
                .angularSizeArcmin(8)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("Messier")
                .constellation("Vulpecula")
                .description("First planetary nebula discovered. One of the brightest.")
                .build());

        // M42 - Orion Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M42")
                .commonName("Orion Nebula")
                .alternateCatalogId("NGC 1976")
                .raDegrees(raToDegreesFromHMS(5, 35, 17.3))
                .decDegrees(decToDegreesFromDMS(-5, 23, 28))
                .distanceLy(1344)
                .angularSizeArcmin(85)
                .type(NebulaType.EMISSION)
                .sourceCatalog("Messier")
                .constellation("Orion")
                .description("Nearest massive star-forming region. Visible to naked eye.")
                .build());

        // M43 - De Mairan's Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M43")
                .commonName("De Mairan's Nebula")
                .alternateCatalogId("NGC 1982")
                .raDegrees(raToDegreesFromHMS(5, 35, 31.3))
                .decDegrees(decToDegreesFromDMS(-5, 16, 3))
                .distanceLy(1600)
                .angularSizeArcmin(20)
                .type(NebulaType.EMISSION)
                .sourceCatalog("Messier")
                .constellation("Orion")
                .description("Part of the Orion Nebula complex, separated by dark dust lane.")
                .build());

        // M57 - Ring Nebula (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M57")
                .commonName("Ring Nebula")
                .alternateCatalogId("NGC 6720")
                .raDegrees(raToDegreesFromHMS(18, 53, 35.08))
                .decDegrees(decToDegreesFromDMS(33, 1, 45))
                .distanceLy(2300)
                .angularSizeArcmin(1.4)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("Messier")
                .constellation("Lyra")
                .description("Iconic ring-shaped planetary nebula between Beta and Gamma Lyrae.")
                .build());

        // M76 - Little Dumbbell Nebula (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M76")
                .commonName("Little Dumbbell Nebula")
                .alternateCatalogId("NGC 650/651")
                .raDegrees(raToDegreesFromHMS(1, 42, 19.69))
                .decDegrees(decToDegreesFromDMS(51, 34, 31.7))
                .distanceLy(2500)
                .angularSizeArcmin(2.7)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("Messier")
                .constellation("Perseus")
                .description("Also called the Cork or Butterfly Nebula. Faintest Messier object.")
                .build());

        // M78 - Reflection Nebula
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M78")
                .commonName("M78")
                .alternateCatalogId("NGC 2068")
                .raDegrees(raToDegreesFromHMS(5, 46, 46.7))
                .decDegrees(decToDegreesFromDMS(0, 0, 50))
                .distanceLy(1600)
                .angularSizeArcmin(8)
                .type(NebulaType.REFLECTION)
                .sourceCatalog("Messier")
                .constellation("Orion")
                .description("Brightest diffuse reflection nebula in the sky.")
                .build());

        // M97 - Owl Nebula (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("M97")
                .commonName("Owl Nebula")
                .alternateCatalogId("NGC 3587")
                .raDegrees(raToDegreesFromHMS(11, 14, 47.71))
                .decDegrees(decToDegreesFromDMS(55, 1, 8.5))
                .distanceLy(2030)
                .angularSizeArcmin(3.4)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("Messier")
                .constellation("Ursa Major")
                .description("Named for its owl-like appearance with two dark 'eyes'.")
                .build());

        // ==================== NGC CATALOG ====================

        // NGC 7000 - North America Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 7000")
                .commonName("North America Nebula")
                .raDegrees(raToDegreesFromHMS(20, 58, 47))
                .decDegrees(decToDegreesFromDMS(44, 19, 48))
                .distanceLy(2200)
                .angularSizeArcmin(120)
                .type(NebulaType.EMISSION)
                .sourceCatalog("NGC")
                .constellation("Cygnus")
                .description("Shape resembles North American continent. Near Deneb.")
                .build());

        // NGC 7293 - Helix Nebula (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 7293")
                .commonName("Helix Nebula")
                .raDegrees(raToDegreesFromHMS(22, 29, 38.55))
                .decDegrees(decToDegreesFromDMS(-20, 50, 13.6))
                .distanceLy(700)
                .angularSizeArcmin(25)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("NGC")
                .constellation("Aquarius")
                .description("Nearest planetary nebula. Called 'Eye of God'.")
                .build());

        // NGC 6543 - Cat's Eye Nebula (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 6543")
                .commonName("Cat's Eye Nebula")
                .raDegrees(raToDegreesFromHMS(17, 58, 33.42))
                .decDegrees(decToDegreesFromDMS(66, 37, 59.5))
                .distanceLy(3300)
                .angularSizeArcmin(0.4)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("NGC")
                .constellation("Draco")
                .description("Complex structure with multiple shells and jets.")
                .build());

        // NGC 2392 - Eskimo Nebula (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 2392")
                .commonName("Eskimo Nebula")
                .raDegrees(raToDegreesFromHMS(7, 29, 10.77))
                .decDegrees(decToDegreesFromDMS(20, 54, 42.5))
                .distanceLy(3000)
                .angularSizeArcmin(0.9)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("NGC")
                .constellation("Gemini")
                .description("Also called Clown Face Nebula. Double-shell structure.")
                .build());

        // NGC 6960/6992 - Veil Nebula (Supernova Remnant)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 6960")
                .commonName("Western Veil Nebula")
                .alternateCatalogId("Witch's Broom")
                .raDegrees(raToDegreesFromHMS(20, 45, 42))
                .decDegrees(decToDegreesFromDMS(30, 42, 30))
                .distanceLy(2400)
                .angularSizeArcmin(70)
                .type(NebulaType.SUPERNOVA_REMNANT)
                .sourceCatalog("NGC")
                .constellation("Cygnus")
                .description("Western portion of the Cygnus Loop supernova remnant.")
                .build());

        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 6992")
                .commonName("Eastern Veil Nebula")
                .alternateCatalogId("Network Nebula")
                .raDegrees(raToDegreesFromHMS(20, 56, 24))
                .decDegrees(decToDegreesFromDMS(31, 43, 0))
                .distanceLy(2400)
                .angularSizeArcmin(60)
                .type(NebulaType.SUPERNOVA_REMNANT)
                .sourceCatalog("NGC")
                .constellation("Cygnus")
                .description("Eastern portion of the Cygnus Loop supernova remnant.")
                .build());

        // NGC 2237 - Rosette Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 2237")
                .commonName("Rosette Nebula")
                .raDegrees(raToDegreesFromHMS(6, 32, 3))
                .decDegrees(decToDegreesFromDMS(5, 3, 0))
                .distanceLy(5200)
                .angularSizeArcmin(80)
                .type(NebulaType.EMISSION)
                .sourceCatalog("NGC")
                .constellation("Monoceros")
                .description("Rose-shaped emission nebula surrounding open cluster NGC 2244.")
                .build());

        // IC 434 region - Horsehead Nebula (Dark)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("B33")
                .commonName("Horsehead Nebula")
                .alternateCatalogId("IC 434")
                .raDegrees(raToDegreesFromHMS(5, 40, 59.0))
                .decDegrees(decToDegreesFromDMS(-2, 27, 30))
                .distanceLy(1500)
                .angularSizeArcmin(8)
                .type(NebulaType.DARK)
                .sourceCatalog("Barnard")
                .constellation("Orion")
                .description("Iconic horse-head shaped dark nebula silhouetted against IC 434.")
                .build());

        // NGC 6826 - Blinking Planetary (Planetary)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 6826")
                .commonName("Blinking Planetary")
                .raDegrees(raToDegreesFromHMS(19, 44, 48.15))
                .decDegrees(decToDegreesFromDMS(50, 31, 30.3))
                .distanceLy(2200)
                .angularSizeArcmin(0.5)
                .type(NebulaType.PLANETARY)
                .sourceCatalog("NGC")
                .constellation("Cygnus")
                .description("Appears to 'blink' when looking directly at it vs. averted vision.")
                .build());

        // NGC 1499 - California Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 1499")
                .commonName("California Nebula")
                .raDegrees(raToDegreesFromHMS(4, 3, 18))
                .decDegrees(decToDegreesFromDMS(36, 25, 18))
                .distanceLy(1000)
                .angularSizeArcmin(145)
                .type(NebulaType.EMISSION)
                .sourceCatalog("NGC")
                .constellation("Perseus")
                .description("Large emission nebula resembling the state of California.")
                .build());

        // NGC 2024 - Flame Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 2024")
                .commonName("Flame Nebula")
                .raDegrees(raToDegreesFromHMS(5, 41, 54))
                .decDegrees(decToDegreesFromDMS(-1, 51, 0))
                .distanceLy(1350)
                .angularSizeArcmin(30)
                .type(NebulaType.EMISSION)
                .sourceCatalog("NGC")
                .constellation("Orion")
                .description("Near Alnitak in Orion's Belt. Flame-like appearance.")
                .build());

        // NGC 6888 - Crescent Nebula (Emission)
        entries.add(NebulaCatalogEntry.builder()
                .catalogId("NGC 6888")
                .commonName("Crescent Nebula")
                .raDegrees(raToDegreesFromHMS(20, 12, 6.5))
                .decDegrees(decToDegreesFromDMS(38, 21, 18))
                .distanceLy(4700)
                .angularSizeArcmin(18)
                .type(NebulaType.EMISSION)
                .sourceCatalog("NGC")
                .constellation("Cygnus")
                .description("Formed by stellar wind from Wolf-Rayet star WR 136.")
                .build());

        return entries;
    }
}
