package com.clubecerto.apps.app.classes;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Module extends RealmObject {

    @PrimaryKey
    private String name;
    private int enabled;


    public int isEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
