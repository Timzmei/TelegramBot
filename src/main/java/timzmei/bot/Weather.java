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
    private HashMap<String, String> iconMap;

    private static final String API_KEY = "5bb528d90985eeb90ef1fea3021e44af";

    public Weather(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        
        iconMap = new HashMap<>();
        fillMap();

    }

    private void fillMap() {
        iconMap.put("01", "☀️");
        iconMap.put("02","⛅️");
        iconMap.put("03","☁️");
        iconMap.put("04","☁️");
        iconMap.put("10","\uD83C\uDF26");
        iconMap.put("09","\uD83C\uDF27");
        iconMap.put("11","⛈");
        iconMap.put("13","\uD83C\uDF28");
        iconMap.put("50","🌫");
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
                "&exclude=minutely,alerts&" +
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
        String picNum = currentWeather.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").toString().replaceAll("\\D", "");
        String currentTemp = currentWeather.get("temp").toString().replaceAll("\\.[\\d]*", ""); // температура
        String crrntTempFl = currentWeather.get("feels_like").toString().replaceAll("\\.[\\d]*", ""); // температура по ощущениям
        String humidity = currentWeather.get("humidity").toString(); //влажность
        String crntDescription = currentWeather.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").toString().replaceAll("\"", "");
        JsonArray daily = weatherResponse.get("daily").getAsJsonArray();
        JsonArray hourly = weatherResponse.get("hourly").getAsJsonArray();

        return crntDescription + " " + iconMap.get(picNum) + "\n темп-ра " + getSignTemp(currentTemp) + " ощущается как: " + getSignTemp(crrntTempFl) + "\nВ следующие дни:\n" + getDaylyWeather(daily);

    }

    private String getDaylyWeather(JsonArray daily) {

        
        String daylyWeather = "";

        for (int i = 0; i < 5; i++){
            JsonObject dayObject = daily.get(i).getAsJsonObject();

//            pressure = dayObject.get("pressure").getAsInt();

            String picNum = dayObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").toString().replaceAll("\\D", "");

//            System.out.println(picNum);
            String date = Instant.ofEpochMilli(dayObject.get("dt").getAsLong() * 1000).atZone(ZoneId.of("UTC")).toLocalDate().format(DateTimeFormatter.ofPattern("EE dd.MM"));
            String tempMax = dayObject.getAsJsonObject("temp").get("max").toString().replaceAll("\\.[\\d]*", "");
            String tempMin = dayObject.getAsJsonObject("temp").get("min").toString().replaceAll("\\.[\\d]*", "");

            daylyWeather = daylyWeather.concat(date + " " + iconMap.get(picNum) + " " + getSignTemp(tempMin) + "..." + getSignTemp(tempMax) + "\n");

        }
        return daylyWeather;
    }

    private static String getSignTemp(String temp) {
        return Double.parseDouble(temp) > 0 ? "+" + temp : "-" + temp;
    }
}
