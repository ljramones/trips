package com.teamgannon.trips.config.application;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Data
@Slf4j
@Component
public class Localization {

    @Value("${app.title:unknown}")
    private String title;

    @Value("${app.version:unknown}")
    private String version;

    @Value("${app.releaseDate:unknown}")
    private String releaseDate;

    @Value("${app.contributors:unknown}")
    private String contributors;

    @Value("${app.projectPage:unknown}")
    private String projectPage;

    @Value("${app.fileDirectory:unknown}")
    private String fileDirectory;

    @Value("${app.scriptDirectory:unknown}")
    private String scriptDirectory;

    @Value("${app.programdata:unknown}")
    private String programdata;

    @PostConstruct
    public void ensureDirectoriesExist() {
        createDir(fileDirectory);
        createDir(scriptDirectory);
        createDir(programdata);
    }

    private void createDir(String path) {
        if (path != null && !path.equals("unknown")) {
            File dir = new File(path);
            if (!dir.exists() && dir.mkdirs()) {
                log.info("Created directory: {}", dir.getAbsolutePath());
            }
        }
    }

}
