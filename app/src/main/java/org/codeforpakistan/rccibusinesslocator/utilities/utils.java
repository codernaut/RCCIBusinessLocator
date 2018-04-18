package org.codeforpakistan.rccibusinesslocator.utilities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.dialog.LocationAlertDialogFragment;

import java.text.DecimalFormat;
import java.util.List;


/**
 * Created by AhmedAbbas on 11/28/2017.
 */

public class utils {
    private static DecimalFormat dtime = new DecimalFormat("#.######");

    public static Float getDecimalValue(Float value)
    {
        return Float.parseFloat(dtime.format(value));
    }
    public static Float getDecimalValue(Double value)
    {
        return Float.parseFloat(dtime.format(value));
    }
    public static boolean checkNetworkState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean checkLocationState(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        // Find out what the settings say about which providers are enabled
        int mode = Settings.Secure.getInt(
                contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

        if (mode == Settings.Secure.LOCATION_MODE_OFF) {
            // Location is turned OFF!
            return false;
        } else {
            // Location is turned ON!
            return true;
        }
    }

    public static boolean canGetLocation(Context context) {
        boolean result = true;
        LocationManager lm = null;
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        if (lm == null)

            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            networkEnabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gpsEnabled == false || networkEnabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public static LatLng getLocationFromAddress(Context context, String strAddress) {
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

    public static void showLocationSettingsAlert(final AppCompatActivity context) {
        LocationAlertDialogFragment fragment = LocationAlertDialogFragment.getInstance();
        fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_NoTitle);
        fragment.show(context.getSupportFragmentManager(), "Location Enabler Fragment");
    }

    public static void showInternetSettingsAlert(final AppCompatActivity context) {
        LocationAlertDialogFragment fragment = LocationAlertDialogFragment.getInstance();
        fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_NoTitle);
        fragment.show(context.getSupportFragmentManager(), "Wifi Enabler Fragment");
    }

    private static void makeAlertDialog(String info, String s, final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialogBuilder.setTitle(info);

        // Setting Dialog Message
        alertDialogBuilder.setMessage(s);
        AlertDialog alertDialog = alertDialogBuilder.create();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // do work
                        Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                        context.startActivity(intent);
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        // do work
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // do work
                        break;
                    default:
                        break;
                }
            }
        };
        // On pressing Ok button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(
                        Settings.ACTION_DATE_SETTINGS);
                context.startActivity(intent);
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", listener);
//        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", listener);
//        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel",

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                Button button = alertDialog.getButton(dialog.BUTTON_POSITIVE);
                button.setTextColor(context.getResources().getColor(R.color.black));
            }
        });


        alertDialog.show();
    }


    public static void showSettingsAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Please ");

        // On pressing Settings button
        alertDialog.setPositiveButton(
                context.getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });

        alertDialog.show();
    }

}
