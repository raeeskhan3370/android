package com.clubecerto.apps.app.parser.api_parser;


import com.clubecerto.apps.app.classes.Fee;
import com.clubecerto.apps.app.parser.Parser;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class FeeParser extends Parser {

    public FeeParser(JSONObject json) {
        super(json);
    }

    public RealmList<Fee> getFees() {

        RealmList<Fee> list = new RealmList<Fee>();

        try {

            JSONObject json_array = json.getJSONObject("taxes");
            Fee mFee = new Fee();
            mFee.setId(json_array.getInt("id"));
            mFee.setName(json_array.getString("name"));
            mFee.setValue(json_array.getDouble("value"));
            mFee.setCreated_at(json_array.getString("created_at"));
            mFee.setUpdated_at(json_array.getString("updated_at"));
            list.add(mFee);
        } catch (JSONException e) {
            e.printStackTrace();
        }

           /* }

        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        return list;
    }


}
