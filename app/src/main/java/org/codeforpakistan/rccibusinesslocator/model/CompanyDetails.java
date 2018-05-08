package org.codeforpakistan.rccibusinesslocator.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import org.codeforpakistan.rccibusinesslocator.RcciApplication;

import io.realm.Realm;
import io.realm.RealmObject;

import static org.codeforpakistan.rccibusinesslocator.RcciApplication.config;


@IgnoreExtraProperties
public class CompanyDetails extends RealmObject {

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String companyName) {
        CompanyName = companyName;
    }

    @PropertyName("CompanyName")
    private String CompanyName;

    @PropertyName("EMAIL")
    private String email;

    @PropertyName("Address")
    private String address;

    @PropertyName("Latitude")
    private  double latitude;

    @PropertyName("Longitude")
    private double longitude;
    public double getLatitude() {

        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static CompanyDetails getInstance(CompanyDetails obj) {

        final CompanyDetails[] companyDetails = new CompanyDetails[1];

            companyDetails[0] = Realm.getDefaultInstance().createObject(CompanyDetails.class);
            companyDetails[0].setEmail(obj.email);
            companyDetails[0].setAddress(obj.address);
            companyDetails[0].setCompanyName(obj.CompanyName);
            companyDetails[0].setLatitude(obj.latitude);
            companyDetails[0].setLongitude(obj.longitude);
        return companyDetails[0];
    }
}
