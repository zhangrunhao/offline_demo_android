package com.example.offlinedemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.packagemanager.OfflinePackageManager;
import com.example.packagemanager.util.Logger;

import java.io.FileInputStream;
import java.io.InputStream;

import wendu.dsbridge.DWebView;

public class WebViewActivity extends AppCompatActivity {
    private DWebView dwebView;

    @Override
    public  void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        dwebView = findViewById(R.id.dwebview);
        dwebView.addJavascriptObject(new JsApi(), null);
        dwebView.setWebContentsDebuggingEnabled(true);
        // 拦截请求, 并更改css
        dwebView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse response = null;
                Uri uri = request.getUrl();
//                String url = request.getUrl().toString();
                String scheme = uri.getScheme();
                String path = uri.getPath().replaceFirst("/", "");
                String packageId = uri.getHost();

                if (TextUtils.equals(scheme, "mini")) {
                    try {
                        response = OfflinePackageManager.getInstance().getWebResponseResource(packageId, path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return response;
            }
        });
        dwebView.loadUrl("mini://meeting/index.html");

        Button btn = (Button) findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadUrl();
            }
        });
    }

    public void reloadUrl() {
        dwebView.reload();
    }
}