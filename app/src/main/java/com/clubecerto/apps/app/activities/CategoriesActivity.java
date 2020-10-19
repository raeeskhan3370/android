package com.clubecerto.apps.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.adapter.lists.CategoriesListAdapter;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.controllers.categories.CategoryController;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.CategoryParser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;


public class CategoriesActivity extends AppCompatActivity implements CategoriesListAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener {

    //GET CATEGORIES FROM  DATABASE
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_description)
    TextView toolbarDescription;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;

    private CategoriesListAdapter adapter;
    private RequestQueue queue;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_categories);
        ButterKnife.bind(this);

        initToolbar();

        toolbarTitle.setText(R.string.categories);

        list = findViewById(R.id.list);
        list.setVisibility(View.VISIBLE);

        adapter = new CategoriesListAdapter(this, new ArrayList<>());


        list.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(mLayoutManager);
        list.setAdapter(adapter);

        adapter.setClickListener(this);


        refresh.setOnRefreshListener(this);


        refresh.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent
        );


        getCatsFromAPIs();


        ((SimpleItemAnimator) list.getItemAnimator()).setSupportsChangeAnimations(false);

    }

    public List<Category> getData() {

        List<Category> results = new ArrayList<>();

        RealmList<Category> listCats = CategoryController.list();

        for (Category cat : listCats) {
            if (cat.getNumCat() > 0)
                results.add(cat);
        }


        return results;
    }

    @Override
    protected void onStart() {
        super.onStart();

        //getCatsFromAPIs data from apis
        //getCatsFromAPIs();
    }

    @Override
    public void itemClicked(View view, final int position) {

        Intent intent = new Intent(this, ListStoresActivity.class);
        intent.putExtra("category", adapter.getItem(position).getNumCat());
        overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
            overridePendingTransition(R.anim.righttoleft_enter, R.anim.righttoleft_exit);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.righttoleft_enter, R.anim.righttoleft_exit);

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
    public void onRefresh() {
        getCatsFromAPIs();
    }


    private void getCatsFromAPIs() {

        if (!ServiceHandler.isNetworkAvailable(this)) {
            if (CategoryController.list().size() == 0) {
                //database.insertCats(getCatsFromAPIser.parseCategoriesFromAssets(this));
            }
        }

        queue = VolleySingleton.getInstance(this).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_USER_GET_CATEGORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                refresh.setRefreshing(false);

                try {

                    if (APP_DEBUG) {
                        Log.e("catsResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final CategoryParser mCategoryParser = new CategoryParser(jsonObject);
                    int success = Integer.parseInt(mCategoryParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1) {

                        RealmList<Category> list = mCategoryParser.getCategories();

                        adapter.clear();

                        for (int i = 0; i < list.size(); i++) {
                            adapter.addItem(list.get(i));
                        }

                        CategoryController.insertCategories(list);

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

                refresh.setRefreshing(false);

            }


        }) {


        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


}
