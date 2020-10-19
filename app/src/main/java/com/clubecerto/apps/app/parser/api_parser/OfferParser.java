package com.clubecerto.apps.app.parser.api_parser;


import com.clubecerto.apps.app.classes.Images;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class OfferParser extends Parser {

    public OfferParser(JSONObject json) {
        super(json);
    }

    public RealmList<Offer> getOffers() {

        RealmList<Offer> list = new RealmList<Offer>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {

                try {
                    JSONObject json_offer = json_array.getJSONObject(i + "");
                    Offer offer = new Offer();

                    offer.setId(json_offer.getInt("id_offer"));
                    offer.setName(json_offer.getString("name"));
                    offer.setDate_end(json_offer.getString("date_end"));
                    offer.setDate_start(json_offer.getString("date_start"));
                    offer.setStatus(json_offer.getInt("status"));
                    offer.setStore_id(json_offer.getInt("store_id"));
                    offer.setStore_name(json_offer.getString("store_name"));
                    offer.setDistance(json_offer.getDouble("distance"));
                    offer.setDescription(json_offer.getString("description"));
                    offer.setValue_type(json_offer.getString("value_type"));
                    offer.setDescription(json_offer.getString("description"));
                    offer.setShort_description(json_offer.getString("short_description"));
                    offer.setOffer_value((float) json_offer.getDouble("offer_value"));
                    offer.setLat(json_offer.getDouble("latitude"));
                    offer.setLng(json_offer.getDouble("longitude"));
                    offer.setLink(json_offer.getString("link"));

                    if (json_offer.has("featured") && !json_offer.isNull("featured")) {
                        offer.setFeatured(json_offer.getInt("featured"));
                    }

                    if (json_offer.has("is_deal") && !json_offer.isNull("is_deal"))
                        offer.setIs_deal(json_offer.getInt("is_deal"));

                    if (json_offer.has("cf_id") && !json_offer.isNull("cf_id"))
                        offer.setCf_id(json_offer.getInt("cf_id"));

                    if (json_offer.has("order_enabled") && !json_offer.isNull("order_enabled"))
                        offer.setOrder_enabled(json_offer.getInt("order_enabled"));

                    if (json_offer.has("order_button") && !json_offer.isNull("order_button"))
                        offer.setOrder_button(json_offer.getString("order_button"));

                    if (json_offer.has("cf") && !json_offer.isNull("cf")) {
                        OfferCFParser mOfferCurrencyParser = new OfferCFParser(new JSONObject(json_offer.getString("cf")));
                        offer.setCf(mOfferCurrencyParser.getCFs());
                    }

                    if (json_offer.has("currency") && !json_offer.isNull("currency")) {
                        OfferCurrencyParser mOfferCurrencyParser = new OfferCurrencyParser(new JSONObject(
                                json_offer.getString("currency")
                        ));
                        offer.setCurrency(mOfferCurrencyParser.getCurrency());

                    }

                    if (json_offer.has("images") && !json_offer.isNull("images")) {
                        String jsonValues = "";

                        try {
                            jsonValues = json_offer.getJSONObject("images").toString();
                            JSONObject jsonObject = new JSONObject(jsonValues);
                            ImagesParser imgp = new ImagesParser(jsonObject);

                            if (imgp.getImagesList().size() > 0) {
                                offer.setListImages(imgp.getImagesList());
                                offer.setImages(imgp.getImagesList().get(0));
                            }

                        } catch (JSONException jex) {
                            offer.setListImages(new RealmList<Images>());
                        }

                    }


                    list.add(offer);
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
