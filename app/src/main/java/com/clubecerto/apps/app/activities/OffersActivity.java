package com.clubecerto.apps.app.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.classes.Store;
import com.clubecerto.apps.app.controllers.stores.StoreController;
import com.clubecerto.apps.app.fragments.ListOffersFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OffersActivity extends AppCompatActivity {

    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_description)
    TextView toolbarDescription;
    @BindView(R.id.store_content)
    LinearLayout storeContent;


    private int store_id = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_store_favortie);
        ButterKnife.bind(this);
        initToolbar();


        try {
            store_id = getIntent().getExtras().getInt("store_id");
            Toast.makeText(this, "b", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }


        Bundle bundle = new Bundle();
        bundle.putInt("store_id", store_id);

        ListOffersFragment fragment = new ListOffersFragment();
        fragment.setArguments(bundle);


        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .replace(R.id.store_content, fragment)
                .commit();

        Store store = StoreController.findStoreById(store_id);

        toolbarTitle.setText(R.string.offers);
        toolbarDescription.setText(store.getName());
        toolbarDescription.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void initToolbar() {

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setSubtitle("E-shop");
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_36dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarDescription = toolbar.findViewById(R.id.toolbar_description);
        //Utils.setFont(.+);
        //Utils.setFont(.+);

        toolbarDescription.setVisibility(View.GONE);

    }
}
