package org.codeforpakistan.rccibusinesslocator.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.model.CompanyDetails;

import java.util.List;

import io.realm.OrderedRealmCollection;


/**
 * Created by shahzaibshahid on 11/01/2018.
 */

public class AdapterSearchCompanies extends RecyclerView.Adapter<AdapterSearchCompanies.MyViewHolder> {
    List<CompanyDetails> companiesList;
    Context mContext;
    private LayoutInflater inflater;
    LocationClickListener mListener;


    public AdapterSearchCompanies(List<CompanyDetails> locationList, Context mContext, LocationClickListener mListener) {
        this.companiesList = locationList;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        this.mListener = mListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.company_list_layout, parent, false);

        return new AdapterSearchCompanies.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final CompanyDetails mCompanyDetail = companiesList.get(position);
        holder.locationName.setText(mCompanyDetail.getCompanyName());
        holder.locationAddress.setText(mCompanyDetail.getAddress());
        holder.container.setOnClickListener(view -> mListener.OnLocationSelect(mCompanyDetail.getCompanyName(), mCompanyDetail.getAddress()));
    }

    @Override
    public int getItemCount() {
        return companiesList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView locationName;
        TextView locationAddress;
        ConstraintLayout container;

        public MyViewHolder(View view) {
            super(view);
            locationName = view.findViewById(R.id.companyName);
            locationAddress = view.findViewById(R.id.companyAddress);
            container = view.findViewById(R.id.container);
        }
    }

    public interface LocationClickListener {
         void OnLocationSelect(String locationName, String locationAddress);
    }
}
