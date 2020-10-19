package com.clubecerto.apps.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Event;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.classes.Store;
import com.clubecerto.apps.app.controllers.events.EventController;
import com.clubecerto.apps.app.controllers.stores.OffersController;
import com.clubecerto.apps.app.controllers.stores.StoreController;
import com.clubecerto.apps.app.fragments.GalleryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryActivity extends AppCompatActivity {


    @BindView(R.id.toolbar_title)
    TextView APP_TITLE_VIEW;
    @BindView(R.id.toolbar_description)
    TextView APP_DESC_VIEW;
    @BindView(R.id.app_bar)
    Toolbar toolbar;


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        initToolbar();


        int int_id = 0;
        String type = "";
        try {

            int_id = getIntent().getExtras().getInt("int_id", 0);
            type = getIntent().getExtras().getString("type", "store");

            if (int_id == 0)
                finish();

            String iname = "";
            if (type.equals("store")) {
                Store store = StoreController.findStoreById(int_id);
                iname = store.getName();
            } else if (type.equals("offer")) {
                Offer offer = OffersController.findOfferById(int_id);
                iname = offer.getName();
            } else if (type.equals("event")) {
                Event event = EventController.findEventById(int_id);
                iname = event.getName();
            } else {
                finish();
            }

            APP_TITLE_VIEW.setText(getResources().getString(R.string.gallery));
            APP_DESC_VIEW.setText(iname);
            APP_TITLE_VIEW.setVisibility(View.VISIBLE);
            APP_DESC_VIEW.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }

        GalleryFragment frag = new GalleryFragment();
        frag.setParent_width(MainActivity.width);

        Bundle b = new Bundle();
        b.putInt("int_id", int_id);
        b.putString("type", type);
        frag.setArguments(b);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame, frag).commit();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_DESC_VIEW.setVisibility(View.GONE);
        //Utils.setFont(.+);
        //Utils.setFont(.+);
        String about = Constances.initConfig.AppInfos.ABOUT_CONTENT;
        APP_TITLE_VIEW.setText(R.string.gallery);
        APP_DESC_VIEW.setVisibility(View.GONE);

    }


}
