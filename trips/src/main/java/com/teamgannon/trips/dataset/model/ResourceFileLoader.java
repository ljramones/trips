package com.teamgannon.trips.dataset.model;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.FileReader;

/**
 * Loading the description file
 * <p>
 * Created by larrymitchell on 2017-03-06.
 */
@Slf4j
public class ResourceFileLoader {

    public LookupFile loadFile() {
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            File file = resolver.getResource("classpath*:/resources/typecodes.csv").getFile();
            LookupFile lookupFile = new LookupFile();

            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextLine = reader.readNext();
            lookupFile.setHeaders(nextLine);
            while ((nextLine = reader.readNext()) != null) {
                LookupDescription lookupDescription = new LookupDescription(nextLine);
                lookupFile.addLookupDescription(lookupDescription);
            }

            return lookupFile;
        } catch (Exception e) {
            log.error("Error while loading the service metadata json files ", e);
        }
        return null;
    }

}
