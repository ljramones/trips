package com.teamgannon.trips.elasticsearch.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

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
     * whether one exists or not
     */
    private boolean present = false;

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
        return "present:" + kuiperBelt.isPresent() + "," +
                "minRadius:" + kuiperBelt.getMinRadius() + "," +
                "maxRadius:" + kuiperBelt.getMaxRadius();
    }

    public KuiperBelt toKuiperBelt(String parametersStr) {
        KuiperBelt parameters = new KuiperBelt();
        String[] fields = parametersStr.split(",");
        try {
            for (String field : fields) {
                String[] attribute = field.split(":");
                switch (attribute[0]) {
                    case "present":
                        parameters.setPresent(Boolean.parseBoolean(attribute[1]));
                        break;
                    case "minRadius":
                        parameters.setMinRadius(Double.parseDouble(attribute[1]));
                        break;
                    case "maxRadius":
                        parameters.setMaxRadius(Double.parseDouble(attribute[1]));
                        break;
                    default:
                        log.error("unknown label: {}", attribute[0]);
                }
            }
        } catch (NumberFormatException nfe) {
            log.error("bad format error:{}", nfe);
        }
        return parameters;
    }

}
