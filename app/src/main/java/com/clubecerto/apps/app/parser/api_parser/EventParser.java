package com.clubecerto.apps.app.parser.api_parser;


import android.util.Log;

import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.Event;
import com.clubecerto.apps.app.classes.Images;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class EventParser extends Parser {

    public EventParser(JSONObject json) {
        super(json);
    }

    public RealmList<Event> getEvents() {

        RealmList<Event> list = new RealmList<Event>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);
            if (AppConfig.APP_DEBUG) {
                Log.e("JSONEventArray", json.toString());
            }

            for (int i = 0; i < json_array.length(); i++) {


                try {

                    JSONObject json_event = json_array.getJSONObject(i + "");

                    if (AppConfig.APP_DEBUG) {
                        Log.e("EventUD", json_event + "");
                    }
                    Event event = new Event();
                    event.setId(json_event.getInt("id_event"));
                    event.setName(json_event.getString("name"));
                    event.setAddress(json_event.getString("address"));
                    event.setLat(json_event.getDouble("lat"));
                    event.setLng(json_event.getDouble("lng"));
                    // store.setType(json_event.getInt("type"));
                    event.setStatus(json_event.getInt("status"));

                    try {
                        event.setLink(json_event.getString("link"));
                    } catch (Exception e) {
                    }


                    try {
                        event.setFeatured(json_event.getInt("featured"));
                    } catch (Exception e) {
                    }

                    try {
                        event.setDistance(json_event.getDouble("distance"));
                    } catch (Exception e) {
                        event.setDistance(0.0);
                    }

                    try {
                        event.setStore_name(json_event.getString("store_name"));
                        if (json_event.has("store_id"))
                            event.setStore_id(json_event.getInt("store_id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        event.setStore_name("");
                        event.setStore_id(0);
                    }

                    event.setTel(json_event.getString("tel"));
                    event.setDateB(json_event.getString("date_b"));
                    event.setDateE(json_event.getString("date_e"));
                    event.setDescription(json_event.getString("description"));
                    event.setWebSite(json_event.getString("website"));

                /*if(!json_event.isNull("detail") && json_event.has("detail"))
                    store.setDetail(json_event.getJSONObject("detail"));
                else
                    store.setDetail(new JSONObject(""));
                    */


                    String jsonValues = "";
                    try {

                        if (!json_event.isNull("images")) {
                            jsonValues = json_event.getJSONObject("images").toString();
                            JSONObject jsonObject = new JSONObject(jsonValues);
                            ImagesParser imgp = new ImagesParser(jsonObject);
                            event.setListImages(imgp.getImagesList());
                            event.setImageJson(json_event.toString());
                        } else {
                            event.setListImages(new RealmList<Images>());
                            event.setImageJson(null);
                        }

                    } catch (JSONException jex) {
                        event.setListImages(new RealmList<Images>());
                    }


                    if (AppConfig.APP_DEBUG) {
                        Log.e("ParserEvent", event.getId() + "  " + event.getAddress() + "   " + event.getWebSite());
                    }


                    list.add(event);
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
