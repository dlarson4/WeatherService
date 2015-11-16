package com.example.weatherservice.operations;

import android.view.View;

/**
 * This class defines all the weather-related operations.
 */
public interface WeatherOperations {
    /**
     * Initiate the service binding protocol.
     */
    public void bindService();

    /**
     * Initiate the service unbinding protocol.
     */
    public void unbindService();

    /*
     * Initiate the synchronous weather lookup when the user presses
     * the "Look Up Sync" button.
     */
    public void getWeatherSync(View v);

    /*
     * Initiate the asynchronous weather lookup when the user presses
     * the "Look Up Async" button.
     */
    public void getWeatherAsync(View v);

    /**
     * Called after a runtime configuration change occurs to finish
     * the initialization steps.
     */
    public void onConfigurationChange(com.example.weatherservice.activities.MainActivity activity);
}
