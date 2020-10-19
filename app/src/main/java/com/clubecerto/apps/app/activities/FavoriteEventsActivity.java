package com.clubecerto.apps.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.fragments.ListEventFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FavoriteEventsActivity extends AppCompatActivity {

    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_description)
    TextView toolbarDescription;
    @BindView(R.id.event_content)
    LinearLayout eventContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_events_favorite);
        ButterKnife.bind(this);

        initToolbar();

        toolbarTitle.setText(getString(R.string.my_events));

        Bundle bundle = new Bundle();
        bundle.putInt("isLiked", 1);


        try {

            if (getIntent().hasExtra("upcomingEventsRequest")) {
                boolean upcomingEventsRequest = getIntent().getExtras().getBoolean("upcomingEventsRequest", false);
                if (upcomingEventsRequest) {
                    bundle.putInt("isLiked", 2);
                    toolbarTitle.setText(getString(R.string.myUpComingEvents));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ListEventFragment fragment = new ListEventFragment();
        fragment.setArguments(bundle);


        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .replace(R.id.event_content, fragment)
                .commit();

        Intent intent = getIntent();
        String liked = intent.getStringExtra("eventNotified");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        menu.findItem(R.id.search_icon).setVisible(false);

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarDescription.setVisibility(View.GONE);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


}
