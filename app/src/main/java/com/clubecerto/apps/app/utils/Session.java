package com.clubecerto.apps.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    // Shared preferences file name
    private static final String PREF_NAME = "snow-intro-slider";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String CARD = "card";
    private static final String CPF = "cpf";
    private static final String CPF_Company = "cpf_company";
    private static final String NOME = "nome";
    private static final String CARD_BACK = "card_back";
    private static final String CORBackgroundCabecalho = "CorBackgroundCabecalho";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    // shared pref mode
    int PRIVATE_MODE = 0;

    public Session(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }
    public void setCard(String card) {
        editor.putString(CARD, card);
        editor.commit();
    }
    public String getCard() {
        return pref.getString(CARD, null);
    }

    public void setCardBack(String card) {
        editor.putString(CARD_BACK, card);
        editor.commit();
    }
    public String getCardBack() {
        return pref.getString(CARD_BACK, null);
    }


    public void setCorBackgroundCabecalho(String CorBackgroundCabecalho) {
        editor.putString(CORBackgroundCabecalho, CorBackgroundCabecalho);
        editor.commit();
    }
    public String getCorBackgroundCabecalho() {
        return pref.getString(CORBackgroundCabecalho, null);
    }

    public void setNome(String nome) {
        editor.putString(NOME, nome);
        editor.commit();
    }
    public String getNome() {
        return pref.getString(NOME, null);
    }

    public void setCPF(String cpf) {
        editor.putString(CPF, cpf);
        editor.commit();
    }
    public String getCPF() {
        return pref.getString(CPF, null);
    }

    public void setCPFCompany(String cpf) {
        editor.putString(CPF_Company, cpf);
        editor.commit();
    }
    public String getCPFCompany() {
        return pref.getString(CPF_Company, null);
    }


}
