package com.teamgannon.trips.dialogs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class RBCsvReader {



    public RBCsvFile loadFile(File file) {
        RBCsvFile rbCsvFile = new RBCsvFile();
        rbCsvFile.setFileName(file.getAbsolutePath());
        rbCsvFile.setAuthor("anonymous");

        return rbCsvFile;
    }

}
