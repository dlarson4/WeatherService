package com.example.weatherservice.jsonweather;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Json weather data returned from the Weather Services API
 * and returns a List of JsonWeather objects that contain this data.
 */
public class WeatherJSONParser {
    /**
     * Used for logging purposes.
     */
    private final String TAG = this.getClass().getCanonicalName();

    /**
     * Parse the @a inputStream and convert it into a List of JsonWeather
     * objects.
     */
    public List<JsonWeather> parseJsonStream(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return parseJsonWeatherArray(reader);
        } finally {
            reader.close();
        }
    }

    /**
     * Parse a Json stream and convert it into a List of JsonWeather
     * objects.
     */
    public List<JsonWeather> parseJsonWeatherArray(JsonReader reader) throws IOException {
        List<JsonWeather> messages = new ArrayList();
        reader.beginObject();
        while (reader.hasNext()) {
            messages.add(parseJsonWeather(reader));
        }
        reader.endObject();
        return messages;
    }

    /**
     * Parse a Json stream and return a JsonWeather object.
     */
    public JsonWeather parseJsonWeather(JsonReader reader) throws IOException {
        Sys sys = null;
        String base = null;
        Main main = null;
        List<Weather> weather = null;
        Wind wind = null;
        long dt = 0;
        long id = 0;
        String name = null;
        long cod = 0;

        while (reader.hasNext()) {
            String propertyName = reader.nextName();
            if (propertyName.equals(JsonWeather.sys_JSON)) {
                sys = parseSys(reader);
            } else if (propertyName.equals(JsonWeather.base_JSON)) {
                base = reader.nextString();
            } else if (propertyName.equals(JsonWeather.main_JSON)) { // && reader.peek() != JsonToken.NULL) {
                main = parseMain(reader);
            } else if (propertyName.equals(JsonWeather.weather_JSON)) {
                weather = parseWeathers(reader);
            } else if (propertyName.equals(JsonWeather.wind_JSON)) {
                wind = parseWind(reader);
            } else if (propertyName.equals(JsonWeather.dt_JSON)) {
                dt = reader.nextLong();
            } else if (propertyName.equals(JsonWeather.id_JSON)) {
                id = reader.nextLong();
            } else if (propertyName.equals(JsonWeather.name_JSON)) {
                name = reader.nextString();
            } else if (propertyName.equals(JsonWeather.cod_JSON)) {
                cod = reader.nextLong();
            } else {
                reader.skipValue();
            }
        }
        return new JsonWeather(sys, base,  main, weather,  wind,  dt,  id,  name,  cod);
    }

    /**
     * Parse a Json stream and return a List of Weather objects.
     */
    public List<Weather> parseWeathers(JsonReader reader) throws IOException {
        List<Weather> messages = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(parseWeather(reader));
        }
        reader.endArray();
        return messages;
    }

    /**
     * Parse a Json stream and return a Weather object.
     */
    public Weather parseWeather(JsonReader reader) throws IOException {
        long id = 0;
        String main = null;
        String description = null;
        String icon = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String propertyName = reader.nextName();
            if (propertyName.equals(Weather.description_JSON)) {
                description = reader.nextString();
            } else if (propertyName.equals(Weather.icon_JSON)) {
                icon = reader.nextString();
            } else if (propertyName.equals(Weather.id_JSON)) { // && reader.peek() != JsonToken.NULL) {
                id = reader.nextLong();
            } else if (propertyName.equals(Weather.main_JSON)) {
                main = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        Weather weather = new Weather();
        weather.setDescription(description);
        weather.setIcon(icon);
        weather.setId(id);
        weather.setMain(main);

        return weather;
    }

    /**
     * Parse a Json stream and return a Main Object.
     */
    public Main parseMain(JsonReader reader) throws IOException {
        double mTemp = 0;
        double mTempMin = 0;
        double mTempMax = 0;
        double mPressure = 0;
        double mSeaLevel = 0;
        double mGrndLevel = 0;
        long mHumidity = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String propertyName = reader.nextName();
            if (propertyName.equals(Main.grndLevel_JSON)) {
                mGrndLevel = reader.nextDouble();
            } else if (propertyName.equals(Main.humidity_JSON)) {
                mHumidity = reader.nextLong();
            } else if (propertyName.equals(Main.pressure_JSON)) { // && reader.peek() != JsonToken.NULL) {
                mPressure = reader.nextDouble();
            } else if (propertyName.equals(Main.seaLevel_JSON)) {
                mSeaLevel = reader.nextDouble();
            } else if (propertyName.equals(Main.temp_JSON)) {
                mTemp = reader.nextDouble();
            } else if (propertyName.equals(Main.tempMin_JSON)) {
                mTempMin = reader.nextDouble();
            } else if (propertyName.equals(Main.tempMax_JSON)) {
                mTempMax = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        Main main = new Main();
        main.setGrndLevel(mGrndLevel);
        main.setHumidity(mHumidity);
        main.setPressure(mPressure);
        main.setSeaLevel(mSeaLevel);
        main.setTemp(mTemp);
        main.setTempMax(mTempMax);
        main.setTempMin(mTempMin);

        return main;
    }

    /**
     * Parse a Json stream and return a Wind Object.
     */
    public Wind parseWind(JsonReader reader) throws IOException {
        double mSpeed = 0;
        double mDeg = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String propertyName = reader.nextName();
            if (propertyName.equals(Wind.deg_JSON)) {
                mDeg = reader.nextDouble();
            } else if (propertyName.equals(Wind.speed_JSON)) {
                mSpeed = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        Wind wind = new Wind();
        wind.setDeg(mDeg);
        wind.setSpeed(mSpeed);

        return wind;
    }

    /**
     * Parse a Json stream and return a Sys Object.
     */
    public Sys parseSys(JsonReader reader) throws IOException {
        double mMessage = 0;
        String mCountry = null;
        long mSunrise = 0;
        long mSunset = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String propertyName = reader.nextName();
            if (propertyName.equals(Sys.country_JSON)) {
                mCountry = reader.nextString();
            } else if (propertyName.equals(Sys.message_JSON)) {
                mMessage = reader.nextDouble();
            } else if (propertyName.equals(Sys.sunrise_JSON)) {
                mSunrise = reader.nextLong();
            } else if (propertyName.equals(Sys.sunset_JSON)) {
                mSunset = reader.nextLong();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        Sys sys = new Sys();
        sys.setCountry(mCountry);
        sys.setMessage(mMessage);
        sys.setSunrise(mSunrise);
        sys.setSunset(mSunset);

        return sys;
    }
}
