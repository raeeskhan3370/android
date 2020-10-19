package com.clubecerto.apps.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.Services.BusStation;
import com.clubecerto.apps.app.Services.GenericNotifyEvent;
import com.clubecerto.apps.app.adapter.ListExpand.AdapterListExpand;
import com.clubecerto.apps.app.adapter.lists.CategoriesListAdapter;
import com.clubecerto.apps.app.animation.LineItemDecoration;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.classes.Order;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.controllers.categories.CategoryController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.OrderParser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;


public class OrdersActivity extends AppCompatActivity implements CategoriesListAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ViewManager.CustomView {

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

    private AdapterListExpand mAdapter;
    private RequestQueue queue;
    private ViewManager mViewManager;

    //pager
    private int COUNT = 0;
    private int REQUEST_PAGE = 1;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private LinearLayoutManager mLayoutManager;
    private boolean loading = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_categories);
        ButterKnife.bind(this);

        initToolbar();


        initComponent();


        setupRefresListener();


        setupViewManager();


        loadOrdersFromServer();


    }


    private void loadOrdersFromServer() {

        //load data from apis
        if (ServiceHandler.isNetworkAvailable(this)) {
            //open a specific order in the list
            if (getIntent().hasExtra("id")) {
                getOrdersFromApi(REQUEST_PAGE, getIntent().getExtras().getInt("id"));
            } else {
                getOrdersFromApi(REQUEST_PAGE, -1);
            }

        } else {
            refresh.setRefreshing(false);
            Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_LONG).show();
            if (mAdapter.getItemCount() == 0)
                mViewManager.error();
        }
    }


    private void setupViewManager() {
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingLayout(findViewById(R.id.loading));
        mViewManager.setResultLayout(findViewById(R.id.content_my_orders));
        mViewManager.setErrorLayout(findViewById(R.id.error));
        mViewManager.setEmpty(findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);
    }

    private void setupRefresListener() {
        refresh.setOnRefreshListener(this);
        refresh.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent
        );
    }


    private void initComponent() {

        mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);
        list.addItemDecoration(new LineItemDecoration(this, LinearLayout.VERTICAL));
        list.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterListExpand(this, new ArrayList<>());
        list.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListExpand.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Order obj, int position) {

            }

        });


        list.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();

                if (loading) {

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;

                        if (ServiceHandler.isNetworkAvailable(OrdersActivity.this)) {
                            if (COUNT > mAdapter.getItemCount())
                                getOrdersFromApi(REQUEST_PAGE, -1);
                        } else {
                            Toast.makeText(OrdersActivity.this, "Network not available ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });


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


    // This method will be called when a Notification is posted (in the UI thread for Toast)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GenericNotifyEvent event) {
        //refresh notification list when the product is deleted
        if (event.message != null && event.message.equals("order_updated")) {
            loadOrdersFromServer();
            event.message = null;
        }
    }

    @Override
    protected void onStart() {
        BusStation.getBus().register(this);
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusStation.getBus().unregister(this);

    }

    @Override
    public void itemClicked(View view, final int position) {
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
        toolbarTitle.setText(R.string.orders);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarDescription.setVisibility(View.GONE);

    }


    @Override
    public void onRefresh() {
                loadOrdersFromServer();
    }


    private void getOrdersFromApi(final int page, final int order_id) {

        refresh.setRefreshing(true);
        queue = VolleySingleton.getInstance(this).getRequestQueue();

        final User mUser = SessionsController.getSession().getUser();
        final int user_id = mUser.getId();


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_ORDERS_GET, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                refresh.setRefreshing(false);

                try {

                    if (APP_DEBUG) {
                        Log.e("catsResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final OrderParser mCategoryParser = new OrderParser(jsonObject);
                    int success = Integer.parseInt(mCategoryParser.getStringAttr(Tags.SUCCESS));
                    COUNT = 0;
                    COUNT = mCategoryParser.getIntArg(Tags.COUNT);

                    if (success == 1) {
                        RealmList<Order> list = mCategoryParser.getOrders();

                        if (page == 1) {
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    mAdapter.removeAll();
                                    for (int i = 0; i < list.size(); i++) {
                                        mAdapter.addItem(list.get(i));
                                    }


                                    if (COUNT > mAdapter.getItemCount())
                                        REQUEST_PAGE++;
                                    if (COUNT == 0 || mAdapter.getItemCount() == 0) {

                                    }

                                    if (APP_DEBUG) {
                                        Log.e("count ", COUNT + " page = " + page);
                                    }
                                }
                            }, 800);


                        } else {
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    for (int i = 0; i < list.size(); i++) {
                                        mAdapter.addItem(list.get(i));
                                    }


                                    if (COUNT > mAdapter.getItemCount())
                                        REQUEST_PAGE++;
                                    if (COUNT == 0 || mAdapter.getItemCount() == 0) {

                                    }

                                    if (APP_DEBUG) {
                                        Log.e("count ", COUNT + " page = " + page);
                                    }
                                }
                            }, 800);

                        }


                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                }


                refresh.setRefreshing(false);


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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();


                if (order_id > 0) params.put("order_id", String.valueOf(order_id));
                params.put("user_id", String.valueOf(user_id));
                params.put("page", String.valueOf(page));
                params.put("limit", "30");


                if (APP_DEBUG) {
                    Log.e("ListOrdersActivity", "  params getOrders :" + params.toString());
                }

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


    @Override
    public void customErrorView(View v) {

        Button retry = v.findViewById(R.id.btn);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrdersFromApi(REQUEST_PAGE, -1);
                REQUEST_PAGE = 1;
                mViewManager.loading();
            }
        });

    }

    @Override
    public void customLoadingView(View v) {
    }

    @Override
    public void customEmptyView(View v) {

        Button btn = v.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewManager.loading();
                getOrdersFromApi(1, -1);
                REQUEST_PAGE = 1;
            }
        });


    }
}
