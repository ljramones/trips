# GraphColorsPersist

Stores the color scheme settings for the 3D graph visualization.

**Database Table**: `GRAPHCOLORSPERSIST`

## Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | String | (auto) | Primary key |
| `labelColor` | String | BEIGE | Color for star name labels |
| `labelFont` | String | "Arial:8" | Font specification for labels |
| `gridColor` | String | MEDIUMBLUE | Color of the reference grid |
| `extensionColor` | String | DARKSLATEBLUE | Color for extension lines |
| `legendColor` | String | BEIGE | Color for legend text |
| `stemLineWidth` | double | 0.5 | Width of stem lines |
| `gridLineWidth` | double | 0.5 | Width of grid lines |

## Color Values

Colors are stored as JavaFX color names (e.g., "BEIGE", "MEDIUMBLUE") or hex values (e.g., "#FF0000").

## Usage

These settings are accessed via **Edit → Preferences → Graph** tab in the View Preferences dialog.

See [Preferences and Settings](../preferences-and-settings.md) for more information.
