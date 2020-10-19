package com.clubecerto.apps.app.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.Services.NotifyDataNotificationEvent;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Notification;
import com.clubecerto.apps.app.controllers.notification.NotificationController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.api_parser.NotificationParser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CommunApiCalls {

    public static void getNotifications(final Context context, final int user_id) {

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_NOTIFICATIONS_GET, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        Log.e("getNotificationResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final NotificationParser mNotificationParser = new NotificationParser(jsonObject);
                    int success = Integer.parseInt(mNotificationParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1 && mNotificationParser.getNotifications(context).size() > 0) {
                        NotificationController.removeAll();
                        NotificationController.insertNotifications(
                                mNotificationParser.getNotifications(context)
                        );


                        //subscrib event listener to update the badge number
                        int countUnseenNotif = NotificationController.countUnseenNotifications();
                        EventBus.getDefault().post(new NotifyDataNotificationEvent("update_badges"));
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("auth_type", "user");
                params.put("auth_id", String.valueOf(user_id));
                if (AppConfig.APP_DEBUG) {
                    Log.e("LoginActivity", "  params get notification :" + params.toString());
                }

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    public static void countUnseenNotifications(final Context context) {


        Map<String, String> params = new HashMap<String, String>();

        if (SessionsController.getLocalDatabase.isLogged()) {
            params.put("user_id", String.valueOf(SessionsController.getLocalDatabase.getUserId()));
            params.put("guest_id", String.valueOf(SessionsController.getLocalDatabase.getGuestId()));
        } else {
            params.put("auth_type", "guest");
            params.put("auth_id", String.valueOf(SessionsController.getLocalDatabase.getGuestId()));
        }

        params.put("status", "0");

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_NOTIFICATIONS_COUNT_GET, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        Log.e("getNotificationResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final Parser mNotificationParser = new Parser(jsonObject);

                    if (mNotificationParser.getSuccess() == 1) {

                        //subscrib esvent listener to update the badge number
                        int countUnseenNotif = Integer.parseInt(mNotificationParser.getStringAttr(Tags.RESULT));

                        if (AppConfig.APP_DEBUG)
                            Log.e("NotificationsCount", "unseen notification " + countUnseenNotif);

                        Notification.notificationsUnseen = countUnseenNotif;

                        //update ui
                        EventBus.getDefault().post(new NotifyDataNotificationEvent("update_badges"));
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                if (AppConfig.APP_DEBUG) {
                    Log.e("UnseenNotifCount", "  params get notification :" + params.toString());
                }

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


}
