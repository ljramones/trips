package com.teamgannon.trips.experiments;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HackerrankTest {

    public int countDuplicate(List<Integer> numbers) {
        int duplicate = 0;
        Set<Integer> markSet = new HashSet<>();
        Set<Integer> countOnce = new HashSet<>();
        for (Integer integer : numbers) {
            if (!countOnce.contains(integer)) {
                if (markSet.contains(integer)) {
                    duplicate++;
                    countOnce.add(integer);
                } else {
                    markSet.add(integer);
                }
            }
        }

        return duplicate;
    }

    public String[] getMovieTitles(String substr) {
        int currentPage = 1;
        int totalPages = Integer.MAX_VALUE;  // pick a ridiculously large number
        List<String> titles = new ArrayList<>();
        while (currentPage <= totalPages) {
            try {
                URL obj = new URL("https://jsonmock.hackerrank.com/api/movies/search/?Title=" + substr + "&page=" + currentPage);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String restResp;
                while ((restResp = in.readLine()) != null) {
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(restResp, JsonObject.class);
                    totalPages = jsonObject.get("total_pages").getAsInt();
                    JsonArray dataArray = jsonObject.get("data").getAsJsonArray();
                    for (JsonElement element: dataArray) {
                       JsonObject elementObj= element.getAsJsonObject();
                       String title = elementObj.get("Title").getAsString();
                       titles.add(title);
                    }
                }
                in.close();
                currentPage++;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        Collections.sort(titles);
        return titles.toArray(new String[0]);
    }


    public static void main(String[] args) {
        HackerrankTest test = new HackerrankTest();

        // question 1
        int unique = test.countDuplicate(Arrays.asList(1, 3, 3, 2, 2, 2, 2, 2, 4, 4));
        System.out.println("Number of duplicate is " + unique);

        // question 3
       String[] resultsString= test.getMovieTitles("spiderman");
       System.out.println("# of Titles is " + resultsString.length);


    }
}
