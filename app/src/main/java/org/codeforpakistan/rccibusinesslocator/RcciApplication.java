package org.codeforpakistan.rccibusinesslocator;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.ConnectivityReceiver;
import org.codeforpakistan.rccibusinesslocator.broadcastRecievers.LocationProviderChangedReceiver;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RcciApplication extends Application {
    private static RcciApplication INSTANCE;
    public static Realm realm;
    public static RealmConfiguration config;
    public static FirebaseDatabase firebaseDatabase;
    public static RcciApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        firebaseDatabase = FirebaseDatabase.getInstance();

        Realm.init(this);
        config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(R.string.REALM_SCHEMA_VERSION).build();
        realm = Realm.getInstance(config);
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setLocationConnectivityListener(LocationProviderChangedReceiver.LocationReceiverListener listener) {
        LocationProviderChangedReceiver.locationReceiverListener = listener;
    }

}
