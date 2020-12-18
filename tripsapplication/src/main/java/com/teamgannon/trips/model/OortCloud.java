package com.teamgannon.trips.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Defines the oort cloud (can only be one)
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class OortCloud implements Serializable {

    private static final long serialVersionUID = -168129162062352722L;

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * whether one exists or not
     */
    private final boolean present = false;

    /**
     * inner edge of the oort cloud
     */
    private double minRadius;

    /**
     * outer edge of the cloud
     */
    private double maxRadius;


    public String convertToJson() {
        return convertToJson(this);
    }

    private String convertToJson(OortCloud oortCloud) {
        try {
            String oortCloudStr = mapper.writeValueAsString(oortCloud);
            log.debug("serialized as:" + oortCloudStr);
            return oortCloudStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", oortCloud, e.getMessage());
            return "";
        }
    }

    public @Nullable OortCloud toOortCloud(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, OortCloud.class);
        } catch (IOException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }

}
