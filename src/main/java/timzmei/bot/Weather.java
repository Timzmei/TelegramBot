package timzmei.bot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

public class Weather {

    private URL url;
    private HttpURLConnection con;

    public Weather() throws IOException {
        url = new URL("https://api.openweathermap.org/data/2.5/onecall?" +
                "lat=54.775002&" +
                "lon=56.037498&" +
                "exclude=minutely,hourly,alerts&" +
                "appid=5bb528d90985eeb90ef1fea3021e44af&" +
                "units=metric&" +
                "lang=ru");
        con  = (HttpURLConnection) url.openConnection();
    }

    public String getWeather() throws ProtocolException {
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String json = getResponse(con);
        con.disconnect();
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//        printWeather(convertedObject);

        return printWeather(convertedObject);
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
        String picSunny = "☀️";
        String picPartlySunny = "⛅️";
        String picCloudy = "☁️";
        String picSmallRain = "\uD83C\uDF26";
        String picRain = "\uD83C\uDF27";
        String picRainLight = "⛈";
        String picSnow = "\uD83C\uDF28";




        System.out.printf("Координаты города Уфы: с.ш. %s, в.д. %s\n", weatherResponse.get("lat").toString(), weatherResponse.get("lon").toString());
        JsonObject currentWeather = weatherResponse.getAsJsonObject("current");
        String currentTemp = currentWeather.get("temp").toString();
        String crrntTempFl = currentWeather.get("feels_like").toString();
        String crntDescription = currentWeather.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").toString().replaceAll("\"", "");
        JsonArray daily = weatherResponse.get("daily").getAsJsonArray();

//        System.out.println(weatherResponse);

        double diffNightMorn = 30d;
        long pressureDay = 0;
        long diffDay = 0;
        int pressure = 0;

        return "Сегодня: " + crntDescription + "\n темп-ра " + (Double.parseDouble(currentTemp) > 0 ? "+" + currentTemp : "-" + currentTemp) + " ощущается: " + (Double.parseDouble(crrntTempFl) > 0 ? "+" + crrntTempFl : "-" + crrntTempFl) + "\nВ следующие дни\n" + getDaylyWeather(daily);

//        System.out.printf("Максимальное давление за предстоящие 5 дней (включая текущий): %s, дата: %s\n", pressure, Instant.ofEpochMilli(pressureDay * 1000).atZone(ZoneId.of("UTC")).toLocalDate());
//        System.out.printf("День с минимальной разницей между ночной и утренней температурой : %.2f, дата: %s\n", diffNightMorn, Instant.ofEpochMilli(diffDay * 1000).atZone(ZoneId.of("UTC")).toLocalDate());
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
