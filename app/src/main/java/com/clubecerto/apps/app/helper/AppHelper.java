package com.clubecerto.apps.app.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Abderrahim on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AppHelper {

    private static ProgressDialog mDialog;

    public static List<User> prepareListWithHeaders(List<User> list) {

        for (int i = 0; i < list.size(); i++) {

            User user = list.get(i);
            if (i == 0) {
                user.setWithHeader(true);
            } else if (i > 0) {

                try {

                    if (list.get(0).getDistance() <= Constances.DISTANCE_CONST &&
                            (user.getDistance() > Constances.DISTANCE_CONST)) {
                        user.setWithHeader(true);
                        list.set(i, user);
                        break;

                    } else if (list.get(0).getDistance() > Constances.DISTANCE_CONST) {
                        break;
                    }

                } catch (Exception e) {

                }

            }

        }

        return list;
    }


    /**
     * method to check if android version is lollipop
     *
     * @return this return value
     */
    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    /**
     * method to load json files from asset directory
     *
     * @param mContext this is  parameter for loadCountriesJSONFromAsset  method
     * @return return value
     */
    public static String loadCountriesJSONFromAsset(Context mContext) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("data/country_phones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Check permissions methods for Android M
     */


}
