# StarDetailsPersist

Stores rendering settings for each stellar spectral class, including display size and color.

**Database Table**: `STARDETAILSPERSIST`

## Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | String | (auto) | Primary key |
| `stellarClass` | String | (unique) | Spectral class (O, B, A, F, G, K, M, L, T, Y) |
| `starColor` | String | varies | Display color for this class |
| `radius` | float | 10.0 | Display size in relative units |

## Default Values by Spectral Class

| Class | Default Color | Default Size | Description |
|-------|---------------|--------------|-------------|
| O | Light Blue | 4.0 | Hottest, blue stars |
| B | Light Blue | 3.5 | Blue-white stars |
| A | Light Blue | 3.0 | White stars |
| F | Yellow | 2.5 | Yellow-white stars |
| G | Yellow | 2.5 | Yellow stars (like Sol) |
| K | Orange | 2.0 | Orange stars |
| M | Red | 1.5 | Red dwarf stars |
| L | Magenta | 1.5 | Brown dwarfs |
| T | Magenta | 1.0 | Cool brown dwarfs |
| Y | Magenta | 1.0 | Coolest brown dwarfs |

## Usage

These settings are accessed via **Edit → Preferences → Stars** tab.

Each spectral class has:
- **Radius (Sol Units)**: Controls the display size relative to other stars
- **Color Value**: The hex color code
- **Color Swatch**: Visual color picker

See [Preferences and Settings](../preferences-and-settings.md) for more information.
