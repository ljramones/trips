# Datasets and Imports

TRIPS supports multiple datasets. You can import new data, switch between datasets, and configure which catalog is active.

## Importing a Dataset

To import a dataset, use **File → Import/Load or Manage dataset(s)** from the menu bar:

![File menu with import option](images/manageDatasets.png)

This opens the dataset management dialog where you can:
- Import a new dataset from a CSV file
- View and manage existing datasets
- Delete datasets you no longer need

A dataset derived from Gaia DR2 data is included with TRIPS as a CSV file. On first launch, you'll need to import this (or your own data) before you can visualize stars.

## Opening a Dataset (Setting the Context)

TRIPS operates on a single dataset at a time, called the **context**. The context determines which stars are available for searching, plotting, and routing operations.

To set the context, use **File → Open Dataset...** to select from your previously imported datasets. Once opened, all operations will work with that dataset until you switch to another.

You can also set the context using the **DataSets Available** section in the side panel:

1. Open the side panel (click **Side Pane** in the toolbar).
2. Expand the **DataSets Available** section.
3. Select the dataset you want to use.
4. The context switches and the plot refreshes with the new data.

## Data Workbench (Advanced)

The Data Workbench is a separate tool for advanced users. It is **not** for importing regular datasets. Instead, it provides:

- Querying online astronomical sources (Gaia, SIMBAD, VizieR)
- Enriching existing data with additional catalog information
- Advanced dataset management and transformation

Access the Data Workbench by clicking the **Workbench** button in the toolbar.

## Troubleshooting

- If no datasets appear, confirm you have imported at least one dataset via File → Import/Load or Manage dataset(s).
- If the import dialog doesn't recognize your file format, ensure it's a properly formatted CSV with the expected columns.
- For issues with the Data Workbench online queries, check your internet connection and that the astronomical services are available.
