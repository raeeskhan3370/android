package com.clubecerto.apps.app.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.Services.NotifyDataNotificationEvent;
import com.clubecerto.apps.app.activities.EventDetailActivity;
import com.clubecerto.apps.app.activities.OfferDetailActivity;
import com.clubecerto.apps.app.activities.OrdersActivity;
import com.clubecerto.apps.app.activities.StoreDetailActivity;
import com.clubecerto.apps.app.adapter.lists.NotificationAdapter;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Notification;
import com.clubecerto.apps.app.controllers.notification.NotificationController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.NotificationParser;
import com.clubecerto.apps.app.parser.tags.Tags;
import com.clubecerto.apps.app.utils.CommunApiCalls;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.realm.RealmList;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements ViewManager.CustomView, SwipeRefreshLayout.OnRefreshListener, NotificationAdapter.ClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewManager mViewManager;
    private StaggeredGridLayoutManager gridLayoutManager;
    //pager
    private int COUNT = 0;
    private int REQUEST_PAGE = 1;
    private boolean loading = true;

    //for scrolling params
    private int[] pastVisiblesItems;
    private int visibleItemCount;
    private int totalItemCount;

    // newInstance constructor for creating fragment with arguments
    public static NotificationFragment newInstance(int page, String title) {
        NotificationFragment fragmentFirst = new NotificationFragment();

        Bundle args = new Bundle();
        args.putInt("id", page);
        args.putString("title", title);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.v2_fragment_notification, container, false);

        initSwipeRefresh(rootView);
        //init view manager
        initViewManager(rootView);


        initComponent(rootView);

        //sync data from server
        pullData(getContext(), REQUEST_PAGE);


        hundleScrollListPagination();


        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    private void initSwipeRefresh(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout)
                view.findViewById(R.id.refresh);

        swipeRefreshLayout.setOnRefreshListener(this);


        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent
        );

    }


    private void hundleScrollListPagination() {
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = gridLayoutManager.getChildCount();
                totalItemCount = gridLayoutManager.getItemCount();
                pastVisiblesItems = gridLayoutManager.findFirstVisibleItemPositions(null);


                if (loading) {

                    if ((visibleItemCount + pastVisiblesItems[0]) >= totalItemCount) {
                        loading = false;

                        if (ServiceHandler.isNetworkAvailable(getContext())) {
                            if (COUNT > mAdapter.getItemCount())
                                pullData(getContext(), REQUEST_PAGE);
                        } else {
                            Toast.makeText(getContext(), "Network not available ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void initViewManager(View view) {
        mViewManager = new ViewManager(getContext());
        mViewManager.setLoadingLayout(view.findViewById(R.id.loading));
        mViewManager.setResultLayout(view.findViewById(R.id.container));
        mViewManager.setErrorLayout(view.findViewById(R.id.error));
        mViewManager.setEmpty(view.findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);
        mViewManager.loading();

    }


    private void initComponent(View view) {


        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        // recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        //recyclerView.addItemDecoration(new SpacingItemDecoration(12, Tools.dpToPx(getContext(), 8), true));

        gridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);


        //set data and list adapter
        mAdapter = new NotificationAdapter(getContext(), new ArrayList());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setClickListener(this);


        //refreshing mode off
        swipeRefreshLayout.setRefreshing(false);


    }

    private void pullData(final Context context, final int page) {

        if (mAdapter.getItemCount() == 0) {
            mViewManager.loading();
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }

        Map<String, String> params = new HashMap<String, String>();

        if (SessionsController.getLocalDatabase.isLogged()) {
            params.put("user_id", String.valueOf(SessionsController.getLocalDatabase.getUserId()));
            params.put("guest_id", String.valueOf(SessionsController.getLocalDatabase.getGuestId()));
        } else {
            params.put("auth_type", "guest");
            params.put("auth_id", String.valueOf(SessionsController.getLocalDatabase.getGuestId()));
        }

        params.put("page", page + "");
        params.put("limit", "30");

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_NOTIFICATIONS_GET, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        Log.e("NotificationResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final NotificationParser mNotificationParser = new NotificationParser(jsonObject);
                    COUNT = 0;
                    COUNT = mNotificationParser.getIntArg(Tags.COUNT);

                    int success = Integer.parseInt(mNotificationParser.getStringAttr(Tags.SUCCESS));
                    RealmList<Notification> notifListParser = mNotificationParser.getNotifications(context);

                    if (success == 1 && notifListParser.size() > 0) {
                        mAdapter.addAll(notifListParser);
                        mViewManager.showResult();

                        //update database
                        NotificationController.insertNotifications(notifListParser);


                    } else {
                        mViewManager.empty();
                    }


                    if (COUNT > mAdapter.getItemCount())
                        REQUEST_PAGE++;
                    loading = true;

                    //get user notification from the api
                    CommunApiCalls.countUnseenNotifications(getContext());

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                    mViewManager.error();

                }

                swipeRefreshLayout.setRefreshing(false);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                if (AppConfig.APP_DEBUG) {
                    Log.e("notificationFrag", "  params get notification :" + params.toString());
                }
                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    private void archiveNotification(final int notification_id, final int position) {

        swipeRefreshLayout.setRefreshing(true);

        RequestQueue queue = VolleySingleton.getInstance(getContext()).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_NOTIFICATIONS_REMOVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        Log.e("archiveNotifResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final NotificationParser mNotificationParser = new NotificationParser(jsonObject);
                    int success = Integer.parseInt(mNotificationParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1) {
                        onRefresh();
                        mViewManager.loading();
                        //mAdapter.removeItem(notification_id, position);
                    } else {
                        if (AppConfig.APP_DEBUG) {
                            Log.e("ERROR", mNotificationParser.getStringAttr(Tags.ERRORS));
                        }
                    }


                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();
                    mViewManager.error();


                } finally {
                    swipeRefreshLayout.setRefreshing(false);

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }
                mViewManager.error();


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id", String.valueOf(notification_id));
                if (AppConfig.APP_DEBUG) {
                    Log.e("NotificationFragment", "  params remove notification :" + params.toString());
                }

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

    private void editStatusNotification(final int position, final int notification_id) {


        RequestQueue queue = VolleySingleton.getInstance(getContext()).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_NOTIFICATIONS_EDIT_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        Log.e("archiveNotifResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    final NotificationParser mNotificationParser = new NotificationParser(jsonObject);
                    int success = Integer.parseInt(mNotificationParser.getStringAttr(Tags.SUCCESS));


                    if (success == 1) {
                        Notification notif = mNotificationParser.getNotification(getContext());
                        //refresh database
                        NotificationController.updateNotification(notif);
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();


                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id", String.valueOf(notification_id));
                params.put("status", String.valueOf(1));
                if (AppConfig.APP_DEBUG) {
                    Log.e("NotificationFragment", "  params remove notification :" + params.toString());
                }

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


    // This method will be called when a Notification is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NotifyDataNotificationEvent event) {
        //refresh notification list when the product is deleted
        if (event.message.equals("recently_added")) {
            if (ServiceHandler.isNetworkAvailable(getActivity())) {
                REQUEST_PAGE = 1;
                pullData(getContext(), REQUEST_PAGE);

            }
        }
    }

    @Override
    public void onRefresh() {

        if (ServiceHandler.isNetworkAvailable(getActivity())) {
            REQUEST_PAGE = 1;
            pullData(getContext(), REQUEST_PAGE);


        } else {
            Toast.makeText(getActivity(), "Network not available ", Toast.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void customErrorView(View v) {
        Button btn = (Button) v.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewManager.loading();
                REQUEST_PAGE = 1;
                pullData(getContext(), REQUEST_PAGE);
            }
        });
    }

    @Override
    public void customLoadingView(View v) {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void customEmptyView(View v) {

        /*ImageView img = (ImageView) v.findViewById(R.id.img);
        img.setImageDrawable(getActivity().getDrawable(R.drawable.no_notification));

        TextView text = (TextView) v.findViewById(R.id.text);
        text.setText(getString(R.string.no_notif_found));

        Button btn = (Button) v.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewManager.loading();
                pullData(getContext());
            }
        });*/
    }

    @Override
    public void onItemClick(View view, int pos) {
        Notification obj = mAdapter.getItems().get(pos);
        if (mAdapter.getItems() != null) {


            if (AppConfig.APP_DEBUG)
                Log.e("_productID", String.valueOf(obj.getModule_id()));

            handleClickNotification(obj);

            //update ui
            if (mAdapter.getItems().get(pos).getStatus() == 0) {

                mAdapter.getItems().get(pos).setStatus(1);

                //push counter to all badges
                Notification.notificationsUnseen = Notification.notificationsUnseen - 1;
                EventBus.getDefault().postSticky(new NotifyDataNotificationEvent("update_badges"));

                //Call the API to change the atatus
                editStatusNotification(pos, obj.getId());

            }

            mAdapter.notifyItemChanged(pos);

        }
    }

    private void handleClickNotification(Notification obj) {
        if (obj != null) {
            switch (obj.getModule()) {
                case "store":
                    Intent intentStore = new Intent(getContext(), StoreDetailActivity.class);
                    intentStore.putExtra("id", obj.getModule_id());
                    Objects.requireNonNull(getContext()).startActivity(intentStore);
                    break;
                case "offer":
                    Intent intentOffer = new Intent(getContext(), OfferDetailActivity.class);
                    intentOffer.putExtra("id", obj.getModule_id());
                    Objects.requireNonNull(getContext()).startActivity(intentOffer);
                    break;
                case "event":
                    Intent intentEvent = new Intent(getContext(), EventDetailActivity.class);
                    intentEvent.putExtra("id", obj.getModule_id());
                    Objects.requireNonNull(getContext()).startActivity(intentEvent);
                    break;
                case "nsorder":
                    Intent intentOrder = new Intent(getContext(), OrdersActivity.class);
                    intentOrder.putExtra("id", obj.getModule_id());
                    Objects.requireNonNull(getContext()).startActivity(intentOrder);
                    break;

            }
        }
    }

    @Override
    public void onMoreButtonClick(View view, int position) {
        Notification obj = mAdapter.getItems().get(position);

        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (R.id.action_archive == item.getItemId()) {
                    //call the api to remove the notification remotely then from the database
                    archiveNotification(obj.getId(), position);
                }
                return true;
            }
        });
        popupMenu.inflate(R.menu.menu_notification_more);
        popupMenu.show();

    }
}
