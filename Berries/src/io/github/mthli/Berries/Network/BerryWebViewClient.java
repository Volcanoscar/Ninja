package io.github.mthli.Berries.Network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.webkit.*;

public class BerryWebViewClient extends WebViewClient {
    private Context context;

    public BerryWebViewClient(Context context) {
        this.context = context;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // TODO
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // TODO
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        // TODO
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO
        return false;
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        // TODO
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        // TODO
    }

    @Override
    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
        // TODO
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        // TODO
        return null;
    }
}