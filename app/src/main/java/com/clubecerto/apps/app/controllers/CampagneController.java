package com.clubecerto.apps.app.controllers;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

/**
 * Created by Droideve on 11/17/2017.
 */

public class CampagneController {

    public static void markView(final int cid) {

        String user_id = "";
        String guest_id = "";

        if (SessionsController.getLocalDatabase.isLogged()) {

            user_id = String.valueOf(SessionsController.getLocalDatabase.getUserId());
            guest_id = String.valueOf(SessionsController.getLocalDatabase.getGuestId());

        } else {
            guest_id = String.valueOf(SessionsController.getLocalDatabase.getGuestId());
        }

        String finalUser_id = user_id;
        String finalGuest_id = guest_id;


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_MARK_VIEW, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {

                    if (APP_DEBUG) {
                        Log.e("CMarkView", response + "----" + cid);
                    }

                    JSONObject jsonObject = new JSONObject(response);


                } catch (JSONException e) {
                    //send a rapport to support
                    if (APP_DEBUG)
                        e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("cid", String.valueOf(cid));
                params.put("user_id", finalUser_id);
                params.put("guest_id", finalGuest_id);

                if (AppConfig.APP_DEBUG)
                    Log.e("CMarkViewSync", params.toString());

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(AppController.getInstance()).getRequestQueue().add(request);

    }

    public static void markReceive(final int cid) {

        String user_id = "";
        String guest_id = "";

        if (SessionsController.getLocalDatabase.isLogged()) {

            user_id = String.valueOf(SessionsController.getLocalDatabase.getUserId());
            guest_id = String.valueOf(SessionsController.getLocalDatabase.getGuestId());

        } else {
            guest_id = String.valueOf(SessionsController.getLocalDatabase.getGuestId());
        }

        String finalUser_id = user_id;
        String finalGuest_id = guest_id;


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_MARK_RECEIVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {

                    if (APP_DEBUG) {
                        Log.e("CMarkView", response + "----" + cid);
                    }

                    JSONObject jsonObject = new JSONObject(response);


                } catch (JSONException e) {
                    //send a rapport to support
                    if (APP_DEBUG)
                        e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("cid", String.valueOf(cid));
                params.put("user_id", finalUser_id);
                params.put("guest_id", finalGuest_id);

                if (AppConfig.APP_DEBUG)
                    Log.e("CMarkViewSync", params.toString());

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(AppController.getInstance()).getRequestQueue().add(request);

    }

}
