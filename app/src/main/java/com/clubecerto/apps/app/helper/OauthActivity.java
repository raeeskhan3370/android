package com.clubecerto.apps.app.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.clubecerto.apps.app.classes.User;
import com.wuadam.awesomewebview.AwesomeWebView;
import com.wuadam.awesomewebview.AwesomeWebViewActivity;
import com.wuadam.awesomewebview.listeners.BroadCastManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class OauthActivity extends AwesomeWebViewActivity {


    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }


    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewNotifs(User bus) {
        if (bus != null) {
            finish();
        }

    }

    public class WebViewClient extends AwesomeWebViewActivity.MyWebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (url.contains("/payment_done")) {
                finish();
            } else if (url.contains("/payment_error")) {
                finish();

            }


        }
    }


    protected WebViewClient buildWebViewClient() {
        return new WebViewClient();
    }

    public static class Builder extends AwesomeWebView.Builder {

        private Context context;


        public Builder(@NonNull Activity activity) {
            super(activity);
            this.context = activity;
        }

        public Builder(@NonNull Context context) {
            super(context);
            this.context = context;

        }

        @Override
        public void load(String data) {
            this.load(data, "text/html", "UTF-8");
        }

        @Override
        public void load(String data, String mimeType, String encoding) {
            this.mimeType = mimeType;
            this.encoding = encoding;
            this.show((String) null, data);
        }

        @Override
        public void show(@NonNull String url) {
            this.show(url, (String) null);
        }

        @Override
        public void show(String url, String data) {
            this.url = url;
            this.data = data;
            this.key = System.identityHashCode(this);

            if (!listeners.isEmpty()) new BroadCastManager(context, key, listeners);

            Intent intent = new Intent(context, OauthActivity.class);
            intent.putExtra("builder", this);

            context.startActivity(intent);

            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(animationOpenEnter, animationOpenExit);
            }
        }
    }


    public static class WebViewListener2 {

        public void onPageStarted(Activity activity, String url) {
            if (url.contains("/payment_done")) {
                activity.finish();
            } else if (url.contains("/payment_error")) {
                activity.finish();

            }

        }

    }
}
