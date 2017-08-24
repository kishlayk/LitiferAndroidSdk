package litifer.com.litiferdemoapp;//package litifer.com.sdk.presentation;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import litifer.com.sdk.data.model.CurrentLocation;
import litifer.com.sdk.data.model.GeofenceEntity;
import litifer.com.sdk.presentation.Litifer;

/**
 * Created by dipu on 7/4/17.
 */

public class LocationService extends Service implements ResultCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mgoogleApiClient = null;
    private String TAG = "Geofence";
    private Location currentLocation;


    public LocationService() {

    }

    private PendingIntent pendingIntent = null;
    private List mgeofenceList = new ArrayList();

    private int mDisconnectionTry = 2;

    LocationRequest locationRequest = null;

    /**
     * Purpose of onCreate is initialisation of google ApiClient.
     * LocationServices.API is api for creating Geofences.
     */

    @Override
    public void onCreate() {

        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();
    }

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not  yet implemented");
    }


    /**
     * @param bundle No use here in this function.
     *               This function is used for creating and monitoring geofences.
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        this.initiateGeofences();
    }


    public void initiateGeofences() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "Access Fine Location and Access Coarse location permission NOT GRANTED.");
        } else {
            Log.i(TAG, "Access Fine Location and Access Coarse location permission granted.");


            Location location = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
            currentLocation = location;
            /**
             * When the location returns null, this generally happens ehrn accessing location
             * first time.
             */
            if (currentLocation == null) {
                locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setFastestInterval(30000);
                locationRequest.setInterval(30000);
                LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, locationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        currentLocation = location;
                        addNewGeofences(location);

                        System.out.println("Location is :" + location);
                    }
                });
            }
            else {
            addNewGeofences(currentLocation);
            }
        }
    }

    /**
     * When google API connection is Suspended.
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("onConnectionSuspended.");
    }


    /**
     * When google API connection fails.
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("ConnectionFailed.");
    }

    /**
     * Result of Google API.
     *
     * @param result
     */
    @Override
    public void onResult(@NonNull Result result) {

        System.out.println("In onResult : " + result.toString());
        if (result.getStatus().isSuccess()) {
            Log.i("LocationServices", "Successfully registered for updates");
        } else {
            Log.e("LocationServices", "Could not register for updates. " + result.getStatus().getStatusMessage());
        }

        mDisconnectionTry--;
        if (mDisconnectionTry == 0) {
            mgoogleApiClient.disconnect();
        }

    }


    /**
     * Destroying the Current Service on Purpose.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();
        System.out.println("location Service is getting destroyed.");
    }

    /**
     * List preparation of Geofences which are to be monitered.
     */


    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getPendingIntent() {
        if (pendingIntent != null)
            return pendingIntent;

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private void addGeofence(List<Geofence> geofences) {
        try {
            // if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            LocationServices.GeofencingApi.addGeofences(mgoogleApiClient,
                    getGeofencingRequest(geofences), getPendingIntent()).setResultCallback(this);
            //}else{
            //Log.i("Gps", "Gps Service not working so GeoTransitionIntentService not launched");
            //this.stopService(new Intent(this, LocationService.class));
            //}


        } catch (SecurityException ex) {
            Log.e("Exception", ex.getStackTrace().toString());
        } catch (IllegalArgumentException e) {
            Log.e("Exception", "Notification Request not made since no internet connection.");
            Log.e("Exception", e.getStackTrace().toString());
        }
    }

    private void addNewGeofences(Location location) {

        CurrentLocation.init()
                .setLatitude(currentLocation.getLatitude())
                .setLongitude(currentLocation.getLongitude());

        Litifer.init(this).getGeofenceList(currentLocation.getLatitude(), currentLocation.getLongitude(), new Litifer.GeofenceListener() {
            @Override
            public void onSuccess(List<GeofenceEntity> geofenceEntities) {
                List<Geofence> m1geofencelist = new ArrayList<>();
                if(geofenceEntities != null){
                    for (GeofenceEntity geofence : geofenceEntities){

                        m1geofencelist.add(new Geofence.Builder()
                                .setRequestId("s1") // Geofence ID
                                .setCircularRegion(Double.parseDouble(geofence.getLatitude()), Double.parseDouble(geofence.getLongitude()), geofence.getRadius()) // defining fence region
                                .setExpirationDuration(100000) // expiring date
                                // Transition types that it should look for
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build());

                    }
                }


                addGeofence(m1geofencelist);

                }


            @Override
            public void onFailure(String error) {

            }
        });
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
        Log.d("LocationService","ontask geofence");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        Log.d(TAG,"on start location");
        return START_STICKY;
    }
