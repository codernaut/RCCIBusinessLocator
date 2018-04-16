package org.codeforpakistan.rccibusinesslocator.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.activities.MapsActivity;
import static org.codeforpakistan.rccibusinesslocator.utilities.PermissionsRequest.LOACTION_ENABLE_REQUEST;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationAlertDialogFragment extends DialogFragment {

    private static LocationAlertDialogFragment INSTANCE;
    private View.OnClickListener OnCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismissAllowingStateLoss();
        }
    };
    private View.OnClickListener OnEnableClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            ((MapsActivity)getActivity()).startActivityForResult(intent,LOACTION_ENABLE_REQUEST);
            dismissAllowingStateLoss();
        }
    };
    public LocationAlertDialogFragment() {
        // Required empty public constructor
    }

    public static  LocationAlertDialogFragment getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocationAlertDialogFragment();
        }
        return INSTANCE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_alert_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button cancel = view.findViewById(R.id.cancel);
        Button enable = view.findViewById(R.id.enable);

        cancel.setOnClickListener(OnCancelClickListener);
        enable.setOnClickListener(OnEnableClickListener);
    }
}
