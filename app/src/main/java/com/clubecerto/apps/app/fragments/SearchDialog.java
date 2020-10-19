package com.clubecerto.apps.app.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.Services.LocationChangedEvent;
import com.clubecerto.apps.app.customview.CategoryCustomView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Droideve on 11/18/2017.
 */

public class SearchDialog extends Dialog {

    public static int AUTOCOMPLETE_REQUEST_CODE = 1001;
    private static int mOldDistance = -1;
    private static String mOldValue = "";
    private TextView doSearch;
    private SeekBar mDistanceRange;
    private TextView mDistanceText;
    private MaterialAutoCompleteTextView searchEditText;
    private TextView searchBy;
    private Listener mListener;
    private CategoryCustomView rectCategoryList;
    private HashMap<String, Object> searchParams;
    private TextView locationLbl;
    private Context ctx;
    private LatLng location = null;


    public SearchDialog(@NonNull Context context) {
        super(context);
        ctx = context;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search);

        doSearch = findViewById(R.id.doSearch);
        mDistanceRange = findViewById(R.id.distance);
        mDistanceText = findViewById(R.id.md);
        searchEditText = findViewById(R.id.search);
        searchBy = findViewById(R.id.searchBy);

        initPlacesAPi();


        initCategoryRV();


        if (mOldDistance == -1) {
            int radius = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("distance_value", 100);
            mOldDistance = radius;
        }

        String val = String.valueOf(mOldDistance);
        if (mOldDistance == 100) {
            val = "+" + mOldDistance;
        }

        String msg = String.format(getContext().getString(R.string.settings_notification_distance_dis), val);
        mDistanceText.setText(msg);
        mDistanceRange.setProgress(mOldDistance);
        searchEditText.setText(mOldValue);

        mDistanceRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String val = String.valueOf(progress);
                if (progress == 100) {
                    val = "+" + progress;
                }

                String msg = String.format(getContext().getString(R.string.settings_notification_distance_dis), val);
                mDistanceText.setText(msg);
                mOldDistance = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        doSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSearchClicked(SearchDialog.this, searchEditText.getText().toString(), mOldDistance, rectCategoryList.getItemCategoryselectedId(), location);
                }
            }
        });

    }

    public static SearchDialog newInstance(Context context) {
        return new SearchDialog(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void putDrawableText(String text, CommunityMaterial.Icon cmd_map_marker) {

        //change text
        locationLbl.setText(text);

        //set the marker when location is changed
        Drawable locationDrawable = new IconicsDrawable(getContext())
                .icon(cmd_map_marker)
                .color(ResourcesCompat.getColor(getContext().getResources(), R.color.colorAccent, null))
                .sizeDp(12);

        locationLbl.setCompoundDrawables(locationDrawable, null, null, null);
        locationLbl.setCompoundDrawablePadding(8);
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void dismiss() {
        EventBus.getDefault().unregister(this);
        super.dismiss();
    }

    // This method will be called when a Notification is posted (in the UI thread for Toast)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationChangedEvent event) {
        if (event != null && event.currentPlaces != null) {
            locationLbl.setText(event.currentPlaces.getName());
            location = event.currentPlaces.getLatLng();
            event.currentPlaces = null;
        }
    }

    private void setViewClickListener() {
        locationLbl.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_place_autocomplet);
                dialog.setCancelable(true);


                dialog.findViewById(R.id.change_location_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Set the fields to specify which types of place data to return.
                        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
                        // Start the autocomplete intent.
                        Intent intent = new Autocomplete.IntentBuilder(
                                AutocompleteActivityMode.FULLSCREEN, fields)
                                .build(getContext());

                        // chances of context not being an activity is very low, but better to check.
                        Activity owner = (ctx instanceof Activity) ? (Activity) ctx : null;
                        if (owner != null) {
                            owner.startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                        }
                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.keep_current_location).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        location = null;
                        putDrawableText(getContext().getResources().getString(R.string.current_location), CommunityMaterial.Icon.cmd_adjust);

                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

    }

    private void initPlacesAPi() {


        //location changer
        locationLbl = findViewById(R.id.locationLbl);
        searchParams = new HashMap<>();

        String apiKey = getContext().getString(R.string.map_api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getContext());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            putDrawableText(getContext().getResources().getString(R.string.current_location), CommunityMaterial.Icon.cmd_adjust);
        }
        setViewClickListener();

    }

    public SearchDialog setHeader(String text) {
        searchBy.setText(text);
        return this;
    }

    public SearchDialog setOnSearchListener(Listener l) {
        if (mListener == null) {
            mListener = l;
        }

        return this;
    }

    public void showDialog() {
        if (!isShowing())
            show();
    }


    private void initCategoryRV() {
        rectCategoryList = (CategoryCustomView) findViewById(R.id.rectCategoryList);
        rectCategoryList.loadData(true);
    }

    public interface Listener {
        void onSearchClicked(SearchDialog mSearchDialog, String value, int radius, int category, LatLng location);
    }


}
