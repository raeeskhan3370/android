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
import com.clubecerto.apps.app.fragments.NotificationFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NotificationActivity extends AppCompatActivity {

    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_description)
    TextView toolbarDescription;
    @BindView(R.id.notification_content)
    LinearLayout notificationContent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_notification);
        ButterKnife.bind(this);

        initToolbar();

        toolbarTitle.setText(getString(R.string.notification));

        Bundle bundle = new Bundle();

        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(bundle);


        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .replace(R.id.notification_content, fragment)
                .commit();

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


    @Override
    protected void onResume() {
        super.onResume();
    }


}
