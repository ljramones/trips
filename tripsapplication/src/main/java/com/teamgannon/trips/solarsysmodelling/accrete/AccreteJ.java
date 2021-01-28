package com.teamgannon.trips.solarsysmodelling.accrete;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccreteJ {
    public static void main(String[] args) {
        boolean habitable = false;

        /*
        System s = new System(true, false, false);
        java.lang.System.out.println(s);
        */

        StarSystem s;
        int count = 0;
        while (!habitable) {
            s = new StarSystem(true, false, false);
            if (s.isHabitable()) {
                habitable = true;
                log.info("Discarded " + count + " systems finding this one.");
                log.info(s.toString());
            } else {
                count++;
            }
        }
    }


}
