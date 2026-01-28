# GraphEnablesPersist

Stores the display toggle settings for graph elements.

**Database Table**: `GRAPHENABLESPERSIST`

## Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | String | (auto) | Primary key |
| `displayPolities` | boolean | true | Show polity indicators |
| `displayGrid` | boolean | true | Show the reference grid |
| `displayStems` | boolean | true | Show vertical stem lines |
| `displayLabels` | boolean | true | Show star name labels |
| `displayLegend` | boolean | true | Show the scale legend |
| `displayRoutes` | boolean | true | Show saved routes |

## Usage

These settings are accessed via **Edit → Preferences → Graph** tab, under the "Graph Enables" section.

The toggles can also be controlled from the toolbar buttons:
- **Star Labels** button toggles `displayLabels`
- **Grid** button toggles `displayGrid`
- **Routes** button toggles `displayRoutes`

See [Preferences and Settings](../preferences-and-settings.md) for more information.
