package org.codeforpakistan.rccibusinesslocator;

import android.app.Application;

import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.ConnectivityReceiver;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.LocationProviderChangedReceiver;

public class App extends Application {
    private static App INSTANCE;
    public static App getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setLocationConnectivityListener(LocationProviderChangedReceiver.LocationReceiverListener listener) {
        LocationProviderChangedReceiver.locationReceiverListener = listener;
    }
}
