package org.codeforpakistan.rccibusinesslocator.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.activities.MapsActivity;
import org.codeforpakistan.rccibusinesslocator.utilities.PermissionsRequest;
import org.codeforpakistan.rccibusinesslocator.utilities.utils;
/**
 * A simple {@link Fragment} subclass.
 */
public class LocationEnablerFragment extends Fragment {


    public LocationEnablerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_enabler, container, false);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionsRequest.LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                if (utils.canGetLocation(getActivity()) == true) {
                    //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
                    if (utils.checkNetworkState(getActivity())) {
                        //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
                        Log.i("Service Binded = ", "1");

                    } else {
                        Toast.makeText(getActivity(), "You are not connected to Internet", Toast.LENGTH_LONG).show();
                    }
                    Log.i("actionLocation", "actionLocation");
//                        setCurrentCity();
                } else {
                    if (utils.checkNetworkState(getActivity())) {
                        //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
                        utils.showLocationSettingsAlert(((MapsActivity) getActivity()));
                    } else {
                        Toast.makeText(getActivity(), "You are not connected to Internet", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button locButton = view.findViewById(R.id.location);

        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), PermissionsRequest.LOCATION_PERMISSIONS, PermissionsRequest.LOCATION_REQUEST_CODE);
                    } else {
                        if (utils.canGetLocation(getActivity()) == true) {
                            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
                            if (utils.checkNetworkState(getActivity())) {
                                //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
                                Log.i("Service Binded = ", "1");

                            } else {
                                Toast.makeText(getActivity(), "You are not connected to Internet", Toast.LENGTH_LONG).show();
                            }
                            Log.i("actionLocation", "actionLocation");
//                        setCurrentCity();
                        } else {
                            if (utils.checkNetworkState(getActivity())) {
                                //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
                                utils.showLocationSettingsAlert(((MapsActivity) getActivity()));
                            } else {
                                Toast.makeText(getActivity(), "You are not connected to Internet", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    if (utils.canGetLocation(getActivity()) == true) {
                        //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
                        if (utils.checkNetworkState(getActivity())) {
                            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
                            Log.i("Service Binded = ", "2");

                        } else {
                            Toast.makeText(getActivity(), "You are not connected to Internet", Toast.LENGTH_LONG).show();
                        }
                        Log.i("actionLocation else", "actionLocation else");
//                        setCurrentCity();
                    } else {
                        if (utils.checkNetworkState(getActivity())) {
                            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
                            utils.showLocationSettingsAlert(((MapsActivity) getActivity()));
                        } else {
                            Toast.makeText(getActivity(), "You are not connected to Internet", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
        });

    }
}
