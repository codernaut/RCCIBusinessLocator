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
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import org.codeforpakistan.rccibusinesslocator.CustomCallBack;
import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.RcciApplication;
import org.codeforpakistan.rccibusinesslocator.backgroundServices.LocationService;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.ConnectivityReceiver;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.LocationProviderChangedReceiver;
import org.codeforpakistan.rccibusinesslocator.model.Companies;
import org.codeforpakistan.rccibusinesslocator.model.CompanyDetails;
import org.codeforpakistan.rccibusinesslocator.utilities.PermissionsRequest;
import org.codeforpakistan.rccibusinesslocator.utilities.utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

import static org.codeforpakistan.rccibusinesslocator.RcciApplication.firebaseDatabase;
import static org.codeforpakistan.rccibusinesslocator.model.Companies.*;
import static org.codeforpakistan.rccibusinesslocator.utilities.utils.*;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener, LocationProviderChangedReceiver.LocationReceiverListener {

    private static final String TAG = "Firebase";
    private static final int SELECTED_LOCATION = 1;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private DatabaseReference myRef;
    private boolean mServiceBound;
    private LocationService mBoundService;
    private List<CompanyDetails> companiesList;
    private ConnectivityReceiver connectivityReceiver;
    private Float maxDistance = 2f;

    private Toolbar toolbar;
    TextView searchTV;
    private FloatingActionButton currentLocation_FAB;
    private Spinner categorySpinner;
    ArrayAdapter<String> spinnerAdapter;
    private List<String> categoryList;

    LatLng current;
    int mSelectedIndex = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.CustomAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        maxDistance = maxDistance == 2f ? 20f : 2f;

        toolbar = findViewById(R.id.toolbar);
        searchTV = findViewById(R.id.search_TV);
        categorySpinner = findViewById(R.id.categorySpinner);
        currentLocation_FAB = findViewById(R.id.currentLocation_fab);
        categoryList = new ArrayList<>();
        setSupportActionBar(toolbar);
        categoryList.add("All");
        companiesList = new ArrayList<>();

        currentLocation_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap!=null && current!=null ){
                    mMap.addMarker(new MarkerOptions().position(current).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current,15));
                }
            }
        });

        searchTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this,SearchLocationActivity.class);
                intent.putExtra("CATEGORY_NAME", categorySpinner.getSelectedItem().toString());
                startActivityForResult(intent,SELECTED_LOCATION);
            }
        });

        initializeViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case SELECTED_LOCATION:
                    String selectedAddress = data.getStringExtra("address");
                    searchTV.setText(selectedAddress);
                    showLocationAt(selectedAddress);
                    break;
                    default:
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RcciApplication.getInstance().setLocationConnectivityListener(this);
        RcciApplication.getInstance().setConnectivityListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this, PermissionsRequest.LOCATION_PERMISSIONS, PermissionsRequest.LOCATION_REQUEST_CODE);
            } else {
                if (utils.checkNetworkState(MapsActivity.this)) {
                    if (utils.canGetLocation(MapsActivity.this)) {
                        bindService();
                        //UpdateMarker();
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
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, PermissionsRequest.LOCATION_FINE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, PermissionsRequest.LOCATION_COARSE) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("onPermissionsAllowed", "3");
                        bindService();
                        UpdateMarker();
                    }

                } else {
                    Log.i("onPermissionsAllowed", "4");
                }
                return;
            }
        }
    }

    private void initializeViews() {
        connectivityReceiver = new ConnectivityReceiver();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        myRef = firebaseDatabase.getReference();
        fetchFirebaseData(myRef, new CustomCallBack.Listener<RealmResults<Companies>>() {
            @Override
            public void onResponse(RealmResults<Companies> response) {
                Log.i("ModelTest", response.toString());
                for (Companies companies : response) {
                    categoryList.add(companies.getCategory());
                    for (CompanyDetails companyDetails : companies.getComapniesList()) {
                        companiesList.add(companyDetails);
                    }
                    initSpinner();
                }

            }
        }, new CustomCallBack.ErrorListener<DatabaseError>() {
            @Override
            public void onErrorResponse(DatabaseError error) {

            }
        });
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

        LatLng islamabad = new LatLng(33.6844, 73.0479);
        mMap.setBuildingsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(islamabad));
        UpdateMarker();
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        //network connection state change listener
        if (isConnected) {
            Log.i("internet ", "connected");
            if (utils.canGetLocation(MapsActivity.this)) {
                myRef.getDatabase();
                bindService();
               // UpdateMarker();
            } else {
            }
        } else {
            Log.i("internet ", "discconnected");
        }
    }

    @Override
    public void onLocationEnabledStateChanged(boolean isConnected) {
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
        if (mMap != null && companiesList != null && companiesList.size() > 0) {
            mMap.clear();
            SharedPreferences spref = getSharedPreferences("USER", MODE_PRIVATE);

            if (spref.contains("lat") && spref.contains("long")) {
                Location currentLocation = new Location("");
                currentLocation.setLatitude(spref.getFloat("lat", 0f));
                currentLocation.setLongitude(spref.getFloat("long", 0f));

                current = new LatLng(spref.getFloat("lat", 0f), spref.getFloat("long", 0f));
                mMap.addMarker(new MarkerOptions().position(current).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on)));
                mMap.setBuildingsEnabled(true);
                for (CompanyDetails company : companiesList) {
                    Location companyLocation = new Location("");
                    LatLng latLng = getLocationFromAddress(MapsActivity.this, company.getAddress());
                    if (latLng != null) {
                        companyLocation.setLatitude(utils.getDecimalValue(latLng.latitude));
                        companyLocation.setLongitude(utils.getDecimalValue(latLng.longitude));
                        Float distanceInKiloMeters = (currentLocation.distanceTo(companyLocation)) / 1000; // as distance is in meter
                        if (distanceInKiloMeters <= maxDistance) {
                            mMap.addMarker(new MarkerOptions().position(latLng).title(company.getAddress()));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
                        } else {

                            Log.i("Distance ", "Not in range");
                        }
                    }
                }
            }
        }
    }

    private void showLocationAt(String Address){
        LatLng latLng = getLocationFromAddress(MapsActivity.this, Address);
        Location companyLocation = new Location("");
        if (latLng != null) {
            mMap.clear();
            companyLocation.setLatitude(utils.getDecimalValue(latLng.latitude));
            companyLocation.setLongitude(utils.getDecimalValue(latLng.longitude));
            mMap.addMarker(new MarkerOptions().position(latLng).title(Address));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
        }
    }
    private void initSpinner() {
        spinnerAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, categoryList) {
            public View getView(int position, View convertView, ViewGroup parent) {

                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);

                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                return tv;
            }
        };

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedIndex = i;
                companiesList.clear();
                companiesList = categorySpinner.getSelectedItem().equals("All")
                ? getAllCompanies(RcciApplication.realm) : filterByCategory(categorySpinner.getSelectedItem().toString(),RcciApplication.realm);
                UpdateMarker();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
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
        }
    };
}
