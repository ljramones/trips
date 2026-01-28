# TripsPrefs

Stores general application preferences.

**Database Table**: `TRIPSPREFS`

## Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | String | (auto) | Primary key |
| `skipStartupDialog` | boolean | false | Skip the welcome/data requirement dialog on startup |
| `datasetName` | String | null | Name of the active dataset |

## Startup Dialog

The `skipStartupDialog` field (stored as `show_welcome_data_req` in the database) controls whether the initial welcome dialogs are shown when TRIPS starts.

Users can set this by checking "Don't show this at startup?" in the startup dialog.

## Active Dataset

The `datasetName` field stores the currently active dataset context. This is the dataset that will be used for:
- Star searches
- Plotting
- Route planning
- All other operations

This can be changed via:
- **File → Open Dataset...**
- The **DataSets Available** section in the side panel
