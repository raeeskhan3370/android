package com.clubecerto.apps.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.AppController;
import com.clubecerto.apps.app.GPS.GPStracker;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.appconfig.Constances;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.controllers.sessions.GuestController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.UserParser;
import com.clubecerto.apps.app.parser.tags.Tags;
import com.clubecerto.apps.app.utils.MessageDialog;
import com.clubecerto.apps.app.utils.Translator;
import com.clubecerto.apps.app.utils.Utils;
import com.clubecerto.apps.app.views.CustomDialog;
import com.github.ybq.android.spinkit.SpinKitView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.login)
    MaterialEditText login;
    @BindView(R.id.password)
    MaterialEditText password;
    @BindView(R.id.connect)
    Button connect;
    @BindView(R.id.forgotpassword)
    TextView forgotpassword;
    @BindView(R.id.signup)
    Button signup;
    @BindView(R.id.progressBar)
    SpinKitView progressBar;


    //init request http
    private RequestQueue queue;
    private CustomDialog mDialogError;


    private ProgressDialog mPdialog;
    private GPStracker gps;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


        gps = new GPStracker(this);

        if (SessionsController.isLogged()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        AppController application = (AppController) getApplication();
        if (SessionsController.isLogged()) {
            if (getIntent().hasExtra("direction"))
                if (getIntent().getExtras().get("direction") != null)
                    startActivity(new Intent(LoginActivity.this, (Class<?>) getIntent().getExtras().get("direction")));
            finish();
        }

        queue = VolleySingleton.getInstance(this).getRequestQueue();

        signup.setOnClickListener(this);
        connect.setOnClickListener(this);
        forgotpassword.setText(Html.fromHtml(forgotpassword.getText().toString()));

        //Utils.setFont(.+);
        //Utils.setFont(.+);

        //Utils.setFont(.+);
        //Utils.setFont(.+);
        //Utils.setFont(.+);


        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AwesomeWebView.Builder(LoginActivity.this)
                        .statusBarColorRes(R.color.colorAccent)
                        .theme(R.style.FinestWebViewAppTheme)
                        .show(Constances.BASE_URL + "/fpassword");

            }
        });


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.signup) {

            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            overridePendingTransition(R.anim.lefttoright_enter, R.anim.lefttoright_exit);
            finish();

        } else if (v.getId() == R.id.connect) {

            if (ServiceHandler.isNetworkAvailable(this)) {
                doLogin();
            } else {
                ServiceHandler.showSettingsAlert(this);
            }
        }

    }

    private void doLogin() {

        signup.setEnabled(false);

        mPdialog = new ProgressDialog(this);
        mPdialog.setMessage("Loading ...");
        mPdialog.show();

        final double lat = gps.getLatitude();
        final double lng = gps.getLongitude();


        int guest_id = 0;

        if (GuestController.isStored())
            guest_id = GuestController.getGuest().getId();


        final int finalGuest_id = guest_id;
        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                signup.setEnabled(true);

                mPdialog.dismiss();

                try {

                    if (APP_DEBUG) {
                        Log.e("response", response);
                    }

                    JSONObject js = new JSONObject(response);
                    UserParser mUserParser = new UserParser(js);

                    int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1) {

                        List<User> list = mUserParser.getUser();


                        if (list.size() > 0) {

                            SessionsController.createSession(list.get(0));

                            if (SessionsController.isLogged())
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));

                        }


                    } else {


                        Map<String, String> errors = mUserParser.getErrors();

                        MessageDialog.newDialog(LoginActivity.this).onCancelClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MessageDialog.getInstance().hide();
                            }
                        }).onOkClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                MessageDialog.getInstance().hide();
                            }
                        }).setContent(Translator.print(getString(R.string.authentification_error_msg), "Message error")).show();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    MessageDialog.newDialog(LoginActivity.this).onCancelClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MessageDialog.getInstance().hide();
                        }
                    }).onOkClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            MessageDialog.getInstance().hide();
                        }
                    }).setContent(Translator.print(getString(R.string.authentification_error_msg), "Message error (Parser)")).show();


                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }

                signup.setEnabled(true);

                mPdialog.dismiss();

                Map<String, String> errors = new HashMap<String, String>();

                errors.put("NetworkException:", getString(R.string.check_network));
                mDialogError = showErrors(errors);
                mDialogError.setTitle(getString(R.string.network_error));

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("token", Utils.getToken(getBaseContext()));
                params.put("mac_adr", ServiceHandler.getMacAddr());
                params.put("password", password.getText().toString());
                params.put("login", login.getText().toString());

                params.put("lat", String.valueOf(lat));
                params.put("lng", String.valueOf(lng));
                params.put("guest_id", String.valueOf(finalGuest_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    public CustomDialog showErrors(Map<String, String> errors) {
        final CustomDialog dialog = new CustomDialog(this);

        dialog.setContentView(R.layout.fragment_dialog_costum);
        dialog.setCancelable(false);


        String text = "";
        for (String key : errors.keySet()) {
            if (!text.equals(""))
                text = text + "<br>";


            text = text + "#" + errors.get(key);
        }

        Button ok = dialog.findViewById(R.id.ok);
        Button cancel = dialog.findViewById(R.id.cancel);

        TextView msgbox = dialog.findViewById(R.id.msgbox);

        if (!text.equals("")) {
            msgbox.setText(Html.fromHtml(text));
        }
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cancel.setVisibility(View.GONE);
        dialog.show();

        return dialog;

    }


    @Override
    public void onBackPressed() {

        if (mDialogError != null) {

            if (mDialogError.isShowing()) {
                mDialogError.dismiss();
            } else {

                super.onBackPressed();

                if (!MainActivity.isOpend()) {
                    overridePendingTransition(R.anim.righttoleft_enter, R.anim.righttoleft_exit);
                    Intent i = new Intent(LoginActivity.this, SplashActivity.class);
                    i.putExtra("loaded", true);
                    startActivity(i);
                }

            }

        } else {

            if (!MainActivity.isOpend()) {
                overridePendingTransition(R.anim.righttoleft_enter, R.anim.righttoleft_exit);
                Intent i = new Intent(LoginActivity.this, SplashActivity.class);
                i.putExtra("loaded", true);
                startActivity(i);
            }

            super.onBackPressed();

        }


    }


}
