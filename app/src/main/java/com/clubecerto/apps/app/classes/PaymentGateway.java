package com.clubecerto.apps.app.classes;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PaymentGateway extends RealmObject {

    @PrimaryKey
    private int id;
    private String code;
    private String payment;
    private String image;
    private String description;
    private RealmList<Fee> fees;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getImages() {
        return image;
    }

    public void setImages(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RealmList<Fee> getFees() {
        return fees;
    }

    public void setFees(RealmList<Fee> fees) {
        this.fees = fees;
    }
}
