package org.codeforpakistan.rccibusinesslocator.backgroundServices;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by AhmedAbbas on 12/11/2017.
 */

public class LocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static final public String SERVICE_NAME = "com.lmkt.weather.view.backgroundService.LocationService";
    static final public String MESSAGE = "location_updated";
    private static final long INTERVAL = 1000 * 3;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    private static final int DISPLACEMENT = 5;
    private static String LOG_TAG = "BoundService";
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    Location loc;
    long mLastLocationMillis;
    DecimalFormat dtime = new DecimalFormat("#.######");
    private IBinder mBinder = new MyBinder();
    private LocalBroadcastManager broadcaster;

    public void sendResult(String message) {
        Intent intent = new Intent(SERVICE_NAME);
        if (message != null)
            intent.putExtra(MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");
        broadcaster = LocalBroadcastManager.getInstance(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                createLocationRequest();
                //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            }
        } else {
            createLocationRequest();
            //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
//        createLocationRequest();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
        createLocationRequest();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        createLocationRequest();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                googleApiClient.unregisterConnectionCallbacks(this);
                googleApiClient.unregisterConnectionFailedListener(this);
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                LocationServices.FusedLocationApi.flushLocations(googleApiClient);
                googleApiClient.disconnect();
                locationRequest = null;
                googleApiClient = null;
            }
        } else {
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            LocationServices.FusedLocationApi.flushLocations(googleApiClient);
            googleApiClient.disconnect();
            locationRequest = null;
            googleApiClient = null;
        }
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, LocationService.this);
            }
        } else {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (googleApiClient.isConnected()) {
            Log.i("Google Client = ", "Connected............");
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Google Client = ", "Suspended............");
    }

    @Override
    public void onLocationChanged(Location location) {
        loc = location;
        mLastLocationMillis = SystemClock.elapsedRealtime();
        Log.d("location", loc.getProvider() + " + " + loc.getLatitude());
//        String toBeUpdateId = sP.getString("activeUserTrackId", "");

//        if (loc.getAccuracy() <= 30.0) {

        trackCurrentLocation(loc);
//        }
//        else{
//            Toast.makeText(this, "Location Accuracy > 30", Toast.LENGTH_SHORT).show();
//        }

    }

    private void trackCurrentLocation(Location loc) {
        //save coordinates in shared preferences
            SharedPreferences spref = getSharedPreferences("USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = spref.edit();
            editor.putFloat("lat", Float.parseFloat(dtime.format(loc.getLatitude())));
            editor.putFloat("long", Float.parseFloat(dtime.format(loc.getLongitude())));
            editor.putString("address", getCityName(loc));
            editor.commit();
            sendResult("1");
    }

    private String getCityName(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Log.i("geoCoder = ", "nulllll");
        if (geocoder != null) {
            Log.i("geoCoder = ", "not nulllll");
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(Double.parseDouble(dtime.format(loc.getLatitude())), Double.parseDouble(dtime.format(loc.getLongitude())), 1);
                String cityName = "";
//            cityName = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1);//addresses.get(0).getSubLocality()==null?addresses.get(0).getLocality():addresses.get(0).getSubLocality()+", "+addresses.get(0).getLocality();
                cityName = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality();//addresses.get(0).getSubLocality()==null?addresses.get(0).getLocality():addresses.get(0).getSubLocality()+", "+addresses.get(0).getLocality();
//            String stateName = addresses.get(0).getAddressLine(1);
//            String countryName = addresses.get(0).getAddressLine(2);
                Log.i("City Name = ", cityName + "*****************************");
                Log.i("Address = ", addresses.toString() + "*****************************");
//            Toast.makeText(this, "City Name : "+cityName, Toast.LENGTH_SHORT).show();
                return cityName;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public class MyBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }
}
