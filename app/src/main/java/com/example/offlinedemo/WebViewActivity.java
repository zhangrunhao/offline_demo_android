package com.example.offlinedemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import wendu.dsbridge.DWebView;

public class WebViewActivity extends AppCompatActivity {
    private DWebView dwebView;

    @Override
    public  void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        dwebView = findViewById(R.id.dwebview);
        dwebView.addJavascriptObject(new JsApi(), null);

        dwebView.loadUrl("http://10.2.153.102:6622/offline_demo_fe/index.html");
        dwebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // 开始加载
            }

            @Override
            public  void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 加载完成
            }
        });
    }

    public void reloadUrl() {
        dwebView.reload();
    }
}