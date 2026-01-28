# StarWorldBuilding

An embedded value object that stores science fiction world-building data for a star. This is embedded within [StarObject](star-object.md).

**Type**: `@Embeddable` (not a separate table)

## Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `polity` | String | "NA" | Controlling political entity/faction |
| `worldType` | String | "NA" | World classification |
| `fuelType` | String | "NA" | Available fuel type |
| `portType` | String | "NA" | Starport classification |
| `populationType` | String | "NA" | Population level category |
| `techType` | String | "NA" | Technology level |
| `productType` | String | "NA" | Primary export/product |
| `milSpaceType` | String | "NA" | Military space presence level |
| `milPlanType` | String | "NA" | Military planetary presence level |
| `other` | boolean | false | User-defined marker flag |
| `anomaly` | boolean | false | Anomaly/special location flag |

## Usage

These fields support science fiction world-building scenarios where users assign political, economic, and military attributes to star systems.

### Polity

The `polity` field links to the civilization colors defined in [CivilizationDisplayPreferences](civilization-preferences.md). When set, the star is displayed using the associated polity color.

Common polity values:
- Human/Terran
- Dornani
- Ktor
- Arat Kur
- Hkh'Rkh
- Slaasriithi
- Custom values (Other 1-4)

### Classification Fields

The classification fields (`worldType`, `portType`, `techType`, etc.) are free-form strings. Users can define their own classification systems based on their world-building needs.

Example classifications:
- **worldType**: "Garden", "Desert", "Ice", "Ocean", "Barren"
- **portType**: "A" (excellent), "B" (good), "C" (routine), "D" (poor), "E" (frontier), "X" (none)
- **techType**: "0-15" scale or descriptive levels

### Special Markers

- **other**: A general-purpose flag for user-defined filtering
- **anomaly**: Marks systems with special characteristics (wormholes, phenomena, etc.)

## Editing

World-building data can be edited through the star properties dialog, accessible by right-clicking a star in the visualization.
