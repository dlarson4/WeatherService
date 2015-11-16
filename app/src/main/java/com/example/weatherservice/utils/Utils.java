package com.example.weatherservice.utils;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.weatherservice.WeatherData;
import com.example.weatherservice.jsonweather.JsonWeather;
import com.example.weatherservice.jsonweather.WeatherJSONParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.example.weatherservice.BuildConfig.DEBUG;

public class Utils {
    /**
     * Logging tag used by the debugger. 
     */
    private final static String TAG = Utils.class.getCanonicalName();

    private Utils() {
    }

    public static List<WeatherData> getWeather(String location) {

        List<JsonWeather> jsonWeatherList = null;
        List<WeatherData> weatherDataList = new ArrayList<>();

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("http://api.openweathermap.org/data/2.5/weather?units=imperial&q=");
            sb.append(URLEncoder.encode(location, "UTF-8"));
            sb.append("&appid=da3b9dcfcc850a80c9df6a08c030a0fc");

            if(DEBUG) Log.d(TAG, "Weather URL: " + sb.toString());

            final URL url = new URL(sb.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try (InputStream in = new BufferedInputStream(urlConnection.getInputStream())) {
                jsonWeatherList = new WeatherJSONParser().parseJsonStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage(), e);
        }

        if (jsonWeatherList != null) {
            for (JsonWeather w : jsonWeatherList) {
                weatherDataList.add(new WeatherData(
                        w.getName(),
                        w.getWind().getSpeed(),
                        w.getWind().getDeg(),
                        w.getMain().getTemp(),
                        w.getMain().getHumidity(),
                        w.getSys().getSunrise(),
                        w.getSys().getSunset(),
                        w.getWeather().get(0).getDescription(),
                        w.getSys().getCountry()));
            }
        }
        return weatherDataList;
    }

    /**
     * This method is used to hide a keyboard after a user has
     * finished typing the url.
     */
    public static void hideKeyboard(Activity activity, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    /**
     * Show a toast message.
     */
    public static void showToast(Context context,
                                 String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
