package com.clubecerto.apps.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.GPS.GPStracker;
import com.clubecerto.apps.app.GPS.Position;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.classes.Discussion;
import com.clubecerto.apps.app.classes.Store;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.controllers.CampagneController;
import com.clubecerto.apps.app.controllers.SettingsController;
import com.clubecerto.apps.app.controllers.categories.CategoryController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.controllers.stores.StoreController;
import com.clubecerto.apps.app.fragments.GalleryFragment;
import com.clubecerto.apps.app.fragments.SlideshowDialogFragment;
import com.clubecerto.apps.app.fragments.StoreOffersFragment;
import com.clubecerto.apps.app.fragments.StoreReviewsFragment;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.StoreParser;
import com.clubecerto.apps.app.unbescape.html.HtmlEscape;
import com.clubecerto.apps.app.utils.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;

public class StoreDetailActivity extends AppCompatActivity implements ViewManager.CustomView,
        GoogleMap.OnMapLoadedCallback, View.OnClickListener, OnMapReadyCallback {

    private static boolean opened = false;
    private static boolean isFirstTime = true;
    public ViewManager mViewManager;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.description_label)
    TextView description_label;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.nbrPictures)
    TextView nbrPictures;
    @BindView(R.id.distanceView)
    TextView distanceView;
    @BindView(R.id.progressMapLL)
    LinearLayout progressMapLL;
    @BindView(R.id.mapcontainer)
    LinearLayout mapcontainer;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.adsLayout)
    LinearLayout adsLayout;
    @BindView(R.id.address_content)
    TextView addressContent;
    @BindView(R.id.catImage)
    ImageView catImage;
    @BindView(R.id.category_content)
    TextView categoryContent;
    @BindView(R.id.category_layout)
    LinearLayout categoryLayout;
    @BindView(R.id.description_content)
    TextView descriptionContent;
    @BindView(R.id.offersBtn)
    Button offersBtn;
    @BindView(R.id.offersBtnLayout)
    LinearLayout offersBtnLayout;
    @BindView(R.id.reviewsBtn)
    Button reviewsBtn;
    @BindView(R.id.reviewsBtnLayout)
    LinearLayout reviewsBtnLayout;
    @BindView(R.id.galleryBtn)
    Button galleryBtn;
    @BindView(R.id.galleryBtnLayout)
    LinearLayout galleryBtnLayout;
    @BindView(R.id.store_content_block_btns)
    LinearLayout storeContentBlockBtns;
    @BindView(R.id.scontainer)
    LinearLayout scontainer;
    @BindView(R.id.store_content_block)
    LinearLayout storeContentBlock;
    @BindView(R.id.btn_chat_customer)
    ImageButton btnChatCustomer;
    @BindView(R.id.websiteBtn)
    ImageButton btnWebsite;
    @BindView(R.id.phoneBtn)
    ImageButton phoneBtn;
    @BindView(R.id.mapBtn)
    ImageButton mapBtn;
    @BindView(R.id.shareBtn)
    ImageButton shareBtn;
    @BindView(R.id.saveBtn)
    ImageButton saveBtn;
    @BindView(R.id.deleteBtn)
    ImageButton deleteBtn;
    @BindView(R.id.btnsLayout)
    LinearLayout btnsLayout;
    @BindView(R.id.mScroll)
    ParallaxScrollView mScroll;
    private Context context;
    ////////////////////////MAPPING
    private GoogleMap mMap;
    private LatLng customerPosition;
    private Toolbar toolbar;
    private Store storedata;
    private GPStracker mGPS;
    //init request http
    private RequestQueue queue;
    private Drawable offerIconWhite, reviewsIconWhite, galleryIconWhite;
    private Drawable offerIconActive, reviewsIconActive, galleryIconActive;
    private User mUserSession;

    public static boolean isOpend() {
        return opened;
    }

    private void attachMap() {

        try {

            SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapping);
            if (mSupportMapFragment == null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mSupportMapFragment = SupportMapFragment.newInstance();
                mSupportMapFragment.setRetainInstance(true);
                fragmentTransaction.replace(R.id.mapping, mSupportMapFragment).commit();
            }
            if (mSupportMapFragment != null) {
                mSupportMapFragment.getMapAsync(StoreDetailActivity.this);
            }

        } catch (Exception e) {
            progressMapLL.setVisibility(View.GONE);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap = googleMap;
        if (storedata.getLatitude() != null && storedata.getLatitude() != null) {

            double TraderLat = storedata.getLatitude();
            double TraderLng = storedata.getLongitude();
            customerPosition = new LatLng(TraderLat, TraderLng);
            //INITIALIZE MY LOCATION
            GPStracker trackMe = new GPStracker(this);
            if (AppConfig.APP_DEBUG)
                Log.e("__lat", String.valueOf(customerPosition.latitude));
            moveToPosition(mMap, customerPosition);
        }

        progressMapLL.setVisibility(View.GONE);

    }

    private void moveToPosition(GoogleMap gm, LatLng targetPosition) {

        gm.moveCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, 16));
        gm.getUiSettings().setZoomControlsEnabled(true);
        gm.addMarker(new MarkerOptions()
                .title(context.getString(R.string.your_destination))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                .position(targetPosition));
    }

    @Override
    protected void onDestroy() {

        if (adView != null)
            adView.destroy();


        super.onDestroy();
        opened = false;

        final android.app.FragmentManager fragManager = this.getFragmentManager();
        final Fragment fragment = fragManager.findFragmentById(R.id.mapping);
        if (fragment != null) {
            fragManager.beginTransaction().remove(fragment).commit();
        }
    }

    private final void focusOnView(final int redId) {

        mScroll.post(new Runnable() {
            @Override
            public void run() {
                //mScroll.scrollTo(0, (findViewById(redId)).getBottom());
                // mScroll.fullScroll(View.FOCUS_DOWN);

                View lastChild = mScroll.getChildAt(mScroll.getChildCount() - 1);
                int bottom = lastChild.getBottom() + mScroll.getPaddingBottom();
                int sy = mScroll.getScrollY();
                int sh = mScroll.getHeight();
                int delta = bottom - (sy + sh);

                mScroll.smoothScrollBy(0, delta);
            }
        });


    }

    @Override
    protected void onPause() {

        if (adView != null)
            adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (adView != null)
            adView.resume();
        super.onResume();
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_store_detail);
        ButterKnife.bind(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (AppConfig.APP_DEBUG)
                        Log.e("onScrollChange", "scrollX=" + scrollX + ";scrollY=" + scrollY);

                    if (scrollY < 600) {
                        toolbar.setBackground(getDrawable(R.drawable.gradient_bg_top_to_bottom_70));
                        toolbarTitle.setVisibility(View.GONE);
                    } else {
                        toolbar.setBackgroundColor(getColor(R.color.toolbarColor));
                        toolbarTitle.setTextColor(getColor(R.color.colorAccent));
                        toolbarTitle.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        if (AppConfig.SHOW_ADS && AppConfig.SHOW_ADS_IN_STORE) {

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("FFD811D6CAB26FA340E98A773B3408ED")
                    .addTestDevice("3CB74DFA141BF4D0823B8EA7D94531B5")
                    .build();
            adView.loadAd(adRequest);
            adView.setVisibility(View.VISIBLE);
            adsLayout.setVisibility(View.VISIBLE);

        } else
            adsLayout.setVisibility(View.GONE);


        reviewsBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        offersBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        galleryBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        ////////////////

        queue = VolleySingleton.getInstance(this).getRequestQueue();


        //GET USER SESSION
        if (SessionsController.isLogged())
            mUserSession = SessionsController.getSession().getUser();
        //INIT TOOLBAR
        setupToolbar();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //GET DATA
        if (isFirstTime == true) {

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    getStore();

                    isFirstTime = false;
                    // openMap();
                    mViewManager.showResult();
                }
            }, 1000);


        } else {

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewManager.showResult();
                    getStore();
                }
            }, 500);
        }

        invalidateOptionsMenu();

        context = this;

        mScroll = findViewById(R.id.mScroll);


        //Initialize map fragment

        mGPS = new GPStracker(this);
        shareBtn.setVisibility(View.GONE); //HIDE THE SHARE BTN



        //  INIT BUTTON CLICK LISTNER
        //make links in a TextView clickable
        descriptionContent.setMovementMethod(LinkMovementMethod.getInstance());

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (storedata != null) {

                    StoreController.doSave(storedata.getId());
                    saveBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.VISIBLE);

                    if (SessionsController.isLogged())
                        doSave(mUserSession.getId(), storedata.getId());

                }

            }
        });


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storedata != null && storedata.getListImages().size() > 0) {
                    SlideshowDialogFragment.newInstance().show(StoreDetailActivity.this, storedata.getListImages(), 0);
                }
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AppConfig.ENABLE_LOCAL_MAPS_DIRECTION) {
                    if (storedata != null && mGPS.canGetLocation()) {

                        Intent intent = new Intent(StoreDetailActivity.this, MapDirectionActivity.class);
                        intent.putExtra("latitude", storedata.getLatitude() + "");
                        intent.putExtra("longitude", storedata.getLongitude() + "");
                        intent.putExtra("name", storedata.getName() + "");
                        intent.putExtra("description", storedata.getAddress() + "");
                        intent.putExtra("distance", storedata.getDistance() + "");

                        startActivity(intent);

                    } else if (!mGPS.canGetLocation()) {
                        mGPS.showSettingsAlert();
                        Toast.makeText(StoreDetailActivity.this, R.string.enable_gps_map_direction, Toast.LENGTH_LONG).show();
                    } else if (!ServiceHandler.isNetworkAvailable(context)) {
                        mGPS.showSettingsAlert();
                        Toast.makeText(StoreDetailActivity.this, R.string.enable_network_map_direction, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Uri gmmIntentUri = Uri.parse(String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", storedata.getLatitude(), storedata.getLongitude()));

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }

            }
        });


        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storedata != null) {

                    StoreController.doDelete(storedata.getId());
                    deleteBtn.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.VISIBLE);

                    if (SessionsController.isLogged())
                        doUnsave(mUserSession.getId(), storedata.getId());

                }
            }
        });


        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //INIT VIEW MANAGER
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingLayout(findViewById(R.id.loading));
        mViewManager.setResultLayout(findViewById(R.id.content_my_store));
        mViewManager.setErrorLayout(findViewById(R.id.error));
        mViewManager.setEmpty(findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);

        mViewManager.loading();

        try {
            int cid = Integer.parseInt(getIntent().getExtras().getString("cid"));
            CampagneController.markView(cid);
        } catch (Exception e) {

        }


        //block reviews,offers,gallery buttons
        //white
        this.reviewsIconWhite = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_comment)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.defaultWhiteColor, null))
                .sizeDp(14);
        this.offerIconWhite = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_tag)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.defaultWhiteColor, null))
                .sizeDp(14);

        this.galleryIconWhite = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_image_album)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.defaultWhiteColor, null))
                .sizeDp(14);


        //active
        this.reviewsIconActive = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_comment)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null))
                .sizeDp(14);
        this.offerIconActive = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_tag)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null))
                .sizeDp(14);

        this.galleryIconActive = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_image_album)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null))
                .sizeDp(14);


        setLeftIcon(this.reviewsBtn, this.reviewsIconWhite);
        setLeftIcon(this.offersBtn, this.offerIconWhite);
        setLeftIcon(this.galleryBtn, this.galleryIconWhite);

    }

    private void setLeftIcon(Button view, Drawable icon) {

        if (AppController.isRTL()) {
            view.setCompoundDrawables(null, null, icon, null);
        } else {
            view.setCompoundDrawables(icon, null, null, null);
        }

        view.setCompoundDrawablePadding(20);
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        /////////////////////////////
        menu.findItem(R.id.rate_review).setVisible(false);
        Drawable review = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_thumb_up_outline)
                .color(ResourcesCompat.getColor(getResources(), R.color.defaultWhiteColor, null))
                .sizeDp(22);
        menu.findItem(R.id.rate_review).setIcon(review);
        //////////////////////////////

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (storedata.isLoaded() && storedata != null && !storedata.isVoted()) {
                        menu.findItem(R.id.rate_review).setVisible(true);
                    }
                } catch (Exception e) {
                }


            }
        }, 5000);

        /////////////////////////////
        menu.findItem(R.id.send_location).setVisible(true);
        Drawable send_location = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_share_variant)
                .color(ResourcesCompat.getColor(getResources(), R.color.defaultWhiteColor, null))
                .sizeDp(22);
        menu.findItem(R.id.send_location).setIcon(send_location);
        /////////////////////////////


        return true;
    }


    @Override
    public void onStart() {
        super.onStart();

        Drawable heart = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_heart)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark, null))
                .sizeDp(24);

        Drawable heart_outline = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_heart_outline)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null))
                .sizeDp(24);


        saveBtn.setImageDrawable(heart_outline);
        deleteBtn.setImageDrawable(heart);
        deleteBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null));


        opened = true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setupToolbar() {

        toolbar = findViewById(R.id.app_bar);
        //toolbar.getContext().setTheme(R.style.ActionBarThemeOverlayCustom);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle.setText(R.string.store_title_detail);

    }


    private void getStore() {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        int store_id = 0;


        //get it from external url (deep linking)
        try {

            Intent appLinkIntent = getIntent();
            String appLinkAction = appLinkIntent.getAction();
            Uri appLinkData = appLinkIntent.getData();

            if (appLinkAction.equals(Intent.ACTION_VIEW)) {

                if (AppConfig.APP_DEBUG)
                    Toast.makeText(getApplicationContext(), appLinkData.toString(), Toast.LENGTH_LONG).show();
                store_id = Utils.dp_get_id_from_url(appLinkData.toString(), "store");

                if (AppConfig.APP_DEBUG)
                    Toast.makeText(getApplicationContext(), "The ID: " + store_id + " " + appLinkAction, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {

        }


        //get it from internal app
        if (store_id == 0) {

            Bundle bundle = getIntent().getExtras();
            store_id = bundle.getInt("id");
            try {
                if (store_id == 0) {
                    store_id = Integer.parseInt(bundle.getString("id"));
                }
            } catch (Exception e) {
                store_id = 0;
            }
        }


        if (AppConfig.APP_DEBUG)
            Log.e("_2_store_id", String.valueOf(store_id));

        storedata = StoreController.getStore(store_id);

        if (storedata != null) {
            setupDataIntoStoreDetailViews();
        } else {
            syncStore(store_id);
        }

        realm.commitTransaction();
    }

    private void setupDataIntoStoreDetailViews() {


        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showOfferFrag();
            }
        }, 1500);


        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                attachMap();
            }
        }, 1500);

        description_label.setText(storedata.getName());
        toolbarTitle.setText(storedata.getName());

        if (storedata.getGallery() > 0) {

            int width = storeContentBlockBtns.getLayoutParams().width;

            storeContentBlockBtns.setWeightSum(12);

            LinearLayout.LayoutParams offersBtnLayoutParams = (LinearLayout.LayoutParams) offersBtnLayout.getLayoutParams();
            offersBtnLayoutParams.width = width / 3;
            offersBtnLayoutParams.weight = 4;
            offersBtnLayout.setLayoutParams(offersBtnLayoutParams);

            LinearLayout.LayoutParams reviewsBtnLayoutParams = (LinearLayout.LayoutParams) reviewsBtnLayout.getLayoutParams();
            reviewsBtnLayoutParams.width = width / 3;
            reviewsBtnLayoutParams.weight = 4;
            reviewsBtnLayout.setLayoutParams(reviewsBtnLayoutParams);

            LinearLayout.LayoutParams galleryBtnLayoutParams = (LinearLayout.LayoutParams) galleryBtnLayout.getLayoutParams();
            galleryBtnLayoutParams.width = width / 3;
            galleryBtnLayoutParams.weight = 4;
            galleryBtnLayout.setLayoutParams(galleryBtnLayoutParams);

        } else {

            int width = storeContentBlockBtns.getLayoutParams().width;

            storeContentBlockBtns.setWeightSum(10);

            LinearLayout.LayoutParams offersBtnLayoutParams = (LinearLayout.LayoutParams) offersBtnLayout.getLayoutParams();
            offersBtnLayoutParams.width = width / 2;
            offersBtnLayoutParams.weight = 5;
            offersBtnLayout.setLayoutParams(offersBtnLayoutParams);


            LinearLayout.LayoutParams reviewsBtnLayoutParams = (LinearLayout.LayoutParams) reviewsBtnLayout.getLayoutParams();
            reviewsBtnLayoutParams.width = width / 2;
            reviewsBtnLayoutParams.weight = 5;
            reviewsBtnLayout.setLayoutParams(reviewsBtnLayoutParams);


            galleryBtnLayout.setVisibility(View.GONE);
            LinearLayout.LayoutParams galleryBtnLayoutParams = (LinearLayout.LayoutParams) galleryBtnLayout.getLayoutParams();
            galleryBtnLayoutParams.width = 0;
            galleryBtnLayoutParams.weight = 0;
            galleryBtnLayout.setLayoutParams(galleryBtnLayoutParams);

            storeContentBlockBtns.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.grey_field, null));


        }

        if (storedata.getListImages() != null && storedata.getListImages().size() > 0) {

            mapcontainer.setVisibility(View.VISIBLE);

            Glide.with(getBaseContext())
                    .load(storedata.getListImages().get(0)
                            .getUrl500_500())
                    .centerCrop().placeholder(R.drawable.def_logo)
                    .into(image);

        } else {

            Glide.with(getBaseContext())
                    .load(R.drawable.def_logo)
                    .centerCrop().placeholder(R.drawable.def_logo)
                    .into(image);

        }

        Drawable markerDrwable = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_map_marker)
                .color(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null))
                .sizeDp(18);

        addressContent.setText(storedata.getAddress());
        addressContent.setCompoundDrawablePadding(10);
        addressContent.setCompoundDrawables(markerDrwable, null, null, null);

        phoneBtn.setOnClickListener(this);
        if (storedata.getPhone().trim().equals("")) {
            phoneBtn.setVisibility(View.GONE);
        }

        if (storedata.getWebsite() != null && !storedata.getWebsite().equals("null")) {
            btnWebsite.setOnClickListener(this);
            btnWebsite.setVisibility(View.VISIBLE);
        }else{
            btnWebsite.setVisibility(View.GONE);
        }

        if (StoreController.isSaved(storedata.getId())) {
            saveBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.VISIBLE);
        } else {
            deleteBtn.setVisibility(View.GONE);
            saveBtn.setVisibility(View.VISIBLE);
        }

        if (storedata.getListImages() != null && storedata.getListImages().size() > 1) {

            Drawable camera = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_camera)
                    .color(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null))
                    .sizeDp(12);

            nbrPictures.setText(storedata.getListImages().size() + "");
            nbrPictures.setCompoundDrawables(camera, null, null, null);
            nbrPictures.setCompoundDrawablePadding(10);

        } else {
            nbrPictures.setVisibility(View.GONE);
        }

        Position newPosition = new Position();
        if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
            distanceView.setVisibility(View.GONE);
        }
        Double mDistance = newPosition.distance(mGPS.getLatitude(), mGPS.getLongitude(), storedata.getLatitude(), storedata.getLongitude());
        distanceView.setText(
                Utils.preparDistance(mDistance)
                        + " " +
                        Utils.getDistanceBy(mDistance).toLowerCase()
        );

        btnChatCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SessionsController.isLogged()) {

                    int userId = 0;
                    try {
                        userId = storedata.getUser().getId();
                    } catch (Exception e) {
                        userId = storedata.getUser_id();
                    }

                    Intent intent = new Intent(StoreDetailActivity.this, MessengerActivity.class);
                    intent.putExtra("type", Discussion.DISCUSION_WITH_USER);
                    intent.putExtra("userId", userId);
                    intent.putExtra("storeName", storedata.getName());
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });


        new decodeHtml().execute(storedata.getDetail());


        offersBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOfferFrag();
            }
        });

        reviewsBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReviewsFrag();
            }
        });

        galleryBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGalleryFrag();
            }
        });


        offersBtn.setText(String.format(getString(R.string.offers)));
        reviewsBtn.setText(getString(R.string.review_title));


        try {

            final Category cat = CategoryController.findId(storedata.getCategory_id());
            categoryContent.setText(cat.getNameCat());

            categoryLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(StoreDetailActivity.this, ListStoresActivity.class);
                    intent.putExtra("category", cat.getNumCat());
                    overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);
                    startActivity(intent);

                }
            });


            if (cat.getImages() != null) {
                Glide.with(this).load(cat.getImages().getUrl200_200())
                        .placeholder(ImageLoaderAnimation.glideLoader(context))
                        .centerCrop().into(catImage);
            }

        } catch (Exception e) {
            categoryLayout.setVisibility(View.GONE);
        }



        if (storedata.getCanChat() == 1 && AppConfig.ENABLE_CHAT) {
            btnChatCustomer.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.phoneBtn) {
            try {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + storedata.getPhone().trim()));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    String[] permission = new String[]{Manifest.permission.CALL_PHONE};
                    SettingsController.requestPermissionM(StoreDetailActivity.this, permission);
                    return;
                }
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.store_call_error) + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.websiteBtn) {

            new AwesomeWebView.Builder(StoreDetailActivity.this)
                    .statusBarColorRes(R.color.colorAccent)
                    .theme(R.style.FinestWebViewAppTheme)
                    .show(storedata.getWebsite());
        }
    }


    private void doSave(final int user_id, final int store_id) {

        saveBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.grey_field, null));
        deleteBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.grey_field, null));

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_SAVE_STORE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (AppConfig.APP_DEBUG)
                    Log.e("response", response);

                Toast.makeText(StoreDetailActivity.this, R.string.saved_messag, Toast.LENGTH_LONG).show();
                saveBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null));
                deleteBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null));

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }

                saveBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null));
                deleteBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null));

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", String.valueOf(user_id));
                params.put("store_id", String.valueOf(store_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    private void doUnsave(final int user_id, final int store_id) {

        saveBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.grey_field, null));
        deleteBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null));


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_REMOVE_STORE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                saveBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null));
                deleteBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null));

                if (AppConfig.APP_DEBUG) {
                    Log.e("response", response);
                }

                Toast.makeText(StoreDetailActivity.this, getString(R.string.unsaved_message), Toast.LENGTH_LONG).show();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }

                saveBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null));
                deleteBtn.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null));

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", String.valueOf(user_id));
                params.put("store_id", String.valueOf(store_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    @Override
    public void onMapLoaded() {

        Toast.makeText(getApplicationContext(), "Map is ready ", Toast.LENGTH_SHORT).show();
    }


    public void syncStore(final int store_id) {

        mViewManager.loading();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_GET_STORES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        Log.e("responseStoresString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);

                    //Log.e("response",response);

                    final StoreParser mStoreParser = new StoreParser(jsonObject);
                    RealmList<Store> list = mStoreParser.getStore();

                    if (list.size() > 0) {

                        StoreController.insertStores(list);

                        storedata = list.get(0);
                        setupDataIntoStoreDetailViews();

                        mViewManager.showResult();


                    } else {


                        Toast.makeText(StoreDetailActivity.this, getString(R.string.store_not_found), Toast.LENGTH_LONG).show();
                        finish();

                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                    mViewManager.error();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }
                mViewManager.error();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                //
                params.put("limit", "1");
                params.put("store_id", String.valueOf(store_id));


                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

    private void showOfferFrag() {


        getOffersFragment();

        setLeftIcon(offersBtn, offerIconActive);
        offersBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        offersBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        offersBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));

        setLeftIcon(reviewsBtn, reviewsIconWhite);
        reviewsBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        reviewsBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        reviewsBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        setLeftIcon(galleryBtn, galleryIconWhite);
        galleryBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        galleryBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        galleryBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        storeContentBlock.setVisibility(View.VISIBLE);

    }

    private void showReviewsFrag() {

        getReviewsFragment();

        setLeftIcon(offersBtn, offerIconWhite);
        offersBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        offersBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        offersBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        setLeftIcon(reviewsBtn, reviewsIconActive);
        reviewsBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        reviewsBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        reviewsBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));

        setLeftIcon(galleryBtn, galleryIconWhite);
        galleryBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        galleryBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        galleryBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        storeContentBlock.setVisibility(View.VISIBLE);
    }

    private void showGalleryFrag() {

        getGalleryFragment();

        setLeftIcon(offersBtn, offerIconWhite);
        offersBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        offersBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        offersBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        setLeftIcon(reviewsBtn, reviewsIconWhite);
        reviewsBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        reviewsBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        reviewsBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        setLeftIcon(galleryBtn, galleryIconActive);
        galleryBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        galleryBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        galleryBtnLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));

        storeContentBlock.setVisibility(View.VISIBLE);

    }

    private void getReviewsFragment() {

        try {
            StoreReviewsFragment frag = new StoreReviewsFragment();
            Bundle b = new Bundle();

            if (AppConfig.APP_DEBUG)
                Log.e("_3_store_id", String.valueOf(storedata.getId()));

            b.putInt("store_id", storedata.getId());
            frag.setArguments(b);

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.scontainer, frag).commit();
        } catch (Exception e) {
            if (AppConfig.APP_DEBUG)
                e.printStackTrace();
        }

    }

    private void getOffersFragment() {

        try {
            StoreOffersFragment frag = new StoreOffersFragment();
            Bundle b = new Bundle();

            b.putInt("store_id", storedata.getId());
            frag.setArguments(b);

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.scontainer, frag).commit();

        } catch (Exception e) {
            if (AppConfig.APP_DEBUG)
                e.printStackTrace();
        }

    }

    private void getGalleryFragment() {

        try {

            GalleryFragment frag = new GalleryFragment();
            frag.setParent_width(storeContentBlockBtns.getWidth());
            frag.setShort_mode(true);


            Bundle b = new Bundle();
            b.putInt("int_id", storedata.getId());
            b.putString("type", "store");
            frag.setArguments(b);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.scontainer, frag).commit();

        } catch (Exception e) {
            if (AppConfig.APP_DEBUG)
                e.printStackTrace();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (!MainActivity.isOpend()) {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }
//        else if (item.getItemId() == R.id.action_map) {
//            openMap();
//        }
        else if (item.getItemId() == R.id.rate_review) {

            focusOnView(R.id.store_content_block);
            showReviewsFrag();

        } else if (item.getItemId() == R.id.send_location) {


            double lat = storedata.getLatitude();
            double lon = storedata.getLongitude();
            String mapLink = null;

            try {
                //https://www.google.com/maps/search/?api=1&query=47.5951518,-122.3316393
                mapLink = "https://maps.google.com/?q=" + URLEncoder.encode(storedata.getAddress(), "UTF-8") + "&ll=" + String.format("%f,%f", lat, lon);
            } catch (UnsupportedEncodingException e) {
                mapLink = "https://maps.google.com/?ll=" + String.format("%f,%f", lat, lon);
                e.printStackTrace();
            }


            @SuppressLint({"StringFormatInvalid", "LocalSuppress", "StringFormatMatches"}) String shared_text =
                    String.format(getString(R.string.shared_text),
                            storedata.getName(),
                            getString(R.string.app_name),
                            storedata.getLink()
                    );

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shared_text);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (!MainActivity.isOpend()) {
            startActivity(new Intent(this, MainActivity.class));
        }
        super.onBackPressed();
    }

    private class decodeHtml extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(final String text) {
            super.onPostExecute(text);
            descriptionContent.setText(Html.fromHtml(text));
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        storedata.setDetail(text);
                        realm.copyToRealmOrUpdate(storedata);
                    } catch (Exception e) {

                    }

                }
            });
            //eventData.setDescription(text);
        }

        @Override
        protected String doInBackground(String... params) {

            return HtmlEscape.unescapeHtml(params[0]);
        }
    }
}
