# TransitSettings

Stores settings for transit (jump) connections between stars.

**Database Table**: `TRANSITSETTINGS`

## Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | String | (auto) | Primary key |
| `upperDistance` | double | 9.0 | Maximum jump distance in light years |
| `lowerDistance` | double | 3.0 | Minimum jump distance in light years |
| `lineWidth` | double | 1.0 | Display line width for transit links |
| `lineColor` | String | CYAN | Display color for transit links |

## Transit Calculation

Transits are calculated between stars that are:
- At least `lowerDistance` light years apart
- No more than `upperDistance` light years apart

This creates a graph of possible jumps for route planning.

## Usage

Transit settings affect:
- Which star pairs are connected by transit links
- The appearance of transit links in the visualization
- Route planning calculations

The Links tab in View Preferences provides additional controls for link display and distance-based coloring.

See [Route Planning](../route-planning.md) for more information on how transits are used.
