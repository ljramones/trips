package util;

import java.io.*;
import java.net.*;

public class SimbadQuery {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java SimbadQuery [identifier]");
            System.exit(1);
        }
        String identifier = args[0];

        String url = "http://simbad.u-strasbg.fr/simbad/sim-script";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");

        // Specify the votable command to get an XML response
        String script = "votable {"
                + "\n  all"
                + "\n}"
                + "\nquery id " + identifier;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes("script=" + URLEncoder.encode(script, "UTF-8"));
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Print the response
        System.out.println(response.toString());

        System.out.println("complete\n\n");
    }
}




