package com.clubecerto.apps.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.clubecerto.apps.app.GPS.GPStracker;
import com.clubecerto.apps.app.GPS.Position;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.controllers.CampagneController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.controllers.stores.OffersController;
import com.clubecerto.apps.app.fragments.SlideshowDialogFragment;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.OfferParser;
import com.clubecerto.apps.app.utils.DateUtils;
import com.clubecerto.apps.app.utils.OfferUtils;
import com.clubecerto.apps.app.utils.TextUtils;
import com.clubecerto.apps.app.utils.Utils;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.bluecabin.textoo.LinksHandler;
import org.bluecabin.textoo.Textoo;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.iwgang.countdownview.CountdownView;
import io.realm.Realm;
import io.realm.RealmList;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;
import static com.clubecerto.apps.app.appconfig.AppConfig.SHOW_ADS;
import static com.clubecerto.apps.app.appconfig.AppConfig.SHOW_ADS_IN_OFFER;

/**
 * Created by Droideve on 11/13/2017.
 */

public class OfferDetailActivity extends AppCompatActivity implements ViewManager.CustomView {

    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @BindView(R.id.progressBar)
    SpinKitView progressBar;
    @BindView(R.id.loading)
    LinearLayout loading;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.offer_label)
    TextView offer_label;
    @BindView(R.id.nbrPictures)
    TextView nbrPictures;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.distanceView)
    TextView distanceView;
    @BindView(R.id.priceView)
    TextView priceView;
    @BindView(R.id.detail_offer)
    TextView detailOffer;
    @BindView(R.id.offer_up_to)
    TextView offerUpTo;
    @BindView(R.id.storeBtn)
    TextView storeBtn;
    @BindView(R.id.storeBtnLayout)
    LinearLayout storeBtnLayout;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.ads)
    LinearLayout ads;
    @BindView(R.id.mScroll)
    ParallaxScrollView mScroll;

    @BindView(R.id.btn_custom_order)
    AppCompatButton btnCustomOrder;


    private int offer_id = 0;
    private ViewManager mViewManager;
    private Offer offerData;

    @Override
    protected void onResume() {

        if (adView != null)
            adView.resume();

        super.onResume();
    }

    @Override
    protected void onPause() {

        if (adView != null)
            adView.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (adView != null)
            adView.destroy();

        super.onDestroy();
    }

    private void toolbarTransactionScroll() {
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
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);
        ButterKnife.bind(this);

        setupToolbar();
        toolbarTransactionScroll();

        //ADD ADMOB BANNER
        if (SHOW_ADS && SHOW_ADS_IN_OFFER) {

            ads.setVisibility(View.VISIBLE);

            adView = findViewById(R.id.adView);
            adView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("4A55E4EA2535643C0D74A5A73C4F550A")
                    .addTestDevice("3CB74DFA141BF4D0823B8EA7D94531B5")
                    .build();
            adView.loadAd(adRequest);
        }

        //INIT VIEW MANAGER
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingLayout(findViewById(R.id.loading));
        mViewManager.setResultLayout(findViewById(R.id.content_offer));
        mViewManager.setErrorLayout(findViewById(R.id.error));
        mViewManager.setEmpty(findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);

        mViewManager.loading();


        try {


            //get it from external url (deep linking)
            if (offer_id == 0) {
                try {

                    Intent appLinkIntent = getIntent();
                    String appLinkAction = appLinkIntent.getAction();
                    Uri appLinkData = appLinkIntent.getData();

                    if (appLinkAction != null && appLinkAction.equals(Intent.ACTION_VIEW)) {

                        if (APP_DEBUG)
                            Toast.makeText(getApplicationContext(), appLinkData.toString(), Toast.LENGTH_LONG).show();
                        offer_id = Utils.dp_get_id_from_url(appLinkData.toString(), "offer");

                        if (APP_DEBUG)
                            Log.e("offer_id", offer_id + "");

                        if (APP_DEBUG)
                            Toast.makeText(getApplicationContext(), "The ID: " + offer_id + " " + appLinkAction, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (offer_id == 0) {
                offer_id = getIntent().getExtras().getInt("offer_id");
            }

            if (offer_id == 0) {
                offer_id = getIntent().getExtras().getInt("id");
            }


            if (offer_id == 0) {
                offer_id = Integer.parseInt(getIntent().getExtras().getString("id"));
            }

            if (APP_DEBUG)
                Toast.makeText(this, String.valueOf(offer_id), Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        final Offer mOffer = OffersController.findOfferById(offer_id);
        if (mOffer != null && mOffer.isLoaded() && mOffer.isValid()) {

            mViewManager.showResult();
            putInsideViews(mOffer);
            offerData = mOffer;
        } else {
            getOffer(offer_id);
        }


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (offerData != null && offerData.getListImages().size() > 0) {
                    SlideshowDialogFragment.newInstance().show(OfferDetailActivity.this, offerData.getListImages(), 0);
                }
            }
        });


        /*
         *
         *   DATE & COUNTDOWN
         *
         */

        String date = "";


        try {
            date = mOffer.getDate_start();
            date = DateUtils.prepareOutputDate(date, "dd MMMM yyyy  hh:mm", this);
        } catch (Exception e) {

            getOffer(offer_id);
            return;

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
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

    private void putInsideViews(final Offer mOffer) {

        toolbarTitle.setText(mOffer.getName());
        offer_label.setText(mOffer.getName());


        if (mOffer.getStore_id() > 0) {

            Drawable storeDrawable = new IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_map_marker)
                    .color(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                    .sizeDp(18);


            GPStracker mGPS = new GPStracker(this);
            Position newPosition = new Position();
            Double mDistance = newPosition.distance(mGPS.getLatitude(), mGPS.getLongitude(), mOffer.getLat(), mOffer.getLng());

            String disStr = Utils.preparDistance(mDistance)
                    + " " +
                    Utils.getDistanceBy(mDistance).toLowerCase();


            if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
                distanceView.setVisibility(View.GONE);
            } else
                distanceView.setVisibility(View.VISIBLE);

            distanceView.setText(
                    String.format(getString(R.string.offerIn), disStr)
            );


            storeBtn.setText(mOffer.getStore_name());
            storeBtn.setCompoundDrawables(storeDrawable, null, null, null);
            storeBtn.setCompoundDrawablePadding(20);
            storeBtn.setPaintFlags(storeBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            storeBtnLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {

                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();

                        if (!StoreDetailActivity.isOpend()) {
                            Intent intent = new Intent(OfferDetailActivity.this, StoreDetailActivity.class);
                            intent.putExtra("id", mOffer.getStore_id());
                            startActivity(intent);
                        }

                        realm.commitTransaction();

                    } catch (Exception e) {
                        if (APP_DEBUG)
                            e.printStackTrace();

                        Toast.makeText(OfferDetailActivity.this, getString(R.string.store_not_found), Toast.LENGTH_LONG).show();

                    }

                }
            });

            storeBtnLayout.setVisibility(View.VISIBLE);
        } else
            storeBtnLayout.setVisibility(View.GONE);


        if (mOffer.getListImages() != null && mOffer.getListImages().size() > 1) {

            Drawable camera = new IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_camera)
                    .color(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
                    .sizeDp(12);

            nbrPictures.setText(mOffer.getListImages().size() + "");
            nbrPictures.setCompoundDrawables(camera, null, null, null);
            nbrPictures.setCompoundDrawablePadding(10);

        } else {
            nbrPictures.setVisibility(View.GONE);
        }


        if (mOffer.getValue_type().equalsIgnoreCase("Percent") && (mOffer.getOffer_value() > 0 || mOffer.getOffer_value() < 0)) {
            DecimalFormat decimalFormat = new DecimalFormat("#0");

            priceView.setText(
                    String.format(getString(R.string.offer_off),
                            decimalFormat.format(mOffer.getOffer_value()) + "%"
                    )
            );

            priceView.setVisibility(View.VISIBLE);


        } else {

            if (mOffer.getValue_type().equalsIgnoreCase("Price") && mOffer.getOffer_value() != 0) {


                priceView.setText(
                        String.format(getString(R.string.offer_for),
                                OfferUtils.parseCurrencyFormat(
                                        mOffer.getOffer_value(),
                                        mOffer.getCurrency())
                        )
                );


                priceView.setVisibility(View.VISIBLE);

            } else {
                priceView.setVisibility(View.GONE);
                priceView.setText(getString(R.string.promo));
            }
        }

        if (mOffer.getImages() != null)
            Glide.with(this)
                    .load(mOffer.getImages().getUrl500_500())
                    .placeholder(ImageLoaderAnimation.glideLoader(this))
                    .centerCrop().into(image);


        detailOffer.setText(mOffer.getDescription());
        new TextUtils.decodeHtml(detailOffer).execute(mOffer.getDescription());

        Textoo
                .config(detailOffer)
                .linkifyWebUrls()  // or just .linkifyAll()
                .addLinksHandler(new LinksHandler() {
                    @Override
                    public boolean onClick(View view, String url) {

                        if (Utils.isValidURL(url)) {

                            new AwesomeWebView.Builder(OfferDetailActivity.this)
                                    .showMenuOpenWith(false)
                                    .statusBarColorRes(R.color.colorAccent)
                                    .theme(R.style.FinestWebViewAppTheme)
                                    .titleColor(
                                            ResourcesCompat.getColor(getResources(), R.color.defaultWhiteColor, null)
                                    ).urlColor(
                                    ResourcesCompat.getColor(getResources(), R.color.defaultWhiteColor, null)
                            ).show(url);

                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .apply();

        try {

            if (getIntent().hasExtra("cid")) {
                int cid = Integer.parseInt(getIntent().getExtras().getString("cid"));
                CampagneController.markView(cid);
            }
        } catch (Exception e) {
            if (APP_DEBUG)
                e.printStackTrace();
        }


        Drawable storeDrawable = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_calendar)
                .color(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                .sizeDp(18);
        offerUpTo.setCompoundDrawables(storeDrawable, null, null, null);
        offerUpTo.setCompoundDrawablePadding(20);


        setupCountDown(mOffer);


        if (mOffer.getOrder_enabled() == 1) {
            if (mOffer.getOrder_button() != null) {
                btnCustomOrder.setText(mOffer.getOrder_button().toUpperCase());
            }
            btnCustomOrder.setVisibility(View.VISIBLE);

            btnCustomOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SessionsController.isLogged()) {
                        Intent intent = new Intent(new Intent(OfferDetailActivity.this, OrderCheckoutActivity.class));
                        intent.putExtra("offer_id", mOffer.getId());
                        startActivity(intent);
                        overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    } else {
                        Intent intent = new Intent(OfferDetailActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            });
        }

    }

    private void setupCountDown(Offer mOffer) {

        String dateStartAt = "";
        String dateEndAt = "";


        try {
            dateStartAt = mOffer.getDate_start();
            dateStartAt = DateUtils.prepareOutputDate(dateStartAt, "dd MMMM yyyy", this);
        } catch (Exception e) {
            return;
        }

        try {
            dateEndAt = mOffer.getDate_end();
            dateEndAt = DateUtils.prepareOutputDate(dateEndAt, "dd MMMM yyyy", this);
        } catch (Exception e) {
            return;
        }


        String inputDateSatrt = DateUtils.prepareOutputDate(mOffer.getDate_start(), "yyyy-MM-dd HH:mm:ss", this);
        long diff_Will_Start = DateUtils.getDiff(inputDateSatrt, "yyyy-MM-dd HH:mm:ss");

        if (APP_DEBUG) {

            Log.e("_start_at_server", mOffer.getDate_start());
            Log.e("_start_at_device ", dateStartAt);
            Log.e("_start_at_diff ", String.valueOf(diff_Will_Start));
        }

        if (diff_Will_Start > 0) {

            if (mOffer.getIs_deal() == 1) {
                CountdownView mCvCountdownView = (CountdownView) findViewById(R.id.cv_countdownViewTest1);
                mCvCountdownView.setVisibility(View.VISIBLE);
                mCvCountdownView.start(diff_Will_Start); // Millisecond
            }

            offerUpTo.setText(String.format(getString(R.string.offer_start_at), dateStartAt));

        }


        String inputDateEnd = DateUtils.prepareOutputDate(mOffer.getDate_end(), "yyyy-MM-dd HH:mm:ss", this);
        long diff_will_end = DateUtils.getDiff(inputDateEnd, "yyyy-MM-dd HH:mm:ss");


        if (APP_DEBUG) {
            Log.e("_end_at_server", mOffer.getDate_end());
            Log.e("_end_at_device ", dateEndAt);
            Log.e("_end_at_diff ", String.valueOf(diff_will_end));
        }

        if (diff_will_end > 0 && diff_Will_Start < 0) {


            if (mOffer.getIs_deal() == 1) {
                CountdownView mCvCountdownView = (CountdownView) findViewById(R.id.cv_countdownViewTest1);
                mCvCountdownView.setVisibility(View.VISIBLE);
                mCvCountdownView.start(diff_will_end); // Millisecond
            }

            offerUpTo.setText(String.format(getString(R.string.offer_end_at), dateEndAt));

        }


        if (diff_Will_Start < 0 && diff_will_end < 0) {
            offerUpTo.setText(String.format(getString(R.string.offer_ended_at), dateEndAt));
        }


    }


    public void setupToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.store_title_detail);

    }


    public void getOffer(final int offer_id) {

        mViewManager.loading();

        final GPStracker mGPS = new GPStracker(this);

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_GET_OFFERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                mViewManager.showResult();


                try {

                    if (APP_DEBUG) {
                        Log.e("responseOffersString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    final OfferParser mOfferParser = new OfferParser(jsonObject);
                    RealmList<Offer> list = mOfferParser.getOffers();

                    if (list.size() > 0) {

                        OffersController.insertOffers(list);

                        putInsideViews(list.get(0));

                    } else {

                        Toast.makeText(OfferDetailActivity.this, getString(R.string.store_not_found), Toast.LENGTH_LONG).show();
                        finish();

                    }

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
                mViewManager.error();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                if (mGPS.canGetLocation()) {
                    params.put("lat", mGPS.getLatitude() + "");
                    params.put("lng", mGPS.getLongitude() + "");
                }

                params.put("limit", "1");
                params.put("offer_id", offer_id + "");

                if (APP_DEBUG) {
                    Log.e("ListStoreFragment", "  params getOffers :" + params.toString());
                }

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(this).getRequestQueue().add(request);

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
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            if (!MainActivity.isOpend()) {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }

        if (item.getItemId() == R.id.send_location) {
            {
                @SuppressLint({"StringFormatInvalid", "LocalSuppress", "StringFormatMatches"}) String shared_text =
                        String.format(getString(R.string.shared_text),
                                offerData.getName(),
                                getString(R.string.app_name),
                                offerData.getLink()
                        );


                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shared_text);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        if (!MainActivity.isOpend()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {

        }

        super.onBackPressed();
    }

}
