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
import com.clubecerto.apps.app.activities.OfferDetailActivity;
import com.clubecerto.apps.app.adapter.lists.OfferListAdapter;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.controllers.ErrorsController;
import com.clubecerto.apps.app.controllers.stores.OffersController;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.OfferParser;
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

import io.realm.RealmList;

import static com.clubecerto.apps.app.R.layout.fragment_offers_list;
import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class ListOffersFragment extends Fragment
        implements OfferListAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ViewManager.CustomView, View.OnClickListener {

    public ViewManager mViewManager;
    //loading
    public SwipeRefreshLayout swipeRefreshLayout;
    //for scrolling params
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    private LinearLayoutManager mLayoutManager;
    private int listType = 1;
    private RecyclerView list;
    private OfferListAdapter adapter;
    //init request http
    private RequestQueue queue;
    private boolean loading = true;
    //pager
    private int COUNT = 0;
    private int REQUEST_PAGE = 1;
    private GPStracker mGPS;
    private List<Offer> listStores = new ArrayList<>();
    private int REQUEST_RANGE_RADIUS = -1;
    private String REQUEST_SEARCH = "";
    private int REQUEST_CATEGORY = -1;
    private LatLng LOCATION = null;

    private AppCompatButton clearSearchBtn;

    private String current_date;

    private int store_id = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        REQUEST_RANGE_RADIUS = Integer.parseInt(getResources().getString(R.string.distance_max_display_route));

        current_date = DateUtils.getUTC("yyyy-MM-dd H:m:s");

    }


    private void updateBadges() {
        MainActivity.updateMessengerBadge(getActivity());
        MainActivity.updateNotificationBadge(getActivity());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.list_view_icon).setVisible(false);
        menu.findItem(R.id.search_icon).setVisible(true);
        menu.findItem(R.id.notification_action).setVisible(true);

        super.onPrepareOptionsMenu(menu);
        updateBadges();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.search_icon) {

            SearchDialog.newInstance(getActivity()).setOnSearchListener(new SearchDialog.Listener() {
                @Override
                public void onSearchClicked(SearchDialog mSearchDialog, String value, int radius, int cat, LatLng location) {

                    if (mSearchDialog.isShowing())
                        mSearchDialog.dismiss();

                    if (AppConfig.APP_DEBUG)
                        Toast.makeText(getContext(), value + " " + radius, Toast.LENGTH_LONG).show();

                    REQUEST_RANGE_RADIUS = radius;
                    REQUEST_SEARCH = value;
                    REQUEST_PAGE = 1;
                    REQUEST_CATEGORY = cat;
                    LOCATION = location;
                    getOffers(1);

                    //display clear button
                    clearSearchBtn.setVisibility(View.VISIBLE);

                }
            }).setHeader(getString(R.string.searchOnOffers)).showDialog();

        } else if (item.getItemId() == R.id.list_view_icon) {

            final RecyclerView.LayoutManager mLayoutManager;
            if (Utils.listViewFormat("list_view_format_offers") == 1) {
                mLayoutManager = new GridLayoutManager(getActivity(), 2);
                item.setIcon(R.drawable.ic_view_list_default);
                Utils.setListViewFormat("list_view_format_offers", 2);
            } else {
                mLayoutManager = new GridLayoutManager(getActivity(), 1);
                item.setIcon(R.drawable.ic_list_view_grid);
                Utils.setListViewFormat("list_view_format_offers", 1);
            }

            list.setLayoutManager(mLayoutManager);

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(fragment_offers_list, container, false);


        try {
            store_id = getArguments().getInt("store_id");
        } catch (Exception e) {
            store_id = 0;
        }

        try {
            //load from assets
           /*if(!ServiceHandler.isNetworkAvailable(getActivity())){

               if(Constances.ENABLE_OFFLINE)
                listStores = loader.parseStoresFromAssets(getActivity(),mCat.getNumCat());
           }*/

        } catch (Exception e) {

            e.printStackTrace();
        }

        mGPS = new GPStracker(getActivity());
        queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();

        mViewManager = new ViewManager(getActivity());
        mViewManager.setLoadingLayout(rootView.findViewById(R.id.loading));
        mViewManager.setResultLayout(rootView.findViewById(R.id.content_my_store));
        mViewManager.setErrorLayout(rootView.findViewById(R.id.error));
        mViewManager.setEmpty(rootView.findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);


        list = rootView.findViewById(R.id.list);

        adapter = new OfferListAdapter(getActivity(), listStores, false);
        adapter.setClickListener(this);

        list.setHasFixedSize(true);

        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Utils.listViewFormat("list_view_format_offers"));
        list.setLayoutManager(mLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(adapter);

//
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
                                getOffers(REQUEST_PAGE);
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
            getOffers(REQUEST_PAGE);

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

        Intent intent = new Intent(getActivity(), OfferDetailActivity.class);
        intent.putExtra("offer_id", adapter.getItem(position).getId());
        startActivity(intent);

    }

    public void getOffers(final int page) {

        swipeRefreshLayout.setRefreshing(true);


        mGPS = new GPStracker(getActivity());

        if (adapter.getItemCount() == 0)
            mViewManager.loading();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_GET_OFFERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("responseOffersString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);


                    //Log.e("response",response);

                    final OfferParser mOfferParser = new OfferParser(jsonObject);
                    // List<Store> list = mStoreParser.getEventRealm();
                    COUNT = 0;
                    COUNT = mOfferParser.getIntArg(Tags.COUNT);
                    mViewManager.showResult();


                    //check server permission and display the errors
                    if (mOfferParser.getSuccess() == -1) {
                        ErrorsController.serverPermissionError(getActivity());
                    }

                    if (page == 1) {

                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RealmList<Offer> list = mOfferParser.getOffers();

                                adapter.removeAll();
                                for (int i = 0; i < list.size(); i++) {
                                    Offer ofr = list.get(i);
                                    if (mGPS.getLongitude() == 0 && mGPS.getLatitude() == 0) {
                                        ofr.setDistance((double) 0);
                                    }
                                    // if (list.get(i).getDistance() <= REQUEST_RANGE_RADIUS)
                                    adapter.addItem(ofr);
                                }

                                OffersController.removeAll();
                                //set it into database
                                OffersController.insertOffers(list);

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
                                RealmList<Offer> list = mOfferParser.getOffers();

                                for (int i = 0; i < list.size(); i++) {
                                    Offer ofr = list.get(i);
                                    if (mGPS.getLongitude() == 0 && mGPS.getLatitude() == 0) {
                                        ofr.setDistance((double) 0);
                                    }
                                    // if (list.get(i).getDistance() <= REQUEST_RANGE_RADIUS)
                                    adapter.addItem(ofr);
                                }


                                //set it into database
                                OffersController.insertOffers(list);

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

                params.put("token", Utils.getToken(getActivity()));
                params.put("mac_adr", ServiceHandler.getMacAddr());

                params.put("limit", "30");
                params.put("page", page + "");
                params.put("search", REQUEST_SEARCH);
                params.put("date", current_date);
                params.put("timezone", TimeZone.getDefault().getID());
                params.put("order_by", Constances.OrderByFilter.NEARBY);


                if (REQUEST_CATEGORY > -1) params.put("category_id", REQUEST_CATEGORY + "");


                if (store_id > 0)
                    params.put("store_id", String.valueOf(store_id));

                if (APP_DEBUG) {
                    Log.e("ListStoreFragment", "  params getOffers :" + params.toString());
                }


                if (REQUEST_RANGE_RADIUS != -1) {
                    if (REQUEST_RANGE_RADIUS <= 99)
                        params.put("radius", String.valueOf((REQUEST_RANGE_RADIUS * 1024)));
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
            REQUEST_SEARCH = "";
            REQUEST_PAGE = 1;
            REQUEST_RANGE_RADIUS = -1;
            REQUEST_CATEGORY = -1;
            LOCATION = null;
            getOffers(1);

        } else {
            swipeRefreshLayout.setRefreshing(false);
            mGPS.showSettingsAlert();

            if (adapter.getItemCount() == 0)
                mViewManager.error();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            //Toast.makeText(getActivity(), "  is Liked  :"+args.get("isLiked"), Toast.LENGTH_LONG).show();
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

                getOffers(1);
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
                getOffers(1);
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
            inflater.inflate(R.menu.home_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);


        if (Utils.listViewFormat("list_view_format_offers") == 1) {
            menu.findItem(R.id.list_view_icon).setIcon(R.drawable.ic_list_view_grid);

        } else {
            menu.findItem(R.id.list_view_icon).setIcon(R.drawable.ic_view_list_default);
        }
    }

    @Override
    public void onClick(View view) {

    }
}
