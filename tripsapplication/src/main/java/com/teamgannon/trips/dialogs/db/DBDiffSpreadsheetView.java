package com.teamgannon.trips.dialogs.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.spreadsheet.*;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class DBDiffSpreadsheetView extends SpreadsheetView {

    /**
     * List for custom cells
     */
    private final List<String> companiesList = Arrays.asList("", "ControlsFX", "Aperture Science",
            "Rapture", "Ammu-Nation", "Nuka-Cola", "Pay'N'Spray", "Umbrella Corporation");

    private final List<String> countryList = Arrays.asList("China", "France", "New Zealand",
            "United States", "Germany", "Canada");

    private final List<String> logoList = Arrays.asList("", "ControlsFX.png", "apertureLogo.png",
            "raptureLogo.png", "ammunationLogo.JPG", "nukaColaLogo.png", "paynsprayLogo.jpg", "umbrellacorporation.png");

    private final List<String> webSiteList = Arrays.asList("", "http://fxexperience.com/controlsfx/",
            "http://aperturescience.com/", "", "http://fr.gta.wikia.com/wiki/Ammu-Nation",
            "http://e-shop.nuka-cola.eu/", "http://fr.gta.wikia.com/wiki/Pay_%27n%27_Spray",
            "http://www.umbrellacorporation.net/");

    public DBDiffSpreadsheetView() {
        int rowCount = 31; //Will be re-calculated after if incorrect.
        int columnCount = 8;

        GridBase grid = new GridBase(rowCount, columnCount);
        grid.setRowHeightCallback(new GridBase.MapBasedRowHeightFactory(generateRowHeight()));
        buildGrid(grid);

        setGrid(grid);

        generatePickers();

        getFixedRows().add(0);
        getColumns().get(0).setFixed(true);
        getColumns().get(1).setPrefWidth(250);
//        getStylesheets().add(Utils.class.getResource("spreadsheetSample.css").toExternalForm());
    }

    /**
     * Add some pickers into the SpreadsheetView in order to give some
     * information.
     */
    private void generatePickers() {
        getRowPickers().put(0, new Picker() {

            @Override
            public void onClick() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("This row contains several fictive companies. "
                        + "The cells are not editable.\n"
                        + "A custom tooltip is applied for the first cell.");
                alert.show();
            }
        });

        getRowPickers().put(1, new Picker() {

            @Override
            public void onClick() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("This row contains cells that can only show a list.");
                alert.show();
            }
        });

        getRowPickers().put(2, new Picker() {

            @Override
            public void onClick() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("This row contains cells that display some dates.");
                alert.show();
            }
        });

        getRowPickers().put(3, new Picker() {

            @Override
            public void onClick() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("This row contains some Images displaying logos of the companies.");
                alert.show();
            }
        });

        getRowPickers().put(4, new Picker() {

            @Override
            public void onClick() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("This row contains Double editable cells. "
                        + "Except for ControlsFX compagny where it's a String.");
                alert.show();
            }
        });
        getRowPickers().put(5, new Picker("picker-label", "picker-label-exclamation") {

            @Override
            public void onClick() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("This row contains Double editable cells with "
                        + "a special format (%). Some cells also have "
                        + "a little icon next to their value.");
                alert.show();
            }
        });

        getColumnPickers().put(0, new Picker("picker-label", "picker-label-security") {

            @Override
            public void onClick() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Each cell of this column (except for the "
                        + "separator in the middle) has a particular css "
                        + "class for changing its color.\n");
                alert.show();
            }
        });
    }

    /**
     * Specify a custom row height.
     *
     * @return a {@link Map} containing the row index and the height.
     */
    private Map<Integer, Double> generateRowHeight() {
        Map<Integer, Double> rowHeight = new HashMap<>();
        rowHeight.put(1, 100.0);
        return rowHeight;
    }

    /**
     * Randomly generate a {@link SpreadsheetCell}.
     */
    private SpreadsheetCell generateCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell;
        List<String> cityList = Arrays.asList("Shanghai", "Paris", "New York City", "Bangkok",
                "Singapore", "Johannesburg", "Berlin", "Wellington", "London", "Montreal");
        final double random = Math.random();
        if (random < 0.25) {
            cell = SpreadsheetCellType.LIST(countryList).createCell(row, column, rowSpan, colSpan,
                    countryList.get((int) (Math.random() * 6)));
        } else if (random >= 0.25 && random < 0.5) {
            cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan,
                    cityList.get((int) (Math.random() * 10)));
        } else if (random >= 0.5 && random < 0.75) {
            cell = generateNumberCell(row, column, rowSpan, colSpan);
        } else {
            cell = generateDateCell(row, column, rowSpan, colSpan);
        }

        // Styling for preview
        if (row % 5 == 0) {
            cell.getStyleClass().add("five_rows");
        }
        return cell;
    }

    /**
     * Generate a Date Cell with a random format.
     *
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return
     */
    private SpreadsheetCell generateDateCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell = SpreadsheetCellType.DATE.createCell(row, column, rowSpan, colSpan, LocalDate.now()
                .plusDays((int) (Math.random() * 10)));
        final double random = Math.random();
        if (random < 0.25) {
            cell.setFormat("EEEE d");
        } else if (random < 0.5) {
            cell.setFormat("dd/MM :YY");
        } else {
            cell.setFormat("dd/MM/YYYY");
        }
        return cell;
    }

    /**
     * Generate a Number Cell with a random format.
     *
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return
     */
    private SpreadsheetCell generateNumberCell(int row, int column, int rowSpan, int colSpan) {
        final double random = Math.random();
        SpreadsheetCell cell;
        if (random < 0.3) {
            cell = SpreadsheetCellType.INTEGER.createCell(row, column, rowSpan, colSpan,
                    Math.round((float) Math.random() * 100));
        } else {
            cell = SpreadsheetCellType.DOUBLE.createCell(row, column, rowSpan, colSpan,
                    (double) Math.round((Math.random() * 100) * 100) / 100);
            final double randomFormat = Math.random();
            if (randomFormat < 0.25) {
                cell.setFormat("#,##0.00" + "\u20AC");
            } else if (randomFormat < 0.5) {
                cell.setFormat("0.###E0 km/h");
            } else {
                cell.setFormat("0.###E0");
            }
        }
        return cell;
    }

    /**
     * Generate a Double Cell
     *
     * @param row     the row index
     * @param column  the column index
     * @param rowSpan the row span
     * @param colSpan the column span
     * @return a {@link SpreadsheetCell}
     */
    private SpreadsheetCell generateDoubleCell(int row, int column, int rowSpan, int colSpan) {
        final double random = Math.random();
        SpreadsheetCell cell;
        cell = SpreadsheetCellType.DOUBLE.createCell(row, column, rowSpan, colSpan,
                (double) Math.round((random * 100) * 100) / 100);
        return cell;
    }

    /**
     * Return a List of SpreadsheetCell displaying the companies.
     *
     * @param grid the grid
     * @param row  the row index
     * @return a {@link List} of {@link SpreadsheetCell}
     */
    private ObservableList<SpreadsheetCell> getCompanies(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> companies = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Company : ");
        ((SpreadsheetCellBase) cell).setTooltip("This cell displays a custom toolTip.");
        cell.setEditable(false);
        companies.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1,
                    companiesList.get(column));
            cell.setEditable(false);
            cell.getStyleClass().add("compagny");
            companies.add(cell);
        }

        return companies;
    }

    /**
     * Return a row with some countries.
     *
     * @param grid the grid
     * @param row  the row index
     * @return a {@link List} of {@link SpreadsheetCell}
     */
    private ObservableList<SpreadsheetCell> getCountries(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> countries = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Countries");
        cell.setEditable(false);
        cell.getStyleClass().add("first-cell");
        countries.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            cell = SpreadsheetCellType.LIST(countryList).createCell(row, column, 1, 1,
                    countryList.get((int) (Math.random() * 6)));
            countries.add(cell);
        }
        return countries;
    }

    /**
     * Return a row with some dates.
     *
     * @param grid  the grid
     * @param row  the row index
     * @return a {@link List} of {@link SpreadsheetCell}
     */
    private ObservableList<SpreadsheetCell> getStartDate(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> startDate = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Start day");
        cell.setEditable(false);
        cell.getStyleClass().add("first-cell");
        startDate.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            startDate.add(generateDateCell(row, column, 1, 1));
        }
        return startDate;
    }

    /**
     * Return a row with some Images.
     *
     * @param grid the grid
     * @param row the row index
     * @return a {@link List} of {@link SpreadsheetCell}
     */
    private ObservableList<SpreadsheetCell> getLogos(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> logos = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Logo");
        cell.setEditable(false);
        cell.getStyleClass().add("first-cell");
        logos.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, null);
            cell.setGraphic(new ImageView(new Image(loadImageResource(logoList.get(column)))));
            cell.getStyleClass().add("logo");
            cell.setEditable(false);
            logos.add(cell);
        }
        return logos;
    }

    /**
     * get a designated image from the resources.
     *
     * @param imagePath the path of the image
     * @return the image as an InputStream
     */
    public InputStream loadImageResource(String imagePath) {
        return getClass().getResourceAsStream(imagePath);
    }


    /**
     * Return a row with Double.
     *
     * @param grid the grid
     * @param row  the row
     * @return the row
     */
    private ObservableList<SpreadsheetCell> getIncome(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> incomes = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Income");
        cell.setEditable(false);
        cell.getStyleClass().add("first-cell");
        incomes.add(cell);

        SpreadsheetCell cell2 = SpreadsheetCellType.STRING.createCell(row, 1, 1, 1, "It's over 9000!");
        incomes.add(cell2);

        for (int column = 2; column < grid.getColumnCount(); ++column) {
            cell = generateDoubleCell(row, column, 1, 1);
            cell.setFormat("#,##0.00" + "\u20AC");

            incomes.add(cell);
        }
        return incomes;
    }

    /**
     * Return a row with Double.
     *
     * @param grid the grid
     * @param row  the row
     * @return the row
     */
    private ObservableList<SpreadsheetCell> getIncrease(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> increase = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Increase");
        cell.setEditable(false);
        cell.getStyleClass().add("first-cell");
        increase.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            cell = SpreadsheetCellType.DOUBLE.createCell(row, column, 1, 1, (double) Math.random());
            if (column % 2 == 1) {
                cell.setGraphic(new ImageView(new Image(Objects.requireNonNull(DBDiffSpreadsheetView.class.getResourceAsStream("images/exclamation.png")))));
            }
            cell.setFormat("#" + "%");
            increase.add(cell);
        }
        return increase;
    }

    /**
     * Return a List with Integer.
     *
     * @param grid the grid
     * @param row  the row
     * @return the row
     */
    private ObservableList<SpreadsheetCell> getEmployees(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> employees = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Number of employees");
        cell.setEditable(false);
        cell.getStyleClass().add("first-cell");
        employees.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            cell = SpreadsheetCellType.INTEGER.createCell(row, column, 1, 1,
                    Math.round((float) Math.random() * 10));
            employees.add(cell);
        }
        return employees;
    }

    /**
     * Return a row with some URL.
     *
     * @param grid the grid
     * @param row  the row
     * @return the row
     */
    private ObservableList<SpreadsheetCell> getWebSite(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> employees = FXCollections.observableArrayList();

        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "WebSite ");
        cell.setEditable(false);
        cell.getStyleClass().add("first-cell");
        employees.add(cell);

        for (int column = 1; column < grid.getColumnCount(); ++column) {
            cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, null);
            Hyperlink link = new Hyperlink(webSiteList.get(column));
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            URI uri;
            try {
                uri = new URI(link.getText());
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    link.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            try {
                                desktop.browse(uri);
                            } catch (IOException ex) {
                            }
                        }
                    });
                }
            } catch (URISyntaxException ex) {
            }
            cell.setGraphic(link);
            cell.setEditable(false);
            employees.add(cell);
        }
        return employees;
    }

    /**
     * Return a row with blank non editable cell.
     *
     * @param grid the grid
     * @param row  the row
     * @return the row
     */
    private ObservableList<SpreadsheetCell> getSeparator(GridBase grid, int row) {

        final ObservableList<SpreadsheetCell> separator = FXCollections.observableArrayList();

        for (int column = 0; column < grid.getColumnCount(); ++column) {
            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
            cell.setEditable(false);
            cell.getStyleClass().add("separator");
            separator.add(cell);
        }
        return separator;
    }

    /**
     * Build the grid.
     *
     * @param grid the grid
     */
    private void buildGrid(GridBase grid) {
        ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();

        int rowIndex = 0;
        rows.add(getCompanies(grid, rowIndex++));
        rows.add(getCountries(grid, rowIndex++));
        rows.add(getStartDate(grid, rowIndex++));
        rows.add(getLogos(grid, rowIndex++));
        rows.add(getIncome(grid, rowIndex++));
        rows.add(getIncrease(grid, rowIndex++));
        rows.add(getEmployees(grid, rowIndex++));
        rows.add(getWebSite(grid, rowIndex++));

        //Separators
        rows.add(getSeparator(grid, rowIndex++));
        rows.add(getSeparator(grid, rowIndex++));
        rows.add(getSeparator(grid, rowIndex++));

        for (int i = rowIndex; i < rowIndex + 1000; ++i) {
            final ObservableList<SpreadsheetCell> randomRow = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(i, 0, 1, 1, "Random " + (i + 1));
            cell.getStyleClass().add("first-cell");
            randomRow.add(cell);

            for (int column = 1; column < grid.getColumnCount(); column++) {
                randomRow.add(generateCell(i, column, 1, 1));
            }
            rows.add(randomRow);
        }
        grid.setRows(rows);

        grid.getRows().get(15).get(1).getStyleClass().add("span");
        grid.spanRow(2, 15, 1);
        grid.spanColumn(2, 15, 1);

        grid.getRows().get(18).get(1).getStyleClass().add("span");
        grid.spanColumn(4, 18, 1);

        grid.getRows().get(19).get(1).getStyleClass().add("span");
        grid.spanRow(3, 19, 1);
    }


}
