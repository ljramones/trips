# CivilizationDisplayPreferences

Stores color settings for different polities (political entities/civilizations) used in science fiction world-building scenarios.

**Database Table**: `CIVILIZATIONDISPLAYPREFERENCES`

## Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | String (UUID) | (auto) | Primary key |
| `storageTag` | String | "Main" | Unique identifier tag |

## Polity Colors

Pre-defined civilizations from the Caine Riordan universe:

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `humanPolityColor` | String | BEIGE | Terran/Human civilization |
| `dornaniPolityColor` | String | FUCHSIA | Dornani civilization |
| `ktorPolityColor` | String | HONEYDEW | Ktor civilization |
| `aratKurPolityColor` | String | ALICEBLUE | Arat Kur civilization |
| `hkhRkhPolityColor` | String | LIGHTGREEN | Hkh'Rkh civilization |
| `slaasriithiPolityColor` | String | LIGHTCORAL | Slaasriithi civilization |

## Custom Polity Slots

| Field | Type | Description |
|-------|------|-------------|
| `other1PolityColor` | String | Custom polity 1 |
| `other2PolityColor` | String | Custom polity 2 |
| `other3PolityColor` | String | Custom polity 3 |
| `other4PolityColor` | String | Custom polity 4 |

## Constants

The model defines constants for polity names:
- `TERRAN` - Human civilization
- `DORNANI` - Dornani
- `KTOR` - Ktor
- `ARAKUR` - Arat Kur
- `HKHRKH` - Hkh'Rkh
- `SLAASRIITHI` - Slaasriithi
- `OTHER1` through `OTHER4` - Custom slots
- `NONE` - No polity assigned

## Usage

These settings are accessed via **Edit → Preferences → Civilization** tab.

When stars are assigned to polities (via the StarWorldBuilding component), they are displayed using these colors in the 3D visualization. This allows users to visualize territorial boundaries or spheres of influence.

See [Preferences and Settings](../preferences-and-settings.md) for more information.
