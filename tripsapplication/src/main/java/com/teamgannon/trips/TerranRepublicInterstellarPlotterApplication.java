package com.teamgannon.trips;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.repository.AstrographicObjectRepository;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
//@SpringBootApplication
public class TerranRepublicInterstellarPlotterApplication implements CommandLineRunner {

    @Autowired
    private AstrographicObjectRepository astrographicObjectRepository;

    public static void main(String[] args) {
        Application.launch(JavafxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // create and save an astro object
        AstrographicObject astrographicObject = new AstrographicObject();
        astrographicObject.setDataSetName("a name");
        astrographicObject.setDisplayName("name");
        astrographicObject.setCatalogIdList(new ArrayList<>());

        AstrographicObject newObj = astrographicObjectRepository.save(astrographicObject);

        Optional<AstrographicObject> optObj = astrographicObjectRepository.findById(newObj.getId());

        if (optObj.isEmpty()) {
            log.info("nothing found");
        } else {
            AstrographicObject retObj = optObj.get();
            log.info("finished saving objects:" + retObj);
        }

    }
}
