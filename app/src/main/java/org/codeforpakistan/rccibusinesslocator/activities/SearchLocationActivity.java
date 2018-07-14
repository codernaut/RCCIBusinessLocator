package org.codeforpakistan.rccibusinesslocator.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;


import com.claudiodegio.msv.MaterialSearchView;
import com.claudiodegio.msv.OnSearchViewListener;

import org.codeforpakistan.rccibusinesslocator.R;
import org.codeforpakistan.rccibusinesslocator.RcciApplication;
import org.codeforpakistan.rccibusinesslocator.adapters.AdapterSearchCompanies;
import org.codeforpakistan.rccibusinesslocator.model.Companies;
import org.codeforpakistan.rccibusinesslocator.model.CompanyDetails;

import io.realm.Realm;
import io.realm.RealmResults;

public class SearchLocationActivity extends AppCompatActivity implements OnSearchViewListener , AdapterSearchCompanies.LocationClickListener {

    MaterialSearchView searchView;
    Toolbar toolbar;
    String selectedCategory;
    RecyclerView recyclerView;
    AdapterSearchCompanies mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.CustomAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        searchView = findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(this);
        getSupportActionBar().hide();
        recyclerView = findViewById(R.id.companiesList_RV);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        if( getIntent()!=null) {
           selectedCategory = getIntent().getStringExtra("CATEGORY_NAME");
           if(selectedCategory.equals("All")){
               mAdapter = new AdapterSearchCompanies(Companies.getAllCompanies(Realm.getDefaultInstance()), SearchLocationActivity.this, SearchLocationActivity.this);
           }else {
               mAdapter = new AdapterSearchCompanies(Companies.filterByCategory(selectedCategory,Realm.getDefaultInstance()), SearchLocationActivity.this, SearchLocationActivity.this);
           }
            recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        searchView.showSearch(true);
        return true;
    }

    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public void onQueryTextChange(String s) {
        if(selectedCategory.equals("All")){
            mAdapter = new AdapterSearchCompanies(Companies.searchCompanies(Realm.getDefaultInstance(),s), SearchLocationActivity.this, SearchLocationActivity.this);
            recyclerView.setAdapter(mAdapter);
        }else {
            mAdapter = new AdapterSearchCompanies(Companies.searchCompaniesNameByCategory(selectedCategory,Realm.getDefaultInstance(),s), SearchLocationActivity.this, SearchLocationActivity.this);
            recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void OnLocationSelect(String locationName, String locationAddress) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("name",locationName);
        returnIntent.putExtra("address",locationAddress);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
