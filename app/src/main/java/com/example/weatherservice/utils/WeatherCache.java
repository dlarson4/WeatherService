package com.example.weatherservice.utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.weatherservice.WeatherData;

import java.util.HashMap;
import java.util.List;

public enum  WeatherCache {
    INSTANCE;

    private final static String TAG =  WeatherCache.class.getSimpleName();
    private final static long MAX_AGE = 10 * 1000; // 10 seconds

    private HashMap<String, List<WeatherData>> weatherDataMap = new HashMap<>();
    private HashMap<String, Long> weatherDataDateMap = new HashMap<>();

    public synchronized List<WeatherData> get(final String name) {
        if(TextUtils.isEmpty(name)) {
            return null;
        }
        List<WeatherData> weatherData = weatherDataMap.get(name);
        Log.d(TAG, "Location " + name + (weatherData == null ? " not" : "") + " found in cache");

        if(weatherData == null) {
            return null;
        }

        final Long cacheTime = weatherDataDateMap.get(name);
        if(cacheTime == null) { // shouldn't happen
            weatherDataMap.remove(name);
            return null;
        }

        final long currentTime = System.currentTimeMillis();
        if(currentTime - MAX_AGE > cacheTime) {
            Log.d(TAG, "Location " + name + " has aged out of cache");
            weatherDataMap.remove(name);
            weatherDataDateMap.remove(name);
            return null;
        }
        return weatherData;
    }

    public synchronized void put(final String name, final List<WeatherData> weatherData) {
        // overwrite if it's already there
        weatherDataMap.put(name, weatherData);
        weatherDataDateMap.put(name, System.currentTimeMillis());
    }
}
