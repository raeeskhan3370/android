package com.clubecerto.apps.app.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import com.clubecerto.apps.app.adapter.messenger.ListDiscussionAdapter;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.appconfig.AppContext;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Discussion;
import com.clubecerto.apps.app.classes.Message;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.controllers.messenger.MessengerController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.dtmessenger.DCMBroadcastReceiver;
import com.clubecerto.apps.app.dtmessenger.MessengerHelper;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.DiscussionParser;
import com.clubecerto.apps.app.utils.Translator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;


/**
 * Created by Droideve on 6/26/2016.
 */
public class InboxActivity extends AppCompatActivity implements ListDiscussionAdapter.ClickListener, ListDiscussionAdapter.TouchListener, ViewManager.CustomView, DCMBroadcastReceiver.NetworkStateReceiverListener, SwipeRefreshLayout.OnRefreshListener {


    public ViewManager mViewManager;
    public int INT_RESULT_VERSION = 120;
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.listdiscussion)
    RecyclerView listdiscussion;
    @BindView(R.id.message_input)
    EditText messageInput;
    @BindView(R.id.send_button)
    ImageButton sendButton;
    private ListDiscussionAdapter adapter;
    private Realm mRealm = Realm.getDefaultInstance();
    private User mUser;
    private RequestQueue queue;
    private DCMBroadcastReceiver mDCMBroadcastReceiver;
    private BroadcastReceiver mBroadcastReceiverWakeUp;

    TextView toolbarDescription;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        ButterKnife.bind(this);

        mDCMBroadcastReceiver = new DCMBroadcastReceiver();
        mDCMBroadcastReceiver.addListener(this);

        queue = VolleySingleton.getInstance(this).getRequestQueue();


        if (!SessionsController.isLogged()) {
            ActivityCompat.finishAffinity(this);
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.lefttoright_enter, R.anim.righttoleft_exit);
        }


        mViewManager = new ViewManager(getApplicationContext());
        mViewManager.setLoadingLayout(findViewById(R.id.loading));
        mViewManager.setResultLayout(findViewById(R.id.no_loading));
        mViewManager.setErrorLayout(findViewById(R.id.error));
        mViewManager.setEmpty(findViewById(R.id.empty));
        mViewManager.loading();
        mViewManager.setCustumizeView(this);

        initToolbar();


        mUser = SessionsController.getSession().getUser();

        adapter = new ListDiscussionAdapter(this, getData());
        listdiscussion = findViewById(R.id.listmessages);


        listdiscussion.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listdiscussion.setLayoutManager(mLayoutManager);
        listdiscussion.setAdapter(adapter);

        adapter.setClickListener(this);
        adapter.setTouchListener(this);


        //loadDiscussions();
        loadDiscussionsFromRealm();

        mBroadcastReceiverWakeUp = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("loa", "eakeUp");
            }
        };

        try {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(2001);
            notificationManager.cancel(2002);

        } catch (Exception e) {

        }


    }

    @Override
    public void onRefresh() {
        //loadDiscussions();
        loadDiscussionsFromRealm();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.messenger_menu, menu);
//
//        return true;
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MessengerController.loadMessages(mUser);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(mDCMBroadcastReceiver, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(mDCMBroadcastReceiver);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (INT_RESULT_VERSION == requestCode && resultCode == Activity.RESULT_OK) {

            try {
                int discussionId = data.getExtras().getInt("discussionId", 0);

                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (adapter.getItem(i).getDiscussionId() == discussionId) {
                        adapter.getItem(i).setNbrMessage(0);
                        adapter.notifyDataSetChanged();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public List<Discussion> getData() {
        List<Discussion> data = new ArrayList<Discussion>();
        return data;
    }

    @Override
    public void itemClicked(View view, int position) {

        Intent intent = new Intent(this, MessengerActivity.class);
        intent.putExtra("type", Discussion.DISCUSION_WITH_USER);
        int userId = adapter.getItem(position).getSenderUser().getId();

        intent.putExtra("userId", userId);
        intent.putExtra("discussionId", adapter.getItem(position).getDiscussionId());
        startActivityForResult(intent, INT_RESULT_VERSION);

    }

    @Override
    public void itemTouched(View view, int position) {


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

        toolbar.findViewById(R.id.toolbar_description).setVisibility(View.GONE);
    }

    @Override
    public void customErrorView(View v) {

    }

    @Override
    public void customLoadingView(View v) {

    }

    @Override
    public void customEmptyView(View v) {

        TextView text = v.findViewById(R.id.text);
        Button btn = v.findViewById(R.id.btn);

        text.setText(Translator.print("No Discussion", null));
        btn.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        BusStation.getBus().register(this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBroadcastReceiverWakeUp, filter);

        if (adapter.getItemCount() == 0) {
            mViewManager.loading();
            loadDiscussionsFromRealm();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusStation.getBus().unregister(this);

        unregisterReceiver(mBroadcastReceiverWakeUp);
    }


    private void loadDiscussionsFromRealm() {

//        mViewManager.loading();
//        RealmResults<Discussion> result = mRealm.where(Discussion.class).equalTo("isSystem",false).findAllSorted("createdAt",Sort.DESCENDING);
//        RealmList<Discussion> listdiscussion = new RealmList<Discussion>();
//        listdiscussion.addAll(result.subList(0, result.size()));
//
//        if(listdiscussion.size()>0){
//            adapter.setData(listdiscussion);
//            mViewManager.showResult();
//        }else{
//            loadDiscussions();
//        }

        loadDiscussions();
    }

    private void loadDiscussions() {


        mViewManager.loading();
        final User mUser = SessionsController.getSession().getUser();
        final int user_id = mUser.getId();

        if (user_id > 0) {

            SimpleRequest request = new SimpleRequest(Request.Method.POST,
                    Constances.API.API_LOAD_DISCUSSION, new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {

                    mViewManager.showResult();
                    try {

                        if (AppContext.DEBUG)
                            Log.e("responseDiscussion", response);

                        JSONObject json = new JSONObject(response);
                        DiscussionParser mDiscussionParser = new DiscussionParser(json);

                        if (mDiscussionParser.getSuccess() == 1) {

                            final List<Discussion> listdiscussion = mDiscussionParser.getDiscussion();

                            for (int i = 0; i < listdiscussion.size(); i++) {

                                int nbrOfNewMessages = 0;
                                final int finalI = i;

                                //save discussion and sender
                                mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealmOrUpdate(listdiscussion.get(finalI).getSenderUser());

                                        Discussion dis = listdiscussion.get(finalI);
                                        RealmList<Message> listMessages;

                                        if (dis.getMessages().size() > 0) {
                                            Message lastMessage = dis.getMessages().get(0);
                                            listMessages = new RealmList<Message>();
                                            listMessages.add(lastMessage);
                                            dis.setMessages(listMessages);
                                        }
                                        realm.copyToRealmOrUpdate(dis);
                                    }
                                });


                                //calcul nmb of new messages
                                JSONArray msgId = new JSONArray();
                                for (int f = 0; f < listdiscussion.get(i).getMessages().size(); f++) {

                                    if (
                                            listdiscussion.get(i).getMessages().get(f).getReceiver_id() == mUser.getId()
                                                    &&
                                                    (listdiscussion.get(i).getMessages().get(f).getStatus() == -1
                                                            ||
                                                            listdiscussion.get(i).getMessages().get(f).getStatus() == -2
                                                    )
                                    ) {

                                        nbrOfNewMessages++;
                                        msgId.put(listdiscussion.get(i).getMessages().get(f).getMessageid());

                                    }
                                }


                                listdiscussion.get(i).setNbrMessage(nbrOfNewMessages);
                                adapter.addItem(listdiscussion.get(i));

                                List<Message> listOfMessage = listdiscussion.get(i).getMessages();
                                MessengerHelper.updateInbox(listdiscussion.get(i).getSenderUser().getId(), listOfMessage);

                            }

                        }

                        //show loadToast with error
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR", error.toString());

                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("user_id", user_id + "");
                    params.put("status", "-1");

                    if (AppContext.DEBUG)
                        Log.e("sync", params.toString());

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
    public void networkAvailable() {
        MessengerController.loadMessages(mUser, this);
    }

    @Override
    public void networkUnavailable() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            bulkMarkerInboxLoaded();
        } catch (Exception e) {
            if (AppConfig.APP_DEBUG) e.printStackTrace();
        }

    }


    private void bulkMarkerInboxLoaded() {

        for (int i = 0; i < adapter.getItemCount(); i++) {
            //mark mesaages as loaded
            MessengerController.inboxMarkAsLoaded(mUser, adapter.getItem(i).getDiscussionId());
        }
    }

}
