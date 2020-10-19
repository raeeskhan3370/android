package com.clubecerto.apps.app.network.api_request;


import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.AppContext;
import com.clubecerto.apps.app.utils.Translator;

import java.util.HashMap;
import java.util.Map;


public class SimpleRequest extends StringRequest {

    public static int TIME_OUT = 40000;

    public SimpleRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        Map<String, String> customHeader = new HashMap<>();

        try {

            customHeader = AppController.getTokens();
            customHeader.put("Language", Translator.DefaultLang);
            customHeader.put("Debug", String.valueOf(AppContext.DEBUG));
            customHeader.put("Api-key-android", AppConfig.ANDROID_API_KEY);

            if (AppConfig.APP_DEBUG)
                Log.e("getHeaders", customHeader.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return customHeader;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {


        return super.getParams();
    }
}