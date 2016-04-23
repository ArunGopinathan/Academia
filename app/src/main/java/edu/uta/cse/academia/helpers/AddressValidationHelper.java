package edu.uta.cse.academia.helpers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.uta.cse.academia.Models.Address;

/**
 * Created by Arun on 4/20/2016.
 */
public class AddressValidationHelper {
    public Address validateAddressWithGoogleApi(Address validateAddr) {
        String result = "";


        List<Address> la = new ArrayList<Address>();

        try {
            String path = "http://maps.google.com/maps/api/geocode/json?address=";
            path += validateAddr.getAddressLine1() + ',' + validateAddr.getAddressLine2() + ',' + validateAddr.getCity() + ',' + validateAddr.getState() + ',' + validateAddr.getZipCode();
            path = path.replaceAll(" ", "");
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                int count = 0;
                List<String> va = new ArrayList<String>();
                JSONObject addr = new JSONObject(sb.toString());
                Log.w("PTS-Android", "re:" + addr);

                JSONArray resultsArray = (JSONArray) addr.get("results");
                Log.w("PTS-Android", "re:" + addr.getString("status"));
                if (addr.getString("status").equals("OK")) {
                    Log.w("PTS-Android", "num:" + resultsArray.length());
                    validateAddr.setLattitude(resultsArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat"));
                    validateAddr.setLongitude(resultsArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng"));

                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return validateAddr;
    }
}

