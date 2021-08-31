package su.tzar.borovovaleksandr.tzar.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;


import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

public class GPS {
    public interface LastLocationRecieved {
        void onLastLocationReceived(Location location);
    }

    private Activity activity;
    private LocationManager manager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public static int INTERVAL_MS = 1000;
    public static int FASTEST_INTERVAL_MS = 1000;
    public static int REQUEST_CHECK_SETTINGS = 3;
    public static int ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    public GPS(Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        this.manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    stopLocationUpdates();
                    log(Float.toString(location.getAccuracy()));
                    // Update UI with location data
                    // ...
                }
            }
        };
    }

    public boolean isEnabled() {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return true;
        }
        return false;
    }

    public void getLastLocation(LastLocationRecieved lastLocationRecieved) {
        fusedLocationClient.getLastLocation().addOnSuccessListener( result -> {
            if(result != null){
                lastLocationRecieved.onLastLocationReceived(result);
            }
        });
    }




    public void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(getLocationRequest(), locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public void updateLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest());
        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build())
                .addOnSuccessListener(result -> {
                    log("LOCATION SETTINGS OK");
                    startLocationUpdates();
                })
                .addOnFailureListener(activity, e -> {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(activity,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_MS);
        locationRequest.setPriority(ACCURACY);
        return locationRequest;
    }

    public float age_minutes(Location last) {
        log("AGE:" + age_ms(last) / (60*1000));
        return age_ms(last) / (60*1000);
    }

    public float age_ms(Location last) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            return age_ms_api_17(last);
        }

        return age_ms_api_pre_17(last);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private float age_ms_api_17(Location last) {
        Long delta = (SystemClock.elapsedRealtimeNanos() - last
                .getElapsedRealtimeNanos()) / 1000000;
        return delta;

    }

    private float age_ms_api_pre_17(Location last) {
        return System.currentTimeMillis() - last.getTime();
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}
