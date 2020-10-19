package com.clubecerto.apps.app.load_manager;

import android.content.Context;
import android.view.View;


public class ViewManager {


    private View error;
    private View loading;
    private View noLoading;
    private View empty;
    private CustomView mCustomView;

    public ViewManager(Context context) {
    }

    public void setEmpty(View empty) {
        this.empty = empty;
    }

    public void error() {

        if (checkAll()) {
            this.empty.setVisibility(View.GONE);
            this.noLoading.setVisibility(View.GONE);
            this.loading.setVisibility(View.GONE);
            this.error.setVisibility(View.VISIBLE);
        }

    }

    public void showResult() {
        if (checkAll()) {
            this.noLoading.setVisibility(View.VISIBLE);
            this.loading.setVisibility(View.GONE);
            this.error.setVisibility(View.GONE);
            this.empty.setVisibility(View.GONE);
        }
    }

    public void loading() {
        if (checkAll()) {
            this.noLoading.setVisibility(View.GONE);
            this.loading.setVisibility(View.VISIBLE);
            this.error.setVisibility(View.GONE);
            this.empty.setVisibility(View.GONE);
        }
    }


    public void empty() {
        if (checkAll()) {
            this.noLoading.setVisibility(View.GONE);
            this.loading.setVisibility(View.GONE);
            this.error.setVisibility(View.GONE);
            this.empty.setVisibility(View.VISIBLE);
        }
    }

    public boolean checkAll() {

        if (error == null) {

            new ManagerException("View error is null");
            return false;
        } else if (loading == null) {


            new ManagerException("View loading is null");
            return false;
        } else if (noLoading == null) {


            new ManagerException("View noloading is null");

            return false;
        } else if (empty == null) {

            new ManagerException("View empty is null");
            return false;
        }


        return true;
    }


    public void setErrorLayout(View error) {
        this.error = error;
    }

    public void setLoadingLayout(View loading) {
        this.loading = loading;
    }

    public void setResultLayout(View noLoading) {
        this.noLoading = noLoading;
    }


    public void setCustumizeView(CustomView cv) {
        mCustomView = cv;
        if (checkAll()) {
            if (mCustomView != null) {
                mCustomView.customErrorView(this.error);
                mCustomView.customLoadingView(this.loading);
                mCustomView.customEmptyView(this.empty);
            }
        }

    }


    public interface CustomView {
        void customErrorView(View v);

        void customLoadingView(View v);

        void customEmptyView(View v);
    }

    class ManagerException extends Exception {
        public ManagerException(String msg) {
            super(msg);
        }
    }

}
