package com.teamgannon.trips.dialogs.tycho2hip;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TychoFileReader {


    public static List<Tycho2HipRecord> readCSV(String filePath) throws IOException {
        List<Tycho2HipRecord> stars = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // skip the header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                Tycho2HipRecord star = new Tycho2HipRecord();

                star.id = parseLongOrDefault(fields[0].trim());
                star.tyc = fields[1].trim();
                star.gaia = fields[2].trim();
                star.hyg = fields[3].trim();
                star.hip = fields[4].trim();
                star.hd = fields[5].trim();
                star.hr = fields[6].trim();
                star.gl = fields[7].trim();
                star.bayer = fields[8].trim();
                star.flam = fields[9].trim();
                star.con = fields[10].trim();
                star.proper = fields[11].trim();
                star.ra = parseDoubleOrDefault(fields[12].trim());
                star.dec = parseDoubleOrDefault(fields[13].trim());
                star.pos_src = fields[14].trim();
                star.dist = parseDoubleOrDefault(fields[15].trim());
                star.x0 = parseDoubleOrDefault(fields[16].trim());
                star.y0 = parseDoubleOrDefault(fields[17].trim());
                star.z0 = parseDoubleOrDefault(fields[18].trim());
                star.dist_src = fields[19].trim();
                star.mag = parseDoubleOrDefault(fields[20].trim());
                star.absmag = parseDoubleOrDefault(fields[21].trim());
                star.mag_src = fields[22].trim();

                stars.add(star);
            }
        }
        return stars;
    }

    public static long parseLongOrDefault(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(input.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public static double parseDoubleOrDefault(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public static void main(String[] args) {
        try {
            List<Tycho2HipRecord> stars = readCSV("/Users/larrymitchell/tripsnew/trips/files/hyg_db/athyg_v1_0.csv");
            // Do something with the stars, like print them out
            for (Tycho2HipRecord star : stars) {
                System.out.println(star.id + " " + star.tyc+ " "+star.ra); // ... and so on
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
