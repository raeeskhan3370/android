package com.clubecerto.apps.app.customview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.clubecerto.apps.app.R;

import im.delight.android.webview.AdvancedWebView;

public class AdvancedWebViewActivity extends AppCompatActivity implements AdvancedWebView.Listener {

    private AdvancedWebView mWebView;
    private String link;
    private Toolbar toolbar;
    private TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advanced_web_view);


        initToolbar();

        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        mWebView.setListener(this, this);

        if (getIntent().hasExtra("plink")) {
            mWebView.loadUrl(getIntent().getExtras().getString("plink"));
        } else {
            Toast.makeText(this, getString(R.string.error_try_later), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    public void initToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toolbar_title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        toolbar_title.setText(R.string.checkout);


        toolbar.findViewById(R.id.toolbar_description).setVisibility(View.GONE);

        //show loading progess
        toolbar.findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
            overridePendingTransition(R.anim.righttoleft_enter, R.anim.righttoleft_exit);
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) {
            return;
        }
        // ...
        super.onBackPressed();

        overridePendingTransition(R.anim.righttoleft_enter, R.anim.righttoleft_exit);

    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
    }

    @Override
    public void onPageFinished(String url) {


        if (url.contains("/payment_done")) {

            //update toolbar status
            toolbar.findViewById(R.id.progressLayout).setVisibility(View.GONE);

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", "payment_done");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else if (url.contains("/payment_error")) {

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", "payment_error");
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
    }

    @Override
    public void onExternalPageRequest(String url) {
    }
}
