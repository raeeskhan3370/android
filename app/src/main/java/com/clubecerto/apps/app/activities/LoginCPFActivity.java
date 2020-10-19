package com.clubecerto.apps.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.clubecerto.apps.app.classes.Companies;
import com.clubecerto.apps.app.classes.Estable;
import com.clubecerto.apps.app.classes.User;
import com.clubecerto.apps.app.controllers.sessions.GuestController;
import com.clubecerto.apps.app.controllers.sessions.SessionsController;
import com.clubecerto.apps.app.network.ServiceHandler;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.parser.api_parser.UserParser;
import com.clubecerto.apps.app.parser.tags.Tags;
import com.clubecerto.apps.app.utils.MessageDialog;
import com.clubecerto.apps.app.utils.Session;
import com.clubecerto.apps.app.utils.Translator;
import com.clubecerto.apps.app.utils.Utils;
import com.clubecerto.apps.app.views.CustomDialog;
import com.github.ybq.android.spinkit.SpinKitView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wuadam.awesomewebview.AwesomeWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class LoginCPFActivity extends AppCompatActivity implements View.OnClickListener {


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
    private Session session;


    private ProgressDialog mPdialog;
    private GPStracker gps;
    ArrayList<Companies>arrayList=new ArrayList<>();

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
        setContentView(R.layout.activity_login_cpf);
        ButterKnife.bind(this);


        gps = new GPStracker(this);
        session = new Session(getApplicationContext());

        if (SessionsController.isLogged()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }




        AppController application = (AppController) getApplication();
        if (SessionsController.isLogged()) {
            if (getIntent().hasExtra("direction"))
                if (getIntent().getExtras().get("direction") != null)
                    startActivity(new Intent(LoginCPFActivity.this, (Class<?>) getIntent().getExtras().get("direction")));
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

                new AwesomeWebView.Builder(LoginCPFActivity.this)
                        .statusBarColorRes(R.color.colorAccent)
                        .theme(R.style.FinestWebViewAppTheme)
                        .show(Constances.BASE_URL + "/fpassword");

            }
        });


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.signup) {

            startActivity(new Intent(LoginCPFActivity.this, SignupActivity.class));
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
        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                "https://www.clubecerto.com.br/ws/api/2/usuarios_login.php?user=aplicativo&token=app@cbc31871certo&cpf="+login.getText(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                signup.setEnabled(true);

                mPdialog.dismiss();

                try {

                    if (APP_DEBUG) {
                        Log.e("response", response);
                    }

                    JSONObject js = new JSONObject(response);



                 String success=   js.getString("success");
                 if (success.equals(getResources().getString(R.string.TRUE)))
                    {


                        JSONArray jsonArray=js.getJSONArray("Usuario");


                        for (int i=0;i<jsonArray.length();i++)
                        {


                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);

                          String status=  jsonObject2.getString("Ativo");
                          if (status.equals("1"))
                          {


                              session.setNome(jsonObject2.getString("Nome"));
                              session.setCPF(jsonObject2.getString("CPF"));

                              JSONObject object=new JSONObject(jsonObject2.getString("Empresa"));
                              Companies companies;

                              arrayList.add(companies=new Companies(object.getString("Codigo"),object.getString("Nome"),
                                      object.getString("Imagem"),object.getString("Tipo"),object.getString("CorTextoCartaoVirtual")
                                      ,object.getString("CorBackgroundCabecalho"),object.getString("CorItensCabecalho"),object.getString("Frente"),
                                      object.getString("Verso"),  status  ));
                          }




                        }
                        if (arrayList.size()!=0)
                        {
                            session.setCPFCompany(login.getText().toString());
                            Intent intent=new Intent(LoginCPFActivity.this,CompaniesActivity.class);
                                  intent.putExtra("companies",arrayList);
                                  startActivity(intent);
                                  finish();
                        }
                    }




//                    UserParser mUserParser = new UserParser(js);
//
//                    int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));
//
//                    if (success == 1) {
//
//                        List<User> list = mUserParser.getUser();
//
//
//                        if (list.size() > 0) {
//
//                            SessionsController.createSession(list.get(0));
//
//                            if (SessionsController.isLogged())
//                                startActivity(new Intent(LoginCPFActivity.this, MainActivity.class));
//
//                        }
//
//
//                    } else {
//
//
//                        Map<String, String> errors = mUserParser.getErrors();
//
//                        MessageDialog.newDialog(LoginCPFActivity.this).onCancelClick(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                MessageDialog.getInstance().hide();
//                            }
//                        }).onOkClick(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//
//                                MessageDialog.getInstance().hide();
//                            }
//                        }).setContent(Translator.print(getString(R.string.authentification_error_msg), "Message error")).show();
//
//                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    MessageDialog.newDialog(LoginCPFActivity.this).onCancelClick(new View.OnClickListener() {
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

//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//
//                params.put("user", Utils.getToken(getBaseContext()));
//                params.put("token", ServiceHandler.getMacAddr());
//                params.put("cpf", password.getText().toString());


//                return params;
//            }

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


            }

        }
        else
        {
            super.onBackPressed();

        }


    }


}
