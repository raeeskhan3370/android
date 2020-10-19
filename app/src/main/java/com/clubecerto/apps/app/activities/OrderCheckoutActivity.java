package com.clubecerto.apps.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.Services.GenericNotifyEvent;
import com.clubecerto.apps.app.appconfig.AppContext;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.CF;
import com.clubecerto.apps.app.classes.Offer;
import com.clubecerto.apps.app.controllers.SettingsController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.controllers.stores.OffersController;
import com.clubecerto.apps.app.customview.AdvancedWebViewActivity;
import com.clubecerto.apps.app.fragments.orderFrags.ConfirmationFragment;
import com.clubecerto.apps.app.fragments.orderFrags.OrderInfoFragment;
import com.clubecerto.apps.app.fragments.orderFrags.PaymentFragment;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;
import static com.clubecerto.apps.app.security.Security.newInstance;

public class OrderCheckoutActivity extends AppCompatActivity {


    //checkout navigation fields
    private enum State {ORDER, PAYMENT, CONFIRMATION}

    State[] array_state = new State[]{State.ORDER, State.CONFIRMATION, State.PAYMENT};
    private View line_first, line_second;
    private ImageView image_shipping, image_payment, image_confirm;
    private TextView tv_shipping, tv_payment, tv_confirm;
    private int idx_state = 0;


    private Offer mOffer;
    private int offer_id = -1;


    //init static params
    public static HashMap<String, String> orderFields;
    public static int order_id = -1;

    public static int PAYMENT_CALLBACK_CODE = 2020;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_checkout_step);
        initToolbar();
        initComponent();


        if (getIntent().hasExtra("offer_id")) {
            offer_id = getIntent().getIntExtra("offer_id", -1);
            mOffer = OffersController.findOfferById(offer_id);
            if (mOffer != null) {
                //disable payment if the modules is disabled or the offer price equal to 0
                if (!SettingsController.isModuleEnabled("order_payment") || mOffer.getOffer_value() == 0 || mOffer.getValue_type().equalsIgnoreCase("Percent")) {
                    disablePaymentModule();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Something went wrong, please try later!", Toast.LENGTH_LONG).show();
                finish();
            }

        }


        if (getIntent().hasExtra("fragmentToOpen") && getIntent().hasExtra("order_id")) {
            if (getIntent().getExtras().getString("fragmentToOpen").equals("fragment_payment")) {
                order_id = getIntent().getExtras().getInt("order_id");
                navigateToPaymentFragment();
            }
        } else {
            // display the first fragment as a default page
            displayFragment(State.ORDER);
        }


    }

    private void navigateToPaymentFragment() {
        idx_state = array_state.length - 1;
        buttonStatusChange();
        displayFragment(State.PAYMENT);
    }

    private void disablePaymentModule() {
        //hide payment wizard
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((RelativeLayout) findViewById(R.id.wizard_confirm)).getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);

        findViewById(R.id.wizard_payment).setVisibility(View.GONE);
        //findViewById(R.id.line_second).setVisibility(View.GONE);
        //remove the payment from state list
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            array_state = Arrays.stream(array_state)
                    .filter(obj -> !obj.equals(State.PAYMENT))
                    .toArray(State[]::new);
        }
    }


    private void initComponent() {
        line_first = (View) findViewById(R.id.line_first);
        line_second = (View) findViewById(R.id.line_second);
        image_shipping = (ImageView) findViewById(R.id.image_shipping);
        image_payment = (ImageView) findViewById(R.id.image_payment);
        image_confirm = (ImageView) findViewById(R.id.image_confirm);

        tv_shipping = (TextView) findViewById(R.id.tv_shipping);
        tv_payment = (TextView) findViewById(R.id.tv_payment);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);


        image_payment.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        image_confirm.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);

        (findViewById(R.id.lyt_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check for required field
                if (array_state[idx_state] == State.ORDER && checkRequiredFields(mOffer)) {
                    Toast.makeText(OrderCheckoutActivity.this, getString(R.string.complet_required_fileds), Toast.LENGTH_LONG).show();
                    return;
                }


                if (array_state[idx_state] == State.CONFIRMATION) {
                    submitOrderAPI();
                    //navigate to the next fragment
                    if (idx_state == array_state.length - 1) {
                        return;

                    }
                }


                if (array_state[idx_state] == State.PAYMENT) {

                    if (SettingsController.isModuleEnabled("order_payment") && mOffer.getOffer_value() > 0) {
                        if (PaymentFragment.paymentChoosed == -1) {
                            Toast.makeText(OrderCheckoutActivity.this, "Please , Choose your payment gateway!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        generatePaymentLinkAPi();
                        return;

                    }

                }

                //navigate to the next fragment
                idx_state++;
                displayFragment(array_state[idx_state]);
                buttonStatusChange();

            }
        });

        (findViewById(R.id.lyt_previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idx_state < 1) return;
                idx_state--;
                displayFragment(array_state[idx_state]);

                buttonStatusChange();

            }
        });

    }

    private void showSuccessPage() {

        findViewById(R.id.layout_content).setVisibility(View.GONE);
        findViewById(R.id.layout_done).setVisibility(View.VISIBLE);
        findViewById(R.id.lyt_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // run event to update  the order list
                EventBus.getDefault().postSticky(new GenericNotifyEvent("order_updated"));
                finish();
            }
        });


    }

    private void buttonStatusChange() {
        if (idx_state == array_state.length - 1) {

            if (!SettingsController.isModuleEnabled("order_payment") || mOffer.getOffer_value() == 0 || mOffer.getValue_type().equalsIgnoreCase("Percent")) {
                ((TextView) findViewById(R.id.btn_next)).setText(getString(R.string.confirm_order));
            } else {
                ((TextView) findViewById(R.id.btn_next)).setText(getString(R.string.confirm_payment));
                findViewById(R.id.lyt_previous).setVisibility(View.GONE);
            }

            (findViewById(R.id.arrow_next)).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.btn_next)).setText(getString(R.string.next));
            (findViewById(R.id.arrow_next)).setVisibility(View.VISIBLE);
            (findViewById(R.id.lyt_previous)).setVisibility(View.VISIBLE);

        }

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void displayFragment(State state) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("offer_id", offer_id);

        Fragment fragment = null;


        refreshStepTitle();

        if (state.name().equalsIgnoreCase(State.ORDER.name())) {
            fragment = new OrderInfoFragment();
            fragment.setArguments(bundle);
            tv_shipping.setTextColor(getResources().getColor(R.color.grey_90));
            image_shipping.clearColorFilter();
            line_first.setBackgroundColor(getResources().getColor(R.color.grey));
            line_second.setBackgroundColor(getResources().getColor(R.color.grey));
        } else if (state.name().equalsIgnoreCase(State.CONFIRMATION.name())) {
            fragment = new ConfirmationFragment();
            fragment.setArguments(bundle);
            line_first.setBackgroundColor(getResources().getColor(R.color.colorAccent));

            //when payment is disabled
            if (!SettingsController.isModuleEnabled("order_payment") || mOffer.getOffer_value() == 0 || mOffer.getValue_type().equalsIgnoreCase("Percent")) {
                line_second.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
            image_confirm.clearColorFilter();
            tv_confirm.setTextColor(getResources().getColor(R.color.grey_90));
        } else if (state.name().equalsIgnoreCase(State.PAYMENT.name())) {
            fragment = new PaymentFragment();
            fragment.setArguments(bundle);
            line_first.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            line_second.setBackgroundColor(getResources().getColor(R.color.colorAccent));

            image_shipping.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            image_payment.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            image_confirm.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

            tv_payment.setTextColor(getResources().getColor(R.color.defaultBlackColor));
        }

        if (fragment == null) return;
        fragmentTransaction.replace(R.id.frame_content, fragment);
        fragmentTransaction.commit();
    }

    private void refreshStepTitle() {
        tv_shipping.setTextColor(getResources().getColor(R.color.grey_40));
        tv_payment.setTextColor(getResources().getColor(R.color.grey_40));
        tv_confirm.setTextColor(getResources().getColor(R.color.grey_40));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_setting, menu);
        //Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey));
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        orderFields = null;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkRequiredFields(final Offer mOffer) {

        Boolean result = false;
        if (mOffer != null) {
            for (CF mCF : mOffer.getCf()) {
                if (mCF.getRequired() == 1) {
                    if (!orderFields.containsKey(mCF.getLabel())
                            || (orderFields.containsKey(mCF.getLabel())
                            && orderFields.get(mCF.getLabel()).trim().length() == 0)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }


    private void displayPaymentWebView(final String plink) {


        // Encode data on your side using BASE64
        String cryptedLink = newInstance().encrypt(plink);
        String link = Constances.API.API_PAYMENT_LINK_CALL + "?redirect=" + cryptedLink + "&token=" + SessionsController.getSession().getUser().getToken();

        if (AppContext.DEBUG)
            Log.e("paymentLink", link);


        Intent intent = new Intent(this, AdvancedWebViewActivity.class);
        intent.putExtra("plink", link);
        startActivityForResult(intent, PAYMENT_CALLBACK_CODE);

/*


        AwesomeWebView.Builder webView = new AwesomeWebView.Builder(this);
        webView.showMenuOpenWith(false)
                .statusBarColorRes(R.color.colorAccent)
                .backPressToClose(true)
                .disableIconMenu(true)
                .addWebViewListener(new WebViewListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        super.onProgressChanged(progress);
                    }

                    @Override
                    public void onPageFinished(String url) {
                        super.onPageFinished(url);
                        Log.i("onPageFinished", url);

                        if (url.contains("/payment_done")) {
                            webView.removeWebViewListener(this);

                            Toast.makeText(OrderCheckoutActivity.this, getString(R.string.payment_done), Toast.LENGTH_LONG).show();
                            showSuccessPage();


                        } else if (url.contains("/payment_error")) {
                            Toast.makeText(OrderCheckoutActivity.this, getString(R.string.payment_error), Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onPageCommitVisible(String url) {
                        super.onPageCommitVisible(url);
                        Log.i("onPageCommitVisible", url);

                    }
                })
                .theme(R.style.FinestWebViewAppTheme)
                .titleColor(
                        ResourcesCompat.getColor(getResources(), R.color.colorAccent, null)
                ).urlColor(
                ResourcesCompat.getColor(getResources(), R.color.colorAccent, null)
        ).show(link);
*/


    }

    private void submitOrderAPI() {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        queue = VolleySingleton.getInstance(this).getRequestQueue();
        Gson gson = new Gson();

        final Map<String, String> params = new HashMap<String, String>();

        if (SessionsController.isLogged()) {
            params.put("user_id", SessionsController.getSession().getUser().getId() + "");
        }


        params.put("module", "store");
        params.put("module_id", String.valueOf(mOffer.getStore_id()));

        if (orderFields != null && !orderFields.isEmpty()) {
            String json = gson.toJson(orderFields); // convert hashmaps to json format
            params.put("req_cf_data", json);
        }

        try {
            JSONArray carts = new JSONArray();
            JSONObject cart = new JSONObject();
            cart.put("module", "offer");
            cart.put("module_id", mOffer.getId());
            cart.put("qty", 1);

            if (mOffer.getValue_type().equalsIgnoreCase("Price")) {
                cart.put("amount", String.valueOf(mOffer.getOffer_value()));
            } else {
                cart.put("amount", 0 + "");
            }

            carts.put(cart);
            params.put("cart", carts.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


        //send these params only when payment is enabled
        if (SettingsController.isModuleEnabled("order_payment") && mOffer.getOffer_value() > 0 && mOffer.getValue_type().equalsIgnoreCase("Price")) {
            params.put("user_token", SessionsController.getSession().getUser().getToken());
            params.put("payment_method", String.valueOf(PaymentFragment.paymentChoosed));
        }


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_ORDERS_CREATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jso = new JSONObject(response);
                    int success = jso.getInt("success");
                    if (success == 1) {
                        order_id = jso.getInt("result");
                        if (!SettingsController.isModuleEnabled("order_payment") || mOffer.getOffer_value() == 0 || mOffer.getValue_type().equalsIgnoreCase("Percent")) {
                            showSuccessPage();
                        } else {
                            // displayPaymentWebView(jso.getString("plink"));
                            navigateToPaymentFragment();
                        }


                        //todo : complete this method
                        //Save custom field in shared pref
                        if (orderFields != null && !orderFields.isEmpty()) {

                            int userId = SessionsController.getSession().getUser().getId();
                            int cfId =  mOffer.getCf_id();
                            final SharedPreferences sharedPref = AppController.getInstance()
                                    .getSharedPreferences("savedCF_" + cfId + "_" + userId, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("user_id", userId);
                            editor.putInt("req_cf_id", mOffer.getCf_id());
                            editor.putString("cf", gson.toJson(orderFields));
                            editor.commit();
                        }


                    } else {
                        Toast.makeText(OrderCheckoutActivity.this, getString(R.string.error_try_later), Toast.LENGTH_LONG).show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) Log.e("ERROR", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                if (AppContext.DEBUG)
                    Log.e("orders_params", params.toString());

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    private void generatePaymentLinkAPi() {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        final Map<String, String> params = new HashMap<String, String>();

        if (SessionsController.isLogged()) {
            params.put("user_id", String.valueOf(SessionsController.getSession().getUser().getId()));
            params.put("user_token", String.valueOf(SessionsController.getSession().getUser().getToken()));
        }


        params.put("order_id", String.valueOf(order_id));
        params.put("payment", String.valueOf(PaymentFragment.paymentChoosed));


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_PAYMENT_LINK, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jso = new JSONObject(response);
                    String payment_link = jso.getString("result");
                    if (payment_link != null) {
                        displayPaymentWebView(payment_link);
                    } else {
                        Toast.makeText(OrderCheckoutActivity.this, getString(R.string.error_try_later), Toast.LENGTH_LONG).show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) Log.e("ERROR", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                if (AppContext.DEBUG)
                    Log.e("orders_params", params.toString());

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_CALLBACK_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                showSuccessPage();
            } else {
                Toast.makeText(this, getString(R.string.payment_error), Toast.LENGTH_LONG).show();

            }
        }
    }
}

