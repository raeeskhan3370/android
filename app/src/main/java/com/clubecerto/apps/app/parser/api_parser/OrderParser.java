package com.clubecerto.apps.app.parser.api_parser;


import com.clubecerto.apps.app.classes.Order;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class OrderParser extends Parser {

    public OrderParser(JSONObject json) {
        super(json);
    }

    public RealmList<Order> getOrders() {

        RealmList<Order> list = new RealmList<Order>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {


                try {
                    JSONObject json_order = json_array.getJSONObject(i + "");
                    Order order = new Order();
                    order.setId(json_order.getInt("id"));
                    order.setStatus(json_order.getString("status"));
                    order.setPayment_status(json_order.getString("payment_status_data"));
                    order.setUser_id(json_order.getInt("user_id"));
                    order.setCart(json_order.getString("cart"));
                    order.setReq_cf_data(json_order.getString("req_cf_data"));
                    order.setUpdated_at(json_order.getString("updated_at"));
                    order.setCreated_at(json_order.getString("created_at"));

                    if (!json_order.isNull("items")) {
                        ProductParser items = new ProductParser(json_order);

                        if (items.getItems().size() > 0) {
                            order.setItems(items.getItems());
                        }
                    }

                    list.add(order);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return list;
    }


}
