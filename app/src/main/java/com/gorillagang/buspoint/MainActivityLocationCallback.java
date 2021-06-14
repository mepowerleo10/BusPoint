package com.gorillagang.buspoint;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;

import java.lang.ref.WeakReference;

public class MainActivityLocationCallback
        implements LocationEngineCallback<LocationEngineResult> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final WeakReference<MainActivity> activityWeakReference;

    public MainActivityLocationCallback(MainActivity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        MainActivity activity = activityWeakReference.get();

        if (activity != null) {
            Location location = result.getLastLocation();
            if (location == null) {
                return;
            }

            // Pass the new location to the Maps SDK's LocationComponent
            if (activity.mapboxMap != null && result.getLastLocation() != null) {
                activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
            }
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        Log.d(TAG, exception.getLocalizedMessage());
        MainActivity activity = activityWeakReference.get();
        if (activity != null) {
            Toast.makeText(activity, exception.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}