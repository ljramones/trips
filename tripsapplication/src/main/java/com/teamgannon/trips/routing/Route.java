package com.teamgannon.trips.routing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Routes are lists of stars which provide a route via jumps of no more than X light years. Generally,
 * it is my understanding that routes sort of “belong” to polities, in that they are generated by the
 * jump capabilities of a polity.  Their appearance on screen is controlled by the route objects in
 * the theme. In principle then, they’re a very simple object
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */

@Slf4j
@Data
public class Route implements Serializable {

    /**
     * used for JSON serialization
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    @Serial
    private static final long serialVersionUID = 6432469144778289966L;

    /**
     * unique id for reference
     */
    private UUID uuid;

    /**
     * Name given to this route.  May default to “Start star to End Star” when generated but it can be
     * user edited to anything.
     */
    private String routeName;

    /**
     * Back up in the theme, we made an array of route types.  Each route has to be of some type
     */
    private UUID RouteType;

    /**
     * An array of star GUIDs which defines this route
     */
    private @NotNull List<UUID> routeStars = new ArrayList<>();

    /**
     * the list of star names in the route
     */
    private @NotNull List<String> routeStarNames = new ArrayList<>();

    /**
     * we calculate the route lengths when we create a route and store them so we don't have to
     * recalculate each time we draw this.
     */
    private @NotNull List<Double> routeLengths = new ArrayList<>();

    /**
     * the notes for the route
     */
    private String routeNotes;

    /**
     * the color we selected for the route
     */
    private String routeColor;

    /**
     * the line width to draw
     */
    private double lineWidth;

    /**
     * the star this route starts at
     */
    private String startingStar;

    /**
     * the total length of the route, so I don't need to recalculate this
     */
    private double totalLength;

    ////////////

    public String convertToJson() {
        return convertToJson(this);
    }

    public String convertToJson(Route route) {
        try {
            String routeStr = mapper.writeValueAsString(route);
            log.debug("serialized as:" + routeStr);
            return routeStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", route, e.getMessage());
            return "";
        }
    }

    public String convertToJson(List<Route> routeList) {
        try {
            String routeListStr = mapper.writeValueAsString(routeList);
            log.debug("serialized as:" + routeListStr);
            return routeListStr;
        } catch (IOException e) {
            log.error("couldn't serialize this {} because of {}:", routeList, e.getMessage());
            return "";
        }
    }

    public @Nullable List<Route> toRoute(String parametersStr) {
        try {
            return mapper.readValue(parametersStr, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("couldn't deserialize this {} because of {}:", parametersStr, e.getMessage());
            return null;
        }
    }


}
