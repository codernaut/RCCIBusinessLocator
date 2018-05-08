package org.codeforpakistan.rccibusinesslocator.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;

import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.RcciApplication;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.ConnectivityReceiver;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.LocationProviderChangedReceiver;
import org.codeforpakistan.rccibusinesslocator.fragments.CompanyDetailsFragment;
import org.codeforpakistan.rccibusinesslocator.model.Companies;
import org.codeforpakistan.rccibusinesslocator.model.CompanyDetails;
import org.codeforpakistan.rccibusinesslocator.utilities.LocationSettings;
import org.codeforpakistan.rccibusinesslocator.utilities.PermissionsRequest;
import org.codeforpakistan.rccibusinesslocator.utilities.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

import static org.codeforpakistan.rccibusinesslocator.RcciApplication.firebaseDatabase;
import static org.codeforpakistan.rccibusinesslocator.model.Companies.fetchFirebaseData;
import static org.codeforpakistan.rccibusinesslocator.model.Companies.filterByCategory;
import static org.codeforpakistan.rccibusinesslocator.model.Companies.getAllCompanies;
import static org.codeforpakistan.rccibusinesslocator.utilities.LocationSettings.REQUEST_PERMISSION;
import static org.codeforpakistan.rccibusinesslocator.utilities.utils.getLocationFromAddress;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener, LocationProviderChangedReceiver.LocationReceiverListener, LocationSettings.LocationsSettingsListener {

    private static final String TAG = "Firebase";
    private static final int SELECTED_LOCATION = 1;
    TextView searchTV;
    ArrayAdapter<String> spinnerAdapter;
    LatLng current;
    int mSelectedIndex = 0;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private DatabaseReference myRef;
    private List<CompanyDetails> companiesList;
    private ConnectivityReceiver connectivityReceiver;
    private Float maxDistance = 2f;
    private Toolbar toolbar;
    private Spinner categorySpinner;
    private List<String> categoryList;
    CompanyDetailsFragment companyDetailsFragment;
    FragmentManager fragmentManager;
    DecimalFormat dtime = new DecimalFormat("#.######");

    LocationSettings mLocationSettings;
    OnMarkerSelectListener mOnMarkerSelectListener;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.CustomAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        maxDistance = maxDistance == 2f ? 5f : 2f;
        toolbar = findViewById(R.id.toolbar);
        searchTV = findViewById(R.id.search_TV);
        categorySpinner = findViewById(R.id.categorySpinner);
        categoryList = new ArrayList<>();
        companyDetailsFragment = new CompanyDetailsFragment();
        fragmentManager = getSupportFragmentManager();
        setSupportActionBar(toolbar);
        categoryList.add("All");
        companiesList = new ArrayList<>();

        initializeViews();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECTED_LOCATION:
                    String selectedAddress = data.getStringExtra("address");
                    String selectedName = data.getStringExtra("name");
                    searchTV.setText(selectedAddress);
                    showLocationAt(selectedAddress,selectedName);
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
                        mLocationSettings = new LocationSettings(this, this);
                    } else {

                    }
                } else {

                }
            }
        } else {
            Log.i("onPermissionsAllowed", "2");
        }

        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(connectivityReceiver);
        progressDialog.dismiss();
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

                        UpdateMarker();
                    }

                } else {
                    Log.i("onPermissionsAllowed", "4");
                }
                return;
            }
            case REQUEST_PERMISSION: {
                new LocationSettings(this, this);
                return;
            }
        }
    }

    private void initializeViews() {
        progressDialog = new ProgressDialog(MapsActivity.this, R.style.progressDialog);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Updating Locations");
        connectivityReceiver = new ConnectivityReceiver();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        myRef = firebaseDatabase.getReference();
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.locationDetails, companyDetailsFragment).hide(companyDetailsFragment).commit();
       List<CompanyDetails> mList = getAllCompanies(Realm.getDefaultInstance());

        if(mList!= null &&mList.size()>0){
            companiesList = mList;
            initSpinner();
        }
        else {
            new DownloadFiles().execute();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng islamabad = new LatLng(33.6844, 73.0479);
        mMap.setBuildingsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(islamabad));
        UpdateMarker();
        mMap.setOnMarkerClickListener(marker -> {
            mOnMarkerSelectListener = companyDetailsFragment;
            mOnMarkerSelectListener.OnMArkerSelecetd(marker.getTitle());

            fragmentManager.beginTransaction().show(companyDetailsFragment).commit();
            return true;
        });
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        if (isConnected) {
            if (utils.canGetLocation(MapsActivity.this)) {
                myRef.getDatabase();
            } else {
            }
        } else {
            Log.i("internet ", "discconnected");
        }
    }

    @Override
    public void onLocationEnabledStateChanged(boolean isConnected) {
        if (isConnected) {
            myRef.getDatabase();
            UpdateMarker();
        } else {
        }
    }

    public void UpdateMarker() {

        Log.i("RCCI_Debug","Update Marker Calling ");
        if (mMap != null && companiesList != null && companiesList.size() > 0) {
            mMap.clear();
            fragmentManager.beginTransaction().hide(companyDetailsFragment).commit();
            mMap.setBuildingsEnabled(true);
            if (mLocationSettings != null) {
                mLocationSettings.showLocation(true);
            }
        }
        else {
            Log.i("RCCI_Debug","Company List is still Empty ");
        }
    }

    private void showLocationAt(String Address, String companyName) {
        LatLng latLng = getLocationFromAddress(MapsActivity.this, Address);
        Location companyLocation = new Location("");
        if (latLng != null) {
            mMap.clear();
            companyLocation.setLatitude(utils.getDecimalValue(latLng.latitude));
            companyLocation.setLongitude(utils.getDecimalValue(latLng.longitude));
            mMap.addMarker(new MarkerOptions().position(latLng).title(companyName+"/"+Address));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
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
                        ? getAllCompanies(Realm.getDefaultInstance()) : filterByCategory(categorySpinner.getSelectedItem().toString(), Realm.getDefaultInstance());
                UpdateMarker();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        runOnUiThread(() -> categorySpinner.setAdapter(spinnerAdapter));
    }

    public void showRegisterURL(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.REGISTER_URL)));
        startActivity(browserIntent);
    }

    public void showRenewURL(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.RENEW_URL)));
        startActivity(browserIntent);
    }

    public void currentLocation(View view) {
        if (mLocationSettings != null) {
            mLocationSettings.showLocation(false);
        }
    }

    public void searchLocation(View view) {
        Intent intent = new Intent(MapsActivity.this, SearchLocationActivity.class);
        intent.putExtra("CATEGORY_NAME", categorySpinner.getSelectedItem().toString());
        startActivityForResult(intent, SELECTED_LOCATION);
    }

    public void showCategories(View view) {
        categorySpinner.performClick();
    }

    @Override
    public void OnLocationSettingRespponse(Location location) {
        Location currentLocation = new Location("");
        currentLocation.setLatitude(Float.parseFloat(dtime.format(location.getLatitude())));
        currentLocation.setLongitude(Float.parseFloat(dtime.format(location.getLongitude())));
        current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(current).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
    }

    @Override
    public void OnLocation(Location location) {

        for (CompanyDetails company : companiesList) {
            LatLng latLng = new LatLng(company.getLatitude(),company.getLongitude());
            Location companyLocation = new Location("");
            companyLocation.setLatitude(utils.getDecimalValue(latLng.latitude));
            companyLocation.setLongitude(utils.getDecimalValue(latLng.longitude));
            Float distanceInKiloMeters = (location.distanceTo(companyLocation)) / 1000; // as distance is in meter
            if (distanceInKiloMeters <= maxDistance) {
                Log.i("RCCI_Debug", "Location Found in 1 km radius");
                mMap.addMarker(new MarkerOptions().position(latLng).title(company.getCompanyName()+"/"+company.getAddress()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            } else {
                Log.i("RCCI_Debug", "Not in range");
            }
        }
        progressDialog.dismiss();

    }

    public interface OnMarkerSelectListener {
        void OnMArkerSelecetd(String companyName);
    }

    private class DownloadFiles extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {

            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            fetchFirebaseData(myRef, response -> {
                Log.i("ModelTest", response.toString());
                for (Companies companies : response) {
                    categoryList.add(companies.getCategory());
                    for (CompanyDetails companyDetails : companies.getComapniesList()) {
                        companiesList.add(companyDetails);
                    }
                }
                Log.i("RCCI_Debug","Data received ");
                initSpinner();
            }, error -> {
                Log.i("Response Error ",error.getMessage());
            });
            return null;
        }
    }
}
