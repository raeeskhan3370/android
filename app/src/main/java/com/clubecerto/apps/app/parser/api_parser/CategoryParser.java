package com.clubecerto.apps.app.parser.api_parser;

import android.util.Log;

import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class CategoryParser extends Parser {
    public CategoryParser(JSONObject json) {
        super(json);
    }


    public RealmList<Category> getCategories() {

        RealmList<Category> list = new RealmList<Category>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {


                try {
                    JSONObject json_user = json_array.getJSONObject(i + "");
                    Category cat = new Category();
                    cat.setNumCat(json_user.getInt("id_category"));
                    cat.setNameCat(json_user.getString("name"));
                    cat.setParentCategory(json_user.getInt("parent_id"));
                    cat.setMenu(false);

                    try {
                        ImagesParser mImagesParser = new ImagesParser(
                                new JSONObject(json_user.getString("image"))
                        );
                        cat.setImages(mImagesParser.getImage());
                    } catch (Exception e) {
                        if (AppConfig.APP_DEBUG)
                            e.printStackTrace();
                    }


                    try {
                        cat.setNbr_stores(json_user.getInt("nbr_stores"));
                    } catch (Exception e) {
                        cat.setNbr_stores(0);
                    }

                    if (AppConfig.APP_DEBUG)
                        Log.e("categoryImages", json_user.getString("image"));


                    list.add(cat);
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
