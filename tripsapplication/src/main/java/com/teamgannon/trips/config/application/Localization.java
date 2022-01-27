package com.teamgannon.trips.config.application;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
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

    @Value("${app.programdata}")
    private String programdata;

}
