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
import androidx.fragment.app.FragmentTransaction;

import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.fragments.ListStoresFragment;
import com.clubecerto.apps.app.fragments.MainFragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;


public class ListStoresActivity extends AppCompatActivity {

    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_description)
    TextView toolbarDescription;
    @BindView(R.id.frame)
    LinearLayout frame;

    private Category mCat = null;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_list_activity);
        ButterKnife.bind(this);

        initToolbar();
        Realm realm = Realm.getDefaultInstance();

        try {
            userId = Objects.requireNonNull(getIntent().getExtras()).getInt("user_id");

        } catch (Exception e) {

        }

        try {

            int catId = Objects.requireNonNull(getIntent().getExtras()).getInt("category");
            mCat = realm.where(Category.class).equalTo("numCat", catId).findFirst();
            if (mCat != null) {
                toolbarTitle.setText(mCat.getNameCat());
            }
        } catch (Exception ex) {
            if (AppConfig.APP_DEBUG)
                Toast.makeText(this, "Error @655", Toast.LENGTH_LONG).show();

            if (AppConfig.APP_DEBUG)
                ex.printStackTrace();

            finish();
        }


        ListStoresFragment listFrag = new ListStoresFragment();


        Bundle b = new Bundle();
        try {
            b.putInt("category", mCat.getNumCat());
        } catch (Exception e) {

        }

        b.putInt("user_id", userId);
        listFrag.setArguments(b);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.frame, listFrag, MainFragment.TAG);
        // transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (android.R.id.home == item.getItemId()) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cat_menu, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
