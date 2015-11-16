package com.example.weatherservice.operations;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.weatherservice.R;
import com.example.weatherservice.WeatherCall;
import com.example.weatherservice.WeatherData;
import com.example.weatherservice.WeatherRequest;
import com.example.weatherservice.WeatherResults;
import com.example.weatherservice.activities.MainActivity;
import com.example.weatherservice.services.WeatherServiceAsync;
import com.example.weatherservice.services.WeatherServiceSync;
import com.example.weatherservice.utils.GenericServiceConnection;
import com.example.weatherservice.utils.Utils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class implements all the acronym-related operations defined in
 * the WeatherOperations interface.
 */
public class WeatherOperationsImpl implements WeatherOperations {
    protected final String TAG = getClass().getSimpleName();

    private final static char DEGREE = '\u00B0';

    /**
     * Used to enable garbage collection.
     */
    protected WeakReference<MainActivity> mActivity;

    /**
     * Acronym entered by the user.
     */
    protected WeakReference<EditText> mLocationInput;

    /**
     * List of results to display (if any).
     */
    protected List<WeatherData> mResults;

    protected WeakReference<TextView> mLocationName;
    protected WeakReference<TextView> mTemperature;
    protected WeakReference<TextView> mSunrise;
    protected WeakReference<TextView> mSunset;
    protected WeakReference<TextView> mHumidity;
    protected WeakReference<TextView> mDescription;

    /**
     * This GenericServiceConnection is used to receive results after
     * binding to the WeatherServiceAync Service using bindService().
     */
    private GenericServiceConnection<WeatherCall> mServiceConnectionSync;

    /**
     * This GenericServiceConnection is used to receive results after
     * binding to the WeatherServiceAsync Service using bindService().
     */
    private GenericServiceConnection<WeatherRequest> mServiceConnectionAsync;


    /**
     * This Handler is used to post Runnables to the UI from the
     * mWeatherResults callback methods to avoid a dependency on the
     * Activity, which may be destroyed in the UI Thread during a
     * runtime configuration change.
     */
    private final Handler mDisplayHandler = new Handler();

    /**
     * The implementation of the WeatherResults AIDL Interface, which
     * will be passed to the Acronym Web service using the
     * WeatherResults.get() method.
     * 
     * This implementation of AcronymResults.Stub plays the role of
     * Invoker in the Broker Pattern since it dispatches the upcall to
     * sendResults().
     */
    private final WeatherResults.Stub mWeatherResults = new WeatherResults.Stub() {

        @Override
        public void sendResults(final List<com.example.weatherservice.WeatherData> results) throws RemoteException {
            mDisplayHandler.post(new Runnable() {
                @Override
                public void run() {
                    displayResults(results);
                }
            });
        }
    };


    /**
     * Constructor initializes the fields.
     */
    public WeatherOperationsImpl(MainActivity activity) {
        // Initialize the WeakReference.
        mActivity = new WeakReference<>(activity);

        // Finish the initialization steps.
        initializeViewFields();
        initializeNonViewFields();
    }

    /**
     * Initialize the View fields, which are all stored as
     * WeakReferences to enable garbage collection.
     */
    private void initializeViewFields() {
        // Get references to the UI components.

        mActivity.get().setContentView(R.layout.activity_main);

        // Store the EditText that holds the urls entered by the user (if any).
        mLocationInput = new WeakReference<>((EditText) mActivity.get().findViewById(R.id.locationInput));

        mLocationName = new WeakReference<>((TextView) mActivity.get().findViewById(R.id.locationName));
        mTemperature = new WeakReference<>((TextView) mActivity.get().findViewById(R.id.temperature));
        mSunrise = new WeakReference<>((TextView) mActivity.get().findViewById(R.id.sunrise));
        mSunset = new WeakReference<>((TextView) mActivity.get().findViewById(R.id.sunset));
        mHumidity = new WeakReference<>((TextView) mActivity.get().findViewById(R.id.humidity));
        mDescription = new WeakReference<>((TextView) mActivity.get().findViewById(R.id.description));

        // Display results if any (due to runtime configuration change).
        if (mResults != null) {
            displayResults(mResults);
        }
    }

    /**
     * (Re)initialize the non-view fields (e.g.,
     * GenericServiceConnection objects).
     */
    private void initializeNonViewFields() {
        mServiceConnectionSync = new GenericServiceConnection<>(WeatherCall.class);
        mServiceConnectionAsync = new GenericServiceConnection<>(WeatherRequest.class);
    }

    /**
     * Initiate the service binding protocol.
     */
    @Override
    public void bindService() {
        Log.d(TAG,
                "calling bindService()");

        // Launch the Weather Bound Services if they aren't already
        // running via a call to bindService(), which binds this
        // activity to the AcronymService* if they aren't already
        // bound.
        if (mServiceConnectionSync.getInterface() == null) {
            mActivity.get().getApplicationContext().bindService(WeatherServiceSync.makeIntent(mActivity.get()), mServiceConnectionSync, Context.BIND_AUTO_CREATE);
        }

        if (mServiceConnectionAsync.getInterface() == null) {
            mActivity.get().getApplicationContext().bindService(WeatherServiceAsync.makeIntent(mActivity.get()), mServiceConnectionAsync, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Initiate the service unbinding protocol.
     */
    @Override
    public void unbindService() {
        if (mActivity.get().isChangingConfigurations()) {
            Log.d(TAG, "just a configuration change - unbindService() not called");
        }
        else {
            Log.d(TAG, "calling unbindService()");

            // Unbind the Async Service if it is connected.
            if (mServiceConnectionAsync.getInterface() != null) {
                mActivity.get().getApplicationContext().unbindService (mServiceConnectionAsync);
            }

            // Unbind the Sync Service if it is connected.
            if (mServiceConnectionSync.getInterface() != null) {
                mActivity.get().getApplicationContext().unbindService(mServiceConnectionSync);
            }
        }
    }

    /**
     * Called after a runtime configuration change occurs to finish
     * the initialization steps.
     */
    public void onConfigurationChange(MainActivity activity) {
        Log.d(TAG, "onConfigurationChange() called");

        // Reset the mActivity WeakReference.
        mActivity = new WeakReference<>(activity);

        // (Re)initialize all the View fields.
        initializeViewFields();
    }

    /*
     * Initiate the asynchronous acronym lookup when the user presses
     * the "Look Up Async" button.
     */
    public void getWeatherAsync(View v) {
        final WeatherRequest request = mServiceConnectionAsync.getInterface();

        if (request != null) {
            // Get the acronym entered by the user.
            final String location = mLocationInput.get().getText().toString();

            resetDisplay();

            try {
                // Invoke a one-way AIDL call, which does not block
                // the client.  The results are returned via the
                // sendResults() method of the mAcronymResults
                // callback object, which runs in a Thread from the
                // Thread pool managed by the Binder framework.
                request.getCurrentWeather(location, mWeatherResults);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.d(TAG, "WeatherRequest was null.");
        }
    }

    /*
     * Initiate the synchronous acronym lookup when the user presses
     * the "Look Up Sync" button.
     */
    public void getWeatherSync(View v) {
        final WeatherCall weatherCall = mServiceConnectionSync.getInterface();

        if (weatherCall != null) {
            // Get the acronym entered by the user.
            final String location = mLocationInput.get().getText().toString();

            resetDisplay();

            // Use an anonymous AsyncTask to download the Acronym data
            // in a separate thread and then display any results in
            // the UI thread.
            new AsyncTask<String, Void, List<WeatherData>>() {
                private String location;
                /**
                 * Retrieve the expanded acronym results via a
                 * synchronous two-way method call, which runs in a
                 * background thread to avoid blocking the UI thread.
                 */
                protected List<WeatherData> doInBackground(String... locations) {
                    try {
                        location = locations[0];
                        return weatherCall.getCurrentWeather(location);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    return null;
                }

                /**
                 * Display the results in the UI Thread.
                 */
                protected void onPostExecute(List<WeatherData> weatherDataList) {
                    if (weatherDataList != null && weatherDataList.size() > 0) {
                        displayResults(weatherDataList);
                    }
                    else {
                        Utils.showToast(mActivity.get(), "No weather found for " + location);
                    }
                }
                // Execute the AsyncTask to expand the acronym without blocking the caller.
            }.execute(location);
        } else {
            Log.d(TAG, "WeatherCall was null.");
        }
    }

    /**
     * Display the results to the screen.
     * 
     * @param results
     *            List of Results to be displayed.
     */
    private void displayResults(List<WeatherData> results) {
        mResults = results;

        if (results != null && results.size() > 0) {
            WeatherData data = results.get(0);
            mLocationName.get().setText(data.getmName() + ", " + data.getmCountry());
            mTemperature.get().setText(String.valueOf(data.getmTemp()) + DEGREE + " F");

            if (data.getmSunrise() > 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss aaa z");
                String formatted = formatter.format(new Date(data.getmSunrise()));
                mSunrise.get().setText(formatted);
            } else {
                mSunrise.get().setText("Not available");
            }

            if (data.getmSunset() > 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss aaa z");
                String formatted = formatter.format(new Date(data.getmSunset()));
                mSunset.get().setText(formatted);
            } else {
                mSunset.get().setText("Not available");
            }

            mHumidity.get().setText(String.valueOf(data.getmHumidity()) + '%');
            mDescription.get().setText(data.getmDescription());
        }
    }

    /**
     * Reset the display prior to attempting to expand a new acronym.
     */
    private void resetDisplay() {
        Utils.hideKeyboard(mActivity.get(), mLocationInput.get().getWindowToken());
        mResults = null;
    }
}
