package com.clubecerto.apps.app.fragments.orderFrags;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.classes.CF;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.controllers.stores.OffersController;
import com.clubecerto.apps.app.utils.Utils;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.clubecerto.apps.app.AppController.getInstance;
import static com.clubecerto.apps.app.activities.OrderCheckoutActivity.orderFields;


public class OrderInfoFragment extends Fragment {

    public static int AUTOCOMPLETE_REQUEST_CODE = 1001;
    private Offer mOffer;
    private int offer_id;


    public OrderInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order_info, container, false);


        Bundle args = getArguments();
        if (args != null) {
            offer_id = args.getInt("offer_id");
            parserInputViews(root, offer_id);
        }


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
    private void parserInputViews(View view, int offer_id) {

        mOffer = OffersController.findOfferById(offer_id);



        LinearLayout itemWrapper = (LinearLayout) view.findViewById(R.id.item_wrapper);

        if (mOffer != null) {


            int userId = SessionsController.getSession().getUser().getId();
            int cfId = mOffer.getCf_id();
            SharedPreferences saveCF = getInstance().getSharedPreferences("savedCF_" + cfId + "_" + userId, Context.MODE_PRIVATE);


            if (saveCF != null) {

                //get saved custom field from shared  pref
                if (saveCF.getInt("user_id", 0) == userId && saveCF.getInt("req_cf_id", 0) == cfId) {
                    Type type = new TypeToken<HashMap<String, String>>() {}.getType();
                    Gson gson = new Gson();
                    orderFields = gson.fromJson(saveCF.getString("cf", null), type);
                }
            }

            if (orderFields == null) {
                orderFields = new HashMap<String, String>();
            }


            for (CF mCF : mOffer.getCf()) {
                if (mCF.getType() != null) {
                    //List<String> arrayType = Arrays.asList(mCF.getType().split("."));
                    String[] arrayType = mCF.getType().split("\\.");
                    if (arrayType.length > 0 && (arrayType[0].equals("input") || arrayType[0].equals("textarea"))) {


                        TextInputLayout txtInpLayout = new TextInputLayout(view.getContext());
                        txtInpLayout.setHintTextAppearance(R.style.cf_et_style);

                        LinearLayout.LayoutParams TILlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        txtInpLayout.setLayoutParams(TILlp);

                        AppCompatEditText et = new AppCompatEditText(view.getContext());


                        //set data if exist
                        if (orderFields.containsKey(mCF.getLabel()) && orderFields.get(mCF.getLabel()) != null) {
                            et.setText((String) orderFields.get(mCF.getLabel()));
                            et.setVisibility(View.GONE);
                        } else {
                            orderFields.put(mCF.getLabel(), "");

                        }


                        // setting input type filter
                        if (arrayType[1].equals("number")) {
                            et.setInputType(InputType.TYPE_CLASS_NUMBER);
                        } else if (arrayType[1].equals("text")) {
                            et.setInputType(InputType.TYPE_CLASS_TEXT);
                        } else if (arrayType[1].equals("phone")) {
                            et.setInputType(InputType.TYPE_CLASS_PHONE);
                        } else if (arrayType[1].equals("date")) {
                            et.setInputType(InputType.TYPE_CLASS_DATETIME);
                        } else if (arrayType[1].equals("location")) {

                            //set the marker when location is changed
                            Drawable locationDrawable = new IconicsDrawable(getContext())
                                    .icon(CommunityMaterial.Icon.cmd_crosshairs_gps)
                                    .color(ResourcesCompat.getColor(getContext().getResources(), R.color.colorAccent, null))
                                    .sizeDp(18);

                            et.setTag(mCF.getLabel());
                            et.setCompoundDrawables(null, null, locationDrawable, null);
                            et.setCompoundDrawablePadding(4);
                            et.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    final int DRAWABLE_LEFT = 0;
                                    final int DRAWABLE_TOP = 1;
                                    final int DRAWABLE_RIGHT = 2;
                                    final int DRAWABLE_BOTTOM = 3;

                                    if (event.getAction() == MotionEvent.ACTION_UP) {
                                        if (event.getRawX() >= (et.getRight() - et.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                                            String apiKey = getContext().getString(R.string.map_api_key);

                                            /**
                                             * Initialize Places. For simplicity, the API key is hard-coded. In a production
                                             * environment we recommend using a secure mechanism to manage API keys.
                                             */
                                            if (!Places.isInitialized()) {
                                                Places.initialize(getContext(), apiKey);
                                            }

                                            // Set the fields to specify which types of place data to return.
                                            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
                                            // Start the autocomplete intent.
                                            Intent intent = new Autocomplete.IntentBuilder(
                                                    AutocompleteActivityMode.FULLSCREEN, fields)
                                                    .build(getContext());
                                            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

                                            return true;
                                        }
                                    }
                                    return false;
                                }
                            });


                            if (et.getText() != null && et.getText().toString().trim().length() > 0) {
                                String[] arrayLocation = et.getText().toString().split(";");
                                if (arrayLocation.length > 0) {
                                    et.setText(arrayLocation[0]);
                                }

                            }

                        }


                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5));


                        String fieldName = mCF.getLabel();
                        if (mCF.getRequired() == 1) {
                            fieldName = fieldName + "*";
                        }
                        et.setHint(fieldName);


                        //set view listener :
                        et.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                orderFields.put(mCF.getLabel(), s.toString());
                            }
                        });

                        et.setVisibility(View.VISIBLE);

                        txtInpLayout.addView(et);
                        itemWrapper.addView(txtInpLayout);


                    }
                }
            }

        }


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("CustomSearchFrag", "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress() + ", " + place.getLatLng());

                for (CF mCF : mOffer.getCf()) {
                    if (getView().findViewWithTag(mCF.getLabel()) != null) {
                        ((AppCompatEditText) getView().findViewWithTag(mCF.getLabel())).setText(place.getName());
                        orderFields.put(mCF.getLabel(), place.getName() + ";" + place.getLatLng().latitude + ";" + place.getLatLng().longitude);
                    }
                }

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the showError.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("CustomSearchFrag", status.getStatusMessage());
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}