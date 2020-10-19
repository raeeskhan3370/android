package com.clubecerto.apps.app.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.GPS.GPStracker;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.Animation;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.AppContext;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.classes.CountriesModel;
import com.clubecerto.apps.app.classes.Module;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.controllers.SettingsController;
import com.clubecerto.apps.app.controllers.categories.CategoryController;
import com.clubecerto.apps.app.controllers.events.EventController;
import com.clubecerto.apps.app.controllers.orders.PaymentController;
import com.clubecerto.apps.app.controllers.stores.OffersController;
import com.clubecerto.apps.app.controllers.stores.StoreController;
import com.clubecerto.apps.app.dtmessenger.TokenInstance;
import com.clubecerto.apps.app.helper.AppHelper;
import com.clubecerto.apps.app.helper.LocalHelper;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.ModuleParser;
import com.clubecerto.apps.app.parser.Parser;
import com.clubecerto.apps.app.parser.api_parser.CategoryParser;
import com.clubecerto.apps.app.parser.api_parser.PayGWParser;
import com.clubecerto.apps.app.parser.tags.Tags;
import com.clubecerto.apps.app.push_notification_firebase.FirebaseInstanceIDService;
import com.clubecerto.apps.app.utils.MessageDialog;
import com.clubecerto.apps.app.utils.Session;
import com.clubecerto.apps.app.utils.Utils;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;

import static com.clubecerto.apps.app.activities.MainActivity.REQUEST_CHECK_SETTINGS;
import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;
import static com.clubecerto.apps.app.security.Security.ANDROID_API_KEY;

public class SplashActivity extends AppCompatActivity implements ViewManager.CustomView, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public ViewManager mViewManager;
    @BindView(R.id.splashImage)
    ImageView splashImage;
    @BindView(R.id.progressBar)
    SpinKitView progressBar;



    private User user = null;
    //init request http
    private RequestQueue queue;
    private boolean firstAppLaunch = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Utils.enableEvent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        AppController.getInstance().updateAndroidSecurityProvider(this);


        LocalHelper.setLocale(getBaseContext(), LocalHelper.getLanguage(getBaseContext()));


        //refresh guest id
        FirebaseInstanceIDService.reloadToken();

        Animation.startZoomEffect(splashImage);

        Realm realm = Realm.getDefaultInstance();


        OffersController.removeAll();
        StoreController.removeAll();
        EventController.removeAll();

        Gson gson = new Gson();
        final List<CountriesModel> list = gson.fromJson(AppHelper.loadCountriesJSONFromAsset(this), new TypeToken<List<CountriesModel>>() {
        }.getType());

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(list);

            }
        });
        //PushNotifcation

        // FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();


        mViewManager = new ViewManager(this);
        mViewManager.setLoadingLayout(findViewById(R.id.loading));
        mViewManager.setResultLayout(findViewById(R.id.content_my_store));
        mViewManager.setErrorLayout(findViewById(R.id.error));
        mViewManager.setEmpty(findViewById(R.id.empty));

        mViewManager.setCustumizeView(this);

        //sync all categories
        getCategories();

        //sync available modules
        availableModulesAPI();

        //sync getway modules
        getPaymentGateway();


    }

    private void initializeCategoryTabs() {

        if (AppConfig.TabsConfig == null)
            AppConfig.TabsConfig = AppController.getInstance().parseAppTabsConfig();


        for (Category cat : AppConfig.TabsConfig)
            CategoryController.insertCategory(cat);
    }


    @Override
    protected void onStart() {
        super.onStart();

        initializeCategoryTabs();


        boolean loaded = false, requiredGpsON = false;

        try {
            loaded = getIntent().getExtras().getBoolean("loaded");
            requiredGpsON = getIntent().getExtras().getBoolean("requiredGpsON");
        } catch (Exception e) {

        }


        //check gps app permission
        //Apply permission for all devices (Version > 5)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && this.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    101);


        } else {

            if (!AppController.isTokenFound()) {

                firstAppLaunch = true;
                mViewManager.loading();
                appInit();

            } else {
                firstAppLaunch = false;
                // re check permission for app
                if (requiredGpsON) {
                    settingsrequest(firstAppLaunch);

                } else if (loaded == false) {
                    mViewManager.loading();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            settingsrequest(firstAppLaunch);

                        }
                    }, 3500);
                } else {
                    settingsrequest(firstAppLaunch);
                }

            }
        }


    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    @Override
    public void customErrorView(View v) {

    }

    @Override
    public void customLoadingView(View v) {

    }

    @Override
    public void customEmptyView(View v) {

    }

    @Override
    public void onClick(View v) {


    }

    private void startMain() {
          Session session;

        session = new Session(this);
        if ( session.isFirstTimeLaunch()  ) {
            Intent intent = new Intent(SplashActivity.this, IntroSliderActivity.class);

            try {
                intent.putExtra("chat", getIntent().getExtras().getBoolean("chat"));
            } catch (Exception e) {
                try {
                    intent.putExtra("chat", Boolean.parseBoolean(getIntent().getExtras().getString("chat")));
                } catch (Exception e1) {

                }
            }

            startActivity(intent);
            finish();

//


        }
        else
        {
            String card= session.getCard();
            if (TextUtils.isEmpty(card))
            {
                launchLoginScreen();
                finish();
            }
            else
            {
                launchHomeScreen();
                finish();
            }
        }



    }
    private void launchHomeScreen() {
        //session.setFirstTimeLaunch(false);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    private void launchLoginScreen() {
        //session.setFirstTimeLaunch(false);
        startActivity(new Intent(this, LoginCPFActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    private void getCategories() {

        queue = VolleySingleton.getInstance(this).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_USER_GET_CATEGORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("catsResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final CategoryParser mModuleParser = new CategoryParser(jsonObject);
                    int success = Integer.parseInt(mModuleParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1 && mModuleParser.getCategories().size() > 0) {
                        CategoryController.removeAll();
                        CategoryController.insertCategories(
                                mModuleParser.getCategories()
                        );
                    }

                } catch (JSONException e) {
                    //send a rapport to support
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

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


    private void appInit() {

        final String device_token = TokenInstance.getTokenID(this);
        final String mac_address = ServiceHandler.getMacAddr();
        final String ip_address = String.valueOf(ServiceHandler.getIpAddress(this));

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_APP_INIT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {

                    if (AppContext.DEBUG)
                        Log.e("response", response);

                    JSONObject js = new JSONObject(response);

                    Parser mParser = new Parser(js);
                    //CityParser mCityParser = new CityParser(js.getJSONObject(Tags.RESULT).getJSONObject(CityParser.TAG));

                    int success = Integer.parseInt(mParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1) {

                        final String token = mParser.getStringAttr("token");

                        if (APP_DEBUG)
                            Toast.makeText(SplashActivity.this, token, Toast.LENGTH_LONG).show();

                        AppController.setTokens(mac_address, device_token, token);

                        startActivity(new Intent(SplashActivity.this, IntroSliderActivity.class));
                        finish();

                        if (AppContext.DEBUG)
                            Log.e("token", token);
                    } else {

                        //show message error
                        MessageDialog.newDialog(SplashActivity.this).onCancelClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MessageDialog.getInstance().hide();
                                finish();
                            }
                        }).onOkClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                appInit();
                                MessageDialog.getInstance().hide();
                            }
                        }).setContent("Token isn't valid!").show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    //show message error
                    MessageDialog.newDialog(SplashActivity.this).onCancelClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MessageDialog.getInstance().hide();
                            finish();
                        }
                    }).onOkClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            appInit();
                            MessageDialog.getInstance().hide();
                        }
                    }).setContent("Error with initialization!").show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                if (AppContext.DEBUG)
                    Log.e("ERROR", error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {

                    //show message error
                    MessageDialog.newDialog(SplashActivity.this).onCancelClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MessageDialog.getInstance().hide();
                            finish();
                        }
                    }).onOkClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            appInit();
                            MessageDialog.getInstance().hide();
                        }
                    }).setContent("Error!").show();


                } else if (error instanceof AuthFailureError) {
                    //TODO
                } else if (error instanceof ServerError) {
                    //TODO
                } else if (error instanceof NetworkError) {
                    //TODO
                } else if (error instanceof ParseError) {
                    //TODO
                }


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("device_token", device_token);
                params.put("mac_address", mac_address);
                params.put("mac_adr", mac_address);
                params.put("crypto_key", ANDROID_API_KEY);

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GPStracker gps = new GPStracker(this);
                    settingsrequest(firstAppLaunch);
                    // keep asking if imp or do whatever
                    if (APP_DEBUG) {
                        Log.e("PermissionRequest", "Granted , lat :" + gps.getLongitude() + "  lng : " + gps.getLatitude());
                    }
                } else {
                    if (APP_DEBUG) {
                        Log.e("PermissionRequest", "Not Granted");
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void settingsrequest(final boolean initApp) {

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (initApp) appInit();
                        else startMain();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(SplashActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        GPStracker gps = new GPStracker(getApplicationContext());
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                GPStracker gps = new GPStracker(getApplicationContext());
                                gps.getLongitude();
                                gps.getLatitude();

                                progressBar.setVisibility(View.GONE);

                                if (firstAppLaunch) appInit();
                                else startMain();
                            }
                        }, 6000);

                        break;
                    case Activity.RESULT_CANCELED:
                        settingsrequest(firstAppLaunch);//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void availableModulesAPI() {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_AVAILABLE_MODULES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppContext.DEBUG)
                        Log.e("modules_manager", response);

                    JSONObject js = new JSONObject(response);

                    // Log.e("response", jsonObject.toString());
                    final ModuleParser mModuleParser = new ModuleParser(js);
                    int success = Integer.parseInt(mModuleParser.getStringAttr(Tags.SUCCESS));
                    if (success == 1) {
                        Module mModule = new Module();
                        RealmList<Module> listModules = mModuleParser.getModules();
                        if (listModules.size() > 0)



                            SettingsController.updateModules(listModules);
                    }

                } catch (JSONException e) {
                    //send a rapport to support
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

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    private void getPaymentGateway() {

        queue = VolleySingleton.getInstance(this).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_PAYMENT_GATEWAY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("paymentGateways", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final PayGWParser mModuleParser = new PayGWParser(jsonObject);
                    int success = Integer.parseInt(mModuleParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1 && mModuleParser.getPaymentGetway().size() > 0) {
                        PaymentController.insertPaymentGatewayList(
                                mModuleParser.getPaymentGetway()
                        );
                    }

                } catch (JSONException e) {
                    //send a rapport to support
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

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

}
