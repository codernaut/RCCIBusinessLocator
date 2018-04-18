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

/**
 * A simple {@link Fragment} subclass.
 */
public class InternetAlertDialogFragment extends DialogFragment {

    private static InternetAlertDialogFragment INSTANCE;
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
                    Settings.ACTION_WIFI_SETTINGS);
            getActivity().startActivity(
                    intent);
            dismissAllowingStateLoss();
        }
    };

    public InternetAlertDialogFragment() {
        // Required empty public constructor
    }

    public static InternetAlertDialogFragment getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InternetAlertDialogFragment();
        }
        return INSTANCE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_internet_alert_dialog, container, false);
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
