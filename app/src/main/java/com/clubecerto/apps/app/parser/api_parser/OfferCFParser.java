package com.clubecerto.apps.app.parser.api_parser;


import com.clubecerto.apps.app.classes.CF;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class OfferCFParser extends Parser {

    public OfferCFParser(JSONObject json) {
        super(json);
    }


    public RealmList<CF> getCFs() {
        RealmList<CF> list = new RealmList<CF>();

        try {

            JSONObject jsonResult = this.json.getJSONObject(Tags.RESULT);


            for (int i = 0; i < jsonResult.length(); i++) {

                JSONObject jsonRow = jsonResult.getJSONObject(String.valueOf(i));


                if (jsonRow.has("fields") && !jsonRow.isNull("fields")) {

                    JSONArray fieldsArray = new JSONArray(jsonRow.getString("fields"));

                    for (int j = 0; j < fieldsArray.length(); j++) {
                        CF mCF = new CF();
                        JSONObject field = fieldsArray.getJSONObject(j);
                        mCF.setLabel(field.getString("label"));
                        mCF.setRequired(field.getInt("required"));
                        mCF.setStep(field.getInt("step"));
                        mCF.setOrder(field.getInt("order"));
                        mCF.setType(field.getString("type"));

                        /*JSONArray typeArray = new JSONArray(field.getString("type"));

                        if (typeArray.length() > 0) {
                            RealmList<String> typeArr = new RealmList<>();
                            for (int k = 0; k < typeArray.length(); k++) {
                                typeArr.add(typeArray.getString(k));
                            }

                            mCF.setType(typeArr);
                        }*/
                        list.add(mCF);
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return list;

    }


}
