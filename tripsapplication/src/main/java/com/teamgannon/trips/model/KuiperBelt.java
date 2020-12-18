package com.teamgannon.trips.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Defines a Kuiper belt
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class KuiperBelt implements Serializable {

    private static final long serialVersionUID = -3576824052821813249L;

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * whether one exists or not
     */
    private final boolean present = false;

    /**
     * inner edge of the kuiper belt
     */
    private double minRadius;

    /**
     * outer edge of the belt
     */
    private double maxRadius;


    public String convertToString() {
        return convertToString(this);
    }

    private String convertToString(KuiperBelt kuiperBelt) {
        try {
            String asteroidStr = mapper.writeValueAsString(kuiperBelt);
            log.debug("serialized as:" + asteroidStr);
            return asteroidStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", kuiperBelt, e.getMessage());
            return "";
        }
    }


    public String convertToJson(List<KuiperBelt> kuiperBelts) {
        try {
            String kuiperStr = mapper.writeValueAsString(kuiperBelts);
            log.debug("serialized as:" + kuiperStr);
            return kuiperStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", kuiperBelts, e.getMessage());
            return "";
        }
    }

    public @Nullable KuiperBelt toKuiperBelt(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, KuiperBelt.class);
        } catch (IOException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }

}
