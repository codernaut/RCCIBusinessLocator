package org.codeforpakistan.rccibusinesslocator.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import io.realm.Realm;
import io.realm.RealmObject;


@IgnoreExtraProperties
public class CompanyDetails extends RealmObject {

    @PropertyName("Name")
    private String name;

    @PropertyName("Phone")
    private String phone;

    @PropertyName("Address")
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static CompanyDetails getInstance(Realm realm, final CompanyDetails obj) {
        final CompanyDetails[] companyDetails = new CompanyDetails[1];
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                companyDetails[0] = realm.createObject(CompanyDetails.class);
                companyDetails[0].setPhone(obj.phone);
                companyDetails[0].setAddress(obj.address);
                companyDetails[0].setName(obj.name);
            }
        });
        return companyDetails[0];
    }
}
