package com.clubecerto.apps.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.clubecerto.apps.app.GPS.GPStracker;
import com.clubecerto.apps.app.GPS.GoogleDirection;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Event;
import com.clubecerto.apps.app.controllers.CampagneController;
import com.clubecerto.apps.app.controllers.SettingsController;
import com.clubecerto.apps.app.controllers.events.EventController;
import com.clubecerto.apps.app.controllers.events.UpComingEventsController;
import com.clubecerto.apps.app.controllers.stores.StoreController;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.EventParser;
import com.clubecerto.apps.app.unbescape.html.HtmlEscape;
import com.clubecerto.apps.app.utils.DateUtils;
import com.clubecerto.apps.app.utils.Utils;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;
import static com.clubecerto.apps.app.appconfig.AppConfig.SHOW_ADS;
import static com.clubecerto.apps.app.appconfig.AppConfig.SHOW_ADS_IN_EVENT;

public class EventDetailActivity extends AppCompatActivity implements ViewManager.CustomView, OnMapReadyCallback {

    LatLng customerPosition, myPosition;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.mScroll)
    ParallaxScrollView scrollView;

    @BindView(R.id.progressBar)
    SpinKitView progressBar;

    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @BindView(R.id.iv_zoom)
    ImageView image;


    @BindView(R.id.participate)
    Button _participate;
    @BindView(R.id.unparticipate)
    Button _unparticipate;


    @BindView(R.id.event_desc)
    TextView description;


    @BindView(R.id.event_date)
    TextView date;

    @BindView(R.id.event_title)
    TextView event_title;

    @BindView(R.id.event_website)
    TextView website;
    @BindView(R.id.event_tel)
    TextView tel;

    @BindView(R.id.layout_phone)
    LinearLayout layout_phone;
    @BindView(R.id.layout_website)
    LinearLayout layout_website;

    //@BindView(R.id.event_date) TextView ;
    @BindView(R.id.event_store_layout)
    LinearLayout event_store_layout;
    @BindView(R.id.event_store_view)
    TextView event_store_view;

    @BindView(R.id.adView)
    AdView mAdView;

    Event eventData;

    private ViewManager mViewManager;

    /*
     *   DECLARATION OF GOOGLE MAPS API
     */
    private GoogleMap mMap;
    private GoogleDirection gd;
    //DATABASE
    private Context context;
    //FloatingActionButton fab;
    //OBJECTS


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        context = this;

        setuToolbar();

        //VIEW LAYOUT MANAGER
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingLayout(findViewById(R.id.loading));
        mViewManager.setResultLayout(findViewById(R.id.content_layout));
        mViewManager.setErrorLayout(findViewById(R.id.error));
        mViewManager.setCustumizeView(this);
        mViewManager.showResult();

        invalidateOptionsMenu();

        setupViews();


        toolbarTransactionScroll();

        //Floating Action Button
        _participate = findViewById(R.id.participate);
        _unparticipate = findViewById(R.id.unparticipate);

        getEventRealm();

        try {
            int cid = Integer.parseInt(getIntent().getExtras().getString("cid"));
            CampagneController.markView(cid);
        } catch (Exception e) {

        }

    }

    private void toolbarTransactionScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
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
    }

    private void initializeBtn() {

        if (EventController.isEventLiked(eventData.getId())) {
            //_participate.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            _participate.setVisibility(View.GONE);
            _unparticipate.setVisibility(View.VISIBLE);

        } else {

            _participate.setVisibility(View.VISIBLE);
            _unparticipate.setVisibility(View.GONE);
            //fab.setImageResource(R.drawable.ic_favorite_white_24dp);
        }
    }

    @Override
    protected void onResume() {

        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();

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

    public void setuToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle.setText(R.string.event_detail);

    }

    private void setupViews() {

        //ADD ADMOB BANNER
        if (SHOW_ADS && SHOW_ADS_IN_EVENT) {

            mAdView = findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("4A55E4EA2535643C0D74A5A73C4F550A")
                    .addTestDevice("3CB74DFA141BF4D0823B8EA7D94531B5")
                    .build();
            mAdView.loadAd(adRequest);
        }

        //make links in a TextView clickable
        description.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        setupMapsView();

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
                mSupportMapFragment.getMapAsync(this);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setupGoogleMaps() {

        if (eventData.getLat() != null && eventData.getLng() != null) {

            double TraderLat = eventData.getLat();
            double TraderLng = eventData.getLng();

            customerPosition = new LatLng(TraderLat, TraderLng);
            customerPosition = new LatLng(TraderLat, TraderLng);

            //
            GPStracker trackMe = new GPStracker(this);
            myPosition = new LatLng(trackMe.getLatitude(), trackMe.getLongitude());

            attachMap();

        } else {

            if (APP_DEBUG)
                Toast.makeText(this, "Debug mode: Couldn't get position maps", Toast.LENGTH_LONG).show();

            findViewById(R.id.mapcontainer).setVisibility(View.GONE);
        }

    }

    private void setupMapsView() {

        if (mMap != null) {

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerPosition, 16));
            //trader location

            Bitmap b = ((BitmapDrawable) Utils.changeDrawableIconMap(
                    EventDetailActivity.this, R.drawable.ic_marker)).getBitmap();
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);


            mMap.addMarker(new MarkerOptions().position(customerPosition)
                    .title(eventData.getName())
                    .anchor(0.0f, 1.0f)
                    .icon(icon)
                    .snippet(eventData.getAddress())).showInfoWindow();

            mMap.addMarker(new MarkerOptions().position(myPosition)
                    .title(eventData.getName())
                    .anchor(0.0f, 1.0f)
                    .draggable(true)
                    //.icon(icon)
                    .snippet(eventData.getAddress())).showInfoWindow();

            if (ServiceHandler.isNetworkAvailable(this)) {

                try {
                    gd = new GoogleDirection(this);
                    //My Location
                    gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {

                        @Override
                        public void onResponse(String status, Document doc, GoogleDirection gd) {
                            mMap.addPolyline(gd.getPolyline(doc, 8,
                                    ResourcesCompat.getColor(getResources(), R.color.colorAccent, null)));
                            //mMap.setMyLocationEnabled(true);
                        }
                    });
                    gd.setLogging(true);
                    gd.request(myPosition, customerPosition, GoogleDirection.MODE_DRIVING);

                } catch (Exception e) {
                    if (APP_DEBUG)
                        e.printStackTrace();
                }

            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        /////////////////////////////
        menu.findItem(R.id.rate_review).setVisible(false);

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
    protected void onPause() {

        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mAdView != null) {
            mAdView.destroy();
        }

    }


    private void setupEventDataIntoViews() {

        toolbarTitle.setText(eventData.getName());
        event_title.setText(eventData.getName());

        //description.setText(eventData.getAddress());
        description.setText(
                Html.fromHtml(/*HtmlEscape.unescapeHtml(*/eventData.getDescription()/*)*/)
        );

        String mDateEvent = String.format(context.getString(R.string.FromTo),
                DateUtils.getDateByTimeZone(eventData.getDateB(), "dd MMMM yyyy") + "",
                DateUtils.getDateByTimeZone(eventData.getDateE(), "dd MMMM yyyy")
        );

        date.setText(Html.fromHtml(mDateEvent));

        if (eventData.getTel().length() > 0 && !eventData.getTel().equals("null")) {
            tel.setText(eventData.getTel());
            tel.setPaintFlags(tel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            layout_phone.setVisibility(View.GONE);

        }

        if (eventData.getListImages().size() > 0) {

            Glide.with(getBaseContext())
                    .load(eventData.getListImages().get(0).getUrl500_500())
                    .centerCrop().placeholder(R.drawable.def_logo)
                    .into(image);
        } else {

            Glide.with(getBaseContext())
                    .load(R.drawable.def_logo)
                    .centerCrop().placeholder(R.drawable.def_logo)
                    .into(image);

        }


        if (eventData.getWebSite().length() > 0 && !eventData.getWebSite().equalsIgnoreCase("null")) {

            try {

                //website.setText(eventData.getWebSite());
                //website.setPaintFlags(website.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                website.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AwesomeWebView.Builder(EventDetailActivity.this)
                                .statusBarColorRes(R.color.colorAccent)
                                .theme(R.style.FinestWebViewAppTheme)
                                .show(eventData.getWebSite());


                    }
                });

            } catch (Exception e) {
                if (APP_DEBUG) {
                    Log.e("WebView", e.getMessage());
                }
            }


        } else {
            layout_website.setVisibility(View.GONE);
        }

        tel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + eventData.getTel().trim()));
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                        String[] permission = new String[]{Manifest.permission.CALL_PHONE};
                        SettingsController.requestPermissionM(EventDetailActivity.this, permission);

                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.store_call_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });


        if (APP_DEBUG) {
            Log.e("eventStore", eventData.getStore_id() + " - " + eventData.getStore_name());
        }

        if (eventData.getStore_id() > 0 && !eventData.getStore_name().equals("") && !eventData.getStore_name().toLowerCase()
                .equals("null")) {

            event_store_layout.setVisibility(View.VISIBLE);
            event_store_view.setVisibility(View.VISIBLE);

            Drawable storeDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_map_marker)
                    .color(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null))
                    .sizeDp(18);

            event_store_view.setText(eventData.getStore_name());
            event_store_view.setCompoundDrawables(storeDrawable, null, null, null);
            event_store_view.setCompoundDrawablePadding(20);
            event_store_view.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
            event_store_view.setPaintFlags(event_store_view.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


            event_store_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    // Do verification if the store exsit before launching the activity
                    // if does not exist launch message
                    if (StoreController.getStore(eventData.getStore_id()) != null) {

                        Intent intent = new Intent(EventDetailActivity.this, StoreDetailActivity.class);
                        intent.putExtra("id", eventData.getStore_id());
                        startActivity(intent);

                    } else {
                        Toast.makeText(getApplicationContext(), R.string.store_not_exist, Toast.LENGTH_LONG).show();
                    }

                    realm.commitTransaction();

                }
            });

        } else {


            event_store_layout.setVisibility(View.VISIBLE);
            event_store_view.setVisibility(View.VISIBLE);

            Drawable storeDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_map_marker)
                    .color(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null))
                    .sizeDp(18);

            event_store_view.setText(eventData.getAddress());
            event_store_view.setCompoundDrawables(storeDrawable, null, null, null);
            event_store_view.setCompoundDrawablePadding(20);


        }


        initializeBtn();
        _participate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                if (eventData != null) {

                    if (!EventController.isEventLiked(eventData.getId())) //CHECK IS THIS EVENT IS ALLREADY LIKED
                    {

                        UpComingEventsController.save(eventData.getId(), eventData.getName(), eventData.getDateB());

                        EventController.doLike(eventData.getId());

                        //ENABLE ALARM MANAGER TO START
                        EventController.addEventToNotify(eventData);
                        Toast.makeText(getApplicationContext(), R.string.event_participation_popup, Toast.LENGTH_SHORT).show();
                    }

                    initializeBtn();
                }

            }
        });

        _unparticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                if (eventData != null) {
                    if (EventController.isEventLiked(eventData.getId())) //CHECK IS THIS EVENT IS ALLREADY LIKED
                    {

                        UpComingEventsController.remove(eventData.getId());


                        EventController.doDisLike(eventData.getId());
                        Toast.makeText(getApplicationContext(), R.string.event_participation_canceled, Toast.LENGTH_SHORT).show();
                    }
                    initializeBtn();
                }
            }
        });

        new decodeHtml().execute(eventData.getDescription());

        setupGoogleMaps();

    }

    protected void getEventRealm() {

        int eid = 0;

        try {
            eid = getIntent().getExtras().getInt("id");
            //get it from external url (deep linking)
            if (eid == 0) {

                Intent appLinkIntent = getIntent();
                String appLinkAction = appLinkIntent.getAction();
                Uri appLinkData = appLinkIntent.getData();

                if (appLinkAction.equals(Intent.ACTION_VIEW)) {

                    if (APP_DEBUG)
                        Toast.makeText(getApplicationContext(), appLinkData.toString(), Toast.LENGTH_LONG).show();

                    eid = Utils.dp_get_id_from_url(appLinkData.toString(), "event");

                    if (APP_DEBUG)
                        Toast.makeText(getApplicationContext(), "The ID: " + eid + " " + appLinkAction, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //get it from external url (deep linking)


        eventData = EventController.findEventById(eid);
        mViewManager.showResult();

        if (eventData != null) {
            setupEventDataIntoViews();
        } else {
            syncEvent(eid);
        }

    }

    public void syncEvent(final int evnt_id) {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        mViewManager.loading();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_GET_EVENTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    mViewManager.showResult();

                    if (APP_DEBUG) {
                        Log.e("responseStoresString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);

                    //Log.e("response",response);

                    final EventParser mEventParser = new EventParser(jsonObject);
                    RealmList<Event> list = mEventParser.getEvents();

                    if (list.size() > 0) {
                        EventController.insertEvents(list);
                        eventData = list.get(0);
                        setupEventDataIntoViews();

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
                if (APP_DEBUG) {
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
                params.put("event_id", String.valueOf(evnt_id));


                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!MainActivity.isOpend()) {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();

        } else if (item.getItemId() == R.id.send_location) {


            double lat = eventData.getLat();
            double lon = eventData.getLng();
            String link = null;

            try {
                //https://www.google.com/maps/search/?api=1&query=47.5951518,-122.3316393
                link = "https://maps.google.com/?q=" + URLEncoder.encode(eventData.getAddress(), "UTF-8") + "&ll=" + String.format("%f,%f", lat, lon);
            } catch (UnsupportedEncodingException e) {
                link = "https://maps.google.com/?ll=" + String.format("%f,%f", lat, lon);
                e.printStackTrace();
            }

            @SuppressLint({"StringFormatInvalid", "LocalSuppress", "StringFormatMatches"}) String shared_text =
                    String.format(getString(R.string.shared_text),
                            eventData.getName(),
                            getString(R.string.app_name),
                            eventData.getLink()
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
        protected void onPostExecute(String text) {
            super.onPostExecute(text);
            description.setText(Html.fromHtml(text));
            //eventData.setDescription(text);
        }

        @Override
        protected String doInBackground(String... params) {

            return HtmlEscape.unescapeHtml(params[0]);
        }
    }

}
