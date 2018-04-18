package org.codeforpakistan.rccibusinesslocator.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.codeforpakistan.rccibusinesslocator.App;
import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.backgroundServices.LocationService;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.ConnectivityReceiver;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.LocationProviderChangedReceiver;
import org.codeforpakistan.rccibusinesslocator.model.Company;
import org.codeforpakistan.rccibusinesslocator.utilities.PermissionsRequest;
import org.codeforpakistan.rccibusinesslocator.utilities.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener, LocationProviderChangedReceiver.LocationReceiverListener {

    private static final String TAG = "Firebase";
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private boolean mServiceBound;
    private LocationService mBoundService;
    private List<Company> companies;
    private ConnectivityReceiver connectivityReceiver;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.MyBinder myBinder = (LocationService.MyBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }
    };
    private Float maxDistance = 2f;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("INTENT RECIEVED", " = INTENT RECIEVED");
            if (intent.hasExtra(LocationService.MESSAGE)) {
                String s = intent.getStringExtra(LocationService.MESSAGE);
                if (s == "1") {
                    Log.i("setCurrentCity = ", "0");
                    if (utils.checkNetworkState(getApplicationContext()))
                        UpdateMarker();
                }
            }
            // do something here.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        initializeViews();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance().setLocationConnectivityListener(this);
        App.getInstance().setConnectivityListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this, PermissionsRequest.LOCATION_PERMISSIONS, PermissionsRequest.LOCATION_REQUEST_CODE);
            } else {
                if (utils.checkNetworkState(MapsActivity.this)) {
                    if (utils.canGetLocation(MapsActivity.this)) {
                        bindService();
                        UpdateMarker();
                    } else {
//                        utils.showLocationSettingsAlert(MapsActivity.this);
                    }
                } else {
//                    utils.showInternetSettingsAlert(MapsActivity.this);
                }
            }
        } else {
            Log.i("onPermissionsAllowed", "2");
//            setToolbarLocationIconEnabled(true);
        }

        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        unBindService();
        unregisterReceiver(connectivityReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionsRequest.LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, PermissionsRequest.LOCATION_FINE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, PermissionsRequest.LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:

                        Log.i("onPermissionsAllowed", "3");
                        bindService();
                        UpdateMarker();
                    }

                } else {

                    Log.i("onPermissionsAllowed", "4");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void setListeners() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                List<HashMap<String, String>> value = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                if (companies == null) {
                    companies = new ArrayList<>();
                } else {
                    companies.clear();
                }
                for (int i = 0; i < value.size(); i++) {
                    companies.add(new Company(value.get(i).get("Name"), value.get(i).get("Phone"), value.get(i).get("Address")));
                }
                UpdateMarker();
//                Map<String, HashMap> value = (Map) dataSnapshot.getValue();
/*
                if (mMap != null) {
                    LatLng addressLmkt = getLocationFromAddress(MapsActivity.this, ((Map<String, String>) value.get("1")).get("Address"));
                    mMap.addMarker(new MarkerOptions().position(addressLmkt).title(((Map<String, String>) value.get("1")).get("Name")));
                    mMap.setMinZoomPreference(14);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(addressLmkt));

                    LatLng addressCyberVision = getLocationFromAddress(MapsActivity.this, ((Map<String, String>) value.get("2")).get("Address"));
                    mMap.addMarker(new MarkerOptions().position(addressCyberVision).title(((Map<String, String>) value.get("2")).get("Name")));

                    LatLng addressWeCreate = getLocationFromAddress(MapsActivity.this, ((Map<String, String>) value.get("3")).get("Address"));
                    mMap.addMarker(new MarkerOptions().position(addressWeCreate).title(((Map<String, String>) value.get("3")).get("Name")));

                }*/
                Log.i(TAG, "Value is: " + value.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void initializeViews() {
        connectivityReceiver = new ConnectivityReceiver();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng islamabad = new LatLng(33.6844, 73.0479);
//        mMap.addMarker(new MarkerOptions().position(islamabad).title("Islamabad"));
        mMap.setBuildingsEnabled(true);
//        mMap.setMinZoomPreference(10);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(islamabad));

    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p1;

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        //network connection state change listener
        if (isConnected) {
            Log.i("internet ", "connected");
            if (utils.canGetLocation(MapsActivity.this)) {
                myRef.getDatabase();
                bindService();
                UpdateMarker();
            } else {
            }
        } else {
            Log.i("internet ", "discconnected");
        }
    }

    @Override
    public void onLocationEnabledStateChanged(boolean isConnected) {
        //location connection state change listener
        if (isConnected) {
            Log.i("location ", "enabled");
            myRef.getDatabase();
            bindService();
            UpdateMarker();
        } else {
            Log.i("location ", "disabled");
        }
    }

    public void UpdateMarker() {
        if (mMap != null && companies != null && companies.size() > 0) {
            mMap.clear();
            SharedPreferences spref = getSharedPreferences("USER", MODE_PRIVATE);
//            spref.getFloat("lat",0f);
//            spref.getFloat("long",0f);
//            spref.getString("address","");

            if (spref.contains("lat") && spref.contains("long")) {
                Location currentLocation = new Location("");
                currentLocation.setLatitude(spref.getFloat("lat", 0f));
                currentLocation.setLongitude(spref.getFloat("long", 0f));

                LatLng current = new LatLng(spref.getFloat("lat", 0f), spref.getFloat("long", 0f));
                mMap.addMarker(new MarkerOptions().position(current).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on)));
                mMap.setBuildingsEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 12f));

                for (Company company : companies) {
                    Location companyLocation = new Location("");
                    LatLng latLng = getLocationFromAddress(MapsActivity.this, company.getAddress());
                    if (latLng != null) {
                        companyLocation.setLatitude(utils.getDecimalValue(latLng.latitude));
                        companyLocation.setLongitude(utils.getDecimalValue(latLng.longitude));

                        Float distanceInKiloMeters = (currentLocation.distanceTo(companyLocation)) / 1000; // as distance is in meter
                        Log.i("Distance ", "= " + distanceInKiloMeters);
                        Log.i("lat1 ", "= " + spref.getFloat("lat", 0f));
                        Log.i("lat11 ", "= " + currentLocation.getLatitude());
                        Log.i("lat2 ", "= " + utils.getDecimalValue(latLng.latitude));
                        Log.i("long1 ", "= " + spref.getFloat("long", 0f));
                        Log.i("long11 ", "= " + currentLocation.getLongitude());
                        Log.i("long2 ", "= " + utils.getDecimalValue(latLng.longitude));
                        if (distanceInKiloMeters <= maxDistance) {
                            // It is in range of 1 km
                            mMap.addMarker(new MarkerOptions().position(latLng).title(company.getAddress()));
                        } else {
                            //not in range of 1 km
                        }
                    }
                }
            }
        }
    }

    public boolean bindService() {
        if (!mServiceBound) {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                    new IntentFilter(LocationService.SERVICE_NAME)
            );
            Log.i("Service Binded", " true");
            return true;
        } else {
            Log.i("Service Binded", " already");
            return false;
        }
    }

    public void unBindService() {
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
            mBoundService.stopSelf();
            mServiceConnection = null;
            mServiceBound = false;
            Log.i("Service UnBinded", " true");
        } else {
            Log.i("Service UnBinded", " already");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_search) {
            return true;
        }
        if (id == R.id.action_location) {
            maxDistance = maxDistance == 2f ? 5f : 2f;
            UpdateMarker();
            Toast.makeText(MapsActivity.this, "Location Radius = " + maxDistance + " KM", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
