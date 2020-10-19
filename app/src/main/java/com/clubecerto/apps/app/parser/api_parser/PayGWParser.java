package com.clubecerto.apps.app.parser.api_parser;


import com.clubecerto.apps.app.classes.PaymentGateway;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class PayGWParser extends Parser {

    public PayGWParser(JSONObject json) {
        super(json);
    }

    public RealmList<PaymentGateway> getPaymentGetway() {

        RealmList<PaymentGateway> list = new RealmList<PaymentGateway>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {


                try {
                    JSONObject json_payment = json_array.getJSONObject(i + "");
                    PaymentGateway mPaymentGateway = new PaymentGateway();
                    mPaymentGateway.setId(json_payment.getInt("id"));
                    mPaymentGateway.setCode(json_payment.getString("code"));
                    mPaymentGateway.setImages(json_payment.getString("image"));
                    mPaymentGateway.setDescription(json_payment.getString("description"));
                    mPaymentGateway.setPayment(json_payment.getString("payment"));

                    if (!json_payment.isNull("taxes")) {
                        FeeParser items = new FeeParser(json_payment);

                        if (items.getFees().size() > 0) {
                            mPaymentGateway.setFees(items.getFees());
                        }
                    }


                    list.add(mPaymentGateway);
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
