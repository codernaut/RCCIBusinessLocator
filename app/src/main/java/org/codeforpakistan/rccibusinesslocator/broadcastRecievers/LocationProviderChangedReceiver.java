package org.codeforpakistan.rccibusinesslocator.broadcastRecievers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import org.codeforpakistan.rccibusinesslocator.App;


/**
 * Created by AhmedAbbas on 12/26/2017.
 */

public class LocationProviderChangedReceiver extends BroadcastReceiver {
    public static LocationReceiverListener locationReceiverListener;
    public static App locationReceiverListenerWidget;

    @Override
    public void onReceive(Context context, Intent intent) {
        ContentResolver contentResolver = context.getContentResolver();
        // Find out what the settings say about which providers are enabled
        int mode = Settings.Secure.getInt(
                contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

        if(locationReceiverListener!=null) {
            if (checkLocationState(context)) {
                // Location is turned OFF!
                locationReceiverListener.onLocationEnabledStateChanged(true);
            } else {
                // Location is turned ON!
                locationReceiverListener.onLocationEnabledStateChanged(false);
            }
        }
    }

    public interface LocationReceiverListener{
        void onLocationEnabledStateChanged(boolean isConnected);
    }
    public boolean checkLocationState(Context context)
    {
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
}
