package com.teamgannon.trips.file.excel;

import com.teamgannon.trips.file.excel.model.RBExcelFile;
import com.teamgannon.trips.file.excel.model.RBStar;
import com.teamgannon.trips.stardata.StellarFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class ExcelReader {

    /**
     * the stellar factory
     */
    private StellarFactory stellarFactory;

    // Create a DataFormatter to format and get each cell's value as String
    private DataFormatter dataFormatter = new DataFormatter();


    /**
     * dependency injection
     *
     * @param stellarFactory the stellar fatory used to create objects in the DB
     */
    public ExcelReader(StellarFactory stellarFactory) {
        this.stellarFactory = stellarFactory;
    }

    /**
     * load the excel file
     *
     * @param file the excel file in RB Format
     */
    public RBExcelFile loadFile(File file) {

        RBExcelFile excelFile = new RBExcelFile();
        excelFile.setFileName(file.getAbsolutePath());
        excelFile.setAuthor("anonymous");

        Set<RBStar> starList = new HashSet<>();
        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = null;
        try {

            workbook = WorkbookFactory.create(file);

            // Retrieving the number of sheets in the Workbook
            log.info("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

            // iterate over each sheet in work book
            // usually there is only one sheet in this type of workbook but this is afor instance
            for (Sheet sheet : workbook) {
                starList.addAll(parseSheet(sheet));
            }

            // close the workbook
            workbook.close();
            starList.forEach(excelFile::addStar);
            return excelFile;

        } catch (IOException e) {
            log.error("Failed to read {} because of ", file.getName(), e);
            return excelFile;
        }

    }

    /**
     * parse a workbook sheet
     *
     * @param sheet the sheet to parse
     */
    private Set<RBStar> parseSheet(Sheet sheet) {
        log.info("=> " + sheet.getSheetName());

        Set<RBStar> starList = new HashSet<>();

        // first row is the headers
        boolean firstRow = true;

        for (Row row : sheet) {
            if (firstRow) {
                firstRow = false;
                continue;
            }
            RBStar star = new RBStar();

            // note that this format is very specific and if the fileds are not in this order then
            // parsing will return crap
            star.setNumber(getCell(row, 0));
            star.setStarName(getCell(row, 1));
            star.setCatalog1(getCell(row, 2));
            star.setCatalog2(getCell(row, 3));
            star.setSimbadId(getCell(row, 4));
            star.setType(getCell(row, 5));
            star.setParallax(getCell(row, 6));
            star.setMagu(getCell(row, 7));
            star.setMagb(getCell(row, 8));
            star.setMagv(getCell(row, 9));
            star.setMagr(getCell(row, 10));
            star.setMagi(getCell(row, 11));
            star.setSpectralType(getCell(row, 12));
            star.setParents(getCell(row, 13));
            star.setSiblings(getCell(row, 14));
            star.setChildren(getCell(row, 15));
            star.setRa_dms(getCell(row, 16));
            star.setDec_dms(getCell(row, 17));
            star.setDec_deg(getCell(row, 18));
            star.setRs_cdeg(getCell(row, 19));
            star.setLyDistance(getCell(row, 20));
            star.setX(getCell(row, 21));
            star.setY(getCell(row, 22));
            star.setZ(getCell(row, 23));

            starList.add(star);
        }

        log.debug("Parsed {} stars", starList.size());
        return starList;

    }

    private String getCell(Row row, int cellNumber) {
        return dataFormatter.formatCellValue(row.getCell(cellNumber));
    }

    /**
     * a maiin for testing
     *
     * @param args the input args which I don't use
     * @throws IOException            file read exception
     * @throws InvalidFormatException bad format
     */
    public static void main(String[] args) throws IOException, InvalidFormatException {

        File file = new File("/Users/larrymitchell/ChuckGannon/terranrepublicviewer/trips/stardata/clean-chview.xlsx");

        ExcelReader reader = new ExcelReader(null);
        RBExcelFile excelFile = reader.loadFile(file);
        log.info("parsing complete, {} read", excelFile.getAstrographicObjects().size());

    }
}
