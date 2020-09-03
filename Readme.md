# Terran Republic Interstellar Plotter System
## Introduction

TRIPS (Terran Interstellar Plotter System) is intended to be a stellar cartography system for loading stellar databases in well-known formats to:
1. view data in tabular form
2. Plot data in a 3 dimensional graph
3. Modify stellar data 
4. Keep data organized in different sets based on purpose and interest
5. Use for either scientific or science fiction writing purposes (world building)


## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request

Please read the [CONTRIBUTING.md](CONTRIBUTING.md) file for more details on how
to contribute to this project.

## Versioning

This project uses [SemVer](http://semver.org/) for versioning. For the versions
available, see the [tags on this repository](https://github.com/ljramones/trips/tags).

## Gitflow
This project is setup to work as a gitflow project. Please read [this guide on how gitflow works](https://nvie.com/posts/a-successful-git-branching-model/).

If you need a tool to make using gitflow easy to use, [check this one out as I use it](https://danielkummer.github.io/git-flow-cheatsheet/).

## Installing
    a. Down load one of the release versions from the release page.
    b. unzip into a target direction of your choice
    c. acquire data files from one of the data pages (TBD) and unzip into the ./files directory

## Building
This project is configured as a spring boot application with a single jar to run.
The tripsapplication subpackage holds all the code need to generate a trip-_version_.jar, where _version_ is the releaseable version.

To build, simply run `mvn package`, which will put a compiled jar in the target subdirectory.
To run simply form the command line, `java -jar target/trips-version.jar`

## Running
    a. Once installed, the install directory is complete and independent. Nothing else needs to be installed
    b. in the main director, run either
        - runme.bat (Windows version)
        - runme.sh (Mac/Linux version)        
  
## Using Application
You will see a screen as below:

Main Screen: 
![alt text](assets/1MainScreenStart.png "Main Screen")

### Application Preferences
Most of the plot items can be toggled through this drop down menu or the tool baf
![alt text](assets/1.4SelectingPreferences.png "Main Screen")

Selecting application preferences is a general non data specific screen
![alt text](assets/1.5ApplicationPreferencesDialog.png "Main Screen")

### Loading a Data set into the Database

Select the Data Manager
![alt text](assets/2SelectDataManager.png "Select Data Manager")

The Data Manager
![alt text](assets/3DataManager.png "Data Manager")

Add A Dataset
![alt text](assets/4AddDataSet.png "Add a dataset")

Fill out description, select format and chose file
![alt text](assets/5FillOutSelectFormat.png "Choose set")

Select File
![alt text](assets/6SelectFile_Mac.png "Select File")

Dataset in Data Manager
![alt text](assets/7AfterLoaded.png "File loaded")

Selecting a Data set context
![alt text](assets/8.5SelectDataSetContext.png "Set dataset context")

Dataset in side panel
![alt text](assets/8DataSetInSidePanel.png "After loaded")

Functions from Dataset side panel
![alt text](assets/8.6DatasetPanelFunctions.png "Functions from Dataset")


### Plotting and Displaying data

Plotting menu
![alt text](assets/9PlotStars.png "Plot Menu")

If there was no dataset selected as current context then you will be asked for one
![alt text](assets/10SelectDataSetToPlot.png "Selection")

Notification of plot
![alt text](assets/11PlotNotification.png "Notification")

Plot of data selected
![alt text](assets/11.5PlotNotification.png "Plot View")

Zooming in and out on a plot
![alt text](assets/12ZoomInOnPlot.png "Plot zoom")

Mouse selection of a star
![alt text](assets/13MouseHoverSelect.png "Mouse selection")

Stellar Properties
Selecting a star in the list view or form the menu above by selecting properties which show all the properties of that star
![alt text](assets/14StellarProperties.png "Properties")

### Querying Stars
Generating a Query on a data set
![alt text](assets/15QueryDialog.png "Query")

Dataset Description
![alt text](assets/16DataSetDescribe.png "Describe a dataset")

Composite Data View
![alt text](assets/17CompositePlotQueryView.png "Composite Plot and Edit Views")

View/Edit table data
![alt text](assets/18ViewEditData.png "View/Edit data")

Edit of Delete of data rows
![alt text](assets/19EditDelete.png "edit/delete")

Update Stellar Object
![alt text](assets/20EditStar.png "Update Stellar Object")

### General Plotting and Star Maintenance
Add Notes to a star on the plot
![alt text](assets/21AddNotesToStar.png "Add notes to a star")

Edit a Star Entry
![alt text](assets/22EditaStarEntry.png "Edit a Star Entry")

The Edit can be actioned from the side panel list view by bringing up a context menu
![alt text](assets/23StarListContextMenu.png "Context Menu actions, Edit and Recenter")

Selecting edit brings up the edit dialog above

Recentering on a star
![alt text](assets/23StarListContextMenu.png "Context Menu actions, Edit and Recenter")

Recenter selection
![alt text](assets/24RecenterSelection.png "Recenter")

Recenter Result
Generally, this does a query on the stars with the same radius view as already selected. 
Often it means we may have less or more stars in the view.
The new center star will be enclosed in a box and slowly rotate to show that it is at the center of a new plot
![alt text](assets/25RecenterResult.png "Recenter")

### Routes

In this  first iteration, we only support manually created routes, but in later versions, this will be done either
1) automatically
2) with heuristics

Starting a route
![alt text](assets/26StartRoute.png "Start a route")

Identifying the route
![alt text](assets/27IdentifyRoute.png "Identify a route")

Selecting the next route segment
Connecting route segments is through the continue function
![alt text](assets/28NextRoute1.png "Next route segment")

Finishing a route
After you have lonked multiple segments, you select finish, which saves the route to the database for that dataset
![alt text](assets/29FinishingRoute.png "Finishing the route")

After which the complete route will be displayed in the 3D plot
![alt text](assets/30CompleteRoute.png "Complete Route")











