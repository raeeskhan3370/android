package com.clubecerto.apps.app.fragments;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
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
import com.clubecerto.apps.app.activities.EventDetailActivity;
import com.clubecerto.apps.app.activities.MainActivity;
import com.clubecerto.apps.app.adapter.lists.EventListAdapter;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Event;
import com.clubecerto.apps.app.controllers.ErrorsController;
import com.clubecerto.apps.app.controllers.events.EventController;
import com.clubecerto.apps.app.controllers.events.UpComingEventsController;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.EventParser;
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
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmList;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class ListEventFragment extends Fragment
        implements EventListAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ViewManager.CustomView, View.OnClickListener {

    public ViewManager mViewManager;
    //loading
    public SwipeRefreshLayout swipeRefreshLayout;
    private int listType = 1;
    //to check if the event is liked
    private int isLiked = 0;
    private RecyclerView list;
    private EventListAdapter adapter;
    //init request http
    private RequestQueue queue;
    //for scrolling params
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private LinearLayoutManager mLayoutManager;
    //pager
    private int COUNT = 0;

    private GPStracker mGPS;
    private List<Event> listEvents = new ArrayList<>();

    private int REQUEST_PAGE = 1;
    private String REQUEST_SEARCH = "";
    private int REQUEST_RANGE_RADIUS = -1;
    private int REQUEST_CATEGORY = -1;
    private LatLng LOCATION = null;

    private String current_date;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        current_date = DateUtils.getUTC("yyyy-MM-dd hh:mm");
    }

    private void updateBadges() {
        MainActivity.updateMessengerBadge(getActivity());
        MainActivity.updateNotificationBadge(getActivity());
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.list_view_icon).setVisible(false);
        menu.findItem(R.id.notification_action).setVisible(true);
        menu.findItem(R.id.search_icon).setVisible(true);

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
                    getEvents(1);

                }
            }).setHeader(getString(R.string.searchOnEvents)).showDialog();

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);

        mGPS = new GPStracker(getActivity());

        if (!mGPS.canGetLocation() && listType == 1)
            mGPS.showSettingsAlert();


        queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();


        mViewManager = new ViewManager(getActivity());
        mViewManager.setLoadingLayout(rootView.findViewById(R.id.loading));
        mViewManager.setResultLayout(rootView.findViewById(R.id.content_events));
        mViewManager.setErrorLayout(rootView.findViewById(R.id.error));
        mViewManager.setEmpty(rootView.findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);
        mViewManager.loading();


        list = rootView.findViewById(R.id.list);


        adapter = new EventListAdapter(getActivity(), listEvents);
        adapter.setClickListener(this);


        list.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        //listcats.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        list.setItemAnimator(new DefaultItemAnimator());
        list.setLayoutManager(mLayoutManager);
        list.setAdapter(adapter);


        list.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;

                        if (COUNT > adapter.getItemCount())
                            getEvents(REQUEST_PAGE);
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

        //load
        REQUEST_SEARCH = "";
        REQUEST_PAGE = 1;
        REQUEST_RANGE_RADIUS = -1;
        getEvents(REQUEST_PAGE);

        return rootView;
    }

    @Override
    public void itemClicked(View view, int position) {

        Event event_c = adapter.getItem(position);

        if (event_c != null) {

            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("id", event_c.getId());
            startActivity(intent);

        }

    }

    private void getEvents(final int page) {

        swipeRefreshLayout.setRefreshing(true);


        if (getArguments() != null && !getArguments().isEmpty()) {
            isLiked = getArguments().getInt("isLiked");
            if (isLiked == 1) {
                //mViewManager.loading();

                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        List<Event> myEvent = EventController.getLikeEventsAsArrayList();
                        adapter.removeAll();
                        for (int j = 0; j < myEvent.size(); j++) {
                            myEvent.get(j).setFeatured(0);
                            adapter.addItem(myEvent.get(j));
                        }
                    }
                });


                if (adapter.getItemCount() == 0) {
                    mViewManager.empty();
                } else {
                    mViewManager.showResult();
                }

            }
        } else {
            mGPS = new GPStracker(getActivity());

           /* if (adapter.getItemCount() == 0) {
                mViewManager.loading();
            } else {
                //swipeRefreshLayout.setRefreshing(true);
            }*/

            String ids = "";

            if (isLiked == 2) {
                ids = UpComingEventsController.getListAsString();
            }

            final String finalIds = ids;
            SimpleRequest request = new SimpleRequest(Request.Method.POST,
                    Constances.API.API_USER_GET_EVENTS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {


                        JSONObject jsonObject = new JSONObject(response);

                        if (APP_DEBUG)
                            Log.e("response", jsonObject.toString());

                        final EventParser mEventParser = new EventParser(jsonObject);
                        COUNT = mEventParser.getIntArg(Tags.COUNT);


                        //check server permission and display the errors
                        if (mEventParser.getSuccess() == -1) {
                            ErrorsController.serverPermissionError(getActivity());
                        }

                        if (page == 1) {

                            adapter.removeAll();

                            if (APP_DEBUG) {
                                Log.e("count", COUNT + "");
                            }

                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    mGPS = new GPStracker(getContext());
                                    RealmList<Event> list = mEventParser.getEvents();

                                    if (list.size() > 0) {
                                        EventController.insertEvents(list);
                                    }

                                    for (int i = 0; i < list.size(); i++) {


                                        Event eV = list.get(i);
                                        if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
                                            eV.setDistance((double) 0);
                                        }
                                        //GET THE ID EVENT FROM EVENT LIKE IN DATABASE AND COMPARE IT WITH THIS ID
                                        adapter.addItem(eV);
                                        if (APP_DEBUG) {
                                            Log.e("EventParser", "id event " + list.get(i).getId() + "   description   " + list.get(i).getAddress());
                                        }
                                    }


                                    if (APP_DEBUG) {
                                        Log.e("EventParserCount", adapter.getItemCount() + "");
                                    }

                                    swipeRefreshLayout.setRefreshing(false);

                                    loading = true;

                                    if (COUNT > adapter.getItemCount())
                                        REQUEST_PAGE++;


                                    if (COUNT == 0) {
                                        mViewManager.empty();
                                    } else {
                                        mViewManager.showResult();
                                    }

                                }
                            }, 800);
                        } else {
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    RealmList<Event> list = mEventParser.getEvents();

                                    if (list.size() > 0) {
                                        EventController.insertEvents(list);
                                    }

                                    for (int i = 0; i < list.size(); i++) {
                                        Event eV = list.get(i);
                                        if (mGPS.getLatitude() == 0 && mGPS.getLongitude() == 0) {
                                            eV.setDistance((double) 0);
                                        }
                                        //GET THE ID EVENT FROM EVENT LIKE IN DATABASE AND COMPARE IT WITH THIS ID
                                        adapter.addItem(eV);
                                    }
                                    swipeRefreshLayout.setRefreshing(false);
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
                        Log.e("ERROR", response);
                        e.printStackTrace();
                        mViewManager.error();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (APP_DEBUG)
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


                    if (REQUEST_RANGE_RADIUS != -1) {
                        if (REQUEST_RANGE_RADIUS <= 99)
                            params.put("radius", String.valueOf((REQUEST_RANGE_RADIUS * 1024)));
                    }

                    params.put("token", Utils.getToken(Objects.requireNonNull(getContext())));
                    params.put("mac_adr", ServiceHandler.getMacAddr());
                    params.put("limit", "12");
                    params.put("page", page + "");
                    params.put("search", REQUEST_SEARCH);
                    params.put("order_by", Constances.OrderByFilter.NEARBY);


                    params.put("date", current_date);

                    if (isLiked == 2) {
                        params.put("event_ids", finalIds);
                    }


                    if (APP_DEBUG) {
                        Log.e("ListEventFragment", "  params getEvent :" + params.toString());
                    }

                    return params;
                }

            };


            request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(request);
        }


    }


    @Override
    public void onRefresh() {

        mGPS = new GPStracker(getContext());
        if (mGPS.canGetLocation()) {
            REQUEST_SEARCH = "";
            REQUEST_PAGE = 1;
            REQUEST_RANGE_RADIUS = -1;
            REQUEST_CATEGORY = -1;
            LOCATION = null;
            getEvents(REQUEST_PAGE);
        } else {
            mGPS.showSettingsAlert();
        }

        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onResume() {
        super.onResume();
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

                REQUEST_SEARCH = "";
                REQUEST_PAGE = 1;
                REQUEST_RANGE_RADIUS = -1;

                getEvents(REQUEST_PAGE);
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

                REQUEST_SEARCH = "";
                REQUEST_PAGE = 1;
                REQUEST_RANGE_RADIUS = -1;
                getEvents(REQUEST_PAGE);

            }
        });


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.clear();
            inflater.inflate(R.menu.home_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onClick(View view) {

    }
}
