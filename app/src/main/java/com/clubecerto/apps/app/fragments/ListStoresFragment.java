package com.clubecerto.apps.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.GPS.GPStracker;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.MainActivity;
import com.clubecerto.apps.app.activities.StoreDetailActivity;
import com.clubecerto.apps.app.adapter.lists.StoreListAdapter;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.classes.Store;
import com.clubecerto.apps.app.controllers.ErrorsController;
import com.clubecerto.apps.app.controllers.stores.StoreController;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.StoreParser;
import com.clubecerto.apps.app.parser.tags.Tags;
import com.clubecerto.apps.app.utils.DateUtils;
import com.clubecerto.apps.app.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class ListStoresFragment extends Fragment
        implements StoreListAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ViewManager.CustomView {

    public ViewManager mViewManager;
    //loading
    public SwipeRefreshLayout swipeRefreshLayout;
    //for scrolling params
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    LinearLayoutManager mLayoutManager;
    private int listType = 1;
    private RecyclerView list;
    private StoreListAdapter adapter;
    //init request http
    private RequestQueue queue;
    private boolean loading = true;
    //pager
    private int COUNT = 0;
    private int REQUEST_PAGE = 1;
    private Category mCat;
    private GPStracker mGPS;
    private List<Store> listStores = new ArrayList<>();
    private int CatId;


    private int REQUEST_RANGE_RADIUS = -1;
    private String REQUEST_SEARCH = "";
    private int REQUEST_CATEGORY = -1;
    private LatLng LOCATION = null;
    private int Fav = 0;
    private int owner_id = 0;

    private AppCompatButton clearSearchBtn;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        REQUEST_RANGE_RADIUS = Integer.parseInt(getResources().getString(R.string.distance_max_display_route));
    }

    private void updateBadges() {
        MainActivity.updateMessengerBadge(getActivity());
        MainActivity.updateNotificationBadge(getActivity());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if ((CatId > 0 || Fav == -1)) return;

        menu.findItem(R.id.list_view_icon).setVisible(false);
        menu.findItem(R.id.search_icon).setVisible(true);
        menu.findItem(R.id.notification_action).setVisible(true);


        updateBadges();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.search_icon) {

            SearchDialog.newInstance(getActivity()).setOnSearchListener(new SearchDialog.Listener() {
                @Override
                public void onSearchClicked(SearchDialog mSearchDialog, String value, int radius, int category, LatLng location) {

                    if (mSearchDialog.isShowing())
                        mSearchDialog.dismiss();

                    if (AppConfig.APP_DEBUG)
                        Toast.makeText(getContext(), value + " " + radius, Toast.LENGTH_LONG).show();

                    REQUEST_RANGE_RADIUS = radius;
                    REQUEST_SEARCH = value;
                    REQUEST_PAGE = 1;
                    REQUEST_CATEGORY = category;
                    LOCATION = location;
                    getStores(1);

                    //display clear button
                    clearSearchBtn.setVisibility(View.VISIBLE);

                }
            }).setHeader(getString(R.string.searchOnStores)).showDialog();

        } else if (item.getItemId() == R.id.list_view_icon) {

            RecyclerView.LayoutManager mLayoutManager;
            if (Utils.listViewFormat("list_view_format_stores") == 1) {
                mLayoutManager = new GridLayoutManager(getActivity(), 2);
                item.setIcon(R.drawable.ic_view_list_default);
                Utils.setListViewFormat("list_view_format_stores", 2);
            } else {
                mLayoutManager = new GridLayoutManager(getActivity(), 1);
                item.setIcon(R.drawable.ic_list_view_grid);
                Utils.setListViewFormat("list_view_format_stores", 1);
            }

            list.setLayoutManager(mLayoutManager);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_store_list, container, false);

        try {

            owner_id = getArguments().getInt("user_id");
        } catch (Exception e) {
        }


        try {

            CatId = getArguments().getInt("category");
            //load from assets
           /*if(!ServiceHandler.isNetworkAvailable(getActivity())){

               if(Constances.ENABLE_OFFLINE)
                listStores = loader.parseStoresFromAssets(getActivity(),mCat.getNumCat());
           }*/

        } catch (Exception e) {

            e.printStackTrace();
        }

        queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();

        mViewManager = new ViewManager(getActivity());
        mViewManager.setLoadingLayout(rootView.findViewById(R.id.loading));
        mViewManager.setResultLayout(rootView.findViewById(R.id.content_my_store));
        mViewManager.setErrorLayout(rootView.findViewById(R.id.error));
        mViewManager.setEmpty(rootView.findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);

        list = rootView.findViewById(R.id.list);

        adapter = new StoreListAdapter(getActivity(), listStores);
        adapter.setClickListener(this);


        list.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Utils.listViewFormat("list_view_format_stores"));

        //mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        list.setItemAnimator(new DefaultItemAnimator());
        list.setLayoutManager(mLayoutManager);
        list.setAdapter(adapter);


        list.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();

                if (loading) {

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;

                        if (ServiceHandler.isNetworkAvailable(getContext())) {
                            if (COUNT > adapter.getItemCount())
                                getStores(REQUEST_PAGE);
                        } else {
                            Toast.makeText(getContext(), "Network not available ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });


        swipeRefreshLayout = rootView.findViewById(R.id.refresh);

        swipeRefreshLayout.setOnRefreshListener(this);


        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent
        );


        if (ServiceHandler.isNetworkAvailable(this.getActivity())) {
            getStores(REQUEST_PAGE);
        } else {

            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), getString(R.string.check_network), Toast.LENGTH_LONG).show();
            if (adapter.getItemCount() == 0)
                mViewManager.error();
        }


        clearSearchBtn = rootView.findViewById(R.id.clear_search);
        clearSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefresh();
                clearSearchBtn.setVisibility(View.GONE);
            }
        });


        return rootView;
    }

    @Override
    public void itemClicked(View view, int position) {


        Store store = adapter.getItem(position);

        if (store != null) {


            if (APP_DEBUG)
                Log.e("_1_store_id", String.valueOf(store.getId()));

            Intent intent = new Intent(getActivity(), StoreDetailActivity.class);
            intent.putExtra("id", store.getId());
            startActivity(intent);
        }

    }

    public void getStores(final int page) {

        swipeRefreshLayout.setRefreshing(true);

        mGPS = new GPStracker(getActivity());
        if (adapter.getItemCount() == 0)
            mViewManager.loading();

        //IF ther's no category in the db then go to the home page ( 0 )
        Realm realm = Realm.getDefaultInstance();
        mCat = realm.where(Category.class).equalTo("numCat", CatId).findFirst();
        final int numCat = mCat != null ? mCat.getNumCat() : Constances.initConfig.Tabs.HOME;

        final String strIds = StoreController.getSavedStoresAsString();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_GET_STORES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("responseStoresString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);

                    //Log.e("response",response);

                    final StoreParser mStoreParser = new StoreParser(jsonObject);
                    // List<Store> list = mStoreParser.getEventRealm();
                    COUNT = 0;
                    COUNT = mStoreParser.getIntArg(Tags.COUNT);
                    mViewManager.showResult();


                    //check server permission and display the errors
                    if (mStoreParser.getSuccess() == -1) {
                        ErrorsController.serverPermissionError(getActivity());
                    }

                    if (page == 1) {

                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RealmList<Store> list = mStoreParser.getStore();

                                if (list.size() > 0) {
                                    StoreController.insertStores(list);
                                }
                                adapter.removeAll();
                                for (int i = 0; i < list.size(); i++) {
                                    Store sTr = list.get(i);
                                    if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
                                        sTr.setDistance((double) 0);
                                    }
                                    //if (list.get(i).getDistance() <=REQUEST_RANGE_RADIUS)
                                    adapter.addItem(sTr);


                                }

                                loading = true;

                                mViewManager.showResult();

                                if (COUNT > adapter.getItemCount())
                                    REQUEST_PAGE++;
                                if (COUNT == 0 || adapter.getItemCount() == 0) {
                                    mViewManager.empty();
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
                                RealmList<Store> list = mStoreParser.getStore();

                                mGPS = new GPStracker(getContext());
                                if (list.size() > 0) {
                                    StoreController.insertStores(list);
                                }

                                for (int i = 0; i < list.size(); i++) {

                                    Store sTr = list.get(i);
                                    if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
                                        sTr.setDistance((double) 0);
                                    }
                                    //if (list.get(i).getDistance() <=REQUEST_RANGE_RADIUS)
                                    adapter.addItem(sTr);

                                }
                                mViewManager.showResult();
                                loading = true;
                                if (COUNT > adapter.getItemCount())
                                    REQUEST_PAGE++;

                                if (COUNT == 0 || adapter.getItemCount() == 0) {
                                    mViewManager.empty();
                                }
                            }
                        }, 800);

                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    if (APP_DEBUG)
                        e.printStackTrace();

                    if (adapter.getItemCount() == 0)
                        mViewManager.error();


                }


                swipeRefreshLayout.setRefreshing(false);

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

                if (LOCATION != null) {
                    params.put("latitude", LOCATION.latitude + "");
                    params.put("longitude", LOCATION.longitude + "");
                } else if (mGPS.canGetLocation()) {
                    params.put("latitude", mGPS.getLatitude() + "");
                    params.put("longitude", mGPS.getLongitude() + "");
                }

                //  params.put("token", Utils.getToken(getActivity()));


                if (REQUEST_RANGE_RADIUS > -1) {
                    if (REQUEST_RANGE_RADIUS <= 99)
                        params.put("radius", String.valueOf((REQUEST_RANGE_RADIUS * 1024)));
                }

                params.put("limit", "30");

                if (owner_id > 0)
                    params.put("user_id", String.valueOf(owner_id));

                if (Fav == -1) {
                    if (!strIds.equals(""))
                        params.put("store_ids", strIds);
                    else {
                        params.put("store_ids", "0");
                    }
                } else {
                    if (numCat == Constances.initConfig.Tabs.BOOKMAKRS) {

                        if (!strIds.equals(""))
                            params.put("store_ids", strIds);
                        else {
                            params.put("store_ids", "0");
                        }
                    }
                    if (numCat == Constances.initConfig.Tabs.MOST_RATED) {

                        params.put("order_by", String.valueOf(Constances.initConfig.Tabs.MOST_RATED));

                    }
                    if (numCat == Constances.initConfig.Tabs.MOST_RECENT) {
                        params.put("order_by", String.valueOf(Constances.initConfig.Tabs.MOST_RECENT));

                    } else if (numCat == Constances.initConfig.Tabs.HOME) {

                    } else {
                        params.put("category_id", numCat + "");
                    }

                }

                if (REQUEST_CATEGORY > -1) params.put("category_id", REQUEST_CATEGORY + "");


                params.put("page", page + "");
                params.put("search", REQUEST_SEARCH);
                params.put("current_date", DateUtils.getUTC("yyyy-MM-dd H:m:s"));
                params.put("current_tz", TimeZone.getDefault().getID());
                params.put("order_by", Constances.OrderByFilter.NEARBY);


                if (APP_DEBUG) {
                    Log.e("ListStoreFragment", "  params getStores :" + params.toString());
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
    public void onRefresh() {
        mGPS = new GPStracker(getActivity());
        if (mGPS.canGetLocation()) {
            swipeRefreshLayout.setRefreshing(true);
            REQUEST_SEARCH = "";
            REQUEST_PAGE = 1;
            REQUEST_RANGE_RADIUS = -1;
            REQUEST_CATEGORY = -1;
            LOCATION = null;
            getStores(1);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            mGPS.showSettingsAlert();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            //Toast.makeText(getActivity(), "  is Liked  :"+args.get("isLiked"), Toast.LENGTH_LONG).show();
            Fav = args.getInt("fav");

        }
    }

    @Override
    public void customErrorView(View v) {

        Button retry = v.findViewById(R.id.btn);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGPS = new GPStracker(getActivity());

                if (!mGPS.canGetLocation() && listType == 1)
                    mGPS.showSettingsAlert();

                getStores(1);
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
                getStores(1);
                REQUEST_PAGE = 1;
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.clear();
            if (CatId > 0 || Fav == -1) inflater.inflate(R.menu.cat_menu, menu);
            else {
                inflater.inflate(R.menu.home_menu, menu);
                if (Utils.listViewFormat("list_view_format_stores") == 1) {
                    menu.findItem(R.id.list_view_icon).setIcon(R.drawable.ic_list_view_grid);

                } else {
                    menu.findItem(R.id.list_view_icon).setIcon(R.drawable.ic_view_list_default);
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater);


    }
}
