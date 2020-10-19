package com.clubecerto.apps.app.fragments;

import android.os.Bundle;
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
import androidx.recyclerview.widget.DividerItemDecoration;
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
import com.clubecerto.apps.app.adapter.lists.EstabHoriAapter;
import com.clubecerto.apps.app.adapter.lists.EstablisAapter;
import com.clubecerto.apps.app.adapter.lists.OfferListAdapter;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.Estable;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.utils.DateUtils;
import com.clubecerto.apps.app.utils.Session;
import com.clubecerto.apps.app.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.clubecerto.apps.app.R.layout.fragment_estable_list;
import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class EstabFragment extends Fragment
        implements EstablisAapter.MyViewHolder.OnItemSkillClickListener, SwipeRefreshLayout.OnRefreshListener, ViewManager.CustomView, View.OnClickListener {

    public ViewManager mViewManager;
    //loading
    public SwipeRefreshLayout swipeRefreshLayout;
    //for scrolling params
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    private LinearLayoutManager mLayoutManager;
    private int listType = 1;
    private RecyclerView list,list_destaque;
    private EstablisAapter adapter;
    private EstabHoriAapter adapter_destaque;
    //init request http
    private RequestQueue queue;
    private boolean loading = true;
    //pager
    private int COUNT = 0;
    private int REQUEST_PAGE = 1;
    private GPStracker mGPS;
    private ArrayList<Estable> listStores = new ArrayList<>();
    private ArrayList<Estable> listDestaque = new ArrayList<>();
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
                    getOffersdestaque(1);

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
        View rootView = inflater.inflate(fragment_estable_list, container, false);




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
       list_destaque  =rootView.findViewById(R.id.list_hori);
        list_destaque.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));


        adapter = new EstablisAapter(getActivity(), listStores,
                EstabFragment.this::OnItemSkillClickListener);
        list.setAdapter(adapter);



        list.setHasFixedSize(true);

//        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Utils.listViewFormat("list_view_format_offers"));
//        list.setLayoutManager(mLayoutManager);
//        list.setItemAnimator(new DefaultItemAnimator());
//        list.setAdapter(adapter);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);


//
//        list.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

//                visibleItemCount = mLayoutManager.getChildCount();
//                totalItemCount = mLayoutManager.getItemCount();
//                pastVisiblesItems = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
//                if (loading) {
//
//                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
//                        loading = false;
//                        if (ServiceHandler.isNetworkAvailable(getContext())) {
//                            if (COUNT > adapter.getItemCount())
//                                getOffers(REQUEST_PAGE);
//                        } else {
//                            Toast.makeText(getContext(), "Network not available ", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            }
//        });








        if (ServiceHandler.isNetworkAvailable(this.getActivity())) {
            getOffers(REQUEST_PAGE);
            getOffersdestaque(REQUEST_PAGE);

        } else {

//            swipeRefreshLayout.setRefreshing(false);
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

//    @Override
//    public void itemClicked(View view, int position) {

//        Intent intent = new Intent(getActivity(), OfferDetailActivity.class);
//        intent.putExtra("offer_id", adapter.getItem(position).getId());
//        startActivity(intent);

//    }

    public void getOffers(final int page) {


        if (adapter.getItemCount() == 0)
            mViewManager.loading();
        Session session=new Session(getContext());
        String cpf=session.getCPFCompany();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                "https://www.clubecerto.com.br/ws/api/2/estabelecimentos.php?user=aplicativo&token=app@cbc31871certo&cpf="+cpf+"&pagina=10", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("responseOfsString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray=jsonObject.getJSONArray("Estabelecimentos");
                    for (int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                        Estable estable;



                           listStores.add(estable=new Estable(jsonObject2.getString("Codigo"),jsonObject2.getString("Nome")
                               ,jsonObject2.getString("Destaque"),jsonObject2.getString("Palavras-chave"),jsonObject2.getString("Marca")
                               ,jsonObject2.getString("Capa"),jsonObject2.getString("CategoriaCodigo"),jsonObject2.getString("CategoriaNome")
                               ,jsonObject2.getString("Beneficio"),jsonObject2.getString("Ativo")));

                    }



//                    adapter.setClickListener(this);

                    //Log.e("response",response);

//                    final OfferParser mOfferParser = new OfferParser(jsonObject);
//                    // List<Store> list = mStoreParser.getEventRealm();
//                    COUNT = 0;
//                    COUNT = mOfferParser.getIntArg(Tags.COUNT);
                    mViewManager.showResult();
//
//
//                    //check server permission and display the errors
//                    if (mOfferParser.getSuccess() == -1) {
//                        ErrorsController.serverPermissionError(getActivity());
//                    }
//
//                    if (page == 1) {
//
//                        (new Handler()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                RealmList<Offer> list = mOfferParser.getOffers();
//
//                                adapter.removeAll();
//                                for (int i = 0; i < list.size(); i++) {
//                                    Offer ofr = list.get(i);
//                                    if (mGPS.getLongitude() == 0 && mGPS.getLatitude() == 0) {
//                                        ofr.setDistance((double) 0);
//                                    }
//                                    // if (list.get(i).getDistance() <= REQUEST_RANGE_RADIUS)
//                                    adapter.addItem(ofr);
//                                }
//
//                                OffersController.removeAll();
//                                //set it into database
//                                OffersController.insertOffers(list);
//
//                                loading = true;
//
//                                mViewManager.showResult();
//
//                                if (COUNT > adapter.getItemCount())
//                                    REQUEST_PAGE++;
//                                if (COUNT == 0 || adapter.getItemCount() == 0) {
//                                    mViewManager.empty();
//                                }
//
//
//                                if (APP_DEBUG) {
//                                    Log.e("count ", COUNT + " page = " + page);
//                                }
//
//                            }
//                        }, 800);
//                    } else {
//                        (new Handler()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                RealmList<Offer> list = mOfferParser.getOffers();
//
//                                for (int i = 0; i < list.size(); i++) {
//                                    Offer ofr = list.get(i);
//                                    if (mGPS.getLongitude() == 0 && mGPS.getLatitude() == 0) {
//                                        ofr.setDistance((double) 0);
//                                    }
//                                    // if (list.get(i).getDistance() <= REQUEST_RANGE_RADIUS)
//                                    adapter.addItem(ofr);
//                                }
//
//
//                                //set it into database
//                                OffersController.insertOffers(list);
//
//                                mViewManager.showResult();
//                                loading = true;
//                                if (COUNT > adapter.getItemCount())
//                                    REQUEST_PAGE++;
//
//                                if (COUNT == 0 || adapter.getItemCount() == 0) {
//                                    mViewManager.empty();
//                                }
//                            }
//                        }, 800);
//
//                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    if (APP_DEBUG)
                        e.printStackTrace();

                    if (adapter.getItemCount() == 0)
                        mViewManager.error();


                }

//                swipeRefreshLayout.setRefreshing(false);

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

//            @Override
//            protected Map<String, String> getParams() {
////                Map<String, String> params = new HashMap<String, String>();
////                params.put("user", "aplicativo");
////                params.put("token", "app@cbc31871certo");
////                params.put("cpf", "0939260639");
////                params.put("pagina", "10");
////
////
////
////
////
////
////                return params;
//            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }
    public void getOffersdestaque(final int page) {



        Session session=new Session(getContext());
        String cpf=session.getCPFCompany();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                "https://www.clubecerto.com.br/ws/api/2/estabelecimentos.php?user=aplicativo&token=app@cbc31871certo&cpf="+cpf+"&destaque=1", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("responseOfsString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray=jsonObject.getJSONArray("Estabelecimentos");
                    for (int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                        Estable estable;
                        if (jsonObject2.getString("Destaque").equals("1"))
                        {
                            listDestaque.add(estable=new Estable(jsonObject2.getString("Codigo"),jsonObject2.getString("Nome")
                                    ,jsonObject2.getString("Destaque"),jsonObject2.getString("Palavras-chave"),jsonObject2.getString("Marca")
                                    ,jsonObject2.getString("Capa"),jsonObject2.getString("CategoriaCodigo"),jsonObject2.getString("CategoriaNome")
                                    ,jsonObject2.getString("Beneficio"),jsonObject2.getString("Ativo")));
                        }


                    }

                    adapter_destaque=new EstabHoriAapter(getActivity(), listDestaque,
                            EstabFragment.this::OnItemSkillClickListener);
                    list_destaque.setAdapter(adapter_destaque);

//                    adapter.setClickListener(this);

                    //Log.e("response",response);

//                    final OfferParser mOfferParser = new OfferParser(jsonObject);
//                    // List<Store> list = mStoreParser.getEventRealm();
//                    COUNT = 0;
//                    COUNT = mOfferParser.getIntArg(Tags.COUNT);
                    mViewManager.showResult();
//
//
//                    //check server permission and display the errors
//                    if (mOfferParser.getSuccess() == -1) {
//                        ErrorsController.serverPermissionError(getActivity());
//                    }
//
//                    if (page == 1) {
//
//                        (new Handler()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                RealmList<Offer> list = mOfferParser.getOffers();
//
//                                adapter.removeAll();
//                                for (int i = 0; i < list.size(); i++) {
//                                    Offer ofr = list.get(i);
//                                    if (mGPS.getLongitude() == 0 && mGPS.getLatitude() == 0) {
//                                        ofr.setDistance((double) 0);
//                                    }
//                                    // if (list.get(i).getDistance() <= REQUEST_RANGE_RADIUS)
//                                    adapter.addItem(ofr);
//                                }
//
//                                OffersController.removeAll();
//                                //set it into database
//                                OffersController.insertOffers(list);
//
//                                loading = true;
//
//                                mViewManager.showResult();
//
//                                if (COUNT > adapter.getItemCount())
//                                    REQUEST_PAGE++;
//                                if (COUNT == 0 || adapter.getItemCount() == 0) {
//                                    mViewManager.empty();
//                                }
//
//
//                                if (APP_DEBUG) {
//                                    Log.e("count ", COUNT + " page = " + page);
//                                }
//
//                            }
//                        }, 800);
//                    } else {
//                        (new Handler()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                RealmList<Offer> list = mOfferParser.getOffers();
//
//                                for (int i = 0; i < list.size(); i++) {
//                                    Offer ofr = list.get(i);
//                                    if (mGPS.getLongitude() == 0 && mGPS.getLatitude() == 0) {
//                                        ofr.setDistance((double) 0);
//                                    }
//                                    // if (list.get(i).getDistance() <= REQUEST_RANGE_RADIUS)
//                                    adapter.addItem(ofr);
//                                }
//
//
//                                //set it into database
//                                OffersController.insertOffers(list);
//
//                                mViewManager.showResult();
//                                loading = true;
//                                if (COUNT > adapter.getItemCount())
//                                    REQUEST_PAGE++;
//
//                                if (COUNT == 0 || adapter.getItemCount() == 0) {
//                                    mViewManager.empty();
//                                }
//                            }
//                        }, 800);
//
//                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    if (APP_DEBUG)
                        e.printStackTrace();

                    if (adapter.getItemCount() == 0)
                        mViewManager.error();


                }

//                swipeRefreshLayout.setRefreshing(false);

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

//            @Override
//            protected Map<String, String> getParams() {
////                Map<String, String> params = new HashMap<String, String>();
////                params.put("user", "aplicativo");
////                params.put("token", "app@cbc31871certo");
////                params.put("cpf", "0939260639");
////                params.put("pagina", "10");
////
////
////
////
////
////
////                return params;
//            }

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
            getOffersdestaque(1);

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
                getOffersdestaque(1);
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
                getOffersdestaque(1);
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

    @Override
    public void OnItemSkillClickListener(View view, int position, long id, int viewType) {

    }
}
