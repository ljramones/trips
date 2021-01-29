package com.teamgannon.trips.stellarmodelling;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpectralClassUtils {

    public static Map<String, String> pecularities = Stream.of(new String[][]{
            {"wk", "Weak lines"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

}
