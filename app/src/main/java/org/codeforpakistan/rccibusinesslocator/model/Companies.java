package org.codeforpakistan.rccibusinesslocator.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.codeforpakistan.rccibusinesslocator.CustomCallBack;
import org.codeforpakistan.rccibusinesslocator.RcciApplication;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

import static org.codeforpakistan.rccibusinesslocator.RcciApplication.realm;

public class Companies extends RealmObject {

    @PrimaryKey
    private String Category;

    private RealmList<CompanyDetails> companiesList;

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public RealmList<CompanyDetails> getComapniesList() {
        return companiesList;
    }

    public void setComapniesList(RealmList<CompanyDetails> comapniesList) {
        this.companiesList = comapniesList;
    }


    public static void fetchFirebaseData(DatabaseReference databaseReference, final CustomCallBack.Listener<RealmResults<Companies>> realmResult, final CustomCallBack.ErrorListener<DatabaseError> Error) {

        final RealmList<CompanyDetails> list = new RealmList<>();
        RealmResults<Companies> companies = RcciApplication.realm.where(Companies.class).findAll();
        if (companies.size() > 0) {
            realmResult.onResponse(companies);
        } else {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Companies companiesRealmObject = getInstance(realm, snapshot.getKey());

                        for (DataSnapshot companyDetails : snapshot.getChildren()) {
                            CompanyDetails obj = CompanyDetails.getInstance(realm, companyDetails.getValue(CompanyDetails.class));

                            realm.beginTransaction();
                            companiesRealmObject.companiesList.add(obj);
                            realm.commitTransaction();
                        }
                    }
                    realmResult.onResponse(RcciApplication.realm.where(Companies.class).findAll());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Error.onErrorResponse(databaseError);
                }
            });
        }
    }

    public static Companies getInstance(Realm realm, final String key) {
        final Companies[] companies = new Companies[1];
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                companies[0] = realm.createObject(Companies.class, key);
            }
        });
        return companies[0];
    }

    public static List<CompanyDetails> filterByCategory(String category, Realm realm) {
        Companies companies=  realm.where(Companies.class).equalTo("Category", category).findFirst();
        List<CompanyDetails> companyList = new ArrayList<>();
        for (CompanyDetails list : companies.getComapniesList()) {
            companyList.add(list);
        }

        return companyList.size()>0 ? companyList : null;
    }

    public  static List<CompanyDetails> getAllCompanies(Realm realm){
        RealmResults<Companies> realmResults = realm.where(Companies.class).findAll();
        List<CompanyDetails> list = new ArrayList<>();
        for (Companies companies: realmResults){
            for (CompanyDetails companyDetails: companies.getComapniesList()){
                list.add(companyDetails);
            }
        }
        return list.size()>0 ? list : null;
    }

    public  static List<CompanyDetails> searchCompaniesNameByCategory(String category,Realm realm,String query){
        Companies companies =  realm.where(Companies.class).equalTo("Category", category).findFirst();
        RealmList<CompanyDetails> realmList = companies.getComapniesList();
        RealmResults<CompanyDetails> results = realmList.where().contains("name",query, Case.INSENSITIVE).findAll();
        if(results.size()==0){

            return  realmList.where().contains("address",query, Case.INSENSITIVE).findAll();
        }
        else {
            return results;
        }
    }
    public static  List<CompanyDetails> searchCompanies(Realm realm,String query){
        RealmResults<Companies> realmResults = realm.where(Companies.class).findAll();
        List<CompanyDetails> list = new ArrayList<>();
        for (Companies companies: realmResults){
            RealmList<CompanyDetails> realmList = companies.getComapniesList();
            RealmResults<CompanyDetails> searchResultByName = realmList.where().contains("name",query, Case.INSENSITIVE).findAll();
            if(searchResultByName.size()==0){
                RealmResults<CompanyDetails> searchResultByAddress = realmList.where().contains("name",query, Case.INSENSITIVE).findAll();
                for(CompanyDetails companyDetails: searchResultByAddress){
                    list.add(companyDetails);
                }
            }
            else {
                for(CompanyDetails companyDetails: searchResultByName){
                    list.add(companyDetails);
                }
            }

        }
        return  list;
    }

    public static void getCategories(CustomCallBack.Listener<ArrayList<String>> categoryList) {
        ArrayList<String> list = new ArrayList<>();
        RealmResults<Companies> categories = realm.where(Companies.class).findAll();
        //Todo: fetch only Categories


        categoryList.onResponse(list);
    }
}

