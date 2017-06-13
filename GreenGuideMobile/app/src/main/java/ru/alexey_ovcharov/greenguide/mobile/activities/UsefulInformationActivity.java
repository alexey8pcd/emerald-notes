package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class UsefulInformationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useful_information);
        WebView webView = (WebView) findViewById(R.id.aUsefulInfo_wvInfo);
        String url = "file:///android_asset/Альтернативные источники энергии.html";
        webView.setWebViewClient(new MyWebView(url));
        AssetManager am = getAssets();
        try {
            webView.getSettings().setBuiltInZoomControls(true);
            webView.loadUrl(url);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
    }

    private class MyWebView extends WebViewClient {

        private String currentUrl;

        public MyWebView(String currentUrl) {
            this.currentUrl = currentUrl;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals(currentUrl)) {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.d(APP_NAME, "onReceivedError :" + error);
        }
    }
}
