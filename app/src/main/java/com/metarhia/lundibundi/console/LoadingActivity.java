package com.metarhia.lundibundi.console;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoadingActivity extends AppCompatActivity {

    @BindView(R.id.webView) WebView mWebView;
    @BindView(R.id.loading_main) ViewGroup mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ButterKnife.bind(this);

        final String url = "http://loli.dance/";
        if (mWebView.getUrl() == null || !mWebView.getUrl().equals(url)) {
            mWebView.setWebChromeClient(new WebChromeClient());
            mWebView.loadUrl(url);
        }

        ProgressDialog.show(this, null, "Waiting for configuration", true, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            mWebView.destroy();
        }
    }
}
