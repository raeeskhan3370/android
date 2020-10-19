package com.clubecerto.apps.app.classes;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Order extends RealmObject {

    /*expand list handling*/
    @Ignore
    public boolean expanded = false;
    @Ignore
    public boolean parent = false;
    @Ignore
    public boolean swiped = false; // flag when item swiped
    @PrimaryKey
    private int id;
    private int user_id;
    private String status;
    private String cart;
    private String req_cf_data;
    private String updated_at;
    private String created_at;
    private String payment_status;
    private RealmList<Product> items;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCart() {
        return cart;
    }

    public void setCart(String cart) {
        this.cart = cart;
    }

    public String getReq_cf_data() {
        return req_cf_data;
    }

    public void setReq_cf_data(String req_cf_data) {
        this.req_cf_data = req_cf_data;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public RealmList<Product> getItems() {
        return items;
    }

    public void setItems(RealmList<Product> items) {
        this.items = items;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }
}
