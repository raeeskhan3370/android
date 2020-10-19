package com.clubecerto.apps.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.ConfigurationCompat;

import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.google.android.gms.maps.model.LatLng;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;


/**
 * Created by Droideve on 09/06/2015.
 */
public class Utils {


    public static final String DEFAULT_VALUE = "N/A";
    private static String SP_NAME = "q2sUn5aZDmL56";
    private static String SP_NAME_KEY = "q2sUn5aZDmL56tOoKeN";

    public static Bitmap flip(Bitmap src) {

        Matrix matrix = new Matrix();


        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static int dp_get_id_from_url(String url, String prefix) {

        if (!Utils.isValidURL(url))
            return 0;

        String[] list = url.split("/");

        try {

            for (int i = 0; i < list.length; i++) {

                if (prefix.equals(list[i])) {
                    String uri = list[i + 1];

                    if (uri.equals("id")) {
                        String id = list[i + 2];
                        if (AppConfig.APP_DEBUG)
                            Log.e("dp_get_id_from_url", prefix + " " + Integer.parseInt(id) + " closed");
                        return Integer.parseInt(id);
                    }

                }
            }

        } catch (Exception e) {
            if (AppConfig.APP_DEBUG)
                e.printStackTrace();
        }


        return 0;
    }

    public static boolean isValidURL(String url) {

        URL u = null;

        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
    }

    public static Drawable changeDrawableIconMap(Context context, int resId) {

        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resId, null);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        drawable.setColorFilter(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null), mode);

        return drawable;
    }

    public static void enableEvent() {

//        for(int i=0;i<Constances.initConfig.ListCats.size();i++){
//
//            Category tab = Constances.initConfig.ListCats.get(i);
//            if(AppConfig.ENABLE_EVENTS==false){
//                if(tab.getType() == Constances.initConfig.Tabs.EVENTS){
//                    Constances.initConfig.ListCats.remove(i);
//                    Constances.initConfig.Numboftabs = Constances.initConfig.ListCats.size();
//                }
//
//            }
//        }
    }

    public static String getToken(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SP_NAME_KEY, "");
    }

    public static String getDistanceBy(double meters) {

        String FINAL_VALUE = "M";
        if (meters > 0) {

            if (meters > 1000) {

                FINAL_VALUE = "Km";

            }
        } else {
            FINAL_VALUE = "";
        }

        return FINAL_VALUE;

    }

    public static Boolean isNearMAXDistance(double meters) {

        return meters >= 0 && meters <= 100000;

    }

    public static String preparDistance(double meters) {

        String FINAL_VALUE = DEFAULT_VALUE + " ";

        if (meters >= 1000 && meters <= 100000) {

            double kilometers = 0.0;
            kilometers = meters * 0.001;

            DecimalFormat decim = new DecimalFormat("#.##");
            FINAL_VALUE = decim.format(kilometers) + "";

        } else if (meters > 100000) {

            FINAL_VALUE = "+100";

        } else if (meters < 1000) {

            FINAL_VALUE = ((int) meters) + "";

        }


        return FINAL_VALUE;
    }

    public static void setFontBold(Context context, TextView view) {

        Typeface tf = Typeface.createFromAsset(context.getAssets(), Constances.Fonts.BOLD);
        view.setTypeface(tf);

    }

    public static int listViewFormat(String mSPN) {

        SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("list_view", Context.MODE_PRIVATE);
        return sharedPref.getInt(mSPN, 1);
    }

    public static void setListViewFormat(String mSPN, int id) {

        SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("list_view", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(mSPN, id);
        editor.commit();

    }

    public static int convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = (int) (px / (metrics.densityDpi / 160f));
        return dp;
    }

    public static boolean toggleArrow(boolean show, View view) {
        return toggleArrow(show, view, true);
    }

    public static boolean toggleArrow(boolean show, View view, boolean delay) {
        if (show) {
            view.animate().setDuration(delay ? 200 : 0).rotation(180);
            return true;
        } else {
            view.animate().setDuration(delay ? 200 : 0).rotation(0);
            return false;
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dip2pix(@NonNull Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getResources().getDisplayMetrics());
    }

    public static class Params {

        private Bundle params;


        @Override
        public String toString() {

            return params.toString();
        }
    }


    @NonNull
    public static String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        if (locale == null) {
            configuration.setLocale(ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0));
        } else {
            configuration.setLocale(new Locale(locale));
        }
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }


    public static LatLng myLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Location location = Utils.getLastKnownLocation(context);
        return new LatLng(location.getLatitude(), location.getLongitude());

    }


    private static Location getLastKnownLocation(Context context) {

        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    /*public static void setFont(Context context,TextView view,String name){

        Typeface tf = Typeface.createFromAsset(context.getAssets(), Constances.Fonts.REGULAR);
       view.setTypeface(tf);

    }*/

}
