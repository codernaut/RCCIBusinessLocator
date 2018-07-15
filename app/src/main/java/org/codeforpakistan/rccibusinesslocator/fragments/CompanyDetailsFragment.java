package org.codeforpakistan.rccibusinesslocator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.activities.MapsActivity;

public class CompanyDetailsFragment extends Fragment implements MapsActivity.OnMarkerSelectListener {

    TextView companyNameTV;
    TextView companyAddressTV;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_details, container, false);
        companyNameTV = rootView.findViewById(R.id.companyName);
        companyAddressTV = rootView.findViewById(R.id.address);



        return rootView;
    }

    @Override
    public void OnMArkerSelecetd(String companyName) {
        String popUpDetails[] = companyName.split("/");
        companyAddressTV.setText(popUpDetails[1]);
        companyNameTV.setText(popUpDetails[0]);
    }
}
