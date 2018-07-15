package org.codeforpakistan.rccibusinesslocator.model;

import io.realm.RealmObject;

/**
 * Created by shahzaib on 11/11/2017.
 */

public class RealmString extends RealmObject {


    private String stringValue;

    public String getStringvalue() {
        return stringValue;
    }

    public void setStringvalue(String stringValue) {
        this.stringValue = stringValue;
    }

}
