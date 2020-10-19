package com.clubecerto.apps.app.parser.api_parser;


import com.clubecerto.apps.app.classes.Product;
import com.clubecerto.apps.app.parser.Parser;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class ProductParser extends Parser {

    public ProductParser(JSONObject json) {
        super(json);
    }

    public RealmList<Product> getItems() {

        RealmList<Product> list = new RealmList<Product>();

        try {

            JSONObject json_array = json.getJSONObject("items");
            //JSONArray json_array = new JSONArray(json.getString("items"));


            for (int i = 0; i < json_array.length(); i++) {


                try {
                    JSONObject json_user = json_array.getJSONObject(i + "");
                    Product product = new Product();
                    product.setId(json_user.getInt("id"));
                    product.setName(json_user.getString("name"));
                    product.setModule(json_user.getString("module"));
                    product.setAmount(json_user.getDouble("amount"));
                    product.setImage(json_user.getString("image"));
                    product.setQty(json_user.getInt("qty"));

                    if (json_user.has("currency") && !json_user.isNull("currency")) {
                        OfferCurrencyParser mOfferCurrencyParser = new OfferCurrencyParser(new JSONObject(
                                json_user.getString("currency")
                        ));
                        product.setCurrency(mOfferCurrencyParser.getCurrency());

                    }
                    list.add(product);
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
