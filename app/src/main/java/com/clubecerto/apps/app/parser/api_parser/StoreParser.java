package com.clubecerto.apps.app.parser.api_parser;


import android.util.Log;

import com.clubecerto.apps.app.appconfig.AppContext;
import com.clubecerto.apps.app.classes.Images;
import com.clubecerto.apps.app.classes.Store;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class StoreParser extends Parser {

    public StoreParser(JSONObject json) {
        super(json);
    }

    public RealmList<Store> getStore() {

        RealmList<Store> list = new RealmList<Store>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {


                try {
                    JSONObject json_user = json_array.getJSONObject(i + "");
                    Store store = new Store();
                    store.setId(json_user.getInt("id_store"));
                    store.setName(json_user.getString("name"));
                    store.setAddress(json_user.getString("address"));
                    store.setLatitude(json_user.getDouble("latitude"));
                    store.setLongitude(json_user.getDouble("longitude"));
                    store.setCategory_id(json_user.getInt("category_id"));
                    // store.setType(json_user.getInt("type"));
                    store.setStatus(json_user.getInt("status"));
                    store.setGallery(json_user.getInt("gallery"));

                    if (json_user.has("website")) {
                        store.setWebsite(json_user.getString("website"));
                    }

                    try {
                        store.setLink(json_user.getString("link"));
                    } catch (Exception e) {
                    }

                    try {
                        store.setDistance(json_user.getDouble("distance"));
                    } catch (Exception e) {
                        store.setDistance(0.0);
                    }

                    store.setPhone(json_user.getString("telephone"));
                    store.setVoted(json_user.getBoolean("voted"));
                    store.setVotes((float) json_user.getDouble("votes"));
                    store.setNbr_votes(json_user.getString("nbr_votes"));

                    store.setNbrOffers(json_user.getInt("nbrOffers"));

                    try {
                        store.setCanChat(json_user.getInt("canChat"));
                    } catch (Exception e) {
                    }

                    try {
                        store.setSaved(json_user.getBoolean("saved"));
                    } catch (Exception e) {
                    }

                    try {
                        store.setLastOffer(json_user.getString("lastOffer"));
                    } catch (Exception e) {
                        store.setLastOffer("");
                    }


                    try {
                        store.setUser_id(json_user.getInt("user_id"));
                    } catch (Exception e) {
                    }


                    try {
                        store.setFeatured(json_user.getInt("featured"));
                    } catch (Exception e) {
                    }


                    try {
                        //   if((json_user.getString("description").equals("{}"))) { store.setDescription("");}
                        store.setDescription(json_user.getString("description"));
                    } catch (Exception e) {
                        store.setDescription("");
                    }

                    try {
                        // if((json_user.getString("detail").equals("{}"))) { store.setDescription("");}
                        store.setDetail(json_user.getString("detail"));
                    } catch (Exception e) {
                        store.setDescription("");
                    }
                /*if(!json_user.isNull("detail") && json_user.has("detail"))
                    store.setDetail(json_user.getJSONObject("detail"));
                else
                    store.setDetail(new JSONObject(""));
                    */


                    JSONObject json_user_manger = new JSONObject(json_user.getString("user"));
                    UserParser mUserParserSender = new UserParser(json_user_manger);
                    User manager = mUserParserSender.getUser().get(0);
                    if (manager != null) {
                        store.setUser(manager);

                        if (AppContext.DEBUG)
                            Log.e("StoreParserManager", manager.getUsername() + "- " + manager.getId() + " sss " + store.getCategory_id());

                    }
                    String jsonValues = "";
                    try {

                        if (!json_user.isNull("images")) {
                            jsonValues = json_user.getJSONObject("images").toString();
                            JSONObject jsonObject = new JSONObject(jsonValues);
                            ImagesParser imgp = new ImagesParser(jsonObject);

                            if (imgp.getImagesList().size() > 0) {
                                store.setImages(imgp.getImagesList().get(0));
                                store.setListImages(imgp.getImagesList());
                                store.setImageJson(jsonObject.toString());
                            }

                          /*  if(imgp.getImagesList().size()>0){
                                store.setImages(imgp.getImagesList().get(0));
                            }
                            store.setListImages(imgp.getImagesList());
                            store.setImageJson(json_user.toString());*/

                        }

                    } catch (JSONException jex) {
                        store.setListImages(new RealmList<Images>());
                    }

                    list.add(store);
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
