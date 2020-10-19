package com.clubecerto.apps.app.navigationdrawer;


import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.activities.AboutActivity;
import com.clubecerto.apps.app.activities.CardDetailActivity;
import com.clubecerto.apps.app.activities.CategoriesActivity;
import com.clubecerto.apps.app.activities.EditProfileActivity;
import com.clubecerto.apps.app.activities.FavoriteEventsActivity;
import com.clubecerto.apps.app.activities.FavoriteStoreActivity;
import com.clubecerto.apps.app.activities.ListUsersActivity;
import com.clubecerto.apps.app.activities.LoginActivity;
import com.clubecerto.apps.app.activities.MapStoresListActivity;
import com.clubecerto.apps.app.activities.OrdersActivity;
import com.clubecerto.apps.app.activities.SettingActivity;
import com.clubecerto.apps.app.activities.SplashActivity;
import com.clubecerto.apps.app.adapter.navigation.SimpleListAdapterNavDrawer;
import com.clubecerto.apps.app.animation.ImageLoaderAnimation;
import com.clubecerto.apps.app.appconfig.AppConfig;
import com.clubecerto.apps.app.classes.HeaderItem;
import com.clubecerto.apps.app.classes.Item;
import com.clubecerto.apps.app.controllers.SettingsController;
import com.clubecerto.apps.app.controllers.categories.CategoryController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.fragments.MainFragment;
import com.clubecerto.apps.app.utils.Session;
import com.wuadam.awesomewebview.AwesomeWebView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class NavigationDrawerFragment extends Fragment implements SimpleListAdapterNavDrawer.ClickListener {


    public static final String PREF_FILE_NAME = "testpref";
    public static final String KEY_USER_LEARNED_DRAWER = "learned_user_drawer";
    public static int INT_CHAT_BOX = 5;
    private static DrawerLayout mDrawerLayout;
    List<Item> listItems = Collections.emptyList();
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private boolean mUserLearedLayout;
    private boolean mFromSaveInstanceState;
    private Session session;


    //init request http
    private SimpleListAdapterNavDrawer adapter;

    public static DrawerLayout getInstance() {
        return mDrawerLayout;
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(preferenceName, preferenceValue);
        edit.apply();

    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mUserLearedLayout = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mFromSaveInstanceState = true;
        }

       /* if(SessionsController.isLogged())
             user = SessionsController.getSession().getUser();*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.navigation_drawer_content, container, false);

        rootView.setClickable(true);
        session = new Session(getActivity());
        String pat=     session.getCard();
        ImageView img_comp=rootView.findViewById(R.id.im_comp);
        img_comp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), CardDetailActivity.class);
                startActivity(intent);
            }
        });


        if ( pat != null) {




            Glide.with(this)
                    .load( pat)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .placeholder(ImageLoaderAnimation.glideLoader(getActivity() ))
                    .into(img_comp);

        } else {
           img_comp.setImageDrawable(ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.def_logo, null));
        }

        RecyclerView drawerList = rootView.findViewById(R.id.drawerLayout);
        drawerList.setVisibility(View.VISIBLE);

        adapter = new SimpleListAdapterNavDrawer(getActivity(), getData());

        drawerList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        drawerList.setLayoutManager(mLayoutManager);
        drawerList.setAdapter(adapter);

        adapter.setClickListener(this);

        return rootView;

    }

    public List<Item> getData() {

        listItems = new ArrayList<Item>();


        HeaderItem header_item = new HeaderItem();
        header_item.setName(AppController.getInstance().getResources().getString(R.string.Home));
        header_item.setEnabled(true);


        Item webdashboard = new Item();
        webdashboard.setName(AppController.getInstance().getResources().getString(R.string.ManageThings));
        webdashboard.setIconDraw(MaterialDrawableBuilder.IconValue.WEB);
        webdashboard.setID(12);


        Item homeItem = new Item();
        homeItem.setName(AppController.getInstance().getResources().getString(R.string.Home));
        homeItem.setIconDraw(MaterialDrawableBuilder.IconValue.HOME);
        homeItem.setID(1);


        int categories_count = CategoryController.getArrayList().size();
        String categoriesMenu = AppController.getInstance().getResources().getString(R.string.Categories);
        if (categories_count > 0) categoriesMenu = categoriesMenu + " (" + categories_count + ")";

        Item catItem = new Item();
        catItem.setName(categoriesMenu);
        catItem.setIconDraw(MaterialDrawableBuilder.IconValue.FORMAT_LIST_BULLETED);
        catItem.setID(2);


//        Item findNewCustomers = null;
//        findNewCustomers = new Item();
//        if (SessionsController.isLogged()) {
//            //savesItem.setName(getResources().getString(R.string.Favoris)+" /*("+bookmaeks_count+")*/");
//            findNewCustomers.setName(AppController.getInstance().getResources().getString(R.string.FindCustomers));
//            findNewCustomers.setIconDraw(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_OUTLINE);
//            findNewCustomers.setID(3);
//
//        } else {
//            //savesItem.setName(AppController.getInstance().getResources().getString(R.string.Favoris)+" /*("+bookmaeks_count+")*/");
//            findNewCustomers.setName(AppController.getInstance().getResources().getString(R.string.Login));
//            findNewCustomers.setIconDraw(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_OUTLINE);
//            findNewCustomers.setID(4);
//        }


        Item savesItem = new Item();
        //savesItem.setName(AppController.getInstance().getResources().getString(R.string.Favoris)+" /*("+bookmaeks_count+")*/");
        savesItem.setName(AppController.getInstance().getResources().getString(R.string.Favoris));
        savesItem.setIconDraw(MaterialDrawableBuilder.IconValue.BOOKMARK_CHECK);
        savesItem.setID(Menu.FAV);


        Item EventLikeItem = new Item();
        EventLikeItem.setName(AppController.getInstance().getResources().getString(R.string.EventLike));
        EventLikeItem.setIconDraw(MaterialDrawableBuilder.IconValue.ALARM_CHECK);
        EventLikeItem.setID(Menu.MY_EVENT);


        Item aboutItem = new Item();
        aboutItem.setName(AppController.getInstance().getResources().getString(R.string.about));
        aboutItem.setIconDraw(MaterialDrawableBuilder.IconValue.INFORMATION_OUTLINE);
        aboutItem.setID(Menu.ABOUT);

        Item createStoreItem = new Item();
        createStoreItem.setName(AppController.getInstance().getResources().getString(R.string.store_create_btn));
        createStoreItem.setIconDraw(MaterialDrawableBuilder.IconValue.PLUS_BOX);
        createStoreItem.setID(9);

        Item settingItem = new Item();
        settingItem.setName(AppController.getInstance().getResources().getString(R.string.Settings));
        settingItem.setIconDraw(MaterialDrawableBuilder.IconValue.SETTINGS);
        settingItem.setID(Menu.SETTING);


        Item mapStoresItem = new Item();
        mapStoresItem.setName(AppController.getInstance().getResources().getString(R.string.MapStoresMenu));
        mapStoresItem.setIconDraw(MaterialDrawableBuilder.IconValue.MAP_MARKER_RADIUS);
        mapStoresItem.setID(Menu.MAP_STORES);


        /*Item notification = new Item();
        notification.setName(AppController.getInstance().getResources().getString(R.string.notification));
        notification.setIconDraw(MaterialDrawableBuilder.IconValue.BELL_OUTLINE);
        notification.setID(Menu.NOTIFICATIONS);*/


        Item editProdile = new Item();
        editProdile.setName(AppController.getInstance().getResources().getString(R.string.editProfile));
        editProdile.setIconDraw(MaterialDrawableBuilder.IconValue.ACCOUNT);
        editProdile.setID(7);


        Item logout = new Item();
        //savesItem.setName(AppController.getInstance().getResources().getString(R.string.Favoris)+" /*("+bookmaeks_count+")*/");
        logout.setName(AppController.getInstance().getResources().getString(R.string.Logout));
        logout.setIconDraw(MaterialDrawableBuilder.IconValue.LOGOUT);
        logout.setID(11);

        Item orders = new Item();
        //savesItem.setName(AppController.getInstance().getResources().getString(R.string.Favoris)+" /*("+bookmaeks_count+")*/");
        orders.setName(AppController.getInstance().getResources().getString(R.string.orders));
        orders.setIconDraw(MaterialDrawableBuilder.IconValue.CART);
        orders.setID(Menu.ORDERS);


        if (header_item.isEnabled())
            listItems.add(header_item);

        //HOME
        if (homeItem.isEnabled())
            listItems.add(homeItem);

        //Categories
        if (catItem.isEnabled())
            listItems.add(catItem);

        //Geo Stores
        if (mapStoresItem.isEnabled() && SettingsController.isModuleEnabled("store")) {
            listItems.add(mapStoresItem);
        }

        //Orders
        // if (orders.isEnabled() && SessionsController.isLogged())
        //     listItems.add(orders);


        //People Around Me
//        if (AppConfig.ENABLE_CHAT && findNewCustomers != null) {
//            if (SessionsController.isLogged() && !SettingsController.isModuleEnabled("messenger")) {
//                // hide item when user is logged and chat module is disabled
//            } else {
//                listItems.add(findNewCustomers);
//            }
//
//        }

        /*//Notifications
        if (orders.isEnabled())
            listItems.add(notification);*/


        //Edit Profile
        if (editProdile.isEnabled() && SessionsController.isLogged())
            listItems.add(editProdile);

        //Settings
        // if (AppConfig.ENABLE_WEB_DASHBOARD)
        //     listItems.add(webdashboard);


        //My Favourite
        if (savesItem.isEnabled() && SettingsController.isModuleEnabled("store")) {
            listItems.add(savesItem);
        }

        //Event Liked
        if (EventLikeItem.isEnabled()) {
            if (SettingsController.isModuleEnabled("event")) {
                listItems.add(EventLikeItem);
            }
        }

        //Settings
        if (settingItem.isEnabled())
            listItems.add(settingItem);

        //About US
        // if (aboutItem.isEnabled())
        //     listItems.add(aboutItem);

        //Edit Profile
//        if (logout.isEnabled() && SessionsController.isLogged())
            listItems.add(logout);


        return listItems;
    }

    public void setUp(int FragId, DrawerLayout drawerlayout, final Toolbar toolbar) {

        View containerView = Objects.requireNonNull(getView()).findViewById(FragId);
        mDrawerLayout = drawerlayout;

        //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerlayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);
                if (!mUserLearedLayout) {
                    mUserLearedLayout = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearedLayout + "");
                }


                getActivity().invalidateOptionsMenu();

            }

            @Override
            public void onDrawerClosed(View drawerView) {

                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();

            }


            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

            }
        };

        if (!mUserLearedLayout && !mFromSaveInstanceState) {
            mDrawerLayout.closeDrawer(containerView);
        }


        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();

            }
        });

    }


    @Override
    public void onStart() {
        //EventBus.getDefault().register(this);

        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();
        //EventBus.getDefault().unregister(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INT_CHAT_BOX) {

            adapter.getData().get(1).setNotify(0);
            adapter.update(1, adapter.getData().get(1));

        }
    }

    /*// This method will be called when a Notification is posted (in the UI thread for Toast)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NotifyDataNotificationEvent event) {

        if (event.message.equals("update_badges")) {
            Item notifItem = adapter.getData().get(5);
            notifItem.setNotify(Notification.notificationsUnseen);
            adapter.update(5, notifItem);
        }

    }*/

    @SuppressLint("ResourceAsColor")
    @Override
    public void itemClicked(View view, int position) {


        MainFragment mf = (MainFragment) getFragmentManager().findFragmentByTag(MainFragment.TAG);

        Item item = adapter.getData().get(position);
        if (item != null) {
            switch (item.getID()) {
                case Menu.HOME_ID:

                    if (mDrawerLayout != null)
                        mDrawerLayout.closeDrawers();

                    mf.setCurrentFragment(0);

                    break;
                case Menu.CAT_ID:

                    if (mDrawerLayout != null)
                        mDrawerLayout.closeDrawers();

                    startActivity(new Intent(getActivity(), CategoriesActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);


                    break;
                case Menu.PEOPLE_AROUND_ME:

                    if (SessionsController.isLogged()) {
                        startActivity(new Intent(getActivity(), ListUsersActivity.class));
                        getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);
                    }

                    break;
                case Menu.CHAT_LOGIN_ID:

                    if (!SessionsController.isLogged()) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }

                    break;
                /*case Menu.NOTIFICATIONS:

                    startActivity(new Intent(getActivity(), NotificationActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    break;*/
                case Menu.FAV:

                    startActivity(new Intent(getActivity(), FavoriteStoreActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    break;
                case Menu.MY_EVENT:

                    startActivity(new Intent(getActivity(), FavoriteEventsActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    break;
                case Menu.EDIT:

                    if (mDrawerLayout != null)
                        mDrawerLayout.closeDrawers();

                    startActivity(new Intent(getActivity(), EditProfileActivity.class));

                    break;
                case Menu.ABOUT:

                    if (mDrawerLayout != null)
                        mDrawerLayout.closeDrawers();

                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    break;
                case Menu.ORDERS:

                    startActivity(new Intent(getActivity(), OrdersActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    break;
                case Menu.SETTING:

                    if (mDrawerLayout != null)
                        mDrawerLayout.closeDrawers();

                    startActivity(new Intent(getActivity(), SettingActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    break;
                case Menu.MAP_STORES:

//                    if(mDrawerLayout!=null)
//                        mDrawerLayout.closeDrawers();

                    startActivity(new Intent(getActivity(), MapStoresListActivity.class));
                    getActivity().overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);

                    break;
                case Menu.LOGOUT:

                    SessionsController.logOut();
                   session.setCard("");
                    getActivity().finish();
                    startActivity(new Intent(getActivity(), SplashActivity.class));

                    break;

                case Menu.WEB_DASHBOARD:

                    if (!AppConfig.ENABLE_WEB_DASHBOARD)
                        break;

                    if (mDrawerLayout != null)
                        mDrawerLayout.closeDrawers();

                    String url = AppConfig.BASE_URL + "/webdashboard/";

                    CookieManager.getInstance().setAcceptCookie(true);

                    new AwesomeWebView.Builder(getActivity())
                            .webViewCookieEnabled(true)
                            .showMenuOpenWith(false)
                            .fileChooserEnabled(true)
                            .iconDefaultColor(R.color.colorBackground)
                            .statusBarColorRes(R.color.colorPrimaryDark)
                            .theme(R.style.FinestWebViewAppTheme)
                            .titleColor(
                                    ResourcesCompat.getColor(getResources(), R.color.defaultWhiteColor, null)
                            ).urlColor(
                            ResourcesCompat.getColor(getResources(), R.color.defaultWhiteColor, null)
                    ).show(url);


                    break;


            }
        }


    }

    private static class Menu {
        static final int HOME_ID = 1;
        static final int CAT_ID = 2;
        static final int PEOPLE_AROUND_ME = 3;
        static final int CHAT_LOGIN_ID = 4;
        static final int NOTIFICATIONS = 5;
        static final int MY_EVENT = 6;
        static final int EDIT = 7;
        static final int ABOUT = 8;
        static final int CREATE_STORE = 9;
        static final int SETTING = 10;
        static final int LOGOUT = 11;
        static final int WEB_DASHBOARD = 12;
        static final int MAP_STORES = 13;
        static final int FAV = 14;
        static final int ORDERS = 15;

    }


}
