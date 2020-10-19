package com.clubecerto.apps.app.customview;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.ListStoresActivity;
import com.clubecerto.apps.app.adapter.lists.CategoriesListAdapter;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.Category;
import com.clubecerto.apps.app.controllers.categories.CategoryController;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.CategoryParser;
import com.clubecerto.apps.app.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class CategoryCustomView extends LinearLayout implements CategoriesListAdapter.ClickListener {

    private Context mContext;
    private CategoriesListAdapter adapter;
    private RecyclerView listView;
    private Map<String, Object> optionalParams;
    private Boolean rectCategoryView = false;
    private View mainContainer;
    private HashMap<String, Object> searchParams;


    //this field will contain the selected id category
    private int itemCategoryselectedId = -1;


    public CategoryCustomView(Context context) {
        super(context);
        mContext = context;
        setRecyclerViewAdapter();
    }

    public CategoryCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setCustomAttribute(context, attrs);
        setRecyclerViewAdapter();
    }

    public int getItemCategoryselectedId() {
        return itemCategoryselectedId;
    }

    public void setItemCategoryselectedId(int itemCategoryselectedId) {
        this.itemCategoryselectedId = itemCategoryselectedId;
    }


    public void loadData(boolean fromDatabase) {


        if (!fromDatabase) loadDataFromAPi();
        else loadDataFromDB();
    }

    public void loadDataFromDB() {
        //ensure the data exist on the database if not load it from api
        RealmList<Category> listCats = CategoryController.list();
        if (!listCats.isEmpty()) {
            adapter.clear();
            adapter.addAllItems(listCats);
            adapter.notifyDataSetChanged();

            listView.setVisibility(VISIBLE);
            mainContainer.setVisibility(VISIBLE);

        } else {
            loadDataFromAPi();
        }

    }


    private void loadDataFromAPi() {

        RequestQueue queue = VolleySingleton.getInstance(mContext).getRequestQueue();


        listView.setVisibility(GONE);

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_USER_GET_CATEGORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("catsResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final CategoryParser mCategoryParser = new CategoryParser(jsonObject);
                    int success = Integer.parseInt(mCategoryParser.getStringAttr(Tags.SUCCESS));
                    if (success == 1) {
                        RealmList<Category> listCats = mCategoryParser.getCategories();

                        if (!listCats.isEmpty()) {
                            adapter.clear();
                            adapter.addAllItems(listCats);
                            mainContainer.setVisibility(VISIBLE);
                            listView.setVisibility(VISIBLE);

                        } else {
                            mainContainer.setVisibility(GONE);
                        }


                        adapter.notifyDataSetChanged();

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

            }


        }) {


        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    private void setRecyclerViewAdapter() {

        if (rectCategoryView) {
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            setOrientation(LinearLayout.VERTICAL);
            setGravity(Gravity.TOP);
        }


        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.horizontal_list_categories, this, true);


        mainContainer = (View) getChildAt(0).findViewById(R.id.cat_container);


        if ((Boolean) optionalParams.get("displayCatTitle")) {
            getChildAt(0).findViewById(R.id.card_title).setVisibility(VISIBLE);
            ((TextView) getChildAt(0).findViewById(R.id.card_title)).setText((String) optionalParams.get("title"));
        } else {
            getChildAt(0).findViewById(R.id.card_title).setVisibility(GONE);
        }

        listView = (RecyclerView) getChildAt(0).findViewById(R.id.list);
        adapter = new CategoriesListAdapter(mContext, new ArrayList<>(), rectCategoryView, optionalParams);

        LinearLayoutManager mLayoutManager;
        if (rectCategoryView) {
            mLayoutManager = new LinearLayoutManager(mContext);
            mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        } else {
            mLayoutManager = new LinearLayoutManager(mContext);
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        listView.setLayoutManager(mLayoutManager);
        listView.setHasFixedSize(true);

        listView.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    @Override
    public void itemClicked(View view, int position) {


        if (optionalParams.containsKey("itemClickRedirection") && (Boolean) optionalParams.get("itemClickRedirection")) {
            searchParams = new HashMap<>();
            searchParams.put("module", "store");
            searchParams.put("category", adapter.getItem(position).getNumCat());

            Intent intent = new Intent(mContext, ListStoresActivity.class);
            intent.putExtra("category", adapter.getItem(position).getNumCat());
            mContext.startActivity(intent);
        } else {
            //adapter.setSelectedItem(position);
            //Toast.makeText(mContext, adapter.getItem(position).getNameCat() + " Selected ", Toast.LENGTH_LONG).show();
            setItemCategoryselectedId(adapter.getItem(position).getNumCat());


        }

    }


    private void setCustomAttribute(Context context, @Nullable AttributeSet attrs) {

        optionalParams = new HashMap<>();
        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CategoryCustomView, 0, 0);

        try {
            //get the text and colors specified using the names in attrs.xml
            optionalParams.put("displayCatTitle", a.getBoolean(R.styleable.CategoryCustomView_ccDisplayTitle, true));
            optionalParams.put("displayStoreNumber", a.getBoolean(R.styleable.CategoryCustomView_ccDisplayStoreNumber, true));
            optionalParams.put("itemClickRedirection", a.getBoolean(R.styleable.CategoryCustomView_ccClickRedirection, true));
            optionalParams.put("loader", a.getBoolean(R.styleable.CategoryCustomView_ccLoader, true));
            optionalParams.put("title", a.getString(R.styleable.CategoryCustomView_ccTitle));
            rectCategoryView = a.getBoolean(R.styleable.CategoryCustomView_ccRect, true);
        } finally {
            a.recycle();
        }
    }

}
