package com.clubecerto.apps.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.clubecerto.apps.app.R;
import com.clubecerto.apps.app.adapter.lists.CompaniesAapter;
import com.clubecerto.apps.app.classes.Companies;
import com.clubecerto.apps.app.controllers.sessions.GuestController;
import com.clubecerto.apps.app.load_manager.ViewManager;
import com.clubecerto.apps.app.network.VolleySingleton;
import com.clubecerto.apps.app.network.api_request.SimpleRequest;
import com.clubecerto.apps.app.utils.MessageDialog;
import com.clubecerto.apps.app.utils.Session;
import com.clubecerto.apps.app.utils.Translator;
import com.clubecerto.apps.app.views.CustomDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

import static com.clubecerto.apps.app.appconfig.AppConfig.APP_DEBUG;

public class CompaniesActivity extends AppCompatActivity implements ViewManager.CustomView,CompaniesAapter.MyViewHolder.OnItemSkillClickListener{

    RecyclerView listcompanies;
    ArrayList<Companies>arrayList=new ArrayList<>();
    CompaniesAapter companiesAapter;
    private RequestQueue queue;
    private CustomDialog mDialogError;
    private Session session;


    private ProgressDialog mPdialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companies);
        ButterKnife.bind(this);
        listcompanies=findViewById(R.id.listcompanies);
        session = new Session(getApplicationContext());
        arrayList= (ArrayList<Companies>) getIntent().getSerializableExtra("companies");
        listcompanies.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        companiesAapter=new CompaniesAapter(CompaniesActivity.this,arrayList,this::OnItemSkillClickListener);
        listcompanies.setAdapter(companiesAapter);
        queue = VolleySingleton.getInstance(this).getRequestQueue();



    }

    @Override
    public void customErrorView(View v) {

    }

    @Override
    public void customLoadingView(View v) {

    }

    @Override
    public void customEmptyView(View v) {

    }

    @Override
    public void OnItemSkillClickListener(View view, int position, long id, int viewType) {
        Companies companies=arrayList.get(position);
       String comp_id= companies.getCodigo();
        String frente= companies.getFrente();
        String verso= companies.getVerso();
        String CorBackgroundCabecalho= companies.getCorBackgroundCabecalho();
        if (comp_id!=null)
        {
            setCompany(comp_id,frente,verso,CorBackgroundCabecalho);
        }

    }

    private void setCompany(String c_id,String Frente,String Verso,String CorBackgroundCabecalho) {



        mPdialog = new ProgressDialog(this);
        mPdialog.setMessage("Loading ...");
        mPdialog.show();




        int guest_id = 0;

        if (GuestController.isStored())
            guest_id = GuestController.getGuest().getId();


        final int finalGuest_id = guest_id;
        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                "https://www.clubecerto.com.br/ws/api/2/empresa.php?user=aplicativo&token=app@cbc31871certo&empresa="+c_id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {



                mPdialog.dismiss();

                try {

                    if (APP_DEBUG) {
                        Log.e("response", response);
                    }

//                    JSONObject js = new JSONObject(response);

//                    String success=   js.getString("success");
//                    if (success.equals(getResources().getString(R.string.TRUE)))
//                    {
                        session.setCard(Frente);
                    session.setCardBack(Verso);
                    session.setCorBackgroundCabecalho(CorBackgroundCabecalho);
                        Intent intent=new Intent(CompaniesActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
//                    }
//                        JSONArray jsonArray=js.getJSONArray("Usuario");
//                        for (int i=0;i<jsonArray.length();i++)
//                        {
//                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
//
//                            String status=  jsonObject2.getString("Ativo");
//                            if (status.equals("1"))
//                            {
//                                JSONObject object=new JSONObject(jsonObject2.getString("Empresa"));
//                                Companies companies;
//                                arrayList.add(companies=new Companies(object.getString("Codigo"),object.getString("Nome"),
//                                        object.getString("Imagem"),object.getString("Tipo"),object.getString("CorTextoCartaoVirtual")
//                                        ,object.getString("CorBackgroundCabecalho"),object.getString("CorItensCabecalho"),object.getString("Frente"),
//                                        object.getString("Verso"),  status ));
//                            }
//
//
//
//
//                        }
//                        if (arrayList.size()!=0)
//                        {
//                            Intent intent=new Intent(CompaniesActivity.this,CompaniesActivity.class);
//                            intent.putExtra("companies",arrayList);
//                            startActivity(intent);
//                            finish();
//                        }
//                    }




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


                } catch (Exception e) {
                    e.printStackTrace();

//                    MessageDialog.newDialog(CompaniesActivity.this).onCancelClick(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            MessageDialog.getInstance().hide();
//                        }
//                    }).onOkClick(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                            MessageDialog.getInstance().hide();
//                        }
//                    }).setContent(Translator.print(getString(R.string.retry), "Message error (Parser)")).show();


                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }



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
}
