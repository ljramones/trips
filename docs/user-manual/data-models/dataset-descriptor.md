# DataSetDescriptor

Metadata and configuration for a dataset. Each dataset has a descriptor that stores information about its source, contents, and custom field definitions.

**Database Table**: `DATASETDESCRIPTOR`

## Identity Fields

| Field | Type | Description |
|-------|------|-------------|
| `dataSetName` | String | Primary key - the dataset name |

## Source Information

| Field | Type | Description |
|-------|------|-------------|
| `filePath` | String | Original file path (required) |
| `fileCreator` | String | Creator/author name (required) |
| `fileOriginalDate` | long | Creation timestamp (epoch milliseconds) |
| `fileNotes` | String | Notes about the dataset |
| `datasetType` | String | Type/format of the dataset |

## Statistics

| Field | Type | Description |
|-------|------|-------------|
| `numberStars` | Long | Total number of stars in dataset |
| `distanceRange` | double | Maximum distance span in light years |
| `numberRoutes` | Integer | Number of saved routes |

## Serialized Data (JSON)

These fields store complex data as JSON strings:

| Field | Type | Description |
|-------|------|-------------|
| `themeStr` | String (LOB) | Color theme as JSON |
| `routesStr` | String (LOB) | Saved routes as JSON |
| `customDataDefsStr` | String (LOB) | Custom field definitions as JSON |
| `customDataValuesStr` | String (LOB) | Custom field values as JSON |
| `transitPreferencesStr` | String (LOB) | Transit definitions as JSON |
| `astrographicDataList` | String (LOB) | Comma-separated list of star UUIDs |

## Custom Data Fields

Datasets can define custom columns for stars. The definitions are stored in `customDataDefsStr` as JSON, allowing each dataset to have unique metadata fields beyond the standard star properties.

Example custom field definition:
```json
{
  "fields": [
    {"name": "Population", "type": "NUMBER"},
    {"name": "Government", "type": "TEXT"},
    {"name": "TechLevel", "type": "NUMBER"}
  ]
}
```

## Theme Configuration

The `themeStr` field stores color scheme preferences specific to this dataset, allowing different datasets to have different visual themes.

## Routes

The `routesStr` field stores saved routes as JSON. Routes are paths between stars that users have planned and saved.

## Transit Preferences

The `transitPreferencesStr` field stores transit (jump) definitions specific to this dataset, including maximum jump distances and filtering rules.

## Usage Notes

- The `dataSetName` is the primary key and must be unique
- When importing a dataset, a DataSetDescriptor is automatically created
- Custom field definitions allow extending star data without modifying the database schema
- Theme and route data are serialized to JSON for flexibility
