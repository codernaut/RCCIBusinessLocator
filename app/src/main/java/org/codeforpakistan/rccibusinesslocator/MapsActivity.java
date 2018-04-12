package org.codeforpakistan.rccibusinesslocator;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Firebase";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Map<String, HashMap> value = (Map) dataSnapshot.getValue();

                if (mMap != null) {

                    LatLng addressLmkt = getLocationFromAddress(MapsActivity.this, ((Map<String, String>) value.get("Company A")).get("Address"));
                    mMap.addMarker(new MarkerOptions().position(addressLmkt).title(((Map<String, String>) value.get("Company A")).get("name")));
                    mMap.setMinZoomPreference(14);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(addressLmkt));

                    LatLng addressCyberVision = getLocationFromAddress(MapsActivity.this, ((Map<String, String>) value.get("Company B")).get("Address"));
                    mMap.addMarker(new MarkerOptions().position(addressCyberVision).title(((Map<String, String>) value.get("Company B")).get("name")));

                    LatLng addressWeCreate = getLocationFromAddress(MapsActivity.this, ((Map<String, String>) value.get("Company C")).get("Address"));
                    mMap.addMarker(new MarkerOptions().position(addressWeCreate).title(((Map<String, String>) value.get("Company C")).get("name")));

                }
                Log.i(TAG, "Value is: " + value.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
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

        // Add a marker in Sydney and move the camera
        LatLng islamabad = new LatLng(33.6844, 73.0479);
//        mMap.addMarker(new MarkerOptions().position(islamabad).title("Islamabad"));
        mMap.setBuildingsEnabled(true);
        mMap.setMinZoomPreference(10);
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
}
