package timzmei.bot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Weather {

    private final String picSunny = "☀️";
    private final String picPartlySunny = "⛅️";
    private final String picCloudy = "☁️";
    private final String picSmallRain = "\uD83C\uDF26";
    private final String picRain = "\uD83C\uDF27";
    private final String picRainLight = "⛈";
    private final String picSnow = "\uD83C\uDF28";

    private URL url;
    private HttpURLConnection con;
    private double lat;
    private double lon;

    private static final String API_KEY = "5bb528d90985eeb90ef1fea3021e44af";

    public Weather(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;

    }

    public String getAddress() throws IOException {
        url = new URL("http://api.openweathermap.org/geo/1.0/reverse?" +
                "lat=" + lat +
                "&lon=" + lon +
                "&limit=5" +
                "&appid=" + API_KEY);
        JsonArray convertedObject = new Gson().fromJson(getOWMResponse(), JsonArray.class);
        String address = convertedObject.getAsJsonArray().get(0).getAsJsonObject().get("local_names").getAsJsonObject().get("ru").toString();
        return address;
    }

    public String getWeather(String location) throws IOException {
        url = new URL("https://api.openweathermap.org/data/2.5/onecall?" +
                "lat=" + lat +
                "&lon=" + lon +
                "&exclude=minutely,hourly,alerts&" +
                "appid=" + API_KEY +
                "&units=metric&" +
                "lang=ru");
        JsonObject convertedObject = new Gson().fromJson(getOWMResponse(), JsonObject.class);

        return "Сегодня в " + location + " " + printWeather(convertedObject);
    }

    private String getOWMResponse() throws IOException {
        con  = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String json = getResponse(con);
        con.disconnect();
        return json;
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

    public String printWeather(JsonObject weatherResponse){

        JsonObject currentWeather = weatherResponse.getAsJsonObject("current");
        String currentTemp = currentWeather.get("temp").toString();
        String crrntTempFl = currentWeather.get("feels_like").toString();
        String crntDescription = currentWeather.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").toString().replaceAll("\"", "");
        JsonArray daily = weatherResponse.get("daily").getAsJsonArray();

        return crntDescription + "\n темп-ра " + (Double.parseDouble(currentTemp) > 0 ? "+" + currentTemp : "-" + currentTemp) + " ощущается: " + (Double.parseDouble(crrntTempFl) > 0 ? "+" + crrntTempFl : "-" + crrntTempFl) + "\nВ следующие дни:\n" + getDaylyWeather(daily);

    }

    private String getDaylyWeather(JsonArray daily) {

        HashMap<String, String> iconMap = new HashMap<>();

        iconMap.put("01", "☀️");
        iconMap.put("02","⛅️");
        iconMap.put("03","☁️");
        iconMap.put("04","☁️");
        iconMap.put("10","\uD83C\uDF26");
        iconMap.put("09","\uD83C\uDF27");
        iconMap.put("11","⛈");
        iconMap.put("13","\uD83C\uDF28");
        iconMap.put("50","🌫");
        String daylyWeather = "";

        for (int i = 0; i < 5; i++){
            JsonObject dayObject = daily.get(i).getAsJsonObject();

//            pressure = dayObject.get("pressure").getAsInt();

            String picNum = dayObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").toString().replaceAll("\\D", "");

//            System.out.println(picNum);
            String date = Instant.ofEpochMilli(dayObject.get("dt").getAsLong() * 1000).atZone(ZoneId.of("UTC")).toLocalDate().format(DateTimeFormatter.ofPattern("EE dd.MM"));
            String tempMax = dayObject.getAsJsonObject("temp").get("max").toString();
            daylyWeather = daylyWeather.concat(date + iconMap.get(picNum) + (Double.parseDouble(tempMax) > 0 ? "+" + tempMax : "-" + tempMax) + "\n");

        }
        return daylyWeather;
    }
}
