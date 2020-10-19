package com.clubecerto.apps.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.fragments.ListStoresFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FavoriteStoreActivity extends AppCompatActivity {

    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_description)
    TextView toolbarDescription;
    @BindView(R.id.store_content)
    LinearLayout storeContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_store_favortie);
        ButterKnife.bind(this);

        initToolbar();

        toolbarTitle.setText(R.string.my_stores);

        Bundle bundle = new Bundle();

        bundle.putInt("fav", -1);

        ListStoresFragment fragment = new ListStoresFragment();
        fragment.setArguments(bundle);


        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .replace(R.id.store_content, fragment)
                .commit();


         /*Intent intent =  getIntent();
        String liked = intent.getStringExtra("eventNotified");
            if(liked != null)
            {

                int event_id = Integer.parseInt(liked);
                DatabaseAdapter database = new DatabaseAdapter(this);

                database.NotifiedEvent(event_id);
            }*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarDescription.setVisibility(View.GONE);

    }
}
