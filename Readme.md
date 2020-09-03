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

Dataset in side panel
![alt text](assets/8DataSetInSidePanel.png "After loaded")

###Plotting and Displaying data

Plotting menu
![alt text](assets/9PlotStars.png "Plot Menu")

Select a dataset to plot
![alt text](assets/10SelectDataSetToPlot.png "Selection")

Notification of plot
![alt text](assets/11PlotNotification.png "Notification")

Zooming in and out on a plot
![alt text](assets/12ZoomInOnPlot.png "Plot zoom")

Mouse selection of a star
![alt text](assets/13MouseHoverSelect.png "Mouse selection")

Stellar Properties
![alt text](assets/14QueryDialog.png "Properties")

###Querying Stars
Generating a Query on a data set
![alt text](assets/15QueryDialog.png "Query")

Dataset Description
![alt text](assets/16DataSetDescribe.png "Describe")

Composite Data View
![alt text](assets/17CompositePlotQueryView.png "Composite")

View/Edit table data
![alt text](assets/18ViewEditData.png "View/Edit data")

Edit of Delete of data rows
![alt text](assets/19EditDelete.png "edit/delete")

Update Stellar Object
![alt text](assets/20EditStar.png "Update Stellar Object")





