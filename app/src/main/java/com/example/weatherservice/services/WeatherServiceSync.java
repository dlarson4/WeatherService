package com.example.weatherservice.services;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.weatherservice.WeatherCall;
import com.example.weatherservice.WeatherData;
import com.example.weatherservice.utils.Utils;
import com.example.weatherservice.utils.WeatherCache;

import java.util.ArrayList;
import java.util.List;

public class WeatherServiceSync extends LifecycleLoggingService {
    /**
     * Factory method that makes an Intent used to start the
     * AcronymServiceSync when passed to bindService().
     *
     * @param context The context of the calling component.
     */
    public static Intent makeIntent(Context context) {
        return new Intent(context, WeatherServiceSync.class);
    }

    /**
     * Called when a client (e.g., AcronymActivity) calls
     * bindService() with the proper Intent.  Returns the
     * implementation of AcronymCall, which is implicitly cast as an
     * IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mWeatherResultsImpl;
    }

    /**
     * The concrete implementation of the AIDL Interface AcronymCall,
     * which extends the Stub class that implements WeatherCall,
     * thereby allowing Android to handle calls across process
     * boundaries.  This method runs in a separate Thread as part of
     * the Android Binder framework.
     * <p/>
     * This implementation plays the role of Invoker in the Broker
     * Pattern.
     */
    private final WeatherCall.Stub mWeatherResultsImpl = new WeatherCall.Stub() {
        @Override
        public List<WeatherData> getCurrentWeather(String location) throws RemoteException {
            Log.d(TAG, "getCurrentWeather");

            List<WeatherData> results = WeatherCache.INSTANCE.get(location);
            if(results == null) {
                results = Utils.getWeather(location);
                WeatherCache.INSTANCE.put(location, results);
            }
            Log.d(TAG, "WeatherData results = " + results);
            if(results == null) {
                return new ArrayList<>();
            }
            return results;
        }
    };
}
