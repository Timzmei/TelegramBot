package timzmei.bot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Dadata {

    private URL url;
    private HttpURLConnection con;


    public Dadata(double lat, double lon) throws IOException {

    url = new URL("https://suggestions.dadata.ru/suggestions/api/4_1/rs/geolocate/address?" +
            "token=503c8ae71dac15cbb1a8c6229f89d65eb5e41234" +
            "&lat=" + lat +
            "&lon=" + lon +
            "&radius_meters=10");
    con  = (HttpURLConnection) url.openConnection();
}

    public String getAddress() throws ProtocolException {
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String json = getResponse(con);
        con.disconnect();
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        return convertedObject.getAsJsonArray("suggestions").get(0).getAsJsonObject().get("value").toString();
    }


    public String getResponse(HttpURLConnection con){
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            final StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            return content.toString();
        } catch (final Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }


}